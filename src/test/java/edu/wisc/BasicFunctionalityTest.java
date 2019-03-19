package edu.wisc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;

import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.enumerate.BadRangeException;
import edu.wisc.regfixer.enumerate.Corpus;
import edu.wisc.regfixer.enumerate.Enumerant;
import edu.wisc.regfixer.enumerate.Enumerants;
import edu.wisc.regfixer.enumerate.Expansion;
import edu.wisc.regfixer.enumerate.ForbiddenExpansionException;
import edu.wisc.regfixer.enumerate.Grafter;
import edu.wisc.regfixer.enumerate.Job;
import edu.wisc.regfixer.enumerate.Range;
import edu.wisc.regfixer.enumerate.Slicer;
import edu.wisc.regfixer.enumerate.UnknownBounds;
import edu.wisc.regfixer.enumerate.UnknownChar;
import edu.wisc.regfixer.parser.CharLiteralNode;
import edu.wisc.regfixer.parser.ConcatNode;
import edu.wisc.regfixer.parser.OptionalNode;
import edu.wisc.regfixer.parser.PlusNode;
import edu.wisc.regfixer.parser.RegexNode;
import edu.wisc.regfixer.parser.RepetitionNode;
import edu.wisc.regfixer.parser.StarNode;
import edu.wisc.regfixer.parser.UnionNode;

public class BasicFunctionalityTest {
	private static Job testCase = null;
	private static ArrayList<Enumerant> enuList = null;
	private static Enumerant enuCase1 = null;
	
	@BeforeClass
	public static void preparation() throws FileNotFoundException {
		testCase = UtilityFunctions.constructJobFromFile("test_date_true.txt");
		enuList = new ArrayList<>();
	}
	
	@Test
	public void testCorpus() throws BadRangeException {
		String tempCorpus = "(123) 456-7890\n" + 
				"(987) 653-2109\n" + 
				"1-800-678-0693\n" + 
				"468-013-5790\n" + 
				"258-925";
		Set<Range> tempPosSet = new HashSet<>();
		tempPosSet.add(new Range("(30:44)"));
		tempPosSet.add(new Range("(45:57)"));
		Set<Range> tempNegSet = new HashSet<>();
		tempNegSet.add(new Range("(58:65)"));
		tempNegSet.add(new Range("(0:14)"));
		tempNegSet.add(new Range("(15:29)"));
		Corpus c = new Corpus(tempCorpus, tempPosSet, tempNegSet);
		assertEquals(c.getTotalCharsInPositiveExamples(),26);
		Pattern p = Pattern.compile("[0-9\\-]+");
		Set<Range> tempExp = new HashSet<>();
		tempExp.add(new Range("(30:44)"));
		tempExp.add(new Range("(45:57)"));
		Set<Range> result = Corpus.inferNegativeRanges(p, tempCorpus, tempExp);
		assertEquals(result.toString(), "[(58:65)]");
	}
	
	/*@Test
	public void testEnumerants() throws TimeoutException, IOException {
		// \d{1,2}\/\d{1,2}\/\d{4} (\d)
		Enumerants enumerants = new Enumerants(testCase.getTree(), testCase.getCorpus(), new Diagnostic());
		Enumerant enu = null;
		int cutoff = 0;
		while ((enu = enumerants.poll()) != null) {
			if (cutoff++ > 100) {
				break;
			}
			if (enu.toString().equals("(\\d){1,2}■(\\d){1,2}\\/(\\d){4}")) {
				assertEquals(enu.getCost(), 1);

			} else if (enu.toString().equals("(■■)(\\d){1,2}\\/(\\d){1,2}\\/(\\d){4}")) {
				assertEquals(enu.getCost(), 2);

			} else if (enu.toString().equals("(\\d){1,2}■(\\d){4}")) {
				assertEquals(enu.getCost(), 4);
			} else if (enu.toString().equals("((((■■)▓)(▓){■})▓)(\\d){1,2}\\/(\\d){1,2}\\/(\\d){4}")) {
				assertEquals(enu.getCost(), 6);

			} else if (enu.toString().equals("(\\d){1,2}\\/(\\d){1,2}■\\/(\\d){4}")) {
				assertEquals(enu.getCost(), 1);
			} else if (enu.toString().equals("■(\\d){1,2}\\/(\\d){1,2}\\/(\\d){4}")) {
				enuCase1 = enu;
			}
			enuList.add(enu);
		}
		List<Enumerant> expands1 = enuCase1.expand();
		List<String> expandsDemo1 = expands1.stream().map(Enumerant::toString).collect(Collectors.toList());
		assertTrue(expandsDemo1.contains("(■|■)(\\d){1,2}\\/(\\d){1,2}\\/(\\d){4}"));
		assertTrue(expandsDemo1.contains("(■■)(\\d){1,2}\\/(\\d){1,2}\\/(\\d){4}"));
		assertTrue(expandsDemo1.contains("(■){■}(\\d){1,2}\\/(\\d){1,2}\\/(\\d){4}"));
	}*/
	
	@Test
	public void testGrafter() throws ForbiddenExpansionException {
		CharLiteralNode cln1 = new CharLiteralNode('a');
		CharLiteralNode cln2 = new CharLiteralNode('b');
		CharLiteralNode cln3 = new CharLiteralNode('c');
		CharLiteralNode cln4 = new CharLiteralNode('d');
		UnknownChar uc1 = new UnknownChar(Expansion.Concat);
		UnknownChar uc2 = new UnknownChar(Expansion.Union);
		UnknownChar uc4 = new UnknownChar(Expansion.Concat);
		UnknownChar uc5 = new UnknownChar(Expansion.Concat);
		List<RegexNode> l1 = new ArrayList<>();
		l1.add(uc1);
		l1.add(cln1);
		l1.add(cln2);
		l1.add(cln3);
		ConcatNode c1 = new ConcatNode(l1);
		UnionNode u1 = new UnionNode(uc2, cln4);
		UnknownChar child = new UnknownChar(Expansion.Concat);
		UnknownBounds bounds = new UnknownBounds();
		
		UnionNode r1 = new UnionNode(uc4, uc5);
		ConcatNode r2 = new ConcatNode(uc4, uc5);
		RepetitionNode r3 = new RepetitionNode(child, bounds);
		RegexNode e1 = Grafter.graft(c1, uc1.getId(), r1);
		RegexNode e2 = Grafter.graft(c1, uc1.getId(), r2);
		RegexNode e3 = Grafter.graft(c1, uc1.getId(), r3);
		assertEquals(e1.toString(), "(■|■)abc");
		assertEquals(e2.toString(), "(■■)abc");
		assertEquals(e3.toString(), "(■){■}abc");
		
		RegexNode e4 = Grafter.graft(u1, uc2.getId(), r1);
		RegexNode e5 = Grafter.graft(u1, uc2.getId(), r2);
		RegexNode e6 = Grafter.graft(u1, uc2.getId(), r3);
		
		assertEquals(e4.toString(), "((■|■)|d)");
		assertEquals(e5.toString(), "(■■|d)");
		assertEquals(e6.toString(), "((■){■}|d)");
		
		RegexNode e7 = Grafter.graft(uc1, uc1.getId(), r1);
		RegexNode e8 = Grafter.graft(uc1, uc1.getId(), r2);
		RegexNode e9 = Grafter.graft(uc1, uc1.getId(), r3);
		
		assertEquals(e7.toString(), "(■|■)");
		assertEquals(e8.toString(), "■■");
		assertEquals(e9.toString(), "(■){■}");
	}
	
	@Test
	public void testRange() throws BadRangeException {
		Range A = new Range(4,6);
		Range B = new Range("(4:6)");
		Range C = new Range(7,20);
		Range D = new Range("(7:20)");
		assertTrue(A.equals(B));
		assertTrue(C.equals(D));
		assertTrue(A.endsBefore(D));
		assertFalse(A.endsAfter(C));
		assertFalse(A.equals(D));
		assertTrue(A.compareTo(C)<0);
	}
	
	// test slicer
	
	@Test
	public void testDateExample() throws FileNotFoundException {
		//   \d{1,2}\/\d{1,2}\/\d{4}
		Job testJob = UtilityFunctions.constructJobFromFile("test_date_true.txt");
		RegexNode n = testJob.getTree();
		List<Enumerant> enumerantsResult = Slicer.slice(n);
		int concatSize = ((ConcatNode) n).getChildren().size();
		int count_between = 0;
		for (Enumerant e : enumerantsResult) {
			RegexNode etree = e.getTree();
			if (etree instanceof ConcatNode && ((ConcatNode)etree).getChildren().size() == concatSize + 1)
			if (((ConcatNode) e.getTree()).getChildren().size() == concatSize + 1) {
				count_between++;
			}
		}
		
		assertEquals(count_between, concatSize + 1);
		assertEquals(enumerantsResult.size(), 27);
	}
	
	@Test
	public void testCommaFormat() throws FileNotFoundException {
		//   (\d|,)*\d+
		Job testJob = UtilityFunctions.constructJobFromFile("test_commaformat_TO.txt");
		RegexNode n = testJob.getTree();
		List<Enumerant> sliceResult = Slicer.slice(n);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertTrue(h.contains("((■\\d|,)){■}(\\d)+"));
		assertTrue(h.contains("((\\d|,)){■}(\\d)+"));
		assertTrue(h.contains("((\\d|,))*■"));
		assertTrue(h.contains("((\\d|,))*(■){■}"));
		assertTrue(h.contains("((\\d|,))*■(\\d)+"));
		assertTrue(h.contains("■"));
	}

	@Test
	public void testConcat() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		CharLiteralNode n2 = new CharLiteralNode('b');
		CharLiteralNode n3 = new CharLiteralNode('c');
		CharLiteralNode n4 = new CharLiteralNode('d');
		List<RegexNode> l = new LinkedList<>();
		l.add(n1);
		l.add(n2);
		l.add(n3);
		l.add(n4);
		ConcatNode n5 = new ConcatNode(l);
		List<Enumerant> sliceResult = Slicer.slice(n5);
		assertEquals(sliceResult.size(), 15);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertTrue(h.contains("■d"));//■
		assertTrue(h.contains("ab■cd"));
		assertTrue(h.contains("ab■d"));
		assertTrue(h.contains("abc■"));
		assertTrue(h.contains("a■"));
		assertTrue(h.contains("■cd"));
		assertTrue(h.contains("■"));
		assertTrue(h.contains("ab■"));
		assertTrue(h.contains("abcd■"));
	}

	@Test
	public void testUnion() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		CharLiteralNode n2 = new CharLiteralNode('b');
		UnionNode n3 = new UnionNode(n1, n2);
		List<Enumerant> sliceResult = Slicer.slice(n3);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertEquals(h.size(), 3);
		assertTrue(h.contains("(■|b)"));
		assertTrue(h.contains("(a|■)"));
		assertTrue(h.contains("■"));
	}
	
	@Test
	public void testRepetition() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		RepetitionNode n2 = new RepetitionNode(n1, 3, 20);
		List<Enumerant> sliceResult = Slicer.slice(n2);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertEquals(h.size(), 3);
		assertTrue(h.contains("(a){■}"));
		assertTrue(h.contains("(■){■}"));
		assertTrue(h.contains("■"));
	}
	
	@Test
	public void testOptional() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		OptionalNode n2 = new OptionalNode(n1);
		List<Enumerant> sliceResult = Slicer.slice(n2);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertEquals(h.size(), 3);
		assertTrue(h.contains("(a){■}"));
		assertTrue(h.contains("(■){■}"));
		assertTrue(h.contains("■"));
	}
	
	@Test
	public void testStar() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		StarNode n2 = new StarNode(n1);
		List<Enumerant> sliceResult = Slicer.slice(n2);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertEquals(h.size(), 3);
		assertTrue(h.contains("(a){■}"));
		assertTrue(h.contains("(■){■}"));
		assertTrue(h.contains("■"));
	}
	
	@Test
	public void testPlus() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		PlusNode n2 = new PlusNode(n1);
		List<Enumerant> sliceResult = Slicer.slice(n2);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertEquals(h.size(), 3);
		assertTrue(h.contains("(a){■}"));
		assertTrue(h.contains("(■){■}"));
		assertTrue(h.contains("■"));
	}
	
	@Test
	public void testChar() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		List<Enumerant> sliceResult = Slicer.slice(n1);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertEquals(h.size(), 1);
		assertTrue(h.contains("■"));
	}
}
