package edu.wisc.regfixer.automata;

import java.util.Map;
import java.util.Set;

import edu.wisc.regfixer.enumerate.UnknownId;

public class Route {
  private Map<UnknownId, Set<Character>> spans;
  private Map<UnknownId, Set<Integer>> exits;

  public Route (Map<UnknownId, Set<Character>> spans, Map<UnknownId, Set<Integer>> exits) {
    this.spans = spans;
    this.exits = exits;
  }

  public Map<UnknownId, Set<Character>> getSpans () {
    return this.spans;
  }

  public Map<UnknownId, Set<Integer>> getExits () {
    return this.exits;
  }

  public boolean hasNoSpans () {
    return this.spans.isEmpty();
  }

  public boolean hasNoExits () {
    return this.exits.isEmpty();
  }

  public boolean hasNoRealExits() {
	  for (Map.Entry<UnknownId, Set<Integer>> entry : this.exits.entrySet()) {
	      if (!entry.getValue().isEmpty())
	    	  return false;
	    }
	  return true;
  }
  
  @Override
  public boolean equals (Object other) {
    if (other instanceof Route) {
      boolean sameSpans = this.spans.equals(((Route) other).spans);
      boolean sameExits = this.exits.equals(((Route) other).exits);
      return sameSpans && sameExits;
    }

    return false;
  }

  @Override
  public int hashCode () {
    return this.spans.hashCode() * this.exits.hashCode();
  }

  /**
   * Route(exits: {H0: 5, H1: 2}, spans: {H0: [a, b, c], H1: [1, 2, 3]})
   * exits:
   *    H0 (5) H1 (2)
   * spans:
   *    H0 { a b c } H1 { 1 2 3 }
   */
  @Override
  public String toString () {
    String accum = "";

    if (this.exits.size() > 0) {
      accum += "exits:";
    }

    for (Map.Entry<UnknownId, Set<Integer>> entry : this.exits.entrySet()) {
      accum += String.format(" %s (%s)", entry.getKey(), entry.getValue());
    }

    if (this.spans.size() > 0) {
      accum += "spans:";
    }

    for (Map.Entry<UnknownId, Set<Character>> entry : this.spans.entrySet()) {
      accum += String.format(" %s {", entry.getKey());

      for (Character ch : entry.getValue()) {
        accum += String.format(" %c", ch);
      }
      accum += " }";
    }

    return accum;
  }
}
