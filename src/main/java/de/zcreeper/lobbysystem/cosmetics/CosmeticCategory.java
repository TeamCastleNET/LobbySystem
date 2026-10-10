package de.zcreeper.lobbysystem.cosmetics;

import de.zcreeper.lobbysystem.cosmetics.types.Aura;
import de.zcreeper.lobbysystem.cosmetics.types.Gadget;
import de.zcreeper.lobbysystem.cosmetics.types.Hat;
import de.zcreeper.lobbysystem.cosmetics.types.Trail;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public enum CosmeticCategory {

    TRAIL("trail", "§bPartikel-Spuren", Material.BLAZE_POWDER, "Hinterlasse eine Spur, wo du läufst."),
    AURA("aura", "§dAuren", Material.AMETHYST_SHARD, "Animierte Effekte rund um dich herum."),
    HAT("hat", "§6Hüte", Material.GOLDEN_HELMET, "Setz dir etwas Schönes auf den Kopf."),
    GADGET("gadget", "§aGadgets", Material.FIREWORK_ROCKET, "Spielzeuge für deine Hotbar.");

    private final String id;
    private final String displayName;
    private final Material icon;
    private final String description;

    CosmeticCategory(String id, String displayName, Material icon, String description) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public List<? extends Cosmetic> getCosmetics() {
        return switch (this) {
            case TRAIL -> Arrays.asList(Trail.values());
            case AURA -> Arrays.asList(Aura.values());
            case HAT -> Arrays.asList(Hat.values());
            case GADGET -> Arrays.asList(Gadget.values());
        };
    }

    public Cosmetic getCosmetic(String id) {
        for (Cosmetic cosmetic : getCosmetics()) {
            if (cosmetic.getId().equals(id)) return cosmetic;
        }

        return null;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

}
