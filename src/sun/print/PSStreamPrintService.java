package sun.print;

import java.io.OutputStream;
import java.util.Locale;
import javax.print.DocFlavor;
import javax.print.DocFlavor.SERVICE_FORMATTED;
import javax.print.DocPrintJob;
import javax.print.ServiceUIFactory;
import javax.print.StreamPrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.AttributeSetUtilities;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.ColorSupported;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.CopiesSupported;
import javax.print.attribute.standard.Fidelity;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize.ISO;
import javax.print.attribute.standard.MediaSize.NA;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.attribute.standard.SheetCollate;
import javax.print.attribute.standard.Sides;
import javax.print.event.PrintServiceAttributeListener;

public class PSStreamPrintService extends StreamPrintService
  implements SunPrinterJobService
{
  private static final Class[] suppAttrCats = { Chromaticity.class, Copies.class, Fidelity.class, JobName.class, Media.class, MediaPrintableArea.class, OrientationRequested.class, PageRanges.class, RequestingUserName.class, SheetCollate.class, Sides.class };
  private static int MAXCOPIES = 1000;
  private static final MediaSizeName[] mediaSizes = { MediaSizeName.NA_LETTER, MediaSizeName.TABLOID, MediaSizeName.LEDGER, MediaSizeName.NA_LEGAL, MediaSizeName.EXECUTIVE, MediaSizeName.ISO_A3, MediaSizeName.ISO_A4, MediaSizeName.ISO_A5, MediaSizeName.ISO_B4, MediaSizeName.ISO_B5 };

  public PSStreamPrintService(OutputStream paramOutputStream)
  {
    super(paramOutputStream);
  }

  public String getOutputFormat()
  {
    return "application/postscript";
  }

  public DocFlavor[] getSupportedDocFlavors()
  {
    return PSStreamPrinterFactory.getFlavors();
  }

  public DocPrintJob createPrintJob()
  {
    return new PSStreamPrintJob(this);
  }

  public boolean usesClass(Class paramClass)
  {
    return (paramClass == PSPrinterJob.class);
  }

  public String getName()
  {
    return "Postscript output";
  }

  public void addPrintServiceAttributeListener(PrintServiceAttributeListener paramPrintServiceAttributeListener)
  {
  }

  public void removePrintServiceAttributeListener(PrintServiceAttributeListener paramPrintServiceAttributeListener)
  {
  }

  public <T extends PrintServiceAttribute> T getAttribute(Class<T> paramClass)
  {
    if (paramClass == null)
      throw new NullPointerException("category");
    if (!(PrintServiceAttribute.class.isAssignableFrom(paramClass)))
      throw new IllegalArgumentException("Not a PrintServiceAttribute");
    if (paramClass == ColorSupported.class)
      return ColorSupported.SUPPORTED;
    return null;
  }

  public PrintServiceAttributeSet getAttributes()
  {
    HashPrintServiceAttributeSet localHashPrintServiceAttributeSet = new HashPrintServiceAttributeSet();
    localHashPrintServiceAttributeSet.add(ColorSupported.SUPPORTED);
    return AttributeSetUtilities.unmodifiableView(localHashPrintServiceAttributeSet);
  }

  public boolean isDocFlavorSupported(DocFlavor paramDocFlavor)
  {
    DocFlavor[] arrayOfDocFlavor = getSupportedDocFlavors();
    for (int i = 0; i < arrayOfDocFlavor.length; ++i)
      if (paramDocFlavor.equals(arrayOfDocFlavor[i]))
        return true;
    return false;
  }

  public Class<?>[] getSupportedAttributeCategories()
  {
    Class[] arrayOfClass = new Class[suppAttrCats.length];
    System.arraycopy(suppAttrCats, 0, arrayOfClass, 0, arrayOfClass.length);
    return arrayOfClass;
  }

  public boolean isAttributeCategorySupported(Class<? extends Attribute> paramClass)
  {
    if (paramClass == null)
      throw new NullPointerException("null category");
    if (!(Attribute.class.isAssignableFrom(paramClass)))
      throw new IllegalArgumentException(paramClass + " is not an Attribute");
    for (int i = 0; i < suppAttrCats.length; ++i)
      if (paramClass == suppAttrCats[i])
        return true;
    return false;
  }

  public Object getDefaultAttributeValue(Class<? extends Attribute> paramClass)
  {
    String str;
    if (paramClass == null)
      throw new NullPointerException("null category");
    if (!(Attribute.class.isAssignableFrom(paramClass)))
      throw new IllegalArgumentException(paramClass + " is not an Attribute");
    if (!(isAttributeCategorySupported(paramClass)))
      return null;
    if (paramClass == Copies.class)
      return new Copies(1);
    if (paramClass == Chromaticity.class)
      return Chromaticity.COLOR;
    if (paramClass == Fidelity.class)
      return Fidelity.FIDELITY_FALSE;
    if (paramClass == Media.class)
    {
      str = Locale.getDefault().getCountry();
      if ((str != null) && (((str.equals("")) || (str.equals(Locale.US.getCountry())) || (str.equals(Locale.CANADA.getCountry())))))
        return MediaSizeName.NA_LETTER;
      return MediaSizeName.ISO_A4;
    }
    if (paramClass == MediaPrintableArea.class)
    {
      float f1;
      float f2;
      str = Locale.getDefault().getCountry();
      float f3 = 0.5F;
      if ((str != null) && (((str.equals("")) || (str.equals(Locale.US.getCountry())) || (str.equals(Locale.CANADA.getCountry())))))
      {
        f1 = MediaSize.NA.LETTER.getX(25400) - 2F * f3;
        f2 = MediaSize.NA.LETTER.getY(25400) - 2F * f3;
      }
      else
      {
        f1 = MediaSize.ISO.A4.getX(25400) - 2F * f3;
        f2 = MediaSize.ISO.A4.getY(25400) - 2F * f3;
      }
      return new MediaPrintableArea(f3, f3, f1, f2, 25400);
    }
    if (paramClass == OrientationRequested.class)
      return OrientationRequested.PORTRAIT;
    if (paramClass == PageRanges.class)
      return new PageRanges(1, 2147483647);
    if (paramClass == SheetCollate.class)
      return SheetCollate.UNCOLLATED;
    if (paramClass == Sides.class)
      return Sides.ONE_SIDED;
    return null;
  }

  public Object getSupportedAttributeValues(Class<? extends Attribute> paramClass, DocFlavor paramDocFlavor, AttributeSet paramAttributeSet)
  {
    Object localObject1;
    if (paramClass == null)
      throw new NullPointerException("null category");
    if (!(Attribute.class.isAssignableFrom(paramClass)))
      throw new IllegalArgumentException(paramClass + " does not implement Attribute");
    if ((paramDocFlavor != null) && (!(isDocFlavorSupported(paramDocFlavor))))
      throw new IllegalArgumentException(paramDocFlavor + " is an unsupported flavor");
    if (!(isAttributeCategorySupported(paramClass)))
      return null;
    if (paramClass == Chromaticity.class)
    {
      localObject1 = new Chromaticity[1];
      localObject1[0] = Chromaticity.COLOR;
      return localObject1;
    }
    if (paramClass == JobName.class)
      return new JobName("", null);
    if (paramClass == RequestingUserName.class)
      return new RequestingUserName("", null);
    if (paramClass == OrientationRequested.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))
      {
        localObject1 = new OrientationRequested[3];
        localObject1[0] = OrientationRequested.PORTRAIT;
        localObject1[1] = OrientationRequested.LANDSCAPE;
        localObject1[2] = OrientationRequested.REVERSE_LANDSCAPE;
        return localObject1;
      }
      return null;
    }
    if ((paramClass == Copies.class) || (paramClass == CopiesSupported.class))
      return new CopiesSupported(1, MAXCOPIES);
    if (paramClass == Media.class)
    {
      localObject1 = new Media[mediaSizes.length];
      System.arraycopy(mediaSizes, 0, localObject1, 0, mediaSizes.length);
      return localObject1;
    }
    if (paramClass == Fidelity.class)
    {
      localObject1 = new Fidelity[2];
      localObject1[0] = Fidelity.FIDELITY_FALSE;
      localObject1[1] = Fidelity.FIDELITY_TRUE;
      return localObject1;
    }
    if (paramClass == MediaPrintableArea.class)
    {
      if (paramAttributeSet == null)
        return null;
      localObject1 = (javax.print.attribute.standard.MediaSize)paramAttributeSet.get(javax.print.attribute.standard.MediaSize.class);
      if (localObject1 == null)
      {
        localObject2 = (Media)paramAttributeSet.get(Media.class);
        if ((localObject2 != null) && (localObject2 instanceof MediaSizeName))
        {
          MediaSizeName localMediaSizeName = (MediaSizeName)localObject2;
          localObject1 = javax.print.attribute.standard.MediaSize.getMediaSizeForName(localMediaSizeName);
        }
      }
      if (localObject1 == null)
        return null;
      Object localObject2 = new MediaPrintableArea[1];
      float f1 = ((javax.print.attribute.standard.MediaSize)localObject1).getX(25400);
      float f2 = ((javax.print.attribute.standard.MediaSize)localObject1).getY(25400);
      float f3 = 0.5F;
      float f4 = 0.5F;
      if (f1 < 5.0F)
        f3 = f1 / 10.0F;
      if (f2 < 5.0F)
        f4 = f2 / 10.0F;
      localObject2[0] = new MediaPrintableArea(f3, f4, f1 - 2F * f3, f2 - 2F * f4, 25400);
      return localObject2;
    }
    if (paramClass == PageRanges.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))
      {
        localObject1 = new PageRanges[1];
        localObject1[0] = new PageRanges(1, 2147483647);
        return localObject1;
      }
      return null;
    }
    if (paramClass == SheetCollate.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))
      {
        localObject1 = new SheetCollate[2];
        localObject1[0] = SheetCollate.UNCOLLATED;
        localObject1[1] = SheetCollate.COLLATED;
        return localObject1;
      }
      localObject1 = new SheetCollate[1];
      localObject1[0] = SheetCollate.UNCOLLATED;
      return localObject1;
    }
    if (paramClass == Sides.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))
      {
        localObject1 = new Sides[3];
        localObject1[0] = Sides.ONE_SIDED;
        localObject1[1] = Sides.TWO_SIDED_LONG_EDGE;
        localObject1[2] = Sides.TWO_SIDED_SHORT_EDGE;
        return localObject1;
      }
      return null;
    }
    return null;
  }

  private boolean isSupportedCopies(Copies paramCopies)
  {
    int i = paramCopies.getValue();
    return ((i > 0) && (i < MAXCOPIES));
  }

  private boolean isSupportedMedia(MediaSizeName paramMediaSizeName)
  {
    for (int i = 0; i < mediaSizes.length; ++i)
      if (paramMediaSizeName.equals(mediaSizes[i]))
        return true;
    return false;
  }

  public boolean isAttributeValueSupported(Attribute paramAttribute, DocFlavor paramDocFlavor, AttributeSet paramAttributeSet)
  {
    if (paramAttribute == null)
      throw new NullPointerException("null attribute");
    if ((paramDocFlavor != null) && (!(isDocFlavorSupported(paramDocFlavor))))
      throw new IllegalArgumentException(paramDocFlavor + " is an unsupported flavor");
    Class localClass = paramAttribute.getCategory();
    if (!(isAttributeCategorySupported(localClass)))
      return false;
    if (paramAttribute.getCategory() == Chromaticity.class)
      return (paramAttribute == Chromaticity.COLOR);
    if (paramAttribute.getCategory() == Copies.class)
      return isSupportedCopies((Copies)paramAttribute);
    if ((paramAttribute.getCategory() == Media.class) && (paramAttribute instanceof MediaSizeName))
      return isSupportedMedia((MediaSizeName)paramAttribute);
    if (paramAttribute.getCategory() == OrientationRequested.class)
    {
      if ((paramAttribute != OrientationRequested.REVERSE_PORTRAIT) && (((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))))
        break label305;
      return false;
    }
    if (paramAttribute.getCategory() == PageRanges.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))
        break label305;
      return false;
    }
    if (paramAttribute.getCategory() == SheetCollate.class)
    {
      if ((paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)))
        break label305;
      return false;
    }
    label305: return ((paramAttribute.getCategory() != Sides.class) || (paramDocFlavor == null) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) || (paramDocFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)));
  }

  public AttributeSet getUnsupportedAttributes(DocFlavor paramDocFlavor, AttributeSet paramAttributeSet)
  {
    if ((paramDocFlavor != null) && (!(isDocFlavorSupported(paramDocFlavor))))
      throw new IllegalArgumentException("flavor " + paramDocFlavor + "is not supported");
    if (paramAttributeSet == null)
      return null;
    HashAttributeSet localHashAttributeSet = new HashAttributeSet();
    Attribute[] arrayOfAttribute = paramAttributeSet.toArray();
    for (int i = 0; i < arrayOfAttribute.length; ++i)
      try
      {
        Attribute localAttribute = arrayOfAttribute[i];
        if (!(isAttributeCategorySupported(localAttribute.getCategory())))
          localHashAttributeSet.add(localAttribute);
        else if (!(isAttributeValueSupported(localAttribute, paramDocFlavor, paramAttributeSet)))
          localHashAttributeSet.add(localAttribute);
      }
      catch (ClassCastException localClassCastException)
      {
      }
    if (localHashAttributeSet.isEmpty())
      return null;
    return localHashAttributeSet;
  }

  public ServiceUIFactory getServiceUIFactory()
  {
    return null;
  }

  public String toString()
  {
    return "PSStreamPrintService: " + getName();
  }

  public boolean equals(Object paramObject)
  {
    return ((paramObject == this) || ((paramObject instanceof PSStreamPrintService) && (((PSStreamPrintService)paramObject).getName().equals(getName()))));
  }

  public int hashCode()
  {
    return (getClass().hashCode() + getName().hashCode());
  }
}