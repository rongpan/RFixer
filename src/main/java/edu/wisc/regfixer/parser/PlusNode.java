package edu.wisc.regfixer.parser;

import com.microsoft.z3.BoolExpr;

public class PlusNode extends QuantifierNode {
	private int len = -1;
	private int upto = 100;
	private BoolExpr nullable;
	public BoolExpr[][] pairs;
	
  public PlusNode (RegexNode child) {
    super('+', child);
  }
  
  public void setNullable() {
	  this.getChild().setNullable();
      this.nullable = this.getChild().isNullable();
  }
  
  public BoolExpr isNullable() {
	  return this.nullable;
  }
  
  public void setLen() {
	  this.getChild().setLen();
	  this.len = this.getChild().getLen();
  }
  
  public int getLen() {
	  return this.len;
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
	  return this.getChild().getEpsilon();
  }
}
