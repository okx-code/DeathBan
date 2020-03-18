package sh.okx.deathban;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sh.okx.deathban.commands.DeathBanCommand;
import sh.okx.deathban.commands.LivesCommand;
import sh.okx.deathban.commands.ReviveCommand;
import sh.okx.deathban.database.Database;
import sh.okx.deathban.database.PlayerData;
import sh.okx.deathban.listeners.DeathListener;
import sh.okx.deathban.listeners.JoinListener;
import sh.okx.deathban.timeformat.TimeFormat;
import sh.okx.deathban.timeformat.TimeFormatFactory;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import sh.okx.deathban.update.JoinUpdateNotifier;
import sh.okx.deathban.update.UpdateNotifier;
import sh.okx.deathban.update.VersionChecker;

public class DeathBan extends JavaPlugin {
  private Database database;
  private Group defaultGroup;
  private Set<Group> groups;
  private TimeFormat timeFormat;

  public Database getSDatabase() {
    return database;
  }

  @Override
  public void onEnable() {
    UpdateNotifier notifier = new UpdateNotifier(new VersionChecker(this));

    saveDefaultConfig();
    init();

    getServer().getPluginManager().registerEvents(new DeathListener(this), this);
    getServer().getPluginManager().registerEvents(new JoinListener(this), this);
    getServer().getPluginManager().registerEvents(new JoinUpdateNotifier(notifier, () -> getConfig().getBoolean("notify-update"), "deathban.notify"), this);

    getCommand("lives").setExecutor(new LivesCommand(this));
    getCommand("revive").setExecutor(new ReviveCommand(this));
    getCommand("deathban").setExecutor(new DeathBanCommand(this));

    Metrics metrics = new Metrics(this, 3947);
    metrics.addCustomChart(new Metrics.SimplePie("time_format",
        () -> getConfig().getString("time-format").split(" ")[0]));

    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      new DeathBanExpansion(this).register();
    }
  }

  public void reload() {
    closeDatabase();
    reloadConfig();
    init();
  }

  private void init() {
    timeFormat = TimeFormatFactory.get(getConfig().getString("time-format"));
    database = new Database(this);
    defaultGroup = Group.deserialize(Objects.requireNonNull(getConfig().getConfigurationSection("default"), "The default group must exist"));

    groups = new TreeSet<>();
    ConfigurationSection sections = getConfig().getConfigurationSection("groups");
    if (sections != null) {
      for (String key : sections.getKeys(false)) {
        ConfigurationSection section = Objects.requireNonNull(sections.getConfigurationSection(key));
        groups.add(Group.deserialize(section));
      }
    }

  }

  @Override
  public void onDisable() {
    closeDatabase();
  }

  private void closeDatabase() {
    if (database != null) {
      database.close();
    }
  }

  public boolean checkBan(PlayerData data) {
    Player player = Bukkit.getPlayer(data.getUuid());
    Group group = getGroup(player);
    if (data.getDeaths() < group.getLives()) {
      return false;
    }

    Bukkit.getScheduler().runTask(this, () -> ban(player, group.getTime(data.getBans())));
    return true;
  }

  public void ban(Player player, long time) {
    Group group = getGroup(player);
    PlayerData data = database.getData(player.getUniqueId());

    data.setDeaths(0);
    Timestamp ban = new Timestamp(System.currentTimeMillis() + time);
    data.setBan(ban);
    data.setBans(data.getBans() + 1);

    for (String command : group.getCommands()) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replaceStats(command, player));
    }
    player.kickPlayer(replaceStats(getDateMessage(ban, "kick"), player));
  }

  public String getDateMessage(Date date, String type) {
    return getMessage(type)
        .replace("%time%", timeFormat.format(date));
  }

  public String replaceStats(String string, Player player) {
    PlayerData data = database.getData(player.getUniqueId());
    Group group = getGroup(player);
    return ChatColor.translateAlternateColorCodes('&', string)
        .replace("%player%", player.getName())
        .replace("%lives%", group.getLives() - data.getDeaths() + "")
        .replace("%maxlives%", group.getLives() + "")
        .replace("%deaths%", data.getDeaths() + "")
        .replace("%bans%", data.getBans() + "");
  }

  public String getMessage(String path) {
    String message = Objects.requireNonNull(getConfig().getString("messages." + path),
        "Message " + path + " not found in config");
    return ChatColor.translateAlternateColorCodes('&', message);
  }

  public Group getGroup(OfflinePlayer player) {
    if (!(player instanceof Player) || !player.isOnline()) {
      return defaultGroup;
    }
    Player p = (Player) player;
    for (Group group : groups) {
      if (p.hasPermission(group.getPermission())) {
        return group;
      }
    }
    return defaultGroup;
  }
}
