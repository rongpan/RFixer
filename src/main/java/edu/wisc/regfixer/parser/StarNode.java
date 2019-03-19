package edu.wisc.regfixer.parser;

import com.microsoft.z3.BoolExpr;

import edu.wisc.regfixer.enumerate.UnknownBounds;

public class StarNode extends QuantifierNode {
	private int upto = 100;
	public BoolExpr[][] pairs;
  public StarNode (RegexNode child) {
    super('*', child);
  }
  
  public void setNullable() {
	 this.getChild().setNullable();
  }
  
  public BoolExpr isNullable() {
	  return Storage.ctx.mkBool(true);
  }
  
  public void setLen() {
	  this.getChild().setLen();
  }
  
  public int getLen() {
	  return 0;
  }
  
  public void calUpto(int upto) {
	  this.upto = upto;
      this.getChild().calUpto(upto);
  }
  
  public void setPairs() {
	  int length = this.upto;
	  this.getChild().setPairs();
      BoolExpr[][] childPairs = this.getChild().getPairs();
      
      PairLevel pl = new PairLevel(childPairs, this.isNullable());
      for (int i = 0; i < length - 1; i++) {
    	  pl.genNext();
      }
      
	  this.pairs =  pl.collect();
	  //return this.pairs;
  }
  
  public BoolExpr[][] getPairs() {
	  return this.pairs;
  }
  
  public void setEpsilon() {
	  this.getChild().setEpsilon();
  }
  
  public boolean getEpsilon() {
	  return true;
  }
}
