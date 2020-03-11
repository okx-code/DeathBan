package sh.okx.deathban.commands;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;
import sh.okx.deathban.DeathBan;
import sh.okx.deathban.database.PlayerData;
import sh.okx.timeapi.TimeAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class DeathBanCommand implements TabExecutor {
  private final DeathBan plugin;

  @SuppressWarnings("deprecation")
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length < 1 || !sender.hasPermission("deathban.command.admin")) {
      return usage(sender, label);
    } else if (args[0].equalsIgnoreCase("reset") && args.length > 1) {
      OfflinePlayer player = Bukkit.getPlayer(args[1]);
      if (player == null) {
        player = Bukkit.getOfflinePlayer(args[1]);
      }
      if (!player.hasPlayedBefore()) {
        sender.sendMessage(ChatColor.RED + "Invalid player");
        return true;
      }

      PlayerData data = plugin.getSDatabase().getData(player.getUniqueId());
      data.setDeaths(0);
      data.setBan(null);
      plugin.getSDatabase().save(data);

      sender.sendMessage(ChatColor.GREEN + player.getName() + " has been reset.");
      return true;
    } else if (args[0].equalsIgnoreCase("set") && args.length > 2) {
      int lives;
      try {
        lives = Integer.parseInt(args[2]);
      } catch (NumberFormatException e) {
        sender.sendMessage(ChatColor.RED + "Invalid number");
        return true;
      }

      OfflinePlayer player = Bukkit.getPlayer(args[1]);
      if (player == null) {
        player = Bukkit.getOfflinePlayer(args[1]);
      }
      if (!player.hasPlayedBefore()) {
        sender.sendMessage(ChatColor.RED + "Invalid player");
        return true;
      }

      PlayerData data = plugin.getSDatabase().getData(player.getUniqueId());
      int maxLives = plugin.getGroup(player).getLives();
      data.setDeaths(maxLives - lives);
      plugin.getSDatabase().save(data);

      sender.sendMessage(ChatColor.GREEN + player.getName() + " now has " + lives + " lives.");
      return true;
    } else if (args[0].equalsIgnoreCase("add") && args.length > 2) {
      int add;
      try {
        add = Integer.parseInt(args[2]);
      } catch (NumberFormatException e) {
        sender.sendMessage(ChatColor.RED + "Invalid number");
        return true;
      }

      OfflinePlayer player = Bukkit.getPlayer(args[1]);
      if (player == null) {
        player = Bukkit.getOfflinePlayer(args[1]);
      }
      if (!player.hasPlayedBefore()) {
        sender.sendMessage(ChatColor.RED + "Invalid player");
        return true;
      }

      PlayerData data = plugin.getSDatabase().getData(player.getUniqueId());
      data.setDeaths(data.getDeaths() - add);
      plugin.getSDatabase().save(data);

      sender.sendMessage(ChatColor.GREEN + player.getName() + " now has "
          + (plugin.getGroup(player).getLives() - data.getDeaths()) + " lives.");
      return true;
    } else if (args[0].equalsIgnoreCase("ban") && args.length > 2) {
      long time;
      try {
        time = new TimeAPI(String.join(" ", Arrays.copyOfRange(args, 2, args.length))).getMilliseconds();
      } catch (IllegalArgumentException e) {
        sender.sendMessage(ChatColor.RED + "Invalid time");
        return true;
      }

      Player player = Bukkit.getPlayer(args[1]);
      if (player == null) {
        sender.sendMessage(ChatColor.RED + "Invalid player");
        return true;
      }

      plugin.ban(player, time);
      sender.sendMessage(ChatColor.GREEN + player.getName() + " has been banned.");
      return true;
    } else if (args[0].equalsIgnoreCase("reload")) {
      plugin.reload();
      sender.sendMessage(getTitle() + ChatColor.GREEN + " Reloaded.");
      return true;
    }
    return usage(sender, label);
  }

  private boolean usage(CommandSender sender, String label) {
    StringBuilder help = new StringBuilder();
    help.append(getTitle());
    if (sender.hasPermission("deathban.command.admin")) {
      usage(help, label, "reset <player>", "Remove a death ban from a player and give them their maximum lives");
      usage(help, label, "set <player> <lives>", "Set the amount of lives for a player");
      usage(help, label, "add <player> <lives>", "Add lives to a player");
      usage(help, label, "ban <player> <time>", "Ban a player as if they had run out of lives");
    }
    sender.sendMessage(help.toString());
    return false;
  }

  private String getTitle() {
    PluginDescriptionFile description = plugin.getDescription();
    return String.valueOf(ChatColor.GREEN) + ChatColor.BOLD + description.getFullName() +
        ChatColor.YELLOW + " by " +
        ChatColor.BLUE + ChatColor.BOLD + description.getAuthors().get(0);
  }

  private void usage(StringBuilder builder, String label, String command, String description) {
    builder.append(ChatColor.GREEN).append("\n/").append(label).append(" ").append(command)
        .append(" ").append(ChatColor.YELLOW).append(description);
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (args.length > 2 || !sender.hasPermission("deathban.command.admin")) {
      return Collections.emptyList();
    } else if (args.length == 1) {
      return StringUtil.copyPartialMatches(args[0], Arrays.asList("unban", "reset", "set", "add", "ban", "help"), new ArrayList<>());
    }
    return null;
  }
}
