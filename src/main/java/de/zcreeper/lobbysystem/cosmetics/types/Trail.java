package de.zcreeper.lobbysystem.cosmetics.types;

import de.zcreeper.lobbysystem.cosmetics.Cosmetic;
import de.zcreeper.lobbysystem.cosmetics.CosmeticCategory;
import de.zcreeper.lobbysystem.cosmetics.CosmeticEffects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public enum Trail implements Cosmetic {

    HEARTS("herzen", "§cHerzen", Material.POPPY, "Verbreite Liebe in der ganzen Lobby.",
            (player, location, tick) -> {
                if (tick % 3 == 0) CosmeticEffects.particle(player, Particle.HEART, location.add(0, 0.3, 0), 1, 0.3, 0.1, 0.3, 0);
            }),

    FLAMES("flammen", "§6Flammen", Material.BLAZE_POWDER, "Heiße Sohlen.",
            (player, location, tick) -> CosmeticEffects.particle(player, Particle.FLAME, location.add(0, 0.1, 0), 3, 0.15, 0, 0.15, 0.01)),

    SOUL_FIRE("seelenfeuer", "§3Seelenfeuer", Material.SOUL_LANTERN, "Blaue Flammen aus dem Seelensandtal.",
            (player, location, tick) -> CosmeticEffects.particle(player, Particle.SOUL_FIRE_FLAME, location.add(0, 0.1, 0), 3, 0.15, 0, 0.15, 0.01)),

    NOTES("noten", "§eMusiknoten", Material.NOTE_BLOCK, "Jeder Schritt ein neuer Ton.",
            (player, location, tick) -> {
                // Bei count 0 bestimmt offsetX die Farbe der Note
                if (tick % 2 == 0) CosmeticEffects.particle(player, Particle.NOTE, location.add(0, 0.4, 0), 0, (tick % 24) / 24D, 0, 0, 1);
            }),

    RAINBOW("regenbogen", "§cR§6e§eg§ae§bn§9b§do§cg§6e§en", Material.PRISMARINE_CRYSTALS, "Alle Farben auf einmal.",
            (player, location, tick) -> {
                for (int i = 0; i < 3; i++) {
                    Location point = location.clone().add((Math.random() - 0.5) * 0.4, 0.15 + Math.random() * 0.2, (Math.random() - 0.5) * 0.4);
                    CosmeticEffects.dust(player, point, CosmeticEffects.rainbow(tick * 0.02 + i * 0.05), 1.2F);
                }
            }),

    MAGIC("magie", "§5Magie", Material.ENCHANTED_BOOK, "Ein Hauch von Zauberei.",
            (player, location, tick) -> CosmeticEffects.particle(player, Particle.WITCH, location.add(0, 0.2, 0), 4, 0.2, 0.1, 0.2, 0)),

    SPARKLE("glitzer", "§fGlitzer", Material.GLOWSTONE_DUST, "Funkelnde Sterne bei jedem Schritt.",
            (player, location, tick) -> {
                CosmeticEffects.particle(player, Particle.END_ROD, location.clone().add(0, 0.2, 0), 1, 0.2, 0.1, 0.2, 0.01);
                CosmeticEffects.particle(player, Particle.ELECTRIC_SPARK, location.add(0, 0.3, 0), 2, 0.25, 0.1, 0.25, 0);
            }),

    SNOW("schnee", "§fSchneeflocken", Material.SNOWBALL, "Ein kleiner Winter zum Mitnehmen.",
            (player, location, tick) -> CosmeticEffects.particle(player, Particle.SNOWFLAKE, location.add(0, 0.2, 0), 3, 0.2, 0.1, 0.2, 0.01)),

    CHERRY_BLOSSOM("kirschblueten", "§dKirschblüten", Material.PINK_PETALS, "Blütenblätter, die sanft herabfallen.",
            (player, location, tick) -> CosmeticEffects.particle(player, Particle.CHERRY_LEAVES, location.add(0, 1.6, 0), 2, 0.35, 0.2, 0.35, 0)),

    EMERALDS("smaragde", "§aSmaragde", Material.EMERALD, "Der Duft von Reichtum.",
            (player, location, tick) -> CosmeticEffects.particle(player, Particle.HAPPY_VILLAGER, location.add(0, 0.2, 0), 2, 0.25, 0.1, 0.25, 0));

    private final String id;
    private final String displayName;
    private final Material icon;
    private final String description;
    private final Effect effect;

    Trail(String id, String displayName, Material icon, String description, Effect effect) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.effect = effect;
    }

    public void play(Player player, int tick) {
        effect.play(player, player.getLocation(), tick);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Material getIcon() {
        return icon;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public CosmeticCategory getCategory() {
        return CosmeticCategory.TRAIL;
    }

    @FunctionalInterface
    private interface Effect {
        void play(Player player, Location location, int tick);
    }

}
