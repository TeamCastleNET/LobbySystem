package de.zcreeper.lobbysystem.listener;

import de.zcreeper.lobbysystem.LobbySystem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class JumpPadListener implements Listener {

    private static final double DEFAULT_STRENGTH = 1.5D;
    private static final double DEFAULT_HEIGHT = 0.8D;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;

        Block block = event.getClickedBlock();

        if (block == null || block.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        FileConfiguration config = LobbySystem.getInstance().getConfig();

        Vector direction = player.getLocation().getDirection().setY(0);
        Vector velocity = direction.lengthSquared() == 0
                ? new Vector()
                : direction.normalize().multiply(config.getDouble("jumppad.strength", DEFAULT_STRENGTH));

        velocity.setY(config.getDouble("jumppad.height", DEFAULT_HEIGHT));

        player.setVelocity(velocity);
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 1F);
    }

}
