# RegFixer

## Install

Install the latest version of [Maven](https://maven.apache.org/).

Run `mvn install` to compile & bundle the project. The finished JAR will be available in `target/`.

## Running on terminal

Running `java -jar target/regfixer.jar fix --limit 4000 --file tests/test_words.txt` should produce the following report:

```
Given the regular expression:

  \w\w\w

That that should match the strings:

  ✓ (8:11)   123
  ✓ (12:15)  456

And reject the strings:

  ✗ (16:19)  ghi
  ✗ (20:23)  5hh
  ✗ (24:27)  h5h
  ✗ (28:31)  hh5
  ✗ (32:35)  1hh
  ✗ (36:39)  4hh
  ✗ (40:43)  h2h
  ✗ (44:47)  h5h
  ✗ (48:51)  hh3
  ✗ (52:55)  hh6
  ✗ (56:59)  66h
  ✗ (60:63)  55h
  ✗ (64:67)  11h
  ✗ (68:71)  12h
  ✗ (72:75)  e33
  ✗ (76:79)  e25
  ✗ (80:83)  e23

Search through possible transformations:

  Order  |  Cost  Template                  Solution
---------|--------------------------------------------------------------------
  1      |  0     ■\w\w                     unsatisfiable SAT formula
  2      |  0     \w■\w                     unsatisfiable SAT formula
  3      |  0     ■\w                       failed dot test
  4      |  0     \w■                       failed dot test
  5      |  0     \w\w■                     unsatisfiable SAT formula
  6      |  1     \w■■\w                    failed dot test
  7      |  1     \w\w■■                    failed dot test
  8      |  1     \w\w\w■                   failed dot test
  9      |  1     \w■\w\w                   failed dot test
  10     |  1     \w\w■\w                   failed dot test

  (lines removed for brevity)

  3541   |  5     (■(■)+)+\w\w              failed dot test
  3542   |  5     ■|(■)?|(■)+\w\w           unsatisfiable SAT formula
  3543   |  5     \w■(■)?■|(■)?\w           no solution for some holes
  3544   |  5     \w(■)?(■|■)+\w            no solution for some holes
  3545   |  5     \w\w■■(■■)+               failed dot test
  3546   |  5     \w\w(■)?|((■)?■)?         unsatisfiable SAT formula
  3547   |  5     \w(■)*|■|■\w              unsatisfiable SAT formula
  3548   |  5     \w(■)?(■)?(■)?\w          no solution for some holes
  3549   |  5     \w\w■■|((■)?■)?           failed dot test
  3550   |  5     \w\w■|((■■)?■)?           unsatisfiable SAT formula
  3551   |  5     ■(■)+|(■)?\w\w            no solution for some holes
  3552   |  5     \w\w■|((■)?(■)?)?         unsatisfiable SAT formula
  3553   |  5     (■)?■                     failed dot test
  3554   |  5     ■■|■|(■)?|■\w\w           no solution for some holes
  3555   |  5     ■■|■|■■|■\w\w             no solution for some holes
  3556   |  5     (■)+■■\w\w\w              failed dot test
  3557   |  5     ■■|■|■|(■)?\w\w           no solution for some holes
  3558   |  5     ■■■                       \d\d\d

Results in the expression:

  \d\d\d

All done
```

## Running on local server (with JAR file)

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
