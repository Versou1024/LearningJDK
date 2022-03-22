package sun.text;

import sun.text.normalizer.NormalizerBase;
import sun.text.normalizer.NormalizerBase.Mode;

public class CollatorUtilities
{
  static NormalizerBase.Mode[] legacyModeMap = { NormalizerBase.NONE, NormalizerBase.NFD, NormalizerBase.NFKD };

  public static int toLegacyMode(NormalizerBase.Mode paramMode)
  {
    int i = legacyModeMap.length;
    do
      if (i <= 0)
        break;
    while (legacyModeMap[(--i)] != paramMode);
    return i;
  }

  public static NormalizerBase.Mode toNormalizerMode(int paramInt)
  {
    NormalizerBase.Mode localMode;
    try
    {
      localMode = legacyModeMap[paramInt];
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
      localMode = NormalizerBase.NONE;
    }
    return localMode;
  }
}