/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.enums;

import java.util.Arrays;

public enum Tag {

    ADMIN(12, "A", "§4", false, "IzPLp", "Admin", "administrator"),
    COORD(11, "B", "§9", false, "CYrov", "Coord", "coordenador"),
    MOD_PLUS(10, "C", "§d", false, "b3761", "Mod+", "modplus"),
    MOD(9, "D", "§5", false, "vAjST", "Mod", "moderator"),
    CREATOR_PLUS(8, "E", "§3", false, "DxmFd", "Creator+", "creatorplus"),
    CREATOR(7, "F", "§b", false, "jSBMB", "Creator", "creator"),
    LUNA_PLUS(6, "G", "§5", false, "MxKpQ", "Luna+", "lunaplus"),
    BETA(5, "H", "§e", false, "VxLmR", "Beta", "beta"),
    LUNA(4, "I", "§d", false, "EalNl", "Luna", "luna"),
    VIP(3, "J", "§a", false, "FbMpQ", "VIP", "vip"),
    BOOSTER(2, "K", "§d", false, "GcNrR", "Booster", "booster"),
    WINNER(1, "L", "§6", false, "HdOsS", "Winner", "winner"),
    MEMBER(0, "M", "§7", false, "IePtT", "Membro", "member", "normal", "default", "none", "null");

    private final int id;
    private final String order;
    private final String color;
    private final boolean dedicated;
    private final String uniqueCode;
    private final String[] usages;

    Tag(int id, String order, String color, boolean dedicated, String uniqueCode, String... usages) {
        this.id = id;
        this.order = order;
        this.color = color;
        this.dedicated = dedicated;
        this.uniqueCode = uniqueCode;
        this.usages = usages;
    }

    private static final Tag[] values = values();

    public Rank getDefaultRank() {
        switch (this) {
            case ADMIN:
                return Rank.ADMIN;
            case COORD:
                return Rank.COORD;
            case MOD_PLUS:
                return Rank.MOD_PLUS;
            case MOD:
                return Rank.MOD;
            case CREATOR_PLUS:
                return Rank.CREATOR_PLUS;
            case CREATOR:
                return Rank.CREATOR;
            case LUNA_PLUS:
                return Rank.LUNA_PLUS;
            case BETA:
                return Rank.BETA;
            case LUNA:
                return Rank.LUNA;
            case VIP:
                return Rank.VIP;
            case BOOSTER:
                return Rank.BOOSTER;
            case WINNER:
                return Rank.WINNER;
            case MEMBER:
            default:
                return Rank.MEMBRO;
        }
    }

    public boolean isBetween(Tag tag1, Tag tag2) {
        return this.getId() < tag1.getId() && this.getId() > tag2.getId();
    }

    public static Tag fromUniqueCode(String code) {
        return Arrays.stream(getValues()).filter(tag -> tag.getUniqueCode().equals(code)).findFirst().orElse(null);
    }

    public static Tag getOrElse(String code, Tag t) {
        return Arrays.stream(getValues()).filter(tag -> tag.getUniqueCode().equals(code)).findFirst().orElse(t);
    }

    public String getName() {
        return this.usages[0];
    }

    public static Tag fromUsages(String text) {
        for (Tag tag : getValues()) {
            for (String u : tag.getUsages()) {
                if (u.equalsIgnoreCase(text))
                    return tag;
            }
        }
        return null;
    }

    public String getMemberSetting(PrefixType prefixType) {
        return (prefixType == PrefixType.DEFAULT_WHITE ? "§f" : "§7");
    }

    public String getFormattedColor() {
        return color;
    }

    public String getColoredPrefix() {
        if (this == MEMBER) {
            return color;
        }
        return color + "&l" + getName().toUpperCase() + " " + color;
    }

    public int getId() {
        return id;
    }

    public String getOrder() {
        return order;
    }

    public String getColor() {
        return color;
    }

    public boolean isDedicated() {
        return dedicated;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public String[] getUsages() {
        return usages;
    }

    public static Tag[] getValues() {
        return values;
    }
}
