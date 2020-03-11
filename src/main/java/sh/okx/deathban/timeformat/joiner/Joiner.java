package sh.okx.deathban.timeformat.joiner;

import java.util.Iterator;
import java.util.Objects;

public class Joiner {
  private final String on;
  private final String finalJoin;

  Joiner(String on, String finalJoin) {
    this.on = on;
    this.finalJoin = finalJoin;
  }

  Joiner(String on) {
    this(on, null);
  }

  public String join(Iterable<String> strings) {
    Objects.requireNonNull(strings);
    return join(strings.iterator());
  }

  public String join(Iterator<String> strings) {
    Objects.requireNonNull(strings);
    if (!strings.hasNext()) {
      return "";
    }

    StringBuilder result = new StringBuilder(strings.next());

    while (strings.hasNext()) {
      String string = strings.next();

      if (strings.hasNext() || finalJoin == null) {
        result.append(on);
      } else {
        result.append(finalJoin);
      }

      result.append(string);
    }

    return result.toString();
  }
}
