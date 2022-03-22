package sun.security.jgss.spnego;

import java.io.IOException;
import java.io.PrintStream;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import sun.security.jgss.GSSUtil;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class NegTokenInit extends SpNegoToken
{
  private byte[] mechTypes = null;
  private Oid[] mechTypeList = null;
  private byte[] reqFlags = null;
  private byte[] mechToken = null;
  private byte[] mechListMIC = null;

  NegTokenInit(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3, byte[] paramArrayOfByte4)
  {
    super(0);
    this.mechTypes = paramArrayOfByte1;
    this.reqFlags = paramArrayOfByte2;
    this.mechToken = paramArrayOfByte3;
    this.mechListMIC = paramArrayOfByte4;
  }

  public NegTokenInit(byte[] paramArrayOfByte)
    throws GSSException
  {
    super(0);
    parseToken(paramArrayOfByte);
  }

  final byte[] encode()
    throws GSSException
  {
    DerOutputStream localDerOutputStream1;
    try
    {
      localDerOutputStream1 = new DerOutputStream();
      if (this.mechTypes != null)
        localDerOutputStream1.write(DerValue.createTag(-128, true, 0), this.mechTypes);
      if (this.reqFlags != null)
      {
        localDerOutputStream2 = new DerOutputStream();
        localDerOutputStream2.putBitString(this.reqFlags);
        localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream2);
      }
      if (this.mechToken != null)
      {
        localDerOutputStream2 = new DerOutputStream();
        localDerOutputStream2.putOctetString(this.mechToken);
        localDerOutputStream1.write(DerValue.createTag(-128, true, 2), localDerOutputStream2);
      }
      if (this.mechListMIC != null)
      {
        if (DEBUG)
          System.out.println("SpNegoToken NegTokenInit: sending MechListMIC");
        localDerOutputStream2 = new DerOutputStream();
        localDerOutputStream2.putOctetString(this.mechListMIC);
        localDerOutputStream1.write(DerValue.createTag(-128, true, 3), localDerOutputStream2);
      }
      DerOutputStream localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.write(48, localDerOutputStream1);
      return localDerOutputStream2.toByteArray();
    }
    catch (IOException localIOException)
    {
      throw new GSSException(10, -1, "Invalid SPNEGO NegTokenInit token : " + localIOException.getMessage());
    }
  }

  private void parseToken(byte[] paramArrayOfByte)
    throws GSSException
  {
    DerValue localDerValue1;
    try
    {
      DerValue localDerValue3;
      localDerValue1 = new DerValue(paramArrayOfByte);
      if (!(localDerValue1.isContextSpecific(0)))
        throw new IOException("SPNEGO NegoTokenInit : did not have right token type");
      DerValue localDerValue2 = localDerValue1.data.getDerValue();
      if (localDerValue2.tag != 48)
        throw new IOException("SPNEGO NegoTokenInit : did not have the Sequence tag");
      if (localDerValue2.data.available() > 0)
      {
        localDerValue3 = localDerValue2.data.getDerValue();
        if (!(localDerValue3.isContextSpecific(0)))
          throw new IOException("SPNEGO NegoTokenInit : did not have the right context tag for mechTypes");
        DerInputStream localDerInputStream = localDerValue3.data;
        this.mechTypes = localDerInputStream.toByteArray();
        DerValue[] arrayOfDerValue = localDerInputStream.getSequence(0);
        this.mechTypeList = new Oid[arrayOfDerValue.length];
        ObjectIdentifier localObjectIdentifier = null;
        for (int i = 0; i < arrayOfDerValue.length; ++i)
        {
          localObjectIdentifier = arrayOfDerValue[i].getOID();
          if (DEBUG)
            System.out.println("SpNegoToken NegTokenInit: reading Mechanism Oid = " + localObjectIdentifier);
          this.mechTypeList[i] = new Oid(localObjectIdentifier.toString());
        }
      }
      if (localDerValue2.data.available() > 0)
      {
        localDerValue3 = localDerValue2.data.getDerValue();
        if ((localDerValue3.isContextSpecific(1)) && (localDerValue2.data.available() > 0))
          localDerValue3 = localDerValue2.data.getDerValue();
        if (!(localDerValue3.isContextSpecific(2)))
          throw new IOException("SPNEGO NegoTokenInit : did not have the right context tag for mechToken");
        if (DEBUG)
          System.out.println("SpNegoToken NegTokenInit: reading Mech Token");
        this.mechToken = localDerValue3.data.getOctetString();
      }
      if ((!(GSSUtil.useMSInterop())) && (localDerValue2.data.available() > 0))
      {
        if (DEBUG)
          System.out.println("SpNegoToken NegTokenInit: receiving MechListMIC");
        localDerValue3 = localDerValue2.data.getDerValue();
        if (!(localDerValue3.isContextSpecific(3)))
          throw new IOException("SPNEGO NegoTokenInit : did not have the right context tag for MICToken");
        this.mechListMIC = localDerValue3.data.getOctetString();
        if (DEBUG)
          System.out.println("SpNegoToken NegTokenInit: MechListMIC Token = " + getHexBytes(this.mechListMIC));
      }
      else if (DEBUG)
      {
        System.out.println("SpNegoToken NegTokenInit : no MIC token included");
      }
    }
    catch (IOException localIOException)
    {
      throw new GSSException(10, -1, "Invalid SPNEGO NegTokenInit token : " + localIOException.getMessage());
    }
  }

  byte[] getMechTypes()
  {
    return this.mechTypes;
  }

  public Oid[] getMechTypeList()
  {
    return this.mechTypeList;
  }

  byte[] getReqFlags()
  {
    return this.reqFlags;
  }

  public byte[] getMechToken()
  {
    return this.mechToken;
  }

  byte[] getMechListMIC()
  {
    return this.mechListMIC;
  }
}