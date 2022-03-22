package sun.misc;

public class Sort
{
  private static void swap(Object[] paramArrayOfObject, int paramInt1, int paramInt2)
  {
    Object localObject = paramArrayOfObject[paramInt1];
    paramArrayOfObject[paramInt1] = paramArrayOfObject[paramInt2];
    paramArrayOfObject[paramInt2] = localObject;
  }

  public static void quicksort(Object[] paramArrayOfObject, int paramInt1, int paramInt2, Compare paramCompare)
  {
    if (paramInt1 >= paramInt2)
      return;
    swap(paramArrayOfObject, paramInt1, (paramInt1 + paramInt2) / 2);
    int j = paramInt1;
    for (int i = paramInt1 + 1; i <= paramInt2; ++i)
      if (paramCompare.doCompare(paramArrayOfObject[i], paramArrayOfObject[paramInt1]) < 0)
        swap(paramArrayOfObject, ++j, i);
    swap(paramArrayOfObject, paramInt1, j);
    quicksort(paramArrayOfObject, paramInt1, j - 1, paramCompare);
    quicksort(paramArrayOfObject, j + 1, paramInt2, paramCompare);
  }

  public static void quicksort(Object[] paramArrayOfObject, Compare paramCompare)
  {
    quicksort(paramArrayOfObject, 0, paramArrayOfObject.length - 1, paramCompare);
  }
}