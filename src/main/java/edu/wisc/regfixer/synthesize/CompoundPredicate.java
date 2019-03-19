package edu.wisc.regfixer.synthesize;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CompoundPredicate implements Predicate {
  private Set<Predicate> components;
  private boolean inclusive;

  public CompoundPredicate (Predicate... components) {
    this.components = new HashSet<>(Arrays.asList(components));
    this.inclusive = true;
  }

  public CompoundPredicate (boolean inclusive, Predicate... components) {
    this.components = new HashSet<>(Arrays.asList(components));
    this.inclusive = inclusive;
  }

  public Set<Predicate> getComponents () {
    return this.components;
  }

  public boolean equals (Predicate other) {
    if (other instanceof SimplePredicate) {
      if (this.components.size() == 1) {
        return this.components.iterator().next().equals(other);
      } else {
        return false;
      }
    } else if (other instanceof CompoundPredicate) {
      return this.toString().equals(other.toString());
    } else {
      return false;
    }
  }

  public boolean includes (Predicate other) {
    for (Predicate component : this.components) {
      if (component.includes(other)) {
        return this.inclusive;
      }
    }

    return !this.inclusive;
  }

  public boolean includes (char other) {
    for (Predicate component : this.components) {
      if (component.includes(other)) {
        return this.inclusive;
      }
    }

    return !this.inclusive;
  }

  public String toString () {
    String out = "[";
    if (this.inclusive == false) {
      out += "^";
    }
    for (Predicate component : this.components) {
      out += component.toString();
    }
    return out + "]";
  }
}
