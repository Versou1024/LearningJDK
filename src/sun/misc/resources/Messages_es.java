package sun.misc.resources;

import java.util.ListResourceBundle;

public class Messages_es extends ListResourceBundle
{
  private static final Object[][] contents = { { "optpkg.versionerror", "ERROR: El formato del archivo JAR {0} pertenece a una versión no válida. Busque en la documentación un formato de una versión compatible." }, { "optpkg.attributeerror", "ERROR: El atributo obligatorio JAR manifest {0} no está definido en el archivo JAR {1}." }, { "optpkg.attributeserror", "ERROR: Algunos atributos obligatorios JAR manifest no están definidos en el archivo JAR {0}." } };

  public Object[][] getContents()
  {
    return contents;
  }
}