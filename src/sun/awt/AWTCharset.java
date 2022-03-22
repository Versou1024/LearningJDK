package sun.awt;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public class AWTCharset extends Charset
{
  protected Charset awtCs;
  protected Charset javaCs;

  public AWTCharset(String paramString, Charset paramCharset)
  {
    super(paramString, null);
    this.javaCs = paramCharset;
    this.awtCs = this;
  }

  public boolean contains(Charset paramCharset)
  {
    if (this.javaCs == null)
      return false;
    return this.javaCs.contains(paramCharset);
  }

  public CharsetEncoder newEncoder()
  {
    if (this.javaCs == null)
      throw new Error("Encoder is not supported by this Charset");
    return new Encoder(this, this.javaCs.newEncoder());
  }

  public CharsetDecoder newDecoder()
  {
    if (this.javaCs == null)
      throw new Error("Decoder is not supported by this Charset");
    return new Decoder(this, this.javaCs.newDecoder());
  }

  public class Decoder extends CharsetDecoder
  {
    protected CharsetDecoder dec;
    private String nr;
    ByteBuffer fbb;

    protected Decoder()
    {
      this(paramAWTCharset, paramAWTCharset.javaCs.newDecoder());
    }

    protected Decoder(, CharsetDecoder paramCharsetDecoder)
    {
      super(paramAWTCharset.awtCs, paramCharsetDecoder.averageCharsPerByte(), paramCharsetDecoder.maxCharsPerByte());
      this.fbb = ByteBuffer.allocate(0);
      this.dec = paramCharsetDecoder;
    }

    protected CoderResult decodeLoop(, CharBuffer paramCharBuffer)
    {
      return this.dec.decode(paramByteBuffer, paramCharBuffer, true);
    }

    protected CoderResult implFlush()
    {
      this.dec.decode(this.fbb, paramCharBuffer, true);
      return this.dec.flush(paramCharBuffer);
    }

    protected void implReset()
    {
      this.dec.reset();
    }

    protected void implReplaceWith()
    {
      if (this.dec != null)
        this.dec.replaceWith(paramString);
    }

    protected void implOnMalformedInput()
    {
      this.dec.onMalformedInput(paramCodingErrorAction);
    }

    protected void implOnUnmappableCharacter()
    {
      this.dec.onUnmappableCharacter(paramCodingErrorAction);
    }
  }

  public class Encoder extends CharsetEncoder
  {
    protected CharsetEncoder enc;

    protected Encoder()
    {
      this(paramAWTCharset, paramAWTCharset.javaCs.newEncoder());
    }

    protected Encoder(, CharsetEncoder paramCharsetEncoder)
    {
      super(paramAWTCharset.awtCs, paramCharsetEncoder.averageBytesPerChar(), paramCharsetEncoder.maxBytesPerChar());
      this.enc = paramCharsetEncoder;
    }

    public boolean canEncode()
    {
      return this.enc.canEncode(paramChar);
    }

    public boolean canEncode()
    {
      return this.enc.canEncode(paramCharSequence);
    }

    protected CoderResult encodeLoop(, ByteBuffer paramByteBuffer)
    {
      return this.enc.encode(paramCharBuffer, paramByteBuffer, true);
    }

    protected CoderResult implFlush()
    {
      return this.enc.flush(paramByteBuffer);
    }

    protected void implReset()
    {
      this.enc.reset();
    }

    protected void implReplaceWith()
    {
      if (this.enc != null)
        this.enc.replaceWith(paramArrayOfByte);
    }

    protected void implOnMalformedInput()
    {
      this.enc.onMalformedInput(paramCodingErrorAction);
    }

    protected void implOnUnmappableCharacter()
    {
      this.enc.onUnmappableCharacter(paramCodingErrorAction);
    }

    public boolean isLegalReplacement()
    {
      return true;
    }
  }
}