package edu.wisc.regfixer.parser;

import java.util.ArrayList;
import java.util.List;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;

public class PairLevel {
    public List<BoolExpr[][]> all;
    public BoolExpr nullable;
    
    public PairLevel(BoolExpr nullable) {
    	int length = Storage.curExample.length;
    	BoolExpr[][] level = new BoolExpr[length][length];
    	this.all = new ArrayList<>();
    	this.all.add(level);
    	this.nullable = nullable;
    }
    
    public PairLevel(BoolExpr[][] first, BoolExpr nullable) {
    	this.all = new ArrayList<>();
    	this.all.add(first);
    	this.nullable = nullable;
    }
    
    public void genNext() {
    	//System.err.println("get next level");
    	int curLevel = all.size();
    	//System.err.println("curLevel: " + curLevel);
    	int length = Storage.curExample.length;
    	BoolExpr[][] cur = all.get(curLevel - 1);
    	BoolExpr[][] next = new BoolExpr[length][length];
    	
    	Context ctx = Storage.ctx;
    	
    	initF(next);
    	
    	/*for (int i = 0; i < length; i++) {
    		for (int j = i; j < length; j++) {
    			next[i][j] = ctx.mkOr(next[i][j], ctx.mkAnd(nullable, cur[i][j]));
    		}
    	}
    		
		BoolExpr[][] postSet = this.all.get(0);
		
		for (int m = 0; m < length; m++) {
			for (int l = m + 1; l < length; l++) {
				for (int n = m; n < l; n++) {
					int k = n + 1;
					next[m][l] = ctx.mkOr(next[m][l], ctx.mkAnd(cur[m][n], postSet[k][l]));
				}
				next[m][l] = ctx.mkOr(next[m][l], ctx.mkAnd(this.nullable, cur[m][l]));
			}
		}*/
    	BoolExpr[][] postSet = this.all.get(0);
    	
    	for (int i = 0; i < length; i++) {
    		int front = Integer.min(length, i + curLevel) - 1;
    		for (int j = i; j < front; j++) {
    			next[i][j] = cur[i][j];
    		}
    		next[i][front] = ctx.mkAnd(nullable, cur[i][front]);
    		for (int j = i + curLevel; j < length; j++) {
    			next[i][j] = ctx.mkOr(next[i][j], ctx.mkAnd(nullable, cur[i][j]));
    			for (int m = i; m < j; m++) {
    				int n = m + 1;
    				next[i][j] = ctx.mkOr(next[i][j], ctx.mkAnd(cur[i][m], postSet[n][j]));
    			}
    		}
    	}
    	
    	this.all.add(next);
    }
    
    public BoolExpr[][] collect() {
    	int length = Storage.curExample.length;
    	BoolExpr[][] res = new BoolExpr[length][length];
    	
    	Context ctx = Storage.ctx;
    	
    	for (int i = 0; i < length; i++) {
    		for (int j = i; j < length; j++) {
    		    res[i][j] = ctx.mkBool(false);
    		    for (int k = 0; k < all.size(); k++) {
    		    	res[i][j] = ctx.mkOr(res[i][j], all.get(k)[i][j]);
    		    }
    		}
    	}
    	return res;
    }
    
    public BoolExpr[][] collectWithBounds(IntExpr l, IntExpr r) {
    	int length = Storage.curExample.length;
    	BoolExpr[][] res = new BoolExpr[length][length];
    	
    	Context ctx = Storage.ctx;
    	
    	for (int i = 0; i < length; i++) {
    		for (int j = i; j < length; j++) {
    		    res[i][j] = ctx.mkBool(false);
    		    for (int k = 0; k < all.size(); k++) {
    		    	res[i][j] = ctx.mkOr(res[i][j], ctx.mkAnd(
    		    			ctx.mkLe(l, ctx.mkInt(k+1)), ctx.mkLe(ctx.mkInt(k+1), r), all.get(k)[i][j]));
    		    }
    		    //res[i][j] = ctx.mkOr(res[i][j], ctx.mkAnd(nullable, ctx.mkLe(l, r), 
    		    	//	all.get(all.size() - 1)[i][j]));
    		}
    	}
    	return res;
    }
    
    public BoolExpr[][] collectWithActualBounds(int min, int max) {
    	int length = Storage.curExample.length;
    	BoolExpr[][] res = new BoolExpr[length][length];
    	
    	Context ctx = Storage.ctx;
    	IntNum l = ctx.mkInt(min);
    	IntNum r = ctx.mkInt(max);
    	
    	for (int i = 0; i < length; i++) {
    		for (int j = i; j < length; j++) {
    		    res[i][j] = ctx.mkBool(false);
    		    for (int k = 0; k < all.size(); k++) {
    		    	res[i][j] = ctx.mkOr(res[i][j], ctx.mkAnd(
    		    			ctx.mkLe(l, ctx.mkInt(k+1)), ctx.mkLe(ctx.mkInt(k+1), r), all.get(k)[i][j]));
    		    }
    		}
    	}
    	return res;
    }
    
    /*public static void init(BoolExpr[][] next){
    	int length = next.length;
    	for (int i = 0; i < length; i++) {
    		for (int j = i; j < length; j++) {
    			next[i][j] = Storage.ctx.mkBoolConst(Integer.toString(Storage.nameIndex++));
    		}
    	}
    }*/
    
    public static void initF(BoolExpr[][] next){
    	int length = next.length;
    	for (int i = 0; i < length; i++) {
    		for (int j = i; j < length; j++) {
    			next[i][j] = Storage.ctx.mkBool(false);
    		}
    	}
    }
}
