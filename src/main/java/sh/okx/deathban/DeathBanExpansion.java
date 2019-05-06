package sh.okx.deathban;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class DeathBanExpansion extends PlaceholderExpansion {
  private final DeathBan plugin;

  @Override
  public String onPlaceholderRequest(Player p, String params) {
    if (params.equalsIgnoreCase("max_lives")) {
      return String.valueOf(plugin.getGroup(p).getLives());
    } else if (params.equalsIgnoreCase("bans")) {
      return String.valueOf(plugin.getDatabase().getData(p.getUniqueId()).getBans());
    } else if (params.equalsIgnoreCase("deaths")) {
      return String.valueOf(plugin.getDatabase().getData(p.getUniqueId()).getDeaths());
    } else if (params.equalsIgnoreCase("lives")) {
      int maxLives = plugin.getGroup(p).getLives();
      int deaths = plugin.getDatabase().getData(p.getUniqueId()).getDeaths();
      return String.valueOf(maxLives - deaths);
    }
    return null;
  }

  @Override
  public String getIdentifier() {
    return "deathban";
  }

  @Override
  public String getAuthor() {
    return "Okx";
  }

  @Override
  public String getVersion() {
    return plugin.getDescription().getVersion();
  }
}
