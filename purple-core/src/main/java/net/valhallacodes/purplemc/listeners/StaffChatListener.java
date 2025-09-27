/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.listeners;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.valhallacodes.purplemc.PurpleCore;
import net.valhallacodes.purplemc.commands.StaffChatCommand;
import net.valhallacodes.purplemc.models.Player;

public class StaffChatListener implements Listener {

    private final PurpleCore plugin;
    private final StaffChatCommand staffChatCommand;

    public StaffChatListener(PurpleCore plugin, StaffChatCommand staffChatCommand) {
        this.plugin = plugin;
        this.staffChatCommand = staffChatCommand;
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String message = event.getMessage();

        if (message.startsWith("/")) {
            return;
        }

        if (!staffChatCommand.isStaffChatEnabled(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        
        plugin.getPlayerManager().getPlayer(player.getUniqueId()).thenAccept(playerData -> {
            if (playerData != null) {
                staffChatCommand.sendStaffMessage(player, playerData, message);
            }
        });
    }

    private boolean canUseStaffChat(net.valhallacodes.purplemc.enums.Rank rank) {
        return rank == net.valhallacodes.purplemc.enums.Rank.ADMIN || 
               rank == net.valhallacodes.purplemc.enums.Rank.COORD || 
               rank == net.valhallacodes.purplemc.enums.Rank.MOD_PLUS || 
               rank == net.valhallacodes.purplemc.enums.Rank.MOD || 
               rank == net.valhallacodes.purplemc.enums.Rank.CREATOR_PLUS;
    }
}
