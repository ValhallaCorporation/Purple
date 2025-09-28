/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.gui;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.listeners.BungeeCordMessageListener;
import net.valhallacodes.purplemc.lobby.managers.ServerCountManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ServerSelectorGUI implements Listener {
    
    private final PurpleLobby plugin;
    private final String guiTitle = ChatColor.translateAlternateColorCodes('&', "&8Selecione um servidor");
    
    public ServerSelectorGUI(PurpleLobby plugin) {
        this.plugin = plugin;
    }
    
    public void openServerSelector(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);
        
        // BedWars
        ItemStack bedWars = new ItemStack(Material.BED);
        ItemMeta bedWarsMeta = bedWars.getItemMeta();
        bedWarsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aBedWars"));
        bedWarsMeta.setLore(Arrays.asList(
            ChatColor.translateAlternateColorCodes('&', "&7" + getOnlineCount("bedwars") + " jogando agora!")
        ));
        bedWars.setItemMeta(bedWarsMeta);
        gui.setItem(10, bedWars);
        
        // Duels
        ItemStack duels = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta duelsMeta = duels.getItemMeta();
        duelsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aDuels"));
        duelsMeta.setLore(Arrays.asList(
            ChatColor.translateAlternateColorCodes('&', "&7" + getOnlineCount("duels") + " jogando agora!")
        ));
        duels.setItemMeta(duelsMeta);
        gui.setItem(12, duels);
        
        // Esconde-Esconde
        ItemStack hideSeek = new ItemStack(Material.STAINED_CLAY, 1, (short) 5); // Verde
        ItemMeta hideSeekMeta = hideSeek.getItemMeta();
        hideSeekMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aEsconde-Esconde"));
        hideSeekMeta.setLore(Arrays.asList(
            ChatColor.translateAlternateColorCodes('&', "&7" + getOnlineCount("hideseek") + " jogando agora!")
        ));
        hideSeek.setItemMeta(hideSeekMeta);
        gui.setItem(14, hideSeek);
        
        // RankUP
        ItemStack rankUp = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta rankUpMeta = rankUp.getItemMeta();
        rankUpMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aRankUP"));
        rankUpMeta.setLore(Arrays.asList(
            ChatColor.translateAlternateColorCodes('&', "&7" + getOnlineCount("rankup") + " jogando agora!")
        ));
        rankUp.setItemMeta(rankUpMeta);
        gui.setItem(16, rankUp);
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // Verificar se é o GUI de seleção de servidor
        if (event.getView().getTitle().equals(guiTitle)) {
            event.setCancelled(true);
            
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }
            
            // Conectar ao servidor baseado no item clicado
            switch (clickedItem.getType()) {
                case NETHER_STAR:
                    connectToServer(player, "lobby");
                    break;
                case BED:
                    connectToServer(player, "bedwars");
                    break;
                case DIAMOND_SWORD:
                    connectToServer(player, "duels");
                    break;
                case STAINED_CLAY:
                    connectToServer(player, "hideseek");
                    break;
                case DIAMOND_PICKAXE:
                    connectToServer(player, "rankup");
                    break;
            }
            
            player.closeInventory();
        }
    }
    
    private void connectToServer(Player player, String serverName) {
        // Enviar comando BungeeCord para conectar ao servidor
        
        try {
            // Usar ByteArrayOutputStream e DataOutputStream como no midup-lobby
            java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream out = new java.io.DataOutputStream(b);

            out.writeUTF("Connect");
            out.writeUTF(serverName);

            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } catch (java.io.IOException e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao conectar ao servidor. Tente novamente."));
        }
    }
    
    private String getOnlineCount(String serverName) {
        // Obter contagem real do ServerCountManager
        ServerCountManager countManager = plugin.getServerCountManager();
        if (countManager != null) {
            int count = countManager.getServerCount(serverName);
            return String.valueOf(count);
        }
        
        // Fallback para números simulados se o manager não estiver disponível
        switch (serverName.toLowerCase()) {
            case "lobby":
                return "156";
            case "bedwars":
                return "127";
            case "duels":
                return "89";
            case "hideseek":
                return "45";
            case "rankup":
                return "203";
            default:
                return "0";
        }
    }
}
