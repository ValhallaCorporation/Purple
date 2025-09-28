/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.valhallacodes.purplemc.lobby.PurpleLobby;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProtocolTabCompleteBlocker {

    private final PurpleLobby plugin;
    private final ProtocolManager protocolManager;
    
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

    public ProtocolTabCompleteBlocker(PurpleLobby plugin) {
        this.plugin = plugin;
        try {
            this.protocolManager = ProtocolLibrary.getProtocolManager();
            registerPacketListener();
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao inicializar ProtocolManager: " + e.getMessage());
            throw new RuntimeException("Falha ao inicializar ProtocolTabCompleteBlocker", e);
        }
    }

    private void registerPacketListener() {
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.TAB_COMPLETE) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() != PacketType.Play.Server.TAB_COMPLETE) {
                    return;
                }

                try {
                    PacketContainer packet = event.getPacket();
                    
                    if (packet == null) {
                        event.setCancelled(true);
                        return;
                    }
                    
                    if (packet.getStringArrays() == null) {
                        event.setCancelled(true);
                        return;
                    }
                    
                    String[] suggestions = packet.getStringArrays().read(0);
                    
                    if (suggestions == null) {
                        event.setCancelled(true);
                        return;
                    }
                    
                    if (suggestions.length == 0) {
                        return;
                    }
                    
                    if (suggestions.length > 100) {
                        event.setCancelled(true);
                        return;
                    }

                    List<String> filteredSuggestions = new ArrayList<>();
                    
                    for (String suggestion : suggestions) {
                        if (suggestion == null) continue;
                        
                        if (suggestion.length() > 256) continue;
                        
                        if (suggestion.trim().isEmpty()) continue;
                        
                        String lowerSuggestion = suggestion.toLowerCase();
                        
                        if (lowerSuggestion == null) continue;
                        
                        if (blockedCommands.contains(lowerSuggestion)) {
                            continue;
                        }
                        
                        if (lowerSuggestion.startsWith("bukkit:") || lowerSuggestion.startsWith("minecraft:") || 
                            lowerSuggestion.startsWith("spigot:") || lowerSuggestion.startsWith("paper:")) {
                            continue;
                        }
                        
                        if (lowerSuggestion.contains(":")) {
                            String[] parts = lowerSuggestion.split(":");
                            if (parts.length > 1 && parts[1] != null && !parts[1].trim().isEmpty() && blockedCommands.contains(parts[1])) {
                                continue;
                            }
                        }
                        
                        filteredSuggestions.add(suggestion);
                    }
                    
                    if (filteredSuggestions != null) {
                        packet.getStringArrays().write(0, filteredSuggestions.toArray(new String[0]));
                    } else {
                        event.setCancelled(true);
                    }
                    
                } catch (Exception e) {
                    // Log do erro para debug
                    plugin.getLogger().warning("Erro no ProtocolTabCompleteBlocker: " + e.getMessage());
                    event.setCancelled(true);
                }
            }
        });
    }

    public void unregister() {
        try {
            if (protocolManager != null) {
                protocolManager.removePacketListeners(plugin);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao remover listeners do ProtocolLib: " + e.getMessage());
        }
    }
}
