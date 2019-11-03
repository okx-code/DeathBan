package sh.okx.deathban.timeformat;

import java.util.Iterator;

public class Joiner {
  private final String on;
  private String finalJoin;

  public static Joiner on(String on) {
    return new Joiner(on);
  }

  private Joiner(String on) {
    this.on = on;
  }

  public Joiner and() {
    return finalJoinString(" and ");
  }

  public Joiner finalJoinString(String finalJoin) {
    this.finalJoin = finalJoin;
    return this;
  }

  public String join(Iterable<String> strings) {
    return join(strings.iterator());
  }

  public String join(Iterator<String> strings) {
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
