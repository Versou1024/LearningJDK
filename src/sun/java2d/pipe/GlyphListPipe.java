package sun.java2d.pipe;

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.FontInfo;

public abstract class GlyphListPipe
  implements TextPipe
{
  public void drawString(SunGraphics2D paramSunGraphics2D, String paramString, double paramDouble1, double paramDouble2)
  {
    float f1;
    float f2;
    FontInfo localFontInfo = paramSunGraphics2D.getFontInfo();
    if (localFontInfo.pixelHeight > 100)
    {
      SurfaceData.outlineTextRenderer.drawString(paramSunGraphics2D, paramString, paramDouble1, paramDouble2);
      return;
    }
    if (paramSunGraphics2D.transformState >= 3)
    {
      localObject = { paramDouble1 + localFontInfo.originX, paramDouble2 + localFontInfo.originY };
      paramSunGraphics2D.transform.transform(localObject, 0, localObject, 0, 1);
      f1 = (float)localObject[0];
      f2 = (float)localObject[1];
    }
    else
    {
      f1 = (float)(paramDouble1 + localFontInfo.originX + paramSunGraphics2D.transX);
      f2 = (float)(paramDouble2 + localFontInfo.originY + paramSunGraphics2D.transY);
    }
    Object localObject = GlyphList.getInstance();
    if (((GlyphList)localObject).setFromString(localFontInfo, paramString, f1, f2))
    {
      drawGlyphList(paramSunGraphics2D, (GlyphList)localObject);
      ((GlyphList)localObject).dispose();
    }
    else
    {
      ((GlyphList)localObject).dispose();
      TextLayout localTextLayout = new TextLayout(paramString, paramSunGraphics2D.getFont(), paramSunGraphics2D.getFontRenderContext());
      localTextLayout.draw(paramSunGraphics2D, (float)paramDouble1, (float)paramDouble2);
    }
  }

  public void drawChars(SunGraphics2D paramSunGraphics2D, char[] paramArrayOfChar, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    float f1;
    float f2;
    FontInfo localFontInfo = paramSunGraphics2D.getFontInfo();
    if (localFontInfo.pixelHeight > 100)
    {
      SurfaceData.outlineTextRenderer.drawChars(paramSunGraphics2D, paramArrayOfChar, paramInt1, paramInt2, paramInt3, paramInt4);
      return;
    }
    if (paramSunGraphics2D.transformState >= 3)
    {
      localObject = { paramInt3 + localFontInfo.originX, paramInt4 + localFontInfo.originY };
      paramSunGraphics2D.transform.transform(localObject, 0, localObject, 0, 1);
      f1 = (float)localObject[0];
      f2 = (float)localObject[1];
    }
    else
    {
      f1 = paramInt3 + localFontInfo.originX + paramSunGraphics2D.transX;
      f2 = paramInt4 + localFontInfo.originY + paramSunGraphics2D.transY;
    }
    Object localObject = GlyphList.getInstance();
    if (((GlyphList)localObject).setFromChars(localFontInfo, paramArrayOfChar, paramInt1, paramInt2, f1, f2))
    {
      drawGlyphList(paramSunGraphics2D, (GlyphList)localObject);
      ((GlyphList)localObject).dispose();
    }
    else
    {
      ((GlyphList)localObject).dispose();
      TextLayout localTextLayout = new TextLayout(new String(paramArrayOfChar, paramInt1, paramInt2), paramSunGraphics2D.getFont(), paramSunGraphics2D.getFontRenderContext());
      localTextLayout.draw(paramSunGraphics2D, paramInt3, paramInt4);
    }
  }

  public void drawGlyphVector(SunGraphics2D paramSunGraphics2D, GlyphVector paramGlyphVector, float paramFloat1, float paramFloat2)
  {
    FontRenderContext localFontRenderContext = paramGlyphVector.getFontRenderContext();
    FontInfo localFontInfo = paramSunGraphics2D.getGVFontInfo(paramGlyphVector.getFont(), localFontRenderContext);
    if (localFontInfo.pixelHeight > 100)
    {
      SurfaceData.outlineTextRenderer.drawGlyphVector(paramSunGraphics2D, paramGlyphVector, paramFloat1, paramFloat2);
      return;
    }
    if (paramSunGraphics2D.transformState >= 3)
    {
      localObject = { paramFloat1, paramFloat2 };
      paramSunGraphics2D.transform.transform(localObject, 0, localObject, 0, 1);
      paramFloat1 = (float)localObject[0];
      paramFloat2 = (float)localObject[1];
    }
    else
    {
      paramFloat1 += paramSunGraphics2D.transX;
      paramFloat2 += paramSunGraphics2D.transY;
    }
    Object localObject = GlyphList.getInstance();
    ((GlyphList)localObject).setFromGlyphVector(localFontInfo, paramGlyphVector, paramFloat1, paramFloat2);
    drawGlyphList(paramSunGraphics2D, (GlyphList)localObject, localFontInfo.aaHint);
    ((GlyphList)localObject).dispose();
  }

  protected abstract void drawGlyphList(SunGraphics2D paramSunGraphics2D, GlyphList paramGlyphList);

  protected void drawGlyphList(SunGraphics2D paramSunGraphics2D, GlyphList paramGlyphList, int paramInt)
  {
    drawGlyphList(paramSunGraphics2D, paramGlyphList);
  }
}