package sun.jdbc.odbc;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public class JdbcOdbcResultSetMetaData extends JdbcOdbcObject
  implements ResultSetMetaData
{
  protected JdbcOdbc OdbcApi;
  protected JdbcOdbcResultSetInterface resultSet;
  protected long hStmt;

  public JdbcOdbcResultSetMetaData(JdbcOdbc paramJdbcOdbc, JdbcOdbcResultSetInterface paramJdbcOdbcResultSetInterface)
  {
    this.OdbcApi = paramJdbcOdbc;
    this.resultSet = paramJdbcOdbcResultSetInterface;
    this.hStmt = paramJdbcOdbcResultSetInterface.getHSTMT();
  }

  public int getColumnCount()
    throws SQLException
  {
    return this.resultSet.getColumnCount();
  }

  public boolean isAutoIncrement(int paramInt)
    throws SQLException
  {
    boolean bool;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.isAutoIncrement (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
      bool = false;
    else
      bool = getColAttributeBoolean(paramInt, 11);
    return bool;
  }

  public boolean isCaseSensitive(int paramInt)
    throws SQLException
  {
    boolean bool;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.isCaseSensitive (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
      bool = false;
    else
      bool = getColAttributeBoolean(paramInt, 12);
    return bool;
  }

  public boolean isSearchable(int paramInt)
    throws SQLException
  {
    int i;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.isSearchable (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
    {
      i = 0;
    }
    else
    {
      int j = getColAttribute(paramInt, 13);
      i = (j != 0) ? 1 : 0;
    }
    return i;
  }

  public boolean isCurrency(int paramInt)
    throws SQLException
  {
    boolean bool;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.isCurrency (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
      bool = false;
    else
      bool = getColAttributeBoolean(paramInt, 9);
    return bool;
  }

  public int isNullable(int paramInt)
    throws SQLException
  {
    int i;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.isNullable (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
      i = 0;
    else
      i = getColAttribute(paramInt, 7);
    return i;
  }

  public boolean isSigned(int paramInt)
    throws SQLException
  {
    int i;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.isSigned (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
      i = 0;
    else
      i = (!(getColAttributeBoolean(paramInt, 8))) ? 1 : 0;
    return i;
  }

  public int getColumnDisplaySize(int paramInt)
    throws SQLException
  {
    int i;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.getColumnDisplaySize (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    JdbcOdbcPseudoCol localJdbcOdbcPseudoCol = this.resultSet.getPseudoCol(paramInt);
    if (localJdbcOdbcPseudoCol != null)
      i = localJdbcOdbcPseudoCol.getColumnDisplaySize();
    else
      i = getColAttribute(paramInt, 6);
    return i;
  }

  public String getColumnLabel(int paramInt)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.getColumnLabel (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    JdbcOdbcPseudoCol localJdbcOdbcPseudoCol = this.resultSet.getPseudoCol(paramInt);
    if (localJdbcOdbcPseudoCol != null)
      str = localJdbcOdbcPseudoCol.getColumnLabel();
    else
      str = getColAttributeString(paramInt, 18);
    String str = this.resultSet.mapColumnName(str, paramInt);
    return str;
  }

  public String getColumnName(int paramInt)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.getColumnName (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    JdbcOdbcPseudoCol localJdbcOdbcPseudoCol = this.resultSet.getPseudoCol(paramInt);
    if (localJdbcOdbcPseudoCol != null)
      str = localJdbcOdbcPseudoCol.getColumnLabel();
    else
      str = getColAttributeString(paramInt, 1);
    String str = this.resultSet.mapColumnName(str, paramInt);
    return str;
  }

  public String getSchemaName(int paramInt)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.getSchemaName (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
      paramInt = 1;
    return getColAttributeString(paramInt, 16);
  }

  public int getPrecision(int paramInt)
    throws SQLException
  {
    int i;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.getPrecision (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    JdbcOdbcPseudoCol localJdbcOdbcPseudoCol = this.resultSet.getPseudoCol(paramInt);
    if (localJdbcOdbcPseudoCol != null)
      i = localJdbcOdbcPseudoCol.getColumnDisplaySize() - 1;
    else
      i = getColAttribute(paramInt, 4);
    return i;
  }

  public int getScale(int paramInt)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.getScale (" + paramInt + ")");
    return this.resultSet.getScale(paramInt);
  }

  public String getTableName(int paramInt)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.getTableName (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
      paramInt = 1;
    return getColAttributeString(paramInt, 15);
  }

  public String getCatalogName(int paramInt)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.getCatalogName (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
      paramInt = 1;
    return getColAttributeString(paramInt, 17);
  }

  public int getColumnType(int paramInt)
    throws SQLException
  {
    int i;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.getColumnType (" + paramInt + ")");
    JdbcOdbcPseudoCol localJdbcOdbcPseudoCol = this.resultSet.getPseudoCol(paramInt);
    if (localJdbcOdbcPseudoCol != null)
      i = localJdbcOdbcPseudoCol.getColumnType() - 1;
    else
      i = this.resultSet.getColumnType(paramInt);
    return i;
  }

  public String getColumnTypeName(int paramInt)
    throws SQLException
  {
    String str;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.getColumnTypeName (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
      str = "";
    else
      str = getColAttributeString(paramInt, 14);
    return str;
  }

  public boolean isReadOnly(int paramInt)
    throws SQLException
  {
    int i;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.isReadOnly (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
    {
      i = 1;
    }
    else
    {
      int j = getColAttribute(paramInt, 10);
      i = (j == 0) ? 1 : 0;
    }
    return i;
  }

  public boolean isWritable(int paramInt)
    throws SQLException
  {
    int i;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.isWritable (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
    {
      i = 0;
    }
    else
    {
      int j = getColAttribute(paramInt, 10);
      i = (j == 2) ? 1 : 0;
    }
    return i;
  }

  public boolean isDefinitelyWritable(int paramInt)
    throws SQLException
  {
    int i;
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.isDefinitelyWritable (" + paramInt + ")");
    paramInt = this.resultSet.mapColumn(paramInt);
    if (this.resultSet.getPseudoCol(paramInt) != null)
    {
      i = 0;
    }
    else
    {
      int j = getColAttribute(paramInt, 10);
      i = (j == 1) ? 1 : 0;
    }
    return i;
  }

  public String getColumnClassName(int paramInt)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*ResultSetMetaData.getColumnClassName (" + paramInt + ")");
    String str = new String().getClass().getName();
    int i = getColumnType(paramInt);
    switch (i)
    {
    case 2:
    case 3:
      str = new BigDecimal(0).getClass().getName();
      break;
    case -7:
      str = new Boolean(false).getClass().getName();
      break;
    case -6:
      str = new Byte("0").getClass().getName();
      break;
    case 5:
      str = new Short("0").getClass().getName();
      break;
    case 4:
      str = new Integer(0).getClass().getName();
      break;
    case -5:
      str = new Long(3412041381378523136L).getClass().getName();
      break;
    case 7:
      str = new Float(0F).getClass().getName();
      break;
    case 6:
    case 8:
      str = new Double(0D).getClass().getName();
      break;
    case -4:
    case -3:
    case -2:
      byte[] arrayOfByte = new byte[0];
      str = arrayOfByte.getClass().getName();
      break;
    case 91:
      str = new Date(123456L).getClass().getName();
      break;
    case 92:
      str = new Time(123456L).getClass().getName();
      break;
    case 93:
      str = new Timestamp(123456L).getClass().getName();
    }
    return str;
  }

  protected int getColAttribute(int paramInt1, int paramInt2)
    throws SQLException
  {
    return this.resultSet.getColAttribute(paramInt1, paramInt2);
  }

  protected boolean getColAttributeBoolean(int paramInt1, int paramInt2)
    throws SQLException
  {
    int i = getColAttribute(paramInt1, paramInt2);
    int j = 0;
    if (i == 1)
      j = 1;
    return j;
  }

  protected String getColAttributeString(int paramInt1, int paramInt2)
    throws SQLException
  {
    String str = "";
    this.resultSet.clearWarnings();
    try
    {
      str = this.OdbcApi.SQLColAttributesString(this.hStmt, paramInt1, paramInt2);
    }
    catch (JdbcOdbcSQLWarning localJdbcOdbcSQLWarning)
    {
      str = (String)localJdbcOdbcSQLWarning.value;
      this.resultSet.setWarning(JdbcOdbc.convertWarning(localJdbcOdbcSQLWarning));
    }
    return str.trim();
  }

  public <T> T unwrap(Class<T> paramClass)
    throws SQLException
  {
    return null;
  }

  public boolean isWrapperFor(Class<?> paramClass)
    throws SQLException
  {
    return false;
  }
}