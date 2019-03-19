package edu.wisc.regfixer.enumerate;

import java.util.regex.Pattern;
import java.util.Set;

import edu.wisc.regfixer.parser.RegexNode;
import org.apache.commons.codec.digest.DigestUtils;

public class Job {
  private final String name;
  private final RegexNode tree;
  private final Corpus corpus;

  public Job (String name, String regex, String corpus, Set<Range> positives) {
    this.name = name;

    try {
      this.tree = edu.wisc.regfixer.parser.Main.parse(regex);
    } catch (Exception ex) {
      // FIXME
      throw new RuntimeException("malformed regular expression");
    }

    Pattern pattern = Pattern.compile(this.tree.toString());
    Set<Range> negatives = Corpus.inferNegativeRanges(pattern, corpus, positives);
    this.corpus = new Corpus(corpus, positives, negatives);
  }

  public Job (String name, String regex, String corpus, Set<Range> positives, Set<Range> negatives) {
    this.name = name;

    try {
      this.tree = edu.wisc.regfixer.parser.Main.parse(regex);
    } catch (Exception ex) {
      // FIXME
      throw new RuntimeException("malformed regular expression");
    }

    this.corpus = new Corpus(corpus, positives, negatives, true);
  }

  public Job (String name, RegexNode tree, Corpus corpus) {
    this.name = name;
    this.tree = tree;
    this.corpus = corpus;
  }

  public String getName () {
    return this.name;
  }

  public RegexNode getTree () {
    return this.tree;
  }

  public Corpus getCorpus () {
    return this.corpus;
  }

  public String toString () {
    StringBuilder builder = new StringBuilder();

    // Print regex string.
    builder.append(String.format("%s\n", this.getTree()));

    // Print range indices.
    builder.append(String.format("%s\n", Benchmark.boundary));
    for (Range range : this.getCorpus().getPositiveRanges()) {
      builder.append(String.format("%s\n", range));
    }

    // Print full corpus.
    builder.append(String.format("%s\n", Benchmark.boundary));
    builder.append(this.getCorpus());

    return builder.toString();
  }

  public String toDigest () {
    return DigestUtils.sha1Hex(this.toString());
  }
}
