package com.codisimus.plugins.quizblock.listeners;

import com.codisimus.plugins.quizblock.Quiz;
import com.codisimus.plugins.quizblock.QuizBlock;
import java.util.Set;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

/**
 * Listens for Players activating Quizzes
 *
 * @author Codisimus
 */
public class PlayerEventListener extends PlayerListener {

    @Override
    public void onPlayerInteract (PlayerInteractEvent event) {
        //Return if the Event was arm flailing
        Block block = event.getClickedBlock();
        if (block == null)
            return;
        
        Player player = event.getPlayer();
        
        CommandSender cs = new CommandSender() {
            @Override
            public void sendMessage(String string) {
                //System.out.println(string);
            }

            @Override
            public boolean isOp() {
                return true;
            }

            @Override
            public Server getServer() {
                return QuizBlock.server;
            }
            
            @Override
            public String getName() {
                return "QuizBlock";
            }

            @Override
            public boolean isPermissionSet(String string) {
                return true;
            }

            @Override
            public boolean isPermissionSet(Permission prmsn) {
                return true;
            }

            @Override
            public boolean hasPermission(String string) {
                return true;
            }

            @Override
            public boolean hasPermission(Permission prmsn) {
                return true;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, int i) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void removeAttachment(PermissionAttachment pa) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void recalculatePermissions() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Set<PermissionAttachmentInfo> getEffectivePermissions() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setOp(boolean bln) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        for (Quiz quiz: QuizBlock.quizes.values())
            if (!quiz.open)
                if (quiz.rightBlocks.contains(block)) {
                    //Open if the Player has permission to use Quizes
                    if (QuizBlock.hasPermission(player, "use")) {
                        if (!quiz.rightMessage.isEmpty())
                            player.sendMessage(quiz.rightMessage);

                        if (!quiz.rightCommand.isEmpty())
                            QuizBlock.server.dispatchCommand(cs, quiz.rightCommand.replace("<player>", player.getName()));

                        quiz.open();
                    }
                }
                else if (quiz.wrongBlocks.contains(block)) {
                    //Teleport if the Player has permission to use Quizes
                    if (QuizBlock.hasPermission(player, "use")) {
                        if (!quiz.wrongMessage.isEmpty())
                            player.sendMessage(quiz.wrongMessage);

                        if (!quiz.wrongCommand.isEmpty())
                            QuizBlock.server.dispatchCommand(cs, quiz.wrongCommand.replace("<player>", player.getName()));

                        player.teleport(quiz.sendTo);
                    }
                }
                else if (quiz.doorBlocks.contains(block))
                    event.setCancelled(true);
    }
}