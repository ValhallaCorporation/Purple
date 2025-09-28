package net.valhallacodes.purplemc.lobby.managers;

import net.valhallacodes.purplemc.lobby.PurpleLobby;
import net.valhallacodes.purplemc.lobby.managers.PlayerManager;
import net.valhallacodes.purplemc.lobby.enums.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VanishManager implements Listener {

    private final Set<UUID> vanishedPlayers;
    private final PurpleLobby plugin;
    private final Map<UUID, Integer> actionBarTasks; 

    public VanishManager(PurpleLobby plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashSet<>();
        this.actionBarTasks = new HashMap<>();
    }

    public boolean isVanished(UUID playerId) {
        return vanishedPlayers.contains(playerId);
    }

    public void setVanished(Player player, boolean vanished) {
        if (vanished) {
            vanishedPlayers.add(player.getUniqueId());
            hidePlayer(player);
            // Colocar no modo criativo
            player.setGameMode(org.bukkit.GameMode.CREATIVE);
            // Enviar actionbar e manter visível
            sendActionBar(player, "&cVocê está no modo vanish!");
            startActionBarTask(player);
        } else {
            vanishedPlayers.remove(player.getUniqueId());
            showPlayer(player);
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
            stopActionBarTask(player);
            sendActionBar(player, "");
        }
    }

    private boolean hasVanishPermission(Player player) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().loadPlayerData(player.getUniqueId());
        if (playerData == null) {
            return false;
        }

        Rank rank = playerData.getRank();
        return rank == Rank.ADMIN || rank == Rank.COORD || rank == Rank.MOD_PLUS || 
               rank == Rank.MOD || rank == Rank.CREATOR_PLUS;
    }

    private void hidePlayer(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!hasVanishPermission(onlinePlayer)) {
                onlinePlayer.hidePlayer(player); // 1.8.8 -> sem plugin
            }
        }
    }

    private void showPlayer(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(player);
        }
    }

    public void onPlayerJoin(Player player) {
        // Verificar se jogador que entrou tem permissão de vanish
        if (!hasVanishPermission(player)) {
            // Se não tem permissão e está vanished, remover do vanish
            if (isVanished(player.getUniqueId())) {
                setVanished(player, false);
                player.sendMessage("§cVocê não tem mais permissão para usar vanish!");
            }
        } else {
            // Se tem permissão, manter vanish se estiver vanished
            if (isVanished(player.getUniqueId())) {
                hidePlayer(player);
                startActionBarTask(player);
            }
        }

        // Esconder jogadores vanished para o jogador que entrou
        for (UUID vanishedId : vanishedPlayers) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedId);
            if (vanishedPlayer != null && vanishedPlayer.isOnline()) {
                if (!hasVanishPermission(player)) {
                    player.hidePlayer(vanishedPlayer);
                }
            }
        }
    }

    public void onPlayerQuit(Player player) {
        stopActionBarTask(player);
        sendActionBar(player, "");
    }

    public void checkAndCleanVanishPermissions() {
        // Verificar todos os jogadores vanished e remover aqueles sem permissão
        vanishedPlayers.removeIf(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                if (!hasVanishPermission(player)) {
                    setVanished(player, false);
                    return true; // Remove da lista
                }
            }
            return false; // Mantém na lista
        });
    }

    private void sendActionBar(Player player, String message) {
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);

        try {
            Object icbc = Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent$ChatSerializer")
                    .getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + coloredMessage + "\"}");

            // Criar packet de ActionBar
            Object packet = Class.forName("net.minecraft.server.v1_8_R3.PacketPlayOutChat")
                    .getConstructor(Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent"), byte.class)
                    .newInstance(icbc, (byte) 2);

            // Enviar packet para o jogador
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass()
                    .getMethod("sendPacket", Class.forName("net.minecraft.server.v1_8_R3.Packet"))
                    .invoke(playerConnection, packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startActionBarTask(Player player) {
        stopActionBarTask(player);
        
        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (isVanished(player.getUniqueId()) && player.isOnline()) {
                sendActionBar(player, "&cVocê está no modo vanish!");
            }
        }, 0L, 40L).getTaskId(); 
        
        actionBarTasks.put(player.getUniqueId(), taskId);
    }

    private void stopActionBarTask(Player player) {
        Integer taskId = actionBarTasks.remove(player.getUniqueId());
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
}
