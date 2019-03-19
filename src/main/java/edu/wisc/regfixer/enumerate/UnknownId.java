package edu.wisc.regfixer.enumerate;

public class UnknownId {
  private int id;
  private Unknown unknown;

  public UnknownId (Unknown unknown) {
    this.id = UnknownId.getNextId();
    this.unknown = unknown;
  }

  public Unknown getUnknown () {
    return this.unknown;
  }

  @Override
  public boolean equals (Object obj) {
    if (obj instanceof UnknownId) {
      return (this.id == ((UnknownId) obj).id);
    }

    return false;
  }

  @Override
  public int hashCode () {
    return this.id;
  }

  @Override
  public String toString () {
    return String.format("H%d", this.id);
  }

  private static int nextId = 0;

  private static int getNextId () {
    return UnknownId.nextId++;
  }
}
