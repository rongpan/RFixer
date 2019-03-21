package edu.wisc.regfixer.enumerate;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import edu.wisc.regfixer.global.Global;
import edu.wisc.regfixer.parser.RegexNode;

public class Benchmark {

	public final static String boundary = "---";

	public static Job readFromFile(String filename) throws IOException {
		File fp = new File(filename);
		Scanner scnr = new Scanner(fp);
		String regExp = scnr.nextLine();
		String truth = null;
		if (Global.cegis)
			truth = scnr.nextLine();
		scnr.nextLine();
		String example = scnr.nextLine();
		String corpus = "";
		Set<Range> pRange = new HashSet<>();
		Set<Range> nRange = new HashSet<>();
		while (!example.equals(boundary)) {
			pRange.add(new Range(corpus.length(), corpus.length() + example.length()));
			corpus = corpus.concat(example);
			example = scnr.nextLine();
		}
		while (scnr.hasNextLine()) {
			example = scnr.nextLine();
			nRange.add(new Range(corpus.length(), corpus.length() + example.length()));
			corpus = corpus.concat(example);
		}
		scnr.close();
		return new Job(filename, regExp, truth, corpus, pRange, nRange);
	}

	public static Job readFromStr(String input, RegexNode tree) {
		Scanner scnr = new Scanner(input);
		String regExp = "";
		scnr.nextLine();
		String example = scnr.nextLine();
		String corpus = "";
		Set<Range> pRange = new HashSet<>();
		Set<Range> nRange = new HashSet<>();
		while (!example.equals(boundary)) {
			pRange.add(new Range(corpus.length(), corpus.length() + example.length()));
			corpus = corpus.concat(example);
			example = scnr.nextLine();
		}
		while (scnr.hasNextLine()) {
			example = scnr.nextLine();
			nRange.add(new Range(corpus.length(), corpus.length() + example.length()));
			corpus = corpus.concat(example);
		}
		scnr.close();
		Corpus c = new Corpus(corpus, pRange, nRange, true);
		return new Job("str", tree, null, c);
	}

	/*
	 * public static Job readFromFile (String filename) throws IOException { String
	 * regex = ""; Set<Range> selectedRanges = new HashSet<Range>(); Set<Range>
	 * negativeRanges = new HashSet<Range>(); String corpus = "";
	 * 
	 * Scanner sc = new Scanner(new File(filename)); int lineNum = 1;
	 * 
	 * if (sc.hasNextLine()) { String line = sc.nextLine(); lineNum++;
	 * 
	 * regex = line;
	 * 
	 * // Digest boundary. if (!sc.hasNextLine()) { lineNum++; String fmt =
	 * "Expected boundary on line %d of '%s'"; throw new
	 * IOException(String.format(fmt, lineNum, filename)); }
	 * 
	 * line = sc.nextLine(); lineNum++; if (!line.equals(boundary)) { String fmt =
	 * "Expected boundary on line %d of '%s'"; throw new
	 * IOException(String.format(fmt, lineNum, filename)); } }
	 * 
	 * while (sc.hasNextLine()) { String line = sc.nextLine(); lineNum++;
	 * 
	 * if (line.equals(boundary)) { // Break loop since boundary was encountered.
	 * break; } else { try { selectedRanges.add(new Range(line)); } catch
	 * (BadRangeException ex) { String fmt =
	 * "Expected index pair or boundary on line %d of '%s'"; throw new
	 * IOException(String.format(fmt, lineNum, filename)); } } }
	 * 
	 * while (sc.hasNextLine()) { corpus += sc.nextLine() + (sc.hasNextLine() ? "\n"
	 * : ""); lineNum++; }
	 * 
	 * return new Job(filename, regex, corpus, selectedRanges); }
	 */

	public static void saveToFile(Job job, String filename) throws IOException {
		PrintWriter pw = new PrintWriter(filename, "UTF-8");
		pw.print(job.toString());
		pw.close();
	}
}
