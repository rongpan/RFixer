package edu.wisc.regfixer.global;

import edu.wisc.regfixer.parser.RegexNode;
import edu.wisc.regfixer.automata.Automaton;

public class Global {
	public static boolean maxSat = false;
	public static boolean findMaxSat = false;
	public static boolean pairMode = false;
	public static boolean baseLine = false;
	public static boolean cegis = false;
	public static boolean tutor = false;
	public static boolean useElimination = true;
	public static Automaton correctAutomaton = null;
	public static RegexNode root = null;
	public static int nextHeight = 0;
	public static boolean skipForStack = false;
	
	public static int positives = 0;
	public static int negatives = 0;
	public static int fail1 = 0;
	public static int fail2 = 0;
	public static int fail3 = 0;
}
