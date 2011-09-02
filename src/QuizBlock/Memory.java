
package QuizBlock;

import java.util.Properties;
import org.bukkit.entity.Player;

/**
 *
 * @author Codisimus
 */
public class Memory {
    private static Properties p = new Properties();
    
    /**
     * Stores the last command the player used
     * 
     * @param player The player who used the command
     * @param msg The msg the player typed
     */
    protected static void set(Player player, String msg) {
        p.setProperty(player.getName(), msg);
    }
    
    /**
     * Returns the last command the player used
     * 
     * @param player The player who used the command
     * @return The last command the player used
     */
    protected static String get(Player player) {
        return p.getProperty(player.getName());
    }
}

