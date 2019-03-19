package edu.wisc.regfixer.enumerate;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import edu.wisc.regfixer.parser.Bounds;
import edu.wisc.regfixer.parser.CharClass;
import edu.wisc.regfixer.parser.ConcatNode;
import edu.wisc.regfixer.parser.OptionalNode;
import edu.wisc.regfixer.parser.PlusNode;
import edu.wisc.regfixer.parser.RegexNode;
import edu.wisc.regfixer.parser.RepetitionNode;
import edu.wisc.regfixer.parser.StarNode;
import edu.wisc.regfixer.parser.UnionNode;

/**
 * The Grafter class takes as input a regular expression syntax tree, the ID of
 * an unknown within that tree to find, and some Object to replace that unknown
 * with.
 */
public class Grafter {
  private static boolean checkForbiddenExpansions = false;
  private static boolean replaceAncestorQuantifiers = false;
  public static List<UnknownId> addedBounds = new LinkedList<>();

  public static RegexNode silentGraft (RegexNode original, UnknownId id, Object scion) {
    try {
      Grafter.checkForbiddenExpansions = false;
      RegexNode graftee = graft(original, id, scion);
      Grafter.checkForbiddenExpansions = true;
      return graftee;
    } catch (ForbiddenExpansionException ex) {
      Grafter.checkForbiddenExpansions = true;
      return null;
    }
  }

  public static RegexNode graftWithUnknownAncestors (RegexNode original, UnknownId id, Object scion) throws ForbiddenExpansionException {
    Grafter.replaceAncestorQuantifiers = true;
    Grafter.addedBounds = new LinkedList<>();
    RegexNode graftee = graft(original, id, scion);
    Grafter.replaceAncestorQuantifiers = false;
    return graftee;
  }

  public static RegexNode graft (RegexNode original, UnknownId id, Object scion) throws ForbiddenExpansionException {
    return graftNode(original, id, scion);
  }

  private static RegexNode graftNode (RegexNode node, UnknownId id, Object scion) throws ForbiddenExpansionException {
         if (node instanceof UnknownChar)    { return graftUnknown((UnknownChar) node, id, scion); }
    else if (node instanceof ConcatNode)     { return graftConcat((ConcatNode) node, id, scion); }
    else if (node instanceof UnionNode)      { return graftUnion((UnionNode) node, id, scion); }
    else if (node instanceof RepetitionNode) { return graftRepetition((RepetitionNode) node, id, scion); }
    else if (node instanceof OptionalNode)   { return graftOptional((OptionalNode) node, id, scion); }
    else if (node instanceof StarNode)       { return graftStar((StarNode) node, id, scion); }
    else if (node instanceof PlusNode)       { return graftPlus((PlusNode) node, id, scion); }
    else if (node instanceof CharClass)      { return graftAtom(node); }
    else {
      System.err.printf("Unknown AST class: %s\n", node.getClass().getName());
      System.exit(1);
      return null;
    }
  }

  private static RegexNode graftUnknown (UnknownChar node, UnknownId id, Object scion) {
    if (node.getId().equals(id)) {
      if (scion instanceof RegexNode) {
        return (RegexNode) scion;
      } else {
        String msg = String.format("cannot graft '%s'", scion);
        throw new IllegalArgumentException(msg);
      }
    } else {
      return node;
    }
  }

  private static RegexNode graftConcat (ConcatNode node, UnknownId id, Object scion) throws ForbiddenExpansionException {
    List<RegexNode> children = node.getChildren();
    List<RegexNode> newChildren = new LinkedList<>(children);
    boolean childrenNoChange = true;

    for (int i = 0; i < children.size(); i++) {
      RegexNode graftee = graftNode(children.get(i), id, scion);

      if (graftee != children.get(i)) {
        childrenNoChange = false;
        newChildren.set(i, graftee);

        if (scion instanceof ConcatNode && i > 0 && children.get(i - 1) instanceof UnknownChar) {
          if (checkForbiddenExpansions) {
            throw new ForbiddenExpansionException("non-first child of concat cannot be expanded with concat");
          }
        }
      }
    }

    if (childrenNoChange) {
      return node;
    } else {
      return new ConcatNode(newChildren);
    }
  }

  private static RegexNode graftUnion (UnionNode node, UnknownId id, Object scion) throws ForbiddenExpansionException {
    if (scion instanceof UnionNode) {
      if (node.getRightChild() instanceof UnknownChar) {
        if (((UnknownChar)node.getRightChild()).getId() == id) {
          if (checkForbiddenExpansions) {
            throw new ForbiddenExpansionException("right side of union cannot be expanded with union");
          }
        }
      }
    }

    RegexNode leftGraftee  = graftNode(node.getLeftChild(), id, scion);
    RegexNode rightGraftee = graftNode(node.getRightChild(), id, scion);

    boolean leftNoChange  = (leftGraftee == node.getLeftChild());
    boolean rightNoChange = (rightGraftee == node.getRightChild());
    if (leftNoChange && rightNoChange) {
      return node;
    } else {
      UnionNode union = new UnionNode(leftGraftee, rightGraftee, node.isSynthetic());

      if (leftGraftee.descendants() < rightGraftee.descendants() && node.isSynthetic()) {
        if (checkForbiddenExpansions) {
          throw new ForbiddenExpansionException("right side of union cannot have > nodes than left side");
        }
      }

      return union;
    }
  }

  private static RegexNode graftRepetition (RepetitionNode node, UnknownId id, Object scion) throws ForbiddenExpansionException {
    if (node.getBounds() instanceof UnknownBounds && scion instanceof Bounds) {
      UnknownBounds unknown = (UnknownBounds)node.getBounds();
      if (unknown.getId().equals(id)) {
        return new RepetitionNode(node.getChild(), (Bounds)scion);
      }
    }

    RegexNode graftee = graftNode(node.getChild(), id, scion);

    if (graftee == node.getChild()) {
      return node;
    } else if (replaceAncestorQuantifiers) {
      UnknownBounds bounds = new UnknownBounds(node.getBounds());
      addedBounds.add(bounds.getId());
      return new RepetitionNode(graftee, bounds);
    } else {
      return new RepetitionNode(graftee, node.getBounds());
    }
  }

  private static RegexNode graftOptional (OptionalNode node, UnknownId id, Object scion) throws ForbiddenExpansionException {
    RegexNode graftee = graftNode(node.getChild(), id, scion);

    if (graftee == node.getChild()) {
      return node;
    } else if (replaceAncestorQuantifiers) {
      UnknownBounds bounds = new UnknownBounds(Bounds.between(0, 1));
      addedBounds.add(bounds.getId());
      return new RepetitionNode(graftee, bounds);
    } else {
      return new OptionalNode(graftee);
    }
  }

  private static RegexNode graftStar (StarNode node, UnknownId id, Object scion) throws ForbiddenExpansionException {
    RegexNode graftee = graftNode(node.getChild(), id, scion);

    if (graftee == node.getChild()) {
      return node;
    } else if (replaceAncestorQuantifiers) {
      UnknownBounds bounds = new UnknownBounds(Bounds.atLeast(0));
      addedBounds.add(bounds.getId());
      return new RepetitionNode(graftee, bounds);
    } else {
      return new StarNode(graftee);
    }
  }

  private static RegexNode graftPlus (PlusNode node, UnknownId id, Object scion) throws ForbiddenExpansionException {
    RegexNode graftee = graftNode(node.getChild(), id, scion);

    if (graftee == node.getChild()) {
      return node;
    } else if (replaceAncestorQuantifiers) {
      UnknownBounds bounds = new UnknownBounds(Bounds.atLeast(1));
      addedBounds.add(bounds.getId());
      return new RepetitionNode(graftee, bounds);
    } else {
      return new PlusNode(graftee);
    }
  }

  private static RegexNode graftAtom (RegexNode node) {
    return node;
  }
}
