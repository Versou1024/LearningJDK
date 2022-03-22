package sun.print;

import javax.print.attribute.PrintServiceAttributeSet;

abstract interface AttributeUpdater
{
  public abstract PrintServiceAttributeSet getUpdatedAttributes();
}