/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.utils;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class LocationUtils {
    
    private final PurpleLobby plugin;
    
    public LocationUtils(PurpleLobby plugin) {
        this.plugin = plugin;
    }
    
    public Location getLobbySpawn() {
        FileConfiguration config = plugin.getConfig();
        
        if (!config.contains("lobby.spawn")) {
            // Se não há spawn definido, usar o spawn padrão do mundo
            World world = Bukkit.getWorlds().get(0);
            if (world != null) {
                return world.getSpawnLocation();
            }
            return null;
        }
        
        String worldName = config.getString("lobby.spawn.world", "world");
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            plugin.getLogger().warning("Mundo '" + worldName + "' não encontrado! Usando spawn padrão.");
            world = Bukkit.getWorlds().get(0);
            if (world == null) {
                return null;
            }
            return world.getSpawnLocation();
        }
        
        double x = config.getDouble("lobby.spawn.x");
        double y = config.getDouble("lobby.spawn.y");
        double z = config.getDouble("lobby.spawn.z");
        float yaw = (float) config.getDouble("lobby.spawn.yaw", 0.0);
        float pitch = (float) config.getDouble("lobby.spawn.pitch", 0.0);
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    public void setLobbySpawn(Location location) {
        FileConfiguration config = plugin.getConfig();
        
        config.set("lobby.spawn.world", location.getWorld().getName());
        config.set("lobby.spawn.x", location.getX());
        config.set("lobby.spawn.y", location.getY());
        config.set("lobby.spawn.z", location.getZ());
        config.set("lobby.spawn.yaw", location.getYaw());
        config.set("lobby.spawn.pitch", location.getPitch());
        
        plugin.saveConfig();
    }
}
