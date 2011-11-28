package com.codisimus.plugins.quizblock.listeners;

import com.codisimus.plugins.quizblock.SaveSystem;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * Loads QuizBlock data for each World that is loaded
 *
 * @author Codisimus
 */
public class worldListener extends WorldListener {

    /**
     * Loads data for the loaded World
     * 
     * @param event The WorldLoadEvent that occurred
     */
    @Override
    public void onWorldLoad (WorldLoadEvent event) {
        SaveSystem.load(event.getWorld());
    }
}

