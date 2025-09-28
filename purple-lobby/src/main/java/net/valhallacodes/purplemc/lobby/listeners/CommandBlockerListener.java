/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.List;

public class CommandBlockerListener implements Listener {

    private final PurpleLobby plugin;
    
    private final List<String> blockedCommands = Arrays.asList(
        "about", "pl", "plugins", "plugin", "ver", "version", "icanhasbukkit",
        "help", "?", "bukkit:help", "bukkit:?", "bukkit:about", "bukkit:pl",
        "bukkit:plugins", "bukkit:ver", "bukkit:version", "minecraft:help",
        "reload", "rl", "stop", "restart", "save-all", "save-on", "save-off",
        "whitelist", "ban", "ban-ip", "pardon", "pardon-ip", "op", "deop",
        "kick", "tell", "msg", "w", "me", "say", "give", "tp", "teleport",
        "tpa", "tphere", "tpall", "world", "mv", "multiverse", "worldedit",
        "we", "/", "worldguard", "wg", "essentials", "ess", "pex", "permissions",
        "lp", "luckperms", "time", "weather", "difficulty",
        "defaultgamemode", "gamerule", "seed", "debug", "forge", "fml",
        "sponge", "bukkit:me", "minecraft:me", "bukkit:tell", "minecraft:tell"
    );

    public CommandBlockerListener(PurpleLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        String message = event.getMessage();
        if (message == null || message.trim().isEmpty()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Comando desconhecido, não encontrado no banco de dados.");
            return;
        }
        
        if (message.length() > 256) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Comando desconhecido, não encontrado no banco de dados.");
            return;
        }
        
        String lowerMessage = message.toLowerCase();
        
        if (!lowerMessage.startsWith("/")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Comando desconhecido, não encontrado no banco de dados.");
            return;
        }
        
        if (lowerMessage.length() < 2) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Comando desconhecido, não encontrado no banco de dados.");
            return;
        }
        
        String command = lowerMessage.substring(1);
        
        if (command == null || command.trim().isEmpty()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Comando desconhecido, não encontrado no banco de dados.");
            return;
        }
        
        if (command.contains(" ")) {
            String[] parts = command.split(" ");
            if (parts.length > 0) {
                command = parts[0];
            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Comando desconhecido, não encontrado no banco de dados.");
                return;
            }
        }

        if (command == null || command.trim().isEmpty()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Comando desconhecido, não encontrado no banco de dados.");
            return;
        }

        if (blockedCommands.contains(command)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Comando desconhecido, não encontrado no banco de dados.");
            return;
        }

        if (command.startsWith("bukkit:") || command.startsWith("minecraft:") || 
            command.startsWith("spigot:") || command.startsWith("paper:")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Comando desconhecido, não encontrado no banco de dados.");
            return;
        }

        if (command.contains(":")) {
            String[] parts = command.split(":");
            if (parts.length > 1 && parts[1] != null && !parts[1].trim().isEmpty() && blockedCommands.contains(parts[1])) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Comando desconhecido, não encontrado no banco de dados.");
                return;
            }
        }
    }
}
