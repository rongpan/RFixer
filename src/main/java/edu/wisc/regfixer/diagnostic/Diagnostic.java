package edu.wisc.regfixer.diagnostic;

public class Diagnostic {
  private ReportStream out;
  private Registry reg;
  private Timing tim;
  public Stat stat;

  public Diagnostic () {
    this.out = new ReportStream(System.out);
    this.reg = new Registry();
    this.tim = new Timing();
    this.stat = new Stat();
  }

  public Diagnostic (ReportStream out, Registry reg, Timing tim) {
    this.out = out;
    this.reg = reg;
    this.tim = tim;
    this.stat = new Stat();
  }
  
  public Diagnostic (ReportStream out) {
    this.out = out;
    this.reg = new Registry();
    this.tim = new Timing();
  }

  public ReportStream output () {
    return this.out;
  }

  public Registry registry () {
    return this.reg;
  }

  public boolean getBool (String name) {
    return this.reg.getBool(name);
  }

  public int getInt (String name) {
    return this.reg.getInt(name);
  }

  public String getStr (String name) {
    return this.reg.getStr(name);
  }

  public Timing timing () {
    return this.tim;
  }
  
  public void printStat() {
	long max = 0;
	String maxName = "";
	for (String name : this.stat.names) {
	  if (this.stat.getTime(name) > max) {
		  max = this.stat.getTime(name);
		  maxName = name;
	  }
      System.out.println("template: " + name + " size: " + this.stat.getSize(name) +
    		  " holes: " + this.stat.getHoles(name) + " time: " + this.stat.getTime(name) / 1e6 + "ms");
	}
	System.out.println("longest: #mn#" + maxName + "#mn# size: #ms#" + this.stat.getSize(maxName)
		+ "#ms# holes: #mh#" + this.stat.getHoles(maxName) + "#mh# time: #mt#" + max / 1e6 + "#mt#ms");
  }
  
}
