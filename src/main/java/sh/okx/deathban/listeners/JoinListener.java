package sh.okx.deathban.listeners;

import java.sql.Timestamp;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import sh.okx.deathban.DeathBan;
import sh.okx.deathban.Group;
import sh.okx.deathban.database.PlayerData;

@RequiredArgsConstructor
public class JoinListener implements Listener {
  private final DeathBan plugin;

  @EventHandler
  public void on(AsyncPlayerPreLoginEvent e) {
    PlayerData data = plugin.getSDatabase().getData(e.getUniqueId());
    Timestamp ban = data.getBan();
    if (ban == null) {
      return;
    } else if (!ban.after(new Date())) {
      data.setBan(null);
      plugin.getSDatabase().save(data);
      return;
    }

    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
        plugin.getDateMessage(ban, "ban"));
  }

  @EventHandler
  public void on(PlayerLoginEvent e) {
    Player player = e.getPlayer();
    PlayerData data = plugin.getSDatabase().getData(player.getUniqueId());

    if (data.isRevived()) {
      data.setRevived(false);

      Group group = plugin.getGroup(player);
      String reviveLivesString = plugin.getConfig().getString("revive-lives", "all");
      if (!reviveLivesString.equalsIgnoreCase("all")) {
        try {
          int reviveLives = Integer.parseInt(reviveLivesString);

          if (reviveLives > 0) {
            data.setDeaths(group.getLives() - reviveLives);
          } else if (reviveLives < 0) {
            data.setDeaths(-reviveLives);
          }
        } catch (NumberFormatException ignored) {
        }
      }

      plugin.getSDatabase().save(data);
    }
  }
}
