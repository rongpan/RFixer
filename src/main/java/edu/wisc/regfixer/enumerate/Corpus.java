package edu.wisc.regfixer.enumerate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.TreeSet;

import edu.wisc.regfixer.parser.RegexNode;
import edu.wisc.regfixer.synthesize.Synthesis;

public class Corpus {
  private final String corpus;
  private final Set<Range> positiveRanges;
  private final Set<Range> negativeRanges;
  private final Set<String> positiveExamples;
  private final Set<String> negativeExamples;
  private final boolean hasAllNegative;

  public Corpus (String corpus, Set<Range> positives, Set<Range> negatives) {
    this(corpus, positives, negatives, false);
  }

  public Corpus (String corpus, Set<Range> positives, Set<Range> negatives, boolean hasAllNegative) {
    this.corpus = corpus;
    this.positiveRanges = new TreeSet<Range>(positives);
    this.negativeRanges = new TreeSet<Range>(negatives);
    this.hasAllNegative = hasAllNegative;

    this.positiveExamples = this.positiveRanges.stream()
      .map(r -> this.getSubstring(r))
      .collect(Collectors.toCollection(TreeSet::new));

    this.negativeExamples = this.negativeRanges.stream()
      .map(r -> this.getSubstring(r))
      .collect(Collectors.toCollection(TreeSet::new));
  }

  public String getCorpus () {
    return this.corpus;
  }

  public int getCorpusLength () {
    return this.corpus.length();
  }

  public int getTotalCharsInPositiveExamples () {
    int total = 0;
    for (Range p : this.positiveRanges) {
      total += p.length();
    }
    return total;
  }

  public String getSubstring (Range range) {
    return this.corpus.substring(range.getLeftIndex(), range.getRightIndex());
  }

  public Set<String> getSubstrings (Set<Range> ranges) {
    return ranges.stream()
      .map(r -> this.getSubstring(r))
      .collect(Collectors.toCollection(TreeSet::new));
  }

  public Set<Range> getPositiveRanges () {
    return this.positiveRanges;
  }

  public Set<Range> getNegativeRanges () {
    return this.negativeRanges;
  }

  public Set<String> getPositiveExamples () {
    return this.positiveExamples;
  }

  public Set<String> getNegativeExamples () {
    return this.negativeExamples;
  }

  public boolean hasAllNegativeExamples () {
    return this.hasAllNegative;
  }

  public boolean passesDotTest (Enumerant enumerant) {
    Pattern pattern = enumerant.toPattern(UnknownChar.FillType.Dot);
    return matchesStrings(pattern, this.positiveExamples);
  }

  public boolean passesDotStarTest (Enumerant enumerant) {
    Pattern pattern = enumerant.toPattern(UnknownChar.FillType.DotStar);
    return matchesStrings(pattern, this.positiveExamples);
  }

  public boolean passesEmptySetTest (Enumerant enumerant) {
    Pattern pattern = enumerant.toPattern(UnknownChar.FillType.EmptySet);
    return doesNotMatchStrings(pattern, this.negativeExamples);
  }

  public void addNegativeMatches (Set<Range> newNegatives) {
    this.negativeRanges.addAll(newNegatives);
  }

  public boolean noUnexpectedMatches (Synthesis synthesis) {
    Set<Range> ranges = getMatchingRanges(synthesis.toPattern(), this.corpus);
    return this.positiveRanges.containsAll(ranges);
  }

  public boolean isPerfectMatch (Synthesis synthesis) {
    Set<Range> ranges = getMatchingRanges(synthesis.toPattern(), this.corpus);
    return ranges.equals(this.positiveRanges);
  }

  public Set<Range> getMatches (Synthesis synthesis) {
    return getMatchingRanges(synthesis.toPattern(), this.corpus);
  }

  public Set<Range> getMatches (RegexNode tree) {
    Pattern p = Pattern.compile(tree.toString());
    return getMatchingRanges(p, this.corpus);
  }

  public Set<Range> getBadMatches (Synthesis synthesis) {
    Set<Range> ranges = getMatchingRanges(synthesis.toPattern(), this.corpus);
    ranges.removeAll(this.positiveRanges);
    return ranges;
  }

  public Set<Range> findUnexpectedMatches (Synthesis synthesis) {
    Set<Range> found = getMatchingRanges(synthesis.toPattern(), this.corpus);
    return Corpus.inferNegativeRanges(found, this.positiveRanges);
  }

  private boolean matchesStrings (Pattern pattern, Set<String> strings) {
    for (String s : strings) {
      if (false == pattern.matcher(s).matches()) {
        return false;
      }
    }

    return true;
  }

  private boolean doesNotMatchStrings (Pattern pattern, Set<String> strings) {
    for (String s : strings) {
      if (pattern.matcher(s).matches()) {
        return false;
      }
    }

    return true;
  }

  public static Set<Range> inferNegativeRanges (Pattern pattern, String corpus, Set<Range> positives) {
    Set<Range> found = getMatchingRanges(pattern, corpus);
    return inferNegativeRanges(found, positives);
  }

  public static Set<Range> inferNegativeRanges (Set<Range> found, Set<Range> expected) {
    List<Range> foundList = new LinkedList<>(found);
    List<Range> expectedList = new LinkedList<>(expected);
    return inferNegativeRanges(foundList, expectedList);
  }

  public static Set<Range> inferNegativeRanges (List<Range> found, List<Range> expected) {
    Set<Range> negatives = new TreeSet<>();

    Collections.sort(found);
    Collections.sort(expected);

    for (Range maybe : found) {
      if (isNegativeRange(maybe, expected)) {
        negatives.add(maybe);
      }
    }

    return negatives;
  }

  public static boolean isNegativeRange (Range maybe, List<Range> positives) {
    for (int i = 0; i < positives.size(); i++) {
      Range pos = positives.get(i);

      if (pos.equals(maybe)) {
        return false;
      }

      if (pos.getLeftIndex() == maybe.getLeftIndex()) {
        if (maybe.endsAfter(pos)) {
          return true;
        }

        return false;
      }

      if (maybe.intersects(pos)) {
        return false;
      }

      if (pos.endsBefore(maybe)) {
        if (i == positives.size() - 1) {
          return true;
        } else if (positives.get(i + 1).startsAfter(maybe)) {
          return true;
        }
      }
    }

    return false;
  }

  private static Set<Range> getMatchingRanges (Pattern pattern, String corpus) {
    Set<Range> ranges = new TreeSet<>();
    Matcher matcher = pattern.matcher(corpus);

    while (matcher.find()) {
      ranges.add(new Range(matcher.start(), matcher.end()));
    }

    return ranges;
  }
}
