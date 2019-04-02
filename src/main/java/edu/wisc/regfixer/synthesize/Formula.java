package edu.wisc.regfixer.synthesize;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Optimize;
import com.microsoft.z3.Status;
import edu.wisc.regfixer.automata.Route;
import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.enumerate.UnknownBounds;
import edu.wisc.regfixer.enumerate.UnknownId;
import edu.wisc.regfixer.global.Global;
import edu.wisc.regfixer.parser.Bounds;
import edu.wisc.regfixer.parser.CharClass;
import edu.wisc.regfixer.parser.CharClassSetNode;
import edu.wisc.regfixer.parser.CharLiteralNode;
import edu.wisc.regfixer.parser.CharRangeNode;
import edu.wisc.regfixer.parser.ConcreteCharClass;
import edu.wisc.regfixer.parser.RegexNode;
import edu.wisc.regfixer.parser.Storage;

public class Formula {
  private List<Set<Route>> positives;
  private List<Set<Route>> negatives;
  private Diagnostic diag;

  private Context ctx;
  private Optimize opt;
  private Model model;

  private Set<UnknownId> unknownChars;
  private Map<UnknownId, Set<BoolExpr>> unknownToVars;
  private Map<UnknownId, List<IntExpr>> unknownToWeights;
  private Map<UnknownId, Map<MetaClassTree, BoolExpr>> unknownToTreeToVar;
  private Map<BoolExpr, MetaClassTree> varToTree;
  private Map<BoolExpr, Predicate> varToPred;
  private MetaClassTree tree;
  private Set<MetaClassTree> misc;
  private Map<UnknownId, Map<Character, BoolExpr>> unknownToCharToVar;

  private Set<UnknownId> unknownBounds;
  private Map<UnknownId, IntExpr> unknownToMinVar;
  private Map<UnknownId, IntExpr> unknownToMaxVar;
  
  private int holes;

  /*public Formula (List<Set<Route>> positives, List<Set<Route>> negatives) {
    this(positives, negatives, new Diagnostic());
  }*/

	public Formula(List<Set<Route>> positives, List<Set<Route>> negatives, Diagnostic diag) {
		this.positives = positives;
		this.negatives = negatives;
		this.diag = diag;

		// Initialize SAT formula objects
		this.ctx = new Context();
		this.opt = this.ctx.mkOptimize();
		this.model = null;

		// Initialize structures for tracking state
		this.unknownChars = new HashSet<>();
		this.unknownToVars = new HashMap<>();
		this.unknownToWeights = new HashMap<>();
		this.unknownToTreeToVar = new HashMap<>();
		this.varToTree = new HashMap<>();
		this.varToPred = new HashMap<>();
		this.tree = MetaClassTree.initialize();
		this.unknownToCharToVar = new HashMap<>();
		this.misc = new HashSet<>();

		this.unknownBounds = new HashSet<>();
		this.unknownToMinVar = new HashMap<>();
		this.unknownToMaxVar = new HashMap<>();

		// Build a list of all unknown IDs encountered by these automata routes.
		this.getAllRelevantUnknownExits(this.positives);
		this.getAllRelevantUnknownExits(this.negatives);

		IntExpr zero = this.ctx.mkInt(0);
		IntExpr one = this.ctx.mkInt(1);

		Storage.reset();
		Storage.ctx = this.ctx;
		holes = Global.root.collectUnknown();
		if (Storage.unknownBoundCounter > -1)
		    Storage.boundPreds = new IntExpr[Storage.unknownBoundCounter + 1];
		
		// Create all 'H?_max' and 'H?_min' variables for all relevant IDs.
		List<IntExpr> quantCosts = new LinkedList<>();
		for (UnknownId id : this.unknownBounds) {
			UnknownBounds unknown = (UnknownBounds) (id.getUnknown());

			// Create minimum and maximum bound variables.
			IntExpr minVar = this.ctx.mkIntConst(id.toString() + "_min");
			IntExpr maxVar = this.ctx.mkIntConst(id.toString() + "_max");
			int lloc = Storage.idToLoc.get(id);
			Storage.boundPreds[lloc] = minVar;
			Storage.boundPreds[lloc + 1] = maxVar;

			// Associate minimum and maximum bound variables with appropriate
			// ID.
			this.unknownToMinVar.put(id, minVar);
			this.unknownToMaxVar.put(id, maxVar);

			// Force every minimum bound to be <= corresponding maximum bound.
			//this.opt.Assert(this.ctx.mkLe(minVar, maxVar));
			this.opt.Assert(this.ctx.mkGe(minVar, zero));
			this.opt.Assert(this.ctx.mkGe(maxVar, zero));

			if (Global.tutor) {
				this.opt.Assert(this.ctx.mkLe(minVar, ctx.mkInt(2)));
				this.opt.Assert(ctx.mkOr(ctx.mkEq(maxVar, ctx.mkInt(1)), 
						ctx.mkEq(maxVar, ctx.mkInt(Bounds.MAX_BOUND))));
			}
			
			// (declare H0_min_cost Int)
			// (declare H0_max_cost Int)
			// (assert (= H0_min_cost (ite (= H0_min <old minimum>) 0 1)))
			// (assert (= H0_max_cost (ite (= H0_max <old maximum>) 0 1)))
			// (minimize (+ H0_min_cost H0_max_cost))
			if (Global.findMaxSat) {
				int oldMin = unknown.getMin();
				int oldMax = unknown.hasMax() ? unknown.getMax() : Bounds.MAX_BOUND;
	
				IntExpr minCost = this.ctx.mkIntConst(id.toString() + "_min_cost");
				IntExpr maxCost = this.ctx.mkIntConst(id.toString() + "_max_cost");
	
				this.opt.Assert(
						this.ctx.mkEq(minCost, this.ctx.mkITE(this.ctx.mkEq(minVar, this.ctx.mkInt(oldMin)), zero, one)));
	
				this.opt.Assert(
						this.ctx.mkEq(maxCost, this.ctx.mkITE(this.ctx.mkEq(maxVar, this.ctx.mkInt(oldMax)), zero, one)));
	
				quantCosts.add(maxCost);
				quantCosts.add(minCost);
			}
		}

		Global.root.setNullable();
		
		// Build the formula and encode meta-class formulae
		this.encodeRoutes();
		
		if (Global.findMaxSat) {
			/*if (quantCosts.size() > 0) {
				IntExpr[] costArray = quantCosts.toArray(new IntExpr[quantCosts.size()]);
				this.opt.MkMinimize(this.ctx.mkAdd(costArray));
			}*/

			ArithExpr quantCost = null;
			if (quantCosts.size() > 0)
				quantCost = this.ctx.mkAdd(quantCosts.toArray(new IntExpr[quantCosts.size()]));
			
			List<ArithExpr> charCosts = new LinkedList<>();
			for (UnknownId id : this.unknownChars) {
				this.encodeCharClass(id, this.tree);
				charCosts.add(this.encodeCharClassSummation(id));
			}
			
			ArithExpr charCost = null;
			if (charCosts.size() > 0)
				charCost = this.ctx.mkAdd(charCosts.toArray(new IntExpr[charCosts.size()]));
			
			if (quantCost != null) {
		    	this.opt.MkMinimize(quantCost);
		    }
			if (charCost != null) {
		    	this.opt.MkMaximize(charCost);
		    }
			
			/*if (quantCost != null && charCost != null) {
				this.opt.MkMinimize(this.ctx.mkSub(quantCost, charCost));
		    } else if (quantCost != null) {
		    	this.opt.MkMinimize(quantCost);
		    } else if (charCost != null) {
		    	this.opt.MkMaximize(charCost);
		    }*/
			
			/*if (charCosts.size() > 0) {
				ArithExpr[] costArray = charCosts.toArray(new ArithExpr[charCosts.size()]);
				this.opt.MkMaximize(this.ctx.mkAdd(costArray));
			}*/
		}
		
	}

  private void encodeRoutes () {
    for (Set<Route> example : this.positives) {
      this.encodePositiveExample(example);
    }

    for (Set<Route> example : this.negatives) {
      this.encodeNegativeExample(example);
    }
  }

  private void encodePositiveExample (Set<Route> example) {
    BoolExpr whole = null;

    for (Route route : example) {
      BoolExpr charFormula = this.buildPositiveRouteFormula(route);
      BoolExpr quantFormula = this.buildPositiveQuantifierFormula(route);

      // If character and quantifier constraints both exist, AND them together.
      BoolExpr part = null;
      if (charFormula != null && quantFormula != null) {
        part = this.ctx.mkAnd(charFormula, quantFormula);
      } else if (charFormula != null) {
        part = charFormula;
      } else {
        part = quantFormula;
      }

      // If this route didn't produce any constraints on either the accepted
      // characters or the bounds for repetition, skip this route.
      if (part == null) {
        continue;
      }

      // Since the entire example is accepted if at least 1 route is accepted,
      // OR this route's formula with the formulae of all the other routes for
      // this positive example.
      if (whole == null) {
        whole = part;
      } else {
        whole = this.ctx.mkOr(whole, part);
      }
    }

    if (whole != null) {
      this.opt.Add(whole);
    }
  }

  public BoolExpr buildPositiveRouteFormula (Route route) {
    return encodeRoute(route, true);
  }

  public BoolExpr buildPositiveQuantifierFormula (Route route) {
    BoolExpr whole = null;

    for (Map.Entry<UnknownId, Set<Integer>> entry : route.getExits().entrySet()) {
      if (entry.getValue().size() == 0) {
        continue;
      }

      IntNum minCountVal;
      IntNum maxCountVal;
      if (entry.getValue().size() == 0) {
    	  minCountVal = this.ctx.mkInt(0);
    	  maxCountVal = this.ctx.mkInt(0);
      } else {
    	  minCountVal = this.ctx.mkInt(Collections.min(entry.getValue()));
          maxCountVal = this.ctx.mkInt(Collections.max(entry.getValue()));
      }
      IntExpr minVar = unknownToMinVar.get(entry.getKey());
      IntExpr maxVar = unknownToMaxVar.get(entry.getKey());
      BoolExpr part = this.ctx.mkAnd(
        this.ctx.mkOr(Storage.idToNullable.get(entry.getKey()), this.ctx.mkLe(minVar, minCountVal)),
        this.ctx.mkGe(maxVar, maxCountVal));

      if (whole == null) {
        whole = part;
      } else {
        whole = this.ctx.mkAnd(whole, part);
      }
    }

    return whole;
  }

  private void encodeNegativeExample (Set<Route> example) {
    BoolExpr whole = null;

    for (Route route : example) {
      BoolExpr charFormula = this.buildNegativeRouteFormula(route);
      BoolExpr quantFormula = this.buildNegativeQuantifierFormula(route);

      // If character and quantifier both exist, OR them together.
      BoolExpr part = null;
      if (charFormula != null && quantFormula != null) {
        part = this.ctx.mkOr(charFormula, quantFormula);
      } else if (charFormula != null) {
        part = charFormula;
      } else {
        part = quantFormula;
      }

      // If this route didn't produce any constraints on either the accepted
      // characters or the bounds for repetition, skip this route.
      if (part == null) {
        continue;
      }

      // Since the entire example is rejected if no routes are accepted, AND
      // this route's formula with the formula of all other routes for this
      // negative example.
      if (whole == null) {
        whole = part;
      } else {
        whole = this.ctx.mkAnd(whole, part);
      }
    }

    if (whole != null) {
      this.opt.Add(whole);
    }
  }

  public BoolExpr buildNegativeRouteFormula (Route route) {
    return encodeRoute(route, false);
  }

  public BoolExpr buildNegativeQuantifierFormula (Route route) {
    BoolExpr whole = null;

    for (Map.Entry<UnknownId, Set<Integer>> entry : route.getExits().entrySet()) {
      if (entry.getValue().size() == 0) {
        continue;
      }

      IntNum minCountVal;
      IntNum maxCountVal;
      if (entry.getValue().size() == 0) {
    	  minCountVal = this.ctx.mkInt(0);
    	  maxCountVal = this.ctx.mkInt(0);
      } else {
    	  minCountVal = this.ctx.mkInt(Collections.min(entry.getValue()));
          maxCountVal = this.ctx.mkInt(Collections.max(entry.getValue()));
      }
      IntExpr minVar = unknownToMinVar.get(entry.getKey());
      IntExpr maxVar = unknownToMaxVar.get(entry.getKey());
      BoolExpr part = this.ctx.mkOr(
        this.ctx.mkAnd(this.ctx.mkNot(Storage.idToNullable.get(entry.getKey())), 
        		this.ctx.mkGt(minVar, minCountVal)),
        this.ctx.mkLt(maxVar, maxCountVal));

      if (whole == null) {
        whole = part;
      } else {
        whole = this.ctx.mkOr(whole, part); // rp changed
      }
    }

    return whole;
  }

  private BoolExpr encodeRoute (Route route, boolean posFlag) {
    Map<UnknownId, Set<Character>> spans = route.getSpans();
    BoolExpr exprRoute = null;

    for (UnknownId id : spans.keySet()) {
      this.unknownChars.add(id);

      BoolExpr exprChars = this.encodeUnknownInRoute(id, spans.get(id), posFlag);
      
      if (exprRoute == null) {
        exprRoute = exprChars;
      } else if (posFlag) {
        exprRoute = this.ctx.mkAnd(exprRoute, exprChars);
      } else {
        exprRoute = this.ctx.mkOr(exprRoute, exprChars);
      }
    }

    return exprRoute;
  }

  private BoolExpr encodeUnknownInRoute (UnknownId id, Set<Character> chars, boolean posFlag) {
    Set<BoolExpr> vars = new HashSet<>();
    for (Character ch : chars) {
      BoolExpr var = this.encodeChar(id, ch, posFlag);
      vars.add(var);
    }

    return vars
      .stream()
      .filter(v -> v != null)
      .map(v -> posFlag ? v : this.ctx.mkNot(v))
      .reduce(null, (accum, v) -> {
        if (accum == null) {
          return v;
        } else if (posFlag) {
          return this.ctx.mkAnd(accum, v);
        } else {
          return this.ctx.mkOr(accum, v);
        }
      });
  }

  private BoolExpr encodeChar (UnknownId id, char ch, boolean posFlag) {
    MetaClassTree tree = this.tree.incrementTally(id, ch);

    if (tree == null) {
      // Create a new tree and categorize this new tree as "miscellaneous".
      tree = new MetaClassTree(new CharLiteralNode(ch), new SimplePredicate(ch), 1);
      this.misc.add(tree);
      tree.incrementTally(id, ch);
    }

    if (this.unknownToCharToVar.containsKey(id)) {
      BoolExpr var = this.unknownToCharToVar.get(id).get(ch);
      if (var != null) {
        return var;
      }
    } else {
      this.unknownToCharToVar.put(id, new HashMap<>());
    }


    BoolExpr var = this.encodeWeightedConstraint(id, tree);
    this.unknownToCharToVar.get(id).put(ch, var);
    return var;
  }

  private void getAllRelevantUnknownExits (List<Set<Route>> data) {
    for (Set<Route> s : data) {
      for (Route r : s) {
        for (UnknownId id : r.getExits().keySet()) {
          this.unknownBounds.add(id);
        }
      }
    }
  }

  private Set<BoolExpr> encodeCharClass (UnknownId id, MetaClassTree tree) {
    // TODO
    // - call encodeCharClass(branch) which returns the SAT variables created
    //   from the tree's child classes
    // - bind each child class variable to the tree variable in the

    Set<BoolExpr> vars = new HashSet<>();
    for (MetaClassTree branch : tree.getBranches()) {
      vars.addAll(encodeCharClass(id, branch));
    }

    if (tree.isCandidateBranch(id)) {
      Set<BoolExpr> s = new HashSet<>();

      if (vars.size() == 0) {
        s.add(this.unknownToTreeToVar.get(id).get(tree));
      } else {
        s.add(encodeMetaCharClass(id, vars, tree));
      }

      return s;
    }

    return vars;
  }

  private ArithExpr encodeCharClassSummation (UnknownId id) {
    List<IntExpr> weightsList = this.unknownToWeights.get(id);
    IntExpr[] weightsArr = new IntExpr[weightsList.size()];

    for (int i = 0; i < weightsList.size(); i++) {
      weightsArr[i] = weightsList.get(i);
    }

    return this.ctx.mkAdd(weightsArr);
  }

  private String createVariableName (UnknownId id, MetaClassTree tree) {
    String idName = id.toString();
    String ccName = tree.getCharClass().toString();

    ccName = ccName.replace("\\", "slash");
    ccName = ccName.replace(" ", "space");
    ccName = ccName.replace("\\t", "tab");

    return String.format("%s_%s", idName, ccName);
  }

  private void saveWeightForSummation (UnknownId id, IntExpr weight) {
    if (this.unknownToWeights.containsKey(id) == false) {
      this.unknownToWeights.put(id, new LinkedList<>());
    }

    this.unknownToWeights.get(id).add(weight);
  }

  // Register variable with unknown -> variable mapping.
  private void saveVar (UnknownId id, MetaClassTree tree, BoolExpr var) {
    if (this.unknownToVars.containsKey(id) == false) {
      this.unknownToVars.put(id, new HashSet<>());
    }
    this.unknownToVars.get(id).add(var);

    if (this.unknownToTreeToVar.containsKey(id) == false) {
      this.unknownToTreeToVar.put(id, new HashMap<>());
    }
    this.unknownToTreeToVar.get(id).put(tree, var);
  }

  private BoolExpr encodeWeightedConstraint (UnknownId id, MetaClassTree tree) {
    String name = this.createVariableName(id, tree);

    // (declare-fun H1_x_v () Bool)
    BoolExpr var = this.ctx.mkBoolConst(name + "_v");

    // (declare-const H1_x_w Int)
    IntExpr weight = this.ctx.mkIntConst(name + "_w");

    // (assert (ite H1_x_v (= H1_x_w 5) (= H1_x_w 0)))
    int scalar = tree.getSATWeight(id);
    BoolExpr ifTrue = this.ctx.mkEq(weight, this.ctx.mkInt(scalar));
    BoolExpr ifFalse = this.ctx.mkEq(weight, this.ctx.mkInt(0));
    this.opt.Assert((BoolExpr)this.ctx.mkITE(var, ifTrue, ifFalse));

    this.saveVar(id, tree, var);
    this.saveWeightForSummation(id, weight);

    // Register variable with variable -> tree mapping.
    this.varToTree.put(var, tree);

    return var;
  }

  private BoolExpr encodeMetaCharClass (UnknownId id, Set<BoolExpr> vars, MetaClassTree tree) {
    BoolExpr var = this.encodeWeightedConstraint(id, tree);
    BoolExpr relations = vars
      .stream()
      .map(v -> this.ctx.mkOr(this.ctx.mkNot(var), v))
      .reduce(null, (accum, v) -> (accum == null) ? v : this.ctx.mkAnd(accum, v));

    if (relations != null) {
      this.opt.Add(relations);
    }

    return var;
  }

  public void solve () throws SynthesisFailure {
    if (this.diag.getBool("debug-vars")) {
      String header = String.format("\nVAR MAP for %d", this.diag.output().count());
      this.diag.output().printBlock(header);
      for (BoolExpr var : this.varToTree.keySet()) {
        CharClass cc = this.varToTree.get(var).getCharClass();
        String line = String.format("%-10s : '%s'\n", var, cc);
        this.diag.output().printBlock(line);
      }
      this.diag.output().printBreak();
    }

    if (this.diag.getBool("debug-classes")) {
      String header = String.format("\nCLASS MAP for %d", this.diag.output().count());
      this.diag.output().printBlock(header);
      this.diag.output().printBlock(this.tree);
      for (MetaClassTree tree : this.misc) {
        this.diag.output().printBlock(tree);
      }
      this.diag.output().printBreak();
    }

    if (this.diag.getBool("debug-formula")) {
      String header = String.format("\nFORMULA for %d", this.diag.output().count());
      this.diag.output().printBlock(header);
      this.diag.output().printBlock(this.opt.toString());
      this.diag.output().printBreak();
    }

    /**
     * First, check that the formula was satisifed
     */
    diag.timing().startTiming("timeSATSolver");
    Status status = this.opt.Check();
    long duration = diag.timing().stopTimingAndAdd("timeSATSolver");
    RegexNode root = Global.root;
    diag.stat.add(root.toString(), root.descendants(), holes, duration);
    if (status == Status.UNSATISFIABLE) {
      throw new SynthesisFailure("unsatisfiable SAT formula");
    } else {
      // Use the SAT solver to attempt to resolve the variables and their constraints.
      this.model = this.opt.getModel();

      if (this.diag.getBool("debug-model")) {
        String header = String.format("\nMODEL for %d", this.diag.output().count());
        this.diag.output().printBlock(header);
        this.diag.output().printBlock(this.model.toString());
        this.diag.output().printBreak();
      }
    }
  }

  public Map<UnknownId, CharClass> getCharSolutions () throws SynthesisFailure {
    if (this.model == null) {
      throw new IllegalStateException("solutions requested before model was solved");
    }

    Map<UnknownId, CharClass> solutions = new HashMap<>();
    boolean someSolutions = false;
    for (UnknownId id : this.unknownChars) {
      if (hasSomeSolutions(id)) {
        someSolutions = true;
      }

      solutions.put(id, getCharSolution(id));
    }

    /*if (someSolutions == false) {
      throw new SynthesisFailure("model produced no solutions for any unknown");
    }*/

    return solutions;
  }

  private boolean hasSomeSolutions (UnknownId id) {
    for (BoolExpr var : this.unknownToVars.get(id)) {
      MetaClassTree tree = this.varToTree.get(var);
      if (this.model.evaluate(var, false).isTrue()) {
        return true;
      }
    }

    return false;
  }

  private CharClass getCharSolution (UnknownId id) throws SynthesisFailure {
    /**
     * - for each variable determined to be TRUE:
     *   - get that variable's corresponding tree
     *   - find the oldest ancestor of that tree determined to be true
     *   - add that ancestor to the set of solutions
     */
    Map<MetaClassTree, Boolean> treeIsTrue = new HashMap<>();

    // Determine which character classes were evaluated to true for this unknown.
    for (BoolExpr var : this.unknownToVars.get(id)) {
      MetaClassTree tree = this.varToTree.get(var);
      if (this.model.evaluate(var, false).isTrue()) {
        treeIsTrue.put(tree, true);
      }
    }

    // For each true variable associated with the given unknown, find its
    // corresponding tree and find the oldest true ancestor of that tree.
    Set<CharClass> solutions = new HashSet<>();
    for (BoolExpr var : this.unknownToVars.get(id)) {
      MetaClassTree ancestor = this.varToTree.get(var);
      if (Global.findMaxSat) {
        ancestor = ancestor.getFurthestTrueAncestor(treeIsTrue);
      } else {
    	Predicate pred = ancestor.getPred();
    	if (!treeIsTrue.containsKey(ancestor) || !treeIsTrue.get(ancestor) || 
    			pred.equals(MetaClassTree.pred_AZ) || pred.equals(MetaClassTree.pred_az) || 
    			pred.equals(MetaClassTree.pred_d) || pred.equals(MetaClassTree.pred_w)) {
    	  ancestor = null;
    	}
      }
      if (ancestor != null) {
        solutions.add(ancestor.getCharClass());
      }
    }

    if (solutions.size() == 0) {
      return new CharClassSetNode(new CharRangeNode('∅'));
    } if (solutions.size() == 1) {
      CharClass only = solutions.iterator().next();
      if (only instanceof CharRangeNode) {
        return new CharClassSetNode((CharRangeNode)only);
      }

      return only;
    } else {
      Collection<CharRangeNode> subClasses = new HashSet<>();

      for (CharClass subClass : solutions) {
        if (subClass instanceof CharRangeNode) {
          subClasses.add((CharRangeNode)subClass);
        } else if (subClass instanceof ConcreteCharClass) {
          subClasses.add(new CharRangeNode((ConcreteCharClass)subClass));
        } else {
          // TODO
          System.out.println("Could not handle character class");
          System.out.println(subClass);
          System.exit(1);
        }
      }

      return new CharClassSetNode(subClasses);
    }
  }

  public Map<UnknownId, Bounds> getBoundsSolutions () throws SynthesisFailure {
    if (this.model == null) {
      throw new IllegalStateException("solutions requested before model was solved");
    }

    Map<UnknownId, Bounds> solutions = new HashMap<>();
    for (UnknownId id : this.unknownBounds) {
      solutions.put(id, getBoundsSolution(id));
    }
    return solutions;
  }

  private Bounds getBoundsSolution (UnknownId id) throws SynthesisFailure {
    IntExpr minVar = this.unknownToMinVar.get(id);
    IntExpr maxVar = this.unknownToMaxVar.get(id);

    int min = ((IntNum)this.model.evaluate(minVar, false)).getInt();
    int max = ((IntNum)this.model.evaluate(maxVar, false)).getInt();

    return Bounds.between(min, max);
  }

  @Override
  public String toString () {
    return this.opt.toString();
  }
}
