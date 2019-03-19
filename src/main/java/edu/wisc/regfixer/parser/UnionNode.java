package edu.wisc.regfixer.parser;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import edu.wisc.regfixer.enumerate.UnknownBounds;
import edu.wisc.regfixer.synthesize.SynthesisFailure;

public class UnionNode implements RegexNode {
	private RegexNode left;
	private RegexNode right;
	private boolean synthetic;
	private int len = -1;
	private int upto = 100;
	private BoolExpr nullable;
	public BoolExpr[][] pairs;
	private boolean epsilon;

	public UnionNode(RegexNode left, RegexNode right) {
		this.left = left;
		this.right = right;
		this.synthetic = false;
	}

	public UnionNode(RegexNode left, RegexNode right, boolean isSynthetic) {
		this.left = left;
		this.right = right;
		this.synthetic = isSynthetic;
	}

	public RegexNode getLeftChild() {
		return this.left;
	}

	public RegexNode getRightChild() {
		return this.right;
	}

	public boolean isSynthetic() {
		return this.synthetic;
	}

	public int descendants() {
		return 1 + this.left.descendants() + this.right.descendants();
	}

	public String toString() {
		return String.format("(%s|%s)", this.left.toString(), this.right.toString());
	}

	public void toBinary() {
		this.left.toBinary();
		this.right.toBinary();
	}

	public int collectUnknown() {
		return left.collectUnknown() + right.collectUnknown();
	}

	public void setNullable() {
		Context ctx = Storage.ctx;

		// this.nullable =
		// ctx.mkBoolConst(Integer.toString(Storage.nameIndex++));

		this.left.setNullable();
		this.right.setNullable();
		/*
		 * ctx.mkImplies(ctx.mkOr(this.left.isNullable(),
		 * this.right.isNullable()), this.nullable);
		 * ctx.mkImplies(this.nullable, ctx.mkOr(this.left.isNullable(),
		 * this.right.isNullable()));
		 */
		this.nullable = ctx.mkOr(this.left.isNullable(), this.right.isNullable());

	}

	public BoolExpr isNullable() {
		return this.nullable;
	}

	public void setLen() {
		this.left.setLen();
		this.right.setLen();
		this.len = Integer.min(left.getLen(), right.getLen());
	}

	public int getLen() {
		return this.len;
	}

	public void calUpto(int upto) {
		this.upto = upto;
		this.left.calUpto(upto);
		this.right.calUpto(upto);
		/*
		 * System.err.println("cur exmaple is: " +
		 * String.valueOf(Storage.curExample)); System.err.println("this is " +
		 * this); System.err.println("this len is " + this.len);
		 * System.err.println("upto is " + this.upto);
		 * System.err.println("left is " + left);
		 * System.err.println("left length is " + left.getLen());
		 * System.err.println("right is " + right);
		 * System.err.println("right length is " + right.getLen());
		 */
	}

	public void setPairs() {
		int length = Storage.curExample.length;
		this.pairs = new BoolExpr[length][length];
		left.setPairs();
		right.setPairs();
		BoolExpr[][] leftPairs = left.getPairs();
		BoolExpr[][] rightPairs = right.getPairs();

		Context ctx = Storage.ctx;

		// PairLevel.init(this.pairs);

		for (int i = 0; i < length; i++) {
			for (int j = i; j < length; j++) {
				if (j < i + upto)
					this.pairs[i][j] = ctx.mkOr(leftPairs[i][j], rightPairs[i][j]);
				else
					this.pairs[i][j] = ctx.mkBool(false);
			}
		}

		// return this.pairs;
	}

	public BoolExpr[][] getPairs() {
		return this.pairs;
	}

	public String finalString() {
		// System.err.println("final called this is " + this);
		return String.format("(%s|%s)", this.left.finalString(), this.right.finalString());
	}

	public void setEpsilon() {
		left.setEpsilon();
		right.setEpsilon();
		this.epsilon = left.getEpsilon() || right.getEpsilon();
	}

	public boolean getEpsilon() {
		return this.epsilon;
	}

	@Override
	public int LRUnknownCount() {
		int leftCount = left.LRUnknownCount();
		int rightCount = right.LRUnknownCount();
		if (leftCount == -2 || rightCount == -2)
			return -2;
		if (leftCount == -1 || rightCount == -1)
			return -1;
		if (leftCount < rightCount)
			return -2;
		
		return leftCount + rightCount;
	}
}
