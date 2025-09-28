/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LobbyProtectionListener implements Listener {
    
    private final Set<UUID> buildMode = new HashSet<>();
    
    public LobbyProtectionListener(PurpleLobby plugin) {
    }
    
    public void toggleBuildMode(Player player) {
        UUID uuid = player.getUniqueId();
        if (buildMode.contains(uuid)) {
            buildMode.remove(uuid);
            player.setGameMode(GameMode.SURVIVAL);
            player.sendMessage("§cModo construção desativado!");
        } else {
            buildMode.add(uuid);
            player.setGameMode(GameMode.CREATIVE);
            player.sendMessage("§aModo construção ativado!");
        }
    }
    
    public boolean isInBuildMode(Player player) {
        return buildMode.contains(player.getUniqueId());
    }
    
    public void disableBuildMode(Player player) {
        UUID uuid = player.getUniqueId();
        if (buildMode.contains(uuid)) {
            buildMode.remove(uuid);
            player.setGameMode(GameMode.SURVIVAL);
            player.sendMessage("§cModo construção desativado automaticamente!");
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isInBuildMode(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!isInBuildMode(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            // Manter 1 coração e cancelar todos os danos
            player.setHealth(2.0);
            player.setMaxHealth(2.0);
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Cancelar todos os danos entre entidades (PvP)
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!isInBuildMode(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        if (isInBuildMode(player)) {
            // Permitir TODAS as ações do inventário em build mode
            event.setCancelled(false);
        } else {
            // Bloquear ações do inventário quando não estiver em build mode
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }
}
