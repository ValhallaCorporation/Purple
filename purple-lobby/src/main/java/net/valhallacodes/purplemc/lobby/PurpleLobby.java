/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby;

import net.valhallacodes.purplemc.lobby.gui.ServerSelectorGUI;
import net.valhallacodes.purplemc.lobby.gui.ProfileGUI;
import net.valhallacodes.purplemc.lobby.gui.LunaPlusColorGUI;
import net.valhallacodes.purplemc.lobby.listeners.PlayerJoinListener;
import net.valhallacodes.purplemc.lobby.listeners.CustomJoinLeaveListener;
import net.valhallacodes.purplemc.lobby.listeners.TagUpdateListener;
import net.valhallacodes.purplemc.lobby.listeners.BungeeCordMessageListener;
import net.valhallacodes.purplemc.lobby.listeners.ChatListener;
import net.valhallacodes.purplemc.lobby.listeners.CommandBlockerListener;
import net.valhallacodes.purplemc.lobby.listeners.ProtocolTabCompleteBlocker;
import net.valhallacodes.purplemc.lobby.listeners.LobbyProtectionListener;
import net.valhallacodes.purplemc.lobby.listeners.WeatherTimeListener;
import net.valhallacodes.purplemc.lobby.listeners.FoodLevelListener;
import net.valhallacodes.purplemc.lobby.listeners.SlimeJumpListener;
import net.valhallacodes.purplemc.lobby.listeners.FireworksListener;
import net.valhallacodes.purplemc.lobby.listeners.LobbyItemsListener;
import net.valhallacodes.purplemc.lobby.listeners.ProfileClickListener;
import net.valhallacodes.purplemc.lobby.listeners.ServerSelectorClickListener;
import net.valhallacodes.purplemc.lobby.listeners.ServerCountListener;
import net.valhallacodes.purplemc.lobby.managers.MySQLManager;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
import net.valhallacodes.purplemc.lobby.managers.ScoreboardManager;
import net.valhallacodes.purplemc.lobby.managers.VanishManager;
import net.valhallacodes.purplemc.lobby.managers.ServerCountManager;
import net.valhallacodes.purplemc.lobby.utils.BungeeUtils;
import net.valhallacodes.purplemc.lobby.utils.LocationUtils;
import net.valhallacodes.purplemc.lobby.commands.SetLobbyCommand;
import net.valhallacodes.purplemc.lobby.commands.GamemodeCommand;
import net.valhallacodes.purplemc.lobby.commands.VanishCommand;
import net.valhallacodes.purplemc.lobby.commands.BuildCommand;
import net.valhallacodes.purplemc.lobby.commands.FlyCommand;
import net.valhallacodes.purplemc.lobby.commands.ServerCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class PurpleLobby extends JavaPlugin {
    
    private static PurpleLobby instance;
    private MySQLManager mySQLManager;
    private PlayerManager playerManager;
    private ScoreboardManager scoreboardManager;
    private TagUpdateListener tagUpdateListener;
    private LocationUtils locationUtils;
    private ProtocolTabCompleteBlocker tabCompleteBlocker;
    private VanishManager vanishManager;
    private LobbyProtectionListener protectionListener;
    private ServerCountManager serverCountManager;
    private ServerCountListener serverCountListener;
    private ProfileGUI profileGUI;
    private LunaPlusColorGUI lunaPlusColorGUI;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        mySQLManager = new MySQLManager(this);
        
        try {
            boolean success = mySQLManager.connectSync();
            if (success) {
                getLogger().info("Conectado ao MySQL com sucesso!");
                initializeComponents();
            } else {
                getLogger().severe("Falha ao conectar com o banco de dados MySQL! Desabilitando plugin...");
                getServer().getPluginManager().disablePlugin(this);
            }
        } catch (SQLException e) {
            getLogger().severe("Erro ao conectar com o banco de dados MySQL: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    private void initializeComponents() {
        playerManager = new PlayerManager(mySQLManager);
        scoreboardManager = new ScoreboardManager(this);
        locationUtils = new LocationUtils(this);
        vanishManager = new VanishManager(this);
        protectionListener = new LobbyProtectionListener(this);
        serverCountListener = new ServerCountListener(this);
        serverCountManager = serverCountListener.getServerCountManager();
        profileGUI = new ProfileGUI(this);
        lunaPlusColorGUI = new LunaPlusColorGUI(this);
        
        BungeeUtils.initialize(this);
        
        // Registrar comandos
        getCommand("setlobby").setExecutor(new SetLobbyCommand(this));
        getCommand("gamemode").setExecutor(new GamemodeCommand(this));
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("build").setExecutor(new BuildCommand(this, protectionListener));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("server").setExecutor(new ServerCommand(this));
        
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CustomJoinLeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandBlockerListener(this), this);
        getServer().getPluginManager().registerEvents(protectionListener, this);
        getServer().getPluginManager().registerEvents(vanishManager, this);
        getServer().getPluginManager().registerEvents(new WeatherTimeListener(), this);
        getServer().getPluginManager().registerEvents(new FoodLevelListener(this), this);
        getServer().getPluginManager().registerEvents(new SlimeJumpListener(), this);
        getServer().getPluginManager().registerEvents(new FireworksListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyItemsListener(this), this);
        getServer().getPluginManager().registerEvents(new ProfileClickListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerSelectorClickListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerSelectorGUI(this), this);
        getServer().getPluginManager().registerEvents(serverCountListener, this);
        getServer().getPluginManager().registerEvents(profileGUI, this);
        getServer().getPluginManager().registerEvents(lunaPlusColorGUI, this);
        
        tagUpdateListener = new TagUpdateListener(this);
        getServer().getPluginManager().registerEvents(tagUpdateListener, this);
        
        getServer().getPluginManager().registerEvents(new BungeeCordMessageListener(this), this);
        
        // Registrar canal BungeeCord para contagem de servidores
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", serverCountListener);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        
        // Configurar tempo e clima para todos os mundos já carregados
        for (World world : getServer().getWorlds()) {
            WeatherTimeListener.configureWorld(world);
        }
        
        // ProtocolLib Tab Completion Blocker
        if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            tabCompleteBlocker = new ProtocolTabCompleteBlocker(this);
            getLogger().info("ProtocolLib detectado - Tab completion bloqueado!");
        } else {
            getLogger().warning("ProtocolLib não encontrado - Tab completion não será bloqueado!");
        }
        
        getLogger().info("PurpleLobby foi habilitado com sucesso!");
    }
    
    @Override
    public void onDisable() {
        if (tagUpdateListener != null) {
            tagUpdateListener.stopPolling();
        }
        
        if (tabCompleteBlocker != null) {
            tabCompleteBlocker.unregister();
        }
        
        if (mySQLManager != null) {
            mySQLManager.disconnect();
        }
        
        getLogger().info("PurpleLobby foi desabilitado!");
    }
    
    public static PurpleLobby getInstance() {
        return instance;
    }
    
    public MySQLManager getMySQLManager() {
        return mySQLManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    
    public LocationUtils getLocationUtils() {
        return locationUtils;
    }
    
    public VanishManager getVanishManager() {
        return vanishManager;
    }
    
    public LobbyProtectionListener getLobbyProtectionListener() {
        return protectionListener;
    }
    
    public ServerCountManager getServerCountManager() {
        return serverCountManager;
    }
    
    public ProfileGUI getProfileGUI() {
        return profileGUI;
    }
    
    public LunaPlusColorGUI getLunaPlusColorGUI() {
        return lunaPlusColorGUI;
    }
}
