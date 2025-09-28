/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelListener implements Listener {
    
    private final PurpleLobby plugin;
    
    public FoodLevelListener(PurpleLobby plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}
