package sun.awt.image;

import java.util.Vector;
import sun.awt.AppContext;

class FetcherInfo
{
  static final int MAX_NUM_FETCHERS_PER_APPCONTEXT = 4;
  Thread[] fetchers = new Thread[4];
  int numFetchers = 0;
  int numWaiting = 0;
  Vector waitList = new Vector();
  private static final Object FETCHER_INFO_KEY = new StringBuffer("FetcherInfo");

  static FetcherInfo getFetcherInfo()
  {
    AppContext localAppContext1 = AppContext.getAppContext();
    synchronized (localAppContext1)
    {
      FetcherInfo localFetcherInfo = (FetcherInfo)localAppContext1.get(FETCHER_INFO_KEY);
      if (localFetcherInfo == null)
      {
        localFetcherInfo = new FetcherInfo();
        localAppContext1.put(FETCHER_INFO_KEY, localFetcherInfo);
      }
      return localFetcherInfo;
    }
  }
}