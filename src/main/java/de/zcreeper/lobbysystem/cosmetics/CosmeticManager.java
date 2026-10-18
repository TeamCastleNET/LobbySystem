package de.zcreeper.lobbysystem.cosmetics;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.data.PlayerData;
import de.zcreeper.lobbysystem.data.PlayerDataManager;
import de.zcreeper.lobbysystem.cosmetics.types.Aura;
import de.zcreeper.lobbysystem.cosmetics.types.Gadget;
import de.zcreeper.lobbysystem.cosmetics.types.Hat;
import de.zcreeper.lobbysystem.cosmetics.types.Trail;
import de.zcreeper.lobbysystem.items.HotbarItems;
import de.zcreeper.lobbysystem.utils.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CosmeticManager {

    public static final int GADGET_SLOT = HotbarItems.GADGET_SLOT;

    private static final long UPDATE_INTERVAL = 2L;
    private static final double MOVE_THRESHOLD_SQUARED = 0.0025D;
    private static final String ENTITY_KEY = "cosmetic_entity";

    private static final Map<UUID, Map<CosmeticCategory, Cosmetic>> equipped = new ConcurrentHashMap<>();
    private static final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<Gadget, Long>> cooldowns = new ConcurrentHashMap<>();

    private static BukkitTask task;
    private static int tick;

    public static void start() {
        registerPermissions();

        task = Bukkit.getScheduler().runTaskTimer(LobbySystem.getInstance(), CosmeticManager::updateAll,
                UPDATE_INTERVAL, UPDATE_INTERVAL);

        Bukkit.getOnlinePlayers().forEach(CosmeticManager::load);
    }

    public static void stop() {
        if (task != null) task.cancel();

        equipped.clear();
        lastLocations.clear();
        cooldowns.clear();
    }

    public static void load(Player player) {
        PlayerData data = PlayerDataManager.get(player);

        for (CosmeticCategory category : CosmeticCategory.values()) {
            String id = data.getSelectedCosmetic(category);
            Cosmetic cosmetic = id == null ? null : category.getCosmetic(id);

            if (cosmetic == null || !cosmetic.isUnlocked(player)) {
                if (id != null) data.setSelectedCosmetic(category, null);
                continue;
            }

            getEquipped(player).put(category, cosmetic);
            apply(player, cosmetic);
        }
    }

    public static void handleQuit(Player player) {
        equipped.remove(player.getUniqueId());
        lastLocations.remove(player.getUniqueId());
        cooldowns.remove(player.getUniqueId());
    }

    public static void equip(Player player, Cosmetic cosmetic) {
        unequip(player, cosmetic.getCategory());

        getEquipped(player).put(cosmetic.getCategory(), cosmetic);
        PlayerDataManager.get(player).setSelectedCosmetic(cosmetic.getCategory(), cosmetic.getId());

        apply(player, cosmetic);
    }

    public static void unequip(Player player, CosmeticCategory category) {
        Cosmetic cosmetic = getEquipped(player).remove(category);

        PlayerDataManager.get(player).setSelectedCosmetic(category, null);

        if (cosmetic == null) return;

        switch (category) {
            case HAT -> {
                if (Hat.isHat(player.getInventory().getHelmet())) player.getInventory().setHelmet(null);
            }
            case GADGET -> {
                if (Gadget.fromItem(player.getInventory().getItem(GADGET_SLOT)) != null) {
                    player.getInventory().setItem(GADGET_SLOT, HotbarItems.getNoGadget());
                }
            }
            default -> {
            }
        }
    }

    public static void unequipAll(Player player) {
        for (CosmeticCategory category : CosmeticCategory.values()) {
            unequip(player, category);
        }
    }

    public static Cosmetic getEquipped(Player player, CosmeticCategory category) {
        return getEquipped(player).get(category);
    }

    public static boolean isEquipped(Player player, Cosmetic cosmetic) {
        return cosmetic.equals(getEquipped(player, cosmetic.getCategory()));
    }

    public static boolean hasAnyEquipped(Player player) {
        return !getEquipped(player).isEmpty();
    }

    public static boolean owns(Player player, Cosmetic cosmetic) {
        return PlayerDataManager.get(player).ownsCosmetic(cosmetic);
    }

    public static void addOwned(Player player, Cosmetic cosmetic) {
        PlayerDataManager.get(player).addOwnedCosmetic(cosmetic);

        // Käufe sofort speichern, nicht erst beim Verlassen
        PlayerDataManager.save(player);
    }

    public static void useGadget(Player player, Gadget gadget) {
        Map<Gadget, Long> playerCooldowns = cooldowns.computeIfAbsent(player.getUniqueId(), uuid -> new EnumMap<>(Gadget.class));
        long remaining = playerCooldowns.getOrDefault(gadget, 0L) - System.currentTimeMillis();

        if (remaining > 0) {
            Sounds.cooldown(player);
            player.sendActionBar("§cBitte warte noch §e" + String.format("%.1f", remaining / 1000D) + "s§c.");
            return;
        }

        playerCooldowns.put(gadget, System.currentTimeMillis() + gadget.getCooldown() * 1000L);
        player.setCooldown(gadget.getIcon(), gadget.getCooldown() * 20);

        gadget.use(player);
    }

    public static void markEntity(Entity entity) {
        entity.getPersistentDataContainer().set(getEntityKey(), PersistentDataType.BYTE, (byte) 1);
    }

    public static boolean isCosmeticEntity(Entity entity) {
        return entity.getPersistentDataContainer().has(getEntityKey(), PersistentDataType.BYTE);
    }

    private static void apply(Player player, Cosmetic cosmetic) {
        if (cosmetic instanceof Hat hat) player.getInventory().setHelmet(hat.getItem());
        if (cosmetic instanceof Gadget gadget) player.getInventory().setItem(GADGET_SLOT, gadget.getItem());
    }

    private static void updateAll() {
        tick++;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Map<CosmeticCategory, Cosmetic> cosmetics = equipped.get(player.getUniqueId());

            if (cosmetics == null || cosmetics.isEmpty()) continue;

            if (cosmetics.get(CosmeticCategory.TRAIL) instanceof Trail trail && hasMoved(player)) trail.play(player, tick);
            if (cosmetics.get(CosmeticCategory.AURA) instanceof Aura aura) aura.play(player, tick);
        }
    }

    private static boolean hasMoved(Player player) {
        Location current = player.getLocation();
        Location last = lastLocations.put(player.getUniqueId(), current);

        if (last == null || last.getWorld() != current.getWorld()) return false;

        return last.distanceSquared(current) > MOVE_THRESHOLD_SQUARED;
    }

    private static Map<CosmeticCategory, Cosmetic> getEquipped(Player player) {
        return equipped.computeIfAbsent(player.getUniqueId(), uuid -> new EnumMap<>(CosmeticCategory.class));
    }

    private static void registerPermissions() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        PermissionDefault defaultValue = LobbySystem.getInstance().getConfig().getBoolean("cosmetics.unlock-all", false)
                ? PermissionDefault.TRUE
                : PermissionDefault.OP;

        Map<String, Boolean> all = new HashMap<>();

        for (CosmeticCategory category : CosmeticCategory.values()) {
            Map<String, Boolean> children = new HashMap<>();

            for (Cosmetic cosmetic : category.getCosmetics()) {
                register(pluginManager, new Permission(cosmetic.getPermission(), "Schaltet " + cosmetic.getId() + " frei.", defaultValue));
                children.put(cosmetic.getPermission(), true);
            }

            String categoryPermission = "lobbysystem.cosmetics." + category.getId() + ".*";
            register(pluginManager, new Permission(categoryPermission, "Schaltet alle " + category.getId() + "-Cosmetics frei.", PermissionDefault.OP, children));
            all.put(categoryPermission, true);
        }

        register(pluginManager, new Permission("lobbysystem.cosmetics.*", "Schaltet alle Cosmetics frei.", PermissionDefault.OP, all));
    }

    private static void register(PluginManager pluginManager, Permission permission) {
        if (pluginManager.getPermission(permission.getName()) == null) pluginManager.addPermission(permission);
    }

    private static NamespacedKey getEntityKey() {
        return new NamespacedKey(LobbySystem.getInstance(), ENTITY_KEY);
    }

}
