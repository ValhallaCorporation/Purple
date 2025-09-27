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
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.valhallacodes.purplemc.PurpleCore;
import net.valhallacodes.purplemc.managers.PlayerManager;
import net.valhallacodes.purplemc.models.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportCommand extends Command {
    
    private final PurpleCore plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    
    public ReportCommand(PurpleCore plugin) {
        super("report");
        this.plugin = plugin;
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser usado por jogadores!");
            return;
        }
        
        ProxiedPlayer player = (ProxiedPlayer) sender;
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /report <jogador> <motivo>");
            return;
        }
        
        UUID playerId = player.getUniqueId();
        long cooldownTime = 5000; // 5 segundos
        
        if (cooldowns.containsKey(playerId)) {
            long timeLeft = (cooldowns.get(playerId) + cooldownTime) - System.currentTimeMillis();
            if (timeLeft > 0) {
                player.sendMessage(ChatColor.RED + "Aguarde " + (timeLeft / 1000) + " segundos antes de reportar novamente!");
                return;
            }
        }
        
        String targetName = args[0];
        ProxiedPlayer target = plugin.getProxy().getPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Jogador '" + targetName + "' não encontrado!");
            return;
        }
        
        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "Você não pode reportar a si mesmo!");
            return;
        }
        
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]);
            if (i < args.length - 1) {
                reasonBuilder.append(" ");
            }
        }
        String reason = reasonBuilder.toString();
        
        cooldowns.put(playerId, System.currentTimeMillis());
        
        player.sendMessage(ChatColor.GREEN + "Report enviado com sucesso!");
        
        String serverName = target.getServer() != null ? target.getServer().getInfo().getName() : "Desconhecido";
        
        notifyStaff(player.getName(), target.getName(), reason, serverName);
    }
    
    private void notifyStaff(String reporter, String accused, String reason, String serverName) {
        TextComponent message = new TextComponent();
        
        message.addExtra(new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "[REPORT]\n"));
        message.addExtra(new TextComponent(ChatColor.RED + "Acusado: " + ChatColor.WHITE + accused + "\n"));
        message.addExtra(new TextComponent(ChatColor.RED + "Quem acusou: " + ChatColor.WHITE + reporter + "\n"));
        message.addExtra(new TextComponent(ChatColor.RED + "Motivo: " + ChatColor.WHITE + reason + "\n"));
        message.addExtra(new TextComponent(ChatColor.RED + "Servidor: " + ChatColor.WHITE + serverName + "\n\n"));
        
        TextComponent teleportButton = new TextComponent(ChatColor.YELLOW + "[CLIQUE AQUI PARA TELEPORTAR]");
        teleportButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/go " + accused));
        teleportButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("Clique para se teleportar até " + accused).create()));
        
        message.addExtra(teleportButton);
        
        for (ProxiedPlayer staff : plugin.getProxy().getPlayers()) {
            plugin.getPlayerManager().getPlayer(staff.getUniqueId()).thenAccept(playerData -> {
                if (playerData != null && playerData.canSeeReports()) {
                    staff.sendMessage(message);
                }
            });
        }
    }
}
