/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.managers;

import net.valhallacodes.purplemc.PurpleCore;
import net.valhallacodes.purplemc.enums.PrefixType;
import net.valhallacodes.purplemc.enums.Rank;
import net.valhallacodes.purplemc.enums.Tag;
import net.valhallacodes.purplemc.models.Player;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlayerManager {
    
    private final PurpleCore plugin;
    private final ConcurrentMap<UUID, Player> players = new ConcurrentHashMap<>();
    
    public PlayerManager(PurpleCore plugin) {
        this.plugin = plugin;
    }
    
    public CompletableFuture<Player> getPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (players.containsKey(uuid)) {
                return players.get(uuid);
            }
            
            try (Connection conn = plugin.getBackendManager().getConnection()) {
                String sql = "SELECT * FROM players WHERE uuid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Player player = createPlayerFromResultSet(rs);
                            players.put(uuid, player);
                            return player;
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao buscar jogador: " + e.getMessage());
            }
            
            return null;
        });
    }
    
    public CompletableFuture<Player> getPlayer(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = plugin.getBackendManager().getConnection()) {
                String sql = "SELECT * FROM players WHERE name = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            UUID uuid = UUID.fromString(rs.getString("uuid"));
                            Player player = createPlayerFromResultSet(rs);
                            players.put(uuid, player);
                            return player;
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao buscar jogador por nome: " + e.getMessage());
            }
            
            return null;
        });
    }
    
    public CompletableFuture<Void> createPlayer(UUID uuid, String name) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = plugin.getBackendManager().getConnection()) {
                String sql = "INSERT INTO players (uuid, name, rank, tag, prefix_type) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, name);
                    stmt.setString(3, Rank.MEMBRO.name());
                    stmt.setString(4, Tag.MEMBER.name());
                    stmt.setString(5, PrefixType.DEFAULT_GRAY.name());
                    stmt.executeUpdate();
                    
                    Player player = new Player(uuid, name);
                    players.put(uuid, player);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao criar jogador: " + e.getMessage());
            }
        });
    }
    
    public CompletableFuture<Void> updatePlayer(Player player) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = plugin.getBackendManager().getConnection()) {
                String sql = "UPDATE players SET name = ?, rank = ?, tag = ?, prefix_type = ?, last_login = ? WHERE uuid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, player.getName());
                    stmt.setString(2, player.getRank().name());
                    stmt.setString(3, player.getTag().name());
                    stmt.setString(4, player.getPrefixType().name());
                    stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                    stmt.setString(6, player.getUuid().toString());
                    stmt.executeUpdate();
                    
                    players.put(player.getUuid(), player);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao atualizar jogador: " + e.getMessage());
            }
        });
    }
    
    public CompletableFuture<Boolean> setRank(UUID uuid, Rank rank) {
        return CompletableFuture.supplyAsync(() -> {
            Player player = players.get(uuid);
            if (player != null) {
                player.setRank(rank);
                updatePlayer(player);
                return true;
            }
            return false;
        });
    }
    
    public CompletableFuture<Boolean> setTag(UUID uuid, Tag tag) {
        return CompletableFuture.supplyAsync(() -> {
            Player player = players.get(uuid);
            if (player == null) {
                try (Connection conn = plugin.getBackendManager().getConnection()) {
                    String sql = "SELECT * FROM players WHERE uuid = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, uuid.toString());
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                player = createPlayerFromResultSet(rs);
                                players.put(uuid, player);
                            } else {
                                return false;
                            }
                        }
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Erro ao carregar jogador para setTag: " + e.getMessage());
                    return false;
                }
            }
            
            player.setTag(tag);
            updatePlayer(player);
            return true;
        });
    }
    
    public CompletableFuture<Boolean> setPrefixType(UUID uuid, PrefixType prefixType) {
        return CompletableFuture.supplyAsync(() -> {
            Player player = players.get(uuid);
            if (player != null) {
                player.setPrefixType(prefixType);
                updatePlayer(player);
                return true;
            }
            return false;
        });
    }
    
    private Player createPlayerFromResultSet(ResultSet rs) throws SQLException {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        String name = rs.getString("name");
        Rank rank = Rank.valueOf(rs.getString("rank"));
        Tag tag = Tag.valueOf(rs.getString("tag"));
        PrefixType prefixType = PrefixType.valueOf(rs.getString("prefix_type"));
        Timestamp firstLogin = rs.getTimestamp("first_login");
        Timestamp lastLogin = rs.getTimestamp("last_login");
        
        return new Player(uuid, name, rank, tag, prefixType, firstLogin, lastLogin);
    }
    
    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }
    
    public ConcurrentMap<UUID, Player> getPlayers() {
        return players;
    }
}
