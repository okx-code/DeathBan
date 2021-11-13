package sh.okx.deathban.timeformat;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InTimeFormatTest {

  @Test
  public void format() {
    TimeFormat format = new InTimeFormat(1, "minutes seconds?");

    assertEquals("2 minutes", format.format(add(130_000)));
    assertEquals("1 minute", format.format(add(110_000)));
    assertEquals("50 seconds", format.format(add(50_000)));
    assertEquals("1 second", format.format(add(500)));
  }

  @Test
  public void testJoiner() {
    long millis = TimeUnit.HOURS.toMillis(1)
        + TimeUnit.MINUTES.toMillis(1)
        + TimeUnit.SECONDS.toMillis(1);

    assertEquals("1 hour, 1 minute and 1 second",
        new InTimeFormat(1, "hours minutes seconds").format(add(millis)));

    assertEquals("1 hour, 1 minute, 1 second",
        new InTimeFormat(2, "hours minutes seconds").format(add(millis)));

    assertEquals("1 hour 1 minute 1 second",
        new InTimeFormat(3, "hours minutes seconds").format(add(millis)));

    assertEquals("1h, 1m and 1s",
        new InTimeFormat(4, "hours minutes seconds").format(add(millis)));

    assertEquals("1h, 1m, 1s",
        new InTimeFormat(5, "hours minutes seconds").format(add(millis)));

    assertEquals("1h 1m 1s",
        new InTimeFormat(6, "hours minutes seconds").format(add(millis)));

    assertEquals("1h1m1s",
        new InTimeFormat(7, "hours minutes seconds").format(add(millis)));
  }

  private Date add(long millis) {
    return new Date(System.currentTimeMillis() + millis);
  }
}