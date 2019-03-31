package edu.wisc.regfixer.parser;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import edu.wisc.regfixer.enumerate.Enumerant;
import edu.wisc.regfixer.enumerate.UnknownBounds;
import edu.wisc.regfixer.synthesize.SynthesisFailure;

public class RepetitionNode implements RegexNode {
  private RegexNode child;
  private Bounds bounds;
  public int lloc = 0;
  public int rloc = 0;
  private int len = -1;
  private int upto = 100;
  private BoolExpr nullable;
  public BoolExpr[][] pairs;
  private boolean epsilon;

  public RepetitionNode (RegexNode child, int min) {
    this.child = child;
    this.bounds = Bounds.atLeast(min);
  }

  public RepetitionNode (RegexNode child, int min, int max) {
    this.child = child;
    this.bounds = Bounds.between(min, max);
  }

  public RepetitionNode (RegexNode child, Bounds bounds) {
    this.child = child;
    this.bounds = bounds;
  }

  public RegexNode getChild () {
    return this.child;
  }

  public Bounds getBounds () {
    return this.bounds;
  }

  public int descendants () {
    return 1 + this.child.descendants();
  }

  public String toString () {
	  
	// handle emptySet test
	if (Enumerant.emptyTest && this.bounds instanceof UnknownBounds) {
		if (this.getChild().getEpsilon() == false)
			return String.format("(%s%s)%s", this.child, (char) 0x2202, this.bounds);
		else
			return String.format("(%s)%s", this.child, Bounds.exactly(0));
	}
    return String.format("(%s)%s", this.child, this.bounds);
  }
  
  public void toBinary() {
	  this.child.toBinary();
  }
  
  public int collectUnknown() {
	  int child = this.child.collectUnknown();
	  if (this.bounds instanceof UnknownBounds) {
		  Storage.unknownBoundCounter++;
		  this.lloc = Storage.unknownBoundCounter;
		  Storage.unknownBoundCounter++;
		  this.rloc = Storage.unknownBoundCounter;
		  Storage.idToLoc.put(((UnknownBounds)bounds).getId(), this.lloc);
		  return child + 2;
	  }
	  return child;
  }
  
  public void setNullable() {
	  Context ctx = Storage.ctx;
	  this.getChild().setNullable();
	  if (this.bounds instanceof UnknownBounds) {
		  this.nullable = ctx.mkOr(this.getChild().isNullable(),
				  ctx.mkLe(Storage.boundPreds[lloc], ctx.mkInt(0)));
		  Storage.idToNullable.put(((UnknownBounds)bounds).getId(), this.nullable);
	  } else {
		  this.nullable = ctx.mkOr(this.getChild().isNullable(), 
				  ctx.mkBool(this.bounds.getMin() <= 0));
	  }
  }
  
  public BoolExpr isNullable() {
	  return this.nullable;
  }
  
  public void setLen() {
	  this.getChild().setLen();
	  if (this.bounds instanceof UnknownBounds) {
		  this.len = 0;
	  } else {
		  this.len = this.getChild().getLen()*this.bounds.getMin();
	  }
  }
  
  public int getLen() {
	  return this.len;
  }
  
  public void calUpto(int upto) {
	  this.upto = upto;
	  this.getChild().calUpto(upto);
	  /*if (this.bounds instanceof UnknownBounds) {
	      this.getChild().calUpto(upto);
	  } else {
		  if (this.bounds.getMin() == 0)
			  this.getChild().calUpto(upto);
		  else
			  this.getChild().calUpto(Integer.min(upto/this.bounds.getMin() + 1, 
					  Storage.curExample.length));
	  }*/
	  /*System.err.println("cur exmaple is: " + String.valueOf(Storage.curExample));
      System.err.println("this is " + this);
      System.err.println("this len is " + this.len);
      System.err.println("upto is " + this.upto);*/
  }
  
  public void setPairs() {
	  
	  this.getChild().setPairs();
      BoolExpr[][] childPairs = this.getChild().getPairs();
      
      PairLevel pl = new PairLevel(childPairs, this.getChild().isNullable());
      
      if (this.bounds instanceof UnknownBounds) {
    	  for (int i = 0; i < this.upto - 1; i++) {
        	  pl.genNext();
          }
          this.pairs =  pl.collectWithBounds(Storage.boundPreds[lloc], Storage.boundPreds[rloc]);
      }
      else {
    	  if (bounds.hasMax()) {
    	      for (int i = 0; i < Integer.min(this.upto, bounds.getMax()) - 1; i++) {
        	      pl.genNext();
              }
    	      this.pairs =  pl.collectWithActualBounds(bounds.getMin(), bounds.getMax());
    	  } else {
    		  for (int i = 0; i < this.upto - 1; i++) {
        	      pl.genNext();
              }
    	      //this.pairs =  pl.collectWithActualBounds(bounds.getMin(), this.upto - 1);
    		  this.pairs =  pl.collectWithActualBounds(bounds.getMin(), this.upto);
    	  }
      }
      
      /*System.err.println("curExample is " + new String(Storage.curExample));
      System.err.println("this is " + this);
	  System.err.println("rep[0][0] is \n" + this.pairs[0][0]);*/
      
      /*if (!(this.bounds instanceof UnknownBounds))
          if (this.bounds.getMax() == 2)
              System.err.println("rep[0][1] is " + this.pairs[0][1]);*/
	  //return this.pairs;
	  
  }
  
  public BoolExpr[][] getPairs() {
	  return this.pairs;
  }
  
  public String finalString () {
	  //System.err.println("final called this is " + this);
	  //System.err.println("rep[0][1] is " + Storage.model.evaluate(this.pairs[0][1], false));
	  if (this.bounds instanceof UnknownBounds)
		  return String.format("(%s)%s", this.child.finalString(),
				  ((UnknownBounds)this.bounds).finalString(lloc, rloc));
	  return String.format("(%s)%s", this.child.finalString(), this.bounds);
  }
  
  public void setEpsilon() {
		child.setEpsilon();
		if (child.getEpsilon() == true) {
			this.epsilon = true;
		} else if (this.bounds instanceof UnknownBounds) {
			this.epsilon = false;
		} else {
			if (this.bounds.getMin() == 0)
				this.epsilon = true;
			else
				this.epsilon = false;
		}
	}

	public boolean getEpsilon() {
		return this.epsilon;
	}

	@Override
	public int LRUnknownCount() {
		int childCount = child.LRUnknownCount();
		if (childCount == -2)
			return -2;
		if (childCount == -1)
			return -1;
		if (bounds instanceof UnknownBounds)
			return childCount + 1;
		return -1;
	}
}
