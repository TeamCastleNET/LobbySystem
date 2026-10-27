package de.zcreeper.lobbysystem.cookieclicker;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.data.PlayerData;
import de.zcreeper.lobbysystem.data.PlayerDataManager;
import de.zcreeper.lobbysystem.utils.Sounds;
import net.teamcastle.teamcastleapi.TeamCastleAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
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

import java.awt.*;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CookieClickerManager {

    private static final String PATH = "cookieclicker.blocks";
    private static final String HOLOGRAM_KEY = "cookieclicker_hologram";
    private static final long DEFAULT_COOKIES_PER_COIN = 100L;

    private static final String HOLOGRAM_TEXT = "§6§lCookieClicker\n"
            + "§eLinksklick §8» §7Cookie sammeln\n"
            + "§eSneak + Linksklick §8» §7Dein Konto";

    // Block-Key -> Position, zu der der Teleporter bringt
    private static final Map<String, Location> clickers = new LinkedHashMap<>();

    // Spieler, deren Auszahlung gerade läuft - verhindert doppelte Auszahlungen durch schnelles Klicken
    private static final Set<UUID> pendingPayouts = ConcurrentHashMap.newKeySet();

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
        return PlayerDataManager.get(player).getCookies();
    }

    public static long addCookie(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        long cookies = data.getCookies() + 1;

        data.setCookies(cookies);
        return cookies;
    }

    public static long getCookiesPerCoin() {
        return Math.max(1L, LobbySystem.getInstance().getConfig().getLong("cookieclicker.cookies-per-coin", DEFAULT_COOKIES_PER_COIN));
    }

    public static long getPayoutCoins(Player player) {
        return getCookies(player) / getCookiesPerCoin();
    }

    /*
    Zahlt so viele Coins aus, wie die Cookies hergeben. Abgezogen werden nur die umgewandelten
    Cookies, der Rest (weniger als ein Coin) bleibt stehen.
    Die Cookies werden zuerst abgezogen und gespeichert - schlägt die Gutschrift fehl, gibt es sie zurück.
     */
    public static void payout(Player player) {
        if (!PlayerDataManager.isLoaded(player)) {
            Sounds.error(player);
            player.sendMessage(LobbySystem.getPrefix() + "§cDeine Daten sind nicht geladen. Bitte joine neu.");
            return;
        }

        long rate = getCookiesPerCoin();
        long coins = getPayoutCoins(player);

        if (coins <= 0) {
            Sounds.error(player);
            player.sendMessage(LobbySystem.getPrefix() + "§cDu brauchst mindestens §e" + format(rate) + " Cookies §cfür einen Coin.");
            return;
        }

        UUID uuid = player.getUniqueId();

        if (!pendingPayouts.add(uuid)) return;

        PlayerData data = PlayerDataManager.get(player);
        long cookies = coins * rate;

        data.setCookies(data.getCookies() - cookies);
        PlayerDataManager.save(player);

        TeamCastleAPI.getCoinAPI().addCoins(uuid, coins).whenComplete((balance, error) ->
                Bukkit.getScheduler().runTask(LobbySystem.getInstance(), () -> {
                    pendingPayouts.remove(uuid);

                    if (error != null) {
                        LobbySystem.getInstance().getLogger().warning("Cookie-Auszahlung für " + player.getName() + " fehlgeschlagen: " + error.getMessage());

                        // Cookies zurückgeben - ist der Spieler schon weg, liegen die Daten nicht mehr im Cache
                        if (PlayerDataManager.isLoaded(player)) {
                            PlayerData current = PlayerDataManager.get(player);
                            current.setCookies(current.getCookies() + cookies);
                            PlayerDataManager.save(player);
                        }

                        if (player.isOnline()) {
                            Sounds.error(player);
                            player.sendMessage(LobbySystem.getPrefix() + "§cDie Auszahlung ist fehlgeschlagen. Deine Cookies wurden nicht abgezogen.");
                        }
                        return;
                    }

                    if (!player.isOnline()) return;

                    Sounds.purchase(player);
                    player.sendMessage(LobbySystem.getPrefix() + "§7Du hast §e" + format(cookies) + " Cookies §7in §b"
                            + format(coins) + " Coins §aumgewandelt§7!");
                }));
    }

    public static String format(long value) {
        return NumberFormat.getInstance(Locale.GERMANY).format(value);
    }

    private static void spawnHologram(Block block) {
        removeHologram(block);

        Location location = block.getLocation().add(0.5, 1.6, 0.5);

        block.getWorld().spawn(location, TextDisplay.class, display -> {
            display.setText(HOLOGRAM_TEXT);
            display.setBillboard(Display.Billboard.VERTICAL);
            display.setPersistent(true);
            display.setDefaultBackground(false);
            display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            display.setShadowed(true);
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

    private static NamespacedKey getHologramKey() {
        return new NamespacedKey(LobbySystem.getInstance(), HOLOGRAM_KEY);
    }

}
