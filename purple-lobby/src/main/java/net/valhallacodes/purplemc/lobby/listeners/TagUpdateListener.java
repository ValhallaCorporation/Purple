/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.managers.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TagUpdateListener implements Listener {
    
    private final PurpleLobby plugin;
    private final Map<UUID, String> lastKnownTags = new HashMap<>();
    private BukkitTask pollingTask;
    
    public TagUpdateListener(PurpleLobby plugin) {
        this.plugin = plugin;
        startTagPolling();
    }
    
    // Removido o handler do comando /tag - agora só atualiza baseado na tag da conta
    
    private void startTagPolling() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                checkForTagUpdates();
            }
        };
        pollingTask = runnable.runTaskTimerAsynchronously(plugin, 20L, 20L); // 1 segundo
    }
    
    private void checkForTagUpdates() {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                String currentTag = getCurrentTagFromDatabase(uuid);
                
                if (currentTag != null) {
                    String lastTag = lastKnownTags.get(uuid);
                    
                    if (!currentTag.equals(lastTag)) {
                        lastKnownTags.put(uuid, currentTag);
                        
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            try {
                                updatePlayerTabName(player);
                                // Invalidar cache e atualizar scoreboard
                                plugin.getScoreboardManager().invalidatePlayerCache(player.getUniqueId());
                                plugin.getScoreboardManager().refreshPlayerScoreboard(player);
                            } catch (Exception e) {
                                plugin.getLogger().warning("Erro ao atualizar tab name para " + player.getName() + ": " + e.getMessage());
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao verificar atualizações de tags: " + e.getMessage());
        }
    }
    
    private String getCurrentTagFromDatabase(UUID uuid) {
        try {
            synchronized (plugin.getMySQLManager()) {
                Connection connection = plugin.getMySQLManager().getConnection();
                if (connection == null) {
                    plugin.getLogger().warning("Conexão MySQL não disponível para verificar tag do jogador " + uuid);
                    return null;
                }
                
                PreparedStatement stmt = connection.prepareStatement(
                    "SELECT tag FROM players WHERE uuid = ?"
                );
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String tag = rs.getString("tag");
                    rs.close();
                    stmt.close();
                    return tag;
                }
                
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Erro ao verificar tag do jogador " + uuid + ": " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().warning("Erro inesperado ao verificar tag do jogador " + uuid + ": " + e.getMessage());
        }
        
        return null;
    }
    
    private void updatePlayerTabName(Player player) {
        if (!player.isOnline()) {
            return;
        }
        
        plugin.getScoreboardManager().updatePlayerTabNameSafe(player);
    }
    
    public void stopPolling() {
        if (pollingTask != null) {
            pollingTask.cancel();
        }
    }
}
