package de.zcreeper.lobbysystem.cosmetics;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface Cosmetic {

    String getId();

    String getDisplayName();

    Material getIcon();

    String getDescription();

    CosmeticCategory getCategory();

    default String getPermission() {
        return "lobbysystem.cosmetics." + getCategory().getId() + "." + getId();
    }

    default boolean isUnlocked(Player player) {
        return player.hasPermission(getPermission());
    }

}
