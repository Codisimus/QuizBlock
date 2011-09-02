
package QuizBlock;

import org.bukkit.event.server.ServerListener;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

/**
 * Checks for plugins whenever one is enabled
 *
 */
public class PluginListener extends ServerListener {
    public PluginListener() { }
    protected static Boolean useOP;

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (QuizBlock.permissions == null && !useOP) {
            Plugin permissions = QuizBlock.pm.getPlugin("Permissions");
            if (permissions != null) {
                QuizBlock.permissions = ((Permissions)permissions).getHandler();
                System.out.println("[QuizBlock] Successfully linked with Permissions!");
            }
        }
    }
}