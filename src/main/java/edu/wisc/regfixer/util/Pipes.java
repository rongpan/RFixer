package edu.wisc.regfixer.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Pipes {
  private static List<String> mapPositions (int[] start, int[] end, boolean ltr) {
    List<String> lines = new LinkedList<>();
    int total = start.length;

    // Index of the path that will be aligned during each loop iteration.
    for (int toAlign = (ltr ? total - 1 : 0); (ltr ? toAlign >= 0 : toAlign < total); toAlign += (ltr ? -1 : 1)) {
      String line = "";

      for (int i = 0; i < toAlign; i++) {
        int offset = (ltr ? start[i] : end[i]) - line.length();
        line += StringUtil.repeatString(" ", offset) + BoxChars.vert;
      }

      // Compute the shift needed to align a single path.
      int shift = end[toAlign] - start[toAlign];

      // Handle special cases where the shift is 0. In cases where the shift is
      // 0, skip the entire line (since it will be redundant) but DO NOT skip
      // the line if skipping would cause this method to return an empty list.
      if (shift != 0) {
        if (shift > 0) {
          int offset = start[toAlign] - line.length();
          line += StringUtil.repeatString(" ", offset);
          line += BoxChars.corner_sw + StringUtil.repeatString(BoxChars.horiz, shift - 1) + BoxChars.corner_ne;
        } else {
          int offset = end[toAlign] - line.length();
          line += StringUtil.repeatString(" ", offset);
          line += BoxChars.corner_nw + StringUtil.repeatString(BoxChars.horiz, -shift - 1) + BoxChars.corner_se;
        }
      } else if (lines.size() == 0 || toAlign == 0) {
        int offset = start[toAlign] - line.length();
        line += StringUtil.repeatString(" ", offset);
        line += BoxChars.vert;
      } else {
        continue;
      }

      for (int i = toAlign + 1; i < total; i++) {
        int offset = (ltr ? end[i] : start[i]) - line.length();
        line += StringUtil.repeatString(" ", offset) + BoxChars.vert;
      }

      lines.add(line);
    }

    return lines;
  }

  private static void areLegalInputs (int[] start, int[] end) {
    // Do input validation to ensure that the `start` and `end` arrays have the
    // same lengths and are both in ascending order where each array has no
    // negative values and no duplicate values.
    if (start.length != end.length) {
      throw new IllegalArgumentException("arrays must have same length");
    } else {
      for (int i = 0; i < start.length; i++) {
        if (i == 0) {
          if (start[0] < 0 || end[0] < 0) {
            throw new IllegalArgumentException("array values must be positive");
          }
        } else if (start[i] <= start[i - 1] || end[i] <= end[i - 1]) {
          throw new IllegalArgumentException("array values must be in ascending order");
        }
      }
    }
  }

  private static List<String> mapBucket (List<Pair> bucket) {
    int[] start = new int[bucket.size()];
    int[] end   = new int[bucket.size()];

    for (int i = 0; i < bucket.size(); i++) {
      Pair pair = bucket.get(i);
      start[i] = pair.start;
      end[i]   = pair.end;
    }

    return mapPositions(start, end, bucket.get(0).isLTR());
  }

  private static List<String> mapBuckets (List<List<Pair>> buckets) {
    List<String> lines = new LinkedList<>();
    boolean extendUpward = true;

    for (List<Pair> bucket : buckets) {
      List<String> bucketLines = mapBucket(bucket);

      if (lines.size() == 0) {
        lines = bucketLines;
        continue;
      }

      int linesHeight = lines.size();
      int bucketHeight = bucketLines.size();

      if (linesHeight < bucketHeight) {
        String toExtend = lines.get(0);
        int howMany = bucketHeight - linesHeight;
        lines.addAll(0, extendLine(toExtend, extendUpward, howMany));
      } else if (bucketHeight < linesHeight) {
        String toExtend = bucketLines.get(0);
        int howMany = linesHeight - bucketHeight;
        bucketLines.addAll(0, extendLine(toExtend, extendUpward, howMany));
      }

      for (int i = 0; i < lines.size(); i++) {
        lines.set(i, StringUtil.layerStrings(lines.get(i), bucketLines.get(i)));
      }
    }

    return lines;
  }

  private static List<String> extendLine (String toExtend, boolean upward, int howMany) {
    String extension = "";

    for (int i = 0; i < toExtend.length(); i++) {
      char ch = toExtend.charAt(i);

      if (ch == BoxChars.vert) {
        extension += BoxChars.vert;
      } else if (upward == true && (ch == BoxChars.corner_sw || ch == BoxChars.corner_se)) {
        extension += BoxChars.vert;
      } else if (upward == false && (ch == BoxChars.corner_nw || ch == BoxChars.corner_ne)) {
        extension += BoxChars.vert;
      } else {
        extension += " ";
      }
    }

    List<String> lines = new LinkedList<>();

    for (; howMany > 0; howMany--) {
      lines.add(extension);
    }

    return lines;
  }

  private static List<List<Pair>> sortIntoBuckets (List<Pair> pairs) {
    List<List<Pair>> buckets = new LinkedList<>();
    Collections.sort(pairs, new PairLeftEdgeComparator());

    Pair prev = null;
    List<Pair> currBucket = null;
    for (Pair pair : pairs) {
      if (prev == null) {
        currBucket = new LinkedList<>();
        buckets.add(currBucket);
        currBucket.add(pair);
        prev = pair;
        continue;
      }

      if (prev.overlaps(pair)) {
        if (prev.isLTR() == pair.isLTR()) {
          currBucket.add(pair);
        } else {
          String fmt = "could not fit %s and %s into same bucket";
          throw new IllegalArgumentException(String.format(fmt, prev, pair));
        }
      } else {
        currBucket = new LinkedList<>();
        buckets.add(currBucket);
        currBucket.add(pair);
      }

      prev = pair;
    }

    return buckets;
  }

  private static class Pair {
    int start;
    int end;
    int left;
    int right;

    public Pair (int start, int end) {
      this.start = start;
      this.end = end;
      this.left = (start < end) ? start : end;
      this.right = (start > end) ? start : end;
    }

    public boolean overlaps (Pair other) {
      return ((this.right >= other.left) && (this.left <= other.left)) ||
             ((this.left <= other.right) && (this.right >= other.right));
    }

    public boolean isLTR () {
      return (this.start <= this.end);
    }

    public boolean isRTL () {
      return (this.start >= this.end);
    }

    public boolean isVertical () {
      return (this.start == this.end);
    }

    public String toString () {
      if (this.isRTL()) {
        return String.format("%d <- %d", this.left, this.right);
      }

      return String.format("%d -> %d", this.left, this.right);
    }
  }

  private static class PairAlignedComparator implements Comparator<Pair> {
    @Override
    public int compare (Pair a, Pair b) {
      if (a.isLTR() && b.isLTR()) {
        return b.left - a.left;
      } else if (a.isRTL() && b.isRTL()) {
        return a.left - b.left;
      }

      String fmt = "cannot compare pairs %s and %s";
      throw new IllegalArgumentException(String.format(fmt, a, b));
    }
  }

  private static class PairLeftEdgeComparator implements Comparator<Pair> {
    @Override
    public int compare (Pair a, Pair b) {
      return a.left - b.left;
    }
  }

  public static void draw (int[] start, int[] end) {
    areLegalInputs(start, end);

    List<Pair> pairs = new LinkedList<>();
    for (int i = 0; i < start.length; i++) {
      pairs.add(new Pair(start[i], end[i]));
    }

    for (String line : mapBuckets(sortIntoBuckets(pairs))) {
      System.out.println(line);
    }
  }
}
