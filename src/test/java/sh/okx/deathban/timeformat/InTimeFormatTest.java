package sh.okx.deathban.timeformat;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class InTimeFormatTest {
  @Test
  public void format() {
    TimeFormat format = new InTimeFormat(0, "minutes seconds?");

//    assertEquals("3 minutes", format.format(add(130_000)));
//    assertEquals("2 minutes", format.format(add(110_000)));
//    assertEquals("50 seconds", format.format(add(50_000)));
    assertEquals("1 second", format.format(add(500)));
  }

  private Date add(long millis) {
    return new Date(System.currentTimeMillis() + millis);
  }
}