
package QuizBlock;

import java.util.LinkedList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Door;

/**
 * A Quiz is a sendTo Location, and LinkedLists of rightBlocks, wrongBlocks, and doorBlocks
 * If a rightBlock is broken, all doorBlocks vanish for a specific amount of time
 * If a wrongBlock is broken, the Player is teleported to the sendTo Location
 * All blockBreak events are canceled
 * 
 * @author Codisimus
 */
public class Quiz {
    protected String name;
    protected LinkedList<Block> doorBlocks = new LinkedList<Block>();
    protected LinkedList<Block> rightBlocks = new LinkedList<Block>();
    protected Location sendTo;
    protected LinkedList<Block> wrongBlocks = new LinkedList<Block>();
    protected String right = QuizBlock.right;
    protected String wrong = QuizBlock.wrong;

    /**
     * Constructs a new Quiz with the given name and Location
     * 
     * @param name The name of the Quiz
     * @param sendTo The Location the Quiz sends the Player to
     * @return The newly created Quiz
     */
    public Quiz (String name, Location sendTo) {
        this.name = name;
        this.sendTo = sendTo;
    }
    
    /**
     * Opens the door for the given amount of time
     * All doorBlocks are changed to AIR then changed back
     * If the block is a door then it is swung open
     */
    public void open() {
        for (final Block block : doorBlocks)
            //Check for door material
            if (isDoor(block.getType())) {
                //Starts a new thread
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Door door = (Door)block.getState().getData();
                            Block neighbor;
                            if (door.isTopHalf())
                                neighbor = block.getRelative(BlockFace.DOWN);
                            else
                                neighbor = block.getRelative(BlockFace.UP);
                            //Swing door open
                            if (!door.isOpen()) {
                                block.setData((byte)(block.getState().getData().getData()^4));
                                neighbor.setData((byte)(neighbor.getState().getData().getData()^4));
                            }
                            //Sleep for given amount of time
                            Thread.currentThread().sleep(QuizBlock.timeOut);
                            //Swing door shut
                            door = (Door)block.getState().getData();
                            if (door.isOpen()) {
                                block.setData((byte)(block.getState().getData().getData()^4));
                                neighbor.setData((byte)(neighbor.getState().getData().getData()^4));
                            }
                        }
                        catch (Exception e) {
                        }
                    }
                };
                thread.start();
            }
            else {
                //Starts a new thread
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            int type = block.getTypeId();
                            byte data = block.getData();
                            //Changes material to AIR
                            block.setTypeId(0);
                            //Sleep for given amount of time
                            Thread.currentThread().sleep(QuizBlock.timeOut);
                            //Changes material back
                            if (type == 35)
                                block.setTypeIdAndData(type, data, true);
                            else
                                block.setTypeId(type);
                        }
                        catch (Exception e) {
                        }
                    }
                };
                thread.start();
            }
    }
    
    /**
     * Checks if the given Material is a Door
     * 
     * @param target The Material to be checked
     * @return true if the Material is a Door
     */
    protected static boolean isDoor(Material door) {
        if (door.equals(Material.WOOD_DOOR))
            return true;
        else if (door.equals(Material.WOODEN_DOOR))
            return true;
        else if (door.equals(Material.IRON_DOOR))
            return true;
        else if (door.equals(Material.IRON_DOOR_BLOCK))
            return true;
        return false;
    }
}
