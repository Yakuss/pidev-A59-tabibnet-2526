package com.pidev.constant;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Enum representing Tunisian governorates - mirrors Symfony Governorate constants.
 */
public enum Governorate {
    TUNIS("Tunis"),
    ARIANA("Ariana"),
    BEN_AROUS("Ben Arous"),
    MANOUBA("Manouba"),
    BIZERTE("Bizerte"),
    NABEUL("Nabeul"),
    BEJA("Béjà"),
    JENDOUBA("Jendouba"),
    ZAGHOUAN("Zaghouane"),
    SILIANA("Siliana"),
    KEF("Kef"),
    SOUSSE("Sousse"),
    MONASTIR("Monastir"),
    MAHDIA("Mahdia"),
    SFAX("Sfax"),
    KAIROUAN("Kairouan"),
    KASSERINE("Kasserine"),
    SIDI_BOUZID("Sidi Bouzid"),
    GABES("Gabès"),
    MEDENINE("Médennine"),
    TATAOUINE("Tataouine"),
    GAFSA("Gafsa"),
    TOZEUR("Tozeur"),
    KEBILI("Kébili");

    private final String displayName;

    Governorate(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a map of display names to enum constants for use in ComboBoxes, etc.
     */
    public static Map<String, Governorate> getChoices() {
        Map<String, Governorate> choices = new LinkedHashMap<>();
        for (Governorate g : values()) {
            choices.put(g.displayName, g);
        }
        return choices;
    }

    /**
     * Gets enum constant from its display name (case-sensitive).
     */
    public static Governorate fromDisplayName(String displayName) {
        for (Governorate g : values()) {
            if (g.displayName.equals(displayName)) {
                return g;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}