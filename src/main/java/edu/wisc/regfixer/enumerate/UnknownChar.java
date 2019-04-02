package edu.wisc.regfixer.enumerate;

import java.util.LinkedList;
import java.util.List;

import com.microsoft.z3.BoolExpr;

import edu.wisc.regfixer.global.Global;
import edu.wisc.regfixer.parser.CharDotNode;
import edu.wisc.regfixer.parser.CharLiteralNode;
import edu.wisc.regfixer.parser.RegexNode;
import edu.wisc.regfixer.parser.StarNode;
import edu.wisc.regfixer.parser.Storage;
import edu.wisc.regfixer.synthesize.SynthesisFailure;

public class UnknownChar implements Unknown, RegexNode, Comparable<UnknownChar> {
  // A bit of a hack...
  //
  // During enumeration of many possible regex templates it's necessary to
  // build regular expressions of incomplete templates where each unknown char
  // is rendered as a some temporary char-class before the whole expression is
  // passed to the regex engine. As long as the 'fill' property is NULL, any
  // call to UnknownChar#toString() will return '■' but if 'fill' is some other
  // value the method will return the appropriate expression.
  //
  // It's important that any time this value is set to a non-NULL value it is
  // reset back to NULL immediately to prevent bugs further in the enumeration
  // process.
  public static enum FillType { Dot, DotStar, EmptySet, Unfrozen, Default }
  private static FillType fill = FillType.Default;
  public static void setFill (FillType which) { UnknownChar.fill = which; }
  public static void clearFill () { UnknownChar.fill = FillType.Default; }

  // Used to compute a unique age for each UnknownChar generated so that older
  // nodes can be expanded before younger nodes.
  private static int nextAge = 0;

  private UnknownId id;
  private int age;
  private List<Expansion> history;
  private boolean frozen = false;
  public int location = 0;
  public BoolExpr[][] pairs;

  public UnknownChar () {
    this(new LinkedList<>());
  }

  public UnknownChar (Expansion latest) {
    this.id = new UnknownId(this);
    if (this.history == null)
    	this.history = new LinkedList<>();
    this.history.add(latest);
  }

  public UnknownChar (List<Expansion> history) {
    this.id = new UnknownId(this);
    this.age = UnknownChar.nextAge++;
    this.history = history;
  }

  public UnknownChar (List<Expansion> history, Expansion latest) {
    this.id = new UnknownId(this);
    this.age = UnknownChar.nextAge++;
    this.history = new LinkedList<>(history);
    this.history.add(latest);
  }

  public UnknownId getId () {
    return this.id;
  }

  public int getAge () {
    return this.age;
  }

  public List<Expansion> getHistory () {
    return this.history;
  }

  public UnknownChar expand (Expansion latest) {
    List<Expansion> newHistory = new LinkedList<>(this.history);
    newHistory.add(latest);
    return new UnknownChar(newHistory);
  }

  public boolean canInsertQuantifierNodes () {
    for (int i = this.history.size() - 1; i >= 0; i--) {
      switch (this.history.get(i)) {
        case Union:
          continue;
        case Concat:
          return true;
        default:
          return false;
      }
    }

    return true;
  }

  public void freeze () {
    this.frozen = true;
  }

  public boolean isFrozen () {
    return this.frozen;
  }

  public int descendants () {
    return 0;
  }

  @Override
  public int compareTo (UnknownChar other) {
    return Integer.compare(this.age, other.age);
  }

  public String toString () {
    switch (UnknownChar.fill) {
      case Dot:
        return ".";
      case DotStar:
        return ".*";
      case EmptySet:
        // FIXME should be ready
        return "∅";
      case Default:
        if (this.isFrozen()) {
          return "▓";
        }
      default:
        return "■";
    }
  }
  
  public void toBinary(){
	  
  }
  
  public int collectUnknown(){
	Storage.unknownCharCounter++;
    this.location = Storage.unknownCharCounter;
    return 1;
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
		  //this.pairs[i][i] = Storage.ctx.mkBoolConst(Integer.toString(Storage.nameIndex++));
		  
		  for (int k = 0; k < Storage.allChars.length; k++) {
			  if (Storage.curExample[i] == Storage.allChars[k]) {
				  this.pairs[i][i] = Storage.charPreds[location][k];
						  
						  /*Storage.ctx.mkBoolConst(Integer.toString(Storage.nameIndex++));
				  Storage.ctx.mkImplies(Storage.charPreds[location][k], this.pairs[i][i]);
				  Storage.ctx.mkImplies(this.pairs[i][i], Storage.charPreds[location][k]);*/
			  }
		  }
	  }
		  
	  //System.err.println("unknown[1][1] is " + this.pairs[1][1]);
	  //return this.pairs;
  }
  
  public BoolExpr[][] getPairs() {
	  return this.pairs;
  }
  
	public String finalString() {
		StringBuilder sb = new StringBuilder();
		boolean hasw = false;
		boolean hasd = false;
		boolean hasaz = false;
		boolean hasAZ = false;
		if (Global.findMaxSat) {
			if (Storage.model.evaluate(Storage.maxCharPreds[location][3], false).toString().equals("true")) {
				//return "\\w";
				sb.append("\\w");
				hasw = true;
			}
			if (Storage.model.evaluate(Storage.maxCharPreds[location][0], false).toString().equals("true")) {
				if (!hasw)
					sb.append("\\d");
				hasd = true;
			}
			if (Storage.model.evaluate(Storage.maxCharPreds[location][1], false).toString().equals("true")) {
				if (!hasw)
					sb.append("[a-z]");
				hasaz = true;
			}
			if (Storage.model.evaluate(Storage.maxCharPreds[location][2], false).toString().equals("true")) {
				if (!hasw)
					sb.append("[A-Z]");
				hasAZ = true;
			}
		}

		for (int cNum = 0; cNum < Storage.allChars.length; cNum++) {
			char c = Storage.allChars[cNum];
			if ('0' <= c && c <= '9' && (hasd || hasw))
				continue;
			if ('a' <= c && c <= 'z' && (hasaz || hasw))
				continue;
			if ('A' <= c && c <= 'Z' && (hasAZ || hasw))
				continue;
			if (c == '_' && hasw)
				continue;
			if (Storage.model.evaluate(Storage.charPreds[location][cNum], false).toString().equals("true")) {
				if (c == '-') {
					sb.append("\\-");
			    } else if (c == '$') {
			    	sb.append("\\$");
			    } else if (c == '.') {
			    	sb.append("\\.");
			    } else {
			    	sb.append(c);
			    }
			}
		}
		if (sb.length() == 0)
			return "∅";
		return "[" + sb.toString() + "]";
	}
  
  /*public String finalString () {
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
  }*/
  
  public void setEpsilon() {
  }

  public boolean getEpsilon() {
	  return false;
  }
  
  @Override
  public int LRUnknownCount() {
	  return 1;
  }
}
