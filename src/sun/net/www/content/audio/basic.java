package sun.net.www.content.audio;

import java.io.IOException;
import java.net.ContentHandler;
import java.net.URLConnection;
import sun.applet.AppletAudioClip;

public class basic extends ContentHandler
{
  public Object getContent(URLConnection paramURLConnection)
    throws IOException
  {
    return new AppletAudioClip(paramURLConnection);
  }
}