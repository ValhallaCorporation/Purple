/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.ChatColor;
import net.valhallacodes.purplemc.PurpleCore;

public class GoCommand extends Command {
    
    private final PurpleCore plugin;
    
    public GoCommand(PurpleCore plugin) {
        super("go");
        this.plugin = plugin;
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser usado por jogadores!");
            return;
        }
        
        ProxiedPlayer player = (ProxiedPlayer) sender;
        
        plugin.getPlayerManager().getPlayer(player.getUniqueId()).thenAccept(playerData -> {
            if (playerData == null || !playerData.canUseGo()) {
                player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando!");
                return;
            }
            
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Uso: /go <jogador>");
                return;
            }
            
            String targetName = args[0];
            ProxiedPlayer target = plugin.getProxy().getPlayer(targetName);
            
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Jogador '" + targetName + "' não encontrado!");
                return;
            }
            
            if (target.getServer() == null) {
                player.sendMessage(ChatColor.RED + "O jogador não está em nenhum servidor!");
                return;
            }
            
            player.connect(target.getServer().getInfo());
            player.sendMessage(ChatColor.GREEN + "Teleportando para " + target.getName() + "...");
        });
    }
}
