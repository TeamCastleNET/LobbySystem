package de.zcreeper.lobbysystem.listener;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatMention implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        final String[] message = event.getMessage().split(" ");

        for (int i = 0; i < message.length; i++) {
            final String currentWord = message[i];
            final Player player;

            if (currentWord.matches("^\\w{3,16}$") && (player = Bukkit.getPlayer(currentWord)) != null) {
                message[i] = "§9@" + player.getName() + "§r";
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
        }

        event.setMessage(String.join(" ", message));

    }

}
