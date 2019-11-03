package sh.okx.deathban.timeformat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;

public class InTimeFormat implements TimeFormat {
  private final int type;
  private final List<InTime> times = new ArrayList<>();
  private final Joiner joiner;

  public InTimeFormat(int type, String string) {
    this.type = type;

    List<String> units = Arrays.asList(string.split(" "));
    add(units, "months", "month", "mo", TimeUnit.DAYS.toMillis(30));
    add(units, "weeks", "week", "w", TimeUnit.DAYS.toMillis(7));
    add(units, "days", "day", "d", TimeUnit.DAYS.toMillis(1));
    add(units, "hours", "hour", "h", TimeUnit.HOURS.toMillis(1));
    add(units, "minutes", "minute", "m", TimeUnit.MINUTES.toMillis(1));
    add(units, "seconds", "second", "s", TimeUnit.SECONDS.toMillis(1));

    this.joiner = JoinerFactory.get(type);
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
    long milliseconds = roundUpToNearestSecond(date.getTime() - System.currentTimeMillis());

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

    return joiner.join(strings);
  }

  private long roundUpToNearestSecond(long millis) {
    return (long) (Math.ceil(millis / 1000D) * 1000);
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
