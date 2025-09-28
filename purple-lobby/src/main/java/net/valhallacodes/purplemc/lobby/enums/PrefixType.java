/*
 * Copyright (C) purplemc.net, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */

package net.valhallacodes.purplemc.lobby.enums;

import lombok.Getter;

@Getter
public enum PrefixType {

    DEFAULT_WHITE("§f"),
    DEFAULT_GRAY("§7");

    private final String color;

    PrefixType(String color) {
        this.color = color;
    }
}
