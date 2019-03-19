package edu.wisc.regfixer.util;

import java.util.LinkedList;
import java.util.List;

public class Braces {
  private static String drawBraces (int[] offsets, int[] joints, int[] widths, boolean facingDown) {
    String line = "";

    for (int i = 0; i < offsets.length; i++) {
      line += StringUtil.repeatString(" ", offsets[i] - line.length());
      line += facingDown ? BoxChars.corner_nw : BoxChars.corner_sw;
      line += StringUtil.repeatString(BoxChars.horiz, joints[i] - offsets[i] - 1);
      line += facingDown ? BoxChars.tee_n : BoxChars.tee_s;
      line += StringUtil.repeatString(BoxChars.horiz, (offsets[i] + widths[i]) - line.length());
      line += facingDown ? BoxChars.corner_ne : BoxChars.corner_se;
    }

    return line;
  }

  private static void areLegalInputs (int[] offsets, int[] joints, int[] widths) {
    if (offsets.length != joints.length || offsets.length != widths.length) {
      throw new IllegalArgumentException("arrays must have the same lengths");
    } else {
      for (int i = 0; i < offsets.length; i++) {
        if (offsets[i] < 0) {
          throw new IllegalArgumentException("offsets must be positive");
        } else if (i > 0 && offsets[i] <= offsets[i-1]) {
          throw new IllegalArgumentException("offsets must be in ascending order");
        }

        if (widths[i] <= 1) {
          throw new IllegalArgumentException("width cannot be less than 2");
        }

        if (joints[i] <= offsets[i]) {
          throw new IllegalArgumentException("joint must be to the right of offset");
        }

        if (i > 0 && offsets[i - 1] + widths[i - 1] >= offsets[i]) {
          throw new IllegalArgumentException("braces cannot overlap");
        }
      }
    }
  }

  public static void draw (int[] offsets, int[] joints, int[] widths) {
    draw(offsets, joints, widths, true);
  }

  public static void draw (int[] offsets, int[] joints, int[] widths, boolean facingDown) {
    areLegalInputs(offsets, joints, widths);
    System.out.println(drawBraces(offsets, joints, widths, facingDown));
  }
}
