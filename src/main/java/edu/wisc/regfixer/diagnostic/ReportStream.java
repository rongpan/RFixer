package edu.wisc.regfixer.diagnostic;

import java.io.OutputStream;

import edu.wisc.regfixer.enumerate.Range;

public class ReportStream extends TableStream {
  public ReportStream (OutputStream out) {
    super(out);
    super.addOrdinalCol("Order", 5);
    super.addCol("Cost", 4);
    super.addCol("Template", 24);
    super.addCol("Solution", 32);
  }

  public void printSectionHeader (String message) {
    this.printf("\n%s\n\n", message);
  }

  public void printIndent (String message) {
    this.printf("  %s\n", message);
  }

  public void printExample (boolean isPositive, Range range, String example) {
    if (isPositive) {
      this.printIndent(String.format("✓ %-8s %s", range, example));
    } else {
      this.printIndent(String.format("✗ %-8s %s", range, example));
    }
  }
}