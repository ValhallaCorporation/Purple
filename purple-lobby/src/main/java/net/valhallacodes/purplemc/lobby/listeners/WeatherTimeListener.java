/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class WeatherTimeListener implements Listener {

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        configureWorld(world);
    }

    public static void configureWorld(World world) {
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(0);
        world.setTime(6000); // Meio-dia
        world.setGameRuleValue("doDaylightCycle", "false");
    }
}
