package sun.text.normalizer;

import java.text.ParsePosition;

public class RuleCharacterIterator
{
  private String text;
  private ParsePosition pos;
  private SymbolTable sym;
  private char[] buf;
  private int bufPos;
  private boolean isEscaped;
  public static final int DONE = -1;
  public static final int PARSE_VARIABLES = 1;
  public static final int PARSE_ESCAPES = 2;
  public static final int SKIP_WHITESPACE = 4;

  public RuleCharacterIterator(String paramString, SymbolTable paramSymbolTable, ParsePosition paramParsePosition)
  {
    if ((paramString == null) || (paramParsePosition.getIndex() > paramString.length()))
      throw new IllegalArgumentException();
    this.text = paramString;
    this.sym = paramSymbolTable;
    this.pos = paramParsePosition;
    this.buf = null;
  }

  public boolean atEnd()
  {
    return ((this.buf == null) && (this.pos.getIndex() == this.text.length()));
  }

  public int next(int paramInt)
  {
    Object localObject;
    int i = -1;
    this.isEscaped = false;
    while (true)
    {
      while (true)
      {
        do
        {
          i = _current();
          _advance(UTF16.getCharCount(i));
          if ((i != 36) || (this.buf != null) || ((paramInt & 0x1) == 0) || (this.sym == null))
            break label147;
          localObject = this.sym.parseReference(this.text, this.pos, this.text.length());
          if (localObject == null)
            break label221:
          this.bufPos = 0;
          this.buf = this.sym.lookup((String)localObject);
          if (this.buf == null)
            throw new IllegalArgumentException("Undefined variable: " + ((String)localObject));
        }
        while (this.buf.length != 0);
        this.buf = null;
      }
      label147: if (((paramInt & 0x4) == 0) || (!(UCharacterProperty.isRuleWhiteSpace(i))))
        break;
    }
    if ((i == 92) && ((paramInt & 0x2) != 0))
    {
      localObject = { 0 };
      i = Utility.unescapeAt(lookahead(), localObject);
      jumpahead(localObject[0]);
      this.isEscaped = true;
      if (i < 0)
        throw new IllegalArgumentException("Invalid escape");
    }
    label221: return i;
  }

  public boolean isEscaped()
  {
    return this.isEscaped;
  }

  public boolean inVariable()
  {
    return (this.buf != null);
  }

  public Object getPos(Object paramObject)
  {
    if (paramObject == null)
      return { this.buf, { this.pos.getIndex(), this.bufPos } };
    Object[] arrayOfObject = (Object[])(Object[])paramObject;
    arrayOfObject[0] = this.buf;
    int[] arrayOfInt = (int[])(int[])arrayOfObject[1];
    arrayOfInt[0] = this.pos.getIndex();
    arrayOfInt[1] = this.bufPos;
    return paramObject;
  }

  public void setPos(Object paramObject)
  {
    Object[] arrayOfObject = (Object[])(Object[])paramObject;
    this.buf = ((char[])(char[])arrayOfObject[0]);
    int[] arrayOfInt = (int[])(int[])arrayOfObject[1];
    this.pos.setIndex(arrayOfInt[0]);
    this.bufPos = arrayOfInt[1];
  }

  public void skipIgnored(int paramInt)
  {
    if ((paramInt & 0x4) != 0)
      while (true)
      {
        int i = _current();
        if (!(UCharacterProperty.isRuleWhiteSpace(i)))
          return;
        _advance(UTF16.getCharCount(i));
      }
  }

  public String lookahead()
  {
    if (this.buf != null)
      return new String(this.buf, this.bufPos, this.buf.length - this.bufPos);
    return this.text.substring(this.pos.getIndex());
  }

  public void jumpahead(int paramInt)
  {
    if (paramInt < 0)
      throw new IllegalArgumentException();
    if (this.buf != null)
    {
      this.bufPos += paramInt;
      if (this.bufPos > this.buf.length)
        throw new IllegalArgumentException();
      if (this.bufPos == this.buf.length)
        this.buf = null;
    }
    else
    {
      int i = this.pos.getIndex() + paramInt;
      this.pos.setIndex(i);
      if (i > this.text.length())
        throw new IllegalArgumentException();
    }
  }

  private int _current()
  {
    if (this.buf != null)
      return UTF16.charAt(this.buf, 0, this.buf.length, this.bufPos);
    int i = this.pos.getIndex();
    return ((i < this.text.length()) ? UTF16.charAt(this.text, i) : -1);
  }

  private void _advance(int paramInt)
  {
    if (this.buf != null)
    {
      this.bufPos += paramInt;
      if (this.bufPos == this.buf.length)
        this.buf = null;
    }
    else
    {
      this.pos.setIndex(this.pos.getIndex() + paramInt);
      if (this.pos.getIndex() > this.text.length())
        this.pos.setIndex(this.text.length());
    }
  }
}