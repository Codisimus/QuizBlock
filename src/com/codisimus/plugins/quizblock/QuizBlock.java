package com.codisimus.plugins.quizblock;

import com.codisimus.plugins.quizblock.listeners.CommandListener.BlockType;
import com.codisimus.plugins.quizblock.listeners.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
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
    public static String rightMessage;
    public static String wrongMessage;
    public static String rightCommand;
    public static String wrongCommand;
    private static Properties p;
    public static LinkedList<Quiz> quizes = new LinkedList<Quiz>();
    public static boolean save = true;

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
        loadSettings();
        loadData();
        registerEvents();
        getCommand("quiz").setExecutor(new CommandListener());
        System.out.println("QuizBlock "+this.getDescription().getVersion()+" is enabled!");
    }
    
    /**
     * Moves file from QuizBlock.jar to appropriate folder
     * Destination folder is created if it doesn't exist
     * 
     * @param fileName The name of the file to be moved
     */
    private void moveFile(String fileName) {
        try {
            //Retrieve file from this plugin's .jar
            JarFile jar = new JarFile("plugins/QuizBlock.jar");
            ZipEntry entry = jar.getEntry(fileName);
            
            //Create the destination folder if it does not exist
            String destination = "plugins/QuizBlock/";
            File file = new File(destination.substring(0, destination.length()-1));
            if (!file.exists())
                file.mkdir();
            
            //Copy the file
            File efile = new File(destination, fileName);
            InputStream in = new BufferedInputStream(jar.getInputStream(entry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(efile));
            byte[] buffer = new byte[2048];
            while (true) {
                int nBytes = in.read(buffer);
                if (nBytes <= 0)
                    break;
                out.write(buffer, 0, nBytes);
            }
            
            out.flush();
            out.close();
            in.close();
        }
        catch (Exception moveFailed) {
            System.err.println("[QuizBlock] File Move Failed!");
            moveFailed.printStackTrace();
        }
    }
    
    /**
     * Loads settings from the config.properties file
     * 
     */
    public void loadSettings() {
        if (!new File("plugins/QuizBlock/config.properties").exists())
            moveFile("config.properties");
        
        p = new Properties();
        try {
            p.load(new FileInputStream("plugins/QuizBlock/config.properties"));
            
            timeOut = Integer.parseInt(loadValue("AutoCloseTimer")) * 1000;
            PluginListener.useBP = Boolean.parseBoolean(loadValue("UseBukkitPermissions"));
            BlockEventListener.breakToUse = Boolean.parseBoolean(loadValue("BreakBlocksToActivate"));
            permission = format(loadValue("PermissionMessage"));
            rightMessage = format(loadValue("DefaultRightMessage"));
            wrongMessage = format(loadValue("DefaultWrongMessage"));
            rightCommand = format(loadValue("DefaultRightCommand"));
            wrongCommand = format(loadValue("DefaultWrongCommand"));
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
     * Registers events for the QuizBlock Plugin
     *
     */
    private void registerEvents() {
        BlockEventListener blockListener = new BlockEventListener();
        pm.registerEvent(Type.PLUGIN_ENABLE, new PluginListener(), Priority.Monitor, this);
        pm.registerEvent(Type.WORLD_LOAD, new WorldLoadListener(), Priority.Monitor, this);
        pm.registerEvent(Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        
        if (!BlockEventListener.breakToUse)
            pm.registerEvent(Type.PLAYER_INTERACT, new PlayerEventListener(), Priority.Monitor, this);
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
     * Saving is turned off if an error occurs
     */
    public static void loadData() {
        try {
            File[] files = new File("plugins/QuizBlock").listFiles();

            for (File file: files) {
                String name = file.getName();
                if (name.endsWith(".dat")) {
                    p.load(new FileInputStream(file));
                    
                    String[] location = p.getProperty("Location").split("'");
                    
                    World world = server.getWorld(location[0]);
                    if (world != null) {
                        Quiz quiz = new Quiz(name, null);
                        
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
                    }
                }
            }
            
            if (!quizes.isEmpty())
                return;
            
            File file = new File("plugins/QuizBlock/QuizBlock.save");
            if (!file.exists())
                return;
        
            System.out.println("[QuizBlock] Loading outdated save file");
            
            BufferedReader bReader = new BufferedReader(new FileReader("plugins/QuizBlock/QuizBlock.save"));
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
                    quiz.rightMessage = QuizBlock.rightMessage;
                String wrong = bReader.readLine();
                if (wrong.contains(";"))
                    quiz.wrongMessage = QuizBlock.wrongMessage;
                quizes.add(quiz);
            }
            
            save();
        }
        catch (Exception loadFailed) {
            save = false;
            System.err.println("[QuizBlock] Load failed, saving turned off to prevent loss of data");
            loadFailed.printStackTrace();
        }
    }
    
    /**
     * Reads save file to load QuizBlock data for given World
     * Saving is turned off if an error occurs
     */
    public static void loadData(World world) {
        try {
            new File("plugins/QuizBlock/QuizBlock.save").createNewFile();
            BufferedReader bReader = new BufferedReader(new FileReader("plugins/QuizBlock/QuizBlock.save"));
            String line;
            while ((line = bReader.readLine()) != null) {
                String[] split = line.split(";");
                Quiz quiz = null;
                for (Quiz q: quizes)
                    if (q.name.equals(split[0]))
                        quiz = q;
                if (split[1].equals(world.getName())) {
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
                        if (split[i].equals(world.getName())) {
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
                    for (int i = 0; i < split.length; i = i+4)
                        if (split[i].equals(world.getName())) {
                            int x = Integer.parseInt(split[i+1]);
                            int y = Integer.parseInt(split[i+2]);
                            int z = Integer.parseInt(split[i+3]);
                            quiz.rightBlocks.add(world.getBlockAt(x, y, z));
                        }
                }
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4)
                        if (split[i].equals(world.getName())) {
                            int x = Integer.parseInt(split[i+1]);
                            int y = Integer.parseInt(split[i+2]);
                            int z = Integer.parseInt(split[i+3]);
                            quiz.wrongBlocks.add(world.getBlockAt(x, y, z));
                        }
                }
            }
        }
        catch (Exception loadFailed) {
            save = false;
            System.err.println("[QuizBlock] Load failed, saving turned off to prevent loss of data");
            loadFailed.printStackTrace();
        }
    }

    /**
     * Writes data to save file
     * Old file is overwritten
     */
    public static void save() {
        //Cancel if saving is turned off
        if (!save) {
            System.out.println("[QuizBlock] Warning! Data is not being saved.");
            return;
        }
        
        try {
            p = new Properties();
            
            for (Quiz quiz: quizes) {
                p.setProperty("Location", quiz.sendTo.getWorld().getName()+"'"+quiz.sendTo.getX()+"'"+
                        quiz.sendTo.getY()+"'"+quiz.sendTo.getZ()+"'"+quiz.sendTo.getPitch()+"'"+quiz.sendTo.getYaw());
                p.setProperty("DoorBlocks", quiz.blocksToString(BlockType.DOOR));
                p.setProperty("RightBlocks", quiz.blocksToString(BlockType.RIGHT));
                p.setProperty("WrongBlocks", quiz.blocksToString(BlockType.WRONG));
                p.setProperty("RightMessage", quiz.rightMessage);
                p.setProperty("WrongMessage", quiz.wrongMessage);
                p.setProperty("RightCommand", quiz.rightCommand);
                p.setProperty("WrongCommand", quiz.wrongCommand);

                p.store(new FileOutputStream("plugins/QuizBlock/"+quiz.name+".dat"), null);
            }
        }
        catch (Exception saveFailed) {
            System.err.println("[QuizBlock] Save Failed!");
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
        //Iterate through all Quizes to find the one with the given Name
        for (Quiz quiz: quizes)
            if (quiz.name.equals(name))
                return quiz;
        
        //Return null because the Quiz does not exist
        return null;
    }
    
    /**
     * Returns the Quiz that the given Block is linked to
     * 
     * @param block The given Block
     * @return The Quiz that the given Block is linked to
     */
    public static Quiz findQuiz(Block block) {
        //Iterate through all Quizes to find the one with the given Block
        for (Quiz quiz: quizes)
            if (quiz.doorBlocks.contains(block) || quiz.rightBlocks.contains(block) || quiz.wrongBlocks.contains(block))
                return quiz;
        
        //Return null because the Quiz does not exist
        return null;
    }
}