package com.codisimus.plugins.quizblock;

import java.util.LinkedList;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
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
    public String name;
    public LinkedList<Block> doorBlocks = new LinkedList<Block>();
    public LinkedList<Block> rightBlocks = new LinkedList<Block>();
    public Location sendTo;
    public LinkedList<Block> wrongBlocks = new LinkedList<Block>();
    public String right = QuizBlock.right;
    public String wrong = QuizBlock.wrong;

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
        for (final Block block: doorBlocks) {
            //Start a new thread
            Thread thread = new Thread() {
                @Override
                public void run() {
                    //Check for door material
                    switch (block.getType()) {
                        case WOOD_DOOR: //Fall through
                        case WOODEN_DOOR: //Fall through
                        case IRON_DOOR: //Fall through
                        case IRON_DOOR_BLOCK:
                            //Convert the Block to a Door
                            BlockState state = block.getState();
                            Door door = (Door)state.getData();

                            //Get the other half of the Door
                            BlockState stateTopHalf = block.getRelative(BlockFace.UP).getState();
                            Door doorTopHalf = (Door)state.getData();

                            //Open the Door
                            door.setOpen(true);
                            doorTopHalf.setOpen(true);
                            state.update();
                            stateTopHalf.update();

                            //Sleep for predetermined amount of time
                            try {
                                Thread.currentThread().sleep(QuizBlock.timeOut);
                            }
                            catch (Exception e) {
                            }

                            //Close the Door
                            door.setOpen(false);
                            doorTopHalf.setOpen(false);
                            state.update();
                            stateTopHalf.update();
                    
                            break;

                        default:
                            int type = block.getTypeId();
                            byte data = block.getData();

                            //Change material to AIR
                            block.setTypeId(0);

                            //Sleep for given amount of time
                            try {
                                Thread.currentThread().sleep(QuizBlock.timeOut);
                            }
                            catch (Exception e) {
                            }

                            //Change material back
                            block.setTypeIdAndData(type, data, true);
                    
                            break;
                    }
                }
            };
            thread.start();
        }
    }
}
