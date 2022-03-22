package sun.net.httpserver;

abstract interface TimeSource
{
  public abstract long getTime();
}