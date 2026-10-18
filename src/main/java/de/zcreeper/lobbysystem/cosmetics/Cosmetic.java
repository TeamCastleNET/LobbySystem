package de.zcreeper.lobbysystem.cosmetics;

import de.zcreeper.lobbysystem.LobbySystem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public interface Cosmetic {

    long DEFAULT_PRICE = 1000L;

    String getId();

    String getDisplayName();

    Material getIcon();

    String getDescription();

    CosmeticCategory getCategory();

    default String getPermission() {
        return "lobbysystem.cosmetics." + getCategory().getId() + "." + getId();
    }

    default long getPrice() {
        FileConfiguration config = LobbySystem.getInstance().getConfig();
        String path = "cosmetics.prices." + getCategory().getId() + "." + getId();

        // contains() berücksichtigt auch die Standard-Preise aus der config.yml im Jar
        return config.contains(path) ? config.getLong(path) : DEFAULT_PRICE;
    }

    default boolean isFree() {
        return getPrice() <= 0;
    }

    // Freigeschaltet ist ein Cosmetic, wenn es gratis ist, gekauft wurde oder per Recht vergeben ist (z. B. für Ränge)
    default boolean isUnlocked(Player player) {
        return isFree() || CosmeticManager.owns(player, this) || player.hasPermission(getPermission());
    }

}
