package com.codisimus.plugins.quizblock.listeners;

import com.codisimus.plugins.quizblock.QuizBlock;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * Loads QuizBlock data for each World that is loaded
 *
 * @author Codisimus
 */
public class WorldLoadListener extends WorldListener {

    /**
     * Loads data for the loaded World
     * 
     * @param event The WorldLoadEvent that occurred
     */
    @Override
    public void onWorldLoad (WorldLoadEvent event) {
        QuizBlock.loadData(event.getWorld());
    }
}

