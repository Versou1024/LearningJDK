package sun.tools.jar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

public class CommandLine
{
  public static String[] parse(String[] paramArrayOfString)
    throws IOException
  {
    ArrayList localArrayList = new ArrayList(paramArrayOfString.length);
    for (int i = 0; i < paramArrayOfString.length; ++i)
    {
      String str = paramArrayOfString[i];
      if ((str.length() > 1) && (str.charAt(0) == '@'))
      {
        str = str.substring(1);
        if (str.charAt(0) == '@')
          localArrayList.add(str);
        else
          loadCmdFile(str, localArrayList);
      }
      else
      {
        localArrayList.add(str);
      }
    }
    return ((String[])(String[])localArrayList.toArray(new String[localArrayList.size()]));
  }

  private static void loadCmdFile(String paramString, List paramList)
    throws IOException
  {
    BufferedReader localBufferedReader = new BufferedReader(new FileReader(paramString));
    StreamTokenizer localStreamTokenizer = new StreamTokenizer(localBufferedReader);
    localStreamTokenizer.resetSyntax();
    localStreamTokenizer.wordChars(32, 255);
    localStreamTokenizer.whitespaceChars(0, 32);
    localStreamTokenizer.commentChar(35);
    localStreamTokenizer.quoteChar(34);
    localStreamTokenizer.quoteChar(39);
    while (localStreamTokenizer.nextToken() != -1)
      paramList.add(localStreamTokenizer.sval);
    localBufferedReader.close();
  }
}