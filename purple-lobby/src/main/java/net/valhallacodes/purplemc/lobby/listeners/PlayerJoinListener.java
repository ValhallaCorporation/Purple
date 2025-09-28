/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.enums.Rank;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerJoinListener implements Listener {

    private final PurpleLobby plugin;

    public PlayerJoinListener(PurpleLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player bukkitPlayer = event.getPlayer();
        
        event.setJoinMessage(null);
        
        // Limpar inventário e definir gamemode 0 (survival)
        bukkitPlayer.getInventory().clear();
        bukkitPlayer.setGameMode(GameMode.SURVIVAL);
        
        // Dar itens do lobby
        giveLobbyItems(bukkitPlayer);
        
        // Desativar build mode se estiver ativo
        plugin.getLobbyProtectionListener().disableBuildMode(bukkitPlayer);
        
        // Configurar vida de 1 coração
        bukkitPlayer.setMaxHealth(2.0);
        bukkitPlayer.setHealth(2.0);
        bukkitPlayer.setFoodLevel(20);
        bukkitPlayer.setSaturation(20.0f);
        
        Location lobbySpawn = plugin.getLocationUtils().getLobbySpawn();
        if (lobbySpawn != null) {
            bukkitPlayer.teleport(lobbySpawn);
        }
        
        showWelcomeTitle(bukkitPlayer);
        
        // Limpar chat do jogador
        clearPlayerChat(bukkitPlayer);
        
        plugin.getPlayerManager().createDefaultPlayer(bukkitPlayer);
        plugin.getScoreboardManager().setScoreboard(bukkitPlayer);
        
        plugin.getVanishManager().onPlayerJoin(bukkitPlayer);
        
        // Fly automático para ranks Booster ou superior
        enableFlyForBoosterPlayers(bukkitPlayer);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(bukkitPlayer.getUniqueId());
            if (playerData != null && playerData.getTag() != null && shouldShowJoinMessage(playerData.getRank())) {
                String tagName = playerData.getTag().getName();
                String tagColor = playerData.getTag().getFormattedColor();
                String playerName = bukkitPlayer.getName();
                
                String message = ChatColor.translateAlternateColorCodes('&', 
                    tagColor + "&l" + tagName.toUpperCase() + " " + tagColor + playerName + " &6entrou no lobby!");
                
                Bukkit.broadcastMessage(message);
            }
        }, 20L); 
    }
    
    private void showWelcomeTitle(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&5&lPURPLE");
        String subtitle = ChatColor.translateAlternateColorCodes('&', "&eSeja bem vindo!");
        
        try {
            player.sendTitle(title, subtitle);
        } catch (Exception e) {
            player.sendMessage(title);
            player.sendMessage(subtitle);
        }
    }
    
    private boolean shouldShowJoinMessage(Rank rank) {
        return rank == Rank.ADMIN || rank == Rank.COORD || rank == Rank.MOD_PLUS || 
               rank == Rank.MOD || rank == Rank.CREATOR_PLUS || rank == Rank.CREATOR || 
               rank == Rank.LUNA_PLUS || rank == Rank.BETA || rank == Rank.LUNA || 
               rank == Rank.VIP || rank == Rank.BOOSTER || rank == Rank.WINNER;
    }
    
    private void clearPlayerChat(Player player) {
        for (int i = 0; i < 100; i++) {
            player.sendMessage("");
        }
    }
    
    private void enableFlyForBoosterPlayers(Player player) {
        PlayerManager playerManager = plugin.getPlayerManager();
        PlayerManager.PlayerData playerData = playerManager.loadPlayerData(player.getUniqueId());
        
        if (playerData != null && playerData.getRank().ordinal() >= Rank.BOOSTER.ordinal()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.setAllowFlight(true);
                    player.setFlying(true); 
                    player.sendMessage("§aFly automático ativado!");
                }
            }, 40L); 
        }
    }
    
    private void giveLobbyItems(Player player) {
        // Bússola no slot 4 (índice 3)
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        compassMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aSelecione um servidor &7(Clique c/direito)"));
        compass.setItemMeta(compassMeta);
        player.getInventory().setItem(3, compass);
        
        // Cabeça do jogador no slot 6 (índice 5)
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(player.getName());
        skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aMeu Perfil"));
        skull.setItemMeta(skullMeta);
        player.getInventory().setItem(5, skull);
        
        // Atualizar inventário
        player.updateInventory();
    }
}
