# RegFixer

## Install

Install the latest version of [Maven](https://maven.apache.org/).

Run `mvn install` to compile & bundle the project. The finished JAR will be available in `target/`.

## Running on terminal

Running `java -jar target/regfixer.jar -m [modeNum] -max -base -c -t fix --file [path to file]`

--mode / -m (default = 1) modeNum = 1 tells the synthesizer to use Automaton-directed SMT Encoding; modeNum = 2 tells the synthesizer to use Regular-expression-directed SMT Encoding.

--max-sat / -max (optional) If this flag is set, RFixer will search for the solution with Max-SMT objectives.

--baseLine / -base (optional) If this flag is set, RFixer will not use any template pruning technique.

--cegis / -c (optional) If this flag is set, RFixer will iteratively find the true repair.

--tutor / -t (optional) If this flag is set, RFixer will put additional constraints on the quantifiers.

The -c and -t flags should be used together for finding true repairs of AutomataTutor benchmarks.

## Folders

tests/benchmark_explicit : Regexlib benchmarks

tests/clean_AutoTutor : AutomataTutor benchmarks without underlying true repairs

tests/clean_AutoTutorWithTrue : AutomataTutor benchmarks with underlying true repairs (on the second line of each test file)

shells : scripts to run evals under each configutations

utils : scripts to generate statistics

## Running on local server (with JAR file) (not maintained)

`java -jar target/regfixer.jar serve --port=8080`

local web application available at http://localhost:8080/

## Comment on each file and directory in edu.wisc.regfixer

- automata: builds an automata with a regular expression
- enumerate: builds a list of regular expressions with hole
- parser: builds a regular expression node with a given expression
- server: contains custom error classes
- synthesize: given a regular expression node with hole and both positive and negative examples, it builds SAT formula and solves it if possible class exists
- util: bunch of util classes
- CLi.java: contains custom terminal commands
- RegFixer.java: class that calls fixing methods
- Server.java: class related with the configuration of server

## Note
- java classes that Isaac and Sang developed are in src/main/java/edu.wisc.regfixer, and rest directories/files are forked libraries
- the main class can be found in src/main/java/edu.wisc.regfixer/CLI
- nested call stacks can be track from there (‘handleFix’ method)
- It output more accurate result using Sang’s branch (major difference is SAT_Formula.java)
- Test result with the latest version can be found from attached excel file
- Z3 library is used for building & solving SAT formula (https://github.com/Z3Prover/z3)
