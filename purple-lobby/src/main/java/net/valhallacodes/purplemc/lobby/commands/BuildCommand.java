/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.commands;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.enums.Rank;
import net.valhallacodes.purplemc.lobby.listeners.LobbyProtectionListener;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildCommand implements CommandExecutor {
    
    private final PurpleLobby plugin;
    private final LobbyProtectionListener protectionListener;
    
    public BuildCommand(PurpleLobby plugin, LobbyProtectionListener protectionListener) {
        this.plugin = plugin;
        this.protectionListener = protectionListener;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!hasStaffPermission(player)) {
            player.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }
        
        protectionListener.toggleBuildMode(player);
        return true;
    }
    
    private boolean hasStaffPermission(Player player) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());
        if (playerData == null) {
            return false;
        }
        
        Rank rank = playerData.getRank();
        return rank == Rank.ADMIN || rank == Rank.COORD || rank == Rank.MOD_PLUS || 
               rank == Rank.MOD || rank == Rank.CREATOR_PLUS;
    }
}
