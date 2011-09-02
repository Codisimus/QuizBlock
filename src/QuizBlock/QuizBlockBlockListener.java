
package QuizBlock;

import java.util.LinkedList;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.Door;

/**
 *
 * @author Codisimus
 */
public class QuizBlockBlockListener extends BlockListener {

    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        if (Quiz.isDoor(block.getType())) {
            LinkedList<Quiz> quizes = SaveSystem.getQuizes();
            for (Quiz quiz : quizes) {
                LinkedList<Block> doorBlocks = quiz.doorBlocks;
                for (Block doorBlock : doorBlocks) {
                    if (Quiz.isDoor(doorBlock.getType()))
                        if (doorBlock.getLocation().equals(block.getLocation()) || areNeighbors(doorBlock, block)) {
                            Door door = (Door)doorBlock.getState().getData();
                            //Allows redstone to close a door but not open it
                            if (!door.isOpen())
                                event.setNewCurrent(event.getOldCurrent());
                        }
                }
            }
        }
    }
    
    @Override
    public void onBlockBreak (final BlockBreakEvent event) {
        Block blockBroke = event.getBlock();
        Player player = event.getPlayer();
        LinkedList<Quiz> quizes = SaveSystem.getQuizes();
        for (Quiz quiz : quizes) {
            if (quiz.rightBlocks.contains(blockBroke)) {
                player.sendMessage(quiz.right);
                event.setCancelled(true);
                quiz.open();
            }
            else if (quiz.doorBlocks.contains(blockBroke))
                event.setCancelled(true);
            else if (quiz.wrongBlocks.contains(blockBroke)) {
                player.sendMessage(quiz.wrong);
                player.teleport(quiz.sendTo);
                event.setCancelled(true);
            }
        }
    }

    /**
     * Returns whether the given Block is above or below the other given Block
     * 
     * @param blockOne The first Block to be compared
     * @param blockTwo The second Block to be compared
     * @return true if the given Block is above or below the other given Block
     */
    protected boolean areNeighbors(Block blockOne, Block blockTwo) {
        int a = blockOne.getX();
        int b = blockOne.getY();
        int c = blockOne.getZ();
        int x = blockTwo.getX();
        int y = blockTwo.getY();
        int z = blockTwo.getZ();
        if (blockOne.getWorld() == blockTwo.getWorld())
            if (a == x && c == z)
                if (b == y+1 || b == y-1)
                    return true;
        return false;
    }
}
