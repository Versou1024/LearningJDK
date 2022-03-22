package sun.security.provider;

import java.io.IOException;

class NativeSeedGenerator extends SeedGenerator
{
  NativeSeedGenerator()
    throws IOException
  {
    if (!(nativeGenerateSeed(new byte[2])))
      throw new IOException("Required native CryptoAPI features not  available on this machine");
  }

  private static native boolean nativeGenerateSeed(byte[] paramArrayOfByte);

  void getSeedBytes(byte[] paramArrayOfByte)
  {
    if (!(nativeGenerateSeed(paramArrayOfByte)))
      throw new InternalError("Unexpected CryptoAPI failure generating seed");
  }

  byte getSeedByte()
  {
    byte[] arrayOfByte = new byte[1];
    getSeedBytes(arrayOfByte);
    return arrayOfByte[0];
  }
}