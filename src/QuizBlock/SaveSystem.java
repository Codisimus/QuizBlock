
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
                Quiz quiz = new Quiz(name, null);
                if (split[1].endsWith("~NETHER"))
                    split[1].replace("~NETHER", "");
                World world = server.getWorld(split[1]);
                if (world != null && !split[1].equals("")) {
                    double x = Double.parseDouble(split[2]);
                    double y = Double.parseDouble(split[3]);
                    double z = Double.parseDouble(split[4]);
                    float pitch = Float.parseFloat(split[5]);
                    float yaw = Float.parseFloat(split[6]);
                    quiz.sendTo = new Location(world, x, y, z, pitch, yaw);
                }
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4) {
                        if (split[i].endsWith("~NETHER"))
                            split[i].replace("~NETHER", "");
                        world = server.getWorld(split[i]);
                        if (world != null) {
                            int x = Integer.parseInt(split[i+1]);
                            int y = Integer.parseInt(split[i+2]);
                            int z = Integer.parseInt(split[i+3]);
                            quiz.doorBlocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4) {
                        if (split[i].endsWith("~NETHER"))
                            split[i].replace("~NETHER", "");
                        world = server.getWorld(split[i]);
                        if (world != null) {
                            int x = Integer.parseInt(split[i+1]);
                            int y = Integer.parseInt(split[i+2]);
                            int z = Integer.parseInt(split[i+3]);
                            quiz.rightBlocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4) {
                        if (split[i].endsWith("~NETHER"))
                            split[i].replace("~NETHER", "");
                        world = server.getWorld(split[i]);
                        if (world != null) {
                            int x = Integer.parseInt(split[i+1]);
                            int y = Integer.parseInt(split[i+2]);
                            int z = Integer.parseInt(split[i+3]);
                            quiz.wrongBlocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                String right = bReader.readLine();
                if (right.contains(";"))
                    quiz.right = QuizBlock.right;
                String wrong = bReader.readLine();
                if (wrong.contains(";"))
                    quiz.wrong = QuizBlock.wrong;
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
     * Reads save file to load QuizBlock data for given World
     * Saving is turned off if an error occurs
     */
    protected static void loadData(World world) {
        BufferedReader bReader = null;
        try {
            new File("plugins/QuizBlock").mkdir();
            new File("plugins/QuizBlock/QuizBlock.save").createNewFile();
            bReader = new BufferedReader(new FileReader("plugins/QuizBlock/QuizBlock.save"));
            Server server = QuizBlock.server;
            String line = "";
            while ((line = bReader.readLine()) != null) {
                String[] split = line.split(";");
                Quiz quiz = null;
                for (Quiz q: quizes)
                    if (q.name.equals(split[0]))
                        quiz = q;
                if (split[1].equals(world.getName())) {
                    double x = Double.parseDouble(split[2]);
                    double y = Double.parseDouble(split[3]);
                    double z = Double.parseDouble(split[4]);
                    float pitch = Float.parseFloat(split[5]);
                    float yaw = Float.parseFloat(split[6]);
                    quiz.sendTo = new Location(world, x, y, z, pitch, yaw);
                }
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4) {
                        if (split[i].endsWith("~NETHER"))
                            split[i].replace("~NETHER", "");
                        if (split[i].equals(world.getName())) {
                            int x = Integer.parseInt(split[i+1]);
                            int y = Integer.parseInt(split[i+2]);
                            int z = Integer.parseInt(split[i+3]);
                            quiz.doorBlocks.add(world.getBlockAt(x, y, z));
                        }
                    }
                }
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4)
                        if (split[i].equals(world.getName())) {
                            int x = Integer.parseInt(split[i+1]);
                            int y = Integer.parseInt(split[i+2]);
                            int z = Integer.parseInt(split[i+3]);
                            quiz.rightBlocks.add(world.getBlockAt(x, y, z));
                        }
                }
                line = bReader.readLine();
                if (!line.trim().isEmpty()) {
                    split = line.split(";");
                    for (int i = 0; i < split.length; i = i+4)
                        if (split[i].equals(world.getName())) {
                            int x = Integer.parseInt(split[i+1]);
                            int y = Integer.parseInt(split[i+2]);
                            int z = Integer.parseInt(split[i+3]);
                            quiz.wrongBlocks.add(world.getBlockAt(x, y, z));
                        }
                }
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
        quizes.add(quiz);
    }

    /**
     * Removes the Quiz from the LinkedList of saved Quizzes
     * 
     * @param quiz The Quiz to be removed
     */
    protected static void removeQuiz(Quiz quiz) {
        quiz.doorBlocks.clear();
        quiz.rightBlocks.clear();
        quiz.wrongBlocks.clear();
        quizes.remove(quiz);
    }
    
    /**
     * Returns the Quiz by the given name
     * 
     * @param name The name of the Quiz to be found
     * @return The Quiz by the given name
     */
    protected static Quiz findQuiz(String name) {
        for (Quiz quiz : quizes)
            if (quiz.name.equals(name))
                return quiz;
        return null;
    }
}
