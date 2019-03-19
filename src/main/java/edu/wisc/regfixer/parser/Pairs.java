package edu.wisc.regfixer.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pairs {
    public List<Set<Pair>> all;
    public boolean nullable;
    
    public Pairs(boolean nullable) {
    	Set<Pair> set = new HashSet<>();
    	this.all = new ArrayList<>();
    	this.all.add(set);
    	this.nullable = nullable;
    }
    
    public Pairs(Set<Pair> set, boolean nullable) {
    	this.all = new ArrayList<>();
    	this.all.add(set);
    	this.nullable = nullable;
    }
    
    public void genNext() {
    	int curLevel = all.size();
    	int nextLevel = curLevel + 1;
    	Set<Pair> nextSet = new HashSet<>();
    	if (nullable) {
    		for (Pair p: this.all.get(curLevel - 1)) {
    			nextSet.add(p);
    		}
    	}
    	
    	for (int i = 1; i < nextLevel; i++) {
    		int j = nextLevel - i;
    		Set<Pair> preSet = this.all.get(i);
    		Set<Pair> postSet = this.all.get(j);
    		for (Pair pre: preSet) {
    			for (Pair post : postSet) {
    				if (pre.right + 1 == post.left)
    					nextSet.add(new Pair(pre.left, post.right));
    			}
    		}
    	}
    	this.all.add(nextSet);
    	
    }
}
