package sun.print;

import java.util.ArrayList;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.standard.MediaTray;

public class Win32MediaTray extends MediaTray
{
  static final Win32MediaTray ENVELOPE_MANUAL = new Win32MediaTray(0, 6);
  static final Win32MediaTray AUTO = new Win32MediaTray(1, 7);
  static final Win32MediaTray TRACTOR = new Win32MediaTray(2, 8);
  static final Win32MediaTray SMALL_FORMAT = new Win32MediaTray(3, 9);
  static final Win32MediaTray LARGE_FORMAT = new Win32MediaTray(4, 10);
  static final Win32MediaTray FORMSOURCE = new Win32MediaTray(5, 15);
  private static ArrayList winStringTable = new ArrayList();
  private static ArrayList winEnumTable = new ArrayList();
  public int winID;
  private static final String[] myStringTable = { "Manual-Envelope", "Automatic-Feeder", "Tractor-Feeder", "Small-Format", "Large-Format", "Form-Source" };
  private static final MediaTray[] myEnumValueTable = { ENVELOPE_MANUAL, AUTO, TRACTOR, SMALL_FORMAT, LARGE_FORMAT, FORMSOURCE };

  private Win32MediaTray(int paramInt1, int paramInt2)
  {
    super(paramInt1);
    this.winID = paramInt2;
  }

  private static synchronized int nextValue(String paramString)
  {
    winStringTable.add(paramString);
    return (getTraySize() - 1);
  }

  protected Win32MediaTray(int paramInt, String paramString)
  {
    super(nextValue(paramString));
    this.winID = paramInt;
    winEnumTable.add(this);
  }

  protected static int getTraySize()
  {
    return (myStringTable.length + winStringTable.size());
  }

  protected String[] getStringTable()
  {
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; i < myStringTable.length; ++i)
      localArrayList.add(myStringTable[i]);
    localArrayList.addAll(winStringTable);
    String[] arrayOfString = new String[localArrayList.size()];
    return ((String[])(String[])localArrayList.toArray(arrayOfString));
  }

  protected EnumSyntax[] getEnumValueTable()
  {
    ArrayList localArrayList = new ArrayList();
    for (int i = 0; i < myEnumValueTable.length; ++i)
      localArrayList.add(myEnumValueTable[i]);
    localArrayList.addAll(winEnumTable);
    MediaTray[] arrayOfMediaTray = new MediaTray[localArrayList.size()];
    return ((MediaTray[])(MediaTray[])localArrayList.toArray(arrayOfMediaTray));
  }
}