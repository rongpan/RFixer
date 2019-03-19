package edu.wisc.regfixer.synthesize;

import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;

import edu.wisc.regfixer.automata.Route;
import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.enumerate.Enumerant;
import edu.wisc.regfixer.enumerate.Grafter;
import edu.wisc.regfixer.enumerate.Unknown;
import edu.wisc.regfixer.enumerate.UnknownBounds;
import edu.wisc.regfixer.enumerate.UnknownChar;
import edu.wisc.regfixer.enumerate.UnknownId;
import edu.wisc.regfixer.parser.Bounds;
import edu.wisc.regfixer.parser.CharClass;
import edu.wisc.regfixer.parser.CharClassSetNode;
import edu.wisc.regfixer.parser.CharLiteralNode;
import edu.wisc.regfixer.parser.CharRangeNode;
import edu.wisc.regfixer.parser.RegexNode;

public class Synthesis {
  private RegexNode tree;
  private int totalCharLiterals;

  public Synthesis() {
	  
  }
  
  public Synthesis (Enumerant enumerant, List<Set<Route>> positives, List<Set<Route>> negatives) throws SynthesisFailure {
    this(enumerant, positives, negatives, new Diagnostic());
  }

  public Synthesis (Enumerant enumerant, List<Set<Route>> positives, List<Set<Route>> negatives, Diagnostic diag) throws SynthesisFailure {
    Formula formula = new Formula(positives, negatives, diag);
    //diag.timing().startTiming("timeSATSolver");
    formula.solve();
    //diag.timing().stopTimingAndAdd("timeSATSolver");

    Map<UnknownId, CharClass> charSolutions = formula.getCharSolutions();
    Map<UnknownId, Bounds> boundsSolutions = formula.getBoundsSolutions();

    for (UnknownId id : enumerant.getIds()) {
      if (charSolutions.containsKey(id) || boundsSolutions.containsKey(id)) {
        continue;
      }

      if (id.getUnknown() instanceof UnknownChar) {
        charSolutions.put(id, new CharClassSetNode(new CharRangeNode('∅')));
      } else if (id.getUnknown() instanceof UnknownBounds) {
        boundsSolutions.put(id, Bounds.exactly(0));
      }
    }

    RegexNode whole = enumerant.getTree();

    int totalCharLiterals = 0;
    for (Entry<UnknownId, CharClass> solution : charSolutions.entrySet()) {
      CharClass cc = solution.getValue();
      whole = Grafter.silentGraft(whole, solution.getKey(), cc);

      // Count single character literal classes included in the solution.
      if (cc instanceof CharLiteralNode) {
        totalCharLiterals++;
      } else if (cc instanceof CharClassSetNode) {
        for (CharRangeNode cr : ((CharClassSetNode)cc).getSubClasses()) {
          if (cr.isSingle() && cr.getLeftChild() instanceof CharLiteralNode) {
            totalCharLiterals++;
          }
        }
      }
    }

    for (Entry<UnknownId, Bounds> solution : boundsSolutions.entrySet()) {
      whole = Grafter.silentGraft(whole, solution.getKey(), solution.getValue());
    }

    this.tree = whole;
    this.totalCharLiterals = totalCharLiterals;
  }

  public RegexNode getTree () {
    return this.tree;
  }

  public int getFitness () {
    return this.totalCharLiterals;
  }

  public Pattern toPattern (boolean withAnchors) {
    UnknownBounds.setFill(Bounds.exactly(0));

    Pattern p = this.toPattern();
    if (withAnchors) {
      p = Pattern.compile(String.format("^%s$", this.tree));
    }

    UnknownBounds.clearFill();
    return p;
  }

  public Pattern toPattern () {
    UnknownBounds.setFill(Bounds.exactly(0));
    Pattern p = Pattern.compile(this.tree.toString());
    UnknownBounds.clearFill();
    return p;
  }

  @Override
  public String toString () {
    return this.tree.toString();
  }
}
