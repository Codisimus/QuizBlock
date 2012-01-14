package com.codisimus.plugins.quizblock.listeners;

import com.codisimus.plugins.quizblock.QuizBlock;
import org.bukkit.event.server.ServerListener;
import org.bukkit.event.server.PluginEnableEvent;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Checks for Permission plugins whenever a Plugin is enabled
 * 
 * @author Codisimus
 */
public class PuginListener extends ServerListener {
    public static Boolean useBP;

    /**
     * Finds and links a Permission plugin
     * 
     * @param event The PluginEnableEvent that occurred
     */
    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        //Return if we have already have a permissions plugin
        if (QuizBlock.permissions != null)
            return;

        //Return if PermissionsEx is not enabled
        if (!QuizBlock.pm.isPluginEnabled("PermissionsEx"))
            return;

        //Return if OP permissions will be used
        if (useBP)
            return;

        QuizBlock.permissions = PermissionsEx.getPermissionManager();
        System.out.println("[QuizBlock] Successfully linked with PermissionsEx!");
    }
}