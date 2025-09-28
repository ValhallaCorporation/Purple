/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.gui;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
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
import java.util.HashMap;
import java.util.Map;

public class LunaPlusColorGUI implements Listener {
    
    private final PurpleLobby plugin;
    private final String guiTitle = ChatColor.translateAlternateColorCodes('&', "&8Escolha a cor do + da LUNA+");
    
    // Mapa de cores disponíveis
    private final Map<Integer, String> colorMap = new HashMap<>();
    
    public LunaPlusColorGUI(PurpleLobby plugin) {
        this.plugin = plugin;
        initializeColorMap();
    }
    
    private void initializeColorMap() {
        // Cores disponíveis para o + da LUNA+
        colorMap.put(10, "§4"); // Vermelho escuro
        colorMap.put(11, "§c"); // Vermelho claro
        colorMap.put(12, "§6"); // Dourado
        colorMap.put(13, "§e"); // Amarelo
        colorMap.put(14, "§a"); // Verde
        colorMap.put(15, "§2"); // Verde escuro
        colorMap.put(16, "§b"); // Aqua
        colorMap.put(19, "§3"); // Aqua escuro
        colorMap.put(20, "§9"); // Azul
        colorMap.put(21, "§1"); // Azul escuro
        colorMap.put(22, "§d"); // Magenta
        colorMap.put(23, "§5"); // Magenta escuro (padrão)
        colorMap.put(24, "§f"); // Branco
        colorMap.put(25, "§7"); // Cinza
    }
    
    public void openLunaPlusColorGUI(Player player) {
        // Verificar se o jogador tem rank LUNA_PLUS
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());
        if (playerData == null || playerData.getRank() != net.valhallacodes.purplemc.lobby.enums.Rank.LUNA_PLUS) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cVocê precisa ter o rank LUNA+ para personalizar a cor!"));
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 36, guiTitle);
        
        // Preencher com vidro preto
        ItemStack blackGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = blackGlass.getItemMeta();
        glassMeta.setDisplayName(" ");
        blackGlass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 36; i++) {
            gui.setItem(i, blackGlass);
        }
        
        // Adicionar cores disponíveis
        for (Map.Entry<Integer, String> entry : colorMap.entrySet()) {
            int slot = entry.getKey();
            String colorCode = entry.getValue();
            
            ItemStack colorItem = new ItemStack(Material.WOOL, 1, getWoolColor(colorCode));
            ItemMeta colorMeta = colorItem.getItemMeta();
            
            String colorName = getColorName(colorCode);
            colorMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
                colorCode + "&l" + colorName + " &7(Clique para escolher)"));
            
            colorMeta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&', "&7Cor atual: " + colorCode + "&l+"),
                ChatColor.translateAlternateColorCodes('&', "&7Exemplo: &5&lLUNA" + colorCode + "&l+")
            ));
            
            colorItem.setItemMeta(colorMeta);
            gui.setItem(slot, colorItem);
        }
        
        // Botão de voltar
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cVoltar"));
        backMeta.setLore(Arrays.asList(
            ChatColor.translateAlternateColorCodes('&', "&7Clique para voltar ao perfil")
        ));
        backButton.setItemMeta(backMeta);
        gui.setItem(31, backButton);
        
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        // Verificar se é o GUI de cor da LUNA+
        if (event.getView().getTitle().equals(guiTitle)) {
            event.setCancelled(true);
            
            int slot = event.getSlot();
            
            if (slot == 31) {
                // Botão de voltar
                player.closeInventory();
                plugin.getProfileGUI().openProfileGUI(player);
                return;
            }
            
            if (colorMap.containsKey(slot)) {
                String selectedColor = colorMap.get(slot);
                updateLunaPlusColor(player, selectedColor);
                player.closeInventory();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    "&aCor do + da LUNA+ alterada para: " + selectedColor + "&l+"));
            }
        }
    }
    
    private void updateLunaPlusColor(Player player, String color) {
        try {
            // Validar se a cor é válida
            if (!isValidColorCode(color)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cCódigo de cor inválido!"));
                return;
            }
            
            PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());
            if (playerData != null) {
                // Criar novo PlayerData com a cor atualizada
                PlayerManager.PlayerData updatedPlayerData = new PlayerManager.PlayerData(
                    playerData.getUuid(),
                    playerData.getName(),
                    playerData.getRank(),
                    playerData.getTag(),
                    playerData.getPrefixType(),
                    color
                );
                
                // Salvar dados atualizados
                plugin.getPlayerManager().savePlayerData(updatedPlayerData);
                
                // Atualizar display name do jogador se ele tiver rank LUNA_PLUS
                if (playerData.getRank() == net.valhallacodes.purplemc.lobby.enums.Rank.LUNA_PLUS) {
                    // Forçar atualização imediata
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        updatePlayerDisplayName(player, updatedPlayerData);
                        
                        // Invalidar cache e atualizar scoreboard
                        plugin.getScoreboardManager().invalidatePlayerCache(player.getUniqueId());
                        plugin.getScoreboardManager().refreshPlayerScoreboard(player);
                        
                        // Forçar atualização adicional após um pequeno delay
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            plugin.getScoreboardManager().updatePlayerTabNameSafe(player);
                        }, 5L);
                    });
                }
            }
        } catch (Exception e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cErro ao alterar cor da LUNA+"));
            plugin.getLogger().warning("Erro ao atualizar cor da LUNA+ para " + player.getName() + ": " + e.getMessage());
        }
    }
    
    private boolean isValidColorCode(String color) {
        if (color == null || color.length() != 2) {
            return false;
        }
        
        // Verificar se é um código de cor válido do Minecraft
        String validCodes = "0123456789abcdef";
        return color.startsWith("§") && validCodes.contains(color.substring(1));
    }
    
    private void updatePlayerDisplayName(Player player, PlayerManager.PlayerData playerData) {
        String formattedName = getFormattedLunaPlusName(playerData);
        player.setDisplayName(formattedName);
        player.setPlayerListName(formattedName);
    }
    
    // Método para limpar códigos de cor corrompidos
    public void cleanCorruptedColors() {
        try {
            // Limpar cores corrompidas no banco de dados
            plugin.getMySQLManager().getConnection().createStatement().executeUpdate(
                "UPDATE players SET luna_plus_color = '§5' WHERE luna_plus_color NOT LIKE '§[0-9a-f]' OR luna_plus_color IS NULL"
            );
            plugin.getLogger().info("Cores corrompidas da LUNA+ foram limpas!");
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao limpar cores corrompidas: " + e.getMessage());
        }
    }
    
    private String getFormattedLunaPlusName(PlayerManager.PlayerData playerData) {
        String lunaColor = playerData.getLunaPlusColor();
        String playerName = playerData.getPrefixType().getColor() + playerData.getName();
        // Usar o método correto do Tag para formatação
        return playerData.getTag().getColoredPrefix(lunaColor) + " " + playerName;
    }
    
    private short getWoolColor(String colorCode) {
        switch (colorCode) {
            case "§4": return 14; // Vermelho escuro
            case "§c": return 14; // Vermelho claro
            case "§6": return 4;  // Dourado
            case "§e": return 4;  // Amarelo
            case "§a": return 5;  // Verde
            case "§2": return 13; // Verde escuro
            case "§b": return 3;  // Aqua
            case "§3": return 9;  // Aqua escuro
            case "§9": return 11; // Azul
            case "§1": return 11; // Azul escuro
            case "§d": return 2;  // Magenta
            case "§5": return 10; // Magenta escuro
            case "§f": return 0;  // Branco
            case "§7": return 8;  // Cinza
            default: return 10;  // Magenta escuro (padrão)
        }
    }
    
    private String getColorName(String colorCode) {
        switch (colorCode) {
            case "§4": return "Vermelho Escuro";
            case "§c": return "Vermelho Claro";
            case "§6": return "Dourado";
            case "§e": return "Amarelo";
            case "§a": return "Verde";
            case "§2": return "Verde Escuro";
            case "§b": return "Aqua";
            case "§3": return "Aqua Escuro";
            case "§9": return "Azul";
            case "§1": return "Azul Escuro";
            case "§d": return "Magenta";
            case "§5": return "Magenta Escuro";
            case "§f": return "Branco";
            case "§7": return "Cinza";
            default: return "Desconhecido";
        }
    }
}
