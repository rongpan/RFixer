package edu.wisc.regfixer.diagnostic;

import java.util.HashMap;
import java.util.Map;

public class Registry {
  private Map<String, Boolean> bools;
  private Map<String, Integer> ints;
  private Map<String, String> strs;

  public Registry () {
    this.bools = new HashMap<String, Boolean>();
    this.ints  = new HashMap<String, Integer>();
    this.strs  = new HashMap<String, String>();
  }

  public void setBool (String name, boolean val) {
    this.bools.put(name, val);
  }

  public int setInt (String name, int val) {
    this.ints.put(name, val);
    return val;
  }

  public void setStr (String name, String val) {
    this.strs.put(name, val);
  }

  public boolean getBool (String name) {
    return this.getBool(name, false);
  }

  public boolean getBool (String name, boolean fallback) {
    Boolean got = this.bools.get(name);

    if (got == null) {
      return fallback;
    } else {
      return got;
    }
  }

  public int getInt (String name) {
    return this.getInt(name, 0);
  }

  public int getInt (String name, int fallback) {
    Integer got = this.ints.get(name);

    if (got == null) {
      return fallback;
    } else {
      return got;
    }
  }

  public int bumpInt (String name) {
    return this.setInt(name, this.getInt(name) + 1);
  }

  public String getStr (String name) {
    return this.getStr(name, "");
  }

  public String getStr (String name, String fallback) {
    String got = this.strs.get(name);

    if (got == null) {
      return fallback;
    } else {
      return got;
    }
  }
}
