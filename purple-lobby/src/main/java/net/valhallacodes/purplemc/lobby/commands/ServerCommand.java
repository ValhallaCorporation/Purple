/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.commands;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServerCommand implements CommandExecutor {
    
    private final PurpleLobby plugin;
    
    public ServerCommand(PurpleLobby plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cApenas jogadores podem usar este comando!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUso: /server <servidor>"));
            return true;
        }
        
        String serverName = args[0].toLowerCase();
        
        // Verificar se o servidor é válido
        if (!isValidServer(serverName)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cServidor inválido!"));
            return true;
        }
        
        // Conectar ao servidor
        connectToServer(player, serverName);
        return true;
    }
    
    private boolean isValidServer(String serverName) {
        return serverName.equals("lobby") ||
               serverName.equals("bedwars") || 
               serverName.equals("duels") || 
               serverName.equals("hideseek") || 
               serverName.equals("rankup");
    }
    
    private void connectToServer(Player player, String serverName) {
        // Enviar comando BungeeCord para conectar ao servidor
        
        try {
            // Usar ByteArrayOutputStream e DataOutputStream como no midup-lobby
            java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream out = new java.io.DataOutputStream(b);

            out.writeUTF("Connect");
            out.writeUTF(serverName);

            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } catch (java.io.IOException e) {
            plugin.getLogger().warning("Erro ao conectar jogador ao servidor " + serverName + ": " + e.getMessage());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao conectar ao servidor. Tente novamente."));
        }
    }
}
