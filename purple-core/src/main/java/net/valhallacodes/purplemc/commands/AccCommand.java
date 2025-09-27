/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.valhallacodes.purplemc.PurpleCore;
import net.valhallacodes.purplemc.enums.Rank;
import net.valhallacodes.purplemc.models.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AccCommand extends Command {
    
    private final PurpleCore plugin;
    
    public AccCommand(PurpleCore plugin) {
        super("acc", null, "account");
        this.plugin = plugin;
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            executeAccCommand(sender, args);
            return;
        }
        
        ProxiedPlayer player = (ProxiedPlayer) sender;
        
        plugin.getPlayerManager().getPlayer(player.getUniqueId()).thenAccept(playerData -> {
            if (playerData == null || (playerData.getRank() != Rank.ADMIN && playerData.getRank() != Rank.COORD)) {
                player.sendMessage("§cVocê não tem permissão para usar este comando!");
                return;
            }
            
            executeAccCommand(sender, args);
        });
    }
    
    private void executeAccCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUso correto: /acc <nick> rank <rank>");
            return;
        }
        
        String targetNick = args[0];
        String action = args[1];
        String rankInput = args[2];
        
        if (!action.equalsIgnoreCase("rank")) {
            sender.sendMessage("§cUso correto: /acc <nick> rank <rank>");
            return;
        }
        
        Rank validRank = Rank.fromString(rankInput);
        if (validRank == null) {
            sender.sendMessage("§cRank inválido! Ranks disponíveis: " + Rank.getAllRanksAsString());
            return;
        }
        
        searchPlayerByName(targetNick).thenAccept(uuid -> {
            if (uuid == null) {
                sender.sendMessage("§cJogador não encontrado!");
                return;
            }
            
            getCurrentPlayerRank(uuid).thenAccept(currentRank -> {
                if (currentRank != null && currentRank.equals(validRank.name())) {
                    setPlayerRank(uuid, Rank.MEMBRO).thenAccept(success -> {
                        if (success) {
                            sender.sendMessage("§eRank '" + validRank.getDisplayName() + "' removido do jogador " + targetNick + "! Rank atual: MEMBRO");
                        } else {
                            sender.sendMessage("§cErro ao remover o rank do jogador " + targetNick + "!");
                        }
                    });
                } else {
                    setPlayerRank(uuid, validRank).thenAccept(success -> {
                        if (success) {
                            sender.sendMessage("§aRank '" + validRank.getDisplayName() + "' definido para o jogador " + targetNick + " com sucesso!");
                        } else {
                            sender.sendMessage("§cErro ao definir o rank para o jogador " + targetNick + "!");
                        }
                    });
                }
            });
        });
    }
    
    private CompletableFuture<UUID> searchPlayerByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = plugin.getBackendManager().getConnection()) {
                if (connection == null || connection.isClosed()) {
                    plugin.getLogger().severe("Conexão com backend não disponível!");
                    return null;
                }
                
                PreparedStatement stmt = connection.prepareStatement("SELECT uuid FROM players WHERE LOWER(name) = LOWER(?)");
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
                
                for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                    if (player.getName().equalsIgnoreCase(name)) {
                        PreparedStatement insertStmt = connection.prepareStatement(
                            "INSERT IGNORE INTO players (uuid, name, rank, tag, prefix_type, first_login, last_login) VALUES (?, ?, ?, ?, ?, ?, ?)"
                        );
                        insertStmt.setString(1, player.getUniqueId().toString());
                        insertStmt.setString(2, player.getName());
                        insertStmt.setString(3, "MEMBRO");
                        insertStmt.setString(4, "MEMBER");
                        insertStmt.setString(5, "DEFAULT_GRAY");
                        insertStmt.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
                        insertStmt.setTimestamp(7, new java.sql.Timestamp(System.currentTimeMillis()));
                        insertStmt.executeUpdate();
                        insertStmt.close();
                        
                        return player.getUniqueId();
                    }
                }
                
                return null;
            } catch (Exception e) {
                plugin.getLogger().severe("Erro ao buscar jogador: " + e.getMessage());
                return null;
            }
        });
    }
    
    
    
    private CompletableFuture<Boolean> setPlayerRank(UUID uuid, Rank rank) {
        return plugin.getPlayerManager().setRank(uuid, rank);
    }
    
    private CompletableFuture<String> getCurrentPlayerRank(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = plugin.getBackendManager().getConnection()) {
                if (connection == null || connection.isClosed()) {
                    plugin.getLogger().severe("Conexão com backend não disponível!");
                    return null;
                }
                
                PreparedStatement stmt = connection.prepareStatement("SELECT rank FROM players WHERE uuid = ?");
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String rank = rs.getString("rank");
                    rs.close();
                    stmt.close();
                    return rank;
                }
                
                rs.close();
                stmt.close();
                return null;
            } catch (Exception e) {
                plugin.getLogger().severe("Erro ao buscar rank do jogador: " + e.getMessage());
                return null;
            }
        });
    }
}
