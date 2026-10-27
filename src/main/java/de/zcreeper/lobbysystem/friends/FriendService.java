package de.zcreeper.lobbysystem.friends;

import com.destroystokyo.paper.profile.PlayerProfile;
import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.inventories.FriendOptionsInventory;
import de.zcreeper.lobbysystem.inventories.FriendRequestsInventory;
import de.zcreeper.lobbysystem.inventories.FriendsInventory;
import de.zcreeper.lobbysystem.utils.DatabaseManager;
import de.zcreeper.lobbysystem.utils.Sounds;
import net.teamcastle.teamcastleapi.TeamCastleAPI;
import net.teamcastle.teamcastleapi.api.friends.Friend;
import net.teamcastle.teamcastleapi.api.friends.FriendAPI;
import net.teamcastle.teamcastleapi.api.friends.FriendRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FriendService {

    private static final long INPUT_TIMEOUT = 30_000L;

    // Freundeslisten fürs Scoreboard werden jede Minute neu geladen (Änderungen von anderen Servern)
    private static final long CACHE_REFRESH_INTERVAL = 20L * 60;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());

    // Spieler, die gerade einen Namen in den Chat schreiben sollen -> Ablaufzeitpunkt
    private static final Map<UUID, Long> awaitingName = new ConcurrentHashMap<>();

    // Spieler -> UUIDs seiner Freunde, damit das Scoreboard nicht jede Sekunde die Datenbank fragt
    private static final Map<UUID, Set<UUID>> friendCache = new ConcurrentHashMap<>();

    /*
    Der Lobby-Server behält keine Spielerdaten von Offline-Spielern. Namen kommen deshalb aus der
    lobby-Tabelle und Skins (Texturen) werden gemerkt, sobald jemand joint, oder bei Mojang nachgeladen.
     */
    private static final Map<UUID, String> names = new ConcurrentHashMap<>();
    private static final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();
    private static final Set<UUID> pendingProfiles = ConcurrentHashMap.newKeySet();

    private static BukkitTask refreshTask;

    public static void start() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            rememberProfile(player);
            friendCache.put(player.getUniqueId(), Set.of());
            refreshCache(player.getUniqueId());
        });

        refreshTask = Bukkit.getScheduler().runTaskTimer(LobbySystem.getInstance(),
                () -> friendCache.keySet().forEach(FriendService::refreshCache),
                CACHE_REFRESH_INTERVAL, CACHE_REFRESH_INTERVAL);
    }

    public static void stop() {
        if (refreshTask != null) refreshTask.cancel();

        friendCache.clear();
        awaitingName.clear();
    }

    public static int getFriendCount(Player player) {
        return friendCache.getOrDefault(player.getUniqueId(), Set.of()).size();
    }

    public static int getFriendsInLobby(Player player) {
        int count = 0;

        for (UUID friend : friendCache.getOrDefault(player.getUniqueId(), Set.of())) {
            if (isInLobby(friend)) count++;
        }

        return count;
    }

    public record Overview(List<Friend> friends, List<FriendRequest> incoming, List<FriendRequest> outgoing) {
    }

    /*
    Öffnen der Menüs - die Daten kommen asynchron aus der TeamCastleAPI,
    das Menü öffnet sich, sobald sie da sind.
     */

    public static void openOverview(Player player, int page) {
        UUID uuid = player.getUniqueId();

        CompletableFuture<List<Friend>> friends = api().getFriends(uuid);
        CompletableFuture<List<FriendRequest>> incoming = api().getIncomingRequests(uuid);
        CompletableFuture<List<FriendRequest>> outgoing = api().getOutgoingRequests(uuid);

        CompletableFuture<Overview> overview = CompletableFuture.allOf(friends, incoming, outgoing)
                .thenCompose(ignored -> loadMissingNames(friends.join().stream().map(Friend::uuid).toList()))
                .thenApply(ignored -> new Overview(sortFriends(friends.join()), incoming.join(), outgoing.join()));

        friends.thenAccept(list -> updateCache(uuid, list));

        sync(player, overview, data -> {
            FriendsInventory.open(player, data, page);
            loadMissingSkins(player, data.friends().stream().map(Friend::uuid).toList());
        });
    }

    public static void openRequests(Player player, boolean incoming) {
        UUID uuid = player.getUniqueId();
        CompletableFuture<List<FriendRequest>> requests = incoming ? api().getIncomingRequests(uuid) : api().getOutgoingRequests(uuid);

        CompletableFuture<List<FriendRequest>> withNames = requests
                .thenCompose(list -> loadMissingNames(otherPlayers(list, incoming)).thenApply(ignored -> list));

        sync(player, withNames, list -> {
            FriendRequestsInventory.open(player, list, incoming);
            loadMissingSkins(player, otherPlayers(list, incoming));
        });
    }

    /*
    Freund hinzufügen - der Name wird im Chat abgefragt
     */

    public static void requestNameInput(Player player) {
        player.closeInventory();
        awaitingName.put(player.getUniqueId(), System.currentTimeMillis() + INPUT_TIMEOUT);

        Sounds.click(player);
        player.sendMessage(LobbySystem.getPrefix() + "§7Schreibe den §eNamen §7des Spielers in den Chat.");
        player.sendMessage(LobbySystem.getPrefix() + "§7Mit §cabbrechen §7brichst du ab.");
    }

    public static boolean isAwaitingName(Player player) {
        Long until = awaitingName.get(player.getUniqueId());

        if (until == null) return false;

        if (until < System.currentTimeMillis()) {
            awaitingName.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    public static void handleNameInput(Player player, String input) {
        awaitingName.remove(player.getUniqueId());

        String name = input.trim();

        if (name.equalsIgnoreCase("abbrechen")) {
            Sounds.click(player);
            player.sendMessage(LobbySystem.getPrefix() + "§7Abgebrochen.");
            return;
        }

        sendRequest(player, name);
    }

    public static void sendRequest(Player player, String name) {
        Player online = Bukkit.getPlayerExact(name);

        if (online != null) {
            sendRequest(player, online.getUniqueId(), online.getName());
            return;
        }

        // Spieler auf anderen Servern des Netzwerks findet die Lobby über die lobby-Tabelle
        sync(player, DatabaseManager.findUuidByName(name), uuid -> {
            if (uuid.isEmpty()) {
                deny(player, "§cDer Spieler §e" + name + " §cwurde nicht gefunden.");
                return;
            }

            sync(player, loadMissingNames(List.of(uuid.get())), ignored -> sendRequest(player, uuid.get(), getName(uuid.get())));
        });
    }

    private static void sendRequest(Player player, UUID target, String targetName) {
        sync(player, api().sendRequest(player.getUniqueId(), target), result -> {
            switch (result) {
                case SENT -> {
                    Sounds.success(player);
                    player.sendMessage(LobbySystem.getPrefix() + "§7Du hast §e" + targetName + " §7eine §aFreundschaftsanfrage §7gesendet.");
                    notify(target, "§e" + player.getName() + " §7hat dir eine §aFreundschaftsanfrage §7gesendet.");
                }
                case ACCEPTED -> {
                    refreshCache(player.getUniqueId());
                    refreshCache(target);
                    Sounds.purchase(player);
                    player.sendMessage(LobbySystem.getPrefix() + "§7Du bist jetzt mit §e" + targetName + " §abefreundet§7!");
                    notify(target, "§e" + player.getName() + " §7hat deine Anfrage §aangenommen§7!");
                }
                case ALREADY_SENT -> deny(player, "§cDu hast §e" + targetName + " §cbereits eine Anfrage gesendet.");
                case ALREADY_FRIENDS -> deny(player, "§cDu bist bereits mit §e" + targetName + " §cbefreundet.");
                case SELF -> deny(player, "§cDu kannst dir nicht selbst eine Anfrage senden.");
            }
        });
    }

    /*
    Anfragen bearbeiten
     */

    public static void accept(Player player, UUID sender) {
        String name = getName(sender);

        sync(player, api().acceptRequest(player.getUniqueId(), sender), success -> {
            if (!success) {
                deny(player, "§cDiese Anfrage existiert nicht mehr.");
            } else {
                refreshCache(player.getUniqueId());
                refreshCache(sender);
                Sounds.purchase(player);
                player.sendMessage(LobbySystem.getPrefix() + "§7Du bist jetzt mit §e" + name + " §abefreundet§7!");
                notify(sender, "§e" + player.getName() + " §7hat deine Anfrage §aangenommen§7!");
            }

            openRequests(player, true);
        });
    }

    public static void denyRequest(Player player, UUID sender) {
        String name = getName(sender);

        sync(player, api().denyRequest(player.getUniqueId(), sender), success -> {
            if (success) {
                Sounds.unequip(player);
                player.sendMessage(LobbySystem.getPrefix() + "§7Du hast die Anfrage von §e" + name + " §cabgelehnt§7.");
            }

            openRequests(player, true);
        });
    }

    public static void cancelRequest(Player player, UUID target) {
        String name = getName(target);

        sync(player, api().cancelRequest(player.getUniqueId(), target), success -> {
            if (success) {
                Sounds.unequip(player);
                player.sendMessage(LobbySystem.getPrefix() + "§7Du hast deine Anfrage an §e" + name + " §czurückgezogen§7.");
            }

            openRequests(player, false);
        });
    }

    public static void removeFriend(Player player, UUID friend) {
        String name = getName(friend);

        sync(player, api().removeFriend(player.getUniqueId(), friend), success -> {
            if (success) {
                refreshCache(player.getUniqueId());
                refreshCache(friend);
                Sounds.unequip(player);
                player.sendMessage(LobbySystem.getPrefix() + "§7Du bist nicht mehr mit §e" + name + " §7befreundet.");
                notify(friend, "§e" + player.getName() + " §7hat dich als Freund §centfernt§7.");
            }

            openOverview(player, 0);
        });
    }

    public static void teleport(Player player, UUID friend) {
        Player target = Bukkit.getPlayer(friend);

        player.closeInventory();

        if (target == null) {
            deny(player, "§e" + getName(friend) + " §cist gerade nicht in der Lobby.");
            return;
        }

        player.teleport(target);
        Sounds.teleport(player);
        player.sendMessage(LobbySystem.getPrefix() + "§7Du wurdest zu §e" + target.getName() + " §7teleportiert.");
    }

    public static void openOptions(Player player, Friend friend) {
        FriendOptionsInventory.open(player, friend);
        loadMissingSkins(player, List.of(friend.uuid()));
    }

    /*
    Beim Join auf offene Anfragen hinweisen
     */

    public static void handleJoin(Player player) {
        rememberProfile(player);
        friendCache.put(player.getUniqueId(), Set.of());
        refreshCache(player.getUniqueId());

        sync(player, api().getIncomingRequests(player.getUniqueId()), requests -> {
            if (requests.isEmpty()) return;

            player.sendMessage(LobbySystem.getPrefix() + "§7Du hast §e" + requests.size() + " §7offene "
                    + (requests.size() == 1 ? "Freundschaftsanfrage" : "Freundschaftsanfragen") + "§7.");
        });
    }

    public static void handleQuit(Player player) {
        awaitingName.remove(player.getUniqueId());
        friendCache.remove(player.getUniqueId());
    }

    /*
    Hilfsmethoden
     */

    public static String getName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);

        if (online != null) return online.getName();

        String cached = names.get(uuid);

        if (cached != null) return cached;

        String offline = Bukkit.getOfflinePlayer(uuid).getName();

        return offline == null ? "Unbekannt" : offline;
    }

    // Profil für Köpfe im GUI - mit Skin, sobald er bekannt ist
    public static PlayerProfile getProfile(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);

        if (online != null) return online.getPlayerProfile();

        PlayerProfile cached = profiles.get(uuid);

        if (cached != null) return cached;

        String name = getName(uuid);

        return Bukkit.createProfile(uuid, name.equals("Unbekannt") ? null : name);
    }

    public static boolean isInLobby(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }

    public static String formatDate(long millis) {
        return DATE_FORMAT.format(Instant.ofEpochMilli(millis));
    }

    private static void rememberProfile(Player player) {
        names.put(player.getUniqueId(), player.getName());

        PlayerProfile profile = player.getPlayerProfile();

        if (profile.hasTextures()) profiles.put(player.getUniqueId(), profile);
    }

    private static List<UUID> otherPlayers(List<FriendRequest> requests, boolean incoming) {
        return requests.stream().map(request -> incoming ? request.sender() : request.target()).toList();
    }

    private static CompletableFuture<Void> loadMissingNames(Collection<UUID> uuids) {
        List<UUID> missing = uuids.stream()
                .filter(uuid -> !names.containsKey(uuid) && Bukkit.getPlayer(uuid) == null)
                .toList();

        return DatabaseManager.loadNames(missing)
                .thenAccept(names::putAll)
                // Wer noch nie in der Lobby war, steht nicht in der Tabelle - dann fragen wir Mojang (eigener Thread, blockiert)
                .thenRunAsync(() -> {
                    for (UUID uuid : missing) {
                        if (names.containsKey(uuid)) continue;

                        try {
                            PlayerProfile profile = Bukkit.createProfile(uuid);

                            if (!profile.complete(true) || profile.getName() == null) continue;

                            names.put(uuid, profile.getName());
                            if (profile.hasTextures()) profiles.put(uuid, profile);
                        } catch (Exception exception) {
                            LobbySystem.getInstance().getLogger().warning("Name von " + uuid + " konnte nicht geladen werden: " + exception.getMessage());
                        }
                    }
                });
    }

    /*
    Fehlende Skins bei Mojang nachladen (blockiert, deshalb asynchron) und danach die Köpfe
    im offenen Menü austauschen. Jeder Skin wird nur einmal pro Serverstart geladen.
     */
    private static void loadMissingSkins(Player player, Collection<UUID> uuids) {
        Set<UUID> missing = new HashSet<>();

        for (UUID uuid : uuids) {
            if (Bukkit.getPlayer(uuid) == null && !profiles.containsKey(uuid) && pendingProfiles.add(uuid)) missing.add(uuid);
        }

        if (missing.isEmpty()) return;

        Bukkit.getScheduler().runTaskAsynchronously(LobbySystem.getInstance(), () -> {
            for (UUID uuid : missing) {
                try {
                    String name = names.get(uuid);
                    PlayerProfile profile = Bukkit.createProfile(uuid, name);

                    if (profile.complete(true) && profile.hasTextures()) profiles.put(uuid, profile);
                } catch (Exception exception) {
                    LobbySystem.getInstance().getLogger().warning("Skin von " + uuid + " konnte nicht geladen werden: " + exception.getMessage());
                } finally {
                    pendingProfiles.remove(uuid);
                }
            }

            Bukkit.getScheduler().runTask(LobbySystem.getInstance(), () -> refreshHeads(player));
        });
    }

    private static void refreshHeads(Player player) {
        if (!player.isOnline()) return;

        InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();

        if (!(holder instanceof FriendsInventory) && !(holder instanceof FriendRequestsInventory)
                && !(holder instanceof FriendOptionsInventory)) return;

        for (ItemStack item : holder.getInventory().getContents()) {
            if (item == null || !(item.getItemMeta() instanceof SkullMeta meta) || meta.getPlayerProfile() == null) continue;

            UUID uuid = meta.getPlayerProfile().getId();
            PlayerProfile profile = uuid == null ? null : profiles.get(uuid);

            if (profile == null || meta.getPlayerProfile().hasTextures()) continue;

            meta.setPlayerProfile(profile);
            item.setItemMeta(meta);
        }
    }

    // Nur für Spieler, die gerade in der Lobby sind - für alle anderen gibt es keinen Eintrag
    private static void refreshCache(UUID uuid) {
        if (!friendCache.containsKey(uuid)) return;

        api().getFriends(uuid).thenAccept(list -> updateCache(uuid, list));
    }

    private static void updateCache(UUID uuid, List<Friend> friends) {
        // replace: Hat der Spieler die Lobby inzwischen verlassen, wird kein neuer Eintrag angelegt
        friendCache.replace(uuid, friends.stream().map(Friend::uuid).collect(Collectors.toUnmodifiableSet()));
    }

    private static List<Friend> sortFriends(List<Friend> friends) {
        return friends.stream()
                .sorted(Comparator.comparing((Friend friend) -> !isInLobby(friend.uuid()))
                        .thenComparing(friend -> getName(friend.uuid()), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static void notify(UUID uuid, String message) {
        Player target = Bukkit.getPlayer(uuid);

        if (target == null) return;

        Sounds.success(target);
        target.sendMessage(LobbySystem.getPrefix() + message);
    }

    private static void deny(Player player, String message) {
        Sounds.error(player);
        player.sendMessage(LobbySystem.getPrefix() + message);
    }

    // Ergebnis zurück auf den Main-Thread holen; ist der Spieler inzwischen weg, passiert nichts
    private static <T> void sync(Player player, CompletableFuture<T> future, Consumer<T> action) {
        future.whenComplete((result, error) -> Bukkit.getScheduler().runTask(LobbySystem.getInstance(), () -> {
            if (!player.isOnline()) return;

            if (error != null) {
                LobbySystem.getInstance().getLogger().warning("Freunde-Abfrage für " + player.getName() + " fehlgeschlagen: " + error.getMessage());
                player.closeInventory();
                deny(player, "§cBeim Laden deiner Freunde ist ein Fehler aufgetreten.");
                return;
            }

            action.accept(result);
        }));
    }

    private static FriendAPI api() {
        return TeamCastleAPI.getFriendAPI();
    }

}
