/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.commands;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.enums.Rank;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
    
    private final PurpleLobby plugin;
    
    public FlyCommand(PurpleLobby plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!hasFlyPermission(player)) {
            player.sendMessage("§cVocê precisa ter rank Booster ou superior para usar este comando!");
            return true;
        }
        
        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage("§cModo de voo desativado!");
        } else {
            player.setAllowFlight(true);
            player.sendMessage("§aModo de voo ativado!");
        }
        
        return true;
    }
    
    private boolean hasFlyPermission(Player player) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());
        if (playerData == null) {
            return false;
        }
        
        Rank rank = playerData.getRank();
        return rank.ordinal() >= Rank.BOOSTER.ordinal();
    }
}
