package sh.okx.deathban.timeformat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class TimeFormatFactory {
  public static TimeFormat get(String options) {
    Objects.requireNonNull(options);

    String[] parts = options.split(" ");
    if (parts.length < 2) {
      throw new RuntimeException("Time format must have a space");
    } else if (parts[0].equalsIgnoreCase("in")) {
      int type;
      try {
        type = Integer.parseInt(parts[1]);
        if (type < 1 || type > 7) {
          throw new RuntimeException("in <number> number must be between 1 and 7");
        }
      } catch (NumberFormatException e) {
        throw new RuntimeException(parts[1] + " is an invalid number in '" + options + "'");
      }

      return new InTimeFormat(type, options.split(" ", 3)[2]);
    } else if (parts[0].equalsIgnoreCase("date-format")) {
      if (parts.length < 3) {
        throw new RuntimeException("date-format must have SHORT/MEDIUM/LONG/FULL SHORT/MEDIUM/LONG/FULL");
      }
      int i = toDateFormatLength(parts[1]);
      int i1 = toDateFormatLength(parts[2]);

      return new DateTimeFormat(DateFormat.getDateTimeInstance(i, i1));
    } else if (parts[0].equalsIgnoreCase("custom-date-format")) {
      SimpleDateFormat format;
      try {
        format = new SimpleDateFormat(options.split(" ", 2)[1]);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException("Could not parse custom-date-format: " + e.getMessage());
      }

      return new DateTimeFormat(format);
    } else {
      throw new RuntimeException("Time format must start with: in, date-format or custom-date-format");
    }
  }

  private static int toDateFormatLength(String string) {
    switch (string.toUpperCase()) {
      case "SHORT":
        return DateFormat.SHORT;
      case "MEDIUM":
        return DateFormat.MEDIUM;
      case "LONG":
        return DateFormat.LONG;
      case "FULL":
        return DateFormat.FULL;
      default:
        throw new RuntimeException("'" + string + "' is not SHORT, MEDIUM, LONG or FULL.");
    }
  }
}
