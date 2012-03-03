package com.codisimus.plugins.quizblock;

import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.material.Door;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Listens for Players interacting with Quizzes
 *
 * @author Codisimus
 */
public class QuizBlockListener implements Listener {
    static boolean breakToUse;
    static Boolean useBP;
    
    /**
     * Blocks Players from opening linked doors with redstone
     * 
     * @param event The BlockRedstoneEvent that occurred
     */
    @EventHandler
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        //Return if the Block is not a Door
        Block block = event.getBlock();
        Material material = block.getType();
        switch (material) {
            case WOOD_DOOR: break;
            case WOODEN_DOOR: break;
            case IRON_DOOR: break;
            case IRON_DOOR_BLOCK: break;
            default: return;
        }
        
        for (Quiz quiz: QuizBlock.quizes.values()) {
            LinkedList<Block> doorBlocks = quiz.doorBlocks;
            for (Block doorBlock: doorBlocks)
                if (doorBlock.getType().equals(material))
                    if (doorBlock.equals(block)) {
                        Door door = (Door)doorBlock.getState().getData();

                        //Allow redstone to close a door but not open it
                        if (!door.isOpen())
                            event.setNewCurrent(event.getOldCurrent());

                        return;
                    }
        }
    }
    
    /**
     * Activates a Quiz when a linked Block is broken
     * 
     * @param event The BlockBreakEvent that occurred
     */
    @EventHandler
    public void onBlockBreak (BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        for (Quiz quiz: QuizBlock.quizes.values())
            if (quiz.rightBlocks.contains(block)) {
                event.setCancelled(true);
                
                //Open if the Player has permission to use Quizes
                if (breakToUse && QuizBlock.hasPermission(player, "use")) {
                    player.sendMessage(quiz.rightMessage);
                    quiz.open();
                }
            }
            else if (quiz.wrongBlocks.contains(block)) {
                event.setCancelled(true);
                
                //Teleport if the Player has permission to use Quizes
                if (breakToUse && QuizBlock.hasPermission(player, "use")) {
                    player.sendMessage(quiz.wrongMessage);
                    player.teleport(quiz.sendTo);
                }
            }
            else if (quiz.doorBlocks.contains(block))
                event.setCancelled(true);
    }
    
    /**
     * Finds and links a Permission plugin
     * 
     * @param event The PluginEnableEvent that occurred
     */
    @EventHandler (priority = EventPriority.MONITOR)
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

    /**
     * Returns whether the given Block is above or below the other given Block
     * 
     * @param blockOne The first Block to be compared
     * @param blockTwo The second Block to be compared
     * @return true if the given Block is above or below the other given Block
     */
    public boolean areNeighbors(Block blockOne, Block blockTwo) {
        if (blockOne.getX() != blockTwo.getX())
            return false;
        
        if (blockOne.getZ() != blockTwo.getZ())
            return false;
        
        int b = blockOne.getY();
        int y = blockTwo.getY();
        if (b != y + 1 && b != y - 1)
            return false;
        
        return blockOne.getWorld() == blockTwo.getWorld();
    }
}