package de.zcreeper.lobbysystem.listener;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.cookieclicker.CookieClickerManager;
import de.zcreeper.lobbysystem.inventories.CookieClickerInventory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CookieClickerListener implements Listener {

    // Höchstens 20 Klicks pro Sekunde, damit Autoklicker nichts bringen
    private static final long CLICK_DELAY = 50L;

    private static final ItemStack COOKIE = new ItemStack(Material.COOKIE);

    private final Map<UUID, Long> lastClicks = new ConcurrentHashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();

        if (!CookieClickerManager.isClicker(block)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        if (player.isSneaking()) {
            CookieClickerInventory.open(player);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5F, 1.2F);
            return;
        }

        long now = System.currentTimeMillis();
        Long lastClick = lastClicks.get(player.getUniqueId());

        if (lastClick != null && now - lastClick < CLICK_DELAY) return;

        lastClicks.put(player.getUniqueId(), now);

        long cookies = CookieClickerManager.addCookie(player);
        Location face = block.getLocation().add(0.5, 0.5, 0.5).add(event.getBlockFace().getDirection().multiply(0.6));

        player.spawnParticle(Particle.ITEM, face, 5, 0.15, 0.15, 0.15, 0.05, COOKIE);
        player.playSound(face, Sound.ENTITY_ITEM_PICKUP, 0.6F, 0.8F + ThreadLocalRandom.current().nextFloat() * 0.6F);
        player.sendActionBar("§6Cookies: §e" + CookieClickerManager.format(cookies));

        if (cookies % 100 == 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7F, 1.4F);
            player.sendMessage(LobbySystem.getPrefix() + "§7Du hast jetzt §e" + CookieClickerManager.format(cookies) + " §6Cookies§7!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (!CookieClickerManager.isClicker(event.getBlock())) return;

        event.setCancelled(true);

        if (event.getPlayer().hasPermission("lobbysystem.cookieclicker.admin")) {
            event.getPlayer().sendMessage(LobbySystem.getPrefix() + "§7Entferne den CookieClicker mit §6/cookieclicker remove§7.");
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CookieClickerInventory)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getInventory()) return;

        if (event.getSlot() == CookieClickerInventory.SLOT_PAYOUT) {
            // Kommt, sobald es das Coins-System gibt
            player.closeInventory();
            player.sendMessage(LobbySystem.getPrefix() + "§cDie Auszahlung in Coins ist noch in Arbeit.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastClicks.remove(event.getPlayer().getUniqueId());
    }

}
