package edu.wisc.regfixer.diagnostic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Stat {
 
  public Set<String> names;
  private Map<String, Integer> size;
  private Map<String, Integer> holes;
  private Map<String, Long> time;
  
  public Stat() {
	this.names = new HashSet<>();
	this.size = new HashMap<>();
	this.holes = new HashMap<>();
	this.time = new HashMap<>();
  }
  
  public Stat(String name, int size, int holes, long time) {
	this.names  = new HashSet<>();
	this.names.add(name);
	this.size = new HashMap<>();
	this.size.put(name, size);
	this.holes = new HashMap<>();
	this.holes.put(name, holes);
	this.time = new HashMap<>();
	this.time.put(name, time);
  }
  
  public void add(String name, int size, int holes, long time) {
	this.names.add(name);
	this.size.put(name, size);
	this.holes.put(name, holes);
	this.time.put(name, time);
  }

  public int getSize(String name) {
	return this.size.get(name);
  }

  public int getHoles(String name) {
	return this.holes.get(name);
  }

  public long getTime(String name) {
	return this.time.get(name);
  }
}
