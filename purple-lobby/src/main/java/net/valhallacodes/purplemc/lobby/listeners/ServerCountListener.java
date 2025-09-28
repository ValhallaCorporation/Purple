/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.managers.ServerCountManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ServerCountListener implements Listener, PluginMessageListener {
    
    private final PurpleLobby plugin;
    private final ServerCountManager serverCountManager;
    
    public ServerCountListener(PurpleLobby plugin) {
        this.plugin = plugin;
        this.serverCountManager = new ServerCountManager(plugin);
    }
    
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        
        // Verificar se a mensagem tem tamanho mínimo
        if (message == null || message.length < 2) {
            return;
        }
        
        try {
            // Usar ByteStreams como no midup-lobby
            com.google.common.io.ByteArrayDataInput in = com.google.common.io.ByteStreams.newDataInput(message);
            String subChannel = in.readUTF();
            
            if (subChannel.equals("PlayerCount")) {
                try {
                    String serverName = in.readUTF();
                    int playerCount = in.readInt();
                    
                    // Validar dados antes de processar
                    if (serverName != null && !serverName.isEmpty() && playerCount >= 0) {
                        // Atualizar contagem do servidor
                        serverCountManager.handlePlayerCountResponse(serverName, playerCount);
                        
                        // Log removido para evitar spam
                    }
                } catch (Exception dataException) {
                    plugin.getLogger().fine("Mensagem PlayerCount incompleta ou inválida - ignorando: " + dataException.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().fine("Mensagem BungeeCord inválida recebida - ignorando: " + e.getMessage());
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Quando um jogador entra, atualizar contagens com delay para evitar spam
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            serverCountManager.updateAllServerCounts();
        }, 100L); // 5 segundos de delay
    }
    
    public ServerCountManager getServerCountManager() {
        return serverCountManager;
    }
}
