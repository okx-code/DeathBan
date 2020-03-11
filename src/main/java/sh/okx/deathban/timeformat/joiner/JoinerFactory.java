package sh.okx.deathban.timeformat.joiner;

public class JoinerFactory {
  public static Joiner get(int type) {
    switch (type) {
      case 1:
      case 4:
        return new Joiner(", ", " and ");
      case 2:
      case 5:
        return new Joiner(", ");
      case 3:
      case 6:
        return new Joiner(" ");
      case 7:
      default:
        return new Joiner("");
    }
  }
}
