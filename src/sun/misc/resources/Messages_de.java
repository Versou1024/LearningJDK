package sun.misc.resources;

import java.util.ListResourceBundle;

public class Messages_de extends ListResourceBundle
{
  private static final Object[][] contents = { { "optpkg.versionerror", "FEHLER: In der JAR-Datei {0} wurde ein ungültiges Versionsformat verwendet. Prüfen Sie in der Dokumentation, welches Versionsformat unterstützt wird." }, { "optpkg.attributeerror", "FEHLER: In der JAR-Datei {1} ist das erforderliche JAR-Manifestattribut {0} nicht gesetzt." }, { "optpkg.attributeserror", "FEHLER: In der JAR-Datei {0} sind einige erforderliche JAR-Manifestattribute nicht gesetzt." } };

  public Object[][] getContents()
  {
    return contents;
  }
}