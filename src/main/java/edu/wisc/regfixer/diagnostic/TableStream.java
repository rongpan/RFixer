package edu.wisc.regfixer.diagnostic;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.wisc.regfixer.util.PrintableTree;
import edu.wisc.regfixer.util.StringUtil;

public class TableStream extends PrintStream {
  private static class TableCol {
    private String name;
    private int width;
    private String fmt;
    private String underline;

    public TableCol (String name, int width) {
      this.name  = name;
      this.width = width;
      this.fmt   = String.format("%%-%ds", this.width);
    }

    public String getName () {
      return this.name;
    }

    public int getWidth () {
      return this.width;
    }

    public String toString (Object val) {
      return String.format(this.fmt, val.toString());
    }
  }

  private TableCol ordinal;
  private List<TableCol> cols;
  private int counter;
  private Object[] partialRow;
  private boolean freshLine = true;

  public TableStream (OutputStream out) {
    super(out);

    this.ordinal    = null;
    this.cols       = new LinkedList<>();
    this.counter    = 0;
    this.partialRow = null;
  }

  public int count () {
    return this.counter;
  }

  public void addOrdinalCol (String name, int width) {
    this.ordinal = new TableCol(name, width);
  }

  public void addCol (String name, int width) {
    this.cols.add(new TableCol(name, width));
  }

  public void printBreak () {
    String row = this.freshLine ? "" : "\n";

    if (this.ordinal != null) {
      row += "  ";
      row += this.ordinal.toString("");
      row += "  |";
    }

    this.println(row);
  }

  public void printHeader () {
    String names = this.freshLine ? "" : "\n";;
    String under = "";

    if (this.ordinal != null) {
      names += "  ";
      names += this.ordinal.toString(this.ordinal.getName());
      names += "  |";

      under += StringUtil.repeatString('-', names.length() - 1);
      under += "|";
    }

    for (TableCol col : this.cols) {
      names += "  ";
      names += col.toString(col.getName());
    }

    under += StringUtil.repeatString('-', names.length() - under.length() + 2);
    this.println(names);
    this.println(under);
  }

  public void printRow (Object... vals) {
    String row = this.freshLine ? "" : "\n";

    if (this.ordinal != null) {
      row += "  ";
      row += this.ordinal.toString(++this.counter);
      row += "  |";
    }

    for (int i = 0; i < vals.length && i < this.cols.size(); i++) {
      row += "  ";
      row += this.cols.get(i).toString(vals[i]);
    }

    this.println(row);
  }

  public void printPartialRow (Object... vals) {
    String row = "";

    if (this.ordinal != null) {
      row += "  ";
      row += this.ordinal.toString(++this.counter);
      row += "  |";
    }

    for (int i = 0; i < vals.length && i < this.cols.size(); i++) {
      row += "  ";
      row += this.cols.get(i).toString(vals[i]);
    }

    this.print(row);
    this.partialRow = vals;
    this.freshLine = false;
  }

  public void finishRow (Object... vals) {
    if (this.freshLine && this.partialRow != null) {
      this.counter--;
      this.printPartialRow(this.partialRow);
    }

    String row = "";
    int already = this.partialRow.length;

    for (int i = 0; i < vals.length && i < this.cols.size() - already; i++) {
      row += "  ";
      row += this.cols.get(i + already).toString(vals[i]);
    }

    this.println(row);
    this.partialRow = null;
  }

  public void printBlock (PrintableTree tree) {
    this.printBlock(PrintableTree.toString(tree));
  }

  public void printBlock (String block) {
    this.printBlock(block.split("\n"));
  }

  public void printBlock (String[] block) {
    if (this.freshLine == false) {
      this.println();
    }

    for (int i = 0; i < block.length; i++) {
      String row = "";

      if (this.ordinal != null) {
        row += "  ";
        row += this.ordinal.toString("");
        row += "  | ";
      }

      row += block[i];
      this.println(row);
    }
  }

  @Override
  public void println () {
    super.println();
    this.freshLine = true;
  }

  @Override
  public void println (String arg) {
    super.println(arg);
    this.freshLine = true;
  }
}
