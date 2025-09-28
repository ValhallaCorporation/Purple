/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyItemsListener implements Listener {
    
    private final PurpleLobby plugin;
    
    public LobbyItemsListener(PurpleLobby plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // Verificar se é um item do lobby
        if (isLobbyItem(event.getCurrentItem()) || isLobbyItem(event.getCursor())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // Verificar se algum item sendo arrastado é do lobby
        for (ItemStack item : event.getNewItems().values()) {
            if (isLobbyItem(item)) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Verificar se o item sendo dropado é do lobby
        if (isLobbyItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cVocê não pode dropar este item!"));
        }
    }
    
    private boolean isLobbyItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        // Verificar bússola do lobby
        if (item.getType() == Material.COMPASS) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                return displayName.equals(ChatColor.translateAlternateColorCodes('&', "&aSelecione um servidor &7(Clique c/direito)"));
            }
        }
        
        // Verificar cabeça do lobby
        if (item.getType() == Material.SKULL_ITEM) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                return displayName.equals(ChatColor.translateAlternateColorCodes('&', "&aMeu Perfil"));
            }
        }
        
        return false;
    }
}
