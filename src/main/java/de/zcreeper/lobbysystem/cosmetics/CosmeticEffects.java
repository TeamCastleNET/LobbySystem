package de.zcreeper.lobbysystem.cosmetics;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CosmeticEffects {

    private static final double VIEW_DISTANCE_SQUARED = 48 * 48;

    /*
    Partikel und Sounds werden nur an Spieler geschickt, die den Besitzer sehen können.
    Wer Spieler versteckt hat, bekommt also auch deren Cosmetics nicht angezeigt.
     */

    public static void particle(Player owner, Particle particle, Location location, int count,
                                double offsetX, double offsetY, double offsetZ, double speed) {
        particle(owner, particle, location, count, offsetX, offsetY, offsetZ, speed, null);
    }

    public static <T> void particle(Player owner, Particle particle, Location location, int count,
                                    double offsetX, double offsetY, double offsetZ, double speed, T data) {
        for (Player viewer : location.getWorld().getPlayers()) {
            if (!canView(viewer, owner, location)) continue;

            viewer.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, data);
        }
    }

    public static void dust(Player owner, Location location, Color color, float size) {
        particle(owner, Particle.DUST, location, 1, 0, 0, 0, 0, new Particle.DustOptions(color, size));
    }

    public static void sound(Player owner, Location location, Sound sound, float volume, float pitch) {
        for (Player viewer : location.getWorld().getPlayers()) {
            if (!canView(viewer, owner, location)) continue;

            viewer.playSound(location, sound, volume, pitch);
        }
    }

    public static Color rainbow(double hue) {
        return Color.fromRGB(java.awt.Color.HSBtoRGB((float) (hue % 1D), 1F, 1F) & 0xFFFFFF);
    }

    private static boolean canView(Player viewer, Player owner, Location location) {
        if (!viewer.equals(owner) && !viewer.canSee(owner)) return false;

        return viewer.getLocation().distanceSquared(location) <= VIEW_DISTANCE_SQUARED;
    }

}
