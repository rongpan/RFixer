package edu.wisc.regfixer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.diagnostic.NullOutputStream;
import edu.wisc.regfixer.diagnostic.Registry;
import edu.wisc.regfixer.diagnostic.ReportStream;
import edu.wisc.regfixer.diagnostic.Timing;
import edu.wisc.regfixer.enumerate.Benchmark;
import edu.wisc.regfixer.enumerate.Job;
import edu.wisc.regfixer.global.Global;
import edu.wisc.regfixer.util.Ansi;

public class CLI {
  public static class TimingChannelValidator implements IValueValidator<List<String>> {
    public void validate(String name, List<String> channels) throws ParameterException {
      for (String channel : channels) {
        switch (channel) {
          case "testing":
          case "tracing":
          case "solving":
            break;
          default:
            String fmt = "Parameter 'timing' given unknown debugging channel '%s'";
            throw new ParameterException(String.format(fmt, channel));
        }
      }
    }
  }

  public static class DebugChannelValidator implements IValueValidator<List<String>> {
    public void validate(String name, List<String> channels) throws ParameterException {
      for (String channel : channels) {
        switch (channel) {
          case "none":
          case "vars":
          case "classes":
          case "formula":
          case "model":
          case "stats":
            break;
          default:
            String fmt = "Parameter 'debug' given unknown debugging channel '%s'";
            throw new ParameterException(String.format(fmt, channel));
        }
      }
    }
  }

  public static class OutputChannelValidator implements IValueValidator<List<String>> {
    public void validate(String name, List<String> channels) throws ParameterException {
      for (String channel : channels) {
        switch (channel) {
          case "none":
          case "csv":
          case "header":
          case "solution":
            break;
          default:
            String fmt = "Parameter 'output' given unknown output channel '%s'";
            throw new ParameterException(String.format(fmt, channel));
        }
      }
    }
  }

  public static class TestValidator implements IValueValidator<List<String>> {
    public void validate(String name, List<String> tests) throws ParameterException {
      for (String test : tests) {
        switch (test) {
          case "none":
          case "all":
          case "dot":
          case "dotstar":
          case "emptyset":
            break;
          default:
            String fmt = "Parameter 'tests' given unknown test name '%s'";
            throw new ParameterException(String.format(fmt, test));
        }
      }
    }
  }

  public static class LimitValidator implements IValueValidator<Integer> {
    public void validate(String name, Integer limit) throws ParameterException {
      if (limit == null) {
        throw new ParameterException("Parameter 'limit' cannot be null");
      } else if (limit <= 0) {
        throw new ParameterException("Parameter 'limit' must be greater than 0");
      }
    }
  }

  private static class ArgsRoot {
    @Parameter(names={"--help", "-h"})
    private boolean help = false;

    @Parameter(names={"--version", "-v"})
    private boolean version = false;

    // mode = 1 -> automata tracing
    // mode = 2 -> pair matching
    @Parameter(names={"--mode", "-m"})
    private int mode = 1;
    
    // max_sat = true -> use max_sat as in optimization
    @Parameter(names={"--max-sat", "-max"})
    private boolean max_sat = false;
    
    // base = true -> use baseLine implementation
    @Parameter(names={"--baseLine", "-base"})
    private boolean baseLine = false;
    
    // base = true -> use baseLine implementation
    @Parameter(names={"--cegis", "-c"})
    private boolean cegis = false;
    
    // base = true -> use baseLine implementation
    @Parameter(names={"--tutor", "-t"})
    private boolean tutor = false;
    
    @Parameter
    private List<String> catchall = new ArrayList<>();
  }

  @Parameters(separators="=")
  private static class ArgsServe {
    @Parameter(names="--port")
    private Integer port = null;

    @Parameter(names="--limit")
    private Integer limit = null;

    @Parameter(names="--open")
    private String open = null;

    @Parameter(names="--debug")
    private boolean debug = false;

    @Parameter
    private List<String> catchall = new ArrayList<>();
  }

  @Parameters(separators="=")
  private static class ArgsFix {
    @Parameter(names="--limit",
               validateValueWith=LimitValidator.class)
    private Integer limit = null;

    @Parameter(names="--timing",
               validateValueWith=TimingChannelValidator.class)
    private List<String> timingChannels = new ArrayList<>();

    @Parameter(names="--debug",
               validateValueWith=DebugChannelValidator.class)
    private List<String> debugChannels = new ArrayList<>();

    @Parameter(names="--output",
               validateValueWith=OutputChannelValidator.class)
    private List<String> outputChannels = new ArrayList<>();

    @Parameter(names="--tests",
               validateValueWith=TestValidator.class)
    private List<String> tests = new ArrayList<>();

    @Parameter(names="--file",
               required=true)
    private String file = null;
  }

  public static void main (String[] argv) {
    ArgsRoot root = new ArgsRoot();
    ArgsServe serve = new ArgsServe();
    ArgsFix fix = new ArgsFix();

    JCommander cli = JCommander.newBuilder()
      .programName("regfixer")
      .addObject(root)
      .addCommand("serve", serve)
      .addCommand("fix", fix)
      .build();

    cli.parse(argv);

    if (root.mode == 1)
    	Global.pairMode = false;
    else
    	Global.pairMode = true;
    
    if (root.max_sat)
    	Global.maxSat = true;
    
    if (root.baseLine)
    	Global.baseLine = true;
    
    if (root.cegis)
    	Global.cegis = true;
    
    if (root.tutor)
    	Global.tutor = true;
    
    if (root.help) {
      System.exit(handleHelp());
    }

    if (root.version) {
      System.exit(handleVersion());
    }

    if (cli.getParsedCommand() == null) {
      System.exit(handleHelp());
    }

    if (cli.getParsedCommand().equals("serve")) {
      if (handleServe(serve) == 0) {
        return;
      } else {
        System.exit(1);
      }
    }

    if (cli.getParsedCommand().equals("fix")) {
      System.exit(handleFix(fix));
    }
  }

  private static int handleHelp () {
    System.err.printf(
        "%n  Usage: regfixer [options]"
      + "%n         regfixer [command] [command options]"
      + "%n"
      + "%n"
      + "%n  Options:"
      + "%n    -h, --help     output usage information"
      + "%n    -v, --version  output the version number"
      + "%n"
      + "%n"
      + "%n  Commands:"
      + "%n    serve [options]"
      + "%n      --port <number>"
      + "%n          The port to listen for incoming connections. If this flag is not"
      + "%n          specified the PORT environment variable will be used instead. If"
      + "%n          neither the flag nor the variable are set, an error will occur."
      + "%n      --limit <number>"
      + "%n          The maximum number of unsuccessful enumeration cycles that occur"
      + "%n          before a TimeoutException is thrown and the job aborts without a"
      + "%n          final result. Default value is 1000."
      + "%n      --open <file> (not implemented)"
      + "%n          Read a benchmark file and use its contents as the initial values"
      + "%n          in the web-app. If this flag is not set, the web-app will launch"
      + "%n          from a clean state."
      + "%n      --debug"
      + "%n          When this flag is set, each repair request will produce a report"
      + "%n          that will be written to a file describing the steps used to find"
      + "%n          the repair. These reports will be written to disk with filenames"
      + "%n          in the format: 'debug_TIMESTAMP_HASH.txt' where TIMESTAMP is the"
      + "%n          UNIX timestamp (in milliseconds) and HASH is the first part of a"
      + "%n          SHA1 hash computed from formatting the repair requests inputs as"
      + "%n          a benchmark file."
      + "%n    fix [options] <file>"
      + "%n      --quiet"
      + "%n          If a solution is successfully determined, output only the result"
      + "%n          to STDOUT. If a solution could not be found before the algorithm"
      + "%n          timed out, print nothing and exit. This flag will stop any other"
      + "%n          debugging output from being printed."
      + "%n      --limit <number>"
      + "%n          The maximum number of unsuccessful enumeration cycles that occur"
      + "%n          before a TimeoutException is thrown and the job aborts without a"
      + "%n          final result. Default value is 1000."
      + "%n      --print-class-tree"
      + "%n          For all SAT formulae, print the character class trees that track"
      + "%n          how frequently each character class is used per unknown."
      + "%n      --print-formula"
      + "%n          For all SAT formulae, print the formula represented as a string."
      + "%n      --print-model"
      + "%n          For all SAT satisfiable formulae, print the model mapping all of"
      + "%n          formula's variables to their computed values."
      + "%n      <file>"
      + "%n          Read a benchmark file and use its contents to compute the repair"
      + "%n          of its regular expression. If this argument is not set, an error"
      + "%n          will occur. A description of the simple benchmark file format is"
      + "%n          included below."
      + "%n"
      + "%n"
      + "%n  Benchmark File Format:"
      + "%n    A benchmark file has 3 sections separated by dividers:"
      + "%n"
      + "%n    Regular Expression"
      + "%n      The first line of the file contains a single regular expression."
      + "%n"
      + "%n    Divider"
      + "%n      A single line that only contains 3 dashes: '---'"
      + "%n"
      + "%n    Set of 0+ Positive Matches"
      + "%n      Each positive match is contained on a separate line with the format:"
      + "%n      '(START_INDEX:END_INDEX)' where START_INDEX is the leftmost index of"
      + "%n      the match (inclusive), END_INDEX is the rightmost index (exclusive)."
      + "%n      Both indices are counted relative to the entire corpus."
      + "%n"
      + "%n    Divider"
      + "%n      A single line that only contains 3 dashes: '---'"
      + "%n"
      + "%n    Corpus"
      + "%n      All characters found after the line with the second divider are part"
      + "%n      of the corpus. The corpus has no restrictions on which characters it"
      + "%n      can contain or whitespace formatting. It is recommended that LF line"
      + "%n      separators be used for consistency and easier index counting."
      + "%n"
      + "%n"
    );

    return 0;
  }

  private static int handleVersion () {
    System.err.println("1.0.0");
    return 0;
  }

  private static int handleServe (ArgsServe args) {
    if (args.port == null) {
      if (System.getenv("PORT") == null) {
        System.err.println("no port given");
        return 1;
      } else {
        try {
          args.port = Integer.valueOf(System.getenv("PORT"));
        } catch (NumberFormatException ex) {
          System.err.println("no port given");
          return 1;
        }
      }
    }

    if (args.limit == null || args.limit < 1) {
      args.limit = -1;
    }

    Server.start(args.port, args.limit, args.debug);
    return 0;
  }

  private static int handleFix (ArgsFix args) {
    Job job = null;

    if (args.limit == null || args.limit < 1) {
      args.limit = -1;
    }

    boolean noDebug  = args.debugChannels.contains("none");
    ReportStream out = new ReportStream(noDebug
      ? new NullOutputStream()
      : System.out);

    // Create a registry of diagnostic-related command-line flags.
    Registry reg = new Registry();

    // Add debugging channels.
    for (String channel : args.debugChannels) {
      reg.setBool("debug-" + channel, true);
    }

    // Add output channels.
    for (String channel : args.outputChannels) {
      reg.setBool("output-" + channel, true);
    }

    // Add test flags.
    for (String test : args.tests) {
      reg.setBool("test-" + test, true);
    }

    // Create a diagnostic object to manage diagnostic flags and any debugging
    // output produced during execution.
    Timing tim = new Timing();
    Diagnostic diag = new Diagnostic(out, reg, tim);

    try {
      job = Benchmark.readFromFile(args.file);
    } catch (IOException ex) {
      System.err.println("unable to read file");
      return 1;
    }

    if (reg.getBool("output-header")) {
      System.out.print("name,");                         // name of benchmark file
      System.out.print("size,");                         // number of nodes in original regular expression
      System.out.print("solution,");                     // first computed expression (empty if no solution found)
      System.out.print("timeTotal,");                    // time between when enumeration started and when it ended (milliseconds)
      System.out.print("timeToFirstSol,");               // time between when enumeration started and the first solution (milliseconds)
      System.out.print("templatesToFirstSol,");          // number of templates explored before first solution
      System.out.print("templatesTotal,");               // number of templates explored in total
      System.out.print("costOfFirstSol,");               // expansion cost of first solution
      // System.out.print("sizeOfSearchSpace,");            // TODO
      // System.out.print("timeTotalNoOptimizations,");     // time for whole enumeration with no optimizations
      System.out.print("timeSATSolver,");                // time spent building & solving SAT formulae
      System.out.print("timeDotTest,");                  // time spent testing examples against the dot test
      System.out.print("timeDotStarTest,");              // time spent testing examples against the dot-star test
      System.out.print("timeEmptySetTest,");             // time spent testing examples against the empty-set test
      System.out.print("totalDotTests,");                // number of times the dot test was applied
      System.out.print("totalDotStarTests,");            // number of times the dot-star test was applied
      System.out.print("totalEmptySetTests,");           // number of times the empty-set test was applied
      System.out.print("totalDotTestsRejects,");         // number of times the dot test rejected a template
      System.out.print("totalDotStarTestsRejects,");     // number of times the dot-star test rejected a template
      System.out.print("totalEmptySetTestsRejects,");    // number of times the empty-set test rejected a template
      System.out.print("maximumRoutes,");                // maximum number of routes ever fed to the SAT solver from any template
      System.out.print("totalPositiveExamples,");        // number of given positive examples
      System.out.print("lengthOfPositiveExamples,");     // number of bytes in all positive examples
      System.out.print("lengthOfCorpus");                // number of bytes in the corpus
      System.out.println();
    }

    try {
      String result = RegFixer.fix(job, args.limit, diag);
      return result != null ? 0 : 1;
    } catch (TimeoutException ex) {
      System.out.println(ex.toString());
      System.out.println("TIMEOUT EXCEPTION");
      return 1;
    }
  }
}
