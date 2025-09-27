/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.enums;

public enum Rank {

    MEMBRO(0, "Membro", "§7", new String[]{"membro", "member"}),
    WINNER(1, "Winner", "§6", new String[]{"winner"}),
    BOOSTER(2, "Booster", "§d", new String[]{"booster"}),
    VIP(3, "Vip", "§a", new String[]{"vip"}),
    LUNA(4, "Luna", "§d", new String[]{"luna"}),
    BETA(5, "Beta", "§e", new String[]{"beta"}),
    LUNA_PLUS(6, "Luna+", "§5", new String[]{"luna+", "lunaplus"}),
    CREATOR(7, "Creator", "§b", new String[]{"creator"}),
    CREATOR_PLUS(8, "Creator+", "§3", new String[]{"creator+", "creatorplus"}),
    MOD(9, "Mod", "§5", new String[]{"mod", "moderator"}),
    MOD_PLUS(10, "Mod+", "§d", new String[]{"mod+", "modplus"}),
    COORD(11, "Coord", "§9", new String[]{"coord", "coordenador"}),
    ADMIN(12, "Admin", "§4", new String[]{"admin", "administrador"});

    private final int level;
    private final String displayName;
    private final String color;
    private final String[] aliases;

    Rank(int level, String displayName, String color, String[] aliases) {
        this.level = level;
        this.displayName = displayName;
        this.color = color;
        this.aliases = aliases;
    }

    public boolean hasPermission(Rank requiredRank) {
        return this.level >= requiredRank.level;
    }

    public boolean canSeeReports() {
        return this == ADMIN || this == COORD || this == MOD_PLUS || this == MOD || this == CREATOR_PLUS;
    }

    public boolean canUseGo() {
        return this == ADMIN || this == COORD || this == MOD_PLUS || this == MOD || this == CREATOR_PLUS;
    }

    public boolean canUseAdminCommands() {
        return this == ADMIN || this == COORD || this == MOD_PLUS || this == MOD || this == CREATOR_PLUS;
    }


    public boolean canManagePlayers() {
        return this == ADMIN || this == COORD || this == MOD_PLUS || this == MOD || this == CREATOR_PLUS;
    }

    public static Rank fromString(String name) {
        if (name == null) return null;
        
        String lowerName = name.toLowerCase();
        
        for (Rank rank : values()) {
            if (rank.name().toLowerCase().equals(lowerName)) {
                return rank;
            }
            
            for (String alias : rank.aliases) {
                if (alias.toLowerCase().equals(lowerName)) {
                    return rank;
                }
            }
        }
        
        return null;
    }

    public String getFormattedName() {
        if (this == MEMBRO) {
            return color;
        }
        return color + displayName;
    }

    public String getColoredPrefix() {
        if (this == MEMBRO) {
            return color;
        }
        return color + "§l" + displayName.toUpperCase() + " " + color;
    }

    public Tag getCorrespondingTag() {
        switch (this) {
            case ADMIN:
                return Tag.ADMIN;
            case COORD:
                return Tag.COORD;
            case MOD_PLUS:
                return Tag.MOD_PLUS;
            case MOD:
                return Tag.MOD;
            case CREATOR_PLUS:
                return Tag.CREATOR_PLUS;
            case CREATOR:
                return Tag.CREATOR;
            case LUNA_PLUS:
                return Tag.LUNA_PLUS;
            case BETA:
                return Tag.BETA;
            case LUNA:
                return Tag.LUNA;
            case VIP:
                return Tag.VIP;
            case BOOSTER:
                return Tag.BOOSTER;
            case WINNER:
                return Tag.WINNER;
            case MEMBRO:
            default:
                return Tag.MEMBER;
        }
    }

    public static String getAllRanksAsString() {
        StringBuilder sb = new StringBuilder();
        for (Rank rank : values()) {
            sb.append(rank.getFormattedName()).append("§7, ");
        }
        return sb.substring(0, sb.length() - 4);
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public String[] getAliases() {
        return aliases;
    }
}
