package edu.wisc.regfixer.server;

import edu.wisc.regfixer.parser.RegexNode;

public class ResponsePayload {
  private final String fix;

  public ResponsePayload (RegexNode regex) {
    this.fix = regex.toString();
  }

  public ResponsePayload (String fix) {
    this.fix = fix;
  }
}
