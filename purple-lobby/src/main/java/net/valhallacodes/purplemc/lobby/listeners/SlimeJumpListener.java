/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class SlimeJumpListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        Material blockBelow = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        
        if (blockBelow == Material.SLIME_BLOCK) {
            if (player.isOnGround()) {
                Vector direction = player.getLocation().getDirection();
                
                direction.setY(0);
                direction.normalize();
                
                direction.multiply(1.5);
                
                direction.setY(0.8);
                player.setVelocity(direction);
                
                try {
                    player.playSound(player.getLocation(), Sound.SLIME_WALK, 1.0f, 1.0f);
                } catch (Exception e) {
                    try {
                        player.playSound(player.getLocation(), Sound.valueOf("SLIME_WALK2"), 1.0f, 1.0f);
                    } catch (Exception e2) {
                    }
                }
            }
        }
    }
}
