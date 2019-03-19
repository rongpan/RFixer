package edu.wisc.regfixer.parser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.microsoft.z3.BoolExpr;

public class CharClassSetNode implements CharClass {
	private boolean not;
	private Set<CharRangeNode> subClasses;
	private BoolExpr nullable;
	public BoolExpr[][] pairs;

	public CharClassSetNode(Collection<CharRangeNode> subClasses) {
		this.not = false;
		this.subClasses = new HashSet<>(subClasses);
	}

	public CharClassSetNode(boolean not, Collection<CharRangeNode> subClasses) {
		this.not = not;
		this.subClasses = new HashSet<>(subClasses);
	}

	public CharClassSetNode(CharRangeNode... subClasses) {
		this.not = false;
		this.subClasses = new HashSet<>();

		for (int i = 0; i < subClasses.length; i++) {
			this.subClasses.add(subClasses[i]);
		}
	}

	public boolean isInverted() {
		return this.not;
	}

	public Set<CharRangeNode> getSubClasses() {
		return this.subClasses;
	}

	public void addSubClass(CharRangeNode subClass) {
		this.subClasses.add(subClass);
	}

	public int descendants() {
		return 1;
	}

	@Override
	public int hashCode() {
		return this.subClasses.stream().mapToInt(c -> c.hashCode()).sum();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CharClassSetNode) {
			CharClassSetNode cast = (CharClassSetNode) obj;
			boolean sameSubClasses = this.subClasses.equals(cast.getSubClasses());
			boolean sameNegation = (this.not == cast.isInverted());
			return (sameSubClasses && sameNegation);
		}

		return false;
	}

	public String toString() {
		String str = this.subClasses.stream().map(elem -> elem.toString()).reduce("", String::concat);

		if (this.not) {
			return String.format("[^%s]", str);
		} else {
			return String.format("[%s]", str);
		}
	}

	public void toBinary() {

	}

	public int collectUnknown() {
		return 0;
	}

	public void setNullable() {
		this.nullable = Storage.ctx.mkBool(false);
	}

	public BoolExpr isNullable() {
		return this.nullable;
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
		for (CharRangeNode rangeNode : this.subClasses) {
			rangeNode.setPairs();
		}
		for (int i = 0; i < length; i++) {
			for (int j = i; j < length; j++) {
				this.pairs[i][j] = Storage.ctx.mkBool(false);
			}
			for (CharRangeNode rangeNode : this.subClasses) {
				this.pairs[i][i] = Storage.ctx.mkOr(this.pairs[i][i], rangeNode.pairs[i][i]);
			}
			if (this.not) {
				this.pairs[i][i] = Storage.ctx.mkNot(this.pairs[i][i]);
			}
		}
		// return this.pairs;
	}

	public BoolExpr[][] getPairs() {
		return this.pairs;
	}

	public String finalString() {
		String str = this.subClasses.stream().map(elem -> elem.finalString()).reduce("", String::concat);

		if (this.not) {
			return String.format("[^%s]", str);
		} else {
			return String.format("[%s]", str);
		}
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
