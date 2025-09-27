/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.commands;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.valhallacodes.purplemc.PurpleCore;
import net.valhallacodes.purplemc.enums.Tag;
import net.valhallacodes.purplemc.enums.Rank;
import net.valhallacodes.purplemc.managers.PlayerManager;
import net.valhallacodes.purplemc.models.Player;

import java.util.*;

public class TagCommand extends Command {

    private final PurpleCore plugin;
    private final PlayerManager playerManager;
    private final Map<UUID, Player> playerDataCache;

    public TagCommand(PurpleCore plugin) {
        super("tag");
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
        this.playerDataCache = new HashMap<>();
    }

    @Override
    public void execute(net.md_5.bungee.api.CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando!");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!playerDataCache.containsKey(player.getUniqueId())) {
            playerManager.getPlayer(player.getUniqueId()).thenAccept(data -> {
                if (data != null) {
                    playerDataCache.put(player.getUniqueId(), data);
                    executeCommand(player, args);
                }
            });
            return;
        }

        executeCommand(player, args);
    }

    private void executeCommand(ProxiedPlayer player, String[] args) {
        if (args.length == 0) {
            showAvailableTags(player);
        } else {
            selectTag(player, args[0]);
        }
    }

    private void showAvailableTags(ProxiedPlayer player) {
        List<Tag> availableTags = new ArrayList<>();
        
        Tag[] orderedTags = {Tag.ADMIN, Tag.COORD, Tag.MOD_PLUS, Tag.MOD, Tag.CREATOR_PLUS, 
                           Tag.CREATOR, Tag.LUNA_PLUS, Tag.BETA, Tag.LUNA, Tag.VIP, Tag.BOOSTER, Tag.WINNER, Tag.MEMBER};
        
        for (Tag tag : orderedTags) {
            if (hasPermissionForTag(player, tag)) {
                availableTags.add(tag);
            }
        }
        
        if (availableTags.isEmpty()) {
            player.sendMessage("§cVocê não possui permissão para usar nenhuma tag!");
            return;
        }
        
        StringBuilder message = new StringBuilder("§aSuas tags: ");
        
        for (int i = 0; i < availableTags.size(); i++) {
            Tag tag = availableTags.get(i);
            
            Player playerData = playerDataCache.get(player.getUniqueId());
            boolean isCurrentTag = playerData != null && playerData.getTag() == tag;
            
            message.append(tag.getColor()).append(tag.getName());
            
            if (isCurrentTag) {
                message.append("§f (atual)");
            }
            
            if (i < availableTags.size() - 1) {
                message.append("§f, ");
            }
        }
        
        player.sendMessage(message.toString());
    }

    private void selectTag(ProxiedPlayer player, String tagName) {
        Tag tag = Tag.fromUsages(tagName);

        if (tag == null) {
            player.sendMessage("§cTag não encontrada!");
            return;
        }

        if (!hasPermissionForTag(player, tag)) {
            player.sendMessage("§cVocê não possui permissão para usar esta tag!");
            return;
        }

        Player playerData = playerDataCache.get(player.getUniqueId());
        
        if (playerData != null && playerData.getTag() == tag) {
            player.sendMessage("§cVocê já está usando esta tag!");
            return;
        }

        playerManager.setTag(player.getUniqueId(), tag).thenAccept(success -> {
            if (success) {
                if (playerData != null) {
                    playerData.setTag(tag);
                }
                
                player.sendMessage("§aTag alterada para: " + tag.getColor() + tag.getName());
            } else {
                player.sendMessage("§cErro ao salvar a tag! Tente novamente.");
            }
        });
    }

    private boolean hasPermissionForTag(ProxiedPlayer player, Tag tag) {
        Player playerData = playerDataCache.get(player.getUniqueId());
        
        if (playerData == null) {
            return tag == Tag.MEMBER;
        }
        
        Rank playerRank = playerData.getRank();
        
        switch (playerRank) {
            case ADMIN:
                return true;
                
            case COORD:
                return tag == Tag.COORD || tag == Tag.MOD_PLUS || tag == Tag.MOD || 
                       tag == Tag.CREATOR_PLUS || tag == Tag.CREATOR || tag == Tag.LUNA_PLUS || 
                       tag == Tag.BETA || tag == Tag.LUNA || tag == Tag.VIP || 
                       tag == Tag.BOOSTER || tag == Tag.WINNER || tag == Tag.MEMBER;
                
            case MOD_PLUS:
                return tag == Tag.MOD_PLUS || tag == Tag.MOD || tag == Tag.CREATOR_PLUS || 
                       tag == Tag.CREATOR || tag == Tag.LUNA_PLUS || tag == Tag.BETA || 
                       tag == Tag.LUNA || tag == Tag.VIP || tag == Tag.BOOSTER || 
                       tag == Tag.WINNER || tag == Tag.MEMBER;
                
            case MOD:
                return tag == Tag.MOD || tag == Tag.CREATOR_PLUS || tag == Tag.CREATOR || 
                       tag == Tag.LUNA_PLUS || tag == Tag.BETA || tag == Tag.LUNA || 
                       tag == Tag.VIP || tag == Tag.BOOSTER || tag == Tag.WINNER || tag == Tag.MEMBER;
                
            case CREATOR_PLUS:
                return tag == Tag.CREATOR_PLUS || tag == Tag.CREATOR || tag == Tag.LUNA_PLUS || 
                       tag == Tag.BETA || tag == Tag.LUNA || tag == Tag.VIP || 
                       tag == Tag.BOOSTER || tag == Tag.WINNER || tag == Tag.MEMBER;
                
            case CREATOR:
                return tag == Tag.CREATOR || tag == Tag.LUNA_PLUS || tag == Tag.BETA || 
                       tag == Tag.LUNA || tag == Tag.VIP || tag == Tag.BOOSTER || 
                       tag == Tag.WINNER || tag == Tag.MEMBER;
                
            case LUNA_PLUS:
                return tag == Tag.LUNA_PLUS || tag == Tag.BETA || tag == Tag.LUNA || 
                       tag == Tag.VIP || tag == Tag.BOOSTER || tag == Tag.WINNER || tag == Tag.MEMBER;
                
            case BETA:
                return tag == Tag.BETA || tag == Tag.LUNA || tag == Tag.VIP || 
                       tag == Tag.BOOSTER || tag == Tag.WINNER || tag == Tag.MEMBER;
                
            case LUNA:
                return tag == Tag.LUNA || tag == Tag.VIP || tag == Tag.BOOSTER || 
                       tag == Tag.WINNER || tag == Tag.MEMBER;
                
            case VIP:
                return tag == Tag.VIP || tag == Tag.BOOSTER || tag == Tag.WINNER || tag == Tag.MEMBER;
                
            case BOOSTER:
                return tag == Tag.BOOSTER || tag == Tag.WINNER || tag == Tag.MEMBER;
                
            case WINNER:
                return tag == Tag.WINNER || tag == Tag.MEMBER;
                
            case MEMBRO:
            default:
                return tag == Tag.MEMBER;
        }
    }
}
