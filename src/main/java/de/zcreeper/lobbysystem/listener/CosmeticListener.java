package de.zcreeper.lobbysystem.listener;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.cosmetics.Cosmetic;
import de.zcreeper.lobbysystem.cosmetics.CosmeticCategory;
import de.zcreeper.lobbysystem.cosmetics.CosmeticManager;
import de.zcreeper.lobbysystem.cosmetics.types.Gadget;
import de.zcreeper.lobbysystem.inventories.CosmeticCategoryInventory;
import de.zcreeper.lobbysystem.inventories.CosmeticsInventory;
import de.zcreeper.lobbysystem.items.HotbarItems;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;

public class CosmeticListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        if (HotbarItems.isCosmetics(event.getItem())) {
            event.setCancelled(true);

            CosmeticsInventory.open(player);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5F, 1.2F);
            return;
        }

        Gadget gadget = Gadget.fromItem(event.getItem());

        if (gadget == null) return;

        event.setCancelled(true);

        if (!gadget.isUnlocked(player)) {
            CosmeticManager.unequip(player, CosmeticCategory.GADGET);
            return;
        }

        CosmeticManager.useGadget(player, gadget);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (!(holder instanceof CosmeticsInventory) && !(holder instanceof CosmeticCategoryInventory)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getInventory()) return;

        if (holder instanceof CosmeticsInventory) {
            handleMainClick(player, event.getSlot());
            return;
        }

        handleCategoryClick(player, (CosmeticCategoryInventory) holder, event.getSlot());
    }

    private void handleMainClick(Player player, int slot) {
        if (slot == CosmeticsInventory.SLOT_REMOVE_ALL) {
            if (!CosmeticManager.hasAnyEquipped(player)) {
                deny(player, "§cDu hast keine Cosmetics ausgerüstet.");
                return;
            }

            CosmeticManager.unequipAll(player);
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 0.8F);
            player.sendMessage(LobbySystem.getPrefix() + "§7Du hast alle Cosmetics §cabgelegt§7.");
            return;
        }

        CosmeticCategory category = CosmeticsInventory.getCategory(slot);

        if (category == null) return;

        CosmeticCategoryInventory.open(player, category);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
    }

    private void handleCategoryClick(Player player, CosmeticCategoryInventory inventory, int slot) {
        CosmeticCategory category = inventory.getCategory();

        if (slot == CosmeticCategoryInventory.SLOT_BACK) {
            CosmeticsInventory.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
            return;
        }

        if (slot == CosmeticCategoryInventory.SLOT_REMOVE) {
            Cosmetic current = CosmeticManager.getEquipped(player, category);

            if (current == null) {
                deny(player, "§cDu hast in dieser Kategorie nichts ausgerüstet.");
                return;
            }

            unequip(player, current);
            inventory.render(player);
            return;
        }

        Cosmetic cosmetic = inventory.getCosmetic(slot);

        if (cosmetic == null) return;

        if (!cosmetic.isUnlocked(player)) {
            deny(player, "§cDieses Cosmetic hast du noch nicht freigeschaltet.");
            return;
        }

        if (CosmeticManager.isEquipped(player, cosmetic)) {
            unequip(player, cosmetic);
        } else {
            CosmeticManager.equip(player, cosmetic);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 1.6F);
            player.sendMessage(LobbySystem.getPrefix() + "§7Du hast " + cosmetic.getDisplayName() + " §aausgerüstet§7.");
        }

        inventory.render(player);
    }

    private void unequip(Player player, Cosmetic cosmetic) {
        CosmeticManager.unequip(player, cosmetic.getCategory());
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 0.8F);
        player.sendMessage(LobbySystem.getPrefix() + "§7Du hast " + cosmetic.getDisplayName() + " §cabgelegt§7.");
    }

    private void deny(Player player, String message) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
        player.sendMessage(LobbySystem.getPrefix() + message);
    }

}
