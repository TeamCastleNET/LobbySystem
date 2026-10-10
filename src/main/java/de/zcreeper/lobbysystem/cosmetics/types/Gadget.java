package de.zcreeper.lobbysystem.cosmetics.types;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.cosmetics.Cosmetic;
import de.zcreeper.lobbysystem.cosmetics.CosmeticCategory;
import de.zcreeper.lobbysystem.cosmetics.CosmeticEffects;
import de.zcreeper.lobbysystem.cosmetics.CosmeticManager;
import de.zcreeper.lobbysystem.utils.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public enum Gadget implements Cosmetic {

    FIREWORK("feuerwerk", "§cFeuerwerk", Material.FIREWORK_ROCKET, "Zünde ein buntes Feuerwerk.", 5,
            Gadget::firework),

    ROCKET_JUMP("raketensprung", "§eRaketensprung", Material.FEATHER, "Schieß dich hoch in die Luft.", 8,
            Gadget::rocketJump),

    MAGIC_WAND("zauberstab", "§dZauberstab", Material.BLAZE_ROD, "Verzaubere andere Spieler aus der Ferne.", 3,
            Gadget::magicWand),

    MUSIC_BOX("musikbox", "§eMusikbox", Material.JUKEBOX, "Spielt eine kleine Melodie.", 12,
            Gadget::musicBox),

    CONFETTI("konfetti", "§bKonfetti", Material.PAPER, "Feier dich selbst!", 4,
            Gadget::confetti);

    private static final String GADGET_KEY = "cosmetic_gadget";

    private static final Color[] FIREWORK_COLORS = {
            Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.AQUA, Color.BLUE, Color.FUCHSIA, Color.WHITE
    };

    // {Tonhöhe auf dem Notenblock (0-24), Länge in Ticks} - "Alle meine Entchen"
    private static final int[][] MELODY = {
            {6, 4}, {8, 4}, {10, 4}, {11, 4}, {13, 8}, {13, 8},
            {15, 4}, {15, 4}, {15, 4}, {15, 4}, {13, 16},
            {15, 4}, {15, 4}, {15, 4}, {15, 4}, {13, 16},
            {11, 4}, {11, 4}, {11, 4}, {11, 4}, {10, 8}, {10, 8},
            {8, 4}, {8, 4}, {8, 4}, {8, 4}, {6, 16}
    };

    private final String id;
    private final String displayName;
    private final Material icon;
    private final String description;
    private final int cooldown;
    private final Action action;

    Gadget(String id, String displayName, Material icon, String description, int cooldown, Action action) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.cooldown = cooldown;
        this.action = action;
    }

    public void use(Player player) {
        action.use(player);
    }

    public int getCooldown() {
        return cooldown;
    }

    public ItemStack getItem() {
        ItemStack item = new ItemBuilder(icon)
                .displayname(displayName + " §8(§7Rechtsklick§8)")
                .lore("§7" + description, "§7Abklingzeit: §e" + cooldown + "s")
                .flag(ItemFlag.HIDE_ATTRIBUTES)
                .build();

        ItemMeta meta = item.getItemMeta();

        meta.getPersistentDataContainer().set(getKey(), PersistentDataType.STRING, id);
        item.setItemMeta(meta);

        return item;
    }

    public static Gadget fromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        String id = item.getItemMeta().getPersistentDataContainer().get(getKey(), PersistentDataType.STRING);

        return id == null ? null : (Gadget) CosmeticCategory.GADGET.getCosmetic(id);
    }

    private static NamespacedKey getKey() {
        return new NamespacedKey(LobbySystem.getInstance(), GADGET_KEY);
    }

    private static void firework(Player player) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        FireworkEffect.Type[] types = FireworkEffect.Type.values();

        FireworkEffect effect = FireworkEffect.builder()
                .with(types[random.nextInt(types.length)])
                .withColor(randomColor(), randomColor())
                .withFade(randomColor())
                .flicker(random.nextBoolean())
                .trail(true)
                .build();

        player.getWorld().spawn(player.getLocation(), Firework.class, firework -> {
            CosmeticManager.markEntity(firework);

            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(effect);
            meta.setPower(1);
            firework.setFireworkMeta(meta);
        });
    }

    private static void rocketJump(Player player) {
        Vector direction = player.getLocation().getDirection().setY(0);
        Vector velocity = direction.lengthSquared() == 0 ? new Vector() : direction.normalize().multiply(0.6);

        player.setVelocity(velocity.setY(1.6));

        CosmeticEffects.sound(player, player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1F, 0.8F);
        CosmeticEffects.particle(player, Particle.EXPLOSION, player.getLocation(), 1, 0, 0, 0, 0);

        new BukkitRunnable() {
            private int ticks;

            @Override
            public void run() {
                if (!player.isOnline() || ticks++ > 20) {
                    cancel();
                    return;
                }

                CosmeticEffects.particle(player, Particle.FIREWORK, player.getLocation(), 3, 0.1, 0, 0.1, 0.02);
                CosmeticEffects.particle(player, Particle.FLAME, player.getLocation(), 2, 0.1, 0, 0.1, 0.01);
            }
        }.runTaskTimer(LobbySystem.getInstance(), 1L, 1L);
    }

    private static void magicWand(Player player) {
        Location eye = player.getEyeLocation();
        Vector step = eye.getDirection().multiply(0.5);
        Location point = eye.clone();

        CosmeticEffects.sound(player, eye, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1F, 1.5F);

        for (int i = 0; i < 40; i++) {
            point.add(step);

            if (point.getBlock().getType().isSolid()) break;

            CosmeticEffects.dust(player, point, CosmeticEffects.rainbow(i / 40D), 1F);

            for (Player target : point.getWorld().getNearbyPlayers(point, 0.8)) {
                if (target.equals(player) || !player.canSee(target)) continue;

                target.setVelocity(step.clone().normalize().multiply(0.4).setY(0.7));

                CosmeticEffects.particle(player, Particle.TOTEM_OF_UNDYING, target.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.3);
                CosmeticEffects.sound(player, target.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1F, 1.2F);
                target.sendMessage(LobbySystem.getPrefix() + "§dDu wurdest von §6" + player.getName() + " §dverzaubert!");
                return;
            }
        }
    }

    private static void musicBox(Player player) {
        new BukkitRunnable() {
            private int index;
            private int wait;

            @Override
            public void run() {
                if (!player.isOnline() || index >= MELODY.length) {
                    cancel();
                    return;
                }

                if (wait-- > 0) return;

                int[] note = MELODY[index++];
                Location head = player.getLocation().add(0, 2.3, 0);
                float pitch = (float) Math.pow(2, (note[0] - 12) / 12D);

                CosmeticEffects.sound(player, head, Sound.BLOCK_NOTE_BLOCK_BELL, 1F, pitch);
                CosmeticEffects.particle(player, Particle.NOTE, head, 0, note[0] / 24D, 0, 0, 1);

                wait = note[1] - 1;
            }
        }.runTaskTimer(LobbySystem.getInstance(), 0L, 1L);
    }

    private static void confetti(Player player) {
        Location location = player.getLocation().add(0, 1.2, 0);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < 60; i++) {
            Location piece = location.clone().add(random.nextGaussian() * 0.8, random.nextDouble() * 1.5, random.nextGaussian() * 0.8);
            CosmeticEffects.dust(player, piece, randomColor(), 1.1F);
        }

        CosmeticEffects.particle(player, Particle.FIREWORK, location, 20, 0.3, 0.3, 0.3, 0.15);
        CosmeticEffects.sound(player, location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1F, 1.2F);
        CosmeticEffects.sound(player, location, Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.8F);
    }

    private static Color randomColor() {
        return FIREWORK_COLORS[ThreadLocalRandom.current().nextInt(FIREWORK_COLORS.length)];
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
        return CosmeticCategory.GADGET;
    }

    @FunctionalInterface
    private interface Action {
        void use(Player player);
    }

}
