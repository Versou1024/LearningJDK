package sun.jdbc.odbc;

public class JdbcOdbcPseudoCol extends JdbcOdbcObject
{
  protected String colLabel;
  protected int colType;
  protected int colLength;
  protected int colDisplaySize;

  public JdbcOdbcPseudoCol(String paramString, int paramInt1, int paramInt2)
  {
    this.colLabel = paramString;
    this.colType = paramInt1;
    this.colLength = paramInt2;
    this.colDisplaySize = this.colLength;
    switch (this.colType)
    {
    case -4:
    case -3:
    case -2:
      this.colDisplaySize *= 2;
      break;
    case 91:
      this.colDisplaySize = 10;
      break;
    case 92:
      this.colDisplaySize = 8;
      break;
    case 93:
      this.colDisplaySize = 29;
      break;
    case 2:
    case 3:
      this.colDisplaySize += 2;
      break;
    case -7:
      this.colDisplaySize = 1;
      break;
    case -6:
      this.colDisplaySize = 4;
      break;
    case 5:
      this.colDisplaySize = 6;
      break;
    case 4:
      this.colDisplaySize = 11;
      break;
    case -5:
      this.colDisplaySize = 20;
      break;
    case 7:
      this.colDisplaySize = 13;
      break;
    case 6:
    case 8:
      this.colDisplaySize = 22;
    }
  }

  public String getColumnLabel()
  {
    return this.colLabel;
  }

  public int getColumnType()
  {
    return this.colType;
  }

  public int getColumnLength()
  {
    return this.colLength;
  }

  public int getColumnDisplaySize()
  {
    return this.colDisplaySize;
  }
}