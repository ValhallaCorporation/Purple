/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.valhallacodes.purplemc.PurpleCore;
import net.valhallacodes.purplemc.enums.Rank;
import net.valhallacodes.purplemc.models.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffChatCommand extends Command {
    
    private final PurpleCore plugin;
    private final Set<UUID> staffChatEnabled;
    
    public StaffChatCommand(PurpleCore plugin) {
        super("sc", null, "staffchat");
        this.plugin = plugin;
        this.staffChatEnabled = new HashSet<>();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return;
        }
        
        ProxiedPlayer player = (ProxiedPlayer) sender;
        
        plugin.getPlayerManager().getPlayer(player.getUniqueId()).thenAccept(playerData -> {
            if (playerData == null || !canUseStaffChat(playerData.getRank())) {
                player.sendMessage("§cVocê não tem permissão para usar este comando!");
                return;
            }
            
            if (args.length == 0) {
                boolean enabled = toggleStaffChat(player.getUniqueId());
                
                if (enabled) {
                    player.sendMessage("§a§l[SC] §aStaffchat ativado! Digite suas mensagens normalmente.");
                } else {
                    player.sendMessage("§c§l[SC] §cStaffchat desativado!");
                }
                return;
            }
            
            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) messageBuilder.append(" ");
                messageBuilder.append(args[i]);
            }
            
            String message = messageBuilder.toString();
            if (message.trim().isEmpty()) {
                player.sendMessage("§cMensagem não pode estar vazia!");
                return;
            }
            
            sendStaffMessageDirect(player, playerData, message);
        });
    }
    
    private boolean canUseStaffChat(Rank rank) {
        return rank == Rank.ADMIN || rank == Rank.COORD || rank == Rank.MOD_PLUS || 
               rank == Rank.MOD || rank == Rank.CREATOR_PLUS;
    }
    
    private boolean toggleStaffChat(UUID uuid) {
        if (staffChatEnabled.contains(uuid)) {
            staffChatEnabled.remove(uuid);
            return false;
        } else {
            staffChatEnabled.add(uuid);
            return true;
        }
    }
    
    public boolean isStaffChatEnabled(UUID uuid) {
        return staffChatEnabled.contains(uuid);
    }
    
    private void sendStaffMessageDirect(ProxiedPlayer sender, Player playerData, String message) {
        String formattedMessage = "§5§l[SC] " + playerData.getRank().getColoredPrefix() + 
                                playerData.getTag().getColor() + sender.getName() + 
                                "§f: " + message;
        
        for (ProxiedPlayer staff : plugin.getProxy().getPlayers()) {
            plugin.getPlayerManager().getPlayer(staff.getUniqueId()).thenAccept(staffData -> {
                if (staffData != null && canUseStaffChat(staffData.getRank())) {
                    staff.sendMessage(formattedMessage);
                }
            });
        }
    }
    
    public void sendStaffMessage(ProxiedPlayer sender, Player playerData, String message) {
        String formattedMessage = "§5§l[SC] " + playerData.getRank().getColoredPrefix() + 
                                playerData.getTag().getColor() + sender.getName() + 
                                "§f: " + message;
        
        for (ProxiedPlayer staff : plugin.getProxy().getPlayers()) {
            plugin.getPlayerManager().getPlayer(staff.getUniqueId()).thenAccept(staffData -> {
                if (staffData != null && canUseStaffChat(staffData.getRank())) {
                    staff.sendMessage(formattedMessage);
                }
            });
        }
    }
}
