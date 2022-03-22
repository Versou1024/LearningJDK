package sun.text;

import java.text.Normalizer.Form;
import sun.text.normalizer.NormalizerBase;
import sun.text.normalizer.NormalizerImpl;

public final class Normalizer
{
  public static final int UNICODE_3_2 = 262432;

  public static String normalize(CharSequence paramCharSequence, Normalizer.Form paramForm, int paramInt)
  {
    return NormalizerBase.normalize(paramCharSequence.toString(), paramForm, paramInt);
  }

  public static boolean isNormalized(CharSequence paramCharSequence, Normalizer.Form paramForm, int paramInt)
  {
    return NormalizerBase.isNormalized(paramCharSequence.toString(), paramForm, paramInt);
  }

  public static final int getCombiningClass(int paramInt)
  {
    return NormalizerImpl.getCombiningClass(paramInt);
  }
}