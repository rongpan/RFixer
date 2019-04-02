package edu.wisc.regfixer.enumerate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.TreeSet;

import edu.wisc.regfixer.automata.Automaton;
import edu.wisc.regfixer.automata.Route;
import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.global.Global;
import edu.wisc.regfixer.parser.Bounds;
import edu.wisc.regfixer.parser.ConcatNode;
import edu.wisc.regfixer.parser.OptionalNode;
import edu.wisc.regfixer.parser.PlusNode;
import edu.wisc.regfixer.parser.RegexNode;
import edu.wisc.regfixer.parser.RepetitionNode;
import edu.wisc.regfixer.parser.StarNode;
import edu.wisc.regfixer.parser.UnionNode;
import edu.wisc.regfixer.parser.Storage;
import edu.wisc.regfixer.synthesize.Synthesis;
import edu.wisc.regfixer.synthesize.SynthesisFailure;
import org.sat4j.specs.TimeoutException;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Optimize;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import org.apache.commons.lang3.*;

public class Enumerant implements Comparable<Enumerant> {
  public final static int UNION_COST    = 1;
  public final static int OPTIONAL_COST = 1;
  public final static int STAR_COST     = 3;
  public final static int PLUS_COST     = 2;
  public final static int CONCAT_COST   = 1;
  public final static int REPEAT_COST   = 1;
  public final static int FREEZE_COST   = 1;
  
  public static boolean emptyTest;
  public Boolean passDotStar;
  public Boolean passDot;
  public Boolean passEmpty;
  public boolean solve;
  public long order;

  private final RegexNode tree;
  private Automaton automaton = null;
  private final Set<UnknownId> ids;
  private final int cost;
  private final Expansion latest;
  private Enumerant parent;
  
  @FunctionalInterface
  public static interface ExpansionFunction {
    Enumerant apply(UnknownChar unknown) throws ForbiddenExpansionException;
  }

  @FunctionalInterface
  public static interface MultExpansionFunction {
    Enumerant apply(Collection<UnknownChar> unknowns) throws ForbiddenExpansionException;
  }

  public Enumerant (RegexNode tree, UnknownId id, int cost, Expansion latest) {
    this(tree, Arrays.asList(id), cost, latest);
  }

  public Enumerant (RegexNode tree, Collection<UnknownId> ids, int cost, Expansion latest) {
    this.tree = tree;
    this.ids = new HashSet<>(ids);
    this.cost = cost;
    this.latest = latest;
    this.parent = null;
    this.passDot = false;
  }

  public RegexNode getTree () {
    return this.tree;
  }
  
  public Enumerant getParent() {
	  return this.parent;
  }
  
  public void setParent(Enumerant p) {
	  this.parent = p;
  }

  public Set<UnknownId> getIds () {
    return this.ids;
  }

  public boolean hasUnknownId (UnknownId id) {
    return this.ids.contains(id);
  }

  public int getCost () {
    return this.cost;
  }

  public Expansion getLatestExpansion () {
    return this.latest;
  }

  public Pattern toPattern (UnknownChar.FillType type) {
    // Set temporary values for unknowns.
    UnknownChar.setFill(type);

    if (type == UnknownChar.FillType.EmptySet) {
      UnknownBounds.setFill(Bounds.exactly(1));
      Enumerant.emptyTest = true;
    } else {
      UnknownBounds.setFill();
    }

    // Build the pattern with temporary values replacing unknowns.
    Pattern pattern = Pattern.compile(String.format("^%s$", this.tree));

    // Clear the temporary values.
    UnknownChar.clearFill();
    UnknownBounds.clearFill();
    Enumerant.emptyTest = false;

    return pattern;
  }

  /*public List<Enumerant> expand () {
    List<Enumerant> expansions = new LinkedList<>();

    // Create a sorted list of UnknownChar's from youngest -> oldest.
    TreeSet<UnknownChar> unknowns = new TreeSet<UnknownChar>(this.ids
      .stream()
      .filter(id -> id.getUnknown() instanceof UnknownChar)
      .map(id -> (UnknownChar)id.getUnknown())
      .collect(Collectors.toSet()));

    // 1. Identify oldest unfrozen unknown
    // 2. Apply all valid expansions to that unknown and push those templates to the stack
    // 3. Freeze that unknown and push that template to the stack
    UnknownChar oldest = null;
    for (UnknownChar unknown : unknowns) {
      if (oldest == null) {
        oldest = unknown;
      } else if (unknown.isFrozen() == false && unknown.getAge() < oldest.getAge()) {
        oldest = unknown;
      }
    }

    if (oldest != null) {
      // Perform expansion converting unknown char -> union, quantifier, and concat.
      this.addExpansion(expansions, oldest, this::expandWithUnion);
      if (oldest.canInsertQuantifierNodes()) {
        this.addExpansion(expansions, oldest, this::expandWithUnknownQuantifier);
      }
      this.addExpansion(expansions, oldest, this::expandWithConcat);
      //this.addExpansion(expansions, oldest, this::expandWithFrozen);
    }

    return expansions;
  }

  private void addExpansion (List<Enumerant> expansions, UnknownChar unknown, ExpansionFunction expander) {
    Enumerant expansion = null;
    try {
      expansion = expander.apply(unknown);
    } catch (ForbiddenExpansionException ex) {
      return;
    }

    if (expansion != null) {
      expansions.add(expansion);
    }
  }

  private void addExpansion (List<Enumerant> expansions, Collection<UnknownChar> unknowns, MultExpansionFunction expander) {
    Enumerant expansion = null;
    try {
      expansion = expander.apply(unknowns);
    } catch (ForbiddenExpansionException ex) {
      return;
    }

    if (expansion != null) {
      expansions.add(expansion);
    }
  }

  private Enumerant expandWithUnion (UnknownChar unknown) throws ForbiddenExpansionException {
    // Create both unknown chars to be added to the regex tree.
    UnknownChar un1 = new UnknownChar(unknown.getHistory(), Expansion.SyntheticUnion);
    UnknownChar un2 = new UnknownChar(unknown.getHistory(), Expansion.SyntheticUnion);

    // Create union node to added in place of the given 'unknown'.
    RegexNode scion = new UnionNode(un1, un2, true);

    // Graft scion onto the root regex tree.
    RegexNode root = Grafter.graft(this.tree, unknown.getId(), scion);

    // Build set of IDs custom to the new enumerant.
    Set<UnknownId> ids = new HashSet<>();
    ids.addAll(this.getIds());
    ids.remove(unknown.getId());
    ids.add(un1.getId());
    ids.add(un2.getId());

    // Add cost of the expansion.
    int cost = this.getCost() + Enumerant.UNION_COST;

    // Build components into new enumerant.
    return new Enumerant(root, ids, cost, Expansion.SyntheticUnion);
  }

  private Enumerant expandWithUnknownQuantifier (UnknownChar unknown) throws ForbiddenExpansionException {
    // Create an unknown char to be added to the regex tree.
    UnknownChar child = new UnknownChar(unknown.getHistory(), Expansion.Repeat);
    UnknownBounds bounds = new UnknownBounds();

    // Create unknown node to add in place of the given 'unknown'.
    RegexNode scion = new RepetitionNode(child, bounds);

    // Graft scion onto the root regex tree.
    RegexNode root = Grafter.graftWithUnknownAncestors(this.tree, unknown.getId(), scion);

    // Build set of IDs custom to the new enumerant.
    Set<UnknownId> ids = new HashSet<>();
    ids.addAll(this.getIds());
    ids.remove(unknown.getId());
    ids.add(child.getId());
    ids.add(bounds.getId());
    ids.addAll(Grafter.addedBounds);

    // Add cost of the expansion.
    int cost = this.getCost() + Enumerant.REPEAT_COST;

    // Build components into new enumerant.
    return new Enumerant(root, ids, cost, Expansion.Repeat);
  }

  private Enumerant expandWithConcat (UnknownChar unknown) throws ForbiddenExpansionException {
    // Create both unknown chars to be added to the regex tree.
    UnknownChar un1 = new UnknownChar(unknown.getHistory(), Expansion.Concat);
    UnknownChar un2 = new UnknownChar(unknown.getHistory(), Expansion.Concat);

    // Create concatenation node to added in place of the given 'unknown'.
    RegexNode scion = new ConcatNode(un1, un2);

    // Graft scion onto the root regex tree.
    RegexNode root = Grafter.graft(this.tree, unknown.getId(), scion);

    // Build set of IDs custom to the new enumerant.
    Set<UnknownId> ids = new HashSet<>();
    ids.addAll(this.getIds());
    ids.remove(unknown.getId());
    ids.add(un1.getId());
    ids.add(un2.getId());

    // Add cost of the expansion.
    int cost = this.getCost() + Enumerant.CONCAT_COST;

    // Build components into new enumerant.
    return new Enumerant(root, ids, cost, Expansion.Concat);
  }

  private Enumerant expandWithFrozen (UnknownChar unknown) throws ForbiddenExpansionException {
    // Create frozen unknown to added in place of the given 'unknown'.
    UnknownChar frozen = new UnknownChar(unknown.getHistory(), Expansion.Freeze);
    frozen.freeze();

    // Graft scion onto the root regex tree.
    RegexNode root = Grafter.graft(this.tree, unknown.getId(), frozen);

    // Build set of IDs custom to the new enumerant.
    Set<UnknownId> ids = new HashSet<>();
    ids.addAll(this.getIds());
    ids.remove(unknown.getId());
    ids.add(frozen.getId());

    // Add cost of expansion.
    int cost = this.getCost() + Enumerant.FREEZE_COST;

    // Build components into new enumerant.
    return new Enumerant(root, ids, cost, Expansion.Freeze);
  }*/

  public Synthesis synthesize (Set<String> p, Set<String> n, Diagnostic diag) throws SynthesisFailure {
    //Automaton automaton = null;

	if (automaton == null) {
		try {
		  automaton = new Automaton(this.tree);
		} catch (TimeoutException ex) {
		  String fmt = "timed-out building automaton for `%s`";
		  throw new SynthesisFailure(String.format(fmt, this.tree));
		}
	}
    List<Set<Route>> positiveRuns = new LinkedList<>();
    List<Set<Route>> negativeRuns = new LinkedList<>();

    /*if (this.tree.toString().equals("(((a)*(b){■})(a){■})*")) {
      System.out.println("automaton: " + automaton);
      System.out.println("unknownToEntryState " + automaton.unknownToEntryState);
      System.out.println("unknownToExitStates " + automaton.unknownToExitStates);
    }*/

    try {
      for (String source : p) {
    	  //if (this.tree.toString().equals("((((■)+@)(([a-zA-Z_])+)?)\\.)([a-zA-Z]){■}"))
    		  //System.out.println("positive example: " + source);
    	//if(emptySetMatching(source))
    		//continue;
    	Set<Route> positiveRun = automaton.trace(source);
    	if (positiveRun.size() == 0) {
    		//System.out.println("get p size() 0 !!!!!!!!!!!!!!!!!!!!!");
    		return null;
    	}
    	boolean ignore = false;
    	for (Route route : positiveRun) {
    		//if (this.tree.toString().equals("((((■)+@)(([a-zA-Z_])+)?)\\.)([a-zA-Z]){■}"))
    			//System.out.println("positive Route: " + route);
    		if (route.hasNoRealExits() && route.hasNoSpans()) {
    			//System.out.println("positive ignore");
    			ignore = true;
    		}
    	}
    	if (ignore)
    		continue;
        positiveRuns.add(positiveRun);
      }

      for (String source : n) {
    	  //if (this.tree.toString().equals("((((■)+@)(([a-zA-Z_])+)?)\\.)([a-zA-Z]){■}"))
    		//  System.out.println("negative example: " + source);
    	  Set<Route> negativeRun = automaton.trace(source);
    	  //System.out.println("negativeRun:\n" + negativeRun);
    	  if (negativeRun.size() == 0) {
    		  continue;
    	  }
    	  //System.err.println("source is " + source);
    	  for (Route route : negativeRun) {
    		  //if (this.tree.toString().equals("((((■)+@)(([a-zA-Z_])+)?)\\.)([a-zA-Z]){■}"))
    			//  System.out.println("negative route: " + route);
      		if (route.hasNoRealExits() && route.hasNoSpans()) {
      			//System.out.println("negative skip");
      			return null;
      		}
      	}
        negativeRuns.add(negativeRun);
      }
    } catch (TimeoutException ex) {
      String fmt = "timed-out computing runs for `%s`";
      throw new SynthesisFailure(String.format(fmt, this.tree));
    }
    
    int totalRuns = positiveRuns.size() + negativeRuns.size();
    if (diag.getInt("maximumRoutes") < totalRuns) {
      diag.registry().setInt("maximumRoutes", totalRuns);
    }

    Global.root = this.tree;
    if (!Global.cegis) {
      return new Synthesis(this, positiveRuns, negativeRuns, diag);
    } else {
    	Synthesis syn = new Synthesis(this, positiveRuns, negativeRuns, diag);
    	RegexNode solution = syn.getTree();
    	System.out.println("get a solution: " + solution);
    	Automaton currentAutomaton;
    			
	    try {
	    	//System.err.println("before building automaton");
	        currentAutomaton = new Automaton(solution);
	        //System.err.println("before verify");
	        if (currentAutomaton.verify(Global.correctAutomaton)) {
		    	return syn;
		    } else {
		    	String positive = currentAutomaton.morePositive(Global.correctAutomaton);
		    	String negative = currentAutomaton.moreNegative(Global.correctAutomaton);
		    	if (positive != null) {
		    		System.out.println("add positive: " + positive);
		    		Global.positives++;
		    		p.add(positive);
		    	}
		    	if (negative != null) {
		    		System.out.println("add negative: " + negative);
		    		Global.negatives++;
		    		n.add(negative);
		    	}
		    	return synthesize(p, n, diag);
		    }
	    } catch (TimeoutException ex) {
	        String fmt = "timed-out cegis for `%s`";
	        throw new SynthesisFailure(String.format(fmt, this.tree));
	    }
	    
    }
  }

  public Synthesis synthesizePair (Set<String> p, Set<String> n, Diagnostic diag) throws SynthesisFailure {
	  /*if (Global.findMaxSat) {
		  return synthesizePairMax(p, n, diag);
	  }*/
	  Storage.reset();
	  //this.tree.toBinary();
	  int holes = this.tree.collectUnknown();
	  
	  Context ctx = new Context();
	  Storage.ctx = ctx;
	  Optimize opt = Storage.ctx.mkOptimize();
	  Set<Character> chars = new HashSet<>();
	  for (String s : p) {
		  chars.addAll(this.CollectChars(s));
	  }
	  if (Global.findMaxSat) {
		  for (String s : n) {
			  chars.addAll(this.CollectChars(s));
		  }
	  }
	  Storage.allChars = new char[chars.size()];
	  int ctr = 0;
	  for (char c : chars) {
		  Storage.allChars[ctr++] = c;
	  }
	  
	  int charNum = chars.size();
	  if (Storage.unknownCharCounter > -1) {
	      Storage.charPreds = new BoolExpr[Storage.unknownCharCounter + 1][charNum];
	  }
	  for (int i = 0; i < Storage.unknownCharCounter + 1; i++) {
		  for (int j = 0; j < Storage.allChars.length; j++) {
			  Storage.charPreds[i][j] = Storage.ctx.mkBoolConst("char" + Integer.toString(i) + "_" + Character.toString(
					  Storage.allChars[j]));
		  }
	  }
	  
	  BoolExpr expr = ctx.mkBoolConst("final");
	  boolean init = false;
	  
	  IntExpr zero = ctx.mkInt(0);
	  
	  if (Storage.unknownBoundCounter > -1)
	      Storage.boundPreds = new IntExpr[Storage.unknownBoundCounter + 1];
	  for (int i = 0; i < Storage.unknownBoundCounter + 1; i++) {
		  IntExpr boundExpr = Storage.ctx.mkIntConst("bound" + Integer.toString(i));
		  opt.Assert(ctx.mkGe(boundExpr, zero));
		  Storage.boundPreds[i] = boundExpr;
		  if (Global.tutor) {
			  if (i % 2 == 0) {
				  if (!init) {
					  expr = ctx.mkAnd(ctx.mkLe(boundExpr, ctx.mkInt(2)));
					  init = true;
				  } else {
					  expr = ctx.mkAnd(expr, ctx.mkLe(boundExpr, ctx.mkInt(2)));
				  }
			  } else {
				  expr = ctx.mkAnd(expr, ctx.mkOr(ctx.mkEq(boundExpr, ctx.mkInt(1)),
						  ctx.mkEq(boundExpr, ctx.mkInt(Bounds.MAX_BOUND))));
			  }
		  }
	  }
	  
	  this.tree.setNullable();
	  this.tree.setLen();
	  
	  for (String s : p) {
		  //if (s.length() < 1)
			  //continue;
		  if (!init) {
			  if (s.length() < 1) {
				  expr = tree.isNullable();
			  } else {
				  expr = ctx.mkAnd(this.getConstraintForOne(s, tree));
			  }
		      init = true;
		  } else {
		      expr = ctx.mkAnd(expr, this.getConstraintForOne(s, tree));
		  }
	  }
	  for (String s : n) {
		  //if (s.length() < 1)
			  //continue;
		  if (s.length() < 1) {
			  expr = ctx.mkAnd(expr, ctx.mkNot(tree.isNullable()));
		  } else {
			  expr = ctx.mkAnd(expr, ctx.mkNot(this.getConstraintForOne(s, tree)));
		  }
	  }
	  
	  //Optimize opt = Storage.ctx.mkOptimize();
	  Model model;
	  
	  if (Global.findMaxSat) {
		  //Storage.costArray = new ArrayList<>();
		  /*if (Storage.unknownBoundCounter > -1)
		      Storage.boundPreds = new IntExpr[Storage.unknownBoundCounter + 1];
		  for (int i = 0; i < Storage.unknownBoundCounter + 1; i++) {
			  Storage.boundPreds[i] = Storage.ctx.mkIntConst("bound" + Integer.toString(i));
		  }
		  for (int i = 0; i < Storage.unknownBoundCounter + 1; i++) {
			  this.setOptBound(opt, i);
		  }*/
		  List<IntExpr> boundList = new ArrayList<>();
		  if (Storage.unknownBoundCounter > -1) {
			  boundList = setOptBound(opt);
		  }
		  List<IntExpr> charList = new ArrayList<>();
		  if (Storage.unknownCharCounter > -1) {
			  Storage.maxCharPreds = new BoolExpr[Storage.unknownCharCounter + 1][4];
			  for (int i = 0; i < Storage.unknownCharCounter + 1; i++) {
				  for (int j = 0; j < 4; j++) {
					  Storage.maxCharPreds[i][j] = ctx.mkBoolConst(String.valueOf(i) + "_" +
				  "maxChar" + String.valueOf(j));
				  }
				  charList.addAll(setOpt(opt, i));
			  }
		  }
		  
		  opt.Assert(expr);
		  if (boundList.size() > 0) {
			  IntExpr[] boundCost = boundList.toArray(new IntExpr[boundList.size()]);
			  opt.MkMinimize(Storage.ctx.mkAdd(boundCost));
		  }
		  if (charList.size() > 0) {
			  IntExpr[] charCost = charList.toArray(new IntExpr[charList.size()]);
			  opt.MkMinimize(Storage.ctx.mkAdd(charCost));
		  }
	  } else {
		  opt.Assert(expr);
	  }
	  
	  diag.timing().startTiming("timeSATSolver");
	  Status status = opt.Check();
	  long duration = diag.timing().stopTimingAndAdd("timeSATSolver");
	  diag.stat.add(tree.toString(), tree.descendants(), holes, duration);
	  if (status != Status.SATISFIABLE) {
	      throw new SynthesisFailure("unsatisfiable SAT formula");
	  } else {
	      model = opt.getModel();
	      Storage.model = model;
	      if (Global.cegis) {
	      	RegexNode solution;
	      	try {
	      		solution = edu.wisc.regfixer.parser.Main.parse(this.tree.finalString());
	      	} catch (Exception ex) {
	            // FIXME
	            throw new RuntimeException("malformed regular expression");
	        }
	      	System.out.println("get a solution: " + solution);
	      	Automaton currentAutomaton;
	      			
	  	    try {
	  	    	//System.err.println("before building automaton");
	  	        currentAutomaton = new Automaton(solution);
	  	        //System.err.println("before verify");
	  	        if (currentAutomaton.verify(Global.correctAutomaton)) {
	  	        	displayPairResults(model);
	  	    	  return new Synthesis();
	  		    } else {
	  		    	String positive = currentAutomaton.morePositive(Global.correctAutomaton);
	  		    	String negative = currentAutomaton.moreNegative(Global.correctAutomaton);
	  		    	if (positive != null) {
	  		    		System.out.println("add positive: " + positive);
	  		    		Global.positives++;
	  		    		p.add(positive);
	  		    	}
	  		    	if (negative != null) {
	  		    		System.out.println("add negative: " + negative);
	  		    		Global.negatives++;
	  		    		n.add(negative);
	  		    	}
	  		    	return synthesizePair(p, n, diag);
	  		    }
	  	    } catch (TimeoutException ex) {
	  	        String fmt = "timed-out cegis for `%s`";
	  	        throw new SynthesisFailure(String.format(fmt, this.tree));
	  	    }
	  	    
	      } else {
	    	  displayPairResults(model);
	    	  return new Synthesis();
	      }
	  }
	  
  }
  
  private void displayPairResults (Model model) {
	  
      //System.err.println("model is " + model.toString());
      System.out.println("tree is" + this.tree);
      
      Global.root = this.tree;
      
      for (int count = 0; count < Storage.unknownCharCounter + 1; count++) {
        	for (int cNum = 0; cNum < Storage.allChars.length; cNum++) {
        	    System.out.println("char_" + Storage.allChars[cNum] + ": " + 
        	    		model.evaluate(Storage.charPreds[count][cNum], false));
        	}
        	if (Global.findMaxSat) {
        		for (int j = 0; j < 4; j++) {
        			System.out.println("maxChar_" + 
            	    		model.evaluate(Storage.maxCharPreds[count][j], false));
        		}
        	}
        }
      
      for (int count = 0; count < Storage.unknownBoundCounter + 1; count++) {
            System.out.println(Storage.boundPreds[count]);
            
            IntExpr intExpr = Storage.boundPreds[count];
            System.out.println("bound is " + intExpr + "_" + model.getConstInterp(intExpr));
      }
      if (Global.findMaxSat) {
    	  System.out.println("final tree is#m#" + this.tree.finalString() + "#m#");
      } else {
    	  System.out.println("final tree is" + this.tree.finalString());
      }
  }
  
  /*public Synthesis synthesizePairMax (Set<String> p, Set<String> n, Diagnostic diag) throws SynthesisFailure {
	  Storage.reset();
	  this.tree.toBinary();
	  int holes = this.tree.collectUnknown();
	  
	  Storage.ctx = new Context();
	  Set<Character> chars = new HashSet<>();
	  for (String s : p) {
		  chars.addAll(this.CollectChars(s));
	  }
	  Storage.allChars = new char[chars.size()];
	  int ctr = 0;
	  for (char c : chars) {
		  Storage.allChars[ctr++] = c;
	  }
	  
	  int charNum = chars.size();
	  Storage.costArray = new ArrayList<>();
	  if (Storage.unknownCharCounter > -1) {
	      Storage.charPreds = new BoolExpr[Storage.unknownCharCounter + 1][charNum];
	      Storage.maxCharPreds = new BoolExpr[Storage.unknownCharCounter + 1][4];
	  }
	  for (int i = 0; i < Storage.unknownCharCounter + 1; i++) {
		  for (int j = 0; j < Storage.allChars.length; j++) {
			  Storage.charPreds[i][j] = Storage.ctx.mkBoolConst("char" + Integer.toString(i) + "_" + Character.toString(
					  Storage.allChars[j]));
		  }
		  Storage.maxCharPreds[i][0] = Storage.ctx.mkBoolConst("max" + Integer.toString(i) + "d");
		  Storage.maxCharPreds[i][1] = Storage.ctx.mkBoolConst("max" + Integer.toString(i) + "az");
		  Storage.maxCharPreds[i][2] = Storage.ctx.mkBoolConst("max" + Integer.toString(i) + "AZ");
		  Storage.maxCharPreds[i][3] = Storage.ctx.mkBoolConst("max" + Integer.toString(i) + "w");
	  }
	  
	  if (Storage.unknownBoundCounter > -1)
	      Storage.boundPreds = new IntExpr[Storage.unknownBoundCounter + 1];
	  for (int i = 0; i < Storage.unknownBoundCounter + 1; i++) {
		  Storage.boundPreds[i] = Storage.ctx.mkIntConst("bound" + Integer.toString(i));
	  }
	  
	  this.tree.setNullable();
	  this.tree.setLen();
	  
	  BoolExpr expr = Storage.ctx.mkBoolConst("final");
	  int ini = 0;
	  for (String s : p) {
		  if (s.length() < 1)
			  continue;
		  if (ini == 0) {
		      expr = Storage.ctx.mkAnd(this.getConstraintForOne(s, tree));
		      ini++;
		  } else {
		      expr = Storage.ctx.mkAnd(expr, this.getConstraintForOne(s, tree));
		  }
	  }
	  ini = 0;
	  for (String s : n) {
		  if (s.length() < 1)
			  continue;
		  expr = Storage.ctx.mkAnd(expr, Storage.ctx.mkNot(this.getConstraintForOne(s, tree)));
	  }
	  
	  //Optimize opt = Storage.ctx.mkOptimize();
	  Model model;
	  
	  //Solver s = Storage.ctx.mkSolver();
	  //s.add(expr);
	  Optimize opt = Storage.ctx.mkOptimize();
	  //opt.Assert(expr);
	  for (int i = 0; i < Storage.unknownCharCounter + 1; i++) {
		  this.setOpt(opt, i);
	  }
	  for (int i = 0; i < Storage.unknownBoundCounter + 1; i++) {
		  this.setOptBound(opt, i);
	  }
	  IntExpr[] costArray = Storage.costArray.toArray(new IntExpr[Storage.costArray.size()]);
      opt.MkMinimize(Storage.ctx.mkAdd(costArray));
      opt.Assert(expr);
      
      System.err.println("check5");
	  diag.timing().startTiming("timeSATSolver");
	  Status status = opt.Check();
	  long duration = diag.timing().stopTimingAndAdd("timeSATSolver");
	  diag.stat.add(tree.toString(), tree.descendants(), holes, duration);
	  System.err.println("check6");
	  if (status != Status.SATISFIABLE) {
	      throw new SynthesisFailure("unsatisfiable SAT formula");
	  } else {
	      model = opt.getModel();
	      Storage.model = model;
	      //System.err.println("model is " + model.toString());
	      System.out.println("tree is" + this.tree);
	      
	      for (int count = 0; count < Storage.unknownCharCounter + 1; count++) {
	        	for (int cNum = 0; cNum < Storage.allChars.length; cNum++) {
	        	    System.out.println("char_" + Storage.allChars[cNum] + ": " + 
	        	    		model.evaluate(Storage.charPreds[count][cNum], false));
	        	}
	        }
	      
	      for (int count = 0; count < Storage.unknownBoundCounter + 1; count++) {
	            System.out.println(Storage.boundPreds[count]);
	            
	            IntExpr intExpr = Storage.boundPreds[count];
	            System.out.println("bound is " + intExpr + "_" + model.getConstInterp(intExpr));
	      }
	      
	      for (int count = 0; count < Storage.unknownCharCounter + 1; count++) {
	    	  System.out.println("max_d" +
      	    		model.evaluate(Storage.maxCharPreds[count][0], false));
	    	  System.out.println("max_az" +
	      	    		model.evaluate(Storage.maxCharPreds[count][1], false));
	    	  System.out.println("max_AZ" +
	      	    		model.evaluate(Storage.maxCharPreds[count][2], false));
	    	  System.out.println("max_w" +
	      	    		model.evaluate(Storage.maxCharPreds[count][3], false));
	      }
	      
	      System.out.println("final tree is" + this.tree.finalString());
	      return new Synthesis();
	  }
	  
  }*/
  
  @Override
  public int compareTo (Enumerant other) {
    int weight = Integer.compare(this.getCost(), other.getCost());
    if (weight != 0)
    	return weight;
    return Long.compare(this.order, other.order);
  }

  @Override
  public String toString () {
    return this.tree.toString();
  }
  
  private boolean emptySetMatching(String s) {
	  Pattern p = toPattern(UnknownChar.FillType.EmptySet);
	  return p.matcher(s).matches();
  }
  
  private Set<Character> CollectChars(String s) {
	  Set<Character> res = new HashSet<>();
	  for (char c : s.toCharArray()) {
		  res.add(c);
	  }
	  return res;
  }
  
  private BoolExpr getConstraintForOne(String s, RegexNode tree) {
	  Storage.curExample = s.toCharArray();
	  int length = Storage.curExample.length;
	  this.tree.calUpto(length);
	  this.tree.setPairs();
	  return this.tree.getPairs()[0][length - 1];
  }
  
  private List<IntExpr> setOpt(Optimize opt, int index) {
	  List<IntExpr> list = new ArrayList<>();
	  Context ctx = Storage.ctx;
	  int num_d = 0;
	  int num_az = 0;
	  int num_AZ = 0;
	  int num_underscore = 0;
	  for (int i = 0; i < Storage.allChars.length; i++) {
		  char c = Storage.allChars[i];
		  if (c >= '0' && c <= '9') {
			  num_d++;
			  opt.Assert(ctx.mkImplies(Storage.maxCharPreds[index][0], Storage.charPreds[index][i]));
			  opt.Assert(ctx.mkImplies(Storage.maxCharPreds[index][3], Storage.charPreds[index][i]));
		  } else if (c >= 'a' && c <= 'z') {
			  num_az++;
			  opt.Assert(ctx.mkImplies(Storage.maxCharPreds[index][1], Storage.charPreds[index][i]));
			  opt.Assert(ctx.mkImplies(Storage.maxCharPreds[index][3], Storage.charPreds[index][i]));
		  } else if (c >= 'A' && c <= 'Z') {
			  num_AZ++;
			  opt.Assert(ctx.mkImplies(Storage.maxCharPreds[index][2], Storage.charPreds[index][i]));
			  opt.Assert(ctx.mkImplies(Storage.maxCharPreds[index][3], Storage.charPreds[index][i]));
		  } else if (c == '_') {
			  num_underscore++;
			  opt.Assert(ctx.mkImplies(Storage.maxCharPreds[index][3], Storage.charPreds[index][i]));
		  }
		  //Storage.ctx.mkImplies(Storage.charPreds[index][i], null);
		  
		  IntExpr weight = Storage.ctx.mkIntConst(Integer.toString(index) + c);
		  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(2));
		  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
		  opt.Assert((BoolExpr)Storage.ctx.mkITE(Storage.charPreds[index][i], ifTrue, ifFalse));
		  list.add(weight);
	  }
	  
	  //ctx.mkImplies(Storage.maxCharPreds[index][3], Storage.maxCharPreds[index][0]);
	  //ctx.mkImplies(Storage.maxCharPreds[index][3], Storage.maxCharPreds[index][1]);
	  //ctx.mkImplies(Storage.maxCharPreds[index][3], Storage.maxCharPreds[index][2]);
	  
	  if (num_d > 0) {
		  IntExpr weight = Storage.ctx.mkIntConst(Integer.toString(index) + "num_d");
		  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(-(num_d*2)+3));
		  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
		  opt.Assert((BoolExpr)Storage.ctx.mkITE(Storage.maxCharPreds[index][0], ifTrue, ifFalse));
		  list.add(weight);
	  }
	  
	  if (num_az > 0) {
		  IntExpr weight = Storage.ctx.mkIntConst(Integer.toString(index) + "num_az");
		  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(-(num_az*2)+3));
		  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
		  opt.Assert((BoolExpr)Storage.ctx.mkITE(Storage.maxCharPreds[index][1], ifTrue, ifFalse));
		  list.add(weight);
	  }
	  
	  if (num_AZ > 0) {
		  IntExpr weight = Storage.ctx.mkIntConst(Integer.toString(index) + "num_AZ");
		  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(-(num_AZ*2)+3));
		  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
		  opt.Assert((BoolExpr)Storage.ctx.mkITE(Storage.maxCharPreds[index][2], ifTrue, ifFalse));
		  list.add(weight);
	  }
	  
	  int hasTwo = 0;
	  if (num_d > 0 && num_az > 0 || num_d > 0 && num_AZ > 0 || num_az > 0 && num_AZ > 0 ||
			  num_d > 0 && num_underscore > 0 || num_az > 0 && num_underscore > 0 ||
			  num_AZ > 0 && num_underscore > 0) {
		  hasTwo = 2;
	  }
	  
	  IntExpr weight = Storage.ctx.mkIntConst(Integer.toString(index) + "num_w");
	  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(-hasTwo+1));
	  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
	  opt.Assert((BoolExpr)Storage.ctx.mkITE(Storage.maxCharPreds[index][3], ifTrue, ifFalse));
	  list.add(weight);
	  
	  return list;
	  
	  /*BoolExpr d = Storage.maxCharPreds[index][0];
	  BoolExpr az = Storage.maxCharPreds[index][1];
	  BoolExpr AZ = Storage.maxCharPreds[index][2];
	  BoolExpr w = Storage.maxCharPreds[index][3];
	  
	  ctx.mkImplies(ctx.mkOr(ctx.mkAnd(d,az), ctx.mkAnd(d,AZ), ctx.mkAnd(az,AZ)), w);
	  */
  }
  
  private List<IntExpr> setOptBound(Optimize opt) {
	  List<IntExpr> list = new ArrayList<>();
	  Context ctx = Storage.ctx;
	  for (int i = 0; i < Storage.unknownBoundCounter + 1; i++) {
		  IntExpr weight = Storage.ctx.mkIntConst(Integer.toString(i) + "bound");
		  if (i % 2 == 0) {
			  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
			  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(1));
			  opt.Assert((BoolExpr)Storage.ctx.mkITE(ctx.mkOr(ctx.mkEq(Storage.boundPreds[i], ctx.mkInt(0)),
					  ctx.mkEq(Storage.boundPreds[i],ctx.mkInt(1))),
					  ifTrue, ifFalse));
			  list.add(weight);
		  } else {
			  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
			  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(1));
			  opt.Assert((BoolExpr)Storage.ctx.mkITE(ctx.mkOr(ctx.mkEq(Storage.boundPreds[i], ctx.mkInt(Bounds.MAX_BOUND)),
					  ctx.mkEq(Storage.boundPreds[i],ctx.mkInt(1))),
					  ifTrue, ifFalse));
			  list.add(weight);
		  }
	  }
	  
	  return list;
  }
  
}
