package edu.wisc.regfixer.enumerate;

import edu.wisc.regfixer.parser.Bounds;
import edu.wisc.regfixer.parser.Storage;

public class UnknownBounds extends Bounds implements Unknown {
  // SEE: UnknownChar
  private static Bounds fill = null;
  public static void setFill () { UnknownBounds.setFill(Bounds.atLeast(0)); }
  public static void setFill (Bounds bounds) { UnknownBounds.fill = bounds; }
  public static void clearFill () { UnknownBounds.fill = null; }

  private UnknownId id;

  public UnknownBounds () {
    super(0, null);
    this.id = new UnknownId(this);
  }

  public UnknownBounds (Bounds original) {
    super(original);
    this.id = new UnknownId(this);
  }

  public UnknownId getId () {
    return this.id;
  }

  public String toString () {
    if (UnknownBounds.fill == null) {
      return "{■}";
    } else {
      return UnknownBounds.fill.toString();
    }
  }
  
  public String finalString (int lloc, int rloc) {
	  String l = "";
	  if (Storage.model.getConstInterp(Storage.boundPreds[lloc]) != null)
		  l = Storage.model.getConstInterp(Storage.boundPreds[lloc]).toString();
	  String r = "";
	  if (Storage.model.getConstInterp(Storage.boundPreds[rloc]) != null)
	  	r = Storage.model.getConstInterp(Storage.boundPreds[rloc]).toString();
	  if (l.equals("0") && r.equals(String.valueOf(Bounds.MAX_BOUND)))
		  return "*";
	  if (l.equals("1") && r.equals(String.valueOf(Bounds.MAX_BOUND)))
		  return "+";
	  return "{" + l + "," + r + "}";
  }
}
