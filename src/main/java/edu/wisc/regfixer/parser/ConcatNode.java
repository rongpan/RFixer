package edu.wisc.regfixer.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import edu.wisc.regfixer.synthesize.SynthesisFailure;

public class ConcatNode implements RegexNode {
  private List<RegexNode> children;
  private int len = -1;
  private int upto = 100;
  public BoolExpr[][] pairs;
  private BoolExpr nullable;
  private boolean epsilon;

  public ConcatNode (List<RegexNode> children) {
    this.children = children;
  }

  public ConcatNode (RegexNode... children) {
    this.children = Arrays.asList(children);
  }

  public List<RegexNode> getChildren () {
    return this.children;
  }

  public int descendants () {
    return this.children.stream().mapToInt(RegexNode::descendants).sum();
  }

  public String toString () {
    String out = "";

    for (RegexNode child : this.children) {
      if (child instanceof ConcatNode) {
        out += "(" + child.toString() + ")";
      } else {
        out += child.toString();
      }
    }

    return out;
  }
  
  public void toBinary() {
	  int size = this.getChildren().size();
	  if (size <= 2) {
		  if (size == 1)
			  this.getChildren().get(0).toBinary();
		  if (size == 2) {
			  this.getChildren().get(0).toBinary();
			  this.getChildren().get(1).toBinary();
		  }
		  return;
	  }
	  RegexNode last = this.getChildren().remove(size - 1);
	  last.toBinary();
	  for (RegexNode node : this.getChildren()) {
		  node.toBinary();
	  }
	  ConcatNode newChild = new ConcatNode(this.getChildren());
	  newChild.toBinary();
	  List<RegexNode> list = new ArrayList<>();
	  list.add(newChild);
	  list.add(last);
	  this.children = list;
	  //System.err.println("this is \n" + this);
	  //System.err.println("number of children is " + this.children.size());
  }
  
  public int collectUnknown() {
	  int sum = 0;
	  for (RegexNode child : this.children) {
		  sum += child.collectUnknown();
	  }
	  return sum;
  }
  
  public void setNullable() {
	  Context ctx = Storage.ctx;
	  //this.nullable = ctx.mkBoolConst(Integer.toString(Storage.nameIndex++));
	  
	  if (this.children.size() == 1) {
		  this.children.get(0).setNullable();
		  //ctx.mkImplies(this.children.get(0).isNullable(), this.nullable);
		  //ctx.mkImplies(this.nullable, this.children.get(0).isNullable());
		  this.nullable = this.children.get(0).isNullable();
	  }
	  
	  if (this.children.size() == 2) {
		  this.children.get(0).setNullable();
		  this.children.get(1).setNullable();
		  /*System.err.println("0 nullable: " + this.children.get(0).isNullable());
		  System.err.println("1 nullable: " + this.children.get(1).isNullable());
		  System.err.println("this : " + this);
		  System.err.println("this nullable: " + this.nullable);
		  this.nullable = ctx.mkBoolConst(this.toString() + "_nullable");
		  ctx.mkImplies(ctx.mkAnd(this.children.get(0).isNullable(),
				  this.children.get(1).isNullable()), this.nullable);
		  ctx.mkImplies(this.nullable, ctx.mkAnd(this.children.get(0).isNullable(), 
				  this.children.get(1).isNullable()));*/
		  this.nullable = ctx.mkAnd(this.children.get(0).isNullable(), 
				  this.children.get(1).isNullable());
	  }
	 
  }
  
  public BoolExpr isNullable() {
	  return this.nullable;
  }
  
  public void setLen() {
	  if (this.children.size() == 1) {
		  this.children.get(0).setLen();
		  this.len = this.children.get(0).getLen();
		  return;
	  }
	  if (this.children.size() == 2) {
		  this.children.get(0).setLen();
		  this.children.get(1).setLen();
	      this.len = this.children.get(0).getLen() + this.children.get(1).getLen();
	  }
  }
  
  public int getLen() {
	  return this.len;
  }
  
  public void calUpto(int upto) {
	  this.upto = upto;
	  if (this.children.size() == 1) {
		  this.children.get(0).calUpto(upto);
	  }
	  if (this.children.size() == 2) {
		  this.children.get(0).calUpto(upto - this.children.get(1).getLen());
		  this.children.get(1).calUpto(upto - this.children.get(0).getLen());
	  }
	  /*System.err.println("cur example is " + String.valueOf(Storage.curExample));
	  System.err.println("this is " + this);
	  System.err.println("this len is " + this.len);
	  System.err.println("this children size is " + this.getChildren().size());
	  System.err.println("upto is " + upto);*/
  }
  
  public void setPairs() {
	  if (this.children.size() == 0 || this.children.size() > 2) {
		  System.err.println("toBinary not correct!! \n" + this);
		  System.err.println("number of children is " + this.children.size());
	  }
	  int length = Storage.curExample.length;
	  this.pairs = new BoolExpr[length][length];
	  for (int i = 0; i < length; i++) {
  		  for (int j = i; j < length; j++) {
  			  this.pairs[i][j] = Storage.ctx.mkBool(false);
  		  }
  	  }
	  
	  for (RegexNode node : this.children) {
		  node.setPairs();
	  }
	  
	  if (this.children.size() == 1) {
		  this.pairs = this.children.get(0).getPairs();
		  return;
	  }
		  
	  Context ctx = Storage.ctx;
	 
	  for (int i = 0; i < length; i++) {
		  this.pairs[i][i] = ctx.mkOr(ctx.mkAnd(this.children.get(0).getPairs()[i][i], 
				  this.children.get(1).isNullable()),
				  ctx.mkAnd(this.children.get(1).getPairs()[i][i], 
						  this.children.get(0).isNullable()));
	  }
	  for (int i = 0; i < length - 1; i++) {
		  for (int j = i + 1; j < Integer.min(length, i + upto); j++) {
			  this.pairs[i][j] = ctx.mkAnd(this.children.get(0).isNullable(), 
					  this.children.get(1).getPairs()[i][j]);
			  for (int k = i; k < j; k++) {
				  this.pairs[i][j] = ctx.mkOr(this.pairs[i][j], 
						  ctx.mkAnd(this.children.get(0).getPairs()[i][k],
								  this.children.get(1).getPairs()[k+1][j]));
			  }
			  this.pairs[i][j] = ctx.mkOr(this.pairs[i][j], ctx.mkAnd(this.children.get(1).isNullable(), 
					  this.children.get(0).getPairs()[i][j]));
	 	  }
	  }
	  
	  /*System.err.println("current exmaple: " + new String(Storage.curExample));
	  System.err.println("this is " + this);
	  System.err.println("concat[0][length - 1] is " + this.pairs[0][length - 1]);
	  */
	  //return this.pairs;
	  
  }
  
  public BoolExpr[][] getPairs() { 
	  return this.pairs;
  }
  
  public String finalString () {
	  //System.err.println("final called this is " + this);
	    String out = "";

	    for (RegexNode child : this.children) {
	      if (child instanceof ConcatNode) {
	        out += "(" + child.finalString() + ")";
	      } else {
	        out += child.finalString();
	      }
	    }

	    return out;
	  }
  
	public void setEpsilon() {
		boolean res = true;
		for (RegexNode child : this.children) {
			child.setEpsilon();
			res = res && child.getEpsilon();
		}
		this.epsilon = res;
	}
	
	public boolean getEpsilon() {
		return this.epsilon;
	}
	
	@Override
	public int LRUnknownCount() {
		int size = this.children.size();
		if (size == 0) {
			return 0;
		} else if (size == 1) {
			return this.children.get(0).LRUnknownCount();
		} else if (size == 2) {
			int leftUnknown = this.children.get(0).LRUnknownCount();
			int rightUnknown = this.children.get(1).LRUnknownCount();
			if (leftUnknown == -2 || rightUnknown == -2)
				return -2;
			if (leftUnknown == -1 || rightUnknown == -1)
				return -1;
			if (leftUnknown < rightUnknown)
				return -2;
			return leftUnknown + rightUnknown;
		} else {
			//throw new SynthesisFailure("ConcatNode not in binary in concatUnknownCount()");
			return -2;
		}
	}
}
