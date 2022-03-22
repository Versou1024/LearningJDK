package sun.management.counter;

public abstract class AbstractCounter
  implements Counter
{
  String name;
  Units units;
  Variability variability;
  int flags;
  int vectorLength;
  private static final long serialVersionUID = 6992337162326171013L;

  protected AbstractCounter(String paramString, Units paramUnits, Variability paramVariability, int paramInt1, int paramInt2)
  {
    this.name = paramString;
    this.units = paramUnits;
    this.variability = paramVariability;
    this.flags = paramInt1;
    this.vectorLength = paramInt2;
  }

  protected AbstractCounter(String paramString, Units paramUnits, Variability paramVariability, int paramInt)
  {
    this(paramString, paramUnits, paramVariability, paramInt, 0);
  }

  public String getName()
  {
    return this.name;
  }

  public Units getUnits()
  {
    return this.units;
  }

  public Variability getVariability()
  {
    return this.variability;
  }

  public boolean isVector()
  {
    return (this.vectorLength > 0);
  }

  public int getVectorLength()
  {
    return this.vectorLength;
  }

  public boolean isInternal()
  {
    return ((this.flags & 0x1) == 0);
  }

  public int getFlags()
  {
    return this.flags;
  }

  public abstract Object getValue();

  public String toString()
  {
    String str = getName() + ": " + getValue() + " " + getUnits();
    if (isInternal())
      return str + " [INTERNAL]";
    return str;
  }

  class Flags
  {
    static final int SUPPORTED = 1;
  }
}