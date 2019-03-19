package edu.wisc.regfixer.parser;

import com.microsoft.z3.BoolExpr;

public class OptionalNode extends QuantifierNode {
	private int upto = 100;
	public BoolExpr[][] pairs;

	public OptionalNode(RegexNode child) {
		super('?', child);
	}

	public void setNullable() {
		this.getChild().setNullable();
	}

	public BoolExpr isNullable() {
		return Storage.ctx.mkBool(true);
	}

	public void setLen() {
		this.getChild().setLen();
	}

	public int getLen() {
		return 0;
	}

	public void calUpto(int upto) {
		this.upto = upto;
		this.getChild().calUpto(upto);
	}

	public void setPairs() {
		this.getChild().setPairs();
		this.pairs = this.getChild().getPairs();
	}

	public BoolExpr[][] getPairs() {
		return this.pairs;
	}

	public void setEpsilon() {

	}

	public boolean getEpsilon() {
		return true;
	}
}
