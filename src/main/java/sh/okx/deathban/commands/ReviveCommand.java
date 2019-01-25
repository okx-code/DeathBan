package sh.okx.deathban.commands;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sh.okx.deathban.DeathBan;
import sh.okx.deathban.Group;
import sh.okx.deathban.database.PlayerData;

import java.util.Map;
import java.util.WeakHashMap;

@RequiredArgsConstructor
public class ReviveCommand implements CommandExecutor {
  private final DeathBan plugin;
  private Map<Player, Long> confirming = new WeakHashMap<>();

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length < 1 || !(sender instanceof Player)) {
      return false;
    }
    Player player = (Player) sender;
    OfflinePlayer revive = Bukkit.getOfflinePlayer(args[0]);
    if (!revive.hasPlayedBefore()) {
      player.sendMessage(plugin.getMessage("revive.invalid-player")
          .replace("%player%", args[0]));
      return true;
    } else if (revive.getUniqueId().equals(player.getUniqueId())) {
      player.sendMessage(plugin.getMessage("revive.self"));
      return true;
    }

    PlayerData data = plugin.getDatabase().getData(player.getUniqueId());
    PlayerData reviveData = plugin.getDatabase().getData(revive.getUniqueId());

    int confirm = plugin.getConfig().getInt("revive-confirm");
    if (reviveData.getBan() == null && reviveData.getDeaths() == 0) {
      send(player, plugin.getMessage("revive.max-lives"), revive);
      return true;
    } else if (confirm > 0
        && plugin.getGroup(player).getLives() - data.getDeaths() <= 1
        && confirming.getOrDefault(player, Long.MIN_VALUE) < System.currentTimeMillis()) {
      player.sendMessage(plugin.getMessage("revive.confirm"));
      confirming.put(player, System.currentTimeMillis() + (confirm * 1000));
      return true;
    }

    data.setDeaths(data.getDeaths() + 1);
    plugin.getDatabase().save(data);
    if (reviveData.getBan() != null) {
      reviveData.setBan(null);
      send(player, plugin.getMessage("revive.revived"), revive);
    } else {
      reviveData.setDeaths(reviveData.getDeaths() - 1);
      send(player, plugin.getMessage("revive.transferred"), revive);
    }

    plugin.checkBan(data);
    plugin.getDatabase().save(reviveData);
    return true;
  }

  private void send(Player player, String message, OfflinePlayer revive) {
    Group group = plugin.getGroup(player);
    PlayerData reviveData = plugin.getDatabase().getData(revive.getUniqueId());
    PlayerData data = plugin.getDatabase().getData(player.getUniqueId());
    player.sendMessage(message
        .replace("%lives%", group.getLives() - data.getDeaths() + "")
        .replace("%deaths%", data.getDeaths() + "")
        .replace("%maxlives%", group.getLives() + "")
        .replace("%bans%", reviveData.getBans() + "")
        .replace("%player%", revive.getName()));
  }
}
