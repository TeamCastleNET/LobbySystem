package de.zcreeper.lobbysystem.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Sounds {

    public static void open(Player player) {
        play(player, Sound.BLOCK_CHEST_OPEN, 0.5F, 1.2F);
    }

    public static void close(Player player) {
        play(player, Sound.BLOCK_CHEST_CLOSE, 0.5F, 1.2F);
    }

    public static void click(Player player) {
        play(player, Sound.UI_BUTTON_CLICK, 0.6F, 1F);
    }

    public static void page(Player player) {
        play(player, Sound.ITEM_BOOK_PAGE_TURN, 1F, 1F);
    }

    public static void success(Player player) {
        play(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8F, 1.2F);
    }

    public static void purchase(Player player) {
        play(player, Sound.ENTITY_PLAYER_LEVELUP, 0.8F, 1.2F);
        play(player, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1F, 1.5F);
    }

    public static void equip(Player player) {
        play(player, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1F, 1.2F);
        play(player, Sound.ENTITY_PLAYER_LEVELUP, 0.4F, 1.8F);
    }

    public static void unequip(Player player) {
        play(player, Sound.ENTITY_ITEM_PICKUP, 1F, 0.8F);
    }

    public static void toggle(Player player, boolean on) {
        play(player, Sound.BLOCK_LEVER_CLICK, 0.8F, on ? 1.4F : 0.8F);
    }

    public static void teleport(Player player) {
        play(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 1F);
    }

    public static void connect(Player player) {
        play(player, Sound.BLOCK_PORTAL_TRIGGER, 0.3F, 1.6F);
    }

    public static void cooldown(Player player) {
        play(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.6F, 0.6F);
    }

    public static void error(Player player) {
        play(player, Sound.ENTITY_VILLAGER_NO, 1F, 1F);
    }

    private static void play(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player, sound, volume, pitch);
    }

}
