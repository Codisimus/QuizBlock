
package QuizBlock;

import org.bukkit.event.server.ServerListener;
import org.bukkit.event.server.PluginEnableEvent;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Checks for plugins whenever one is enabled
 *
 */
public class PluginListener extends ServerListener {
    public PluginListener() { }
    protected static Boolean useOP;

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        //Return if we have already have a permissions plugin
        if (QuizBlock.permissions != null)
            return;

        //Return if PermissionsEx is not enabled
        if (!QuizBlock.pm.isPluginEnabled("PermissionsEx"))
            return;

        //Return if OP permissions will be used
        if (useOP)
            return;

        QuizBlock.permissions = PermissionsEx.getPermissionManager();
        System.out.println("[QuizBlock] Successfully linked with PermissionsEx!");
    }
}