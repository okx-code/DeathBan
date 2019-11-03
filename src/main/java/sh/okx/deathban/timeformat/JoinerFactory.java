package sh.okx.deathban.timeformat;

public class JoinerFactory {
  public static Joiner get(int type) {
    switch (type) {
      case 1:
      case 4:
        return Joiner.on(", ").and();
      case 2:
      case 5:
        return Joiner.on(", ");
      case 3:
      case 6:
        return Joiner.on(" ");
      case 7:
      default:
        return Joiner.on("");
    }
  }
}
