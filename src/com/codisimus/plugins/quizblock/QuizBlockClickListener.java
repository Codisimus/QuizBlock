package com.codisimus.plugins.quizblock;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Listens for Players activating Quizzes
 *
 * @author Codisimus
 */
public class QuizBlockClickListener implements Listener {

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerInteract (PlayerInteractEvent event) {
        //Return if the Event was arm flailing
        Block block = event.getClickedBlock();
        if (block == null)
            return;
        
        Player player = event.getPlayer();
        CommandSender cs = new QuizBlockCommandSender();
        
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