package sun.print;

import java.util.ArrayList;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;

class Win32MediaSize extends MediaSizeName
{
  private static ArrayList winStringTable = new ArrayList();
  private static ArrayList winEnumTable = new ArrayList();

  private Win32MediaSize(int paramInt)
  {
    super(paramInt);
  }

  private static synchronized int nextValue(String paramString)
  {
    winStringTable.add(paramString);
    return (winStringTable.size() - 1);
  }

  public Win32MediaSize(String paramString)
  {
    super(nextValue(paramString));
    winEnumTable.add(this);
  }

  private MediaSizeName[] getSuperEnumTable()
  {
    return ((MediaSizeName[])(MediaSizeName[])super.getEnumValueTable());
  }

  protected String[] getStringTable()
  {
    String[] arrayOfString = new String[winStringTable.size()];
    return ((String[])(String[])winStringTable.toArray(arrayOfString));
  }

  protected EnumSyntax[] getEnumValueTable()
  {
    MediaSizeName[] arrayOfMediaSizeName = new MediaSizeName[winEnumTable.size()];
    return ((MediaSizeName[])(MediaSizeName[])winEnumTable.toArray(arrayOfMediaSizeName));
  }

  static
  {
    Win32MediaSize localWin32MediaSize = new Win32MediaSize(-1);
    MediaSizeName[] arrayOfMediaSizeName = localWin32MediaSize.getSuperEnumTable();
    if (arrayOfMediaSizeName != null)
    {
      Win32PrintService.predefMedia = new MediaSize[arrayOfMediaSizeName.length];
      for (int i = 0; i < arrayOfMediaSizeName.length; ++i)
        Win32PrintService.predefMedia[i] = MediaSize.getMediaSizeForName(arrayOfMediaSizeName[i]);
    }
  }
}