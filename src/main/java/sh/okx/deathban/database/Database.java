package sh.okx.deathban.database;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import sh.okx.deathban.DeathBan;
import sh.okx.timeapi.TimeAPI;

public class Database {

  private static final int VERSION = 1;

  private static final String PLAYERS_EXISTS = "PRAGMA table_info(players)";
  private static final String CREATE_PLAYERS = "CREATE TABLE IF NOT EXISTS players (" +
      "uuid VARCHAR(36) PRIMARY KEY," +
      "ban TIMESTAMP," +
      "deaths INT," +
      "bans INT," +
      "revived INT DEFAULT 0)";
  private static final String GET_PLAYERS = "SELECT * FROM players WHERE uuid=?";
  private static final String SET_PLAYERS = "INSERT OR REPLACE INTO players VALUES (?, ?, ?, ?, ?)";

  private final LoadingCache<UUID, PlayerData> data;
  private final Set<PlayerData> saveQueue = new HashSet<>();

  private Connection connection;

  @SneakyThrows(SQLException.class)
  public Database(DeathBan plugin) {
    ConfigurationSection config = plugin.getConfig().getConfigurationSection("database");

    //long expireSeconds = new TimeAPI(config.getString("cache.expire-after-access")).getSeconds();
    data = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.DAYS)
        .maximumSize(1000)
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
                  results.getInt("bans"),
                  results.getBoolean("revived"));
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

    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException ignored) {
    }
    connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);

    boolean updated = false;

    ResultSet resultSet = connection.createStatement().executeQuery(PLAYERS_EXISTS);
    // there will be no rows returned if it doesn't exist
    if (!resultSet.next()) {
      // set to latest version since we are just making the table
      // otherwise let it fall through
      setUserVersion(VERSION);
      updated = true;
    }

    connection.createStatement().executeUpdate(CREATE_PLAYERS);

    // version 0 - base
    // version 1 - added "revive" field

    if (getUserVersion() == 0) {
      // add revive field
      connection.createStatement()
          .executeUpdate("ALTER TABLE players ADD COLUMN revived INT DEFAULT 0");
    }

    if (!updated) {
      setUserVersion(VERSION);
    }

    long interval = new TimeAPI(config.getString("flush-interval")).getSeconds();
    plugin.getDeathExecutor().scheduleAtFixedRate(this::flush, interval, interval, TimeUnit.SECONDS);
  }

  @SneakyThrows(SQLException.class)
  private int getUserVersion() {
    ResultSet userVersion = connection.createStatement().executeQuery("PRAGMA user_version");
    if (userVersion.next()) {
      return userVersion.getInt("user_version");
    } else {
      return 0;
    }

  }

  @SneakyThrows(SQLException.class)
  private void setUserVersion(int version) {
    connection.createStatement().executeUpdate("PRAGMA user_version = " + version);
  }

  public PlayerData getData(UUID uuid) {
    return data.getUnchecked(uuid);
  }

  public void save(PlayerData playerData) {
    data.put(playerData.getUuid(), playerData);

    saveQueue.add(playerData);
  }

  private void queue(PlayerData data) {
    saveQueue.removeIf(next -> next.getUuid().equals(data.getUuid()));
    saveQueue.add(data);
  }

  @SneakyThrows(SQLException.class)
  public synchronized void close() {
    flush();
    connection.close();
  }

  @SneakyThrows(SQLException.class)
  public synchronized void flush() {
    PreparedStatement statement = connection.prepareStatement(SET_PLAYERS);
    for (PlayerData data : saveQueue) {
      statement.setString(1, data.getUuid().toString());
      statement.setTimestamp(2, data.getBan());
      statement.setInt(3, data.getDeaths());
      statement.setInt(4, data.getBans());
      statement.setInt(5, data.isRevived() ? 1 : 0);
      statement.addBatch();
    }

    statement.executeBatch();
  }
}
