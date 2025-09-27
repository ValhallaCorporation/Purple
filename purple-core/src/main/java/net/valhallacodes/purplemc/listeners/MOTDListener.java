/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.listeners;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.valhallacodes.purplemc.PurpleCore;

public class MOTDListener implements Listener {
    
    private final PurpleCore plugin;
    
    public MOTDListener(PurpleCore plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onProxyPing(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();
        
        ping.setDescriptionComponent(new net.md_5.bungee.api.chat.TextComponent(plugin.getMOTDManager().getMOTD()));
        ping.setVersion(plugin.getMOTDManager().getProtocol());
        ping.setPlayers(plugin.getMOTDManager().getPlayers());
        
        event.setResponse(ping);
    }
}
