
package QuizBlock;

import java.util.LinkedList;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author Codisimus
 */
public class QuizBlockPlayerListener extends PlayerListener{

    @Override
    public void onPlayerCommandPreprocess (PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage();
        String[] split = msg.split(" ");
        if (split[0].equals("/quizblock") || split[0].equals("/qb")) {
            event.setCancelled(true);
            Memory.set(player, msg);
            LinkedList<Quiz> quizes = SaveSystem.getQuizes();
            try {
                if (split[1].startsWith("r")) {
                    split = Memory.get(player).split(" ");
                }
                if (split[1].startsWith("make")) {
                    if (!QuizBlock.hasPermission(player, "make")) {
                        player.sendMessage(QuizBlock.permission);
                        return;
                    }
                    for (Quiz quiz : quizes) {
                        if (quiz.name.equals(split[2])) {
                            player.sendMessage("A Quiz named "+split[2]+" already exists.");
                            return;
                        }
                    }
                    Quiz quiz = new Quiz(split[2], player.getLocation());
                    player.sendMessage("Quiz "+split[2]+" made!");
                    SaveSystem.addQuiz(quiz);
                }
                else if (split[1].startsWith("link")) {
                    if (!QuizBlock.hasPermission(player, "make")) {
                        player.sendMessage(QuizBlock.permission);
                        return;
                    }
                    Block block = player.getTargetBlock(null, 100);
                    Quiz quiz = null;
                    for (Quiz tempQuiz : quizes) {
                        if (tempQuiz.name.equals(split[3]))
                            quiz = tempQuiz;
                    }
                    if (quiz == null) {
                        event.getPlayer().sendMessage("Quiz "+split[3]+" does not exsist.");
                        return;
                    }
                    if (quiz.doorBlocks.contains(block) || quiz.rightBlocks.contains(block) || quiz.wrongBlocks.contains(block)) {
                        player.sendMessage("That block is already linked to "+split[3]+"!");
                        return;
                    }
                    else if (split[2].equals("right")) {
                        quiz.rightBlocks.add(block);
                        player.sendMessage("Succesfully linked as right block of "+split[3]+"!");
                    }
                    else if (split[2].equals("door")) {
                        quiz.doorBlocks.add(block);
                        player.sendMessage("Succesfully linked as door block of "+split[3]+"!");
                    }
                    else if(split[2].equals("wrong")) {
                        quiz.wrongBlocks.add(block);
                        player.sendMessage("Succesfully linked as wrong block of "+split[3]+"!");
                    }
                    SaveSystem.save();
                }
                else if (split[1].startsWith("msg")) {
                    if (!QuizBlock.hasPermission(player, "make")) {
                        player.sendMessage(QuizBlock.permission);
                        return;
                    }
                    Quiz quiz = null;
                    for (Quiz tempQuiz : quizes) {
                        if (tempQuiz.name.equals(split[3])) {
                            quiz = tempQuiz;
                            break;
                        }
                    }
                    if (quiz == null) {
                        event.getPlayer().sendMessage("Quiz "+split[3]+" does not exsist.");
                        return;
                    }
                    if (split[2].equals("right")) {
                        quiz.right = msg.replace(split[0]+" "+split[1]+" "+split[2]+" "+split[3]+" ", "").replaceAll("&", "§");
                        player.sendMessage("Correct message for "+split[3]+" is now '"+quiz.right+"'");
                    }
                    else if (split[2].equals("wrong")) {
                        quiz.wrong = msg.replace(split[0]+" "+split[1]+" "+split[2]+" "+split[3]+" ", "").replaceAll("&", "§");
                        player.sendMessage("Wrong message for "+split[3]+" is now '"+quiz.wrong+"'");
                    }
                    else
                        throw new Exception();
                    SaveSystem.save();
                }
                else if (split[1].startsWith("unlink")) {
                    if (!QuizBlock.hasPermission(player, "make")) {
                        player.sendMessage(QuizBlock.permission);
                        return;
                    }
                    Block block = player.getTargetBlock(null, 100);
                    Quiz quiz = null;
                    for (Quiz tempQuiz : quizes) {
                        if (tempQuiz.name.equals(split[2])) {
                            quiz = tempQuiz;
                            break;
                        }
                    }
                    if (quiz == null) {
                        event.getPlayer().sendMessage("Quiz "+split[2]+" does not exsist.");
                        return;
                    }
                    if (quiz.doorBlocks.contains(block))
                        quiz.doorBlocks.remove(block);
                    else if (quiz.rightBlocks.contains(block))
                        quiz.rightBlocks.remove(block);
                    else if (quiz.wrongBlocks.contains(block))
                        quiz.wrongBlocks.remove(block);
                    else {
                        player.sendMessage("Block was not linked to "+split[2]+"!");
                        return;
                    }
                    player.sendMessage("Succesfully unlinked block from "+split[2]+"!");
                    SaveSystem.save();
                }
                else if (split[1].startsWith("delete")) {
                    if (!QuizBlock.hasPermission(player, "make")) {
                        player.sendMessage(QuizBlock.permission);
                        return;
                    }
                    Quiz quiz = null;
                    for (Quiz tempQuiz : quizes) {
                        if (tempQuiz.name.equals(split[2])) {
                            quiz = tempQuiz;
                            break;
                        }
                    }
                    if (quiz == null) {
                        player.sendMessage("Quiz "+split[2]+" does not exsist.");
                        return;
                    }
                    player.sendMessage("Quiz "+split[2]+" deleted.");
                    quiz.doorBlocks.clear();
                    quiz.rightBlocks.clear();
                    quiz.wrongBlocks.clear();
                    SaveSystem.removeQuiz(quiz);
                }
                else if (split[1].startsWith("list")) {
                    if (!QuizBlock.hasPermission(player, "make")) {
                        player.sendMessage(QuizBlock.permission);
                        return;
                    }
                    player.sendMessage("Current Quizes:");
                    String quizList = "";
                    for (Quiz quiz : quizes) {
                        quizList = quizList + quiz.name+", ";
                    }
                    player.sendMessage(quizList);
                }
                else if (split[1].startsWith("help") ) {
                    throw new Exception();
                }
            }
            catch (Exception e) {
                player.sendMessage("§e     QuizBlock Help Page:");
                player.sendMessage("§2/qb make [Name]§b Makes Quiz at target location");
                player.sendMessage("§2/qb link right [Name]§b Links target block with Quiz");
                player.sendMessage("§2/qb link door [Name]§b Links target block with Quiz");
                player.sendMessage("§2/qb link wrong [Name]§b Links target block with Quiz");
                player.sendMessage("§2/qb msg right [Name] [msg]§b Sets right msg for Quiz");
                player.sendMessage("§2/qb msg wrong [Name] [msg]§b Sets wrong msg for Quiz");
                player.sendMessage("§2/qb unlink [Name]§b Unlinks target block from Quiz");
                player.sendMessage("§2/qb delete [Name]§b Deletes Quiz and unlinks blocks");
                player.sendMessage("§2/qb list§b Lists all Quizes");
                player.sendMessage("§2/qb r§b Repeats last command");
            }
        }
        else
            return;
    }
}

