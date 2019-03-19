package edu.wisc.regfixer.parser;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.microsoft.z3.BoolExpr;

import edu.wisc.regfixer.automata.Automaton;

public class CharEscapedNode implements ConcreteCharClass {
  private char ch;
  private BoolExpr[][] pairs;

  public CharEscapedNode (char ch) {
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
    return Objects.hash(this.toString());
  }

  @Override
  public boolean equals (Object obj) {
    if (obj instanceof CharEscapedNode) {
      return (this.ch == ((CharEscapedNode) obj).getChar());
    }

    return false;
  }

  public String toString () {
    return String.format("\\%c", this.ch);
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
  
  public void setPairs() {
	  int length = Storage.curExample.length;
	  this.pairs = new BoolExpr[length][length];
	  for (int i = 0; i < length; i++) {
		  for (int j = i; j < length; j++) {
			  this.pairs[i][j] = Storage.ctx.mkBool(false);
		  }
		  char cur = Storage.curExample[i];
		  switch (ch) {
		      case 'd': if ('0'<= cur && cur <= '9') {
	                        this.pairs[i][i] = Storage.ctx.mkBool(true);
                        }
                        break;
		      case 'D': if (('\t'<= cur && cur <= '\r') || (cur == ' ')){
                            this.pairs[i][i] = Storage.ctx.mkBool(true);
                        }
                        break;
		      case 's': if (('0'<= cur && cur <= '\r') ||('A'<= cur && cur <= 'Z') ||
		    		        ('a'<= cur && cur <= 'z') || ('_'<= cur && cur <= '_')){
                            this.pairs[i][i] = Storage.ctx.mkBool(true);
                        }
                        break;
		      case 'S': if (!('0'<= cur && cur <= '9')) {
                            this.pairs[i][i] = Storage.ctx.mkBool(true);
                        }
                        break;
		      case 'w': if (!(('\t'<= cur && cur <= '\r') || (cur == ' '))){
                            this.pairs[i][i] = Storage.ctx.mkBool(true);
                        }
                        break;
		      case 'W': if (!(('0'<= cur && cur <= '\r') ||('A'<= cur && cur <= 'Z') ||
	    		            ('a'<= cur && cur <= 'z') || ('_'<= cur && cur <= '_'))){
                            this.pairs[i][i] = Storage.ctx.mkBool(true);
                        }
                        break;
		      default: if (this.ch == cur) {
			              this.pairs[i][i] = Storage.ctx.mkBool(true);
		               }
		               break;
		  }
	  }
	  //return this.pairs;
  }
  
  public BoolExpr[][] getPairs() {
	  return this.pairs;
  }
  
  public String finalString () {
	    return String.format("\\%c", this.ch);
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
