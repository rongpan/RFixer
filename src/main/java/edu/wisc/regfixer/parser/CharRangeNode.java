package edu.wisc.regfixer.parser;

import java.util.Objects;

import com.microsoft.z3.BoolExpr;

public class CharRangeNode implements CharClass {
  private ConcreteCharClass left;
  private ConcreteCharClass right;
  private BoolExpr nullable;
  public BoolExpr[][] pairs;

  public CharRangeNode (ConcreteCharClass left) {
    this.left = left;
    this.right = left;
  }

  public CharRangeNode (char left) {
    this.left = new CharLiteralNode(left);
    this.right = this.left;
  }

  public CharRangeNode (char left, char right) {
    this.left = new CharLiteralNode(left);
    this.right = new CharLiteralNode(right);
  }

  public CharRangeNode (ConcreteCharClass left, ConcreteCharClass right) {
    this.left = left;
    this.right = right;
  }

  public ConcreteCharClass getLeftChild () {
    return this.left;
  }

  public boolean isSingle () {
    return (this.left == this.right);
  }

  public ConcreteCharClass getRightChild () {
    return this.right;
  }

  public int descendants () {
    return 1;
  }

  @Override
  public int hashCode () {
    return Objects.hash(this.toString());
  }

  @Override
  public boolean equals (Object obj) {
    if (obj instanceof CharRangeNode) {
      CharRangeNode cast = (CharRangeNode) obj;
      boolean sameLeft = (this.left == cast.getLeftChild());
      boolean sameRight = (this.right == cast.getRightChild());
      return (sameLeft && sameRight);
    }

    return false;
  }

  public String toString () {
    if (this.left == this.right) {
      return this.left.toString();
    } else {
      return String.format("%s-%s", this.left, this.right);
    }
  }
  
  public void toBinary(){
	  
  }
  
  public int collectUnknown(){
	  return 0;
  }
  
  public void setNullable() {
	  this.nullable = Storage.ctx.mkBool(false);
  }
  
  public BoolExpr isNullable() {
	  return this.nullable;
  }
  
  public void setLen() {
	  
  }
  
  public int getLen() {
	  return 1;
  }
  
  public void calUpto(int upto) {
	  
  }
  
  public void setPairs() {
	  int length = Storage.curExample.length;
	  this.pairs = new BoolExpr[length][length];
	  for (int i = 0; i < length; i++) {
		  for (int j = i; j < length; j++) {
			  this.pairs[i][j] = Storage.ctx.mkBool(false);
		  }
		  if (this.left.getChar() <= Storage.curExample[i] && this.right.getChar() >= Storage.curExample[i]) {
			  this.pairs[i][i] = Storage.ctx.mkBool(true);
		  }
	  }
	  //return this.pairs;
  }
  
  public BoolExpr[][] getPairs() {
	  return this.pairs;
  }
  
  public String finalString () {
	    if (this.left == this.right) {
	      return this.left.toString();
	    } else {
	      return String.format("%s-%s", this.left, this.right);
	    }
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
