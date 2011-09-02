
package QuizBlock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;

/**
 *
 * @author Codisimus
 */
public class SaveSystem {
    private static LinkedList<Quiz> quizes = new LinkedList<Quiz>();
    private static boolean save = true;

    /**
     * Reads save file to load QuizBlock data
     * Saving is turned off if an error occurs
     */
    protected static void loadFromFile() {
        BufferedReader bReader = null;
        try {
            new File("plugins/QuizBlock").mkdir();
            new File("plugins/QuizBlock/QuizBlock.save").createNewFile();
            bReader = new BufferedReader(new FileReader("plugins/QuizBlock/QuizBlock.save"));
            Server server = QuizBlock.server;
            String line = "";
            while ((line = bReader.readLine()) != null) {
                String[] split = line.split(";");
                String name = split[0];
                Location sendTo = null;
                if (!split[1].equals("")) {
                    if (split[1].endsWith("~NETHER"))
                        split[1].replace("~NETHER", "");
                    World world = server.getWorld(split[1]);
                    double x = Double.parseDouble(split[2]);
                    double y = Double.parseDouble(split[3]);
                    double z = Double.parseDouble(split[4]);
                    float pitch = Float.parseFloat(split[5]);
                    float yaw = Float.parseFloat(split[6]);
                    sendTo = new Location(world, x, y, z, pitch, yaw);
                }
                LinkedList<Block> doorBlocks = new LinkedList<Block>();
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4) {
                        if (split[0].endsWith("~NETHER"))
                            split[0].replace("~NETHER", "");
                        World world = server.getWorld(split[i]);
                        int x = Integer.parseInt(split[i+1]);
                        int y = Integer.parseInt(split[i+2]);
                        int z = Integer.parseInt(split[i+3]);
                        doorBlocks.add(world.getBlockAt(x, y, z));
                    }
                }
                LinkedList<Block> rightBlocks = new LinkedList<Block>();
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4) {
                        if (split[0].endsWith("~NETHER"))
                            split[0].replace("~NETHER", "");
                        World world = server.getWorld(split[i]);
                        int x = Integer.parseInt(split[i+1]);
                        int y = Integer.parseInt(split[i+2]);
                        int z = Integer.parseInt(split[i+3]);
                        rightBlocks.add(world.getBlockAt(x, y, z));
                    }
                }
                LinkedList<Block> wrongBlocks = new LinkedList<Block>();
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4) {
                        if (split[0].endsWith("~NETHER"))
                            split[0].replace("~NETHER", "");
                        World world = server.getWorld(split[i]);
                        int x = Integer.parseInt(split[i+1]);
                        int y = Integer.parseInt(split[i+2]);
                        int z = Integer.parseInt(split[i+3]);
                        wrongBlocks.add(world.getBlockAt(x, y, z));
                    }
                }
                String correct = bReader.readLine();
                if (correct.contains(";"))
                    correct = QuizBlock.right;
                String wrong = bReader.readLine();
                if (wrong.contains(";"))
                    wrong = QuizBlock.wrong;
                Quiz quiz = new Quiz(name, doorBlocks, rightBlocks, sendTo, wrongBlocks, correct, wrong);
                quizes.add(quiz);
            }
        }
        catch (Exception e) {
            save = false;
            System.err.println("[QuizBlock] Load failed, saving turned off to prevent loss of data");
            e.printStackTrace();
        }
    }

    /**
     * Writes data to save file
     * Old file is overwritten
     */
    protected static void save() {
        //cancels if saving is turned off
        if (!save)
            return;
        BufferedWriter bWriter = null;
        try {
            bWriter = new BufferedWriter(new FileWriter("plugins/QuizBlock/QuizBlock.save"));
            for(Quiz quiz : quizes) {
                bWriter.write(quiz.name.concat(";"));
                Location sendTo = quiz.sendTo;
                World world = sendTo.getWorld();
                String name = world.getName();
                if (world.getEnvironment().equals(Environment.NETHER))
                    name = name.concat("~NETHER");
                bWriter.write(name+";");
                bWriter.write(sendTo.getX()+";");
                bWriter.write(sendTo.getY()+";");
                bWriter.write(sendTo.getZ()+";");
                bWriter.write(sendTo.getPitch()+";");
                bWriter.write(sendTo.getYaw()+";");
                bWriter.newLine();
                for (Block block : quiz.doorBlocks) {
                    world = block.getWorld();
                    name = world.getName();
                    if (world.getEnvironment().equals(Environment.NETHER))
                        name = name.concat("~NETHER");
                    bWriter.write(name+";");
                    bWriter.write(block.getX()+";");
                    bWriter.write(block.getY()+";");
                    bWriter.write(block.getZ()+";");
                }
                bWriter.newLine();
                for (Block block : quiz.rightBlocks) {
                    world = block.getWorld();
                    name = world.getName();
                    if (world.getEnvironment().equals(Environment.NETHER))
                        name = name.concat("~NETHER");
                    bWriter.write(name+";");
                    bWriter.write(block.getX()+";");
                    bWriter.write(block.getY()+";");
                    bWriter.write(block.getZ()+";");
                }
                bWriter.newLine();
                for (Block block : quiz.wrongBlocks) {
                    world = block.getWorld();
                    name = world.getName();
                    if (world.getEnvironment().equals(Environment.NETHER))
                        name = name.concat("~NETHER");
                    bWriter.write(name+";");
                    bWriter.write(block.getX()+";");
                    bWriter.write(block.getY()+";");
                    bWriter.write(block.getZ()+";");
                }
                bWriter.newLine();
                bWriter.write(quiz.right);
                bWriter.newLine();
                bWriter.write(quiz.wrong);
                bWriter.newLine();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                bWriter.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the LinkedList of saved Quizzes
     * 
     * @return the LinkedList of saved Quizzes
     */
    protected static LinkedList<Quiz> getQuizes() {
        return quizes;
    }
    
    /**
     * Adds the Quiz to the LinkedList of saved Quizzes
     * 
     * @param quiz The Quiz to be added
     */
    protected static void addQuiz(Quiz quiz) {
        try {
            quizes.add(quiz);
            save();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes the Quiz from the LinkedList of saved Quizzes
     * 
     * @param quiz The Quiz to be removed
     */
    protected static void removeQuiz(Quiz quiz){
        try {
            quiz.doorBlocks.clear();
            quiz.rightBlocks.clear();
            quiz.wrongBlocks.clear();
            quizes.remove(quiz);
            save();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
