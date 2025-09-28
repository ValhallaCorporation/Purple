/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.gui;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
import net.valhallacodes.purplemc.lobby.enums.Rank;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class ProfileGUI implements Listener {
    
    private final PurpleLobby plugin;
    private final String guiTitle = ChatColor.translateAlternateColorCodes('&', "&8Meu perfil");
    
    public ProfileGUI(PurpleLobby plugin) {
        this.plugin = plugin;
    }
    
    public void openProfileGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);
        
        // Preencher com vidro preto
        ItemStack blackGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = blackGlass.getItemMeta();
        glassMeta.setDisplayName(" ");
        blackGlass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, blackGlass);
        }
        
        ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();
        headMeta.setOwner(player.getName());
        
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());
        if (playerData != null) {
            Rank rank = playerData.getRank();
            String rankColor = rank.getColor();
            String rankName = rank.getDisplayName();
            
            String playerName = rankColor + "&l" + rankName.toUpperCase() + " " + rankColor + player.getName();
            
            String firstLogin = getFirstLoginFormatted(player.getUniqueId());
            
            headMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', playerName));
            headMeta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&', "&7" + firstLogin),
                ChatColor.translateAlternateColorCodes('&', "&7Rank: " + rankColor + rankName + " &7(Permanente)")
            ));
        } else {
            headMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7" + player.getName()));
            headMeta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&', "&7Primeiro login: Desconhecido"),
                ChatColor.translateAlternateColorCodes('&', "&7Rank: Membro")
            ));
        }
        
        playerHead.setItemMeta(headMeta);
        gui.setItem(13, playerHead);
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // Verificar se é o GUI de perfil
        if (event.getView().getTitle().equals(guiTitle)) {
            event.setCancelled(true);
            
            // Fechar GUI se clicar em qualquer lugar
            player.closeInventory();
        }
    }
    
    private String getFirstLoginFormatted(java.util.UUID uuid) {
        try {
            java.sql.Connection connection = plugin.getMySQLManager().getConnection();
            if (connection == null) {
                return "Primeiro login: Desconhecido";
            }
            
            java.sql.PreparedStatement stmt = connection.prepareStatement(
                "SELECT first_login FROM players WHERE uuid = ?"
            );
            stmt.setString(1, uuid.toString());
            java.sql.ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Timestamp firstLogin = rs.getTimestamp("first_login");
                if (firstLogin != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    String formattedDate = sdf.format(new Date(firstLogin.getTime()));
                    rs.close();
                    stmt.close();
                    return "Primeiro login: " + formattedDate;
                }
            }
            
            rs.close();
            stmt.close();
            return "Primeiro login: Desconhecido";
            
        } catch (Exception e) {
            return "Primeiro login: Desconhecido";
        }
    }
}
