package edu.wisc.regfixer.automata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import utilities.Pair;

public class EpsPath {
	ArrayList<Integer> path;
	HashSet<Pair<Integer, Integer>> rep;

	public EpsPath(Integer i) {
		path = new ArrayList<Integer>();
		path.add(i);
		rep = new HashSet<Pair<Integer, Integer>>();
	}

	public EpsPath(EpsPath other) {
		this.path = new ArrayList<Integer>(other.path);
		this.rep = new HashSet<Pair<Integer, Integer>>(other.rep);
	}

	public void append(Integer s) {
		Set<Integer> appSet = new HashSet<>();
		for (int i = 0; i < path.size(); i++) {
			if (path.get(i) == (int) s) {
				appSet.add(i);
			}
		}
		boolean contracted = false;
		for (Integer app : appSet) {
			int diff = path.size() - app - 1;
			boolean contract = true;
			while (diff > 0) {
				if (app - diff < 0 || path.get(app - diff) != (int) path.get(path.size() - diff)) {
					contract = false;
					break;
				}
				diff--;
			}
			if (contract) {
				contracted = true;
				int start = path.get(app + 1);
				path = (ArrayList<Integer>) path.subList(0, app + 1);
				rep.add(new Pair<Integer, Integer>(path.get(app), start));
				break;
			}
		}
		if(!contracted) {
			path.add(s);
		}
	}

	public Integer getLast() {
		return path.get(path.size() - 1);
	}

	public Set<Pair<Integer, Integer>> getRep() {
		return this.rep;
	}

}
