package sun.security.krb5.internal.ccache;

import java.io.ByteArrayOutputStream;

public class Tag
{
  int length;
  int tag;
  int tagLen;
  Integer time_offset;
  Integer usec_offset;

  public Tag(int paramInt1, int paramInt2, Integer paramInteger1, Integer paramInteger2)
  {
    this.tag = paramInt2;
    this.tagLen = 8;
    this.time_offset = paramInteger1;
    this.usec_offset = paramInteger2;
    this.length = (4 + this.tagLen);
  }

  public Tag(int paramInt)
  {
    this.tag = paramInt;
    this.tagLen = 0;
    this.length = (4 + this.tagLen);
  }

  public byte[] toByteArray()
  {
    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
    localByteArrayOutputStream.write(this.length);
    localByteArrayOutputStream.write(this.tag);
    localByteArrayOutputStream.write(this.tagLen);
    if (this.time_offset != null)
      localByteArrayOutputStream.write(this.time_offset.intValue());
    if (this.usec_offset != null)
      localByteArrayOutputStream.write(this.usec_offset.intValue());
    return localByteArrayOutputStream.toByteArray();
  }
}