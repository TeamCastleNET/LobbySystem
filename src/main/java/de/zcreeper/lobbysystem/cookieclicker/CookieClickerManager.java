package de.zcreeper.lobbysystem.cookieclicker;

import de.zcreeper.lobbysystem.LobbySystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class CookieClickerManager {

    private static final String PATH = "cookieclicker.blocks";
    private static final String COOKIES_KEY = "cookies";
    private static final String HOLOGRAM_KEY = "cookieclicker_hologram";

    private static final String HOLOGRAM_TEXT = "§6§lCookieClicker\n"
            + "§eLinksklick §8» §7Cookie sammeln\n"
            + "§eSneak + Linksklick §8» §7Dein Konto";

    // Block-Key -> Position, zu der der Teleporter bringt
    private static final Map<String, Location> clickers = new LinkedHashMap<>();

    public static void load() {
        clickers.clear();

        ConfigurationSection section = LobbySystem.getInstance().getConfig().getConfigurationSection(PATH);

        if (section == null) return;

        for (String key : section.getKeys(false)) {
            World world = Bukkit.getWorld(key.split(";")[0]);

            if (world == null) {
                LobbySystem.getInstance().getLogger().warning("Die Welt vom CookieClicker '" + key + "' wurde nicht gefunden.");
                continue;
            }

            clickers.put(key, new Location(
                    world,
                    section.getDouble(key + ".x"),
                    section.getDouble(key + ".y"),
                    section.getDouble(key + ".z"),
                    (float) section.getDouble(key + ".yaw"),
                    (float) section.getDouble(key + ".pitch")
            ));
        }
    }

    public static boolean add(Block block, Location teleportLocation) {
        String key = getKey(block);

        if (clickers.containsKey(key)) return false;

        FileConfiguration config = LobbySystem.getInstance().getConfig();
        String path = PATH + "." + key;

        config.set(path + ".x", teleportLocation.getX());
        config.set(path + ".y", teleportLocation.getY());
        config.set(path + ".z", teleportLocation.getZ());
        config.set(path + ".yaw", teleportLocation.getYaw());
        config.set(path + ".pitch", teleportLocation.getPitch());

        LobbySystem.getInstance().saveConfig();

        clickers.put(key, teleportLocation.clone());
        spawnHologram(block);
        return true;
    }

    public static boolean remove(Block block) {
        String key = getKey(block);

        if (clickers.remove(key) == null) return false;

        LobbySystem.getInstance().getConfig().set(PATH + "." + key, null);
        LobbySystem.getInstance().saveConfig();

        removeHologram(block);
        return true;
    }

    public static boolean isClicker(Block block) {
        return block != null && clickers.containsKey(getKey(block));
    }

    public static Location getTeleportLocation() {
        return clickers.values().stream().findFirst().map(Location::clone).orElse(null);
    }

    public static long getCookies(Player player) {
        return player.getPersistentDataContainer().getOrDefault(getCookiesKey(), PersistentDataType.LONG, 0L);
    }

    public static long addCookie(Player player) {
        long cookies = getCookies(player) + 1;

        player.getPersistentDataContainer().set(getCookiesKey(), PersistentDataType.LONG, cookies);
        return cookies;
    }

    public static String format(long value) {
        return NumberFormat.getInstance(Locale.GERMANY).format(value);
    }

    private static void spawnHologram(Block block) {
        removeHologram(block);

        Location location = block.getLocation().add(0.5, 1.6, 0.5);

        block.getWorld().spawn(location, TextDisplay.class, display -> {
            display.setText(HOLOGRAM_TEXT);
            display.setBillboard(Display.Billboard.CENTER);
            display.setPersistent(true);
            display.getPersistentDataContainer().set(getHologramKey(), PersistentDataType.STRING, getKey(block));
        });
    }

    private static void removeHologram(Block block) {
        String key = getKey(block);

        for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 1.6, 0.5), 1, 1, 1)) {
            if (key.equals(entity.getPersistentDataContainer().get(getHologramKey(), PersistentDataType.STRING))) {
                entity.remove();
            }
        }
    }

    private static String getKey(Block block) {
        return block.getWorld().getName() + ";" + block.getX() + ";" + block.getY() + ";" + block.getZ();
    }

    private static NamespacedKey getCookiesKey() {
        return new NamespacedKey(LobbySystem.getInstance(), COOKIES_KEY);
    }

    private static NamespacedKey getHologramKey() {
        return new NamespacedKey(LobbySystem.getInstance(), HOLOGRAM_KEY);
    }

}
