package sun.java2d.pipe;

import java.awt.AlphaComposite;
import java.awt.Composite;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.SurfaceType;

public abstract class BufferedMaskFill extends MaskFill
{
  protected final RenderQueue rq;

  protected BufferedMaskFill(RenderQueue paramRenderQueue, SurfaceType paramSurfaceType1, CompositeType paramCompositeType, SurfaceType paramSurfaceType2)
  {
    super(paramSurfaceType1, paramCompositeType, paramSurfaceType2);
    this.rq = paramRenderQueue;
  }

  public void MaskFill(SunGraphics2D paramSunGraphics2D, SurfaceData paramSurfaceData, Composite paramComposite, int paramInt1, int paramInt2, int paramInt3, int paramInt4, byte[] paramArrayOfByte, int paramInt5, int paramInt6)
  {
    AlphaComposite localAlphaComposite = (AlphaComposite)paramComposite;
    if (localAlphaComposite.getRule() != 3)
      paramComposite = AlphaComposite.SrcOver;
    this.rq.lock();
    try
    {
      int i;
      validateContext(paramSunGraphics2D, paramComposite, 2);
      if (paramArrayOfByte != null)
        i = paramArrayOfByte.length + 3 & 0xFFFFFFFC;
      else
        i = 0;
      int j = 32 + i;
      RenderBuffer localRenderBuffer = this.rq.getBuffer();
      if (j <= localRenderBuffer.capacity())
      {
        if (j > localRenderBuffer.remaining())
          this.rq.flushNow();
        localRenderBuffer.putInt(32);
        localRenderBuffer.putInt(paramInt1).putInt(paramInt2).putInt(paramInt3).putInt(paramInt4);
        localRenderBuffer.putInt(paramInt5);
        localRenderBuffer.putInt(paramInt6);
        localRenderBuffer.putInt(i);
        if (paramArrayOfByte != null)
        {
          int k = i - paramArrayOfByte.length;
          localRenderBuffer.put(paramArrayOfByte);
          if (k != 0)
            localRenderBuffer.position(localRenderBuffer.position() + k);
        }
      }
      else
      {
        this.rq.flushAndInvokeNow(new Runnable(this, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, paramArrayOfByte)
        {
          public void run()
          {
            this.this$0.maskFill(this.val$x, this.val$y, this.val$w, this.val$h, this.val$maskoff, this.val$maskscan, this.val$mask.length, this.val$mask);
          }
        });
      }
    }
    finally
    {
      this.rq.unlock();
    }
  }

  protected abstract void maskFill(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, byte[] paramArrayOfByte);

  protected abstract void validateContext(SunGraphics2D paramSunGraphics2D, Composite paramComposite, int paramInt);
}