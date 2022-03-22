package sun.jdbc.odbc;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;

public class JdbcOdbcBoundArrayOfParams extends JdbcOdbcObject
{
  protected int numParams;
  protected Hashtable hashedLenIdx;
  protected Object[] storedParams;
  protected int[] paramLenIdx;
  protected Object[][] storedInputStreams;
  protected Object[][] paramSets;
  protected int[][] paramLenIdxSets;
  protected int batchSize;

  public JdbcOdbcBoundArrayOfParams(int paramInt)
  {
    this.numParams = paramInt;
    initialize();
  }

  public void initialize()
  {
    this.storedParams = new Object[this.numParams];
    this.paramLenIdx = new int[this.numParams];
    this.hashedLenIdx = new Hashtable();
    this.batchSize = 0;
    for (int i = 0; i < this.numParams; ++i)
      this.paramLenIdx[i] = -5;
  }

  public void storeValue(int paramInt1, Object paramObject, int paramInt2)
  {
    this.storedParams[paramInt1] = paramObject;
    this.paramLenIdx[paramInt1] = paramInt2;
  }

  public void clearParameterSet()
  {
    if (this.storedParams != null)
      for (int i = 0; i < this.numParams; ++i)
      {
        this.storedParams[i] = new Object();
        this.paramLenIdx[i] = -5;
      }
  }

  public Object[] getStoredParameterSet()
  {
    Object[] arrayOfObject = new Object[0];
    if (this.storedParams != null)
    {
      arrayOfObject = new Object[this.numParams];
      try
      {
        for (int i = 0; i < this.numParams; ++i)
          arrayOfObject[i] = this.storedParams[i];
      }
      catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
      {
        System.out.println("exception: " + localArrayIndexOutOfBoundsException.getMessage());
        localArrayIndexOutOfBoundsException.printStackTrace();
      }
    }
    return arrayOfObject;
  }

  public void storeRowIndex(int paramInt, int[] paramArrayOfInt)
  {
    this.hashedLenIdx.put(new Integer(paramInt), paramArrayOfInt);
  }

  public int[] getStoredRowIndex(int paramInt)
  {
    return ((int[])(int[])this.hashedLenIdx.get(new Integer(paramInt)));
  }

  public void clearStoredRowIndexs()
  {
    if (!(this.hashedLenIdx.isEmpty()))
      this.hashedLenIdx.clear();
  }

  public int[] getStoredIndexSet()
  {
    int[] arrayOfInt = new int[0];
    if (this.paramLenIdx != null)
    {
      arrayOfInt = new int[this.numParams];
      try
      {
        for (int i = 0; i < this.numParams; ++i)
        {
          arrayOfInt[i] = this.paramLenIdx[i];
          if (arrayOfInt[i] == -5)
          {
            arrayOfInt = new int[0];
            return arrayOfInt;
          }
        }
      }
      catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
      {
        System.out.println("exception: " + localArrayIndexOutOfBoundsException.getMessage());
        localArrayIndexOutOfBoundsException.printStackTrace();
      }
    }
    return arrayOfInt;
  }

  public void builtColumWiseParameteSets(int paramInt, Vector paramVector)
  {
    int[] arrayOfInt = new int[0];
    Object[] arrayOfObject = new Object[0];
    this.batchSize = paramInt;
    if (paramVector.size() == this.batchSize)
    {
      this.storedInputStreams = new Object[this.batchSize][this.numParams];
      this.paramSets = new Object[this.batchSize][this.numParams];
      this.paramLenIdxSets = new int[this.batchSize][this.numParams];
      for (int i = 0; i < this.batchSize; ++i)
      {
        arrayOfInt = getStoredRowIndex(i);
        arrayOfObject = (Object[])(Object[])paramVector.elementAt(i);
        for (int j = 0; j < this.numParams; ++j)
        {
          this.paramSets[i][j] = arrayOfObject[j];
          this.paramLenIdxSets[i][j] = arrayOfInt[j];
        }
      }
    }
  }

  public Object[] getColumnWiseParamSet(int paramInt)
  {
    Object[] arrayOfObject = new Object[this.batchSize];
    if (this.paramSets != null)
      for (int i = 0; i < this.batchSize; ++i)
        arrayOfObject[i] = this.paramSets[i][(paramInt - 1)];
    return arrayOfObject;
  }

  public int[] getColumnWiseIndexArray(int paramInt)
  {
    int[] arrayOfInt = new int[this.batchSize];
    if (this.paramLenIdxSets != null)
      for (int i = 0; i < this.batchSize; ++i)
        arrayOfInt[i] = this.paramLenIdxSets[i][(paramInt - 1)];
    return arrayOfInt;
  }

  public void setInputStreamElements(int paramInt, Object[] paramArrayOfObject)
  {
    if ((paramInt >= 1) && (paramInt <= this.numParams) && (this.storedInputStreams != null) && (paramArrayOfObject != null))
      for (int i = 0; i < this.batchSize; ++i)
        this.storedInputStreams[i][(paramInt - 1)] = paramArrayOfObject[i];
  }

  public InputStream getInputStreamElement(int paramInt1, int paramInt2)
  {
    InputStream localInputStream = null;
    if ((paramInt1 >= 1) && (paramInt1 <= this.numParams) && (paramInt2 >= 1) && (paramInt2 <= this.batchSize))
      localInputStream = (InputStream)this.storedInputStreams[(paramInt2 - 1)][(paramInt1 - 1)];
    return localInputStream;
  }

  public int getElementLength(int paramInt1, int paramInt2)
  {
    return this.paramLenIdxSets[(paramInt2 - 1)][(paramInt1 - 1)];
  }
}