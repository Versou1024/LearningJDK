package sun.jdbc.odbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.SQLWarning;

public class JdbcOdbcDatabaseMetaData extends JdbcOdbcObject
  implements DatabaseMetaData
{
  protected JdbcOdbc OdbcApi;
  protected JdbcOdbcConnectionInterface Con;
  protected long hDbc;

  public JdbcOdbcDatabaseMetaData(JdbcOdbc paramJdbcOdbc, JdbcOdbcConnectionInterface paramJdbcOdbcConnectionInterface)
  {
    this.OdbcApi = paramJdbcOdbc;
    this.Con = paramJdbcOdbcConnectionInterface;
    this.hDbc = this.Con.getHDBC();
  }

  public boolean allProceduresAreCallable()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.allProceduresAreCallable");
    return getInfoBooleanString(20);
  }

  public boolean allTablesAreSelectable()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.allTablesAreSelectable");
    return getInfoBooleanString(19);
  }

  public String getURL()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getURL");
    return this.Con.getURL();
  }

  public String getUserName()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getUserName");
    return getInfoString(47);
  }

  public boolean isReadOnly()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.isReadOnly");
    return getInfoBooleanString(25);
  }

  public boolean nullsAreSortedHigh()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.nullsAreSortedHigh");
    int i = getInfoShort(85);
    return (i == 0);
  }

  public boolean nullsAreSortedLow()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.nullsAreSortedLow");
    int i = getInfo(85);
    return (i == 1);
  }

  public boolean nullsAreSortedAtStart()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.nullsAreSortedAtStart");
    int i = getInfo(85);
    return (i == 2);
  }

  public boolean nullsAreSortedAtEnd()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.nullsAreSortedAtEnd");
    int i = getInfo(85);
    return (i == 4);
  }

  public String getDatabaseProductName()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getDatabaseProductName");
    return getInfoString(17);
  }

  public String getDatabaseProductVersion()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getDatabaseProductVersion");
    return getInfoString(18);
  }

  public String getDriverName()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getDriverName");
    return "JDBC-ODBC Bridge (" + getInfoString(6) + ")";
  }

  public String getDriverVersion()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getDriverVersion");
    int i = getDriverMinorVersion();
    String str = "";
    if (i < 1000)
      str = str + "0";
    if (i < 100)
      str = str + "0";
    if (i < 10)
      str = str + "0";
    str = str + "" + i;
    return "" + getDriverMajorVersion() + "." + str + " (" + getInfoString(7) + ")";
  }

  public int getDriverMajorVersion()
  {
    return 2;
  }

  public int getDriverMinorVersion()
  {
    return 1;
  }

  public boolean usesLocalFiles()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.usesLocalFiles");
    int i = getInfoShort(84);
    return (i == 2);
  }

  public boolean usesLocalFilePerTable()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.usesLocalFilePerTable");
    int i = getInfoShort(84);
    return (i == 1);
  }

  public boolean supportsMixedCaseIdentifiers()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsMixedCaseIdentifiers");
    int i = getInfoShort(28);
    return (i == 3);
  }

  public boolean storesUpperCaseIdentifiers()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.storesUpperCaseIdentifiers");
    int i = getInfoShort(28);
    return (i == 1);
  }

  public boolean storesLowerCaseIdentifiers()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.storesLowerCaseIdentifiers");
    int i = getInfoShort(28);
    return (i == 2);
  }

  public boolean storesMixedCaseIdentifiers()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.storesMixedCaseIdentifiers");
    int i = getInfoShort(28);
    return (i == 4);
  }

  public boolean supportsMixedCaseQuotedIdentifiers()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsMixedCaseQuotedIdentifiers");
    int i = getInfoShort(93);
    return (i == 3);
  }

  public boolean storesUpperCaseQuotedIdentifiers()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.storesUpperCaseQuotedIdentifiers");
    int i = getInfoShort(93);
    return (i == 1);
  }

  public boolean storesLowerCaseQuotedIdentifiers()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.storesLowerCaseQuotedIdentifiers");
    int i = getInfoShort(93);
    return (i == 2);
  }

  public boolean storesMixedCaseQuotedIdentifiers()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.storesMixedCaseQuotedIdentifiers");
    int i = getInfoShort(93);
    return (i == 4);
  }

  public String getIdentifierQuoteString()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getIdentifierQuoteString");
    return getInfoString(29);
  }

  public String getSQLKeywords()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getSQLKeywords");
    return getInfoString(89, 16383);
  }

  public String getNumericFunctions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getNumericFunctions");
    String str = "";
    int i = getInfo(49);
    if ((i & 0x1) != 0)
      str = str + "ABS,";
    if ((i & 0x2) != 0)
      str = str + "ACOS,";
    if ((i & 0x4) != 0)
      str = str + "ASIN,";
    if ((i & 0x8) != 0)
      str = str + "ATAN,";
    if ((i & 0x10) != 0)
      str = str + "ATAN2,";
    if ((i & 0x20) != 0)
      str = str + "CEILING,";
    if ((i & 0x40) != 0)
      str = str + "COS,";
    if ((i & 0x80) != 0)
      str = str + "COT,";
    if ((i & 0x40000) != 0)
      str = str + "DEGREES,";
    if ((i & 0x100) != 0)
      str = str + "EXP,";
    if ((i & 0x200) != 0)
      str = str + "FLOOR,";
    if ((i & 0x400) != 0)
      str = str + "LOG,";
    if ((i & 0x80000) != 0)
      str = str + "LOG10,";
    if ((i & 0x800) != 0)
      str = str + "MOD,";
    if ((i & 0x10000) != 0)
      str = str + "PI,";
    if ((i & 0x100000) != 0)
      str = str + "POWER,";
    if ((i & 0x200000) != 0)
      str = str + "RADIANS,";
    if ((i & 0x20000) != 0)
      str = str + "RAND,";
    if ((i & 0x400000) != 0)
      str = str + "ROUND,";
    if ((i & 0x1000) != 0)
      str = str + "SIGN,";
    if ((i & 0x2000) != 0)
      str = str + "SIN,";
    if ((i & 0x4000) != 0)
      str = str + "SQRT,";
    if ((i & 0x8000) != 0)
      str = str + "TAN,";
    if ((i & 0x800000) != 0)
      str = str + "TRUNCATE,";
    if (str.length() > 0)
      str = str.substring(0, str.length() - 1);
    return str;
  }

  public String getStringFunctions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getStringFunctions");
    String str = "";
    int i = getInfo(50);
    if ((i & 0x2000) != 0)
      str = str + "ASCII,";
    if ((i & 0x4000) != 0)
      str = str + "CHAR,";
    if ((i & 0x1) != 0)
      str = str + "CONCAT,";
    if ((i & 0x8000) != 0)
      str = str + "DIFFERENCE,";
    if ((i & 0x2) != 0)
      str = str + "INSERT,";
    if ((i & 0x40) != 0)
      str = str + "LCASE,";
    if ((i & 0x4) != 0)
      str = str + "LEFT,";
    if ((i & 0x10) != 0)
      str = str + "LENGTH,";
    if ((i & 0x20) != 0)
      str = str + "LOCATE,";
    if ((i & 0x10000) != 0)
      str = str + "LOCATE_2,";
    if ((i & 0x8) != 0)
      str = str + "LTRIM,";
    if ((i & 0x80) != 0)
      str = str + "REPEAT,";
    if ((i & 0x100) != 0)
      str = str + "REPLACE,";
    if ((i & 0x200) != 0)
      str = str + "RIGHT,";
    if ((i & 0x400) != 0)
      str = str + "RTRIM,";
    if ((i & 0x20000) != 0)
      str = str + "SOUNDEX,";
    if ((i & 0x40000) != 0)
      str = str + "SPACE,";
    if ((i & 0x800) != 0)
      str = str + "SUBSTRING,";
    if ((i & 0x1000) != 0)
      str = str + "UCASE,";
    if (str.length() > 0)
      str = str.substring(0, str.length() - 1);
    return str;
  }

  public String getSystemFunctions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getSystemFunctions");
    String str = "";
    int i = getInfo(51);
    if ((i & 0x2) != 0)
      str = str + "DBNAME,";
    if ((i & 0x4) != 0)
      str = str + "IFNULL,";
    if ((i & 0x1) != 0)
      str = str + "USERNAME,";
    if (str.length() > 0)
      str = str.substring(0, str.length() - 1);
    return str;
  }

  public String getTimeDateFunctions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getTimeDateFunctions");
    String str = "";
    int i = getInfo(52);
    if ((i & 0x2) != 0)
      str = str + "CURDATE,";
    if ((i & 0x200) != 0)
      str = str + "CURTIME,";
    if ((i & 0x8000) != 0)
      str = str + "DAYNAME,";
    if ((i & 0x4) != 0)
      str = str + "DAYOFMONTH,";
    if ((i & 0x8) != 0)
      str = str + "DAYOFWEEK,";
    if ((i & 0x10) != 0)
      str = str + "DAYOFYEAR,";
    if ((i & 0x400) != 0)
      str = str + "HOUR,";
    if ((i & 0x800) != 0)
      str = str + "MINUTE,";
    if ((i & 0x20) != 0)
      str = str + "MONTH,";
    if ((i & 0x10000) != 0)
      str = str + "MONTHNAME,";
    if ((i & 0x1) != 0)
      str = str + "NOW,";
    if ((i & 0x40) != 0)
      str = str + "QUARTER,";
    if ((i & 0x1000) != 0)
      str = str + "SECOND,";
    if ((i & 0x2000) != 0)
      str = str + "TIMESTAMPADD,";
    if ((i & 0x4000) != 0)
      str = str + "TIMESTAMPDIFF,";
    if ((i & 0x80) != 0)
      str = str + "WEEK,";
    if ((i & 0x100) != 0)
      str = str + "YEAR,";
    if (str.length() > 0)
      str = str.substring(0, str.length() - 1);
    return str;
  }

  public String getSearchStringEscape()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getSearchStringEscape");
    return getInfoString(14);
  }

  public String getExtraNameCharacters()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getExtraNameCharacters");
    return getInfoString(94);
  }

  public boolean supportsAlterTableWithAddColumn()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsAlterTableWithAddColumn");
    int i = getInfo(86);
    return ((i & 0x1) > 0);
  }

  public boolean supportsAlterTableWithDropColumn()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsAlterTableWithDropColumn");
    int i = getInfo(86);
    return ((i & 0x2) > 0);
  }

  public boolean supportsColumnAliasing()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsColumnAliasing");
    return getInfoBooleanString(87);
  }

  public boolean nullPlusNonNullIsNull()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.nullPlusNullIsNull");
    int i = getInfoShort(22);
    return (i == 0);
  }

  public boolean supportsConvert()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsConvert");
    int i = getInfo(48);
    return (i == 1);
  }

  public boolean supportsConvert(int paramInt1, int paramInt2)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsConvert (" + paramInt1 + "," + paramInt2 + ")");
    short s = 0;
    int i = 0;
    int j = 0;
    switch (paramInt1)
    {
    case -7:
      s = 55;
      break;
    case -6:
      s = 68;
      break;
    case 5:
      s = 65;
      break;
    case 4:
      s = 61;
      break;
    case -5:
      s = 53;
      break;
    case 6:
      s = 60;
      break;
    case 7:
      s = 64;
      break;
    case 8:
      s = 59;
      break;
    case 2:
      s = 63;
      break;
    case 3:
      s = 58;
      break;
    case 1:
      s = 56;
      break;
    case 12:
      s = 70;
      break;
    case -1:
      s = 62;
      break;
    case 91:
      s = 57;
      break;
    case 92:
      s = 66;
      break;
    case 93:
      s = 67;
      break;
    case -2:
      s = 54;
      break;
    case -3:
      s = 69;
      break;
    case -4:
      s = 71;
    }
    int k = getInfo(s);
    switch (paramInt2)
    {
    case -7:
      j = 4096;
      break;
    case -6:
      j = 8192;
      break;
    case 5:
      j = 16;
      break;
    case 4:
      j = 8;
      break;
    case -5:
      j = 16384;
      break;
    case 6:
      j = 32;
      break;
    case 7:
      j = 64;
      break;
    case 8:
      j = 128;
      break;
    case 2:
      j = 2;
      break;
    case 3:
      j = 4;
      break;
    case 1:
      j = 1;
      break;
    case 12:
      j = 256;
      break;
    case -1:
      j = 512;
      break;
    case 91:
      j = 32768;
      break;
    case 92:
      j = 65536;
      break;
    case 93:
      j = 131072;
      break;
    case -2:
      j = 1024;
      break;
    case -3:
      j = 2048;
      break;
    case -4:
      j = 262144;
    }
    return ((k & j) > 0);
  }

  public boolean supportsTableCorrelationNames()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsTableCorrelationNames");
    int i = getInfoShort(74);
    return ((i == 1) || (i == 2));
  }

  public boolean supportsDifferentTableCorrelationNames()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsDifferentTableCorrelationNames");
    int i = getInfoShort(74);
    return (i == 1);
  }

  public boolean supportsExpressionsInOrderBy()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsExpressionsInOrderBy");
    return getInfoBooleanString(27);
  }

  public boolean supportsOrderByUnrelated()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsOrderByUnrelated");
    return getInfoBooleanString(90);
  }

  public boolean supportsGroupBy()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsGroupBy");
    int i = getInfoShort(88);
    return (i != 0);
  }

  public boolean supportsGroupByUnrelated()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsGroupByUnrelated");
    int i = getInfoShort(88);
    return (i == 3);
  }

  public boolean supportsGroupByBeyondSelect()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsGroupByBeyondSelect");
    int i = getInfoShort(88);
    return (i == 2);
  }

  public boolean supportsLikeEscapeClause()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsLikeEscapeClause");
    return getInfoBooleanString(113);
  }

  public boolean supportsMultipleResultSets()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsMultipleResultSets");
    return getInfoBooleanString(36);
  }

  public boolean supportsMultipleTransactions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsMultipleTransactions");
    return getInfoBooleanString(37);
  }

  public boolean supportsNonNullableColumns()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsNonNullableColumns");
    int i = getInfoShort(75);
    return (i == 1);
  }

  public boolean supportsMinimumSQLGrammar()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsMinimumSQLGrammar");
    int i = getInfoShort(15);
    return ((i == 0) || (i == 1) || (i == 2));
  }

  public boolean supportsCoreSQLGrammar()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsCoreSQLGrammar");
    int i = getInfoShort(15);
    return ((i == 1) || (i == 2));
  }

  public boolean supportsANSI92EntryLevelSQL()
    throws SQLException
  {
    return true;
  }

  public boolean supportsANSI92IntermediateSQL()
    throws SQLException
  {
    return false;
  }

  public boolean supportsANSI92FullSQL()
    throws SQLException
  {
    return false;
  }

  public boolean supportsExtendedSQLGrammar()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsExtendedSQLGrammar");
    int i = getInfoShort(15);
    return (i == 2);
  }

  public boolean supportsIntegrityEnhancementFacility()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsIntegrityEnhancementFacility");
    return getInfoBooleanString(73);
  }

  public boolean supportsOuterJoins()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsOuterJoins");
    String str = getInfoString(38);
    return (!(str.equalsIgnoreCase("N")));
  }

  public boolean supportsFullOuterJoins()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsFullOuterJoins");
    String str = getInfoString(38);
    return str.equalsIgnoreCase("F");
  }

  public boolean supportsLimitedOuterJoins()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsLimitedOuterJoins");
    String str = getInfoString(38);
    return str.equalsIgnoreCase("P");
  }

  public String getSchemaTerm()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getSchemaTerm");
    return getInfoString(39);
  }

  public String getProcedureTerm()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getProcedureTerm");
    return getInfoString(40);
  }

  public String getCatalogTerm()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getCatalogTerm");
    return getInfoString(42);
  }

  public boolean isCatalogAtStart()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.isCatalogAtStart");
    int i = getInfoShort(114);
    return (i == 1);
  }

  public String getCatalogSeparator()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getCatalogSeparator");
    return getInfoString(41);
  }

  public boolean supportsSchemasInDataManipulation()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsSchemasInDataManipulation");
    int i = getInfo(91);
    return ((i & 0x1) > 0);
  }

  public boolean supportsSchemasInProcedureCalls()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsSchemasInProcedureCalls");
    int i = getInfo(91);
    return ((i & 0x2) > 0);
  }

  public boolean supportsSchemasInTableDefinitions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsSchemasInTableDefinitions");
    int i = getInfo(91);
    return ((i & 0x4) > 0);
  }

  public boolean supportsSchemasInIndexDefinitions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsSchemasInIndexDefintions");
    int i = getInfo(91);
    return ((i & 0x8) > 0);
  }

  public boolean supportsSchemasInPrivilegeDefinitions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsSchemasInPrivilegeDefintions");
    int i = getInfo(91);
    return ((i & 0x10) > 0);
  }

  public boolean supportsCatalogsInDataManipulation()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsCatalogsInDataManipulation");
    int i = getInfo(92);
    return ((i & 0x1) > 0);
  }

  public boolean supportsCatalogsInProcedureCalls()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsCatalogsInProcedureCalls");
    int i = getInfo(92);
    return ((i & 0x2) > 0);
  }

  public boolean supportsCatalogsInTableDefinitions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsCatalogsInTableDefinitions");
    int i = getInfo(92);
    return ((i & 0x4) > 0);
  }

  public boolean supportsCatalogsInIndexDefinitions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsCatalogsInIndexDefinitions");
    int i = getInfo(92);
    return ((i & 0x8) > 0);
  }

  public boolean supportsCatalogsInPrivilegeDefinitions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsCatalogsInPrivilegeDefintions");
    int i = getInfo(92);
    return ((i & 0x10) > 0);
  }

  public boolean supportsPositionedDelete()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsPositionedDelete");
    int i = getInfo(80);
    return ((i & 0x1) > 0);
  }

  public boolean supportsPositionedUpdate()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsPositionedUpdate");
    int i = getInfo(80);
    return ((i & 0x2) > 0);
  }

  public boolean supportsSelectForUpdate()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsSelectForUpdate");
    int i = getInfo(80);
    return ((i & 0x4) > 0);
  }

  public boolean supportsStoredProcedures()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsStoredProcedures");
    return getInfoBooleanString(21);
  }

  public boolean supportsSubqueriesInComparisons()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsSubqueriesInComparisions");
    int i = getInfo(95);
    return ((i & 0x1) > 0);
  }

  public boolean supportsSubqueriesInExists()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsSubqueriesInExists");
    int i = getInfo(95);
    return ((i & 0x2) > 0);
  }

  public boolean supportsSubqueriesInIns()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsSubqueriesInIns");
    int i = getInfo(95);
    return ((i & 0x4) > 0);
  }

  public boolean supportsSubqueriesInQuantifieds()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsSubqueriesInQuantifieds");
    int i = getInfo(95);
    return ((i & 0x8) > 0);
  }

  public boolean supportsCorrelatedSubqueries()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsCorrelatedSubqueries");
    int i = getInfo(95);
    return ((i & 0x10) > 0);
  }

  public boolean supportsUnion()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsUnion");
    int i = getInfo(96);
    return ((i & 0x1) > 0);
  }

  public boolean supportsUnionAll()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsUnionAll");
    int i = getInfo(96);
    return ((i & 0x2) > 0);
  }

  public boolean supportsOpenCursorsAcrossCommit()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsOpenCursorsAcrossCommit");
    int i = getInfoShort(23);
    return (i == 2);
  }

  public boolean supportsOpenCursorsAcrossRollback()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsOpenCursorsAcrossRollback");
    int i = getInfoShort(24);
    return (i == 2);
  }

  public boolean supportsOpenStatementsAcrossCommit()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsOpenStatementsAcrossCommit");
    int i = getInfoShort(23);
    return ((i == 2) || (i == 1));
  }

  public boolean supportsOpenStatementsAcrossRollback()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsOpenStatementsAcrossRollback");
    int i = getInfoShort(24);
    return ((i == 2) || (i == 1));
  }

  public int getMaxBinaryLiteralLength()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxBinaryLiteralLength");
    return getInfo(112);
  }

  public int getMaxCharLiteralLength()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxCharLiteralLength");
    return getInfo(108);
  }

  public int getMaxColumnNameLength()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxColumnNameLength");
    return getInfoShort(30);
  }

  public int getMaxColumnsInGroupBy()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxColumnsInGroupBy");
    return getInfoShort(97);
  }

  public int getMaxColumnsInIndex()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxColumnsInIndex");
    return getInfoShort(98);
  }

  public int getMaxColumnsInOrderBy()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxColumnsInOrderBy");
    return getInfoShort(99);
  }

  public int getMaxColumnsInSelect()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxColumnsInSeleted");
    return getInfoShort(100);
  }

  public int getMaxColumnsInTable()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxColumnsInTable");
    return getInfoShort(101);
  }

  public int getMaxConnections()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxConnections");
    return getInfoShort(0);
  }

  public int getMaxCursorNameLength()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxCursorNameLength");
    return getInfo(31);
  }

  public int getMaxIndexLength()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxIndexLength");
    return getInfo(102);
  }

  public int getMaxSchemaNameLength()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxSchemaNameLength");
    return getInfoShort(32);
  }

  public int getMaxProcedureNameLength()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxProcedureNameLength");
    return getInfoShort(33);
  }

  public int getMaxCatalogNameLength()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxCatalogNameLength");
    return getInfoShort(34);
  }

  public int getMaxRowSize()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxRowSize");
    return getInfo(104);
  }

  public boolean doesMaxRowSizeIncludeBlobs()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.doesMaxRowSizeIncludeBlobs");
    return getInfoBooleanString(103);
  }

  public int getMaxStatementLength()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxStatementLength");
    return getInfo(105);
  }

  public int getMaxStatements()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxStatements");
    return getInfoShort(1);
  }

  public int getMaxTableNameLength()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxTableNameLength");
    return getInfoShort(35);
  }

  public int getMaxTablesInSelect()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxTablesInSelect");
    return getInfoShort(106);
  }

  public int getMaxUserNameLength()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getMaxUserNameLength");
    return getInfoShort(107);
  }

  public int getDefaultTransactionIsolation()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getDefaultTransactionIsolation");
    int i = getInfo(26);
    int j = 0;
    switch (i)
    {
    case 1:
      j = 1;
      break;
    case 2:
      j = 2;
      break;
    case 4:
      j = 4;
      break;
    case 8:
      j = 8;
    case 3:
    case 5:
    case 6:
    case 7:
    }
    return j;
  }

  public boolean supportsTransactions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsTransactions");
    int i = getInfoShort(46);
    return (i != 0);
  }

  public boolean supportsTransactionIsolationLevel(int paramInt)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsTransactionIsolationLevel (" + paramInt + ")");
    if (paramInt == 0)
      return (!(supportsTransactions()));
    int i = getInfo(72);
    int j = 0;
    switch (paramInt)
    {
    case 1:
      j = ((i & 0x1) > 0) ? 1 : 0;
      break;
    case 2:
      j = ((i & 0x2) > 0) ? 1 : 0;
      break;
    case 4:
      j = ((i & 0x4) > 0) ? 1 : 0;
      break;
    case 8:
      j = ((i & 0x8) > 0) ? 1 : 0;
    case 3:
    case 5:
    case 6:
    case 7:
    }
    return j;
  }

  public boolean supportsDataDefinitionAndDataManipulationTransactions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsDataDefinitionAndDataManipulationTransactions");
    int i = getInfoShort(46);
    return ((i & 0x2) > 0);
  }

  public boolean supportsDataManipulationTransactionsOnly()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsDataManipulationTransactionsOnly");
    int i = getInfoShort(46);
    return ((i & 0x1) > 0);
  }

  public boolean dataDefinitionCausesTransactionCommit()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.dataDefintionCausesTransactionCommit");
    int i = getInfoShort(46);
    return ((i & 0x3) > 0);
  }

  public boolean dataDefinitionIgnoredInTransactions()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.dataDefintionIgnoredInTransactions");
    int i = getInfoShort(46);
    return ((i & 0x4) > 0);
  }

  public ResultSet getProcedures(String paramString1, String paramString2, String paramString3)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getProcedures (" + paramString1 + "," + paramString2 + "," + paramString3 + ")");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLProcedures(l, paramString1, paramString2, paramString3);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    return localJdbcOdbcResultSet;
  }

  public ResultSet getProcedureColumns(String paramString1, String paramString2, String paramString3, String paramString4)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getProcedureColumns (" + paramString1 + "," + paramString2 + "," + paramString3 + "," + paramString4 + ")");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLProcedureColumns(l, paramString1, paramString2, paramString3, paramString4);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    if (this.Con.getODBCVer() >= 2)
    {
      localJdbcOdbcResultSet.setSQLTypeColumn(6);
      localJdbcOdbcResultSet.setAliasColumnName("PRECISION", 8);
      localJdbcOdbcResultSet.setAliasColumnName("LENGTH", 9);
      localJdbcOdbcResultSet.setAliasColumnName("SCALE", 10);
      localJdbcOdbcResultSet.setAliasColumnName("RADIX", 11);
    }
    return localJdbcOdbcResultSet;
  }

  public ResultSet getTables(String paramString1, String paramString2, String paramString3, String[] paramArrayOfString)
    throws SQLException
  {
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    String str1 = null;
    Object localObject = null;
    if (paramArrayOfString != null)
    {
      str1 = "";
      int i = 0;
      for (i = 0; i < paramArrayOfString.length; i = (short)(i + 1))
      {
        String str2 = paramArrayOfString[i];
        if (i > 0)
          str1 = str1 + ",";
        str1 = str1 + str2;
      }
    }
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getTables (" + paramString1 + "," + paramString2 + "," + paramString3 + "," + str1 + ")");
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLTables(l, paramString1, paramString2, paramString3, str1);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    return localJdbcOdbcResultSet;
  }

  public ResultSet getSchemas()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getSchemas");
    JdbcOdbcResultSet localJdbcOdbcResultSet = (JdbcOdbcResultSet)getTables("", "%", "", null);
    int[] arrayOfInt = new int[1];
    arrayOfInt[0] = 2;
    localJdbcOdbcResultSet.setColumnMappings(arrayOfInt);
    return localJdbcOdbcResultSet;
  }

  public ResultSet getCatalogs()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getCatalogs");
    JdbcOdbcResultSet localJdbcOdbcResultSet = (JdbcOdbcResultSet)getTables("%", "", "", null);
    int[] arrayOfInt = new int[1];
    arrayOfInt[0] = 1;
    localJdbcOdbcResultSet.setColumnMappings(arrayOfInt);
    return localJdbcOdbcResultSet;
  }

  public ResultSet getTableTypes()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getTableTypes");
    String[] arrayOfString = new String[1];
    arrayOfString[0] = "%";
    JdbcOdbcResultSet localJdbcOdbcResultSet = (JdbcOdbcResultSet)getTables(null, null, "%", null);
    int[] arrayOfInt = new int[1];
    arrayOfInt[0] = 4;
    localJdbcOdbcResultSet.setColumnMappings(arrayOfInt);
    return localJdbcOdbcResultSet;
  }

  public ResultSet getColumns(String paramString1, String paramString2, String paramString3, String paramString4)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getColumns (" + paramString1 + "," + paramString2 + "," + paramString3 + "," + paramString4 + ")");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLColumns(l, paramString1, paramString2, paramString3, paramString4);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    if (this.Con.getODBCVer() == 2)
    {
      JdbcOdbcPseudoCol[] arrayOfJdbcOdbcPseudoCol = new JdbcOdbcPseudoCol[6];
      arrayOfJdbcOdbcPseudoCol[0] = new JdbcOdbcPseudoCol("COLUMN_DEF", 12, 254);
      arrayOfJdbcOdbcPseudoCol[1] = new JdbcOdbcPseudoCol("SQL_DATA_TYPE", 5, 0);
      arrayOfJdbcOdbcPseudoCol[2] = new JdbcOdbcPseudoCol("SQL_DATETIME_SUB", 5, 0);
      arrayOfJdbcOdbcPseudoCol[3] = new JdbcOdbcPseudoCol("CHAR_OCTET_LENGTH", 4, 0);
      arrayOfJdbcOdbcPseudoCol[4] = new JdbcOdbcPseudoCol("ORDINAL_POSITION", 4, 0);
      arrayOfJdbcOdbcPseudoCol[5] = new JdbcOdbcPseudoCol("IS_NULLABLE", 12, 254);
      localJdbcOdbcResultSet.setPseudoCols(13, 18, arrayOfJdbcOdbcPseudoCol);
      localJdbcOdbcResultSet.setSQLTypeColumn(5);
    }
    else if (this.Con.getODBCVer() >= 3)
    {
      localJdbcOdbcResultSet.setSQLTypeColumn(5);
      localJdbcOdbcResultSet.setAliasColumnName("SQL_DATETIME_SUB", 15);
    }
    return localJdbcOdbcResultSet;
  }

  public ResultSet getColumnPrivileges(String paramString1, String paramString2, String paramString3, String paramString4)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getColumnPrivileges (" + paramString1 + "," + paramString2 + "," + paramString3 + "," + paramString4 + ")");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLColumnPrivileges(l, paramString1, paramString2, paramString3, paramString4);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    return localJdbcOdbcResultSet;
  }

  public ResultSet getTablePrivileges(String paramString1, String paramString2, String paramString3)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getTablePrivileges (" + paramString1 + "," + paramString2 + "," + paramString3 + ")");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLTablePrivileges(l, paramString1, paramString2, paramString3);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    return localJdbcOdbcResultSet;
  }

  public ResultSet getBestRowIdentifier(String paramString1, String paramString2, String paramString3, int paramInt, boolean paramBoolean)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getBestRowIdentifier (" + paramString1 + "," + paramString2 + "," + paramString3 + "," + paramInt + "," + paramBoolean + ")");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLSpecialColumns(l, 1, paramString1, paramString2, paramString3, paramInt, paramBoolean);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    if (this.Con.getODBCVer() >= 2)
      localJdbcOdbcResultSet.setSQLTypeColumn(3);
    return localJdbcOdbcResultSet;
  }

  public ResultSet getVersionColumns(String paramString1, String paramString2, String paramString3)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getVersionColumns (" + paramString1 + "," + paramString2 + "," + paramString3 + ")");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLSpecialColumns(l, 2, paramString1, paramString2, paramString3, 0, false);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    if (this.Con.getODBCVer() >= 2)
      localJdbcOdbcResultSet.setSQLTypeColumn(3);
    return localJdbcOdbcResultSet;
  }

  public ResultSet getPrimaryKeys(String paramString1, String paramString2, String paramString3)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getPrimaryKeys (" + paramString1 + "," + paramString2 + "," + paramString3 + ")");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLPrimaryKeys(l, paramString1, paramString2, paramString3);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    return localJdbcOdbcResultSet;
  }

  public ResultSet getImportedKeys(String paramString1, String paramString2, String paramString3)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getImportedKeys (" + paramString1 + "," + paramString2 + "," + paramString3 + ")");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLForeignKeys(l, null, null, null, paramString1, paramString2, paramString3);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    if (this.Con.getODBCVer() >= 2)
    {
      JdbcOdbcPseudoCol[] arrayOfJdbcOdbcPseudoCol = new JdbcOdbcPseudoCol[1];
      arrayOfJdbcOdbcPseudoCol[0] = new JdbcOdbcPseudoCol("DEFERRABILITY", 5, 0);
      localJdbcOdbcResultSet.setPseudoCols(14, 14, arrayOfJdbcOdbcPseudoCol);
    }
    return localJdbcOdbcResultSet;
  }

  public ResultSet getExportedKeys(String paramString1, String paramString2, String paramString3)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getExportedKeys (" + paramString1 + "," + paramString2 + "," + paramString3 + ")");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLForeignKeys(l, paramString1, paramString2, paramString3, null, null, null);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    if (this.Con.getODBCVer() >= 2)
    {
      JdbcOdbcPseudoCol[] arrayOfJdbcOdbcPseudoCol = new JdbcOdbcPseudoCol[1];
      arrayOfJdbcOdbcPseudoCol[0] = new JdbcOdbcPseudoCol("DEFERRABILITY", 5, 0);
      localJdbcOdbcResultSet.setPseudoCols(14, 14, arrayOfJdbcOdbcPseudoCol);
    }
    return localJdbcOdbcResultSet;
  }

  public ResultSet getCrossReference(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getCrossReference (" + paramString1 + "," + paramString2 + "," + paramString3 + "," + paramString4 + "," + paramString5 + "," + paramString6 + ")");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLForeignKeys(l, paramString1, paramString2, paramString3, paramString4, paramString5, paramString6);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    if (this.Con.getODBCVer() >= 2)
    {
      JdbcOdbcPseudoCol[] arrayOfJdbcOdbcPseudoCol = new JdbcOdbcPseudoCol[1];
      arrayOfJdbcOdbcPseudoCol[0] = new JdbcOdbcPseudoCol("DEFERRABILITY", 5, 0);
      localJdbcOdbcResultSet.setPseudoCols(14, 14, arrayOfJdbcOdbcPseudoCol);
    }
    return localJdbcOdbcResultSet;
  }

  public boolean supportsResultSetType(int paramInt)
    throws SQLException
  {
    short s = getConnectionSupportType(paramInt);
    switch (paramInt)
    {
    case 1003:
      return (s == 0);
    case 1004:
      return ((s == 3) || (s == 1));
    case 1005:
      if (s == 1)
      {
        int i = this.Con.getOdbcCursorAttr2(s);
        return ((i & 0x40) != 0);
      }
      return (s == 2);
    }
    return false;
  }

  public boolean supportsResultSetConcurrency(int paramInt1, int paramInt2)
    throws SQLException
  {
    if (supportsResultSetType(paramInt1))
    {
      int i = this.Con.getOdbcConcurrency(paramInt2);
      switch (paramInt2)
      {
      case 1007:
        return (i == 1);
      case 1008:
        if (paramInt1 != 1003)
          return (i == 2);
        return false;
      }
      return false;
    }
    return false;
  }

  public boolean ownUpdatesAreVisible(int paramInt)
    throws SQLException
  {
    if (paramInt != 1003)
      return updatesAreDetected(paramInt);
    return false;
  }

  public boolean ownDeletesAreVisible(int paramInt)
    throws SQLException
  {
    if (paramInt != 1003)
      return deletesAreDetected(paramInt);
    return false;
  }

  public boolean ownInsertsAreVisible(int paramInt)
    throws SQLException
  {
    if (paramInt != 1003)
      return insertsAreDetected(paramInt);
    return false;
  }

  public boolean othersUpdatesAreVisible(int paramInt)
    throws SQLException
  {
    if (paramInt == 1005)
      return updatesAreDetected(paramInt);
    return false;
  }

  public boolean othersDeletesAreVisible(int paramInt)
    throws SQLException
  {
    if (paramInt == 1005)
      return deletesAreDetected(paramInt);
    return false;
  }

  public boolean othersInsertsAreVisible(int paramInt)
    throws SQLException
  {
    if (paramInt == 1005)
      return insertsAreDetected(paramInt);
    return false;
  }

  public boolean updatesAreDetected(int paramInt)
    throws SQLException
  {
    short s = getCursorAttribute(paramInt);
    if (s > 0)
      try
      {
        int i = this.OdbcApi.SQLGetInfo(this.hDbc, s);
        return ((i & 0x40) > 0);
      }
      catch (SQLException localSQLException)
      {
        return false;
      }
    return false;
  }

  public boolean deletesAreDetected(int paramInt)
    throws SQLException
  {
    short s = getCursorAttribute(paramInt);
    if (s > 0)
      try
      {
        int i = this.OdbcApi.SQLGetInfo(this.hDbc, s);
        return ((i & 0x20) > 0);
      }
      catch (SQLException localSQLException)
      {
        return false;
      }
    return false;
  }

  public boolean insertsAreDetected(int paramInt)
    throws SQLException
  {
    short s = getCursorAttribute(paramInt);
    if (s > 0)
      try
      {
        int i = this.OdbcApi.SQLGetInfo(this.hDbc, s);
        return ((i & 0x10) > 0);
      }
      catch (SQLException localSQLException)
      {
        return false;
      }
    return false;
  }

  public boolean supportsBatchUpdates()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.supportsBatchUpdates");
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;
    try
    {
      i = this.OdbcApi.SQLGetInfo(this.hDbc, 121);
      if ((i & 0x2) > 0)
        j = 1;
      i = this.OdbcApi.SQLGetInfo(this.hDbc, 153);
      if ((i & 0x1) > 0)
        l = 1;
    }
    catch (SQLException localSQLException)
    {
      j = 0;
      l = 0;
    }
    return ((j == 1) && (l == 1));
  }

  public ResultSet getUDTs(String paramString1, String paramString2, String paramString3, int[] paramArrayOfInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public Connection getConnection()
    throws SQLException
  {
    if ((this.Con != null) && (this.hDbc > 3412046827397054464L))
      return this.Con;
    return null;
  }

  public ResultSet getTypeInfo()
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getTypeInfo");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLGetTypeInfo(l, 0);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    if (this.Con.getODBCVer() == 2)
    {
      JdbcOdbcPseudoCol[] arrayOfJdbcOdbcPseudoCol = new JdbcOdbcPseudoCol[5];
      arrayOfJdbcOdbcPseudoCol[0] = new JdbcOdbcPseudoCol("SQL_DATA_TYPE", 5, 0);
      arrayOfJdbcOdbcPseudoCol[1] = new JdbcOdbcPseudoCol("SQL_DATETIME_SUB", 5, 0);
      arrayOfJdbcOdbcPseudoCol[2] = new JdbcOdbcPseudoCol("NUM_PREC_RADIX", 5, 0);
      localJdbcOdbcResultSet.setPseudoCols(16, 18, arrayOfJdbcOdbcPseudoCol);
      localJdbcOdbcResultSet.setSQLTypeColumn(2);
    }
    else if (this.Con.getODBCVer() >= 3)
    {
      localJdbcOdbcResultSet.setSQLTypeColumn(2);
    }
    if (this.Con.getODBCVer() >= 2)
    {
      localJdbcOdbcResultSet.setAliasColumnName("PRECISION", 3);
      localJdbcOdbcResultSet.setAliasColumnName("AUTO_INCREMENT", 12);
    }
    return localJdbcOdbcResultSet;
  }

  public ResultSet getIndexInfo(String paramString1, String paramString2, String paramString3, boolean paramBoolean1, boolean paramBoolean2)
    throws SQLException
  {
    if (this.OdbcApi.getTracer().isTracing())
      this.OdbcApi.getTracer().trace("*DatabaseMetaData.getIndexInfo (" + paramString1 + "," + paramString2 + "," + paramString3 + paramBoolean1 + "," + paramBoolean2 + ")");
    JdbcOdbcResultSet localJdbcOdbcResultSet = null;
    Object localObject = null;
    long l = this.OdbcApi.SQLAllocStmt(this.hDbc);
    try
    {
      this.OdbcApi.SQLStatistics(l, paramString1, paramString2, paramString3, paramBoolean1, paramBoolean2);
    }
    catch (SQLWarning localSQLWarning)
    {
      localObject = localSQLWarning;
    }
    catch (SQLException localSQLException)
    {
      this.OdbcApi.SQLFreeStmt(l, 1);
      throw localSQLException;
    }
    localJdbcOdbcResultSet = new JdbcOdbcResultSet();
    localJdbcOdbcResultSet.initialize(this.OdbcApi, this.hDbc, l, false, null);
    localJdbcOdbcResultSet.setWarning(localObject);
    return localJdbcOdbcResultSet;
  }

  protected void validateConnection()
    throws SQLException
  {
    this.Con.validateConnection();
  }

  protected int getInfo(short paramShort)
    throws SQLException
  {
    validateConnection();
    return this.OdbcApi.SQLGetInfo(this.hDbc, paramShort);
  }

  protected int getInfoShort(short paramShort)
    throws SQLException
  {
    validateConnection();
    return this.OdbcApi.SQLGetInfoShort(this.hDbc, paramShort);
  }

  protected boolean getInfoBooleanString(short paramShort)
    throws SQLException
  {
    validateConnection();
    String str = this.OdbcApi.SQLGetInfoString(this.hDbc, paramShort);
    return str.equalsIgnoreCase("Y");
  }

  protected String getInfoString(short paramShort)
    throws SQLException
  {
    validateConnection();
    return this.OdbcApi.SQLGetInfoString(this.hDbc, paramShort);
  }

  protected String getInfoString(short paramShort, int paramInt)
    throws SQLException
  {
    validateConnection();
    return this.OdbcApi.SQLGetInfoString(this.hDbc, paramShort, paramInt);
  }

  protected short getConnectionSupportType(int paramInt)
    throws SQLException
  {
    int i = this.Con.getOdbcCursorType(paramInt);
    if (i == -1)
      i = this.Con.getBestOdbcCursorType();
    return i;
  }

  protected short getCursorAttribute(int paramInt)
    throws SQLException
  {
    int i = 0;
    if (supportsResultSetType(paramInt))
    {
      int j = getConnectionSupportType(paramInt);
      switch (j)
      {
      case 1:
        i = 151;
        break;
      case 2:
        i = 145;
        break;
      case 3:
        i = 168;
      }
      return i;
    }
    return i;
  }

  public boolean supportsSavepoints()
    throws SQLException
  {
    return false;
  }

  public boolean supportsNamedParameters()
    throws SQLException
  {
    return false;
  }

  public boolean supportsMultipleOpenResults()
    throws SQLException
  {
    return false;
  }

  public boolean supportsGetGeneratedKeys()
    throws SQLException
  {
    return false;
  }

  public ResultSet getSuperTypes(String paramString1, String paramString2, String paramString3)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public ResultSet getSuperTables(String paramString1, String paramString2, String paramString3)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public ResultSet getAttributes(String paramString1, String paramString2, String paramString3, String paramString4)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean supportsResultSetHoldability(int paramInt)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public int getResultSetHoldability()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public int getDatabaseMajorVersion()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public int getDatabaseMinorVersion()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public int getSQLStateType()
    throws SQLException
  {
    return 1;
  }

  public int getJDBCMajorVersion()
    throws SQLException
  {
    return 2;
  }

  public int getJDBCMinorVersion()
    throws SQLException
  {
    return 0;
  }

  public boolean locatorsUpdateCopy()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean supportsStatementPooling()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public RowIdLifetime getRowIdLifetime()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public ResultSet getSchemas(String paramString1, String paramString2)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean supportsStoredFunctionsUsingCallSyntax()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public boolean autoCommitFailureClosesAllResultSets()
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public ResultSet getClientInfoProperties()
    throws SQLException
  {
    throw new UnsupportedOperationException();
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

  public ResultSet getFunctions(String paramString1, String paramString2, String paramString3)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public ResultSet getFunctionParameters(String paramString1, String paramString2, String paramString3, String paramString4)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }

  public ResultSet getFunctionColumns(String paramString1, String paramString2, String paramString3, String paramString4)
    throws SQLException
  {
    throw new UnsupportedOperationException("Operation not yet supported");
  }
}