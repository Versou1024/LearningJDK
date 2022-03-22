package sun.jdbc.odbc;

public class JdbcOdbcTypeInfo extends JdbcOdbcObject
{
  String typeName;
  int precision;

  public void setName(String paramString)
  {
    this.typeName = paramString;
  }

  public String getName()
  {
    return this.typeName;
  }

  public void setPrec(int paramInt)
  {
    this.precision = paramInt;
  }

  public int getPrec()
  {
    return this.precision;
  }
}