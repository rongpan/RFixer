package edu.wisc.regfixer.util;

public class StringUtil {
  public static String repeatString (char ch, int times) {
    return repeatString(Character.toString(ch), times);
  }

  public static String repeatString (String str, int times) {
    String out = "";
    for (; times > 0; times--) {
      out += str;
    }
    return out;
  }

  public static String layerStrings (String a, String b) {
    // Given the strings:
    // "foo bar baz"
    // "            zip zap zoom"
    //
    // They are layered to become:
    // "foo bar baz zip zap zoom"
    //
    // Any collisions between non-whitespace characters reuslt in an error.

    String shortest = (a.length() < b.length()) ? a : b;
    String longest  = (a.length() < b.length()) ? b : a;

    if (longest.substring(0, shortest.length()).trim().length() != 0) {
      String fmt = "cannot layer strings \"%s\" and \"%s\"";
      throw new IllegalArgumentException(String.format(fmt, shortest, longest));
    }

    return shortest + longest.substring(shortest.length());
  }
}
