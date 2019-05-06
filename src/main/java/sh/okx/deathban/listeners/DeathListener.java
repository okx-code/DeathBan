package sh.okx.deathban.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import sh.okx.deathban.DeathBan;
import sh.okx.deathban.database.PlayerData;

@RequiredArgsConstructor
public class DeathListener implements Listener {
  private final DeathBan plugin;

  @EventHandler
  public void on(PlayerDeathEvent e) {
    Player player = e.getEntity();
    if (player.hasPermission("deathban.bypass")) {
      return;
    }

    PlayerData data = plugin.getDatabase().getData(player.getUniqueId());
    data.setDeaths(data.getDeaths() + 1);
    plugin.checkBan(data);
    plugin.getDatabase().save(data);
  }
}
