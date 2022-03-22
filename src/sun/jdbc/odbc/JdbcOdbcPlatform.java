package sun.jdbc.odbc;

public class JdbcOdbcPlatform
{
  static final int sizeofSQLLEN = JdbcOdbc.getSQLLENSize();

  public static boolean is32BitPlatform()
  {
    return (sizeofSQLLEN == 4);
  }

  public static boolean is64BitPlatform()
  {
    return (sizeofSQLLEN == 8);
  }

  public static int getLengthBufferSize()
  {
    return sizeofSQLLEN;
  }

  public static byte[] convertIntToByteArray(int paramInt)
  {
    byte[] arrayOfByte = new byte[sizeofSQLLEN];
    JdbcOdbc.intToBytes(paramInt, arrayOfByte);
    return arrayOfByte;
  }

  public static byte[] convertLongToByteArray(long paramLong)
  {
    byte[] arrayOfByte = new byte[sizeofSQLLEN];
    JdbcOdbc.longToBytes(paramLong, arrayOfByte);
    return arrayOfByte;
  }
}