package edu.wisc.regfixer.synthesize;

/**
 * The SAT weight of a particular character class is determined based on the
 * weight of the character class layer (where classes in the bottom layer have
 * a weight of 2 and each layer up increments the previous layer's weight)
 * minus the sum of all child classes (if no child classes exist, use a sum of
 * 0 is used).
 *
 * A larger character class should only be chosen if choosing the larger class
 * would prevent the use of 2 or more separate character classes. An example:
 *
 * (Tally represents how many characters matched a particular class)
 *
 *   Class   Tally   Layer Weight
 * --------------------------------
 *   \w      6       4
 *   \d      0       3
 *   [a-z]   4       3
 *   [A-Z]   2       3
 *   F       1       2
 *   o       2       2
 *   B       1       2
 *   a       1       2
 *   r       1       2
 *
 *
 * In this case, the following classes would all match these inputs:
 *
 *   Class
 * ------------
 *   \w
 *   [a-zA-Z]
 *   [FooBar]
 *
 *
 * But which resultant class is preferred? The algorithm will prefer the \w
 * class because it can be used instead of the 2 seperate character classes
 * present in the second option and the 5 seperate classes present in the
 * third option.
 *
 * However, in the following example the \d class will be preferred over the
 * \w class because the using the \w class does not simplify the resultant
 * class as in the first example.
 *
 *   Class   Tally   Layer Weight
 * --------------------------------
 *   \w      3       4
 *   \d      3       3
 *   1       1       2
 *   2       1       2
 *   3       1       2
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.wisc.regfixer.enumerate.UnknownId;
import edu.wisc.regfixer.parser.CharClass;
import edu.wisc.regfixer.parser.CharClassSetNode;
import edu.wisc.regfixer.parser.CharEscapedNode;
import edu.wisc.regfixer.parser.CharLiteralNode;
import edu.wisc.regfixer.parser.CharRangeNode;
import edu.wisc.regfixer.util.PrintableTree;

public class MetaClassTree extends PrintableTree {
	
  private static Predicate pred_underscore = new SimplePredicate('_');
  public static Predicate pred_d = new SimplePredicate('0', '9');
  private static Predicate pred_D = new CompoundPredicate(false,
    new SimplePredicate('0', '9'));
  public static Predicate pred_w = new CompoundPredicate(
    new SimplePredicate('_'),
    new SimplePredicate('A', 'Z'),
    new SimplePredicate('a', 'z'),
    new SimplePredicate('0', '9'));
  private static Predicate pred_W = new CompoundPredicate(false,
    new SimplePredicate('_'),
    new SimplePredicate('A', 'Z'),
    new SimplePredicate('a', 'z'),
    new SimplePredicate('0', '9'));
  public static Predicate pred_az = new SimplePredicate('a', 'z');
  public static Predicate pred_AZ = new SimplePredicate('A', 'Z');
  private static Predicate pred_azAZ = new CompoundPredicate(
    new SimplePredicate('a', 'z'),
    new SimplePredicate('A', 'Z'));

  private static CharClass class_underscore = new CharLiteralNode('_');
  private static CharClass class_d = new CharEscapedNode('d');
  private static CharClass class_D = new CharEscapedNode('D');
  private static CharClass class_w = new CharEscapedNode('w');
  private static CharClass class_W = new CharEscapedNode('W');
  private static CharRangeNode class_az = new CharRangeNode('a', 'z');
  private static CharRangeNode class_AZ = new CharRangeNode('A', 'Z');
  private static CharClass class_azAZ = new CharClassSetNode(class_az, class_AZ);

  private CharClass cc;
  private Predicate pred;
  private int layer;
  private MetaClassTree parent;
  private List<MetaClassTree> children;
  private Map<UnknownId, Integer> tally;

  public MetaClassTree (CharClass cc, Predicate pred, int layer, MetaClassTree... children) {
    this(cc, pred, layer, Arrays.asList(children));
  }

  public MetaClassTree (CharClass cc, Predicate pred, int layer, List<MetaClassTree> children) {
    this.cc       = cc;
    this.pred     = pred;
    this.layer    = layer;
    this.parent   = null;
    this.children = children;
    this.tally    = new HashMap<>();

    for (MetaClassTree child : this.children) {
      child.parent = this;
    }
  }

  public static int LEAF_WEIGHT = 3;

  public static int layerWeight (int layer) {
    int weight = 0;

    for (int i = 0; i < layer; i++) {
      if (i == 0) {
        weight = MetaClassTree.LEAF_WEIGHT;
      } else {
        weight = (2 * weight) - 1;
      }
    }

    return weight;
  }

  public CharClass getCharClass () {
    return this.cc;
  }

  public Predicate getPred () {
    return this.pred;
  }

  public List<PrintableTree> getChildren () {
    List<PrintableTree> cast = new LinkedList<>();
    for (MetaClassTree child : this.children) {
      if (child.tally.size() > 0) {
        cast.add(child);
      }
    }
    return cast;
  }

  public MetaClassTree getFurthestTrueAncestor (Map<MetaClassTree, Boolean> evaluation) {
    MetaClassTree ancestor = null;
    if (this.parent != null) {
      ancestor = this.parent.getFurthestTrueAncestor(evaluation);
    }

    Boolean isTrue = evaluation.get(this);

    if (ancestor != null) {
      return ancestor;
    } else if (isTrue != null && isTrue) {
      return this;
    } else {
      return null;
    }
  }

  public List<MetaClassTree> getBranches () {
    return this.children;
  }

  public int getSATWeight (UnknownId id) {
    int totalChildren = 0;
    for (MetaClassTree child : this.children) {
      if (child.tally.containsKey(id)) {
        totalChildren++;
      }
    }

    int childWeight = MetaClassTree.layerWeight(this.layer - 1);
    int treeWeight = MetaClassTree.layerWeight(this.layer);
    return (totalChildren * childWeight) - treeWeight;
  }

  private boolean isSatisfied (char ch) {
    return this.pred.includes(ch);
  }

  public int getTally (UnknownId id) {
    return (this.tally.containsKey(id)) ? this.tally.get(id) : 0;
  }

  public MetaClassTree incrementTally (UnknownId id, char ch) {
    if (this.isSatisfied(ch) == false) {
      return null;
    }

    this.tally.put(id, this.getTally(id) + 1);

    for (MetaClassTree child : this.children) {
      MetaClassTree match = child.incrementTally(id, ch);
      if (match != null) {
        return match;
      }
    }

    return this;
  }

  public boolean isCandidateBranch (UnknownId id) {
    /**
     * Literal-classes should only be considered for inclusion in the SAT
     * formula iff:
     * - Class has more than 0 tallies
     */
    if (this.getTally(id) > 0) {
      return true;
    }

    return false;
  }

  public String toString () {
    String out = String.format("'%s'", this.cc.toString());

    for (Map.Entry<UnknownId, Integer> entry : this.tally.entrySet()) {
      out += String.format("\n%s %d (%s)", entry.getKey(), entry.getValue(), this.getSATWeight(entry.getKey()));
    }

    return out;
  }

  public static MetaClassTree[] createMetaClassRange (char low, char high, int level) {
    int total = high - low + 1;
    MetaClassTree[] out = new MetaClassTree[total];

    int i = 0;
    for (char c = low; c <= high; c++) {
      CharLiteralNode cc = new CharLiteralNode(c);
      SimplePredicate pred = new SimplePredicate(c);
      out[i++] = new MetaClassTree(cc, pred, level);
    }

    return out;
  }

  public static MetaClassTree initialize () {
    MetaClassTree[] children_d  = createMetaClassRange('0', '9', 1);
    MetaClassTree[] children_az = createMetaClassRange('a', 'z', 1);
    MetaClassTree[] children_AZ = createMetaClassRange('A', 'Z', 1);

    MetaClassTree cls_underscore = new MetaClassTree(class_underscore, pred_underscore, 1);
    MetaClassTree cls_d  = new MetaClassTree(class_d,  pred_d,  2, children_d);
    MetaClassTree cls_az = new MetaClassTree(class_az, pred_az, 2, children_az);
    MetaClassTree cls_AZ = new MetaClassTree(class_AZ, pred_AZ, 2, children_AZ);

    MetaClassTree cls_w  = new MetaClassTree(class_w,  pred_w,  3, cls_underscore, cls_d, cls_az, cls_AZ);

    return cls_w;
  }
}
