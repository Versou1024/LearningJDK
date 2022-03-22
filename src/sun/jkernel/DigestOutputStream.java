package sun.jkernel;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

public class DigestOutputStream extends FilterOutputStream
{
  private static final String DEFAULT_ALGORITHM = "SHA-1";
  private final boolean debug = 0;
  private StandaloneMessageDigest smd = null;
  protected volatile OutputStream out;

  private void initDigest(String paramString)
    throws NoSuchAlgorithmException
  {
    this.smd = StandaloneMessageDigest.getInstance(paramString);
  }

  public DigestOutputStream(OutputStream paramOutputStream, String paramString)
    throws NoSuchAlgorithmException
  {
    super(paramOutputStream);
    initDigest(paramString);
    this.out = paramOutputStream;
  }

  public DigestOutputStream(OutputStream paramOutputStream)
  {
    super(paramOutputStream);
    try
    {
      initDigest("SHA-1");
    }
    catch (NoSuchAlgorithmException localNoSuchAlgorithmException)
    {
      throw new RuntimeException("DigestOutputStream() unknown algorithm");
    }
  }

  public void write(int paramInt)
    throws IOException
  {
    super.write(paramInt);
    byte[] arrayOfByte = { (byte)(paramInt & 0xFF) };
    this.smd.update(arrayOfByte, 0, 1);
  }

  public void write(byte[] paramArrayOfByte)
    throws IOException
  {
    write(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    if (paramArrayOfByte == null)
      throw new NullPointerException("null array in DigestOutputStream.write");
    if ((paramInt1 < 0) || (paramInt2 < 0) || (paramInt2 > paramArrayOfByte.length - paramInt1))
      throw new IndexOutOfBoundsException();
    this.out.write(paramArrayOfByte, paramInt1, paramInt2);
    this.smd.update(paramArrayOfByte, paramInt1, paramInt2);
  }

  public void close()
    throws IOException
  {
    super.close();
  }

  public byte[] getCheckValue()
  {
    byte[] arrayOfByte = this.smd.digest();
    this.smd.reset();
    return arrayOfByte;
  }

  public void flush()
    throws IOException
  {
    super.flush();
  }
}