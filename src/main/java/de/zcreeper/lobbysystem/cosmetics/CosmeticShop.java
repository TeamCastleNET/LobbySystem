package de.zcreeper.lobbysystem.cosmetics;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.data.PlayerDataManager;
import de.zcreeper.lobbysystem.utils.Sounds;
import net.teamcastle.teamcastleapi.TeamCastleAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class CosmeticShop {

    // Spieler, deren Kauf gerade in der Datenbank läuft - verhindert Doppelkäufe durch schnelles Klicken
    private static final Set<UUID> pending = ConcurrentHashMap.newKeySet();

    public static long getCoins(Player player) {
        return TeamCastleAPI.getCoinAPI().getCachedCoins(player.getUniqueId()).orElse(0L);
    }

    public static boolean canAfford(Player player, Cosmetic cosmetic) {
        return getCoins(player) >= cosmetic.getPrice();
    }

    public static void buy(Player player, Cosmetic cosmetic, Consumer<Boolean> callback) {
        if (cosmetic.isUnlocked(player)) {
            callback.accept(true);
            return;
        }

        if (!PlayerDataManager.isLoaded(player)) {
            Sounds.error(player);
            player.sendMessage(LobbySystem.getPrefix() + "§cDeine Daten sind nicht geladen. Bitte joine neu.");
            return;
        }

        if (!pending.add(player.getUniqueId())) return;

        long price = cosmetic.getPrice();
        UUID uuid = player.getUniqueId();

        // removeCoins zieht nur ab, wenn genug Coins da sind, und liefert dann true
        TeamCastleAPI.getCoinAPI().removeCoins(uuid, price).whenComplete((success, error) ->
                Bukkit.getScheduler().runTask(LobbySystem.getInstance(), () -> {
                    pending.remove(uuid);

                    if (error != null) {
                        LobbySystem.getInstance().getLogger().warning("Kauf von " + cosmetic.getId() + " für " + uuid + " fehlgeschlagen: " + error.getMessage());

                        if (player.isOnline()) {
                            Sounds.error(player);
                            player.sendMessage(LobbySystem.getPrefix() + "§cBeim Kauf ist ein Fehler aufgetreten. Bitte versuche es später erneut.");
                        }
                        return;
                    }

                    if (!Boolean.TRUE.equals(success)) {
                        if (player.isOnline()) {
                            Sounds.error(player);
                            player.sendMessage(LobbySystem.getPrefix() + "§cDu hast nicht genug Coins. §7Benötigt: §e" + format(price) + " Coins");
                            callback.accept(false);
                        }
                        return;
                    }

                    // Spieler hat den Server verlassen, bevor der Kauf fertig war - Coins zurückgeben
                    if (!player.isOnline()) {
                        TeamCastleAPI.getCoinAPI().addCoins(uuid, price);
                        return;
                    }

                    CosmeticManager.addOwned(player, cosmetic);

                    Sounds.purchase(player);
                    player.sendMessage(LobbySystem.getPrefix() + "§7Du hast " + cosmetic.getDisplayName()
                            + " §7für §e" + format(price) + " Coins §agekauft§7!");

                    callback.accept(true);
                }));
    }

    public static String format(long value) {
        return NumberFormat.getInstance(Locale.GERMANY).format(value);
    }

}
