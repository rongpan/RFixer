package edu.wisc.regfixer.parser;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.microsoft.z3.BoolExpr;

import edu.wisc.regfixer.global.Global;

public class CharLiteralNode implements ConcreteCharClass {
  private char ch;
  public int location = 0;
  public BoolExpr[][] pairs;

  public CharLiteralNode (char ch) {
    this.ch = ch;
  }

  public char getChar () {
    return this.ch;
  }

  public int descendants () {
    return 1;
  }

  @Override
  public int hashCode () {
    return Objects.hash(this.ch);
  }

  @Override
  public boolean equals (Object obj) {
    if (obj instanceof CharLiteralNode) {
      return (this.ch == ((CharLiteralNode) obj).getChar());
    }

    return false;
  }

  public String toString () {
    if (this.ch == '-') {
      return "\\-";
    } else if (this.ch == '$') {
      return "\\$";
    } else if (this.ch == '.') {
      return "\\.";
    } 

    return String.format("%c", this.ch);
  }
  
  public void toBinary(){
	  
  }
  
  public int collectUnknown() {
	  if (this.ch == '■') {
		  Storage.unknownCharCounter++;
		  this.location = Storage.unknownCharCounter;
		  return 1;
	  }
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
  
  public void setPairs() {
	  int length = Storage.curExample.length;
	  this.pairs = new BoolExpr[length][length];
	  if (this.ch != '■') {
		  
		  for (int i = 0; i < length; i++) {
			  for (int j = i; j < length; j++) {
				  this.pairs[i][j] = Storage.ctx.mkBool(false);
			  }
			  if (this.ch == Storage.curExample[i]) {
				  this.pairs[i][i] = Storage.ctx.mkBool(true);
			  }
		  }
	  } else {
		  for (int i = 0; i < length; i++) {
			  for (int j = i; j < length; j++) {
				  this.pairs[i][j] = Storage.ctx.mkBool(false);
			  }
			  
			  //System.err.println("allchars: " + Storage.allChars.toString());
			  //System.err.println("currChar: " + Storage.curExample[i]);
			  for (int k = 0; k < Storage.allChars.length; k++) {
				  if (Storage.curExample[i] == Storage.allChars[k]) {
					  /*this.pairs[i][i] = Storage.ctx.mkBoolConst(Integer.toString(Storage.nameIndex++));
					  Storage.ctx.mkImplies(Storage.charPreds[location][k], this.pairs[i][i]);
					  Storage.ctx.mkImplies(this.pairs[i][i], Storage.charPreds[location][k]);*/
					  this.pairs[i][i] = Storage.charPreds[location][k];
				  }
			  }
		  }
		  //System.err.println("literal[1][1] is " + this.pairs[1][1]);
	  }
	  //return this.pairs;
  }
  
  public BoolExpr[][] getPairs() {
	  return this.pairs;
  }
  
  public String finalString () {
	    if (this.ch == '-') {
	      return "\\-";
	    } else if (this.ch == '$') {
	      return "\\$";
	    } else if (this.ch == '.') {
	      return "\\.";
	    } else if (this.ch == '■') {
	    	StringBuilder sb = new StringBuilder();
	    	int num_d = 0;
	    	int num_az = 0;
	    	int num_AZ = 0;
	    	boolean neg_d = false;
	    	boolean neg_az = false;
	    	boolean neg_AZ = false;
	    	for (int cNum = 0; cNum < Storage.allChars.length; cNum++) {
	    		char c = Storage.allChars[cNum];
	    	    if (Storage.model.evaluate(Storage.charPreds[location][cNum], false).toString()
	    	    		.equals("true")) {
	    	    	sb.append(c);
	    	    	if (Global.findMaxSat) {
	    		    	if (c >= '0' && c <= '9') {
	    				    num_d++;
	    		    	} else if (c >= 'a' && c <= 'z') {
	    		    	    num_az++;
	    		    	} else if (c >= 'A' && c <= 'Z') {
	    		    	    num_AZ++;
	    		    	}
	    		    		
	    	    	}
	    	    }
	    	    if (Global.findMaxSat) {
	    	        if (Storage.model.evaluate(Storage.charPreds[location][cNum], false).toString()
	    	    		.equals("false")) {
	    	        	if (c >= '0' && c <= '9') {
	    				    neg_d = true;
	    		    	} else if (c >= 'a' && c <= 'z') {
	    		    	    neg_az = true;
	    		    	} else if (c >= 'A' && c <= 'Z') {
	    		    	    neg_AZ = true;
	    		    	}
	    	        }
	    	    }
	    	}
	    	if (sb.length() == 0)
	    		return "∅";
	    	if (Global.findMaxSat) {
	    		int use_d = (!neg_d) && (num_d >= 2) ? 1 : 0;
	    		int use_az = (!neg_az) && (num_az >= 2) ? 1 : 0;
	    		int use_AZ = (!neg_AZ) && (num_AZ >= 2) ? 1 : 0;
	    		if (use_d + use_az + use_AZ >=2) {
	    			return "\\w";
	    		}
	    		if (use_d + use_az + use_AZ == 1) {
	    			if (use_d == 1)
	    				return "\\d";
	    			if (use_az == 1)
	    				return "[a-z]";
	    			if (use_AZ == 1)
	    				return "[A-Z]";
	    		}
	    	}
	    	return "[" + sb.toString() + "]";
	    }

	    return String.format("%c", this.ch);
	  }
  
	public void setEpsilon() {
		
	}
	
	public boolean getEpsilon() {
		return false;
	}
	
	@Override
	public int LRUnknownCount() {
		if (this.ch == '■') {
			return 1;
		}
		return -1;
	}
}
