/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.gui.ProfileGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ProfileClickListener implements Listener {
    
    private final PurpleLobby plugin;
    private final ProfileGUI profileGUI;
    
    public ProfileClickListener(PurpleLobby plugin) {
        this.plugin = plugin;
        this.profileGUI = new ProfileGUI(plugin);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        // Verificar se é a cabeça do perfil
        if (item.getType() == Material.SKULL_ITEM) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                String expectedName = ChatColor.translateAlternateColorCodes('&', "&aMeu Perfil");
                
                if (displayName.equals(expectedName)) {
                    // Abrir GUI de perfil
                    profileGUI.openProfileGUI(player);
                }
            }
        }
    }
    
    public ProfileGUI getProfileGUI() {
        return profileGUI;
    }
}
