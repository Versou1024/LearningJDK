package sun.java2d.pipe.hw;

import sun.java2d.pipe.BufferedContext;

public abstract interface BufferedContextProvider
{
  public abstract BufferedContext getContext();
}