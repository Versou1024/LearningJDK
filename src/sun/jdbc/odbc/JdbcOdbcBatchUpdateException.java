package sun.jdbc.odbc;

import java.sql.BatchUpdateException;

public class JdbcOdbcBatchUpdateException extends BatchUpdateException
{
  int[] exceptionCounts;

  public JdbcOdbcBatchUpdateException(String paramString1, String paramString2, int paramInt, int[] paramArrayOfInt)
  {
    super(paramString1, paramString2, paramInt, paramArrayOfInt);
    this.exceptionCounts = paramArrayOfInt;
  }

  public JdbcOdbcBatchUpdateException(String paramString1, String paramString2, int[] paramArrayOfInt)
  {
    super(paramString1, paramString2, paramArrayOfInt);
    this.exceptionCounts = paramArrayOfInt;
  }

  public JdbcOdbcBatchUpdateException(String paramString, int[] paramArrayOfInt)
  {
    super(paramString, paramArrayOfInt);
    this.exceptionCounts = paramArrayOfInt;
  }

  public JdbcOdbcBatchUpdateException(int[] paramArrayOfInt)
  {
    super(paramArrayOfInt);
    this.exceptionCounts = paramArrayOfInt;
  }

  public JdbcOdbcBatchUpdateException()
  {
  }

  public int[] getUpdateCounts()
  {
    return this.exceptionCounts;
  }
}