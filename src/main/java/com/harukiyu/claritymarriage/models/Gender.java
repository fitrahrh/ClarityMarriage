package com.harukiyu.claritymarriage.models;

import java.util.Locale;

/**
 * Represents player gender with display symbols and localization support.
 */
public enum Gender {
    MALE("Male", "♂", "<blue>♂</blue>"),
    FEMALE("Female", "♀", "<light_purple>♀</light_purple>"),
    NONE("None", "", "");

    private final String displayName;
    private final String icon;
    private final String formattedIcon;

    Gender(String displayName, String icon, String formattedIcon) {
        this.displayName = displayName;
        this.icon = icon;
        this.formattedIcon = formattedIcon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getFormattedIcon() {
        return formattedIcon;
    }

    public static Gender fromString(String str) {
        if (str == null) return NONE;
        String s = str.trim().toUpperCase(Locale.ROOT);
        return switch (s) {
            case "MALE", "M", "BOY", "MAN", "PRIA", "COWOK" -> MALE;
            case "FEMALE", "F", "GIRL", "WOMAN", "WANITA", "CEWEK" -> FEMALE;
            default -> NONE;
        };
    }
}
