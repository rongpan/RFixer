package edu.wisc.regfixer.diagnostic;

import java.util.HashMap;
import java.util.Map;

public class Timing {
  private final Map<String, Long> timings;
  private final Map<String, Long> pending;

  public Timing () {
    this.timings = new HashMap<>();
    this.pending = new HashMap<>();
  }

  public void startTiming (String name) {
    this.pending.put(name, System.nanoTime());
  }

  public long stopTimingAndAdd (String name) {
    if (this.timings.containsKey(name) == false) {
      this.timings.put(name, (long)0);
    }

    long duration = System.nanoTime() - this.pending.get(name);
    this.timings.put(name, this.timings.get(name) + duration);
    return duration;
  }

  public long getTiming (String name) {
    if (this.timings.containsKey(name)) {
      return this.timings.get(name);
    }

    return 0;
  }
  
  public static enum Format { Sec, MilliSec, NanoSec }

  public long getTiming (String name, Format format) {
    long time = this.getTiming(name);

    switch (format) {
      case Sec:
        return Math.round(time / 1e9);
      case MilliSec:
        return Math.round(time / 1e6);
      default:
        return time;
    }
  }
}
