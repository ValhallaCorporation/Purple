/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */


package net.valhallacodes.purplemc.listeners;

import net.md_5.bungee.api.plugin.Listener;
import net.valhallacodes.purplemc.PurpleCore;

public class PlayerListener implements Listener {
    
    private final PurpleCore plugin;
    
    public PlayerListener(PurpleCore plugin) {
        this.plugin = plugin;
    }
    
}