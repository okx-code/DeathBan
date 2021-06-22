package sh.okx.deathban.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import sh.okx.deathban.DeathBan;
import sh.okx.deathban.database.PlayerData;
import sh.okx.deathban.events.PlayerDeathBanEvent;

@RequiredArgsConstructor
public class DeathListener implements Listener {
  private final DeathBan plugin;

  @EventHandler
  public void on(PlayerDeathEvent e) {
    Player player = e.getEntity();
    PlayerData data = plugin.getSDatabase().getData(player.getUniqueId());
    data.setDeaths(data.getDeaths() + 1);
    if(!plugin.checkBan(data)) return;

    PlayerDeathBanEvent event = new PlayerDeathBanEvent(player,
            data,
            player.hasPermission("deathban.bypass"));

    //Fire event
    Bukkit.getPluginManager().callEvent(event);

    //Revert death addition if event was cancelled
    if(event.isCancelled()){
      data.setDeaths(data.getDeaths() - 1);
      return;
    }

    //Finally ban the player
    plugin.ban(player, plugin.getGroup(player).getTime());
    plugin.getSDatabase().save(data);
  }
}
