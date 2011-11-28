package com.codisimus.plugins.quizblock.listeners;

import com.codisimus.plugins.quizblock.Quiz;
import com.codisimus.plugins.quizblock.QuizBlock;
import com.codisimus.plugins.quizblock.SaveSystem;
import java.util.LinkedList;
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
public class blockListener extends BlockListener {

    /**
     * Blocks Players from opening linked doors with redstone
     * 
     * @param event The BlockRedstoneEvent that occurred
     */
    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        //Return if the Block is not a Door
        Block block = event.getBlock();
        if (!Quiz.isDoor(block.getType()))
            return;
        
        for (Quiz quiz: SaveSystem.quizes) {
            LinkedList<Block> doorBlocks = quiz.doorBlocks;
            for (Block doorBlock: doorBlocks)
                if (Quiz.isDoor(doorBlock.getType()))
                    if (doorBlock.equals(block) || areNeighbors(doorBlock, block)) {
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
    public void onBlockBreak (final BlockBreakEvent event) {
        Block blockBroke = event.getBlock();
        Player player = event.getPlayer();
        
        for (Quiz quiz: SaveSystem.quizes)
            if (quiz.rightBlocks.contains(blockBroke)) {
                //Return if the Player does not have permission to use Quizes
                if (QuizBlock.hasPermission(player, "use")) {
                    player.sendMessage(quiz.right);
                    quiz.open();
                }
                
                event.setCancelled(true);
                return;
            }
            else if (quiz.doorBlocks.contains(blockBroke)) {
                event.setCancelled(true);
                return;
            }
            else if (quiz.wrongBlocks.contains(blockBroke)) {
                //Return if the Player does not have permission to use Quizes
                if (QuizBlock.hasPermission(player, "use")) {
                    player.sendMessage(quiz.wrong);
                    player.teleport(quiz.sendTo);
                }
                    
                event.setCancelled(true);
                return;
            }
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