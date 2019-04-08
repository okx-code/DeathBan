package sh.okx.deathban.database;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;
import sh.okx.deathban.DeathBan;
import sh.okx.timeapi.TimeAPI;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Database {
  private static final String CREATE_PLAYERS = "CREATE TABLE IF NOT EXISTS players (" +
      "uuid VARCHAR(36) PRIMARY KEY," +
      "ban TIMESTAMP," +
      "deaths INT," +
      "bans INT)";
  private static final String GET_PLAYERS = "SELECT * FROM players WHERE uuid=?";
  private static final String SET_PLAYERS = "INSERT OR REPLACE INTO players VALUES (?, ?, ?, ?)";

  private final LoadingCache<UUID, PlayerData> data;
  private final Queue<PlayerData> saveQueue = new LinkedBlockingQueue<>();
  private final BukkitTask flushTask;

  private Connection connection;

  @SneakyThrows(SQLException.class)
  public Database(DeathBan plugin) {
    ConfigurationSection config = plugin.getConfig().getConfigurationSection("database");

    data = CacheBuilder.newBuilder()
        .expireAfterAccess(new TimeAPI(config.getString("cache.expire-after-access")).getSeconds(), TimeUnit.SECONDS)
        .maximumSize(config.getInt("cache.size"))
        .build(new CacheLoader<UUID, PlayerData>() {
          @Override
          @SneakyThrows(SQLException.class)
          public PlayerData load(UUID key) {
            PreparedStatement statement = connection.prepareStatement(GET_PLAYERS);
            statement.setString(1, key.toString());

            ResultSet results = statement.executeQuery();
            if (results.next()) {
              return new PlayerData(key,
                  results.getTimestamp("ban"),
                  results.getInt("deaths"),
                  results.getInt("bans"));
            } else {
              return new PlayerData(key);
            }
          }
        });

    File dataFolder = plugin.getDataFolder();
    if (dataFolder.exists()) {
      dataFolder.mkdir();
    }

    File databaseFile = new File(dataFolder, "database.db");

    connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);

    connection.createStatement().executeUpdate(CREATE_PLAYERS);

    long interval = new TimeAPI(config.getString("flush-interval")).getSeconds() * 20;
    flushTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::flush, interval, interval);
  }

  public PlayerData getData(UUID uuid) {
    return data.getUnchecked(uuid);
  }

  public void save(PlayerData playerData) {
    data.put(playerData.getUuid(), playerData);
    saveQueue.add(playerData);
  }

  @SneakyThrows(SQLException.class)
  public synchronized void close() {
    flush();
    flushTask.cancel();
    connection.close();
  }

  @SneakyThrows(SQLException.class)
  public synchronized void flush() {
    PreparedStatement statement = connection.prepareStatement(SET_PLAYERS);
    while (!saveQueue.isEmpty()) {
      PlayerData data = saveQueue.remove();
      statement.setString(1, data.getUuid().toString());
      statement.setTimestamp(2, data.getBan());
      statement.setInt(3, data.getDeaths());
      statement.setInt(4, data.getBans());
      statement.addBatch();
    }

    statement.executeBatch();
  }
}
