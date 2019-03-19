package edu.wisc.regfixer.parser;

import com.microsoft.z3.BoolExpr;

public interface RegexNode {
  public String toString ();
  public int descendants ();  
  public void toBinary();
  public int collectUnknown();
  public void setNullable();
  public BoolExpr isNullable();
  public void setLen();
  public int getLen();
  public void calUpto(int upto);
  public void setPairs();
  public BoolExpr[][] getPairs();
  public String finalString();
  public void setEpsilon();
  public boolean getEpsilon();
  // return value:
  // > 0: # of unknowns
  // - 1: no violation because the presence of non-hole nodes
  // - 2: violation
  public int LRUnknownCount();
}
