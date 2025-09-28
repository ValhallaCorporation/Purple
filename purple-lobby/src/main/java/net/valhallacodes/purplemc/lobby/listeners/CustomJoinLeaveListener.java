/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.enums.Rank;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CustomJoinLeaveListener implements Listener {
    
    private final PurpleLobby plugin;
    
    public CustomJoinLeaveListener(PurpleLobby plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        
        // Integrar com VanishManager
        plugin.getVanishManager().onPlayerQuit(event.getPlayer());
    }
    
    private boolean shouldShowJoinMessage(Rank rank) {
        return rank == Rank.ADMIN || rank == Rank.COORD || rank == Rank.MOD_PLUS || 
               rank == Rank.MOD || rank == Rank.CREATOR_PLUS || rank == Rank.CREATOR || 
               rank == Rank.LUNA_PLUS || rank == Rank.BETA || rank == Rank.LUNA || 
               rank == Rank.VIP || rank == Rank.BOOSTER || rank == Rank.WINNER;
    }
}
