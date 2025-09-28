/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BungeeUtils {
    
    private static BungeeUtils instance;
    private Plugin plugin;
    private int totalOnlineCount = 0;
    private Map<String, Integer> serverCounts = new HashMap<>();
    
    private BungeeUtils(Plugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }
    
    public static void initialize(Plugin plugin) {
        if (instance == null) {
            instance = new BungeeUtils(plugin);
        }
    }
    
    public static BungeeUtils getInstance() {
        return instance;
    }
    
    public int getOnlineCount() {
        return totalOnlineCount;
    }
    
    public int getServerCount(String serverName) {
        return serverCounts.getOrDefault(serverName, 0);
    }
    
    public boolean isServerOnline(String serverName) {
        return serverCounts.containsKey(serverName);
    }
    
    public void updateOnlineCount(int count) {
        this.totalOnlineCount = count;
    }
    
    public void updateServerCount(String serverName, int count) {
        serverCounts.put(serverName, count);
    }
    
    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                requestPlayerCount();
            }
        }.runTaskTimer(plugin, 0L, 100L);
    }
    
    private void requestPlayerCount() {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            
            out.writeUTF("PlayerCount");
            out.writeUTF("ALL");
            
            plugin.getServer().sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().warning("Erro ao solicitar contagem de jogadores: " + e.getMessage());
        }
    }
}
