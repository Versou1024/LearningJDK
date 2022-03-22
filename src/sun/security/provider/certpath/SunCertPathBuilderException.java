package sun.security.provider.certpath;

import java.security.cert.CertPathBuilderException;

public class SunCertPathBuilderException extends CertPathBuilderException
{
  private static final long serialVersionUID = -7814288414129264709L;
  private transient AdjacencyList adjList;

  public SunCertPathBuilderException()
  {
  }

  public SunCertPathBuilderException(String paramString)
  {
    super(paramString);
  }

  public SunCertPathBuilderException(Throwable paramThrowable)
  {
    super(paramThrowable);
  }

  public SunCertPathBuilderException(String paramString, Throwable paramThrowable)
  {
    super(paramString, paramThrowable);
  }

  SunCertPathBuilderException(String paramString, AdjacencyList paramAdjacencyList)
  {
    this(paramString);
    this.adjList = paramAdjacencyList;
  }

  SunCertPathBuilderException(String paramString, Throwable paramThrowable, AdjacencyList paramAdjacencyList)
  {
    this(paramString, paramThrowable);
    this.adjList = paramAdjacencyList;
  }

  public AdjacencyList getAdjacencyList()
  {
    return this.adjList;
  }
}