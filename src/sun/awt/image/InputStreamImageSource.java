package sun.awt.image;

import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public abstract class InputStreamImageSource
  implements ImageProducer, ImageFetchable
{
  ImageConsumerQueue consumers;
  ImageDecoder decoder;
  ImageDecoder decoders;
  boolean awaitingFetch = false;

  abstract boolean checkSecurity(Object paramObject, boolean paramBoolean);

  int countConsumers(ImageConsumerQueue paramImageConsumerQueue)
  {
    int i = 0;
    while (paramImageConsumerQueue != null)
    {
      ++i;
      paramImageConsumerQueue = paramImageConsumerQueue.next;
    }
    return i;
  }

  synchronized int countConsumers()
  {
    ImageDecoder localImageDecoder = this.decoders;
    int i = countConsumers(this.consumers);
    while (localImageDecoder != null)
    {
      i += countConsumers(localImageDecoder.queue);
      localImageDecoder = localImageDecoder.next;
    }
    return i;
  }

  public void addConsumer(ImageConsumer paramImageConsumer)
  {
    addConsumer(paramImageConsumer, false);
  }

  synchronized void printQueue(ImageConsumerQueue paramImageConsumerQueue, String paramString)
  {
    while (paramImageConsumerQueue != null)
    {
      System.out.println(paramString + paramImageConsumerQueue);
      paramImageConsumerQueue = paramImageConsumerQueue.next;
    }
  }

  synchronized void printQueues(String paramString)
  {
    System.out.println(paramString + "[ -----------");
    printQueue(this.consumers, "  ");
    for (ImageDecoder localImageDecoder = this.decoders; localImageDecoder != null; localImageDecoder = localImageDecoder.next)
    {
      System.out.println("    " + localImageDecoder);
      printQueue(localImageDecoder.queue, "      ");
    }
    System.out.println("----------- ]" + paramString);
  }

  synchronized void addConsumer(ImageConsumer paramImageConsumer, boolean paramBoolean)
  {
    checkSecurity(null, false);
    for (Object localObject1 = this.decoders; localObject1 != null; localObject1 = ((ImageDecoder)localObject1).next)
      if (((ImageDecoder)localObject1).isConsumer(paramImageConsumer))
        return;
    for (localObject1 = this.consumers; (localObject1 != null) && (((ImageConsumerQueue)localObject1).consumer != paramImageConsumer); localObject1 = ((ImageConsumerQueue)localObject1).next);
    if (localObject1 == null)
    {
      localObject1 = new ImageConsumerQueue(this, paramImageConsumer);
      ((ImageConsumerQueue)localObject1).next = this.consumers;
      this.consumers = ((ImageConsumerQueue)localObject1);
    }
    else
    {
      if (!(((ImageConsumerQueue)localObject1).secure))
      {
        Object localObject2 = null;
        SecurityManager localSecurityManager = System.getSecurityManager();
        if (localSecurityManager != null)
          localObject2 = localSecurityManager.getSecurityContext();
        if (((ImageConsumerQueue)localObject1).securityContext == null)
        {
          ((ImageConsumerQueue)localObject1).securityContext = localObject2;
        }
        else if (!(((ImageConsumerQueue)localObject1).securityContext.equals(localObject2)))
        {
          errorConsumer((ImageConsumerQueue)localObject1, false);
          throw new SecurityException("Applets are trading image data!");
        }
      }
      ((ImageConsumerQueue)localObject1).interested = true;
    }
    if ((paramBoolean) && (this.decoder == null))
      startProduction();
  }

  public synchronized boolean isConsumer(ImageConsumer paramImageConsumer)
  {
    for (ImageDecoder localImageDecoder = this.decoders; localImageDecoder != null; localImageDecoder = localImageDecoder.next)
      if (localImageDecoder.isConsumer(paramImageConsumer))
        return true;
    return ImageConsumerQueue.isConsumer(this.consumers, paramImageConsumer);
  }

  private void errorAllConsumers(ImageConsumerQueue paramImageConsumerQueue, boolean paramBoolean)
  {
    while (paramImageConsumerQueue != null)
    {
      if (paramImageConsumerQueue.interested)
        errorConsumer(paramImageConsumerQueue, paramBoolean);
      paramImageConsumerQueue = paramImageConsumerQueue.next;
    }
  }

  private void errorConsumer(ImageConsumerQueue paramImageConsumerQueue, boolean paramBoolean)
  {
    paramImageConsumerQueue.consumer.imageComplete(1);
    if ((paramBoolean) && (paramImageConsumerQueue.consumer instanceof ImageRepresentation))
      ((ImageRepresentation)paramImageConsumerQueue.consumer).image.flush();
    removeConsumer(paramImageConsumerQueue.consumer);
  }

  public synchronized void removeConsumer(ImageConsumer paramImageConsumer)
  {
    for (ImageDecoder localImageDecoder = this.decoders; localImageDecoder != null; localImageDecoder = localImageDecoder.next)
      localImageDecoder.removeConsumer(paramImageConsumer);
    this.consumers = ImageConsumerQueue.removeConsumer(this.consumers, paramImageConsumer, false);
  }

  public void startProduction(ImageConsumer paramImageConsumer)
  {
    addConsumer(paramImageConsumer, true);
  }

  private synchronized void startProduction()
  {
    if (!(this.awaitingFetch))
    {
      ImageFetcher.add(this);
      this.awaitingFetch = true;
    }
  }

  private synchronized void stopProduction()
  {
    if (this.awaitingFetch)
    {
      ImageFetcher.remove(this);
      this.awaitingFetch = false;
    }
  }

  public void requestTopDownLeftRightResend(ImageConsumer paramImageConsumer)
  {
  }

  protected abstract ImageDecoder getDecoder();

  protected ImageDecoder decoderForType(InputStream paramInputStream, String paramString)
  {
    return null;
  }

  protected ImageDecoder getDecoder(InputStream paramInputStream)
  {
    if (!(paramInputStream.markSupported()))
      paramInputStream = new BufferedInputStream(paramInputStream);
    try
    {
      paramInputStream.mark(8);
      int i = paramInputStream.read();
      int j = paramInputStream.read();
      int k = paramInputStream.read();
      int l = paramInputStream.read();
      int i1 = paramInputStream.read();
      int i2 = paramInputStream.read();
      int i3 = paramInputStream.read();
      int i4 = paramInputStream.read();
      paramInputStream.reset();
      paramInputStream.mark(-1);
      if ((i == 71) && (j == 73) && (k == 70) && (l == 56))
        return new GifImageDecoder(this, paramInputStream);
      if ((i == 255) && (j == 216) && (k == 255))
        return new JPEGImageDecoder(this, paramInputStream);
      if ((i == 35) && (j == 100) && (k == 101) && (l == 102))
        return new XbmImageDecoder(this, paramInputStream);
      if ((i == 137) && (j == 80) && (k == 78) && (l == 71) && (i1 == 13) && (i2 == 10) && (i3 == 26) && (i4 == 10))
        return new PNGImageDecoder(this, paramInputStream);
    }
    catch (IOException localIOException)
    {
    }
    return null;
  }

  public void doFetch()
  {
    synchronized (this)
    {
      if (this.consumers != null)
        break label19;
      this.awaitingFetch = false;
      label19: return;
    }
    ??? = getDecoder();
    if (??? == null)
    {
      badDecoder();
    }
    else
    {
      setDecoder((ImageDecoder)???);
      try
      {
        ((ImageDecoder)???).produceImage();
      }
      catch (IOException localIOException)
      {
        localIOException.printStackTrace();
      }
      catch (ImageFormatException localImageFormatException)
      {
        localImageFormatException.printStackTrace();
      }
      finally
      {
        removeDecoder((ImageDecoder)???);
        if ((Thread.currentThread().isInterrupted()) || (!(Thread.currentThread().isAlive())))
          errorAllConsumers(((ImageDecoder)???).queue, true);
        else
          errorAllConsumers(((ImageDecoder)???).queue, false);
      }
    }
  }

  private void badDecoder()
  {
    ImageConsumerQueue localImageConsumerQueue;
    synchronized (this)
    {
      localImageConsumerQueue = this.consumers;
      this.consumers = null;
      this.awaitingFetch = false;
    }
    errorAllConsumers(localImageConsumerQueue, false);
  }

  private void setDecoder(ImageDecoder paramImageDecoder)
  {
    ImageConsumerQueue localImageConsumerQueue;
    synchronized (this)
    {
      paramImageDecoder.next = this.decoders;
      this.decoders = paramImageDecoder;
      this.decoder = paramImageDecoder;
      localImageConsumerQueue = this.consumers;
      paramImageDecoder.queue = localImageConsumerQueue;
      this.consumers = null;
      this.awaitingFetch = false;
    }
    while (localImageConsumerQueue != null)
    {
      if ((localImageConsumerQueue.interested) && (!(checkSecurity(localImageConsumerQueue.securityContext, true))))
        errorConsumer(localImageConsumerQueue, false);
      localImageConsumerQueue = localImageConsumerQueue.next;
    }
  }

  private synchronized void removeDecoder(ImageDecoder paramImageDecoder)
  {
    doneDecoding(paramImageDecoder);
    Object localObject = null;
    for (ImageDecoder localImageDecoder = this.decoders; localImageDecoder != null; localImageDecoder = localImageDecoder.next)
    {
      if (localImageDecoder == paramImageDecoder)
      {
        if (localObject == null)
        {
          this.decoders = localImageDecoder.next;
          return;
        }
        localObject.next = localImageDecoder.next;
        return;
      }
      localObject = localImageDecoder;
    }
  }

  synchronized void doneDecoding(ImageDecoder paramImageDecoder)
  {
    if (this.decoder == paramImageDecoder)
    {
      this.decoder = null;
      if (this.consumers != null)
        startProduction();
    }
  }

  void latchConsumers(ImageDecoder paramImageDecoder)
  {
    doneDecoding(paramImageDecoder);
  }

  synchronized void flush()
  {
    this.decoder = null;
  }
}