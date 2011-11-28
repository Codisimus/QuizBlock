package com.codisimus.plugins.quizblock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Holds QuizBlock data and is used to load/save data
 * 
 * @author Codisimus
 */
public class SaveSystem {
    public static LinkedList<Quiz> quizes = new LinkedList<Quiz>();
    public static boolean save = true;

    /**
     * Reads save file to load QuizBlock data
     * Saving is turned off if an error occurs
     */
    public static void load() {
        try {
            new File("plugins/QuizBlock/QuizBlock.save").createNewFile();
            BufferedReader bReader = new BufferedReader(new FileReader("plugins/QuizBlock/QuizBlock.save"));
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
        catch (Exception loadFailed) {
            save = false;
            System.err.println("[QuizBlock] Load failed, saving turned off to prevent loss of data");
            loadFailed.printStackTrace();
        }
    }
    
    /**
     * Reads save file to load QuizBlock data for given World
     * Saving is turned off if an error occurs
     */
    public static void load(World world) {
        try {
            new File("plugins/QuizBlock/QuizBlock.save").createNewFile();
            BufferedReader bReader = new BufferedReader(new FileReader("plugins/QuizBlock/QuizBlock.save"));
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
        catch (Exception loadFailed) {
            save = false;
            System.err.println("[QuizBlock] Load failed, saving turned off to prevent loss of data");
            loadFailed.printStackTrace();
        }
    }

    /**
     * Writes data to save file
     * Old file is overwritten
     */
    public static void save() {
        //Cancel if saving is turned off
        if (!save)
            return;
        
        try {
            BufferedWriter bWriter = new BufferedWriter(new FileWriter("plugins/QuizBlock/QuizBlock.save"));
            
            //Write each Quiz to the save file
            for(Quiz quiz: quizes) {
                //Write data in the format "name;world;x;y;z;pitch;yaw;"
                bWriter.write(quiz.name.concat(";"));
                Location sendTo = quiz.sendTo;
                bWriter.write(sendTo.getWorld().getName()+";");
                bWriter.write(sendTo.getX()+";");
                bWriter.write(sendTo.getY()+";");
                bWriter.write(sendTo.getZ()+";");
                bWriter.write(sendTo.getPitch()+";");
                bWriter.write(sendTo.getYaw()+";");
                
                //Write doorBlocks data on a new line in the format "world;x;y;z;world;x;y;z;world;x;y;z;"
                bWriter.newLine();
                for (Block block : quiz.doorBlocks) {
                    bWriter.write(block.getWorld().getName()+";");
                    bWriter.write(block.getX()+";");
                    bWriter.write(block.getY()+";");
                    bWriter.write(block.getZ()+";");
                }
                
                //Write rightBlocks data on a new line in the format "world;x;y;z;world;x;y;z;world;x;y;z;"
                bWriter.newLine();
                for (Block block : quiz.rightBlocks) {
                    bWriter.write(block.getWorld().getName()+";");
                    bWriter.write(block.getX()+";");
                    bWriter.write(block.getY()+";");
                    bWriter.write(block.getZ()+";");
                }
                
                //Write wrongBlocks data on a new line in the format "world;x;y;z;world;x;y;z;world;x;y;z;"
                bWriter.newLine();
                for (Block block : quiz.wrongBlocks) {
                    bWriter.write(block.getWorld().getName()+";");
                    bWriter.write(block.getX()+";");
                    bWriter.write(block.getY()+";");
                    bWriter.write(block.getZ()+";");
                }
                
                //Write right and wrong messages on new lines
                bWriter.newLine();
                bWriter.write(quiz.right);
                bWriter.newLine();
                bWriter.write(quiz.wrong);
                
                //Start a new line for the next Quiz
                bWriter.newLine();
            }
            
            bWriter.close();
        }
        catch (Exception saveFailed) {
            System.err.println("[QuizBlock] Save Failed!");
            saveFailed.printStackTrace();
        }
    }
    
    /**
     * Returns the Quiz by the given name
     * 
     * @param name The name of the Quiz to be found
     * @return The Quiz by the given name
     */
    public static Quiz findQuiz(String name) {
        //Iterate through all Quizes to find the one with the given Name
        for (Quiz quiz: quizes)
            if (quiz.name.equals(name))
                return quiz;
        
        //Return null because the Quiz does not exist
        return null;
    }
    
    /**
     * Returns the Quiz that the given Block is linked to
     * 
     * @param block The given Block
     * @return The Quiz that the given Block is linked to
     */
    public static Quiz findQuiz(Block block) {
        //Iterate through all Quizes to find the one with the given Block
        for (Quiz quiz: quizes)
            if (quiz.doorBlocks.contains(block) || quiz.rightBlocks.contains(block) || quiz.wrongBlocks.contains(block))
                return quiz;
        
        //Return null because the Quiz does not exist
        return null;
    }
}
