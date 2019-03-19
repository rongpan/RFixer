package edu.wisc;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import edu.wisc.regfixer.RegFixer;
import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.diagnostic.Registry;
import edu.wisc.regfixer.diagnostic.ReportStream;
import edu.wisc.regfixer.diagnostic.Timing;
import edu.wisc.regfixer.enumerate.Benchmark;
import edu.wisc.regfixer.enumerate.Job;

@RunWith(Parameterized.class)
public class SimplifiedBenchmarkTest {
	@Parameters
	public static Iterable<? extends Object> data() {
		return Arrays.asList("test_part_date.txt", "test_partial_time.txt", "test_words.txt", 
				"test_rep_generation.txt", "test_add_dash.txt", "test_filename_prefix.txt");
	}

	@Parameter
	public String fileName;

	private static Diagnostic diag;
	private static FileOutputStream outCSV;
	private static PrintWriter writer;

	@BeforeClass
	public static void preparation() throws FileNotFoundException {
		FileOutputStream fp = new FileOutputStream("testAllResult2.log", false);
		diag = new Diagnostic(new ReportStream(fp), new Registry(), new Timing());
		outCSV = new FileOutputStream("stat2.csv");
		writer = new PrintWriter(outCSV);
		printHeader();
	}
	
	@AfterClass
	public static void clean() throws IOException {
		writer.close();
		outCSV.close();
	}


	@Test(timeout = 5000)
	public void test() {
		/*String fullName = "tests/benchmark_explicit2/" + fileName;
		Job job = null;
		try {
			job = Benchmark.readFromFile(fullName);
		} catch (IOException e) {
			System.err.println("unable to read file");
		}
		String result = null;
		try {
			result = RegFixer.fix(job, 4000, diag);
		} catch (java.util.concurrent.TimeoutException e) {
			System.err.println("No solution found within 4000 templates. ");
			assertNotNull(result);
		}
		try {
			outputStats(job, result);
		} catch (IOException e) {
			System.err.println("Cannot output stats. ");
		}
		assertTrue(UtilityFunctions.resultValid(result, job.getCorpus()));*/
	}

	private static void printHeader() {
		writer.print("name,"); // name of benchmark file
		writer.print("size,"); // number of nodes in original regular expression
		writer.print("solution,"); // first computed expression (empty if no solution found)
		writer.print("timeTotal,"); // time between when enumeration started and when it ended (milliseconds)
		writer.print("timeToFirstSol,"); // time between when enumeration started and the first solution (milliseconds)
		writer.print("templatesToFirstSol,"); // number of templates explored before first solution
		writer.print("templatesTotal,"); // number of templates explored in total
		writer.print("costOfFirstSol,"); // expansion cost of first solution
		// writer.print("sizeOfSearchSpace,"); // TODO
		// writer.print("timeTotalNoOptimizations,"); // time for whole enumeration with
		// no optimizations
		writer.print("timeSATSolver,"); // time spent building & solving SAT formulae
		writer.print("timeDotTest,"); // time spent testing examples against the dot test
		writer.print("timeDotStarTest,"); // time spent testing examples against the dot-star test
		writer.print("timeEmptySetTest,"); // time spent testing examples against the empty-set test
		writer.print("totalDotTests,"); // number of times the dot test was applied
		writer.print("totalDotStarTests,"); // number of times the dot-star test was applied
		writer.print("totalEmptySetTests,"); // number of times the empty-set test was applied
		writer.print("totalDotTestsRejects,"); // number of times the dot test rejected a template
		writer.print("totalDotStarTestsRejects,"); // number of times the dot-star test rejected a template
		writer.print("totalEmptySetTestsRejects,"); // number of times the empty-set test rejected a template
		writer.print("maximumRoutes,"); // maximum number of routes ever fed to the SAT solver from any template
		writer.print("totalPositiveExamples,"); // number of given positive examples
		writer.print("lengthOfPositiveExamples,"); // number of bytes in all positive examples
		writer.print("lengthOfCorpus"); // number of bytes in the corpus
		writer.println();
	}

	private static void outputStats(Job job, String solution) throws IOException {
		writer.printf("\"%s\",", job.getName());
		writer.printf("%d,", diag.registry().getInt("size", 0));
		writer.printf("\"%s\",", (solution == null) ? "" : solution);
		writer.printf("%dms,", diag.timing().getTiming("timeTotal", Timing.Format.MilliSec));
		writer.printf("%dms,", diag.timing().getTiming("timeToFirstSol", Timing.Format.MilliSec));
		writer.printf("%d,", diag.registry().getInt("templatesToFirstSol", 0));
		writer.printf("%d,", diag.registry().getInt("templatesTotal", 0));
		writer.printf("%d,", diag.registry().getInt("costOfFirstSol", 0));
		// sizeOfSearchSpace
		// timeTotalNoOptimizations
		writer.printf("%dms,", diag.timing().getTiming("timeSATSolver", Timing.Format.MilliSec));
		writer.printf("%dms,", diag.timing().getTiming("timeDotTest", Timing.Format.MilliSec));
		writer.printf("%dms,", diag.timing().getTiming("timeDotStarTest", Timing.Format.MilliSec));
		writer.printf("%dms,", diag.timing().getTiming("timeEmptySetTest", Timing.Format.MilliSec));
		writer.printf("%d,", diag.registry().getInt("totalDotTests", 0));
		writer.printf("%d,", diag.registry().getInt("totalDotStarTests", 0));
		writer.printf("%d,", diag.registry().getInt("totalEmptySetTests", 0));
		writer.printf("%d,", diag.registry().getInt("totalDotTestsRejects", 0));
		writer.printf("%d,", diag.registry().getInt("totalDotStarTestsRejects", 0));
		writer.printf("%d,", diag.registry().getInt("totalEmptySetTestsRejects", 0));
		writer.printf("%d,", diag.registry().getInt("maximumRoutes", 0));
		writer.printf("%d,", job.getCorpus().getPositiveRanges().size());
		writer.printf("%d,", job.getCorpus().getTotalCharsInPositiveExamples());
		writer.printf("%d", job.getCorpus().getCorpusLength());
		writer.println();
	}

}
