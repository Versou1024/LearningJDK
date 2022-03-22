package sun.print;

import java.util.ArrayList;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaTray;

class CustomMediaTray extends MediaTray
{
  private static ArrayList customStringTable = new ArrayList();
  private static ArrayList customEnumTable = new ArrayList();
  private String choiceName;
  private static final long serialVersionUID = 1019451298193987013L;

  private CustomMediaTray(int paramInt)
  {
    super(paramInt);
  }

  private static synchronized int nextValue(String paramString)
  {
    customStringTable.add(paramString);
    return (customStringTable.size() - 1);
  }

  public CustomMediaTray(String paramString1, String paramString2)
  {
    super(nextValue(paramString1));
    this.choiceName = paramString2;
    customEnumTable.add(this);
  }

  public String getChoiceName()
  {
    return this.choiceName;
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
    MediaTray[] arrayOfMediaTray = new MediaTray[customEnumTable.size()];
    return ((MediaTray[])(MediaTray[])customEnumTable.toArray(arrayOfMediaTray));
  }
}