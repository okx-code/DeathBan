package sh.okx.deathban;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import sh.okx.timeapi.TimeAPI;

import java.util.ArrayList;
import java.util.List;

@Data
public class Group implements Comparable<Group> {
  private final String permission;
  private final int lives;
  private final int priority;
  private final List<String> commands;
  private final List<Long> times = new ArrayList<>();
  private long time = -1;

  public static Group deserialize(ConfigurationSection section) {
    Group group = new Group(
        section.getString("permission"),
        section.getInt("lives"),
        section.getInt("priority"),
        section.getStringList("commands"));
    if (section.isList("time")) {
      for (String t : section.getStringList("time")) {
        group.times.add(new TimeAPI(t).getMilliseconds());
      }
    } else {
      group.time = new TimeAPI(section.getString("time")).getMilliseconds();
    }
    return group;
  }

  @Override
  public int compareTo(Group group) {
    return Integer.compare(priority, group.getPriority());
  }

  public long getTime(int bans) {
    if (time != -1) {
      return time;
    } else {
      return bans >= times.size() ? 3155695200000L : times.get(bans);
    }
  }
}
