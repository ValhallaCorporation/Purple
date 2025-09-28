/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
import net.valhallacodes.purplemc.lobby.enums.Rank;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class FireworksListener implements Listener {
    
    private final PurpleLobby plugin;
    private final Random random = new Random();
    
    public FireworksListener(PurpleLobby plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (isVipOrStaff(player)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    launchFireworks(player);
                }
            }.runTaskLater(plugin, 20L);
        }
    }
    
    private boolean isVipOrStaff(Player player) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());
        if (playerData == null) {
            return false;
        }

        Rank rank = playerData.getRank();
        
        // Todos os ranks exceto MEMBRO recebem fogos de artifício
        return rank != Rank.MEMBRO;
    }
    
    private void launchFireworks(Player player) {
        Location playerLoc = player.getLocation();
        
        for (int i = 0; i < 5; i++) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    double x = playerLoc.getX() + (random.nextDouble() - 0.5) * 6;
                    double y = playerLoc.getY() + 1;
                    double z = playerLoc.getZ() + (random.nextDouble() - 0.5) * 6;
                    
                    Location fireworkLoc = new Location(playerLoc.getWorld(), x, y, z);
                    
                    Firework firework = (Firework) playerLoc.getWorld().spawnEntity(fireworkLoc, EntityType.FIREWORK);
                    FireworkMeta meta = firework.getFireworkMeta();
                    
                    FireworkEffect.Builder effectBuilder = FireworkEffect.builder();
                    
                    Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PURPLE, Color.ORANGE, Color.WHITE};
                    effectBuilder.withColor(colors[random.nextInt(colors.length)]);
                    effectBuilder.withFade(colors[random.nextInt(colors.length)]);
                    
                    FireworkEffect.Type[] types = {
                        FireworkEffect.Type.BALL,
                        FireworkEffect.Type.BALL_LARGE,
                        FireworkEffect.Type.STAR,
                        FireworkEffect.Type.BURST
                    };
                    effectBuilder.with(types[random.nextInt(types.length)]);
                    
                    if (random.nextBoolean()) {
                        effectBuilder.withFlicker();
                    }
                    if (random.nextBoolean()) {
                        effectBuilder.withTrail();
                    }
                    
                    meta.addEffect(effectBuilder.build());
                    meta.setPower(random.nextInt(2) + 1);
                    
                    firework.setFireworkMeta(meta);
                }
            }.runTaskLater(plugin, i * 10L);
        }
    }
}
