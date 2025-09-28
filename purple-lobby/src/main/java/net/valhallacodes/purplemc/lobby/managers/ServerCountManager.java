/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.managers;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerCountManager {
    
    private final PurpleLobby plugin;
    private final Map<String, Integer> serverCounts;
    private final Map<UUID, Long> lastRequestTime;
    private long lastGlobalUpdate = 0;
    
    public ServerCountManager(PurpleLobby plugin) {
        this.plugin = plugin;
        this.serverCounts = new HashMap<>();
        this.lastRequestTime = new HashMap<>();
        
        // Inicializar contagens
        serverCounts.put("lobby", 0);
        serverCounts.put("bedwars", 0);
        serverCounts.put("duels", 0);
        serverCounts.put("hideseek", 0);
        serverCounts.put("rankup", 0);
        
        // Iniciar sistema de atualização
        startUpdateTask();
    }
    
    public int getServerCount(String serverName) {
        return serverCounts.getOrDefault(serverName.toLowerCase(), 0);
    }
    
    public void updateServerCount(String serverName, int count) {
        serverCounts.put(serverName.toLowerCase(), count);
    }
    
    public void requestServerCount(Player player, String serverName) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Evitar spam de requests (máximo 1 por segundo por jogador)
        if (lastRequestTime.containsKey(playerId)) {
            long lastRequest = lastRequestTime.get(playerId);
            if (currentTime - lastRequest < 1000) {
                return;
            }
        }
        
        lastRequestTime.put(playerId, currentTime);
        
        // Enviar request para BungeeCord
        sendPlayerCountRequest(player, serverName);
    }
    
    private void sendPlayerCountRequest(Player player, String serverName) {
        try {
            // Verificar se o jogador ainda está online antes de enviar
            if (player == null || !player.isOnline()) {
                return;
            }
            
            // Validar serverName
            if (serverName == null || serverName.trim().isEmpty()) {
                return;
            }
            
            // Usar ByteArrayOutputStream e DataOutputStream como no midup-lobby
            java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream out = new java.io.DataOutputStream(b);
            
            out.writeUTF("PlayerCount");
            out.writeUTF(serverName);
            
            byte[] message = b.toByteArray();
            
            // Verificar se a mensagem não está vazia
            if (message == null || message.length == 0) {
                plugin.getLogger().warning("Mensagem vazia gerada para " + serverName);
                return;
            }
            
            player.sendPluginMessage(plugin, "BungeeCord", message);
        } catch (java.io.IOException ioException) {
            plugin.getLogger().warning("Erro de IO ao enviar request de contagem para " + serverName + ": " + ioException.getMessage());
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao enviar request de contagem para " + serverName + ": " + e.getMessage());
        }
    }
    
    private void startUpdateTask() {
        // Atualizar contagens a cada 5 minutos para evitar spam excessivo
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllServerCounts();
            }
        }.runTaskTimer(plugin, 3000L, 6000L); // 5 minutos (6000 ticks)
    }
    
    public void updateAllServerCounts() {
        // Verificar se há jogadores online antes de fazer requests
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            return;
        }
        
        // Rate limiting global - máximo 1 update por minuto
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastGlobalUpdate < 60000) { // 1 minuto
            return;
        }
        lastGlobalUpdate = currentTime;
        
        // Enviar request para todos os servidores
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null && player.isOnline() && player.hasPermission("purplelobby.servercount")) {
                for (String server : serverCounts.keySet()) {
                    requestServerCount(player, server);
                }
                break; // Apenas um jogador precisa fazer o request
            }
        }
    }
    
    public void handlePlayerCountResponse(String serverName, int count) {
        updateServerCount(serverName, count);
    }
    
    public Map<String, Integer> getAllServerCounts() {
        return new HashMap<>(serverCounts);
    }
}
