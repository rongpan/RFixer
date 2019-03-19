package edu.wisc.regfixer.synthesize;

public class SimplePredicate implements Predicate {
  private char lower;
  private char upper;

  public SimplePredicate (char only) {
    this.lower = only;
    this.upper = only;
  }

  public SimplePredicate (char lower, char upper) {
    this.lower = lower;
    this.upper = upper;
  }

  public boolean equals (Predicate other) {
    if (other instanceof SimplePredicate) {
      SimplePredicate pred = (SimplePredicate)other;
      return (
        (this.lower == pred.lower) &&
        (this.upper == pred.upper));
    } else {
      return false;
    }
  }

  public boolean includes (Predicate other) {
    if (other instanceof SimplePredicate) {
      SimplePredicate pred = (SimplePredicate)other;
      return (this.lower <= pred.lower) && (this.upper >= pred.upper);
    } else if (other instanceof CompoundPredicate) {
      CompoundPredicate pred = (CompoundPredicate)other;
      for (Predicate comp : pred.getComponents()) {
        if (this.includes(comp) == false) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean includes (char charVal) {
    return (this.lower <= charVal) && (this.upper >= charVal);
  }

  public String toString () {
    String str = Character.toString(this.lower);

    if (this.lower != this.upper) {
      str += "-" + Character.toString(this.upper);
    }

    return str;
  }
}
