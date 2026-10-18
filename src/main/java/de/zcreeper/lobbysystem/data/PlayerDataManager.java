package de.zcreeper.lobbysystem.data;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.cosmetics.Cosmetic;
import de.zcreeper.lobbysystem.cosmetics.CosmeticCategory;
import de.zcreeper.lobbysystem.utils.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    // Alle 5 Minuten werden geänderte Daten (z. B. Cookies) gespeichert
    private static final long AUTOSAVE_INTERVAL = 20L * 60 * 5;

    private static final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    private static BukkitTask autosaveTask;

    public static void start() {
        autosaveTask = Bukkit.getScheduler().runTaskTimer(LobbySystem.getInstance(), PlayerDataManager::saveDirty,
                AUTOSAVE_INTERVAL, AUTOSAVE_INTERVAL);

        // Nach einem Reload sind schon Spieler online
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                cache.put(player.getUniqueId(), DatabaseManager.load(player.getUniqueId(), player.getName()));
            } catch (SQLException exception) {
                LobbySystem.getInstance().getLogger().severe("Laden von " + player.getName() + " fehlgeschlagen: " + exception.getMessage());
            }
        }
    }

    public static void stop() {
        if (autosaveTask != null) autosaveTask.cancel();

        if (!DatabaseManager.isConnected()) return;

        DatabaseManager.awaitPending();

        for (PlayerData data : cache.values()) {
            try {
                DatabaseManager.save(data);
            } catch (SQLException exception) {
                LobbySystem.getInstance().getLogger().severe("Speichern von " + data.getName() + " fehlgeschlagen: " + exception.getMessage());
            }
        }

        cache.clear();
    }

    public static void handlePreLogin(AsyncPlayerPreLoginEvent event) {
        if (!DatabaseManager.isConnected()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, LobbySystem.getPrefix() + "§cDie Lobby-Datenbank ist gerade nicht erreichbar.");
            return;
        }

        try {
            cache.put(event.getUniqueId(), DatabaseManager.loadQueued(event.getUniqueId(), event.getName()));
        } catch (SQLException exception) {
            LobbySystem.getInstance().getLogger().severe("Laden von " + event.getName() + " fehlgeschlagen: " + exception.getMessage());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, LobbySystem.getPrefix() + "§cDeine Daten konnten nicht geladen werden. Bitte versuche es erneut.");
        }
    }

    // Wurde der Login danach noch von einem anderen Plugin abgelehnt, brauchen wir die Daten nicht
    public static void handleLoginResult(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) cache.remove(event.getUniqueId());
    }

    public static void handleJoin(Player player) {
        PlayerData data = cache.get(player.getUniqueId());

        if (data == null) return;

        data.setName(player.getName());

        if (data.isNewPlayer()) {
            migrateLegacyData(player, data);
            save(player);
        }
    }

    public static void handleQuit(Player player) {
        PlayerData data = cache.remove(player.getUniqueId());

        if (data != null && DatabaseManager.isConnected()) DatabaseManager.saveAsync(data);
    }

    public static boolean isLoaded(Player player) {
        return cache.containsKey(player.getUniqueId());
    }

    // Gibt nie null zurück. Fehlen die Daten, gibt es leere Daten, die nicht gespeichert werden.
    public static PlayerData get(Player player) {
        PlayerData data = cache.get(player.getUniqueId());

        return data != null ? data : new PlayerData(player.getUniqueId(), player.getName(), false);
    }

    public static void save(Player player) {
        PlayerData data = cache.get(player.getUniqueId());

        if (data != null && DatabaseManager.isConnected()) DatabaseManager.saveAsync(data);
    }

    private static void saveDirty() {
        if (!DatabaseManager.isConnected()) return;

        for (PlayerData data : cache.values()) {
            if (data.isDirty()) DatabaseManager.saveAsync(data);
        }
    }

    /*
    Vor der Datenbank lagen Cookies und Cosmetics im PersistentDataContainer des Spielers.
    Beim ersten Join nach dem Update werden sie einmalig übernommen und danach gelöscht.
     */
    private static void migrateLegacyData(Player player, PlayerData data) {
        PersistentDataContainer container = player.getPersistentDataContainer();

        NamespacedKey cookiesKey = key("cookies");
        NamespacedKey ownedKey = key("cosmetics_owned");

        Long cookies = container.get(cookiesKey, PersistentDataType.LONG);
        if (cookies != null) data.setCookies(cookies);

        List<String> owned = container.get(ownedKey, PersistentDataType.LIST.strings());

        if (owned != null) {
            for (String entry : owned) {
                Cosmetic cosmetic = findCosmetic(entry);
                if (cosmetic != null) data.addOwnedCosmetic(cosmetic);
            }
        }

        for (CosmeticCategory category : CosmeticCategory.values()) {
            NamespacedKey selectedKey = key("cosmetic_" + category.getId());
            String selected = container.get(selectedKey, PersistentDataType.STRING);

            if (selected != null) data.setSelectedCosmetic(category, selected);

            container.remove(selectedKey);
        }

        container.remove(cookiesKey);
        container.remove(ownedKey);
    }

    private static Cosmetic findCosmetic(String entry) {
        String[] parts = entry.split(":", 2);

        if (parts.length != 2) return null;

        for (CosmeticCategory category : CosmeticCategory.values()) {
            if (category.getId().equals(parts[0])) return category.getCosmetic(parts[1]);
        }

        return null;
    }

    private static NamespacedKey key(String key) {
        return new NamespacedKey(LobbySystem.getInstance(), key);
    }

}
