package edu.wisc.regfixer.global;

import edu.wisc.regfixer.parser.RegexNode;
import edu.wisc.regfixer.automata.Automaton;

public class Global {
	public static boolean maxSat = false;
	public static boolean findMaxSat = false;
	public static boolean pairMode = false;
	public static boolean baseLine = false;
	public static boolean cegis = false;
	public static Automaton correctAutomaton = null;
	public static RegexNode root = null;
	public static int nextHeight = 0;
	public static boolean skipForStack = false;
}
