package sun.management.counter;

import java.io.Serializable;

public abstract interface Counter extends Serializable
{
  public abstract String getName();

  public abstract Units getUnits();

  public abstract Variability getVariability();

  public abstract boolean isVector();

  public abstract int getVectorLength();

  public abstract Object getValue();

  public abstract boolean isInternal();

  public abstract int getFlags();
}