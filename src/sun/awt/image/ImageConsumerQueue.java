package sun.awt.image;

import java.awt.image.ImageConsumer;

class ImageConsumerQueue
{
  ImageConsumerQueue next;
  ImageConsumer consumer;
  boolean interested;
  Object securityContext;
  boolean secure;

  static ImageConsumerQueue removeConsumer(ImageConsumerQueue paramImageConsumerQueue, ImageConsumer paramImageConsumer, boolean paramBoolean)
  {
    Object localObject = null;
    for (ImageConsumerQueue localImageConsumerQueue = paramImageConsumerQueue; localImageConsumerQueue != null; localImageConsumerQueue = localImageConsumerQueue.next)
    {
      if (localImageConsumerQueue.consumer == paramImageConsumer)
      {
        if (localObject == null)
          paramImageConsumerQueue = localImageConsumerQueue.next;
        else
          localObject.next = localImageConsumerQueue.next;
        localImageConsumerQueue.interested = paramBoolean;
        break;
      }
      localObject = localImageConsumerQueue;
    }
    return paramImageConsumerQueue;
  }

  static boolean isConsumer(ImageConsumerQueue paramImageConsumerQueue, ImageConsumer paramImageConsumer)
  {
    for (ImageConsumerQueue localImageConsumerQueue = paramImageConsumerQueue; localImageConsumerQueue != null; localImageConsumerQueue = localImageConsumerQueue.next)
      if (localImageConsumerQueue.consumer == paramImageConsumer)
        return true;
    return false;
  }

  ImageConsumerQueue(InputStreamImageSource paramInputStreamImageSource, ImageConsumer paramImageConsumer)
  {
    this.consumer = paramImageConsumer;
    this.interested = true;
    if (paramImageConsumer instanceof ImageRepresentation)
    {
      localObject = (ImageRepresentation)paramImageConsumer;
      if (((ImageRepresentation)localObject).image.source != paramInputStreamImageSource)
        throw new SecurityException("ImageRep added to wrong image source");
      this.secure = true;
    }
    else
    {
      localObject = System.getSecurityManager();
      if (localObject != null)
        this.securityContext = ((SecurityManager)localObject).getSecurityContext();
      else
        this.securityContext = null;
    }
  }

  public String toString()
  {
    return "[" + this.consumer + ", " + ((this.interested) ? "" : "not ") + "interested" + ((this.securityContext != null) ? ", " + this.securityContext : "") + "]";
  }
}