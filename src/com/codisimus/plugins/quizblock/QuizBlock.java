package com.codisimus.plugins.quizblock;

import com.codisimus.plugins.quizblock.QuizBlockCommand.BlockType;
import java.io.*;
import java.util.HashMap;
import java.util.Properties;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;

/**
 * Loads Plugin and manages Data/Permissions
 *
 * @author Codisimus
 */
public class QuizBlock extends JavaPlugin {
    public static PermissionManager permissions;
    public static PluginManager pm;
    public static Server server;
    public static int timeOut;
    public static String permission;
    public static String defaultRightMsg;
    public static String defaultWrongMsg;
    public static String defaultRightCmd;
    public static String defaultWrongCmd;
    private static Properties p;
    public static HashMap<String, Quiz> quizes = new HashMap<String, Quiz>();
    private static String dataFolder;
    static Plugin plugin;

    @Override
    public void onDisable () {
    }

    /**
     * Calls methods to load this Plugin when it is enabled
     *
     */
    @Override
    public void onEnable () {
        server = getServer();
        pm = getServer().getPluginManager();
        plugin = this;
        
        File dir = this.getDataFolder();
        if (!dir.isDirectory())
            dir.mkdir();
        
        dataFolder = dir.getPath();
        
        dir = new File(dataFolder+"/Quizes");
        if (!dir.isDirectory())
            dir.mkdir();
        
        loadSettings();
        loadData();
        
        //Register Events
        pm.registerEvents(new QuizBlockListener(), this);
        if (!QuizBlockListener.breakToUse)
            pm.registerEvents(new QuizBlockClickListener(), this);
        
        //Register the command found in the plugin.yml
        QuizBlockCommand.command = (String)this.getDescription().getCommands().keySet().toArray()[0];
        getCommand(QuizBlockCommand.command).setExecutor(new QuizBlockCommand());
        
        System.out.println("QuizBlock "+this.getDescription().getVersion()+" is enabled!");
    }
    
    /**
     * Loads settings from the config.properties file
     * 
     */
    public void loadSettings() {
        try {
            //Copy the file from the jar if it is missing
            File file = new File(dataFolder+"/config.properties");
            if (!file.exists())
                this.saveResource("config.properties", true);
            
            //Load config file
            p = new Properties();
            FileInputStream fis = new FileInputStream(file);
            p.load(fis);
            
            timeOut = Integer.parseInt(loadValue("AutoCloseTimer")) * 1000;
            QuizBlockListener.useBP = Boolean.parseBoolean(loadValue("UseBukkitPermissions"));
            QuizBlockListener.breakToUse = Boolean.parseBoolean(loadValue("BreakBlocksToActivate"));
            permission = format(loadValue("PermissionMessage"));
            defaultRightMsg = format(loadValue("DefaultRightMessage"));
            defaultWrongMsg = format(loadValue("DefaultWrongMessage"));
            defaultRightCmd = format(loadValue("DefaultRightCommand"));
            defaultWrongCmd = format(loadValue("DefaultWrongCommand"));
        }
        catch (Exception e) {
        }
    }

    /**
     * Loads the given key and prints an error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    private String loadValue(String key) {
        //Print an error if the key is not found
        if (!p.containsKey(key)) {
            System.err.println("[QuizBlock] Missing value for "+key+" in config file");
            System.err.println("[QuizBlock] Please regenerate config file");
        }
        
        return p.getProperty(key);
    }

    /**
     * Returns boolean value of whether the given player has the specific permission
     * 
     * @param player The Player who is being checked for permission
     * @param type The String of the permission, ex. admin
     * @return true if the given player has the specific permission
     */
    public static boolean hasPermission(Player player, String type) {
        //Check if a Permission Plugin is present
        if (permissions != null)
            return permissions.has(player, "quizblock."+type);
        
        //Return Bukkit Permission value
        return player.hasPermission("quizblock."+type);
    }
    
    /**
     * Adds various Unicode characters and colors to a string
     * 
     * @param string The string being formated
     * @return The formatted String
     */
    public static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
    }

    /**
     * Reads save file to load QuizBlock data
     *
     */
    public static void loadData() {
        File[] files = plugin.getDataFolder().listFiles();

        //Organize files
        if (files != null)
            for (File file: files) {
                String name = file.getName();
                if (name.endsWith(".dat")) {
                    File dest = new File(dataFolder+"/Quizes/"+name.substring(0, name.length() - 4)+".properties");
                    file.renameTo(dest);
                }
            }
        
        files = new File(dataFolder+"/Quizes/").listFiles();

        for (File file: files) {
            String name = file.getName();
            if (name.endsWith(".properties")) {
                String quizName = name.substring(0, name.length() - 4);
                try {
                    p.load(new FileInputStream(file));
                    
                    Quiz quiz = new Quiz(quizName, null);
                    
                    String[] location = p.getProperty("Location").split("'");

                    World world = server.getWorld(location[0]);
                    double x = Double.parseDouble(location[1]);
                    double y = Double.parseDouble(location[2]);
                    double z = Double.parseDouble(location[3]);
                    float pitch = Float.parseFloat(location[4]);
                    float yaw = Float.parseFloat(location[5]);

                    quiz.sendTo = new Location(world, x, y, z, pitch, yaw);

                    quiz.setBlocks(BlockType.DOOR, p.getProperty("DoorBlocks"));
                    quiz.setBlocks(BlockType.RIGHT, p.getProperty("RightBlocks"));
                    quiz.setBlocks(BlockType.WRONG, p.getProperty("WrongBlocks"));

                    quiz.rightMessage = p.getProperty("RightMessage");
                    quiz.wrongMessage = p.getProperty("WrongMessage");
                    quiz.rightCommand = p.getProperty("RightCommand");
                    quiz.wrongCommand = p.getProperty("WrongCommand");
                    
                    quizes.put(quizName, quiz);
                }
                catch (Exception loadFailed) {
                    System.err.println("[QuizBlock] Failed to load "+name);
                    loadFailed.printStackTrace();
                }
            }
        }
            
        if (!quizes.isEmpty())
            return;

        File file = new File(dataFolder+"/QuizBlock.save");
        if (!file.exists())
            return;

        System.out.println("[QuizBlock] Loading outdated save file");

        try {
            BufferedReader bReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bReader.readLine()) != null) {
                String[] split = line.split(";");
                String name = split[0];
                Quiz quiz = new Quiz(name, null);
                if (split[1].endsWith("~NETHER"))
                    split[1].replace("~NETHER", "");
                World world = server.getWorld(split[1]);
                if (world != null && !split[1].equals("")) {
                    double x = Double.parseDouble(split[2]);
                    double y = Double.parseDouble(split[3]);
                    double z = Double.parseDouble(split[4]);
                    float pitch = Float.parseFloat(split[5]);
                    float yaw = Float.parseFloat(split[6]);
                    quiz.sendTo = new Location(world, x, y, z, pitch, yaw);
                }
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4) {
                        if (split[i].endsWith("~NETHER"))
                            split[i].replace("~NETHER", "");
                        world = server.getWorld(split[i]);
                        if (world != null) {
                            int x = Integer.parseInt(split[i+1]);
                            int y = Integer.parseInt(split[i+2]);
                            int z = Integer.parseInt(split[i+3]);
                            quiz.doorBlocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4) {
                        if (split[i].endsWith("~NETHER"))
                            split[i].replace("~NETHER", "");
                        world = server.getWorld(split[i]);
                        if (world != null) {
                            int x = Integer.parseInt(split[i+1]);
                            int y = Integer.parseInt(split[i+2]);
                            int z = Integer.parseInt(split[i+3]);
                            quiz.rightBlocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4) {
                        if (split[i].endsWith("~NETHER"))
                            split[i].replace("~NETHER", "");
                        world = server.getWorld(split[i]);
                        if (world != null) {
                            int x = Integer.parseInt(split[i+1]);
                            int y = Integer.parseInt(split[i+2]);
                            int z = Integer.parseInt(split[i+3]);
                            quiz.wrongBlocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                String right = bReader.readLine();
                if (right.contains(";"))
                    quiz.rightMessage = QuizBlock.defaultRightMsg;
                String wrong = bReader.readLine();
                if (wrong.contains(";"))
                    quiz.wrongMessage = QuizBlock.defaultWrongMsg;
                quizes.put(name, quiz);
            }
        }
        catch (Exception loadFailed) {
            System.err.println("[QuizBlock] Failed to load outdated file");
            loadFailed.printStackTrace();
        }
    }

    /**
     * Saves each Quiz
     * 
     */
    public static void saveAll() {
        for (Quiz quiz: quizes.values())
            quiz.save();
    }
    
    /**
     * Writes data to save file
     * Old file is overwritten
     */
    static void save(Quiz quiz) {
        try {
            File file = new File(dataFolder+"/"+quiz.name+".properties");
            if (!file.exists())
                file.createNewFile();
            
            Properties p = new Properties();
            
            p.setProperty("Location", quiz.sendTo.getWorld().getName()+"'"+quiz.sendTo.getX()+"'"+
                    quiz.sendTo.getY()+"'"+quiz.sendTo.getZ()+"'"+quiz.sendTo.getPitch()+"'"+quiz.sendTo.getYaw());
            p.setProperty("DoorBlocks", quiz.blocksToString(BlockType.DOOR));
            p.setProperty("RightBlocks", quiz.blocksToString(BlockType.RIGHT));
            p.setProperty("WrongBlocks", quiz.blocksToString(BlockType.WRONG));
            p.setProperty("RightMessage", quiz.rightMessage);
            p.setProperty("WrongMessage", quiz.wrongMessage);
            p.setProperty("RightCommand", quiz.rightCommand);
            p.setProperty("WrongCommand", quiz.wrongCommand);

            p.store(new FileOutputStream(file), null);
        }
        catch (Exception saveFailed) {
            System.err.println("[QuizBlock] Saving of Quiz "+quiz.name+" Failed!");
            saveFailed.printStackTrace();
        }
    }
    
    /**
     * Returns the Quiz by the given name
     * 
     * @param name The name of the Quiz to be found
     * @return The Quiz by the given name
     */
    public static Quiz findQuiz(String name) {
        return quizes.get(name);
    }
    
    /**
     * Returns the Quiz that the given Block is linked to
     * 
     * @param block The given Block
     * @return The Quiz that the given Block is linked to
     */
    public static Quiz findQuiz(Block block) {
        //Iterate through all Quizes to find the one with the given Block
        for (Quiz quiz: quizes.values())
            if (quiz.doorBlocks.contains(block) || quiz.rightBlocks.contains(block) || quiz.wrongBlocks.contains(block))
                return quiz;
        
        //Return null because the Quiz does not exist
        return null;
    }
}