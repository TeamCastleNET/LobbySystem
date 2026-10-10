package de.zcreeper.lobbysystem.cosmetics.types;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.cosmetics.Cosmetic;
import de.zcreeper.lobbysystem.cosmetics.CosmeticCategory;
import de.zcreeper.lobbysystem.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public enum Hat implements Cosmetic {

    CROWN("krone", "§6Königskrone", Material.GOLDEN_HELMET, "Für die wahren Herrscher der Lobby.", true),
    ASTRONAUT("astronaut", "§bAstronautenhelm", Material.GLASS, "Bereit für den Flug ins All.", false),
    DRAGON("drachenkopf", "§5Drachenkopf", Material.DRAGON_HEAD, "Der Schrecken des Endes.", false),
    CREEPER("creeper", "§aCreeper", Material.CREEPER_HEAD, "Psst... sssss...", false),
    PIGLIN("piglin", "§6Piglin", Material.PIGLIN_HEAD, "Hast du Gold dabei?", false),
    BEACON("leuchtfeuer", "§bLeuchtfeuer", Material.BEACON, "Ein strahlender Kopf.", true),
    JACK_O_LANTERN("kuerbis", "§6Kürbislaterne", Material.JACK_O_LANTERN, "Das ganze Jahr Halloween.", false),
    MELON("melone", "§aMelone", Material.MELON, "Erfrischend.", false),
    SLIME("schleim", "§aSchleimkopf", Material.SLIME_BLOCK, "Ziemlich klebrig.", false),
    ICE("eiskopf", "§bEiskopf", Material.PACKED_ICE, "Immer einen kühlen Kopf bewahren.", false);

    private static final String HAT_KEY = "cosmetic_hat";

    private final String id;
    private final String displayName;
    private final Material icon;
    private final String description;
    private final boolean glint;

    Hat(String id, String displayName, Material icon, String description, boolean glint) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.glint = glint;
    }

    public ItemStack getItem() {
        ItemStack item = new ItemBuilder(icon)
                .displayname(displayName)
                .lore("§7Cosmetic-Hut")
                .flag(ItemFlag.HIDE_ATTRIBUTES)
                .build();

        ItemMeta meta = item.getItemMeta();

        meta.setEnchantmentGlintOverride(glint);
        meta.getPersistentDataContainer().set(getKey(), PersistentDataType.STRING, id);
        item.setItemMeta(meta);

        return item;
    }

    public static boolean isHat(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        return item.getItemMeta().getPersistentDataContainer().has(getKey(), PersistentDataType.STRING);
    }

    private static NamespacedKey getKey() {
        return new NamespacedKey(LobbySystem.getInstance(), HAT_KEY);
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
        return CosmeticCategory.HAT;
    }

}
