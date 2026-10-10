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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VisibilityListener implements Listener {

    // Beim Klick auf einen Block schickt der Client zusätzlich ein Use-Item-Paket,
    // dadurch kommt das Event direkt zweimal (RIGHT_CLICK_BLOCK + RIGHT_CLICK_AIR).
    private static final long TOGGLE_DELAY = 250L;

    private final Map<UUID, Long> lastToggles = new ConcurrentHashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!HotbarItems.isVisibilityItem(event.getItem())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        long now = System.currentTimeMillis();
        Long lastToggle = lastToggles.get(player.getUniqueId());

        if (lastToggle != null && now - lastToggle < TOGGLE_DELAY) return;

        lastToggles.put(player.getUniqueId(), now);

        boolean hiding = PlayerHider.toggle(player);

        player.getInventory().setItemInMainHand(HotbarItems.getVisibilityItem(hiding));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);

        player.sendMessage(hiding
                ? LobbySystem.getPrefix() + "§cDie Spieler werden dir nicht mehr angezeigt."
                : LobbySystem.getPrefix() + "§aDie Spieler werden dir wieder angezeigt.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastToggles.remove(event.getPlayer().getUniqueId());
    }

}
