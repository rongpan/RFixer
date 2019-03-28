package edu.wisc.regfixer.parser;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.microsoft.z3.BoolExpr;

public class CharDotNode implements CharClass {
	public BoolExpr[][] pairs;
	
  public int descendants () {
    return 1;
  }

  @Override
  public int hashCode () {
    return Objects.hash(".");
  }

  @Override
  public boolean equals (Object obj) {
    return (obj instanceof CharDotNode);
  }

  public String toString () {
    return ".";
  }
  
  public void toBinary(){
	  
  }
  
  public int collectUnknown(){
	  return 0;
  }
  
  public void setNullable() {
		
  }
  
  public BoolExpr isNullable() {
	  return Storage.ctx.mkBool(false);
  }
  
  public void setLen() {
	  
  }
  
  public int getLen() {
	  return 1;
  }
  
  public void calUpto(int upto) {
	  
  }
  
  public void setPairs(){
	  int length = Storage.curExample.length;
	  this.pairs = new BoolExpr[length][length];
	  for (int i = 0; i < length; i++) {
		  for (int j = i; j < length; j++) {
			  this.pairs[i][j] = Storage.ctx.mkBool(false);
		  }
		  this.pairs[i][i] = Storage.ctx.mkBool(true);
	  }
	  //return this.pairs;
  }
  
  public BoolExpr[][] getPairs(){
	  return this.pairs;
  }
  
  public String finalString () {
	  return ".";
  }
  
	public void setEpsilon() {
		
	}
	
	public boolean getEpsilon() {
		return false;
	}
	
	@Override
	public int LRUnknownCount() {
		return -1;
	}
}
