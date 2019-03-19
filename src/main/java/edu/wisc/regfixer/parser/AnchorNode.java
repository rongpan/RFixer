package edu.wisc.regfixer.parser;

import java.util.Set;

import com.microsoft.z3.BoolExpr;

import edu.wisc.regfixer.synthesize.SynthesisFailure;

public class AnchorNode implements RegexNode {
  private RegexNode child;
  private boolean start;
  private boolean end;
  private BoolExpr nullable;
  private BoolExpr[][] pairs;

  public AnchorNode (RegexNode child, boolean start, boolean end) {
    this.child = child;
    this.start = start;
    this.end = end;
  }

  public int descendants () {
    return 1 + this.child.descendants();
  }

  public String toString () {
    return (
      ((this.start) ? "^" : "") +
      this.child.toString() +
      ((this.end) ? "$" : "")
    );
  }
  
  public void toBinary() {
	  child.toBinary();
  }
  
  public int collectUnknown() {
	  return child.collectUnknown();
  }
  
  public void setNullable() {
	  this.child.setNullable();
	  this.nullable = this.child.isNullable();
  }
  
  public BoolExpr isNullable() {
	  return this.nullable;
  }
  
  public void setLen() {
	  this.child.setLen();
  }
  
  public int getLen() {
	  return this.child.getLen();
  }
  
  public void calUpto(int upto) {
	this.child.calUpto(upto);  
  }
  
  public void setPairs() {
	  this.child.setPairs();
	  this.pairs = this.child.getPairs();
  }
  
  public BoolExpr[][] getPairs() {
	  return this.pairs;
  }
  
  public String finalString() {
	  return (
		      ((this.start) ? "^" : "") +
		      this.child.finalString() +
		      ((this.end) ? "$" : "")
		    );
  }
  
  public void setEpsilon() {
	  this.child.setEpsilon();
  }
  
  public boolean getEpsilon() {
	  return this.child.getEpsilon();
  }
  
  @Override
  public int LRUnknownCount() {
	  return this.child.LRUnknownCount();
  }
}
