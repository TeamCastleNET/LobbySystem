package de.zcreeper.lobbysystem.cosmetics.types;

import de.zcreeper.lobbysystem.cosmetics.Cosmetic;
import de.zcreeper.lobbysystem.cosmetics.CosmeticCategory;
import de.zcreeper.lobbysystem.cosmetics.CosmeticEffects;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public enum Aura implements Cosmetic {

    HALO("heiligenschein", "§eHeiligenschein", Material.GOLD_NUGGET, "Ein goldener Ring über deinem Kopf.",
            Aura::halo),

    HELIX("doppelhelix", "§bDoppelhelix", Material.PRISMARINE_SHARD, "Zwei Farbstränge winden sich um dich.",
            Aura::helix),

    RAIN_CLOUD("regenwolke", "§9Regenwolke", Material.WATER_BUCKET, "Für Tage, an denen alles grau ist.",
            Aura::rainCloud),

    FIRE_VORTEX("feuerwirbel", "§6Feuerwirbel", Material.FIRE_CHARGE, "Ein Tornado aus Flammen.",
            Aura::fireVortex),

    ORBIT("sternenorbit", "§dSternenorbit", Material.AMETHYST_CLUSTER, "Kleine Planeten kreisen um dich.",
            Aura::orbit),

    ANGEL_WINGS("engelsfluegel", "§fEngelsflügel", Material.FEATHER, "Schlagende Flügel auf deinem Rücken.",
            Aura::wings);

    private static final Color GOLD = Color.fromRGB(255, 200, 40);
    private static final Color CYAN = Color.fromRGB(60, 220, 255);
    private static final Color MAGENTA = Color.fromRGB(255, 70, 200);
    private static final Color[] PLANETS = {Color.fromRGB(255, 120, 40), Color.fromRGB(120, 160, 255), Color.fromRGB(180, 255, 120)};

    // Rechter Flügel, Spalte 0 liegt an der Wirbelsäule; der linke wird gespiegelt
    private static final String[] WING_PATTERN = {
            "   xxx",
            " xxxxxxx",
            "xxxxxxxx",
            "xxxxxxx",
            "xxxxx",
            " xxx",
            "  x"
    };

    private final String id;
    private final String displayName;
    private final Material icon;
    private final String description;
    private final Effect effect;

    Aura(String id, String displayName, Material icon, String description, Effect effect) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.effect = effect;
    }

    public void play(Player player, int tick) {
        effect.play(player, player.getLocation(), tick);
    }

    private static void halo(Player player, Location location, int tick) {
        double y = player.getEyeHeight() + 0.5;
        int points = 14;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points + tick * 0.1;
            CosmeticEffects.dust(player, location.clone().add(Math.cos(angle) * 0.35, y, Math.sin(angle) * 0.35), GOLD, 0.6F);
        }
    }

    private static void helix(Player player, Location location, int tick) {
        for (int step = 0; step < 2; step++) {
            double progress = ((tick * 2 + step) % 40) / 40D;
            double height = progress * 2.2;
            double angle = progress * 4 * Math.PI;

            CosmeticEffects.dust(player, location.clone().add(Math.cos(angle) * 0.8, height, Math.sin(angle) * 0.8), CYAN, 1F);
            CosmeticEffects.dust(player, location.clone().add(Math.cos(angle + Math.PI) * 0.8, height, Math.sin(angle + Math.PI) * 0.8), MAGENTA, 1F);
        }
    }

    private static void rainCloud(Player player, Location location, int tick) {
        Location cloud = location.clone().add(0, 2.9, 0);

        CosmeticEffects.particle(player, Particle.CLOUD, cloud, 6, 0.4, 0.05, 0.4, 0);
        CosmeticEffects.particle(player, Particle.FALLING_WATER, cloud.add(0, -0.15, 0), 2, 0.3, 0, 0.3, 0);
    }

    private static void fireVortex(Player player, Location location, int tick) {
        for (int strand = 0; strand < 3; strand++) {
            double progress = ((tick * 0.04) + strand / 3D) % 1D;
            double radius = 1.0 - progress * 0.6;
            double angle = tick * 0.45 + strand * 2 * Math.PI / 3;

            Location point = location.clone().add(Math.cos(angle) * radius, progress * 2.2, Math.sin(angle) * radius);
            CosmeticEffects.particle(player, Particle.FLAME, point, 1, 0, 0, 0, 0);
        }
    }

    private static void orbit(Player player, Location location, int tick) {
        for (int i = 0; i < PLANETS.length; i++) {
            double radius = 0.7 + i * 0.3;
            double angle = tick * (0.25 - i * 0.06) + i * 2;
            double height = 1.0 + Math.sin(tick * 0.1 + i) * 0.4;

            Location planet = location.clone().add(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
            CosmeticEffects.dust(player, planet, PLANETS[i], 1.3F);
        }

        if (tick % 4 == 0) CosmeticEffects.particle(player, Particle.END_ROD, location.clone().add(0, 1, 0), 1, 0.6, 0.6, 0.6, 0);
    }

    private static void wings(Player player, Location location, int tick) {
        double yaw = Math.toRadians(player.getBodyYaw());
        Vector forward = new Vector(-Math.sin(yaw), 0, Math.cos(yaw));
        Vector right = new Vector(-Math.cos(yaw), 0, -Math.sin(yaw));

        Location base = location.clone().subtract(forward.clone().multiply(0.3));
        double flap = Math.toRadians(25 + Math.sin(tick * 0.25) * 20);
        double spacing = 0.15;
        double top = player.isSneaking() ? 1.45 : 1.75;

        for (int row = 0; row < WING_PATTERN.length; row++) {
            String line = WING_PATTERN[row];

            for (int column = 0; column < line.length(); column++) {
                if (line.charAt(column) != 'x') continue;

                double distance = 0.1 + column * spacing;
                double y = top - row * spacing;

                for (int side = -1; side <= 1; side += 2) {
                    Vector offset = right.clone().multiply(side * distance * Math.cos(flap))
                            .subtract(forward.clone().multiply(distance * Math.sin(flap)));

                    CosmeticEffects.dust(player, base.clone().add(offset).add(0, y, 0), Color.WHITE, 0.7F);
                }
            }
        }
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
        return CosmeticCategory.AURA;
    }

    @FunctionalInterface
    private interface Effect {
        void play(Player player, Location location, int tick);
    }

}
