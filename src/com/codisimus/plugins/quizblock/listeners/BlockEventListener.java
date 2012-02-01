package com.codisimus.plugins.quizblock.listeners;

import com.codisimus.plugins.quizblock.Quiz;
import com.codisimus.plugins.quizblock.QuizBlock;
import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.Door;

/**
 * Listens for illegal redstone events and block breaking
 *
 * @author Codisimus
 */
public class BlockEventListener extends BlockListener {
    public static boolean breakToUse;

    /**
     * Blocks Players from opening linked doors with redstone
     * 
     * @param event The BlockRedstoneEvent that occurred
     */
    @Override
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
    @Override
    public void onBlockBreak (BlockBreakEvent event) {
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