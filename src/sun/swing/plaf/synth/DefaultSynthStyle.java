package sun.swing.plaf.synth;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.UIDefaults.LazyValue;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthGraphicsUtils;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;

public class DefaultSynthStyle extends SynthStyle
  implements Cloneable
{
  private static final Object PENDING = new String("Pending");
  private boolean opaque;
  private Insets insets;
  private StateInfo[] states;
  private Map data;
  private Font font;
  private SynthGraphicsUtils synthGraphics;
  private SynthPainter painter;

  public DefaultSynthStyle()
  {
  }

  public DefaultSynthStyle(DefaultSynthStyle paramDefaultSynthStyle)
  {
    this.opaque = paramDefaultSynthStyle.opaque;
    if (paramDefaultSynthStyle.insets != null)
      this.insets = new Insets(paramDefaultSynthStyle.insets.top, paramDefaultSynthStyle.insets.left, paramDefaultSynthStyle.insets.bottom, paramDefaultSynthStyle.insets.right);
    if (paramDefaultSynthStyle.states != null)
    {
      this.states = new StateInfo[paramDefaultSynthStyle.states.length];
      for (int i = paramDefaultSynthStyle.states.length - 1; i >= 0; --i)
        this.states[i] = ((StateInfo)paramDefaultSynthStyle.states[i].clone());
    }
    if (paramDefaultSynthStyle.data != null)
    {
      this.data = new HashMap();
      this.data.putAll(paramDefaultSynthStyle.data);
    }
    this.font = paramDefaultSynthStyle.font;
    this.synthGraphics = paramDefaultSynthStyle.synthGraphics;
    this.painter = paramDefaultSynthStyle.painter;
  }

  public DefaultSynthStyle(Insets paramInsets, boolean paramBoolean, StateInfo[] paramArrayOfStateInfo, Map paramMap)
  {
    this.insets = paramInsets;
    this.opaque = paramBoolean;
    this.states = paramArrayOfStateInfo;
    this.data = paramMap;
  }

  public Color getColor(SynthContext paramSynthContext, ColorType paramColorType)
  {
    return getColor(paramSynthContext.getComponent(), paramSynthContext.getRegion(), paramSynthContext.getComponentState(), paramColorType);
  }

  public Color getColor(JComponent paramJComponent, Region paramRegion, int paramInt, ColorType paramColorType)
  {
    if ((!(paramRegion.isSubregion())) && (paramInt == 1))
    {
      if (paramColorType == ColorType.BACKGROUND)
        return paramJComponent.getBackground();
      if (paramColorType == ColorType.FOREGROUND)
        return paramJComponent.getForeground();
      if (paramColorType == ColorType.TEXT_FOREGROUND)
      {
        localColor = paramJComponent.getForeground();
        if (!(localColor instanceof UIResource))
          return localColor;
      }
    }
    Color localColor = getColorForState(paramJComponent, paramRegion, paramInt, paramColorType);
    if (localColor == null)
    {
      if ((paramColorType == ColorType.BACKGROUND) || (paramColorType == ColorType.TEXT_BACKGROUND))
        return paramJComponent.getBackground();
      if ((paramColorType == ColorType.FOREGROUND) || (paramColorType == ColorType.TEXT_FOREGROUND))
        return paramJComponent.getForeground();
    }
    return localColor;
  }

  protected Color getColorForState(SynthContext paramSynthContext, ColorType paramColorType)
  {
    return getColorForState(paramSynthContext.getComponent(), paramSynthContext.getRegion(), paramSynthContext.getComponentState(), paramColorType);
  }

  protected Color getColorForState(JComponent paramJComponent, Region paramRegion, int paramInt, ColorType paramColorType)
  {
    StateInfo localStateInfo = getStateInfo(paramInt);
    if (localStateInfo != null)
    {
      Color localColor;
      if ((localColor = localStateInfo.getColor(paramColorType)) != null)
        return localColor;
    }
    if ((localStateInfo == null) || (localStateInfo.getComponentState() != 0))
    {
      localStateInfo = getStateInfo(0);
      if (localStateInfo != null)
        return localStateInfo.getColor(paramColorType);
    }
    return null;
  }

  public void setFont(Font paramFont)
  {
    this.font = paramFont;
  }

  public Font getFont(SynthContext paramSynthContext)
  {
    return getFont(paramSynthContext.getComponent(), paramSynthContext.getRegion(), paramSynthContext.getComponentState());
  }

  public Font getFont(JComponent paramJComponent, Region paramRegion, int paramInt)
  {
    if ((!(paramRegion.isSubregion())) && (paramInt == 1))
      return paramJComponent.getFont();
    Font localFont = paramJComponent.getFont();
    if ((localFont != null) && (!(localFont instanceof UIResource)))
      return localFont;
    return getFontForState(paramJComponent, paramRegion, paramInt);
  }

  protected Font getFontForState(JComponent paramJComponent, Region paramRegion, int paramInt)
  {
    Font localFont;
    if (paramJComponent == null)
      return this.font;
    StateInfo localStateInfo = getStateInfo(paramInt);
    if (localStateInfo != null)
      if ((localFont = localStateInfo.getFont()) != null)
        return localFont;
    if ((localStateInfo == null) || (localStateInfo.getComponentState() != 0))
    {
      localStateInfo = getStateInfo(0);
      if (localStateInfo != null)
        if ((localFont = localStateInfo.getFont()) != null)
          return localFont;
    }
    return this.font;
  }

  protected Font getFontForState(SynthContext paramSynthContext)
  {
    return getFontForState(paramSynthContext.getComponent(), paramSynthContext.getRegion(), paramSynthContext.getComponentState());
  }

  public void setGraphicsUtils(SynthGraphicsUtils paramSynthGraphicsUtils)
  {
    this.synthGraphics = paramSynthGraphicsUtils;
  }

  public SynthGraphicsUtils getGraphicsUtils(SynthContext paramSynthContext)
  {
    if (this.synthGraphics == null)
      return super.getGraphicsUtils(paramSynthContext);
    return this.synthGraphics;
  }

  public void setInsets(Insets paramInsets)
  {
    this.insets = paramInsets;
  }

  public Insets getInsets(SynthContext paramSynthContext, Insets paramInsets)
  {
    if (paramInsets == null)
      paramInsets = new Insets(0, 0, 0, 0);
    if (this.insets != null)
    {
      paramInsets.left = this.insets.left;
      paramInsets.right = this.insets.right;
      paramInsets.top = this.insets.top;
      paramInsets.bottom = this.insets.bottom;
    }
    else
    {
      paramInsets.left = (paramInsets.right = paramInsets.top = paramInsets.bottom = 0);
    }
    return paramInsets;
  }

  public void setPainter(SynthPainter paramSynthPainter)
  {
    this.painter = paramSynthPainter;
  }

  public SynthPainter getPainter(SynthContext paramSynthContext)
  {
    return this.painter;
  }

  public void setOpaque(boolean paramBoolean)
  {
    this.opaque = paramBoolean;
  }

  public boolean isOpaque(SynthContext paramSynthContext)
  {
    return this.opaque;
  }

  public void setData(Map paramMap)
  {
    this.data = paramMap;
  }

  public Map getData()
  {
    return this.data;
  }

  public Object get(SynthContext paramSynthContext, Object paramObject)
  {
    StateInfo localStateInfo = getStateInfo(paramSynthContext.getComponentState());
    if ((localStateInfo != null) && (localStateInfo.getData() != null) && (getKeyFromData(localStateInfo.getData(), paramObject) != null))
      return getKeyFromData(localStateInfo.getData(), paramObject);
    localStateInfo = getStateInfo(0);
    if ((localStateInfo != null) && (localStateInfo.getData() != null) && (getKeyFromData(localStateInfo.getData(), paramObject) != null))
      return getKeyFromData(localStateInfo.getData(), paramObject);
    if (getKeyFromData(this.data, paramObject) != null)
      return getKeyFromData(this.data, paramObject);
    return getDefaultValue(paramSynthContext, paramObject);
  }

  private Object getKeyFromData(Map paramMap, Object paramObject)
  {
    Object localObject1 = null;
    if (paramMap != null)
    {
      synchronized (paramMap)
      {
        localObject1 = paramMap.get(paramObject);
      }
      while (localObject1 == PENDING)
        synchronized (paramMap)
        {
          try
          {
            paramMap.wait();
          }
          catch (InterruptedException localInterruptedException)
          {
          }
          localObject1 = paramMap.get(paramObject);
        }
      if (localObject1 instanceof UIDefaults.LazyValue)
      {
        synchronized (paramMap)
        {
          paramMap.put(paramObject, PENDING);
        }
        localObject1 = ((UIDefaults.LazyValue)localObject1).createValue(null);
        synchronized (paramMap)
        {
          paramMap.put(paramObject, localObject1);
          paramMap.notifyAll();
        }
      }
    }
    return localObject1;
  }

  public Object getDefaultValue(SynthContext paramSynthContext, Object paramObject)
  {
    return super.get(paramSynthContext, paramObject);
  }

  public Object clone()
  {
    DefaultSynthStyle localDefaultSynthStyle;
    try
    {
      localDefaultSynthStyle = (DefaultSynthStyle)clone();
    }
    catch (CloneNotSupportedException localCloneNotSupportedException)
    {
      return null;
    }
    if (this.states != null)
    {
      localDefaultSynthStyle.states = new StateInfo[this.states.length];
      for (int i = this.states.length - 1; i >= 0; --i)
        localDefaultSynthStyle.states[i] = ((StateInfo)this.states[i].clone());
    }
    if (this.data != null)
    {
      localDefaultSynthStyle.data = new HashMap();
      localDefaultSynthStyle.data.putAll(this.data);
    }
    return localDefaultSynthStyle;
  }

  public DefaultSynthStyle addTo(DefaultSynthStyle paramDefaultSynthStyle)
  {
    if (this.insets != null)
      paramDefaultSynthStyle.insets = this.insets;
    if (this.font != null)
      paramDefaultSynthStyle.font = this.font;
    if (this.painter != null)
      paramDefaultSynthStyle.painter = this.painter;
    if (this.synthGraphics != null)
      paramDefaultSynthStyle.synthGraphics = this.synthGraphics;
    paramDefaultSynthStyle.opaque = this.opaque;
    if (this.states != null)
    {
      int i;
      if (paramDefaultSynthStyle.states == null)
      {
        paramDefaultSynthStyle.states = new StateInfo[this.states.length];
        for (i = this.states.length - 1; i >= 0; --i)
          if (this.states[i] != null)
            paramDefaultSynthStyle.states[i] = ((StateInfo)this.states[i].clone());
      }
      else
      {
        int i1;
        int i2;
        int i3;
        i = 0;
        int j = 0;
        int k = paramDefaultSynthStyle.states.length;
        for (int l = this.states.length - 1; l >= 0; --l)
        {
          i1 = this.states[l].getComponentState();
          i2 = 0;
          for (i3 = k - 1 - j; i3 >= 0; --i3)
            if (i1 == paramDefaultSynthStyle.states[i3].getComponentState())
            {
              paramDefaultSynthStyle.states[i3] = this.states[l].addTo(paramDefaultSynthStyle.states[i3]);
              StateInfo localStateInfo = paramDefaultSynthStyle.states[(k - 1 - j)];
              paramDefaultSynthStyle.states[(k - 1 - j)] = paramDefaultSynthStyle.states[i3];
              paramDefaultSynthStyle.states[i3] = localStateInfo;
              ++j;
              i2 = 1;
              break;
            }
          if (i2 == 0)
            ++i;
        }
        if (i != 0)
        {
          StateInfo[] arrayOfStateInfo = new StateInfo[i + k];
          i1 = k;
          System.arraycopy(paramDefaultSynthStyle.states, 0, arrayOfStateInfo, 0, k);
          for (i2 = this.states.length - 1; i2 >= 0; --i2)
          {
            i3 = this.states[i2].getComponentState();
            int i4 = 0;
            for (int i5 = k - 1; i5 >= 0; --i5)
              if (i3 == paramDefaultSynthStyle.states[i5].getComponentState())
              {
                i4 = 1;
                break;
              }
            if (i4 == 0)
              arrayOfStateInfo[(i1++)] = ((StateInfo)this.states[i2].clone());
          }
          paramDefaultSynthStyle.states = arrayOfStateInfo;
        }
      }
    }
    if (this.data != null)
    {
      if (paramDefaultSynthStyle.data == null)
        paramDefaultSynthStyle.data = new HashMap();
      paramDefaultSynthStyle.data.putAll(this.data);
    }
    return paramDefaultSynthStyle;
  }

  public void setStateInfo(StateInfo[] paramArrayOfStateInfo)
  {
    this.states = paramArrayOfStateInfo;
  }

  public StateInfo[] getStateInfo()
  {
    return this.states;
  }

  public StateInfo getStateInfo(int paramInt)
  {
    if (this.states != null)
    {
      int i = 0;
      int j = -1;
      int k = -1;
      if (paramInt == 0)
      {
        for (l = this.states.length - 1; l >= 0; --l)
          if (this.states[l].getComponentState() == 0)
            return this.states[l];
        return null;
      }
      for (int l = this.states.length - 1; l >= 0; --l)
      {
        int i1 = this.states[l].getComponentState();
        if (i1 == 0)
        {
          if (k == -1)
            k = l;
        }
        else if ((paramInt & i1) == i1)
        {
          int i2 = i1;
          i2 -= ((0xAAAAAAAA & i2) >>> 1);
          i2 = (i2 & 0x33333333) + (i2 >>> 2 & 0x33333333);
          i2 = i2 + (i2 >>> 4) & 0xF0F0F0F;
          i2 += (i2 >>> 8);
          i2 += (i2 >>> 16);
          i2 &= 255;
          if (i2 > i)
          {
            j = l;
            i = i2;
          }
        }
      }
      if (j != -1)
        return this.states[j];
      if (k != -1)
        return this.states[k];
    }
    return null;
  }

  public String toString()
  {
    StringBuffer localStringBuffer = new StringBuffer();
    localStringBuffer.append(toString()).append(',');
    localStringBuffer.append("data=").append(this.data).append(',');
    localStringBuffer.append("font=").append(this.font).append(',');
    localStringBuffer.append("insets=").append(this.insets).append(',');
    localStringBuffer.append("synthGraphics=").append(this.synthGraphics).append(',');
    localStringBuffer.append("painter=").append(this.painter).append(',');
    StateInfo[] arrayOfStateInfo = getStateInfo();
    if (arrayOfStateInfo != null)
    {
      localStringBuffer.append("states[");
      for (int i = 0; i < arrayOfStateInfo.length; ++i)
        localStringBuffer.append(arrayOfStateInfo[i].toString()).append(',');
      localStringBuffer.append(']').append(',');
    }
    localStringBuffer.deleteCharAt(localStringBuffer.length() - 1);
    return localStringBuffer.toString();
  }

  public static class StateInfo
  {
    private Map data;
    private Font font;
    private Color[] colors;
    private int state;

    public StateInfo()
    {
    }

    public StateInfo(int paramInt, Font paramFont, Color[] paramArrayOfColor)
    {
      this.state = paramInt;
      this.font = paramFont;
      this.colors = paramArrayOfColor;
    }

    public StateInfo(StateInfo paramStateInfo)
    {
      this.state = paramStateInfo.state;
      this.font = paramStateInfo.font;
      if (paramStateInfo.data != null)
      {
        if (this.data == null)
          this.data = new HashMap();
        this.data.putAll(paramStateInfo.data);
      }
      if (paramStateInfo.colors != null)
      {
        this.colors = new Color[paramStateInfo.colors.length];
        System.arraycopy(paramStateInfo.colors, 0, this.colors, 0, paramStateInfo.colors.length);
      }
    }

    public Map getData()
    {
      return this.data;
    }

    public void setData(Map paramMap)
    {
      this.data = paramMap;
    }

    public void setFont(Font paramFont)
    {
      this.font = paramFont;
    }

    public Font getFont()
    {
      return this.font;
    }

    public void setColors(Color[] paramArrayOfColor)
    {
      this.colors = paramArrayOfColor;
    }

    public Color[] getColors()
    {
      return this.colors;
    }

    public Color getColor(ColorType paramColorType)
    {
      if (this.colors != null)
      {
        int i = paramColorType.getID();
        if (i < this.colors.length)
          return this.colors[i];
      }
      return null;
    }

    public StateInfo addTo(StateInfo paramStateInfo)
    {
      if (this.font != null)
        paramStateInfo.font = this.font;
      if (this.data != null)
      {
        if (paramStateInfo.data == null)
          paramStateInfo.data = new HashMap();
        paramStateInfo.data.putAll(this.data);
      }
      if (this.colors != null)
        if (paramStateInfo.colors == null)
        {
          paramStateInfo.colors = new Color[this.colors.length];
          System.arraycopy(this.colors, 0, paramStateInfo.colors, 0, this.colors.length);
        }
        else
        {
          if (paramStateInfo.colors.length < this.colors.length)
          {
            Color[] arrayOfColor = paramStateInfo.colors;
            paramStateInfo.colors = new Color[this.colors.length];
            System.arraycopy(arrayOfColor, 0, paramStateInfo.colors, 0, arrayOfColor.length);
          }
          for (int i = this.colors.length - 1; i >= 0; --i)
            if (this.colors[i] != null)
              paramStateInfo.colors[i] = this.colors[i];
        }
      return paramStateInfo;
    }

    public void setComponentState(int paramInt)
    {
      this.state = paramInt;
    }

    public int getComponentState()
    {
      return this.state;
    }

    private final int getMatchCount(int paramInt)
    {
      paramInt &= this.state;
      paramInt -= ((0xAAAAAAAA & paramInt) >>> 1);
      paramInt = (paramInt & 0x33333333) + (paramInt >>> 2 & 0x33333333);
      paramInt = paramInt + (paramInt >>> 4) & 0xF0F0F0F;
      paramInt += (paramInt >>> 8);
      paramInt += (paramInt >>> 16);
      return (paramInt & 0xFF);
    }

    public Object clone()
    {
      return new StateInfo(this);
    }

    public String toString()
    {
      StringBuffer localStringBuffer = new StringBuffer();
      localStringBuffer.append(super.toString()).append(',');
      localStringBuffer.append("state=").append(Integer.toString(this.state)).append(',');
      localStringBuffer.append("font=").append(this.font).append(',');
      if (this.colors != null)
        localStringBuffer.append("colors=").append(Arrays.asList(this.colors)).append(',');
      return localStringBuffer.toString();
    }
  }
}