package de.zcreeper.lobbysystem.listener;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.items.HotbarItems;
import de.zcreeper.lobbysystem.utils.PlayerHider;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class VisibilityListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!HotbarItems.isVisibilityItem(event.getItem())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        boolean hiding = PlayerHider.toggle(player);

        player.getInventory().setItemInMainHand(HotbarItems.getVisibilityItem(hiding));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);

        player.sendMessage(hiding
                ? LobbySystem.getPrefix() + "§cDie Spieler werden dir nicht mehr angezeigt."
                : LobbySystem.getPrefix() + "§aDie Spieler werden dir wieder angezeigt.");
    }

}
