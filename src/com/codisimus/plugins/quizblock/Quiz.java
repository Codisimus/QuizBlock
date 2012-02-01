package com.codisimus.plugins.quizblock;

import com.codisimus.plugins.quizblock.listeners.CommandListener.BlockType;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.Properties;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
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
    public Location sendTo;
    public LinkedList<Block> doorBlocks = new LinkedList<Block>();
    public LinkedList<Block> rightBlocks = new LinkedList<Block>();
    public LinkedList<Block> wrongBlocks = new LinkedList<Block>();
    public String rightMessage = QuizBlock.defaultRightMsg;
    public String wrongMessage = QuizBlock.defaultWrongMsg;
    public String rightCommand = QuizBlock.defaultRightCmd;
    public String wrongCommand = QuizBlock.defaultWrongCmd;
    public boolean open = false;

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
        if (doorBlocks.isEmpty())
            return;
        
        open = true;
        
        for (final Block block: doorBlocks) {
            //Start a new thread
            Thread thread = new Thread() {
                @Override
                public void run() {
                    BlockState state = block.getState();
                            
                    //Check for door material
                    switch (block.getType()) {
                        case WOOD_DOOR: //Fall through
                        case WOODEN_DOOR: //Fall through
                        case IRON_DOOR: //Fall through
                        case IRON_DOOR_BLOCK:
                            //Convert the Block to a Door
                            Door door = (Door)state.getData();

                            //Get the other half of the Door
                            BlockState stateTopHalf = block.getRelative(BlockFace.UP).getState();
                            Door doorTopHalf = (Door)state.getData();

                            //Open the Door
                            door.setOpen(true);
                            doorTopHalf.setOpen(true);
                            state.update();
                            stateTopHalf.update();
                            
                            //Play Door sound
                            block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 4);

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
                            
                            //Play Door sound
                            block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 4);
                    
                            break;

                        default:
                            //Change material to AIR
                            block.setTypeId(0);

                            long endTime = System.currentTimeMillis() + QuizBlock.timeOut;
                            
                            while (System.currentTimeMillis() < endTime) {
                                //Play smoke effect
                                block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4);
                            
                                //Sleep for 0.1 second
                                try {
                                    Thread.currentThread().sleep(100);
                                }
                                catch (Exception e) {
                                }
                            }

                            //Change material back
                            state.update(true);
                    
                            break;
                    }
                    
                    open = false;
                }
            };
            thread.start();
        }
    }
    
    /**
     * Creates a LinkedList of Blocks from a String
     * 
     * @param type The given type
     * @param data The Location data of the Blocks
     */
    public void setBlocks(BlockType type, String string) {
        LinkedList<Block> blocks = new LinkedList<Block>();
        
        String[] blocksData = string.split(", ");
        for (String data: blocksData) {
            String[] location = data.split("'");
            
            World world = QuizBlock.server.getWorld(location[0]);
            if (world != null) {
                int x = Integer.parseInt(location[1]);
                int y = Integer.parseInt(location[2]);
                int z = Integer.parseInt(location[3]);
                
                blocks.add(world.getBlockAt(x, y, z));
            }
        }
        
        switch (type) {
            case DOOR: doorBlocks = blocks; break;
            case RIGHT: rightBlocks = blocks; break;
            case WRONG: wrongBlocks = blocks; break;
        }
    }
    
    /**
     * Returns the LinkedList of given Blocks as a String
     * 
     * @param type The given type
     * @return the LinkedList of given Blocks as a String
     */
    public String blocksToString(BlockType type) {
        LinkedList<Block> blocks = null;
        switch (type) {
            case DOOR: blocks = doorBlocks; break;
            case RIGHT: blocks = rightBlocks; break;
            case WRONG: blocks = wrongBlocks; break;
        }
        
        String string = "";
        for (Block block: blocks) {
            string = string.concat(block.getWorld().getName()+"'"+
                    block.getX()+"'"+block.getY()+"'"+block.getZ()+", ");
        }
        
        return string;
    }
    
    /**
     * Writes data to save file
     * Old file is overwritten
     */
    public void save() {
        try {
            File file = new File("plugins/QuizBlock/"+name+".dat");
            if (!file.exists())
                file.createNewFile();
            
            Properties p = new Properties();
            
            p.setProperty("Location", sendTo.getWorld().getName()+"'"+sendTo.getX()+"'"+
                    sendTo.getY()+"'"+sendTo.getZ()+"'"+sendTo.getPitch()+"'"+sendTo.getYaw());
            p.setProperty("DoorBlocks", blocksToString(BlockType.DOOR));
            p.setProperty("RightBlocks", blocksToString(BlockType.RIGHT));
            p.setProperty("WrongBlocks", blocksToString(BlockType.WRONG));
            p.setProperty("RightMessage", rightMessage);
            p.setProperty("WrongMessage", wrongMessage);
            p.setProperty("RightCommand", rightCommand);
            p.setProperty("WrongCommand", wrongCommand);

            p.store(new FileOutputStream(file), null);
        }
        catch (Exception saveFailed) {
            System.err.println("[QuizBlock] Saving of Quiz "+name+" Failed!");
            saveFailed.printStackTrace();
        }
    }
}