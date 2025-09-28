/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.commands;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.enums.Rank;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamemodeCommand implements CommandExecutor {
    
    private final PurpleLobby plugin;
    
    public GamemodeCommand(PurpleLobby plugin) {
        this.plugin = plugin;
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
        
        if (args.length == 0) {
            player.sendMessage("§cUso: /gamemode <0|1|2|3> [jogador]");
            return true;
        }
        
        GameMode gameMode;
        try {
            int mode = Integer.parseInt(args[0]);
            switch (mode) {
                case 0:
                    gameMode = GameMode.SURVIVAL;
                    break;
                case 1:
                    gameMode = GameMode.CREATIVE;
                    break;
                case 2:
                    gameMode = GameMode.ADVENTURE;
                    break;
                case 3:
                    gameMode = GameMode.SPECTATOR;
                    break;
                default:
                    player.sendMessage("§cModo de jogo inválido! Use 0, 1, 2 ou 3.");
                    return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cModo de jogo inválido! Use 0, 1, 2 ou 3.");
            return true;
        }
        
        Player target = player;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cJogador não encontrado!");
                return true;
            }
        }
        
        target.setGameMode(gameMode);
        
        String modeName = getGameModeName(gameMode);
        if (target == player) {
            player.sendMessage("§aModo de jogo alterado para " + modeName + "!");
        } else {
            player.sendMessage("§aModo de jogo de " + target.getName() + " alterado para " + modeName + "!");
            target.sendMessage("§aSeu modo de jogo foi alterado para " + modeName + " por " + player.getName() + "!");
        }
        
        return true;
    }
    
    private String getGameModeName(GameMode gameMode) {
        switch (gameMode) {
            case SURVIVAL:
                return "Sobrevivência";
            case CREATIVE:
                return "Criativo";
            case ADVENTURE:
                return "Aventura";
            case SPECTATOR:
                return "Espectador";
            default:
                return "Desconhecido";
        }
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
