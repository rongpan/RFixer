package edu.wisc.regfixer.enumerate;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.wisc.regfixer.enumerate.Expansion;
import edu.wisc.regfixer.parser.Bounds;
import edu.wisc.regfixer.parser.CharClass;
import edu.wisc.regfixer.parser.ConcatNode;
import edu.wisc.regfixer.parser.OptionalNode;
import edu.wisc.regfixer.parser.PlusNode;
import edu.wisc.regfixer.parser.RegexNode;
import edu.wisc.regfixer.parser.RepetitionNode;
import edu.wisc.regfixer.parser.StarNode;
import edu.wisc.regfixer.parser.UnionNode;

public class Expander {
	
	private static Map<UnknownId, Expansion> expanTypes = new LinkedHashMap<>();

	private static RegexNode simplify(RegexNode node) {
		
	    if (node instanceof ConcatNode)          { return simplifyConcat((ConcatNode) node); }
	    else if (node instanceof UnionNode)      { return simplifyUnion((UnionNode) node); }
	    else if (node instanceof RepetitionNode) { return simplifyRepetition((RepetitionNode) node); }
	    else if (node instanceof OptionalNode)   { return simplifyOptional((OptionalNode) node); }
	    else if (node instanceof StarNode)       { return simplifyStar((StarNode) node); }
	    else if (node instanceof PlusNode)       { return simplifyPlus((PlusNode) node); }
	    else if (node instanceof CharClass)      { return node; }
	    else if (node instanceof UnknownChar)      { return node; }
	    else {
	        System.err.printf("Unknown AST class: %s\n", node.getClass().getName());
	        System.exit(1);
	        return null;
	    }
    }
	
	private static RegexNode simplifyConcat(ConcatNode node) {
	    
	    List<RegexNode> children = node.getChildren();
	    int totalChildren = children.size();
	    // this should not happen
	    if (totalChildren == 0)
	    	return null;
	    if (totalChildren == 1)
	    	return children.get(0);
	    List<RegexNode> newChildren = new LinkedList<>();
	    for (int i = 0; i < totalChildren; i++) {
	    	newChildren.add(simplify(children.get(i)));
	    }
	    return new ConcatNode(newChildren);
	}
	
	private static RegexNode simplifyUnion (UnionNode node) {
	    
	    RegexNode left = node.getLeftChild();
	    RegexNode newLeft = simplify(left);
	    RegexNode right = node.getRightChild();
	    RegexNode newRight = simplify(right);
	    
	    return new UnionNode(newLeft, newRight);
	}
	
	private static RegexNode simplifyRepetition (RepetitionNode node) {
	    
	    RegexNode child = node.getChild();
	    RegexNode newChild = simplify(child);
	    Bounds bounds = node.getBounds();
	    
	    return new RepetitionNode(newChild, bounds);
	}
	
	private static RegexNode simplifyOptional (OptionalNode node) {
	    
	    RegexNode child = node.getChild();
	    RegexNode newChild = simplify(child);
	    return new OptionalNode(newChild);
	}
	
	private static RegexNode simplifyStar (StarNode node) {
	    
	    RegexNode child = node.getChild();
	    RegexNode newChild = simplify(child);
	    return new StarNode(newChild);
	}
	
	private static RegexNode simplifyPlus (PlusNode node) {
	    
		RegexNode child = node.getChild();
	    RegexNode newChild = simplify(child);
	    return new PlusNode(newChild);
	}
	
	public static Set<Enumerant> add(RegexNode ori) {
		ori = simplify(ori);
		Set<UnknownId> ids = new LinkedHashSet<>();
		int cost = 0;
		Expansion expansion  = null;
		
		Map<RegexNode, UnknownId> newNodes = addOriginal(ori);
		Set<Enumerant> res = new LinkedHashSet<>();
		for (Map.Entry<RegexNode, UnknownId> node: newNodes.entrySet()) {
			Set<UnknownId> newId = new LinkedHashSet<>(ids);
			newId.add(node.getValue());
			res.add(new Enumerant(node.getKey(), newId, cost + 1, expansion));
		}
		return res;
	}
	
	public static Set<Enumerant> addOriginal(Enumerant enumerant) {
		Set<UnknownId> ids = enumerant.getIds();
		int cost = enumerant.getCost();
		//Expansion expansion  = enumerant.getLatestExpansion();
		
		Map<RegexNode, UnknownId> newNodes = addOriginal(enumerant.getTree());
		Set<Enumerant> res = new LinkedHashSet<>();
		for (Map.Entry<RegexNode, UnknownId> node: newNodes.entrySet()) {
			Set<UnknownId> newId = new LinkedHashSet<>(ids);
			newId.add(node.getValue());
			res.add(new Enumerant(node.getKey(), newId, cost + 1, null));
		}
		return res;
	}
	
	public static Map<RegexNode, UnknownId> addOriginal(RegexNode node) {
		
		node.toBinary();
		
	    if (node instanceof ConcatNode)          { return oriConcat((ConcatNode) node); }
	    else if (node instanceof UnionNode)      { return oriUnion((UnionNode) node); }
	    else if (node instanceof RepetitionNode) { return oriRepetition((RepetitionNode) node); }
	    else if (node instanceof OptionalNode)   { return oriOptional((OptionalNode) node); }
	    else if (node instanceof StarNode)       { return oriStar((StarNode) node); }
	    else if (node instanceof PlusNode)       { return oriPlus((PlusNode) node); }
	    else if (node instanceof CharClass)      { return oriAtomic((CharClass) node); }
	    else if (node instanceof UnknownChar)      { return new LinkedHashMap<RegexNode, UnknownId>(); }
	    else {
	        System.err.printf("Unknown AST class: %s\n", node.getClass().getName());
	        System.exit(1);
	        return null;
	    }
    }
	
	private static Map<RegexNode, UnknownId> oriConcat (ConcatNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    List<RegexNode> children = node.getChildren();
	    int totalChildren = children.size();
	    if (totalChildren == 0)
	    	return partials;
	    for (int i = 0; i < totalChildren; i++) {
	    	List<RegexNode> newChildren = new LinkedList<>();
	    	//System.err.println("concat child is " + i + " " + children.get(i));
	    	for (int j = 0; j < totalChildren; j++) {
	    		if (j != i)
	    			newChildren.add(children.get(j));
	    	}
	    	Map<RegexNode, UnknownId> newChild = addOriginal(children.get(i));
	    	for (Map.Entry<RegexNode, UnknownId> child : newChild.entrySet()) {
	    		//System.err.println("concat new child is " + child);
	    		List<RegexNode> newChildrenCopy = new LinkedList<>(newChildren);
	    		newChildrenCopy.add(i, child.getKey());
	    		partials.put(new ConcatNode(newChildrenCopy), child.getValue());
	    	}
	    }
	    return partials;
	}

	private static Map<RegexNode, UnknownId> oriUnion (UnionNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode left = node.getLeftChild();
	    Map<RegexNode, UnknownId> newLeft = addOriginal(left);
	    RegexNode right = node.getRightChild();
	    Map<RegexNode, UnknownId> newRight = addOriginal(right);
	    
	    if (newLeft.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newL : newLeft.entrySet()) {
	    		partials.put(new UnionNode(newL.getKey(), right), newL.getValue());
	    	}
	    }
	    if (newRight.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newR : newRight.entrySet()) {
	    		partials.put(new UnionNode(left, newR.getKey()), newR.getValue());
	    	}
	    }
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> oriRepetition (RepetitionNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode child = node.getChild();
	    Map<RegexNode, UnknownId> newChild = addOriginal(child);
	    Bounds bounds = node.getBounds();
	    
	    if (newChild.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newC : newChild.entrySet()) {
	    		partials.put(new RepetitionNode(newC.getKey(), bounds), newC.getValue());
	    	}
	    }
	    if (!(bounds instanceof UnknownBounds)) {
	    	UnknownBounds newBound = new UnknownBounds();
	    	partials.put(new RepetitionNode(child, newBound), newBound.getId());
	    }
	    
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> oriOptional (OptionalNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode child = node.getChild();
	    Map<RegexNode, UnknownId> newChild = addOriginal(child);
	    if (newChild.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newC : newChild.entrySet()) {
	    		partials.put(new OptionalNode(newC.getKey()), newC.getValue());
	    	}
	    }
	    partials.putAll(mkRepetitionNode(child, Bounds.between(0, 1)));
	    
	    return partials;
	}

	private static Map<RegexNode, UnknownId> oriStar (StarNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode child = node.getChild();
	    Map<RegexNode, UnknownId> newChild = addOriginal(child);
	    if (newChild.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newC : newChild.entrySet()) {
	    		partials.put(new StarNode(newC.getKey()), newC.getValue());
	    	}
	    }
	    partials.putAll(mkRepetitionNode(child, Bounds.atLeast(0)));
	    
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> oriPlus (PlusNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode child = node.getChild();
	    Map<RegexNode, UnknownId> newChild = addOriginal(child);
	    if (newChild.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newC : newChild.entrySet()) {
	    		partials.put(new PlusNode(newC.getKey()), newC.getValue());
	    	}
	    }
	    partials.putAll(mkRepetitionNode(child, Bounds.atLeast(1)));
	    
	    return partials;
	}

	private static Map<RegexNode, UnknownId> oriAtomic (CharClass node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
		UnknownChar newChar = new UnknownChar();
	    partials.put(newChar, newChar.getId());
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> mkRepetitionNode (RegexNode child, Bounds original) {
	    UnknownBounds unknown = new UnknownBounds(original);
	
	    RepetitionNode branch = new RepetitionNode(child, unknown);
	
	    Map<RegexNode, UnknownId> res = new LinkedHashMap<RegexNode, UnknownId>();
	    res.put(branch, unknown.getId());
	    return res;
	}
	
	public static Set<Enumerant> reduce(Enumerant enumerant) {
		Set<UnknownId> ids = enumerant.getIds();
		int cost = enumerant.getCost();
		//Expansion expansion  = enumerant.getLatestExpansion();
		
		Map<RegexNode, UnknownId> newNodes = reduce(enumerant.getTree());
		Set<Enumerant> res = new LinkedHashSet<>();
		for (Map.Entry<RegexNode, UnknownId> node: newNodes.entrySet()) {
			Set<UnknownId> newId = new LinkedHashSet<>(ids);
			if (node.getValue() != null)
				newId.remove(node.getValue());
			res.add(new Enumerant(node.getKey(), newId, cost + 1, null));
		}
		return res;
	}
	
	
	public static Map<RegexNode, UnknownId> reduce(RegexNode node) {
		
		node.toBinary();
		
		if (node instanceof ConcatNode)          { return reduceConcat((ConcatNode) node); }
	    else if (node instanceof UnionNode)      { return reduceUnion((UnionNode) node); }
	    else if (node instanceof RepetitionNode) { return reduceRepetition((RepetitionNode) node); }
	    else if (node instanceof OptionalNode)   { return reduceOptional((OptionalNode) node); }
	    else if (node instanceof StarNode)       { return reduceStar((StarNode) node); }
	    else if (node instanceof PlusNode)       { return reducePlus((PlusNode) node); }
	    else if (node instanceof CharClass)      { return reduceAtomic((CharClass) node); }
	    else if (node instanceof UnknownChar)      { return new LinkedHashMap<RegexNode, UnknownId>(); }
	    else {
	        System.err.printf("Unknown AST class: %s\n", node.getClass().getName());
	        System.exit(1);
	        return null;
	    }
	}
	
	private static Map<RegexNode, UnknownId> reduceConcat (ConcatNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    List<RegexNode> children = node.getChildren();
	    int totalChildren = children.size();
	    if (totalChildren == 0)
	    	return partials;
	    if (totalChildren == 1) {
	    	if (children.get(0) instanceof UnknownChar)
	    		partials.put(children.get(0), null);
	    	return partials;
	    }
	    for (int i = 0; i < totalChildren - 1; i++) {
	    	List<RegexNode> newChildren = new LinkedList<>();
	    	
	    	RegexNode first = children.get(i);
	    	Map<RegexNode, UnknownId> reducedFirst = reduce(first);
	    	if (reducedFirst.size() != 0) {
	    		for (int j = 0; j < totalChildren; j++) {
		    		if (j != i)
		    			newChildren.add(children.get(j));
		    	}
	    		for (Map.Entry<RegexNode, UnknownId> child : reducedFirst.entrySet()) {
	    			List<RegexNode> newChildrenCopy = new LinkedList<>(newChildren);
		    		newChildrenCopy.add(i, child.getKey());
		    		partials.put(new ConcatNode(newChildrenCopy), child.getValue());
	    		}
	    	} else {
	    		RegexNode second = children.get(i + 1);
		    	
		    	if (first instanceof UnknownChar && second instanceof UnknownChar) {
		    		for (int j = 0; j < totalChildren; j++) {
			    		if (j != i + 1)
			    			newChildren.add(children.get(j));
			    	}
		    		partials.put(new ConcatNode(newChildren), ((UnknownChar) second).getId());
		    	}
	    	}
	    }
	    RegexNode last = children.get(totalChildren - 1);
	    Map<RegexNode, UnknownId> reducedLast = reduce(last);
    	if (reducedLast.size() != 0) {
    		List<RegexNode> newChildren = new LinkedList<>();
    		for (int j = 0; j < totalChildren - 1; j++) {
	    		newChildren.add(children.get(j));
	    	}
    		for (Map.Entry<RegexNode, UnknownId> child : reducedLast.entrySet()) {
    			List<RegexNode> newChildrenCopy = new LinkedList<>(newChildren);
	    		newChildrenCopy.add(child.getKey());
	    		partials.put(new ConcatNode(newChildrenCopy), child.getValue());
    		}
    	}
	    
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> reduceUnion (UnionNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode left = node.getLeftChild();
	    RegexNode right = node.getRightChild();
	    
	    Map<RegexNode, UnknownId> reducedLeft = reduce(left);
    	if (reducedLeft.size() != 0) {
    		for (Map.Entry<RegexNode, UnknownId> newL : reducedLeft.entrySet()) {
	    		partials.put(new UnionNode(newL.getKey(), right), newL.getValue());
    		}
    	} else {
	    	if (left instanceof UnknownChar && right instanceof UnknownChar) {
	    		partials.put(left, ((UnknownChar) right).getId());
	    	}
    	}
	    
	    Map<RegexNode, UnknownId> reducedRight = reduce(right);
    	if (reducedRight.size() != 0) {
    		for (Map.Entry<RegexNode, UnknownId> newR : reducedRight.entrySet()) {
	    		partials.put(new UnionNode(left, newR.getKey()), newR.getValue());
    		}
    	}
    	
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> reduceRepetition (RepetitionNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode child = node.getChild();
	    Map<RegexNode, UnknownId> newChild = reduce(child);
	    Bounds bounds = node.getBounds();
	    
	    if (newChild.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newC : newChild.entrySet()) {
	    		partials.put(new RepetitionNode(newC.getKey(), bounds), newC.getValue());
	    	}
	    //changed
	    } else if (child instanceof UnknownChar || bounds instanceof UnknownBounds) {
	    	if (child instanceof UnknownChar)
	    		partials.put(child, ((UnknownChar) child).getId());
	    		//partials.put(child, ((UnknownBounds) bounds).getId());
	    	else {
	    		UnknownChar uchar = new UnknownChar();
	    		partials.put(uchar, ((UnknownBounds) bounds).getId());
	    	}
	    }
	    
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> reduceOptional (OptionalNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode child = node.getChild();
	    Map<RegexNode, UnknownId> newChild = reduce(child);
	    if (newChild.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newC : newChild.entrySet()) {
	    		partials.put(new OptionalNode(newC.getKey()), newC.getValue());
	    	}
	    }
	    
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> reduceStar (StarNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode child = node.getChild();
	    Map<RegexNode, UnknownId> newChild = reduce(child);
	    if (newChild.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newC : newChild.entrySet()) {
	    		partials.put(new StarNode(newC.getKey()), newC.getValue());
	    	}
	    }
	    
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> reducePlus (PlusNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode child = node.getChild();
	    //System.err.println("plus child is " + child);
	    Map<RegexNode, UnknownId> newChild = reduce(child);
	    if (newChild.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newC : newChild.entrySet()) {
	    		//System.err.println("plus newC is " + newC);
	    		partials.put(new PlusNode(newC.getKey()), newC.getValue());
	    	}
	    }
	    
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> reduceAtomic (CharClass node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    return partials;
	}
	
	public static Set<Enumerant> expand(Enumerant enumerant) {
		Set<UnknownId> ids = enumerant.getIds();
		int cost = enumerant.getCost();
		
		Map<RegexNode, UnknownId> newNodes = expand(enumerant.getTree());
		Set<Enumerant> res = new LinkedHashSet<>();
		for (Map.Entry<RegexNode, UnknownId> node: newNodes.entrySet()) {
			Set<UnknownId> newId = new LinkedHashSet<>(ids);
			newId.add(node.getValue());
			Expansion expansion = expanTypes.get(node.getValue());
			expanTypes.remove(node.getValue());
			res.add(new Enumerant(node.getKey(), newId, cost + 1, expansion));
			//System.out.println("added enumerant: " + node.getKey().toString() 
				//	+ " expansion: " + expansion);
		}
		return res;
	}
	
	public static Map<RegexNode, UnknownId> expand(RegexNode node) {
		
		node.toBinary();
		
	    if (node instanceof ConcatNode)          { return expandConcat((ConcatNode) node); }
	    else if (node instanceof UnionNode)      { return expandUnion((UnionNode) node); }
	    else if (node instanceof RepetitionNode) { return expandRepetition((RepetitionNode) node); }
	    else if (node instanceof OptionalNode)   { return expandOptional((OptionalNode) node); }
	    else if (node instanceof StarNode)       { return expandStar((StarNode) node); }
	    else if (node instanceof PlusNode)       { return expandPlus((PlusNode) node); }
	    else if (node instanceof CharClass)      { return expandAtomic((CharClass) node); }
	    else if (node instanceof UnknownChar)      { return expandUnknownChar((UnknownChar) node); }
	    else {
	        System.err.printf("Unknown AST class: %s\n", node.getClass().getName());
	        System.exit(1);
	        return null;
	    }
    }
	
	private static Map<RegexNode, UnknownId> expandConcat (ConcatNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    List<RegexNode> children = node.getChildren();
	    int totalChildren = children.size();
	    if (totalChildren == 0)
	    	return partials;
	    for (int i = 0; i < totalChildren; i++) {
	    	List<RegexNode> newChildren = new LinkedList<>();
	    	for (int j = 0; j < totalChildren; j++) {
	    		if (j != i)
	    			newChildren.add(children.get(j));
	    	}
	    	Map<RegexNode, UnknownId> newChild = expand(children.get(i));
	    	for (Map.Entry<RegexNode, UnknownId> child : newChild.entrySet()) {
	    		List<RegexNode> newChildrenCopy = new LinkedList<>(newChildren);
	    		newChildrenCopy.add(i, child.getKey());
	    		partials.put(new ConcatNode(newChildrenCopy), child.getValue());
	    	}
	    }
	    
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> expandUnion (UnionNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode left = node.getLeftChild();
	    Map<RegexNode, UnknownId> newLeft = expand(left);
	    RegexNode right = node.getRightChild();
	    Map<RegexNode, UnknownId> newRight = expand(right);
	    
	    if (newLeft.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newL : newLeft.entrySet()) {
	    		partials.put(new UnionNode(newL.getKey(), right), newL.getValue());
	    	}
	    }
	    if (newRight.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newR : newRight.entrySet()) {
	    		partials.put(new UnionNode(left, newR.getKey()), newR.getValue());
	    	}
	    }
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> expandRepetition (RepetitionNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode child = node.getChild();
	    Map<RegexNode, UnknownId> newChild = expand(child);
	    Bounds bounds = node.getBounds();
	    
	    if (newChild.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newC : newChild.entrySet()) {
	    		partials.put(new RepetitionNode(newC.getKey(), bounds), newC.getValue());
	    	}
	    }
	    /*if (!(bounds instanceof UnknownBounds)) {
	    	UnknownBounds newBound = new UnknownBounds();
	    	partials.put(new RepetitionNode(child, newBound), newBound.getId());
	    }*/
	    
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> expandOptional (OptionalNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode child = node.getChild();
	    Map<RegexNode, UnknownId> newChild = expand(child);
	    if (newChild.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newC : newChild.entrySet()) {
	    		partials.put(new OptionalNode(newC.getKey()), newC.getValue());
	    	}
	    }
	    partials.putAll(mkRepetitionNode(child, Bounds.between(0, 1)));
	    
	    return partials;
	}

	private static Map<RegexNode, UnknownId> expandStar (StarNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode child = node.getChild();
	    Map<RegexNode, UnknownId> newChild = expand(child);
	    if (newChild.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newC : newChild.entrySet()) {
	    		partials.put(new StarNode(newC.getKey()), newC.getValue());
	    	}
	    }
	    partials.putAll(mkRepetitionNode(child, Bounds.atLeast(0)));
	    
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> expandPlus (PlusNode node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    
	    RegexNode child = node.getChild();
	    Map<RegexNode, UnknownId> newChild = expand(child);
	    if (newChild.size() != 0) {
	    	for (Map.Entry<RegexNode, UnknownId> newC : newChild.entrySet()) {
	    		partials.put(new PlusNode(newC.getKey()), newC.getValue());
	    	}
	    }
	    partials.putAll(mkRepetitionNode(child, Bounds.atLeast(1)));
	    
	    return partials;
	}

	private static Map<RegexNode, UnknownId> expandAtomic (CharClass node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
	    return partials;
	}
	
	private static Map<RegexNode, UnknownId> expandUnknownChar(UnknownChar node) {
		Map<RegexNode, UnknownId> partials = new LinkedHashMap<>();
		UnknownChar newCharCon = new UnknownChar();
	    partials.put(new ConcatNode(node, newCharCon), newCharCon.getId());
	    expanTypes.put(newCharCon.getId(), Expansion.Concat);
	    
	    UnknownChar newCharUni = new UnknownChar();
	    partials.put(new UnionNode(node, newCharUni), newCharUni.getId());
	    expanTypes.put(newCharUni.getId(), Expansion.Union);
	    
	    UnknownBounds newBound = new UnknownBounds();
    	partials.put(new RepetitionNode(node, newBound), newBound.getId());
    	expanTypes.put(newBound.getId(), Expansion.Repeat);
    	
	    return partials;
	}
	
}
