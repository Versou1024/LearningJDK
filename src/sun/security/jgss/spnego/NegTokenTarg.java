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

public class NegTokenTarg extends SpNegoToken
{
  private int negResult = 0;
  private Oid supportedMech = null;
  private byte[] responseToken = null;
  private byte[] mechListMIC = null;

  NegTokenTarg(int paramInt, Oid paramOid, byte[] paramArrayOfByte1, byte[] paramArrayOfByte2)
  {
    super(1);
    this.negResult = paramInt;
    this.supportedMech = paramOid;
    this.responseToken = paramArrayOfByte1;
    this.mechListMIC = paramArrayOfByte2;
  }

  public NegTokenTarg(byte[] paramArrayOfByte)
    throws GSSException
  {
    super(1);
    parseToken(paramArrayOfByte);
  }

  final byte[] encode()
    throws GSSException
  {
    DerOutputStream localDerOutputStream1;
    try
    {
      localDerOutputStream1 = new DerOutputStream();
      DerOutputStream localDerOutputStream2 = new DerOutputStream();
      localDerOutputStream2.putEnumerated(this.negResult);
      localDerOutputStream1.write(DerValue.createTag(-128, true, 0), localDerOutputStream2);
      if (this.supportedMech != null)
      {
        localDerOutputStream3 = new DerOutputStream();
        byte[] arrayOfByte = this.supportedMech.getDER();
        localDerOutputStream3.write(arrayOfByte);
        localDerOutputStream1.write(DerValue.createTag(-128, true, 1), localDerOutputStream3);
      }
      if (this.responseToken != null)
      {
        localDerOutputStream3 = new DerOutputStream();
        localDerOutputStream3.putOctetString(this.responseToken);
        localDerOutputStream1.write(DerValue.createTag(-128, true, 2), localDerOutputStream3);
      }
      if (this.mechListMIC != null)
      {
        if (DEBUG)
          System.out.println("SpNegoToken NegTokenTarg: sending MechListMIC");
        localDerOutputStream3 = new DerOutputStream();
        localDerOutputStream3.putOctetString(this.mechListMIC);
        localDerOutputStream1.write(DerValue.createTag(-128, true, 3), localDerOutputStream3);
      }
      else if ((GSSUtil.useMSInterop()) && (this.responseToken != null))
      {
        if (DEBUG)
          System.out.println("SpNegoToken NegTokenTarg: sending additional token for MS Interop");
        localDerOutputStream3 = new DerOutputStream();
        localDerOutputStream3.putOctetString(this.responseToken);
        localDerOutputStream1.write(DerValue.createTag(-128, true, 3), localDerOutputStream3);
      }
      DerOutputStream localDerOutputStream3 = new DerOutputStream();
      localDerOutputStream3.write(48, localDerOutputStream1);
      return localDerOutputStream3.toByteArray();
    }
    catch (IOException localIOException)
    {
      throw new GSSException(10, -1, "Invalid SPNEGO NegTokenTarg token : " + localIOException.getMessage());
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
      if (!(localDerValue1.isContextSpecific(1)))
        throw new IOException("SPNEGO NegoTokenTarg : did not have the right token type");
      DerValue localDerValue2 = localDerValue1.data.getDerValue();
      if (localDerValue2.tag != 48)
        throw new IOException("SPNEGO NegoTokenTarg : did not have the Sequence tag");
      if (localDerValue2.data.available() > 0)
      {
        localDerValue3 = localDerValue2.data.getDerValue();
        if (!(localDerValue3.isContextSpecific(0)))
          throw new IOException("SPNEGO NegoTokenTarg : did not have the right context tag for negResult");
        this.negResult = localDerValue3.data.getEnumerated();
        if (DEBUG)
          System.out.println("SpNegoToken NegTokenTarg: negotiated result = " + getNegoResultString(this.negResult));
      }
      if (localDerValue2.data.available() > 0)
      {
        localDerValue3 = localDerValue2.data.getDerValue();
        if (!(localDerValue3.isContextSpecific(1)))
          throw new IOException("SPNEGO NegoTokenTarg : did not have the right context tag for supportedMech");
        ObjectIdentifier localObjectIdentifier = localDerValue3.data.getOID();
        this.supportedMech = new Oid(localObjectIdentifier.toString());
        if (DEBUG)
          System.out.println("SpNegoToken NegTokenTarg: supported mechanism = " + this.supportedMech);
      }
      if (localDerValue2.data.available() > 0)
      {
        localDerValue3 = localDerValue2.data.getDerValue();
        if (!(localDerValue3.isContextSpecific(2)))
          throw new IOException("SPNEGO NegoTokenTarg : did not have the right context tag for response token");
        this.responseToken = localDerValue3.data.getOctetString();
      }
      if ((!(GSSUtil.useMSInterop())) && (localDerValue2.data.available() > 0))
      {
        if (DEBUG)
          System.out.println("SpNegoToken NegTokenTarg: receiving MechListMIC");
        localDerValue3 = localDerValue2.data.getDerValue();
        if (!(localDerValue3.isContextSpecific(3)))
          throw new IOException("SPNEGO NegoTokenTarg : did not have the right context tag for mechListMIC");
        this.mechListMIC = localDerValue3.data.getOctetString();
        if (DEBUG)
          System.out.println("SpNegoToken NegTokenTarg: MechListMIC Token = " + getHexBytes(this.mechListMIC));
      }
      else if (DEBUG)
      {
        System.out.println("SpNegoToken NegTokenTarg : no MIC token included");
      }
    }
    catch (IOException localIOException)
    {
      throw new GSSException(10, -1, "Invalid SPNEGO NegTokenTarg token : " + localIOException.getMessage());
    }
  }

  int getNegotiatedResult()
  {
    return this.negResult;
  }

  public Oid getSupportedMech()
  {
    return this.supportedMech;
  }

  byte[] getResponseToken()
  {
    return this.responseToken;
  }

  byte[] getMechListMIC()
  {
    return this.mechListMIC;
  }
}