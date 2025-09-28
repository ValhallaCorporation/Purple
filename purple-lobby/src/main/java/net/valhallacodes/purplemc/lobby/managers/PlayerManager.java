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
                    "SELECT uuid, name, rank, tag, prefix_type FROM players WHERE uuid = ?"
                );
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String name = rs.getString("name");
                    String rankStr = rs.getString("rank");
                    String tagStr = rs.getString("tag");
                    String prefixTypeStr = rs.getString("prefix_type");

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
                    return new PlayerData(uuid, name, rank, tag, prefixType);
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

    public boolean savePlayerData(PlayerData playerData) {
        try {
            Connection connection = mySQLManager.getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO players (uuid, name, rank, tag, prefix_type) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), rank = VALUES(rank), tag = VALUES(tag), prefix_type = VALUES(prefix_type)"
            );
            
            stmt.setString(1, playerData.getUuid().toString());
            stmt.setString(2, playerData.getName());
            stmt.setString(3, playerData.getRank().name());
            stmt.setString(4, playerData.getTag().name());
            stmt.setString(5, playerData.getPrefixType().name());
            
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
                "INSERT IGNORE INTO players (uuid, name, rank, tag, prefix_type) VALUES (?, ?, ?, ?, ?)"
            );
            
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.setString(3, Rank.MEMBRO.name());
            stmt.setString(4, Tag.MEMBER.name());
            stmt.setString(5, PrefixType.DEFAULT_GRAY.name());
            
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

        public PlayerData(UUID uuid, String name, Rank rank, Tag tag, PrefixType prefixType) {
            this.uuid = uuid;
            this.name = name;
            this.rank = rank;
            this.tag = tag;
            this.prefixType = prefixType;
        }

        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public Rank getRank() { return rank; }
        public Tag getTag() { return tag; }
        public PrefixType getPrefixType() { return prefixType; }
    }
}
