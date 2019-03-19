package edu.wisc.regfixer.util;

/**
 * Admittedly, this class is a little crazy. But it's done in the service of
 * allowing following kinds of method calls:
 *
 *   Ansi.Red.sprintf("foobar");
 *
 * The bizzare strings are special codes for ANSI terminals that tell the
 * terminal which color on the text that follows the code. The `ColorNormal`
 * code applies the terminal's default styles (effectively ending any colored
 * strings).
 *
 * The private constructor and the public, static, and final properties with the
 * color names make the name chaining possible. `Ansi.Blue.printf(...)` where
 * `Ansi` is a static reference to this class, `Blue` is an instantiated
 * reference this class where the `Blue` object has its private `color` property
 * set to the `ColorBlue` string.
 */

public class Ansi {
  // ANSI color codes from: https://gist.github.com/dainkaplan/4651352
  private final static String ColorNormal  = "\u001B[0m";

  private final static String ColorBlack   = "\u001B[30m";
  private final static String ColorRed     = "\u001B[31m";
  private final static String ColorGreen   = "\u001B[32m";
  private final static String ColorYellow  = "\u001B[33m";
  private final static String ColorBlue    = "\u001B[34m";
  private final static String ColorMagenta = "\u001B[35m";
  private final static String ColorCyan    = "\u001B[36m";
  private final static String ColorWhite   = "\u001B[37m";

  public final static Ansi Black   = new Ansi(Ansi.ColorBlack);
  public final static Ansi Red     = new Ansi(Ansi.ColorRed);
  public final static Ansi Green   = new Ansi(Ansi.ColorGreen);
  public final static Ansi Yellow  = new Ansi(Ansi.ColorYellow);
  public final static Ansi Blue    = new Ansi(Ansi.ColorBlue);
  public final static Ansi Magenta = new Ansi(Ansi.ColorMagenta);
  public final static Ansi Cyan    = new Ansi(Ansi.ColorCyan);
  public final static Ansi White   = new Ansi(Ansi.ColorWhite);

  private static boolean isColorEnabled = true;

  public static void disableColor () {
    isColorEnabled = false;
  }

  public static void enableColor () {
    isColorEnabled = true;
  }

  // Private constructor & `color` property for
  // creating a new instance for each color.
  private String color;
  private Ansi (String color) { this.color = color; }

  public String sprintf (String fmt, Object ...args) {
    String formatted = String.format(fmt, args);

    if (Ansi.isColorEnabled) {
      return this.color + formatted + Ansi.ColorNormal;
    } else {
      return formatted;
    }
  }
}
