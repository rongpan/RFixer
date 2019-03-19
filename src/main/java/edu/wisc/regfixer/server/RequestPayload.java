package edu.wisc.regfixer.server;

import java.util.Set;

import edu.wisc.regfixer.enumerate.Corpus;
import edu.wisc.regfixer.enumerate.Job;
import edu.wisc.regfixer.enumerate.Range;
import edu.wisc.regfixer.parser.Main;
import edu.wisc.regfixer.parser.RegexNode;

public class RequestPayload {
  private final String regex;
  private final Set<Range> ranges;
  private final String corpus;

  public RequestPayload (String regex, Set<Range> ranges, String corpus) {
    this.regex = regex;
    this.ranges = ranges;
    this.corpus = corpus;
  }

  public String getRegex () {
    return this.regex;
  }

  public Set<Range> getRanges () {
    return this.ranges;
  }

  public String getCorpus () {
    return this.corpus;
  }

  public Job toJob () throws Exception {
    return new Job("<API>", this.regex, this.corpus, this.ranges);
  }
}
