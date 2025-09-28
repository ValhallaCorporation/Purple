/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.gui.ServerSelectorGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ServerSelectorClickListener implements Listener {
    
    private final PurpleLobby plugin;
    private final ServerSelectorGUI serverSelectorGUI;
    
    public ServerSelectorClickListener(PurpleLobby plugin) {
        this.plugin = plugin;
        this.serverSelectorGUI = new ServerSelectorGUI(plugin);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        // Verificar se é a bússola do servidor
        if (item.getType() == Material.COMPASS) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                String expectedName = ChatColor.translateAlternateColorCodes('&', "&aSelecione um servidor &7(Clique c/direito)");
                
                if (displayName.equals(expectedName)) {
                    // Abrir GUI de seleção de servidor
                    serverSelectorGUI.openServerSelector(player);
                }
            }
        }
    }
    
    public ServerSelectorGUI getServerSelectorGUI() {
        return serverSelectorGUI;
    }
}
