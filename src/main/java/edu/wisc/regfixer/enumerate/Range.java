package edu.wisc.regfixer.enumerate;

import com.google.gson.annotations.SerializedName;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Range implements Comparable<Range> {
  @SerializedName("left") private int leftIndex;
  @SerializedName("right") private int rightIndex;

  public Range (int leftIndex, int rightIndex) {
    this.leftIndex = leftIndex;
    this.rightIndex = rightIndex;
  }

  public Range (String st) throws BadRangeException {
    Pattern pt = Pattern.compile("\\((\\d+):(\\d+)\\)");
    Matcher mt = pt.matcher(st);

    if (mt.find()) {
      this.leftIndex = Integer.parseInt(mt.group(1));
      this.rightIndex = Integer.parseInt(mt.group(2));
    } else {
      String fmt = "cannot parse '%s', expect form '(1:2)'";
      throw new BadRangeException(String.format(fmt, st));
    }
  }

  public int getLeftIndex () {
    return this.leftIndex;
  }

  public int getRightIndex () {
    return this.rightIndex;
  }

  public int length () {
    return this.rightIndex - this.leftIndex;
  }

  @Override
  public boolean equals (Object other) {
    if (other instanceof Range) {
      Range otherRange = (Range) other;
      boolean leftIsEqual = (this.leftIndex == otherRange.getLeftIndex());
      boolean rightIsEqual = (this.rightIndex == otherRange.getRightIndex());

      return leftIsEqual && rightIsEqual;
    }

    return false;
  }

  @Override
  public int hashCode () {
    return this.leftIndex + this.rightIndex;
  }

  public boolean startsBefore (Range other) {
    return (this.leftIndex <= other.getLeftIndex());
  }

  public boolean startsAfter (Range other) {
    return (this.leftIndex >= other.getRightIndex());
  }

  public boolean endsBefore (Range other) {
    return (this.rightIndex <= other.getLeftIndex());
  }

  public boolean endsAfter (Range other) {
    return (this.rightIndex >= other.getRightIndex());
  }

  public boolean intersects (Range other) {
    boolean intersectionAtStart = (this.leftIndex < other.getRightIndex());
    boolean intersectionAtEnd = (this.rightIndex < other.getLeftIndex());

    return intersectionAtStart || intersectionAtEnd;
  }

  @Override
  public int compareTo (Range other) {
    if (this.leftIndex != other.leftIndex) {
      return Integer.compare(this.leftIndex, other.leftIndex);
    } else {
      return Integer.compare(this.rightIndex, other.rightIndex);
    }
  }

  public String toString () {
    return String.format("(%d:%d)", this.leftIndex, this.rightIndex);
  }
}
