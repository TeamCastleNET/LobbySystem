package de.zcreeper.lobbysystem.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.data.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DatabaseManager {

    /*
    Features:

    - erkennen wenn ein Spieler das erste mal joint + extra 500 Coins
    - Coins, Spielzeit und Freunde aus den entsprechenden Datenbanken für das Scoreboard holen


     */

    private static final String TABLE = "lobby";

    private static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS %s (
                uuid CHAR(36) NOT NULL PRIMARY KEY,
                name VARCHAR(16) NULL,
                cookies BIGINT NOT NULL DEFAULT 0,
                cosmetics_owned TEXT NULL,
                cosmetics_selected TEXT NULL,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4""".formatted(TABLE);

    private static final String SELECT = "SELECT name, cookies, cosmetics_owned, cosmetics_selected FROM " + TABLE + " WHERE uuid = ?";

    private static final String UPSERT = "INSERT INTO " + TABLE + " (uuid, name, cookies, cosmetics_owned, cosmetics_selected) "
            + "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
            + "name = VALUES(name), cookies = VALUES(cookies), "
            + "cosmetics_owned = VALUES(cosmetics_owned), cosmetics_selected = VALUES(cosmetics_selected)";

    private static HikariDataSource dataSource;

    // Ein einzelner Thread, damit Speichervorgänge eines Spielers immer in der richtigen Reihenfolge ankommen
    private static ExecutorService executor;

    public static boolean connect() {
        FileConfiguration config = LobbySystem.getInstance().getConfig();

        String host = config.getString("mysql.host", "localhost");
        int port = config.getInt("mysql.port", 3306);
        String database = config.getString("mysql.database", "lobby");
        String properties = config.getString("mysql.properties", "");

        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("LobbySystem-Pool");
        hikari.setDriverClassName("org.mariadb.jdbc.Driver");
        hikari.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database + (properties.isBlank() ? "" : "?" + properties));
        hikari.setUsername(config.getString("mysql.username", "root"));
        hikari.setPassword(config.getString("mysql.password", ""));
        hikari.setMaximumPoolSize(config.getInt("mysql.pool-size", 5));
        hikari.setConnectionTimeout(TimeUnit.SECONDS.toMillis(10));

        try {
            dataSource = new HikariDataSource(hikari);

            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.executeUpdate(CREATE_TABLE);
            }
        } catch (Exception exception) {
            LobbySystem.getInstance().getLogger().severe("Verbindung zur MySQL-Datenbank fehlgeschlagen: " + exception.getMessage());
            disconnect();
            return false;
        }

        executor = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "LobbySystem-Database"));

        LobbySystem.getInstance().getLogger().info("Mit der MySQL-Datenbank verbunden (" + host + ":" + port + "/" + database + ").");
        return true;
    }

    public static void disconnect() {
        awaitPending();

        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    // Wartet, bis alle offenen Speichervorgänge durch sind. Danach wird direkt gespeichert.
    public static void awaitPending() {
        if (executor != null) {
            executor.shutdown();

            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) executor.shutdownNow();
            } catch (InterruptedException exception) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            executor = null;
        }
    }

    public static boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    // Blockiert - nur außerhalb des Main-Threads aufrufen (z. B. im AsyncPlayerPreLoginEvent)
    public static PlayerData load(UUID uuid, String name) throws SQLException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(SELECT)) {
            statement.setString(1, uuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return new PlayerData(uuid, name, true);

                PlayerData data = new PlayerData(uuid, name, false);

                data.setCookies(result.getLong("cookies"));
                data.deserializeOwned(result.getString("cosmetics_owned"));
                data.deserializeSelected(result.getString("cosmetics_selected"));
                data.setDirty(false);

                return data;
            }
        }
    }

    /*
    Lädt über den Datenbank-Thread und wartet auf das Ergebnis. So kommt ein Speichervorgang vom
    letzten Verlassen garantiert vorher an, auch wenn der Spieler sofort wieder joint.
    Blockiert - nur außerhalb des Main-Threads aufrufen.
     */
    public static PlayerData loadQueued(UUID uuid, String name) throws SQLException {
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return load(uuid, name);
                } catch (SQLException exception) {
                    throw new CompletionException(exception);
                }
            }, executor).join();
        } catch (CompletionException exception) {
            if (exception.getCause() instanceof SQLException sqlException) throw sqlException;
            throw new SQLException(exception.getCause());
        }
    }

    // Blockiert - beim Abschalten des Servers oder auf dem Datenbank-Thread
    public static void save(PlayerData data) throws SQLException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(UPSERT)) {
            statement.setString(1, data.getUuid().toString());
            statement.setString(2, data.getName());
            statement.setLong(3, data.getCookies());
            statement.setString(4, data.serializeOwned());
            statement.setString(5, data.serializeSelected());
            statement.executeUpdate();
        }
    }

    public static CompletableFuture<Void> saveAsync(PlayerData data) {
        // Kopie, damit der Datenbank-Thread nicht liest, während der Main-Thread schreibt
        PlayerData snapshot = data.copy();

        data.setDirty(false);

        Runnable task = () -> {
            try {
                save(snapshot);
            } catch (SQLException exception) {
                data.setDirty(true);
                LobbySystem.getInstance().getLogger().severe("Speichern von " + snapshot.getName() + " fehlgeschlagen: " + exception.getMessage());
            }
        };

        if (executor == null) {
            task.run();
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(task, executor);
    }

}
