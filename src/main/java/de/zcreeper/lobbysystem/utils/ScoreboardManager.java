package de.zcreeper.lobbysystem.utils;

import de.zcreeper.lobbysystem.LobbySystem;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {

    private static final String TITLE = "§6§lTEAM§b§lCASTLE";
    private static final String DEFAULT_IP = "teamcastle.net";

    private static final long UPDATE_INTERVAL = 20L;

    private static final Map<UUID, FastBoard> boards = new ConcurrentHashMap<>();

    private static BukkitTask task;

    public static void start() {
        task = Bukkit.getScheduler().runTaskTimer(LobbySystem.getInstance(), ScoreboardManager::updateAll,
                UPDATE_INTERVAL, UPDATE_INTERVAL);
    }

    public static void stop() {
        if (task != null) task.cancel();

        boards.values().forEach(FastBoard::delete);
        boards.clear();
    }

    public static void create(Player player) {
        FastBoard board = new FastBoard(player);

        board.updateTitle(TITLE);
        boards.put(player.getUniqueId(), board);

        update(board);
    }

    public static void remove(Player player) {
        FastBoard board = boards.remove(player.getUniqueId());

        if (board != null && !board.isDeleted()) board.delete();
    }

    private static void updateAll() {
        boards.values().removeIf(FastBoard::isDeleted);
        boards.values().forEach(ScoreboardManager::update);
    }

    private static void update(FastBoard board) {
        board.updateLines(
                "",
                "§6Rang:",
                "§8» §cIn Arbeit",
                " ",
                "§6Coins:",
                "§8» §b" + format(0),
                "",
                "§6Online:",
                "§8» §b" + format(Bukkit.getOnlinePlayers().size()),
                "  ",
                "§6§o" + getServerIp()
        );
    }

    private static String getServerIp() {
        return LobbySystem.getInstance().getConfig().getString("scoreboard.ip", DEFAULT_IP);
    }

    private static String format(long value) {
        return NumberFormat.getInstance(Locale.GERMANY).format(value);
    }

}
