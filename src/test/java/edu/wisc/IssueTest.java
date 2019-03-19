package edu.wisc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.diagnostic.ReportStream;
import edu.wisc.regfixer.enumerate.*;
import edu.wisc.regfixer.parser.CharClassSetNode;
import edu.wisc.regfixer.parser.CharEscapedNode;
import edu.wisc.regfixer.parser.CharLiteralNode;
import edu.wisc.regfixer.parser.CharRangeNode;
import edu.wisc.regfixer.parser.ConcatNode;
import edu.wisc.regfixer.parser.RepetitionNode;
import edu.wisc.regfixer.parser.UnionNode;
import edu.wisc.regfixer.synthesize.Synthesis;
import edu.wisc.regfixer.synthesize.SynthesisFailure;

public class IssueTest {
	private static Diagnostic diag;
	private static Enumerant issue1Template;
	private static Enumerant issue2Template;
	private static Enumerant issue3Template;
	private static Enumerant issue4Template;
	private static Job issue1Job;
	private static Job issue2Job;
	private static Job issue3Job;
	private static Job issue4Job;

	@BeforeClass
	public static void construction() throws FileNotFoundException {
		FileOutputStream fp = new FileOutputStream("issueTest.log", false);
		diag = new Diagnostic(new ReportStream(fp));
		// issue 1
		UnknownChar uc1 = new UnknownChar(Expansion.Concat);
		CharRangeNode char09 = new CharRangeNode('0', '9');
		CharClassSetNode chars09 = new CharClassSetNode(char09);
		CharLiteralNode char2 = new CharLiteralNode('2');
		CharRangeNode char03 = new CharRangeNode('0', '3');
		CharClassSetNode chars03 = new CharClassSetNode(char03);
		ConcatNode cn1 = new ConcatNode(uc1, chars09);
		ConcatNode cn2 = new ConcatNode(char2, chars03);
		UnionNode un1 = new UnionNode(cn1, cn2);
		String exmps1 = "+++\n19\n21\n---\n24\n25";
		issue1Job = Benchmark.readFromStr(exmps1, un1);
		issue1Template = new Enumerant(un1, uc1.getId(), 1, Expansion.Concat);

		// issue2
		CharEscapedNode ce1 = new CharEscapedNode('d');
		RepetitionNode rp1 = new RepetitionNode(ce1, 3, 3);
		UnknownChar uc2 = new UnknownChar(Expansion.Concat);
		ConcatNode cn3 = new ConcatNode(uc2, rp1);
		UnknownBounds ub1 = new UnknownBounds();
		RepetitionNode rp2 = new RepetitionNode(cn3, ub1);
		String exmps2 = "+++\n,123\n,234\n---\n24\n25";
		issue2Job = Benchmark.readFromStr(exmps2, rp2);
		issue2Template = new Enumerant(rp2, Arrays.asList(uc2.getId(), ub1.getId()), 1, Expansion.Concat);
		// issue 3
		UnknownChar uc3 = new UnknownChar(Expansion.Concat);
		CharLiteralNode charb = new CharLiteralNode('b');
		UnionNode un2 = new UnionNode(uc3, charb);
		String exmps3 = "+++\na\n---\nb";
		issue3Job = Benchmark.readFromStr(exmps3, un2);
		issue3Template = new Enumerant(un2, uc3.getId(), 1, Expansion.Concat);

		// issue 4
		UnknownChar uc4 = new UnknownChar(Expansion.Concat);
		ConcatNode cn4 = new ConcatNode(uc4, charb);
		String exmps4 = "+++\na\n---\nb";
		issue4Job = Benchmark.readFromStr(exmps4, cn4);
		issue4Template = new Enumerant(cn4, uc4.getId(), 1, Expansion.Concat);
	}

	/**
	 * Template: (■[0-9]|2[0-3]) Pos example: 19 21 Neg example: 24 25 Expectation:
	 * [1] filled in hole
	 * 
	 * @throws SynthesisFailure,
	 *             BadRangeException
	 */
	/*@Test
	public void issueTest1() throws BadRangeException, SynthesisFailure {
		Synthesis result = issue1Template.synthesize(issue1Job.getCorpus().getPositiveExamples(),
				issue1Job.getCorpus().getNegativeExamples(), diag);
		assertNotNull(result);
		assertEquals(result.toString(), "(1[0-9]|2[0-3])");
	}

	/**
	 * Template: (■(\d){3}){■} Pos example: ",123" ",234" Neg example: 24 25
	 * Expectation: [,] filled for unknown char and [1] filled for unknown bound
	 * 
	 * @throws SynthesisFailure
	 */
	/*@Test
	public void issueTest2() throws SynthesisFailure {
		Synthesis result = issue2Template.synthesize(issue2Job.getCorpus().getPositiveExamples(),
				issue2Job.getCorpus().getNegativeExamples(), diag);
		assertNotNull(result);
		assertEquals(result.toString(), "(,(\\d){3}){1}");
	}

	/**
	 * Template: (■|b) Pos example: "a" Neg example: "b" Expectation: Failed empty
	 * set test
	 */
	/*@Test
	public void issueTest3() {
		assertFalse(issue3Job.getCorpus().passesEmptySetTest(issue3Template));
	}

	/**
	 * Template: (■b)
	 * Pos example: "a"
	 * Neg example: "b"
	 * Expectation: Passed empty set test
	 */
	/*@Test
	public void issueTest4() {
		assertTrue(issue4Job.getCorpus().passesEmptySetTest(issue4Template));
	}*/

}
