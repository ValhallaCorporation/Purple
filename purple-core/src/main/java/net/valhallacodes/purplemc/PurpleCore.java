/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.valhallacodes.purplemc.backend.BackendManager;
import net.valhallacodes.purplemc.listeners.PlayerListener;
import net.valhallacodes.purplemc.listeners.MOTDListener;
import net.valhallacodes.purplemc.managers.MOTDManager;
import net.valhallacodes.purplemc.managers.PlayerManager;
import net.valhallacodes.purplemc.commands.ReportCommand;
import net.valhallacodes.purplemc.commands.GoCommand;
import net.valhallacodes.purplemc.commands.TagCommand;
import net.valhallacodes.purplemc.commands.AccCommand;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;

public class PurpleCore extends Plugin {
    
    private static PurpleCore instance;
    private BackendManager backendManager;
    private MOTDManager motdManager;
    private PlayerManager playerManager;
    private Configuration config;
    
    @Override
    public void onEnable() {
        instance = this;
        
        loadConfig();
        
        try {
            backendManager = new BackendManager(this);
            backendManager.initialize();
            getLogger().info("Backend inicializado com sucesso!");
        } catch (SQLException e) {
            getLogger().severe("Erro ao conectar com backend: " + e.getMessage());
            getProxy().getPluginManager().unregisterListeners(this);
            return;
        }
        
        motdManager = new MOTDManager(this);
        playerManager = new PlayerManager(this);
        
        getProxy().getPluginManager().registerListener(this, new PlayerListener(this));
        getProxy().getPluginManager().registerListener(this, new MOTDListener(this));
        
                getProxy().getPluginManager().registerCommand(this, new ReportCommand(this));
                getProxy().getPluginManager().registerCommand(this, new GoCommand(this));
                getProxy().getPluginManager().registerCommand(this, new TagCommand(this));
                getProxy().getPluginManager().registerCommand(this, new AccCommand(this));
        
        getLogger().info("PurpleCore habilitado com sucesso!");
    }
    
    @Override
    public void onDisable() {
        if (backendManager != null) {
            backendManager.close();
        }
        if (motdManager != null) {
            motdManager.shutdown();
        }
        getLogger().info("PurpleCore desabilitado com sucesso!");
    }
    
    private void loadConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        
        File configFile = new File(getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
            }
        }
        
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
        }
    }
    
    public void reloadConfig() {
        loadConfig();
        if (motdManager != null) {
            motdManager.reload();
        }
    }
    
    public static PurpleCore getInstance() {
        return instance;
    }
    
    public BackendManager getBackendManager() {
        return backendManager;
    }
    
    public MOTDManager getMOTDManager() {
        return motdManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public Configuration getConfig() {
        return config;
    }
}
