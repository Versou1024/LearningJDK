package sun.print;

import java.util.ArrayList;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;

class CustomMediaSizeName extends MediaSizeName
{
  private static ArrayList customStringTable = new ArrayList();
  private static ArrayList customEnumTable = new ArrayList();
  private String choiceName;
  private MediaSizeName mediaName;
  private static final long serialVersionUID = 7412807582228043717L;

  private CustomMediaSizeName(int paramInt)
  {
    super(paramInt);
  }

  private static synchronized int nextValue(String paramString)
  {
    customStringTable.add(paramString);
    return (customStringTable.size() - 1);
  }

  public CustomMediaSizeName(String paramString)
  {
    super(nextValue(paramString));
    customEnumTable.add(this);
    this.choiceName = null;
    this.mediaName = null;
  }

  public CustomMediaSizeName(String paramString1, String paramString2, float paramFloat1, float paramFloat2)
  {
    super(nextValue(paramString1));
    this.choiceName = paramString2;
    customEnumTable.add(this);
    this.mediaName = null;
    try
    {
      this.mediaName = MediaSize.findMedia(paramFloat1, paramFloat2, 25400);
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
    }
  }

  public String getChoiceName()
  {
    return this.choiceName;
  }

  public MediaSizeName getStandardMedia()
  {
    return this.mediaName;
  }

  public static MediaSizeName findMedia(Media[] paramArrayOfMedia, float paramFloat1, float paramFloat2, int paramInt)
  {
    if ((paramFloat1 <= 0F) || (paramFloat2 <= 0F) || (paramInt < 1))
      throw new IllegalArgumentException("args must be +ve values");
    if ((paramArrayOfMedia == null) || (paramArrayOfMedia.length == 0))
      throw new IllegalArgumentException("args must have valid array of media");
    int i = 0;
    MediaSizeName[] arrayOfMediaSizeName = new MediaSizeName[paramArrayOfMedia.length];
    for (int j = 0; j < paramArrayOfMedia.length; ++j)
      if (paramArrayOfMedia[j] instanceof MediaSizeName)
        arrayOfMediaSizeName[(i++)] = ((MediaSizeName)paramArrayOfMedia[j]);
    if (i == 0)
      return null;
    j = 0;
    double d1 = paramFloat1 * paramFloat1 + paramFloat2 * paramFloat2;
    float f1 = paramFloat1;
    float f2 = paramFloat2;
    for (int k = 0; k < i; ++k)
    {
      MediaSize localMediaSize = MediaSize.getMediaSizeForName(arrayOfMediaSizeName[k]);
      if (localMediaSize == null)
        break label230:
      float[] arrayOfFloat = localMediaSize.getSize(paramInt);
      if ((paramFloat1 == arrayOfFloat[0]) && (paramFloat2 == arrayOfFloat[1]))
      {
        j = k;
        break;
      }
      f1 = paramFloat1 - arrayOfFloat[0];
      f2 = paramFloat2 - arrayOfFloat[1];
      double d2 = f1 * f1 + f2 * f2;
      if (d2 < d1)
      {
        d1 = d2;
        label230: j = k;
      }
    }
    return arrayOfMediaSizeName[j];
  }

  public Media[] getSuperEnumTable()
  {
    return ((Media[])(Media[])super.getEnumValueTable());
  }

  protected String[] getStringTable()
  {
    String[] arrayOfString = new String[customStringTable.size()];
    return ((String[])(String[])customStringTable.toArray(arrayOfString));
  }

  protected EnumSyntax[] getEnumValueTable()
  {
    MediaSizeName[] arrayOfMediaSizeName = new MediaSizeName[customEnumTable.size()];
    return ((MediaSizeName[])(MediaSizeName[])customEnumTable.toArray(arrayOfMediaSizeName));
  }
}