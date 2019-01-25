package sh.okx.deathban.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import sh.okx.deathban.DeathBan;
import sh.okx.deathban.database.PlayerData;

import java.sql.Timestamp;
import java.util.Date;

@RequiredArgsConstructor
public class JoinListener implements Listener {
  private final DeathBan plugin;

  @EventHandler
  public void on(AsyncPlayerPreLoginEvent e) {
    PlayerData data = plugin.getDatabase().getData(e.getUniqueId());
    Timestamp ban = data.getBan();
    if (ban == null) {
      return;
    } else if (!ban.after(new Date())) {
      data.setBan(null);
      plugin.getDatabase().save(data);
      return;
    }

    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
        plugin.getDateMessage(ban, "ban"));
  }
}
