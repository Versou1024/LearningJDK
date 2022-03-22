package sun.misc.resources;

import java.util.ListResourceBundle;

public class Messages_fr extends ListResourceBundle
{
  private static final Object[][] contents = { { "optpkg.versionerror", "ERREUR : Format de version utilisé pour le fichier JAR {0} non valide. Consultez la documentation pour voir le format de version pris en charge." }, { "optpkg.attributeerror", "ERREUR : L''attribut manifeste JAR {0} nécessaire n''est pas défini pour le fichier {1}. " }, { "optpkg.attributeserror", "ERREUR : Certains attributs manifeste JAR {0} nécessaires ne sont pas définis pour le fichier {1}. " } };

  public Object[][] getContents()
  {
    return contents;
  }
}