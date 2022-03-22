package sun.security.krb5.internal.ccache;

import java.io.IOException;
import java.io.OutputStream;
import sun.security.krb5.Asn1Exception;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.internal.AuthorizationData;
import sun.security.krb5.internal.HostAddresses;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Ticket;
import sun.security.krb5.internal.TicketFlags;
import sun.security.krb5.internal.util.KrbDataOutputStream;

public class CCacheOutputStream extends KrbDataOutputStream
  implements FileCCacheConstants
{
  public CCacheOutputStream(OutputStream paramOutputStream)
  {
    super(paramOutputStream);
  }

  public void writeHeader(PrincipalName paramPrincipalName, int paramInt)
    throws IOException
  {
    write((paramInt & 0xFF00) >> 8);
    write(paramInt & 0xFF);
    paramPrincipalName.writePrincipal(this);
  }

  public void addCreds(Credentials paramCredentials)
    throws IOException, Asn1Exception
  {
    paramCredentials.cname.writePrincipal(this);
    paramCredentials.sname.writePrincipal(this);
    paramCredentials.key.writeKey(this);
    write32((int)(paramCredentials.authtime.getTime() / 1000L));
    if (paramCredentials.starttime != null)
      write32((int)(paramCredentials.starttime.getTime() / 1000L));
    else
      write32(0);
    write32((int)(paramCredentials.endtime.getTime() / 1000L));
    if (paramCredentials.renewTill != null)
      write32((int)(paramCredentials.renewTill.getTime() / 1000L));
    else
      write32(0);
    if (paramCredentials.isEncInSKey)
      write8(1);
    else
      write8(0);
    writeFlags(paramCredentials.flags);
    if (paramCredentials.caddr == null)
      write32(0);
    else
      paramCredentials.caddr.writeAddrs(this);
    if (paramCredentials.authorizationData == null)
      write32(0);
    else
      paramCredentials.authorizationData.writeAuth(this);
    writeTicket(paramCredentials.ticket);
    writeTicket(paramCredentials.secondTicket);
  }

  void writeTicket(Ticket paramTicket)
    throws IOException, Asn1Exception
  {
    if (paramTicket == null)
    {
      write32(0);
    }
    else
    {
      byte[] arrayOfByte = paramTicket.asn1Encode();
      write32(arrayOfByte.length);
      write(arrayOfByte, 0, arrayOfByte.length);
    }
  }

  void writeFlags(TicketFlags paramTicketFlags)
    throws IOException
  {
    int i = 0;
    boolean[] arrayOfBoolean = paramTicketFlags.toBooleanArray();
    if (arrayOfBoolean[1] == 1)
      i |= 1073741824;
    if (arrayOfBoolean[2] == 1)
      i |= 536870912;
    if (arrayOfBoolean[3] == 1)
      i |= 268435456;
    if (arrayOfBoolean[4] == 1)
      i |= 134217728;
    if (arrayOfBoolean[5] == 1)
      i |= 67108864;
    if (arrayOfBoolean[6] == 1)
      i |= 33554432;
    if (arrayOfBoolean[7] == 1)
      i |= 16777216;
    if (arrayOfBoolean[8] == 1)
      i |= 8388608;
    if (arrayOfBoolean[9] == 1)
      i |= 4194304;
    if (arrayOfBoolean[10] == 1)
      i |= 2097152;
    if (arrayOfBoolean[11] == 1)
      i |= 1048576;
    write32(i);
  }
}