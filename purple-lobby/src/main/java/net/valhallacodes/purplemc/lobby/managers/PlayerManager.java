/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.managers;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.enums.PrefixType;
import net.valhallacodes.purplemc.lobby.enums.Rank;
import net.valhallacodes.purplemc.lobby.enums.Tag;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerManager {

    private final MySQLManager mySQLManager;

    public PlayerManager(MySQLManager mySQLManager) {
        this.mySQLManager = mySQLManager;
    }

    public PlayerData loadPlayerData(UUID uuid) {
        try {
            synchronized (mySQLManager) {
                Connection connection = mySQLManager.getConnection();
                if (connection == null) {
                    return null;
                }
                
                PreparedStatement stmt = connection.prepareStatement(
                    "SELECT uuid, name, rank, tag, prefix_type, luna_plus_color FROM players WHERE uuid = ?"
                );
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String name = rs.getString("name");
                    String rankStr = rs.getString("rank");
                    String tagStr = rs.getString("tag");
                    String prefixTypeStr = rs.getString("prefix_type");
                    String lunaPlusColor = rs.getString("luna_plus_color");
                    
                    // Validar e corrigir cor da LUNA+ se necessário
                    if (lunaPlusColor != null && !isValidColorCode(lunaPlusColor)) {
                        lunaPlusColor = "§5"; // Cor padrão
                        // Atualizar no banco de dados
                        updateLunaPlusColorInDB(uuid, lunaPlusColor);
                    }

                    Rank rank = Rank.fromString(rankStr);
                    Tag tag;
                    try {
                        tag = Tag.valueOf(tagStr);
                    } catch (IllegalArgumentException e) {
                        tag = Tag.fromUsages(tagStr);
                        if (tag == null) {
                            tag = Tag.MEMBER; 
                        }
                    }
                    PrefixType prefixType = PrefixType.valueOf(prefixTypeStr != null ? prefixTypeStr.toUpperCase() : "DEFAULT_GRAY");

                    rs.close();
                    stmt.close();
                    return new PlayerData(uuid, name, rank, tag, prefixType, lunaPlusColor);
                }

                rs.close();
                stmt.close();
                return null;
            }
        } catch (SQLException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean isValidColorCode(String color) {
        if (color == null || color.length() != 2) {
            return false;
        }
        
        // Verificar se é um código de cor válido do Minecraft
        String validCodes = "0123456789abcdef";
        return color.startsWith("§") && validCodes.contains(color.substring(1));
    }
    
    private void updateLunaPlusColorInDB(UUID uuid, String color) {
        try {
            Connection connection = mySQLManager.getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "UPDATE players SET luna_plus_color = ? WHERE uuid = ?"
            );
            stmt.setString(1, color);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
        }
    }

    public boolean savePlayerData(PlayerData playerData) {
        try {
            Connection connection = mySQLManager.getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO players (uuid, name, rank, tag, prefix_type, luna_plus_color) VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), rank = VALUES(rank), tag = VALUES(tag), prefix_type = VALUES(prefix_type), luna_plus_color = VALUES(luna_plus_color)"
            );
            
            stmt.setString(1, playerData.getUuid().toString());
            stmt.setString(2, playerData.getName());
            stmt.setString(3, playerData.getRank().name());
            stmt.setString(4, playerData.getTag().name());
            stmt.setString(5, playerData.getPrefixType().name());
            stmt.setString(6, playerData.getLunaPlusColor());
            
            stmt.executeUpdate();
            stmt.close();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createDefaultPlayer(Player player) {
        try {
            Connection connection = mySQLManager.getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT IGNORE INTO players (uuid, name, rank, tag, prefix_type, luna_plus_color) VALUES (?, ?, ?, ?, ?, ?)"
            );
            
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.setString(3, Rank.MEMBRO.name());
            stmt.setString(4, Tag.MEMBER.name());
            stmt.setString(5, PrefixType.DEFAULT_GRAY.name());
            stmt.setString(6, "§5"); // Cor padrão para LUNA+
            
            stmt.executeUpdate();
            stmt.close();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public UUID searchPlayerByName(String name) {
        try {
            Connection connection = mySQLManager.getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT uuid FROM players WHERE LOWER(name) = LOWER(?)"
            );
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String uuidString = rs.getString("uuid");
                rs.close();
                stmt.close();
                return UUID.fromString(uuidString);
            }

            rs.close();
            stmt.close();
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class PlayerData {
        private final UUID uuid;
        private final String name;
        private final Rank rank;
        private final Tag tag;
        private final PrefixType prefixType;
        private final String lunaPlusColor;

        public PlayerData(UUID uuid, String name, Rank rank, Tag tag, PrefixType prefixType) {
            this.uuid = uuid;
            this.name = name;
            this.rank = rank;
            this.tag = tag;
            this.prefixType = prefixType;
            this.lunaPlusColor = "§5"; // Cor padrão
        }

        public PlayerData(UUID uuid, String name, Rank rank, Tag tag, PrefixType prefixType, String lunaPlusColor) {
            this.uuid = uuid;
            this.name = name;
            this.rank = rank;
            this.tag = tag;
            this.prefixType = prefixType;
            this.lunaPlusColor = lunaPlusColor != null ? lunaPlusColor : "§5";
        }

        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public Rank getRank() { return rank; }
        public Tag getTag() { return tag; }
        public PrefixType getPrefixType() { return prefixType; }
        public String getLunaPlusColor() { return lunaPlusColor; }
    }
}
