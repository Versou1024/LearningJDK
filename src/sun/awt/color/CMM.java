package sun.awt.color;

import java.awt.color.CMMException;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.security.AccessController;
import sun.security.action.LoadLibraryAction;

public class CMM
{
  private static long ID = 3412045659165949952L;
  static final int cmmStatSuccess = 0;
  static final int cmmStatBadProfile = 503;
  static final int cmmStatBadTagData = 504;
  static final int cmmStatBadTagType = 505;
  static final int cmmStatBadTagId = 506;
  static final int cmmStatBadXform = 507;
  static final int cmmStatXformNotActive = 508;
  static final int cmmStatOutOfRange = 518;
  static final int cmmStatTagNotFound = 519;

  public static native int cmmLoadProfile(byte[] paramArrayOfByte, long[] paramArrayOfLong);

  public static native int cmmFreeProfile(long paramLong);

  public static native int cmmGetProfileSize(long paramLong, int[] paramArrayOfInt);

  public static native int cmmGetProfileData(long paramLong, byte[] paramArrayOfByte);

  public static native int cmmGetTagSize(long paramLong, int paramInt, int[] paramArrayOfInt);

  public static native int cmmGetTagData(long paramLong, int paramInt, byte[] paramArrayOfByte);

  public static native int cmmSetTagData(long paramLong, int paramInt, byte[] paramArrayOfByte);

  public static native int cmmGetTransform(ICC_Profile paramICC_Profile, int paramInt1, int paramInt2, ICC_Transform paramICC_Transform);

  public static native int cmmCombineTransforms(ICC_Transform[] paramArrayOfICC_Transform, ICC_Transform paramICC_Transform);

  public static native int cmmFreeTransform(long paramLong);

  public static native int cmmGetNumComponents(long paramLong, int[] paramArrayOfInt);

  public static native int cmmColorConvert(long paramLong, CMMImageLayout paramCMMImageLayout1, CMMImageLayout paramCMMImageLayout2);

  public static native int cmmFindICC_Profiles(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, String paramString, long[] paramArrayOfLong, int[] paramArrayOfInt);

  public static native int cmmCullICC_Profiles(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, long[] paramArrayOfLong1, long[] paramArrayOfLong2, int[] paramArrayOfInt);

  static native int cmmInit();

  static native int cmmTerminate();

  protected void finalize()
  {
    checkStatus(cmmTerminate());
  }

  public static void checkStatus(int paramInt)
  {
    if (paramInt != 0)
      throw new CMMException(errorString(paramInt));
  }

  static String errorString(int paramInt)
  {
    switch (paramInt)
    {
    case 0:
      return "Success";
    case 519:
      return "No such tag";
    case 503:
      return "Invalid profile data";
    case 504:
      return "Invalid tag data";
    case 505:
      return "Invalid tag type";
    case 506:
      return "Invalid tag signature";
    case 507:
      return "Invlaid transform";
    case 508:
      return "Transform is not active";
    case 518:
      return "Invalid image format";
    }
    return "General CMM error" + paramInt;
  }

  static
  {
    AccessController.doPrivileged(new LoadLibraryAction("cmm"));
    int i = cmmInit();
  }

  public static class CSAccessor
  {
    public static ColorSpace GRAYspace;
    public static ColorSpace LINEAR_RGBspace;
  }
}