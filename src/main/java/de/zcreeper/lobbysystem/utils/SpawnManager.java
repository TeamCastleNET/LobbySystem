package de.zcreeper.lobbysystem.utils;

import de.zcreeper.lobbysystem.LobbySystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SpawnManager {

    private static final String PATH = "spawn";

    private static volatile Location spawn;

    public static void loadSpawn() {
        spawn = readSpawn();
    }

    public static void setSpawn(Location location) {
        FileConfiguration config = LobbySystem.getInstance().getConfig();

        config.set(PATH + ".world", location.getWorld().getName());
        config.set(PATH + ".x", location.getX());
        config.set(PATH + ".y", location.getY());
        config.set(PATH + ".z", location.getZ());
        config.set(PATH + ".yaw", location.getYaw());
        config.set(PATH + ".pitch", location.getPitch());

        LobbySystem.getInstance().saveConfig();

        spawn = location.clone();
    }

    public static Location getSpawn() {
        Location location = spawn;

        return location == null ? null : location.clone();
    }

    public static boolean hasSpawn() {
        return spawn != null;
    }

    public static boolean teleportToSpawn(Player player) {
        Location location = getSpawn();

        if (location == null) return false;

        player.teleport(location);
        return true;
    }

    private static Location readSpawn() {
        FileConfiguration config = LobbySystem.getInstance().getConfig();

        String worldName = config.getString(PATH + ".world");

        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            LobbySystem.getInstance().getLogger().warning("Die Spawn-Welt '" + worldName + "' wurde nicht gefunden.");
            return null;
        }

        return new Location(
                world,
                config.getDouble(PATH + ".x"),
                config.getDouble(PATH + ".y"),
                config.getDouble(PATH + ".z"),
                (float) config.getDouble(PATH + ".yaw"),
                (float) config.getDouble(PATH + ".pitch")
        );
    }

}
