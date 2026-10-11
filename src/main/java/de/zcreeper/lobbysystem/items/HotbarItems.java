package de.zcreeper.lobbysystem.items;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

public class HotbarItems {

    private static final String TELEPORTER_ID = "teleporter";
    private static final String VISIBILITY_ID = "visibility";
    private static final String COSMETIC_ID = "cosmetic";
    private static final String NO_GADGET_ID = "no_gadget";
    private static final String FRIENDS_ID = "friends";

    public static final int GADGET_SLOT = 6;

    public static void getItems(Player player) {
        player.getInventory().setItem(4, getTeleporter());
        player.getInventory().setItem(7, getCosmetics());
        player.getInventory().setItem(GADGET_SLOT, getNoGadget());
        player.getInventory().setItem(1, getVisibilityItem(false));
        player.getInventory().setItem(2, getFriends(player));
    }

    public static ItemStack getTeleporter() {
        return mark(new ItemBuilder(Material.NETHER_STAR).displayname("§6Teleporter").build(), TELEPORTER_ID);
    }

    public static ItemStack getCosmetics() {
        return mark(new ItemBuilder(Material.TOTEM_OF_UNDYING).displayname("§6Cosmetics").build(), COSMETIC_ID);
    }

    public static ItemStack getNoGadget() {
        return mark(new ItemBuilder(Material.BARRIER)
                .displayname("§cKein Gadget ausgewählt")
                .lore("§7Wähle im §6Cosmetics§7-Menü", "§7ein Gadget aus.")
                .build(), NO_GADGET_ID);
    }

    public static ItemStack getFriends(Player player) {
        ItemStack item = new ItemBuilder(Material.PLAYER_HEAD).displayname("§6Freunde").build();
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwningPlayer(player);
        item.setItemMeta(meta);

        return mark(item, FRIENDS_ID);
    }

    public static ItemStack getVisibilityItem(boolean hiding) {
        ItemStack item = hiding
                ? new ItemBuilder(Material.GRAY_DYE).displayname("§cSpieler versteckt").build()
                : new ItemBuilder(Material.ENDER_EYE).displayname("§aSpieler sichtbar").build();

        return mark(item, VISIBILITY_ID);
    }

    public static boolean isTeleporter(ItemStack item) {
        return hasId(item, TELEPORTER_ID);
    }

    public static boolean isCosmetics(ItemStack item) {
        return hasId(item, COSMETIC_ID);
    }

    public static boolean isNoGadget(ItemStack item) {
        return hasId(item, NO_GADGET_ID);
    }

    public static boolean isFriends(ItemStack item) {
        return hasId(item, FRIENDS_ID);
    }

    public static boolean isVisibilityItem(ItemStack item) {
        return hasId(item, VISIBILITY_ID);
    }

    private static ItemStack mark(ItemStack item, String id) {
        ItemMeta meta = item.getItemMeta();

        meta.getPersistentDataContainer().set(getKey(id), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);

        return item;
    }

    private static boolean hasId(ItemStack item, String id) {
        if (item == null || !item.hasItemMeta()) return false;

        return item.getItemMeta().getPersistentDataContainer().has(getKey(id), PersistentDataType.BYTE);
    }

    private static NamespacedKey getKey(String id) {
        return new NamespacedKey(LobbySystem.getInstance(), id);
    }

}
