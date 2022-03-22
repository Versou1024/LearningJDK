package sun.security.x509;

import java.io.IOException;
import java.util.Enumeration;
import sun.security.util.BitArray;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class ReasonFlags
{
  public static final String UNUSED = "unused";
  public static final String KEY_COMPROMISE = "key_compromise";
  public static final String CA_COMPROMISE = "ca_compromise";
  public static final String AFFILIATION_CHANGED = "affiliation_changed";
  public static final String SUPERSEDED = "superseded";
  public static final String CESSATION_OF_OPERATION = "cessation_of_operation";
  public static final String CERTIFICATE_HOLD = "certificate_hold";
  public static final String PRIVILEGE_WITHDRAWN = "privilege_withdrawn";
  public static final String AA_COMPROMISE = "aa_compromise";
  private static final String[] NAMES = { "unused", "key_compromise", "ca_compromise", "affiliation_changed", "superseded", "cessation_of_operation", "certificate_hold", "privilege_withdrawn", "aa_compromise" };
  private boolean[] bitString;

  private static int name2Index(String paramString)
    throws IOException
  {
    for (int i = 0; i < NAMES.length; ++i)
      if (NAMES[i].equalsIgnoreCase(paramString))
        return i;
    throw new IOException("Name not recognized by ReasonFlags");
  }

  private boolean isSet(int paramInt)
  {
    return this.bitString[paramInt];
  }

  private void set(int paramInt, boolean paramBoolean)
  {
    if (paramInt >= this.bitString.length)
    {
      boolean[] arrayOfBoolean = new boolean[paramInt + 1];
      System.arraycopy(this.bitString, 0, arrayOfBoolean, 0, this.bitString.length);
      this.bitString = arrayOfBoolean;
    }
    this.bitString[paramInt] = paramBoolean;
  }

  public ReasonFlags(byte[] paramArrayOfByte)
  {
    this.bitString = new BitArray(paramArrayOfByte.length * 8, paramArrayOfByte).toBooleanArray();
  }

  public ReasonFlags(boolean[] paramArrayOfBoolean)
  {
    this.bitString = paramArrayOfBoolean;
  }

  public ReasonFlags(BitArray paramBitArray)
  {
    this.bitString = paramBitArray.toBooleanArray();
  }

  public ReasonFlags(DerInputStream paramDerInputStream)
    throws IOException
  {
    DerValue localDerValue = paramDerInputStream.getDerValue();
    this.bitString = localDerValue.getUnalignedBitString(true).toBooleanArray();
  }

  public ReasonFlags(DerValue paramDerValue)
    throws IOException
  {
    this.bitString = paramDerValue.getUnalignedBitString(true).toBooleanArray();
  }

  public boolean[] getFlags()
  {
    return this.bitString;
  }

  public void set(String paramString, Object paramObject)
    throws IOException
  {
    if (!(paramObject instanceof Boolean))
      throw new IOException("Attribute must be of type Boolean.");
    boolean bool = ((Boolean)paramObject).booleanValue();
    set(name2Index(paramString), bool);
  }

  public Object get(String paramString)
    throws IOException
  {
    return Boolean.valueOf(isSet(name2Index(paramString)));
  }

  public void delete(String paramString)
    throws IOException
  {
    set(paramString, Boolean.FALSE);
  }

  public String toString()
  {
    String str = "Reason Flags [\n";
    try
    {
      if (isSet(0))
        str = str + "  Unused\n";
      if (isSet(1))
        str = str + "  Key Compromise\n";
      if (isSet(2))
        str = str + "  CA Compromise\n";
      if (isSet(3))
        str = str + "  Affiliation_Changed\n";
      if (isSet(4))
        str = str + "  Superseded\n";
      if (isSet(5))
        str = str + "  Cessation Of Operation\n";
      if (isSet(6))
        str = str + "  Certificate Hold\n";
      if (isSet(7))
        str = str + "  Privilege Withdrawn\n";
      if (isSet(8))
        str = str + "  AA Compromise\n";
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
    }
    str = str + "]\n";
    return str;
  }

  public void encode(DerOutputStream paramDerOutputStream)
    throws IOException
  {
    paramDerOutputStream.putUnalignedBitString(new BitArray(this.bitString));
  }

  public Enumeration getElements()
  {
    AttributeNameEnumeration localAttributeNameEnumeration = new AttributeNameEnumeration();
    for (int i = 0; i < NAMES.length; ++i)
      localAttributeNameEnumeration.addElement(NAMES[i]);
    return localAttributeNameEnumeration.elements();
  }
}