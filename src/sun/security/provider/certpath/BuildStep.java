package sun.security.provider.certpath;

import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;

public class BuildStep
{
  private static final Debug debug = Debug.getInstance("certpath");
  private Vertex vertex;
  private X509Certificate cert;
  private Throwable throwable;
  private int result;
  public static final int POSSIBLE = 1;
  public static final int BACK = 2;
  public static final int FOLLOW = 3;
  public static final int FAIL = 4;
  public static final int SUCCEED = 5;

  public BuildStep(Vertex paramVertex, int paramInt)
  {
    this.vertex = paramVertex;
    if (this.vertex != null)
    {
      this.cert = ((X509Certificate)this.vertex.getCertificate());
      this.throwable = this.vertex.getThrowable();
    }
    this.result = paramInt;
  }

  public Vertex getVertex()
  {
    return this.vertex;
  }

  public X509Certificate getCertificate()
  {
    return this.cert;
  }

  public String getIssuerName()
  {
    return ((this.cert == null) ? null : this.cert.getIssuerX500Principal().toString());
  }

  public String getIssuerName(String paramString)
  {
    return ((this.cert == null) ? paramString : this.cert.getIssuerX500Principal().toString());
  }

  public String getSubjectName()
  {
    return ((this.cert == null) ? null : this.cert.getSubjectX500Principal().toString());
  }

  public String getSubjectName(String paramString)
  {
    return ((this.cert == null) ? paramString : this.cert.getSubjectX500Principal().toString());
  }

  public Throwable getThrowable()
  {
    return this.throwable;
  }

  public int getResult()
  {
    return this.result;
  }

  public String resultToString(int paramInt)
  {
    String str = "";
    switch (paramInt)
    {
    case 1:
      str = "Certificate to be tried.\n";
      break;
    case 2:
      str = "Certificate backed out since path does not satisfy build requirements.\n";
      break;
    case 3:
      str = "Certificate satisfies conditions.\n";
      break;
    case 4:
      str = "Certificate backed out since path does not satisfy conditions.\n";
      break;
    case 5:
      str = "Certificate satisfies conditions.\n";
      break;
    default:
      str = "Internal error: Invalid step result value.\n";
    }
    return str;
  }

  public String toString()
  {
    String str = "Internal Error\n";
    switch (this.result)
    {
    case 2:
    case 4:
      str = resultToString(this.result);
      str = str + this.vertex.throwableToString();
      break;
    case 1:
    case 3:
    case 5:
      str = resultToString(this.result);
      break;
    default:
      str = "Internal Error: Invalid step result\n";
    }
    return str;
  }

  public String verboseToString()
  {
    String str = resultToString(getResult());
    switch (this.result)
    {
    case 2:
    case 4:
      str = str + this.vertex.throwableToString();
      break;
    case 3:
    case 5:
      str = str + this.vertex.moreToString();
      break;
    case 1:
    }
    str = str + "Certificate contains:\n" + this.vertex.certToString();
    return str;
  }

  public String fullToString()
  {
    String str = resultToString(getResult());
    str = str + this.vertex.toString();
    return str;
  }
}