/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.enums.Tag;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
import net.valhallacodes.purplemc.lobby.utils.BungeeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class BungeeCordMessageListener implements Listener, PluginMessageListener {

    private final PurpleLobby plugin;

    public BungeeCordMessageListener(PurpleLobby plugin) {
        this.plugin = plugin;
        
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
    }

    @Override
    public void onPluginMessageReceived(String channel, org.bukkit.entity.Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subChannel = in.readUTF();

            if (subChannel.equals("PlayerCount")) {
                String server = in.readUTF();
                int count = in.readInt();
                
                if (server.equals("ALL")) {
                    BungeeUtils.getInstance().updateOnlineCount(count);
                } else {
                    BungeeUtils.getInstance().updateServerCount(server, count);
                }
            } else if (subChannel.equals("TagUpdate")) {
                String playerName = in.readUTF();
                String tagName = in.readUTF();
                
                updatePlayerTag(playerName, tagName);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao processar mensagem do BungeeCord: " + e.getMessage());
        }
    }

    private void updatePlayerTag(String playerName, String tagName) {
        org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(playerName);
        if (bukkitPlayer == null) {
            return;
        }

        Tag tag = Tag.fromUsages(tagName);
        if (tag == null) {
            return;
        }

        // Carregar dados do jogador
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(bukkitPlayer.getUniqueId());
        if (playerData != null) {
            // Criar novo PlayerData com a tag atualizada
            PlayerManager.PlayerData updatedPlayerData = new PlayerManager.PlayerData(
                playerData.getUuid(),
                playerData.getName(),
                playerData.getRank(),
                tag,
                playerData.getPrefixType()
            );
            
            // Salvar dados atualizados
            plugin.getPlayerManager().savePlayerData(updatedPlayerData);
            
            // Atualizar display name do jogador
            String displayName = getFormattedName(updatedPlayerData);
            bukkitPlayer.setDisplayName(displayName);
            bukkitPlayer.setPlayerListName(displayName);
        }
    }
    
    private String getFormattedName(PlayerManager.PlayerData playerData) {
        if (playerData.getRank() == net.valhallacodes.purplemc.lobby.enums.Rank.MEMBRO) {
            return playerData.getPrefixType().getColor() + playerData.getName();
        }
        return playerData.getTag().getColoredPrefix() + " " + playerData.getPrefixType().getColor() + playerData.getName();
    }
}