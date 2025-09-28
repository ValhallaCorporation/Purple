/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.managers;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.utils.BungeeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    
    private final PurpleLobby plugin;
    private final Map<UUID, String> cachedRanks = new HashMap<>();
    private final Map<UUID, Long> lastRankUpdate = new HashMap<>();
    private static final long RANK_CACHE_TIME = 5000L;
    
    private BukkitRunnable updateTask;
    private Scoreboard globalTeamScoreboard;
    
    public ScoreboardManager(PurpleLobby plugin) {
        this.plugin = plugin;
        this.globalTeamScoreboard = null;
    }

    public void setScoreboard(Player player) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> setScoreboard(player));
            return;
        }
        
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        
        String objectiveName = "lobby_" + player.getName();
        if (objectiveName.length() > 16) {
            objectiveName = objectiveName.substring(0, 16);
        }
        Objective objective = scoreboard.registerNewObjective(objectiveName, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', "§d✦ §5§lPURPLE§f§lMC §d✦"));
        
        setupPlayerNameTag(player, globalTeamScoreboard);
        syncTeamsToPlayerScoreboard(player, scoreboard);
        
        updateScoreboard(player, scoreboard, objective);
        player.setScoreboard(scoreboard);
        
        startGlobalUpdateTask();
    }

    private void updateScoreboard(Player player, Scoreboard scoreboard, Objective objective) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> updateScoreboard(player, scoreboard, objective));
            return;
        }
        
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        
        int score = 15;
        
        setScore(scoreboard, objective, ChatColor.translateAlternateColorCodes('&', "§7"), score--);
        
        String occupation = getPlayerGroup(player);
        setScore(scoreboard, objective, ChatColor.translateAlternateColorCodes('&', "§fRank: " + occupation), score--);        
        setScore(scoreboard, objective, " ", score--);
        
        setScore(scoreboard, objective, ChatColor.translateAlternateColorCodes('&', "§fLobby: §7#01"), score--);
        
        BungeeUtils bungeeUtils = BungeeUtils.getInstance();
        int totalOnline = bungeeUtils != null ? bungeeUtils.getOnlineCount() : Bukkit.getOnlinePlayers().size();
        setScore(scoreboard, objective, ChatColor.translateAlternateColorCodes('&', "§fPlayers: §a" + totalOnline), score--);
        
        setScore(scoreboard, objective, "  ", score--);
        
        setScore(scoreboard, objective, ChatColor.translateAlternateColorCodes('&', "§epurplemc.net"), score--);
    }
    
    private void setScore(Scoreboard scoreboard, Objective objective, String text, int score) {
        scoreboard.resetScores(text);
        objective.getScore(text).setScore(score);
    }
    
    private String getPlayerGroup(Player player) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        if (cachedRanks.containsKey(uuid) && 
            lastRankUpdate.containsKey(uuid) &&
            (currentTime - lastRankUpdate.get(uuid)) < RANK_CACHE_TIME) {
            return cachedRanks.get(uuid);
        }
        
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(uuid);
        String rank = "§7Membro";
        
        if (playerData != null) {
            rank = playerData.getRank().getFormattedName();
        }
        
        cachedRanks.put(uuid, rank);
        lastRankUpdate.put(uuid, currentTime);
        
        return rank;
    }
    
    private String getPlayerTag(Player player) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());
        if (playerData != null) {
            return playerData.getTag().getFormattedColor() + playerData.getTag().getName();
        }
        return "§7Membro";
    }
    
    private void startGlobalUpdateTask() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, this::startGlobalUpdateTask);
            return;
        }
        
        if (updateTask != null) {
            return;
        }
        
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().isEmpty()) {
                    this.cancel();
                    updateTask = null;
                    return;
                }
                
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    try {
                        Scoreboard playerScoreboard = onlinePlayer.getScoreboard();
                        if (playerScoreboard != null) {
                            Objective objective = playerScoreboard.getObjective(DisplaySlot.SIDEBAR);
                            if (objective != null) {
                                updateScoreboard(onlinePlayer, playerScoreboard, objective);
                            }
                        }
                        
                        setupPlayerNameTag(onlinePlayer, globalTeamScoreboard);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Erro ao atualizar scoreboard para " + onlinePlayer.getName() + ": " + e.getMessage());
                    }
                }
            }
        };
        
        updateTask.runTaskTimer(plugin, 60L, 60L);
    }
    
    public void updatePlayerTabNameSafe(Player player) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> updatePlayerTabNameSafe(player));
            return;
        }
        
        try {
            PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());
            if (playerData != null && playerData.getTag() != null) {
                String tagName = playerData.getTag().getName();
                String tagColor = playerData.getTag().getFormattedColor();
                
                String formattedName;
                if (tagName.equals("Membro")) {
                    formattedName = ChatColor.translateAlternateColorCodes('&', "§7" + player.getName());
                } else {
                    formattedName = ChatColor.translateAlternateColorCodes('&', tagColor + "&l" + tagName.toUpperCase() + " " + tagColor + player.getName());
                }
                
                player.setPlayerListName(formattedName);
                player.setDisplayName(formattedName);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao atualizar nome no tab para " + player.getName() + ": " + e.getMessage());
        }
    }
    
    public void invalidatePlayerCache(UUID uuid) {
        cachedRanks.remove(uuid);
        lastRankUpdate.remove(uuid);
    }
    
    public void refreshPlayerScoreboard(Player player) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> refreshPlayerScoreboard(player));
            return;
        }
        
        try {
            Scoreboard playerScoreboard = player.getScoreboard();
            if (playerScoreboard != null) {
                Objective objective = playerScoreboard.getObjective(DisplaySlot.SIDEBAR);
                if (objective != null) {
                    updateScoreboard(player, playerScoreboard, objective);
                }
            }
            
            setupPlayerNameTag(player, globalTeamScoreboard);
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao atualizar scoreboard para " + player.getName() + ": " + e.getMessage());
        }
    }
    
    private void ensureGlobalScoreboardInitialized() {
        if (globalTeamScoreboard == null) {
            if (!Bukkit.isPrimaryThread()) {
                throw new IllegalStateException("Tentativa de criar scoreboard fora da thread principal!");
            }
            globalTeamScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }
    }
    
    private void setupPlayerNameTag(Player player, Scoreboard scoreboard) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> setupPlayerNameTag(player, scoreboard));
            return;
        }
        
        if (scoreboard == null) {
            plugin.getLogger().warning("Scoreboard nulo ao configurar tag de nome para " + player.getName());
            return;
        }
        
        ensureGlobalScoreboardInitialized();
        
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());
        
        if (playerData != null && playerData.getTag() != null) {
            String tagName = playerData.getTag().getName();
            String tagColor = playerData.getTag().getFormattedColor();
            
            int priority = getTagPriority(tagName);
            
            String teamName = String.format("%03d_%s", priority, tagName.toLowerCase());
            if (teamName.length() > 16) {
                teamName = teamName.substring(0, 16);
            }
            Team team = scoreboard.getTeam(teamName);
            
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
            }
            
            for (Team existingTeam : scoreboard.getTeams()) {
                if (existingTeam.hasEntry(player.getName())) {
                    existingTeam.removeEntry(player.getName());
                }
            }
            
            if (tagName.equals("Membro")) {
                team.setPrefix(ChatColor.translateAlternateColorCodes('&', "§7"));
            } else {
                team.setPrefix(ChatColor.translateAlternateColorCodes('&', tagColor + "&l" + tagName.toUpperCase() + " " + tagColor));
            }
            
            team.addEntry(player.getName());
            
            if (tagName.equals("Membro")) {
                player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "§7" + player.getName()));
            } else {
                player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', tagColor + "&l" + tagName.toUpperCase() + " " + tagColor + player.getName()));
            }
            
            if (scoreboard == globalTeamScoreboard) {
                syncPlayerToAllScoreboards(player, teamName);
            }
        }
    }
    
    private void syncPlayerToAllScoreboards(Player player, String teamName) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> syncPlayerToAllScoreboards(player, teamName));
            return;
        }
        
        ensureGlobalScoreboardInitialized();
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Scoreboard playerScoreboard = onlinePlayer.getScoreboard();
            if (playerScoreboard != null && playerScoreboard != globalTeamScoreboard) {
                try {
                    Team playerTeam = playerScoreboard.getTeam(teamName);
                    if (playerTeam == null) {
                        Team globalTeam = globalTeamScoreboard.getTeam(teamName);
                        if (globalTeam != null) {
                            playerTeam = playerScoreboard.registerNewTeam(teamName);
                            playerTeam.setPrefix(globalTeam.getPrefix());
                            playerTeam.setSuffix(globalTeam.getSuffix());
                            playerTeam.setDisplayName(globalTeam.getDisplayName());
                            
                            try {
                                playerTeam.setAllowFriendlyFire(globalTeam.allowFriendlyFire());
                                playerTeam.setCanSeeFriendlyInvisibles(globalTeam.canSeeFriendlyInvisibles());
                                playerTeam.setNameTagVisibility(globalTeam.getNameTagVisibility());
                            } catch (Exception e) {
                                // Ignorar erros de compatibilidade
                            }
                        }
                    }
                    
                    if (playerTeam != null) {
                        playerTeam.addEntry(player.getName());
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Erro ao sincronizar jogador " + player.getName() + " com scoreboard de " + onlinePlayer.getName() + ": " + e.getMessage());
                }
            }
        }
    }
    
    private int getTagPriority(String tagName) {
        switch (tagName.toLowerCase()) {
            case "admin": return 1;
            case "coord": return 2;
            case "mod+": return 3;
            case "mod": return 4;
            case "creator+": return 5;
            case "creator": return 6;
            case "luna+": return 7;
            case "beta": return 8;
            case "luna": return 9;
            case "vip": return 10;
            case "booster": return 11;
            case "winner": return 12;
            case "membro": return 13;
            default: return 999;
        }
    }
    
    private void syncTeamsToPlayerScoreboard(Player player, Scoreboard playerScoreboard) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> syncTeamsToPlayerScoreboard(player, playerScoreboard));
            return;
        }
        
        ensureGlobalScoreboardInitialized();
        
        if (globalTeamScoreboard == null || playerScoreboard == null) return;
        
        try {
            for (Team globalTeam : globalTeamScoreboard.getTeams()) {
                String teamName = globalTeam.getName();
                Team playerTeam = playerScoreboard.getTeam(teamName);
                
                if (playerTeam == null) {
                    playerTeam = playerScoreboard.registerNewTeam(teamName);
                }
                
                playerTeam.setPrefix(globalTeam.getPrefix());
                playerTeam.setSuffix(globalTeam.getSuffix());
                playerTeam.setDisplayName(globalTeam.getDisplayName());
                
                try {
                    playerTeam.setAllowFriendlyFire(globalTeam.allowFriendlyFire());
                    playerTeam.setCanSeeFriendlyInvisibles(globalTeam.canSeeFriendlyInvisibles());
                    playerTeam.setNameTagVisibility(globalTeam.getNameTagVisibility());
                } catch (Exception e) {
                }
                
                for (String entry : globalTeam.getEntries()) {
                    playerTeam.addEntry(entry);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao sincronizar teams para " + player.getName() + ": " + e.getMessage());
        }
    }
}
