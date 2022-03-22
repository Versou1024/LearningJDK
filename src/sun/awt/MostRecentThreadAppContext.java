package sun.awt;

final class MostRecentThreadAppContext
{
  final Thread thread;
  final AppContext appContext;

  MostRecentThreadAppContext(Thread paramThread, AppContext paramAppContext)
  {
    this.thread = paramThread;
    this.appContext = paramAppContext;
  }
}