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

        connectToServer(player, server);
        return true;
    }

    // server = Name des Servers auf dem Proxy
    public static void connectToServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(LobbySystem.getInstance(), CHANNEL, out.toByteArray());
    }

    // Spielmodi aus dem Teleporter bekommen ihren schönen Namen, alle anderen Server ihren Proxy-Namen
    public static String getDisplayName(String server) {
        if (server.equalsIgnoreCase(getServerName("gemrush"))) return "§aGemRush";
        if (server.equalsIgnoreCase(getServerName("lavabattle"))) return "§dLavaBattle";

        return "§e" + server;
    }

}
