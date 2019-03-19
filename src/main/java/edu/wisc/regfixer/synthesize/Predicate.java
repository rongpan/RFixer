package edu.wisc.regfixer.synthesize;

public interface Predicate {
  public boolean equals (Predicate other);
  public boolean includes (Predicate other);
  public boolean includes (char other);
  public String toString ();
}
