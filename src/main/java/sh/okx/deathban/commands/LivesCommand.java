package sh.okx.deathban.commands;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sh.okx.deathban.DeathBan;

@RequiredArgsConstructor
public class LivesCommand implements CommandExecutor {
  private final DeathBan plugin;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    Player player;
    if (args.length == 0) {
      if (!(sender instanceof Player)) {
        return false;
      }
      player = (Player) sender;
    } else {
      player = Bukkit.getPlayer(args[0]);
      if (player == null) {
        sender.sendMessage(plugin.getMessage("lives.invalid-player"));
        return true;
      }
    }

    String message = plugin.getMessage("lives." + (player == sender ? "self" : "other"));
    sender.sendMessage(plugin.replaceStats(message, player));
    return true;
  }
}
