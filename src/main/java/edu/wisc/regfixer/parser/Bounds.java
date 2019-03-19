package edu.wisc.regfixer.parser;

public class Bounds {
  public static final int MAX_BOUND = 1000;

  protected int min;
  protected Integer max;

  public static Bounds exactly (int n) {
    return new Bounds(n, n);
  }

  public static Bounds atLeast (int min) {
    return new Bounds(min, null);
  }

  public static Bounds between (int min, int max) {
    return new Bounds(min, max);
  }

  public Bounds (Bounds other) {
    this.min = other.min;
    this.max = other.max;
  }

  protected Bounds (int min, Integer max) {
    if (min < 0) {
      throw new IllegalArgumentException("illegal min < 0");
    }

    if (max != null && max < 0) {
      throw new IllegalArgumentException("illegal max < 0");
    }

    this.min = min;
    this.max = max;
  }

  public int getMin () {
    return this.min;
  }

  public boolean hasMax () {
    return (this.max != null);
  }

  public int getMax () {
    return this.max;
  }

  public String toString () {
    if (this.max == null) {
      return String.format("{%d,}", this.min);
    } else if (this.min == this.max) {
      return String.format("{%d}", this.min);
    } else if (this.max >= Bounds.MAX_BOUND && this.min == 0) {
      return "*";
    } else if (this.max >= Bounds.MAX_BOUND && this.min == 1) {
      return "+";
    } else if (this.max >= Bounds.MAX_BOUND) {
      return String.format("{%d,}", this.min);
    } else {
      return String.format("{%d,%d}", this.min, this.max);
    }
  }
}
