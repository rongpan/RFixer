package edu.wisc.regfixer.automata;

import edu.wisc.regfixer.enumerate.UnknownId;
import theory.characters.CharPred;
import theory.characters.StdCharPred;

public class UnknownPred extends CharPred {
  protected UnknownId id = null;

  public UnknownPred (UnknownId id) {
    super(StdCharPred.TRUE.intervals);
    this.id = id;
  }

  public UnknownId getId () {
    return this.id;
  }
}
