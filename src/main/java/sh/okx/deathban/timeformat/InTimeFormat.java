package sh.okx.deathban.timeformat;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InTimeFormat implements TimeFormat {
  private final int type;
  private List<InTime> times = new ArrayList<>();

  public InTimeFormat(int type, String string) {
    this.type = type;

    List<String> units = Arrays.asList(string.split(" "));
    add(units, "months", "month", "mo", TimeUnit.DAYS.toMillis(30));
    add(units, "weeks", "week", "w", TimeUnit.DAYS.toMillis(7));
    add(units, "days", "day", "d", TimeUnit.DAYS.toMillis(1));
    add(units, "hours", "hour", "h", TimeUnit.HOURS.toMillis(1));
    add(units, "minutes", "minute", "m", TimeUnit.MINUTES.toMillis(1));
    add(units, "seconds", "second", "s", TimeUnit.SECONDS.toMillis(1));
  }

  private void add(List<String> units, String unit, String singular, String shortened, long time) {
    if (units.contains(unit)) {
      times.add(new InTime(singular, unit, shortened, time, false));
    } else if (units.contains(unit + "?")) {
      times.add(new InTime(singular, unit, shortened, time, true));
    }
  }

  @Override
  public String format(Date date) {
    // round up to the nearest second
    long milliseconds = (long) (Math.ceil((date.getTime() - System.currentTimeMillis()) / 1000D) * 1000);

    List<String> strings = new ArrayList<>();

    long amount = 0;
    for (InTime time : times) {
      if (time.weak && amount > 0) {
        continue;
      }

      amount = milliseconds / time.time;
      if (amount < 1) {
        continue;
      }
      milliseconds %= time.time;

      if (type > 3) {
        strings.add(amount + time.shortened);
      } else if (amount == 1) {
        strings.add(amount + " " + time.singular);
      } else {
        strings.add(amount + " " + time.plural);
      }
    }

    if (type == 1 || type == 4) {
      return join(strings, ", ", true);
    } else if (type == 2 || type == 5) {
      return join(strings, ", ", false);
    } else if (type == 3 || type == 6) {
      return join(strings, " ", false);
    } else /*if (type == 7)*/ {
      return join(strings, "", false);
    }
  }

  private String join(List<String> strings, String join, boolean and) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < strings.size(); i++) {
      if (i > 0) {
        if (and && i == strings.size() - 1) {
          result.append(" and ");
        } else {
          result.append(join);
        }
      }
      result.append(strings.get(i));
    }
    return result.toString();
  }

  @RequiredArgsConstructor
  class InTime {
    private final String singular;
    private final String plural;
    private final String shortened;
    private final long time;
    private final boolean weak;
  }
}
