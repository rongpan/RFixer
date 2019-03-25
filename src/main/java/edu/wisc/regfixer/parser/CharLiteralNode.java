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
			boolean hasd = false;
			boolean hasaz = false;
			boolean hasAZ = false;
			if (Global.findMaxSat) {
				if (Storage.model.evaluate(Storage.maxCharPreds[location][3], false).toString().equals("true")) {
					return "\\w";
				}
				if (Storage.model.evaluate(Storage.maxCharPreds[location][0], false).toString().equals("true")) {
					sb.append("\\d");
					hasd = true;
				}
				if (Storage.model.evaluate(Storage.maxCharPreds[location][1], false).toString().equals("true")) {
					sb.append("[a-z]");
					hasaz = true;
				}
				if (Storage.model.evaluate(Storage.maxCharPreds[location][2], false).toString().equals("true")) {
					sb.append("[A-Z]");
					hasAZ = true;
				}
			}

			for (int cNum = 0; cNum < Storage.allChars.length; cNum++) {
				char c = Storage.allChars[cNum];
				if ('0' <= c && c <= '9' && hasd)
					continue;
				if ('a' <= c && c <= 'z' && hasaz)
					continue;
				if ('A' <= c && c <= 'Z' && hasAZ)
					continue;
				if (Storage.model.evaluate(Storage.charPreds[location][cNum], false).toString().equals("true")) {
					sb.append(c);
				}
			}
			if (sb.length() == 0)
				return "∅";
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
