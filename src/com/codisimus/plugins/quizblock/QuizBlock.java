package com.codisimus.plugins.quizblock;

import com.codisimus.plugins.quizblock.listeners.worldListener;
import com.codisimus.plugins.quizblock.listeners.blockListener;
import com.codisimus.plugins.quizblock.listeners.commandListener;
import com.codisimus.plugins.quizblock.listeners.pluginListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;

/**
 * Loads Plugin and manages Permissions
 *
 * @author Codisimus
 */
public class QuizBlock extends JavaPlugin {
    public static PermissionManager permissions;
    public static PluginManager pm;
    public static Server server;
    public static int timeOut;
    public static String permission;
    public static String right;
    public static String wrong;
    public static Properties p;

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
        checkFiles();
        loadConfig();
        SaveSystem.load();
        registerEvents();
        getCommand("quiz").setExecutor(new commandListener());
        System.out.println("QuizBlock "+this.getDescription().getVersion()+" is enabled!");
    }
    
    /**
     * Makes sure all needed files exist
     * 
     */
    public void checkFiles() {
        if (!new File("plugins/QuizBlock/config.properties").exists())
            moveFile("config.properties");
    }
    
    /**
     * Moves file from QuizBlock.jar to appropriate folder
     * Destination folder is created if it doesn't exist
     * 
     * @param fileName The name of the file to be moved
     */
    public void moveFile(String fileName) {
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
    public void loadConfig() {
        p = new Properties();
        try {
            p.load(new FileInputStream("plugins/QuizBlock/config.properties"));
        }
        catch (Exception e) {
        }
        timeOut = Integer.parseInt(loadValue("AutoCloseTimer")) * 1000;
        pluginListener.useBP = Boolean.parseBoolean(loadValue("UseBukkitPermissions"));
        permission = loadValue("PermissionMessage").replaceAll("&", "§");
        right = loadValue("DefaultRightMessage").replaceAll("&", "§");
        wrong = loadValue("DefaultWrongMessage").replaceAll("&", "§");
    }

    /**
     * Loads the given key and prints an error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    public String loadValue(String key) {
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
    public void registerEvents() {
        blockListener blockListener = new blockListener();
        pm.registerEvent(Type.PLUGIN_ENABLE, new pluginListener(), Priority.Monitor, this);
        pm.registerEvent(Type.WORLD_LOAD, new worldListener(), Priority.Monitor, this);
        pm.registerEvent(Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
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
}
