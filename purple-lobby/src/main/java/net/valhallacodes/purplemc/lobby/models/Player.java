/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.models;

import net.valhallacodes.purplemc.lobby.enums.PrefixType;
import net.valhallacodes.purplemc.lobby.enums.Rank;
import net.valhallacodes.purplemc.lobby.enums.Tag;

import java.sql.Timestamp;
import java.util.UUID;

public class Player {

    private final UUID uuid;
    private String name;
    private Rank rank;
    private Tag tag;
    private PrefixType prefixType;
    private Timestamp firstLogin;
    private Timestamp lastLogin;

    public Player(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.rank = Rank.MEMBRO;
        this.tag = Tag.MEMBER;
        this.prefixType = PrefixType.DEFAULT_GRAY;
        this.firstLogin = new Timestamp(System.currentTimeMillis());
        this.lastLogin = new Timestamp(System.currentTimeMillis());
    }

    public Player(UUID uuid, String name, Rank rank, Tag tag, PrefixType prefixType,
                 Timestamp firstLogin, Timestamp lastLogin) {
        this.uuid = uuid;
        this.name = name;
        this.rank = rank;
        this.tag = tag;
        this.prefixType = prefixType;
        this.firstLogin = firstLogin;
        this.lastLogin = lastLogin;
    }

    public String getFormattedName() {
        if (rank == Rank.MEMBRO) {
            return prefixType.getColor() + name;
        }
        return rank.getColoredPrefix() + " " + prefixType.getColor() + name;
    }

    public String getFormattedTag() {
        if (tag == Tag.MEMBER) {
            return prefixType.getColor();
        }
        return tag.getColoredPrefix() + " " + prefixType.getColor();
    }

    public boolean hasPermission(String permission) {
        return rank.hasPermission(getRankForPermission(permission));
    }

    public boolean canSeeReports() {
        return rank == Rank.ADMIN || rank == Rank.COORD || rank == Rank.MOD_PLUS ||
               rank == Rank.MOD || rank == Rank.CREATOR_PLUS;
    }

    public boolean canUseGo() {
        return rank == Rank.ADMIN || rank == Rank.COORD || rank == Rank.MOD_PLUS ||
               rank == Rank.MOD || rank == Rank.CREATOR_PLUS;
    }

    public boolean canUseAdminCommands() {
        return rank == Rank.ADMIN || rank == Rank.COORD || rank == Rank.MOD_PLUS ||
               rank == Rank.MOD || rank == Rank.CREATOR_PLUS;
    }

    public boolean canManagePlayers() {
        return rank == Rank.ADMIN || rank == Rank.COORD || rank == Rank.MOD_PLUS ||
               rank == Rank.MOD || rank == Rank.CREATOR_PLUS;
    }

    private Rank getRankForPermission(String permission) {
        if (permission.contains("admin") || permission.contains("manage")) {
            return Rank.ADMIN;
        }
        if (permission.contains("coord") || permission.contains("coordenador")) {
            return Rank.COORD;
        }
        if (permission.contains("modplus") || permission.contains("mod+")) {
            return Rank.MOD_PLUS;
        }
        if (permission.contains("mod") || permission.contains("moderator")) {
            return Rank.MOD;
        }
        if (permission.contains("creatorplus") || permission.contains("creator+")) {
            return Rank.CREATOR_PLUS;
        }
        return Rank.MEMBRO;
    }

    public void updateLastLogin() {
        this.lastLogin = new Timestamp(System.currentTimeMillis());
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public PrefixType getPrefixType() {
        return prefixType;
    }

    public void setPrefixType(PrefixType prefixType) {
        this.prefixType = prefixType;
    }

    public Timestamp getFirstLogin() {
        return firstLogin;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }
}
