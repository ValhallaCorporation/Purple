/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.managers;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.config.Configuration;
import net.valhallacodes.purplemc.PurpleCore;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MOTDManager {
    
    private final PurpleCore plugin;
    private Configuration config;
    private List<String> messages;
    private int currentIndex = 0;
    private ScheduledExecutorService scheduler;
    
    public MOTDManager(PurpleCore plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.scheduler = Executors.newScheduledThreadPool(1);
        loadMessages();
        startRotation();
    }
    
    public void reload() {
        this.config = plugin.getConfig();
        loadMessages();
        stopRotation();
        startRotation();
    }
    
    private void loadMessages() {
        this.messages = config.getStringList("motd.messages");
        if (messages.isEmpty()) {
            messages.add("§r                        §d✦ §5§lPURPLE§f§lMC §d✦§r                  §e§lNOVOS MODOS §6§l> §b§lDUELS§6§l §c❘ §a§lESCONDE-ESCONDE§6§l");
        }
    }
    
    private void startRotation() {
        if (!config.getBoolean("motd.enabled", true)) {
            return;
        }
        
        int interval = config.getInt("motd.update-interval", 5);
        scheduler.scheduleAtFixedRate(() -> {
            currentIndex = (currentIndex + 1) % messages.size();
        }, interval, interval, TimeUnit.SECONDS);
    }
    
    private void stopRotation() {
        if (scheduler != null) {
            scheduler.shutdown();
            this.scheduler = Executors.newScheduledThreadPool(1);
        }
    }
    
    public ServerPing.Protocol getProtocol() {
        return new ServerPing.Protocol("Purple Proxy", 47);
    }
    
    public ServerPing.Players getPlayers() {
        int online = plugin.getProxy().getOnlineCount();
        int max = plugin.getProxy().getConfig().getPlayerLimit();
        
        return new ServerPing.Players(max, online, null);
    }
    
    public String getMOTD() {
        if (!config.getBoolean("motd.enabled", true)) {
            return "";
        }
        
        if (plugin.getBackendManager() == null) {
            return "";
        }
        
        if (messages.isEmpty()) {
            return "§r                        §d✦ §5§lPURPLE§f§lMC §d✦§r                  §e§lNOVOS MODOS §6§l> §b§lDUELS§6§l §c❘ §a§lESCONDE-ESCONDE§6§l";
        }
        
        String message = messages.get(currentIndex);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
    
}
