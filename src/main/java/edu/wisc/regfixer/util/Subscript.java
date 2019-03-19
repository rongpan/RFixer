package edu.wisc.regfixer.util;

import java.util.Arrays;

public class Subscript {
  public static String format (int notation) {
    return Integer.toString(notation).chars()
                  .mapToObj(i -> single((char) i))
                  .reduce("", String::concat);
  }

  private static String single (char digit) {
    switch (digit) {
      case '0': return "₀";
      case '1': return "₁";
      case '2': return "₂";
      case '3': return "₃";
      case '4': return "₄";
      case '5': return "₅";
      case '6': return "₆";
      case '7': return "₇";
      case '8': return "₈";
      case '9': return "₉";
      default:  return Character.toString(digit);
    }
  }
}
