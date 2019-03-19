package edu.wisc.regfixer.enumerate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Layer {
	
	public int index;
	public Set<Integer> states;
	public Map<Integer, Boolean> activated;
	public Set<Integer> reachFinal;
	public Map<Integer, Set<Integer>> pathToNext;
	public Map<Integer, Map<UnknownId, Set<Character>>> crossed = new HashMap<>();
	public Map<Integer, Map<UnknownId, Set<Integer>>> exits = new HashMap<>();
	
	public Layer(int index, Set<Integer> states) {
		this.index = index;
		this.states = states;
		activated = new HashMap<>();
		reachFinal = new HashSet<>();
		pathToNext = new HashMap<>();
		for (int i : states) {
			activated.put(i, false);
			pathToNext.put(i, new HashSet<>());
		}
	}

}
