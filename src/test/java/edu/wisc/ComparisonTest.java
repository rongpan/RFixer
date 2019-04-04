package edu.wisc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import edu.wisc.regfixer.RegFixer;
import edu.wisc.regfixer.automata.Automaton;
import edu.wisc.regfixer.enumerate.Job;
import edu.wisc.regfixer.enumerate.Range;
import edu.wisc.regfixer.global.Global;
import utilities.Pair;

@RunWith(Parameterized.class)
public class ComparisonTest {
	@Parameters
	public static Collection<Object[]> data() {
		Collection<Object[]> result = new ArrayList<Object[]>();
		for (int i = 0; i < 5; i++) {
			result.add(new Object[] { "relie-coursenum/", "train" + i });
		}
		for (int i = 0; i < 5; i++) {
			result.add(new Object[] { "relie-phonenum/", "train" + i });
		}
		for (int i = 0; i < 5; i++) {
			result.add(new Object[] { "relie-softwarename/", "train" + i });
		}
		for (int i = 0; i < 5; i++) {
			result.add(new Object[] { "relie-urls/", "train" + i });
		}
		for (int i = 1; i <= 15; i++) {
			result.add(new Object[] { "YAGO-date/", "train" + i });
		}
		for (int i = 1; i <= 15; i++) {
			result.add(new Object[] { "YAGO-number/", "train" + i });
		}
		result.add(new Object[] { "enron-date/", "train" });
		result.add(new Object[] { "enron-phone/", "train" });
		return result;
	}

	@Parameter
	public String datasetName;
	@Parameter(1)
	public String fileName;

	private static PrintWriter pw;
	private static String direct = "./dataset/";
	private static FileOutputStream fp;
	private static Pair<ArrayList<String>, ArrayList<String>> test;
	private static FileOutputStream fcsv;
	private static PrintWriter pwcsv;
	private static boolean outoftime = false;
	private static boolean exOccured = false;
	private static String csvInfo;
	private static FileOutputStream fExp;
	private static PrintWriter pwExp;

	@BeforeClass
	public static void before() throws FileNotFoundException {
		if (Global.pairMode) {
			fp = new FileOutputStream("./result_pairmode_true", true);
			fcsv = new FileOutputStream("./result_pairmode_true.csv", true);
		} else {
			fp = new FileOutputStream("./result_pairmode_false", true);
			fcsv = new FileOutputStream("./result_pairmode_false.csv", true);
		}
		fExp = new FileOutputStream("./result_expressions.csv", true);
		pwExp = new PrintWriter(fExp);
		pw = new PrintWriter(fp);
		pwcsv = new PrintWriter(fcsv);
	}

	@Before
	public void prep() throws FileNotFoundException {
		test = constructTest(direct + datasetName + "test");
	}

	@Test(timeout = 300000)
	public void test() throws TimeoutException, IOException {
		outoftime = true;
		exOccured = false;
		pw.println("==================");
		pw.println(direct + datasetName + fileName);

		Job j = constructJobFromFile(direct + datasetName + fileName);
		String origRegex = j.regex;
		Global.maxSat = true;
		long timeS = -System.currentTimeMillis();
		String s = "";
		try {
			s = RegFixer.fix(j, -1);
		} catch (Exception e) {
			e.printStackTrace();
			exOccured = true;
			return;
		}
		timeS += System.currentTimeMillis();
		int posMatcho = 0;
		int negMatcho = 0;
		int posMatch = 0;
		int negMatch = 0;
		
		Automaton origAutomaton;
		try {
			origAutomaton = new Automaton(edu.wisc.regfixer.parser.Main.parse(origRegex));
		} catch (org.sat4j.specs.TimeoutException e) {
			e.printStackTrace();
			exOccured = true;
			return;
		} catch (Exception e) {
			e.printStackTrace();
			exOccured = true;
			return;
		}
		
		try {
			for (String ex : test.first) {
				if (Global.solutionAutomaton.accepts(ex)) {
					posMatch++;
				}
				if (origAutomaton.accepts(ex)) {
					posMatcho++;
				}
			}
			for (String ex : test.second) {
				if (Global.solutionAutomaton.accepts(ex)) {
					negMatch++;
				}
				if (origAutomaton.accepts(ex)) {
					negMatcho++;
				}
			}
		} catch (Exception ex) {
			// FIXME
			System.out.println("exception while checking results");
		}
		double preo = posMatcho / (double) test.first.size();
		double pre = posMatch / (double) test.first.size();
		double reco = posMatcho / (double) (posMatcho + negMatcho);
		double rec = posMatch / (double) (posMatch + negMatch);

		double f1o = 2 * (preo * reco) / (preo + reco);
		double f1 = 2 * (pre * rec) / (pre + rec);
		pwExp.println(s);
		pw.println("Original regex: " + origRegex);
		pw.println("\tOriginal length: " + origRegex.length());
		pw.println("\tPositive matches: " + posMatcho + "/" + test.first.size() + "\n\tNegative matches: " + negMatcho
				+ "/" + test.second.size());
		pw.println("F1 score: " + f1o);
		pw.println("Regex Result: " + s);
		pw.println("\tResult length: " + s.length());
		pw.println("\tRunning time: " + timeS);
		pw.println("\tPositive matches: " + posMatch + "/" + test.first.size() + "\n\tNegative matches: " + negMatch
				+ "/" + test.second.size());
		pw.println("F1 score: " + f1);
		csvInfo = posMatch + "," + negMatch + "," + f1 + "," + s.length() + "," + timeS;
		outoftime = false;
	}

	@After
	public void info() throws IOException {
		if (exOccured) {
			pwcsv.println("ERR,ERR,ERR,ERR,ERR");
		} else if (!outoftime) {
			pwcsv.println(csvInfo);
		} else {
			pwcsv.println("TO,TO,TO,TO,TO");
		}
		pw.flush();
		fp.flush();
		pwcsv.flush();
		fcsv.flush();
	}

	@AfterClass
	public static void after() throws IOException {
		pw.flush();
		fp.flush();
		pw.close();
		fp.close();
		pwcsv.flush();
		fcsv.flush();
		pwcsv.close();
		fcsv.close();
		pwExp.flush();
		fExp.flush();
		pwExp.close();
		fExp.close();
	}

	public static Job constructJobFromFile(String filename) throws FileNotFoundException {
		File fp = new File("src/test/resources/" + filename);
		Scanner scnr = new Scanner(fp);
		String regExp = scnr.nextLine();
		scnr.nextLine();
		String example = scnr.nextLine();
		String corpus = "";
		Set<Range> pRange = new HashSet<>();
		Set<Range> nRange = new HashSet<>();
		while (!example.equals("---")) {
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
		return new Job(filename, regExp, corpus, pRange, nRange);
	}

	public static Pair<ArrayList<String>, ArrayList<String>> constructTest(String filename)
			throws FileNotFoundException {
		File fp = new File("src/test/resources/" + filename);
		Scanner scnr = new Scanner(fp);
		ArrayList<String> pos = new ArrayList<String>();
		ArrayList<String> neg = new ArrayList<String>();
		scnr.nextLine();
		String example = scnr.nextLine();
		while (!example.equals("---")) {
			pos.add(example);
			example = scnr.nextLine();
		}
		while (scnr.hasNextLine()) {
			example = scnr.nextLine();
			neg.add(example);
		}
		return new Pair<>(pos, neg);
	}

}
