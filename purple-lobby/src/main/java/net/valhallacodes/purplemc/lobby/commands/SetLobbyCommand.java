/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.commands;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.enums.Rank;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand implements CommandExecutor {
    
    private final PurpleLobby plugin;
    
    public SetLobbyCommand(PurpleLobby plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser executado por jogadores!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!hasAdminPermission(player)) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando!");
            return true;
        }
        
        plugin.getLocationUtils().setLobbySpawn(player.getLocation());
        
        player.sendMessage(ChatColor.GREEN + "Spawn do lobby definido com sucesso!");
        return true;
    }
    
    private boolean hasAdminPermission(Player player) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());
        if (playerData == null) {
            return false;
        }
        
        Rank rank = playerData.getRank();
        return rank == Rank.ADMIN || rank == Rank.COORD;
    }
}
