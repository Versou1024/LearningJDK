package sun.net.www;

import java.util.Iterator;

public class HeaderParser
{
  String raw;
  String[][] tab;
  int nkeys;
  int asize = 10;

  public HeaderParser(String paramString)
  {
    this.raw = paramString;
    this.tab = new String[this.asize][2];
    parse();
  }

  private HeaderParser()
  {
  }

  public HeaderParser subsequence(int paramInt1, int paramInt2)
  {
    if ((paramInt1 == 0) && (paramInt2 == this.nkeys))
      return this;
    if ((paramInt1 < 0) || (paramInt1 >= paramInt2) || (paramInt2 > this.nkeys))
      throw new IllegalArgumentException("invalid start or end");
    HeaderParser localHeaderParser = new HeaderParser();
    localHeaderParser.tab = new String[this.asize][2];
    localHeaderParser.asize = this.asize;
    System.arraycopy(this.tab, paramInt1, localHeaderParser.tab, 0, paramInt2 - paramInt1);
    localHeaderParser.nkeys = (paramInt2 - paramInt1);
    return localHeaderParser;
  }

  private void parse()
  {
    if (this.raw != null)
    {
      this.raw = this.raw.trim();
      char[] arrayOfChar = this.raw.toCharArray();
      int i = 0;
      int j = 0;
      int k = 0;
      int l = 1;
      int i1 = 0;
      int i2 = arrayOfChar.length;
      while (true)
      {
        while (true)
        {
          if (j >= i2)
            break label362;
          int i3 = arrayOfChar[j];
          if ((i3 == 61) && (i1 == 0))
          {
            this.tab[k][0] = new String(arrayOfChar, i, j - i).toLowerCase();
            l = 0;
            i = ++j;
            break label307:
          }
          if (i3 == 34)
          {
            if (i1 != 0)
            {
              this.tab[(k++)][1] = new String(arrayOfChar, i, j - i);
              i1 = 0;
              do;
              while ((++j < i2) && (((arrayOfChar[j] == ' ') || (arrayOfChar[j] == ','))));
              l = 1;
              i = j;
              break label307:
            }
            i1 = 1;
            i = ++j;
            break label307:
          }
          if ((i3 != 32) && (i3 != 44))
            break label304;
          if (i1 == 0)
            break;
          ++j;
        }
        if (l != 0)
          this.tab[(k++)][0] = new String(arrayOfChar, i, j - i).toLowerCase();
        else
          this.tab[(k++)][1] = new String(arrayOfChar, i, j - i);
        while ((j < i2) && (((arrayOfChar[j] == ' ') || (arrayOfChar[j] == ','))))
          ++j;
        l = 1;
        i = j;
        label304: ++j;
        if (k == this.asize)
        {
          label307: this.asize *= 2;
          String[][] arrayOfString = new String[this.asize][2];
          System.arraycopy(this.tab, 0, arrayOfString, 0, this.tab.length);
          this.tab = arrayOfString;
        }
      }
      if (--j > i)
        if (l == 0)
          if (arrayOfChar[j] == '"')
            label362: this.tab[(k++)][1] = new String(arrayOfChar, i, j - i);
          else
            this.tab[(k++)][1] = new String(arrayOfChar, i, j - i + 1);
        else
          this.tab[(k++)][0] = new String(arrayOfChar, i, j - i + 1).toLowerCase();
      else if (j == i)
        if (l == 0)
          if (arrayOfChar[j] == '"')
            this.tab[(k++)][1] = String.valueOf(arrayOfChar[(j - 1)]);
          else
            this.tab[(k++)][1] = String.valueOf(arrayOfChar[j]);
        else
          this.tab[(k++)][0] = String.valueOf(arrayOfChar[j]).toLowerCase();
      this.nkeys = k;
    }
  }

  public String findKey(int paramInt)
  {
    if ((paramInt < 0) || (paramInt > this.asize))
      return null;
    return this.tab[paramInt][0];
  }

  public String findValue(int paramInt)
  {
    if ((paramInt < 0) || (paramInt > this.asize))
      return null;
    return this.tab[paramInt][1];
  }

  public String findValue(String paramString)
  {
    return findValue(paramString, null);
  }

  public String findValue(String paramString1, String paramString2)
  {
    if (paramString1 == null)
      return paramString2;
    paramString1 = paramString1.toLowerCase();
    for (int i = 0; i < this.asize; ++i)
    {
      if (this.tab[i][0] == null)
        return paramString2;
      if (paramString1.equals(this.tab[i][0]))
        return this.tab[i][1];
    }
    return paramString2;
  }

  public Iterator keys()
  {
    return new ParserIterator(this, false);
  }

  public Iterator values()
  {
    return new ParserIterator(this, true);
  }

  public String toString()
  {
    Iterator localIterator = keys();
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append("{size=" + this.asize + " nkeys=" + this.nkeys + " ");
    for (int i = 0; localIterator.hasNext(); ++i)
    {
      String str1 = (String)localIterator.next();
      String str2 = findValue(i);
      if ((str2 != null) && ("".equals(str2)))
        str2 = null;
      localStringBuffer.append(" {" + str1 + ((str2 == null) ? "" : new StringBuilder().append(",").append(str2).toString()) + "}");
      if (localIterator.hasNext())
        localStringBuffer.append(",");
    }
    localStringBuffer.append(" }");
    return new String(localStringBuffer);
  }

  public int findInt(String paramString, int paramInt)
  {
    try
    {
      return Integer.parseInt(findValue(paramString, String.valueOf(paramInt)));
    }
    catch (Throwable localThrowable)
    {
    }
    return paramInt;
  }

  class ParserIterator
  implements Iterator
  {
    int index;
    boolean returnsValue;

    ParserIterator(, boolean paramBoolean)
    {
      this.returnsValue = paramBoolean;
    }

    public boolean hasNext()
    {
      return (this.index < this.this$0.nkeys);
    }

    public Object next()
    {
      return this.this$0.tab[(this.index++)][0];
    }

    public void remove()
    {
      throw new UnsupportedOperationException("remove not supported");
    }
  }
}