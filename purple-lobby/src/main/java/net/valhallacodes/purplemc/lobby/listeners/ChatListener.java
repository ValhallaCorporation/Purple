/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final PurpleLobby plugin;

    public ChatListener(PurpleLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());
        
        String formattedMessage;
        if (playerData != null && playerData.getTag() != null) {
            String tagName = playerData.getTag().getName();
            String tagColor = playerData.getTag().getFormattedColor();
            
            if (tagName.equals("Membro")) {
                formattedMessage = ChatColor.translateAlternateColorCodes('&', 
                    "&7" + player.getName() + "&f: &f" + message);
            } else {
                // Usar cor personalizada para LUNA+
                String prefix;
                if (playerData.getTag() == net.valhallacodes.purplemc.lobby.enums.Tag.LUNA_PLUS && playerData.getLunaPlusColor() != null) {
                    prefix = playerData.getTag().getColoredPrefix(playerData.getLunaPlusColor());
                } else {
                    prefix = tagColor + "&l" + tagName.toUpperCase() + " " + tagColor;
                }
                
                formattedMessage = ChatColor.translateAlternateColorCodes('&',
                    prefix + player.getName() + "&f: &f" + message);
            }
        } else {
            formattedMessage = ChatColor.translateAlternateColorCodes('&',
                "&7" + player.getName() + "&8: &f" + message);
        }
        
        event.setFormat(formattedMessage);
    }
}
