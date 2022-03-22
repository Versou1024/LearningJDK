package sun.security.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.HashMap;

public class ManifestDigester
{
  public static final String MF_MAIN_ATTRS = "Manifest-Main-Attributes";
  private byte[] rawBytes;
  private HashMap<String, Entry> entries;

  private boolean findSection(int paramInt, Position paramPosition)
  {
    int i = paramInt;
    int j = this.rawBytes.length;
    int k = paramInt;
    int l = 1;
    paramPosition.endOfFirstLine = -1;
    while (i < j)
    {
      int i1 = this.rawBytes[i];
      switch (i1)
      {
      case 13:
        if (paramPosition.endOfFirstLine == -1)
          paramPosition.endOfFirstLine = (i - 1);
        if ((i < j) && (this.rawBytes[(i + 1)] == 10))
          ++i;
      case 10:
        if (paramPosition.endOfFirstLine == -1)
          paramPosition.endOfFirstLine = (i - 1);
        if ((l != 0) || (i == j - 1))
        {
          if (i == j - 1)
            paramPosition.endOfSection = i;
          else
            paramPosition.endOfSection = k;
          paramPosition.startOfNext = (i + 1);
          return true;
        }
        k = i;
        l = 1;
        break;
      default:
        l = 0;
      }
      ++i;
    }
    return false;
  }

  public ManifestDigester(byte[] paramArrayOfByte)
  {
    this.rawBytes = paramArrayOfByte;
    this.entries = new HashMap();
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    Position localPosition = new Position();
    if (!(findSection(0, localPosition)))
      return;
    this.entries.put("Manifest-Main-Attributes", new Entry(0, localPosition.endOfSection + 1, localPosition.startOfNext, this.rawBytes));
    for (int i = localPosition.startOfNext; findSection(i, localPosition); i = localPosition.startOfNext)
    {
      int j = localPosition.endOfFirstLine - i + 1;
      int k = localPosition.endOfSection - i + 1;
      int l = localPosition.startOfNext - i;
      if ((j > 6) && (isNameAttr(paramArrayOfByte, i)))
      {
        StringBuilder localStringBuilder = new StringBuilder();
        try
        {
          localStringBuilder.append(new String(paramArrayOfByte, i + 6, j - 6, "UTF8"));
          int i1 = i + j;
          if (i1 - i < k)
            if (paramArrayOfByte[i1] == 13)
              i1 += 2;
            else
              ++i1;
          while ((i1 - i < k) && (paramArrayOfByte[(i1++)] == 32))
          {
            int i3;
            int i2 = i1;
            while ((i1 - i < k) && (paramArrayOfByte[(i1++)] != 10));
            if (paramArrayOfByte[(i1 - 1)] != 10)
              return;
            if (paramArrayOfByte[(i1 - 2)] == 13)
              i3 = i1 - i2 - 2;
            else
              i3 = i1 - i2 - 1;
            localStringBuilder.append(new String(paramArrayOfByte, i2, i3, "UTF8"));
          }
          this.entries.put(localStringBuilder.toString(), new Entry(i, k, l, this.rawBytes));
        }
        catch (UnsupportedEncodingException localUnsupportedEncodingException)
        {
          throw new IllegalStateException("UTF8 not available on platform");
        }
      }
    }
  }

  private boolean isNameAttr(byte[] paramArrayOfByte, int paramInt)
  {
    return ((((paramArrayOfByte[paramInt] == 78) || (paramArrayOfByte[paramInt] == 110))) && (((paramArrayOfByte[(paramInt + 1)] == 97) || (paramArrayOfByte[(paramInt + 1)] == 65))) && (((paramArrayOfByte[(paramInt + 2)] == 109) || (paramArrayOfByte[(paramInt + 2)] == 77))) && (((paramArrayOfByte[(paramInt + 3)] == 101) || (paramArrayOfByte[(paramInt + 3)] == 69))) && (paramArrayOfByte[(paramInt + 4)] == 58) && (paramArrayOfByte[(paramInt + 5)] == 32));
  }

  public Entry get(String paramString, boolean paramBoolean)
  {
    Entry localEntry = (Entry)this.entries.get(paramString);
    if (localEntry != null)
      localEntry.oldStyle = paramBoolean;
    return localEntry;
  }

  public byte[] manifestDigest(MessageDigest paramMessageDigest)
  {
    paramMessageDigest.reset();
    paramMessageDigest.update(this.rawBytes, 0, this.rawBytes.length);
    return paramMessageDigest.digest();
  }

  public static class Entry
  {
    int offset;
    int length;
    int lengthWithBlankLine;
    byte[] rawBytes;
    boolean oldStyle;

    public Entry(int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte)
    {
      this.offset = paramInt1;
      this.length = paramInt2;
      this.lengthWithBlankLine = paramInt3;
      this.rawBytes = paramArrayOfByte;
    }

    public byte[] digest(MessageDigest paramMessageDigest)
    {
      paramMessageDigest.reset();
      if (this.oldStyle)
        doOldStyle(paramMessageDigest, this.rawBytes, this.offset, this.lengthWithBlankLine);
      else
        paramMessageDigest.update(this.rawBytes, this.offset, this.lengthWithBlankLine);
      return paramMessageDigest.digest();
    }

    private void doOldStyle(MessageDigest paramMessageDigest, byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    {
      int i = paramInt1;
      int j = paramInt1;
      int k = paramInt1 + paramInt2;
      int l = -1;
      while (i < k)
      {
        if ((paramArrayOfByte[i] == 13) && (l == 32))
        {
          paramMessageDigest.update(paramArrayOfByte, j, i - j - 1);
          j = i;
        }
        l = paramArrayOfByte[i];
        ++i;
      }
      paramMessageDigest.update(paramArrayOfByte, j, i - j);
    }

    public byte[] digestWorkaround(MessageDigest paramMessageDigest)
    {
      paramMessageDigest.reset();
      paramMessageDigest.update(this.rawBytes, this.offset, this.length);
      return paramMessageDigest.digest();
    }
  }

  static class Position
  {
    int endOfFirstLine;
    int endOfSection;
    int startOfNext;
  }
}