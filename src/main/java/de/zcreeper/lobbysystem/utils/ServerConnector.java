package de.zcreeper.lobbysystem.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.zcreeper.lobbysystem.LobbySystem;
import org.bukkit.entity.Player;

public class ServerConnector {

    public static final String CHANNEL = "BungeeCord";

    public static String getServerName(String key) {
        String server = LobbySystem.getInstance().getConfig().getString("teleporter." + key);

        return server == null || server.isBlank() ? null : server;
    }

    public static boolean connect(Player player, String key) {
        String server = getServerName(key);

        if (server == null) return false;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(LobbySystem.getInstance(), CHANNEL, out.toByteArray());
        return true;
    }

}
