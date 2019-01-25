package sh.okx.deathban.timeformat;

import lombok.RequiredArgsConstructor;

import java.text.DateFormat;
import java.util.Date;

@RequiredArgsConstructor
public class DateTimeFormat implements TimeFormat {
  private final DateFormat format;

  @Override
  public String format(Date date) {
    return format.format(date);
  }
}
