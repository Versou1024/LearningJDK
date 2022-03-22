package sun.awt.windows;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.InputMethodEvent;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodHighlight;
import java.awt.im.InputSubset;
import java.awt.im.spi.InputMethodContext;
import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;
import java.text.Annotation;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import sun.awt.im.InputMethodAdapter;

public class WInputMethod extends InputMethodAdapter
{
  private InputMethodContext inputContext;
  private Component awtFocussedComponent;
  private WComponentPeer awtFocussedComponentPeer = null;
  private WComponentPeer lastFocussedComponentPeer = null;
  private boolean isLastFocussedActiveClient = false;
  private boolean isActive;
  private int context = createNativeContext();
  private boolean open = getOpenStatus(this.context);
  private int cmode = getConversionStatus(this.context);
  private Locale currentLocale = getNativeLocale();
  private boolean statusWindowHidden = false;
  public static final byte ATTR_INPUT = 0;
  public static final byte ATTR_TARGET_CONVERTED = 1;
  public static final byte ATTR_CONVERTED = 2;
  public static final byte ATTR_TARGET_NOTCONVERTED = 3;
  public static final byte ATTR_INPUT_ERROR = 4;
  public static final int IME_CMODE_ALPHANUMERIC = 0;
  public static final int IME_CMODE_NATIVE = 1;
  public static final int IME_CMODE_KATAKANA = 2;
  public static final int IME_CMODE_LANGUAGE = 3;
  public static final int IME_CMODE_FULLSHAPE = 8;
  public static final int IME_CMODE_HANJACONVERT = 64;
  public static final int IME_CMODE_ROMAN = 16;
  private static final boolean COMMIT_INPUT = 1;
  private static final boolean DISCARD_INPUT = 0;
  private static Map[] highlightStyles;

  public WInputMethod()
  {
    if (this.currentLocale == null)
      this.currentLocale = Locale.getDefault();
  }

  protected void finalize()
    throws Throwable
  {
    if (this.context != 0)
    {
      destroyNativeContext(this.context);
      this.context = 0;
    }
    finalize();
  }

  public synchronized void setInputMethodContext(InputMethodContext paramInputMethodContext)
  {
    this.inputContext = paramInputMethodContext;
  }

  public final void dispose()
  {
  }

  public Object getControlObject()
  {
    return null;
  }

  public boolean setLocale(Locale paramLocale)
  {
    return setLocale(paramLocale, false);
  }

  private boolean setLocale(Locale paramLocale, boolean paramBoolean)
  {
    Locale[] arrayOfLocale = WInputMethodDescriptor.getAvailableLocalesInternal();
    for (int i = 0; i < arrayOfLocale.length; ++i)
    {
      Locale localLocale = arrayOfLocale[i];
      if ((paramLocale.equals(localLocale)) || ((localLocale.equals(Locale.JAPAN)) && (paramLocale.equals(Locale.JAPANESE))) || ((localLocale.equals(Locale.KOREA)) && (paramLocale.equals(Locale.KOREAN))))
      {
        if (this.isActive)
          setNativeLocale(localLocale.toString(), paramBoolean);
        this.currentLocale = localLocale;
        return true;
      }
    }
    return false;
  }

  public Locale getLocale()
  {
    if (this.isActive)
    {
      this.currentLocale = getNativeLocale();
      if (this.currentLocale == null)
        this.currentLocale = Locale.getDefault();
    }
    return this.currentLocale;
  }

  public void setCharacterSubsets(Character.Subset[] paramArrayOfSubset)
  {
    int i;
    if (paramArrayOfSubset == null)
    {
      setConversionStatus(this.context, this.cmode);
      setOpenStatus(this.context, this.open);
      return;
    }
    Character.Subset localSubset = paramArrayOfSubset[0];
    Locale localLocale = getNativeLocale();
    if (localLocale == null)
      return;
    if (localLocale.getLanguage().equals(Locale.JAPANESE.getLanguage()))
      if ((localSubset == Character.UnicodeBlock.BASIC_LATIN) || (localSubset == InputSubset.LATIN_DIGITS))
      {
        setOpenStatus(this.context, false);
      }
      else
      {
        if ((localSubset == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) || (localSubset == InputSubset.KANJI) || (localSubset == Character.UnicodeBlock.HIRAGANA))
          i = 9;
        else if (localSubset == Character.UnicodeBlock.KATAKANA)
          i = 11;
        else if (localSubset == InputSubset.HALFWIDTH_KATAKANA)
          i = 3;
        else if (localSubset == InputSubset.FULLWIDTH_LATIN)
          i = 8;
        else
          return;
        setOpenStatus(this.context, true);
        i |= getConversionStatus(this.context) & 0x10;
        setConversionStatus(this.context, i);
      }
    else if (localLocale.getLanguage().equals(Locale.KOREAN.getLanguage()))
      if ((localSubset == Character.UnicodeBlock.BASIC_LATIN) || (localSubset == InputSubset.LATIN_DIGITS))
      {
        setOpenStatus(this.context, false);
      }
      else
      {
        if ((localSubset == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) || (localSubset == InputSubset.HANJA) || (localSubset == Character.UnicodeBlock.HANGUL_SYLLABLES) || (localSubset == Character.UnicodeBlock.HANGUL_JAMO) || (localSubset == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO))
          i = 1;
        else if (localSubset == InputSubset.FULLWIDTH_LATIN)
          i = 8;
        else
          return;
        setOpenStatus(this.context, true);
        setConversionStatus(this.context, i);
      }
    else if (localLocale.getLanguage().equals(Locale.CHINESE.getLanguage()))
      if ((localSubset == Character.UnicodeBlock.BASIC_LATIN) || (localSubset == InputSubset.LATIN_DIGITS))
      {
        setOpenStatus(this.context, false);
      }
      else
      {
        if ((localSubset == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) || (localSubset == InputSubset.TRADITIONAL_HANZI) || (localSubset == InputSubset.SIMPLIFIED_HANZI))
          i = 1;
        else if (localSubset == InputSubset.FULLWIDTH_LATIN)
          i = 8;
        else
          return;
        setOpenStatus(this.context, true);
        setConversionStatus(this.context, i);
      }
  }

  public void dispatchEvent(AWTEvent paramAWTEvent)
  {
    if (paramAWTEvent instanceof ComponentEvent)
    {
      Component localComponent = ((ComponentEvent)paramAWTEvent).getComponent();
      if (localComponent == this.awtFocussedComponent)
      {
        if ((this.awtFocussedComponentPeer == null) || (this.awtFocussedComponentPeer.isDisposed()))
          this.awtFocussedComponentPeer = getNearestNativePeer(localComponent);
        if (this.awtFocussedComponentPeer != null)
          handleNativeIMEEvent(this.awtFocussedComponentPeer, paramAWTEvent);
      }
    }
  }

  public void activate()
  {
    boolean bool = haveActiveClient();
    if ((this.lastFocussedComponentPeer != this.awtFocussedComponentPeer) || (this.isLastFocussedActiveClient != bool))
    {
      if (this.lastFocussedComponentPeer != null)
        disableNativeIME(this.lastFocussedComponentPeer);
      if (this.awtFocussedComponentPeer != null)
        enableNativeIME(this.awtFocussedComponentPeer, this.context, !(bool));
      this.lastFocussedComponentPeer = this.awtFocussedComponentPeer;
      this.isLastFocussedActiveClient = bool;
    }
    this.isActive = true;
    if (this.currentLocale != null)
      setLocale(this.currentLocale, true);
    if (this.statusWindowHidden)
    {
      setStatusWindowVisible(this.awtFocussedComponentPeer, true);
      this.statusWindowHidden = false;
    }
  }

  public void deactivate(boolean paramBoolean)
  {
    getLocale();
    if (this.awtFocussedComponentPeer != null)
    {
      this.lastFocussedComponentPeer = this.awtFocussedComponentPeer;
      this.isLastFocussedActiveClient = haveActiveClient();
    }
    this.isActive = false;
  }

  public void disableInputMethod()
  {
    if (this.lastFocussedComponentPeer != null)
    {
      disableNativeIME(this.lastFocussedComponentPeer);
      this.lastFocussedComponentPeer = null;
      this.isLastFocussedActiveClient = false;
    }
  }

  public String getNativeInputMethodInfo()
  {
    return getNativeIMMDescription();
  }

  protected void stopListening()
  {
    disableInputMethod();
  }

  protected void setAWTFocussedComponent(Component paramComponent)
  {
    if (paramComponent == null)
      return;
    WComponentPeer localWComponentPeer = getNearestNativePeer(paramComponent);
    if (this.isActive)
    {
      if (this.awtFocussedComponentPeer != null)
        disableNativeIME(this.awtFocussedComponentPeer);
      if (localWComponentPeer != null)
        enableNativeIME(localWComponentPeer, this.context, !(haveActiveClient()));
    }
    this.awtFocussedComponent = paramComponent;
    this.awtFocussedComponentPeer = localWComponentPeer;
  }

  public void hideWindows()
  {
    if (this.awtFocussedComponentPeer != null)
    {
      setStatusWindowVisible(this.awtFocussedComponentPeer, false);
      this.statusWindowHidden = true;
    }
  }

  public void removeNotify()
  {
    endCompositionNative(this.context, false);
    this.awtFocussedComponent = null;
    this.awtFocussedComponentPeer = null;
  }

  static Map mapInputMethodHighlight(InputMethodHighlight paramInputMethodHighlight)
  {
    int i;
    int j = paramInputMethodHighlight.getState();
    if (j == 0)
      i = 0;
    else if (j == 1)
      i = 2;
    else
      return null;
    if (paramInputMethodHighlight.isSelected());
    return highlightStyles[(++i)];
  }

  protected boolean supportsBelowTheSpot()
  {
    return true;
  }

  public void endComposition()
  {
    endCompositionNative(this.context, haveActiveClient());
  }

  public void setCompositionEnabled(boolean paramBoolean)
  {
    setOpenStatus(this.context, paramBoolean);
  }

  public boolean isCompositionEnabled()
  {
    return getOpenStatus(this.context);
  }

  public void sendInputMethodEvent(int paramInt1, long paramLong, String paramString, int[] paramArrayOfInt1, String[] paramArrayOfString, int[] paramArrayOfInt2, byte[] paramArrayOfByte, int paramInt2, int paramInt3, int paramInt4)
  {
    AttributedCharacterIterator localAttributedCharacterIterator = null;
    if (paramString != null)
    {
      int i;
      localObject = new AttributedString(paramString);
      ((AttributedString)localObject).addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, Locale.getDefault(), 0, paramString.length());
      if ((paramArrayOfInt1 != null) && (paramArrayOfString != null) && (paramArrayOfString.length != 0) && (paramArrayOfInt1.length == paramArrayOfString.length + 1) && (paramArrayOfInt1[0] == 0) && (paramArrayOfInt1[paramArrayOfString.length] == paramString.length()))
      {
        for (i = 0; i < paramArrayOfInt1.length - 1; ++i)
        {
          ((AttributedString)localObject).addAttribute(AttributedCharacterIterator.Attribute.INPUT_METHOD_SEGMENT, new Annotation(null), paramArrayOfInt1[i], paramArrayOfInt1[(i + 1)]);
          ((AttributedString)localObject).addAttribute(AttributedCharacterIterator.Attribute.READING, new Annotation(paramArrayOfString[i]), paramArrayOfInt1[i], paramArrayOfInt1[(i + 1)]);
        }
      }
      else
      {
        ((AttributedString)localObject).addAttribute(AttributedCharacterIterator.Attribute.INPUT_METHOD_SEGMENT, new Annotation(null), 0, paramString.length());
        ((AttributedString)localObject).addAttribute(AttributedCharacterIterator.Attribute.READING, new Annotation(new String("")), 0, paramString.length());
      }
      if ((paramArrayOfInt2 != null) && (paramArrayOfByte != null) && (paramArrayOfByte.length != 0) && (paramArrayOfInt2.length == paramArrayOfByte.length + 1) && (paramArrayOfInt2[0] == 0) && (paramArrayOfInt2[paramArrayOfByte.length] == paramString.length()))
        for (i = 0; i < paramArrayOfInt2.length - 1; ++i)
        {
          InputMethodHighlight localInputMethodHighlight;
          switch (paramArrayOfByte[i])
          {
          case 0:
          case 4:
          default:
            localInputMethodHighlight = InputMethodHighlight.UNSELECTED_RAW_TEXT_HIGHLIGHT;
            break;
          case 1:
            localInputMethodHighlight = InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT;
            break;
          case 2:
            localInputMethodHighlight = InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT;
            break;
          case 3:
            localInputMethodHighlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
          }
          ((AttributedString)localObject).addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT, localInputMethodHighlight, paramArrayOfInt2[i], paramArrayOfInt2[(i + 1)]);
        }
      else
        ((AttributedString)localObject).addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT, InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT, 0, paramString.length());
      localAttributedCharacterIterator = ((AttributedString)localObject).getIterator();
    }
    Object localObject = getClientComponent();
    if (localObject == null)
      return;
    InputMethodEvent localInputMethodEvent = new InputMethodEvent((Component)localObject, paramInt1, paramLong, localAttributedCharacterIterator, paramInt2, TextHitInfo.leading(paramInt3), TextHitInfo.leading(paramInt4));
    WToolkit.postEvent(WToolkit.targetToAppContext(localObject), localInputMethodEvent);
  }

  public void inquireCandidatePosition()
  {
    EventQueue.invokeLater(new Runnable(this)
    {
      public void run()
      {
        int i = 0;
        int j = 0;
        Component localComponent = WInputMethod.access$000(this.this$0);
        if (localComponent != null)
        {
          Object localObject;
          if (WInputMethod.access$100(this.this$0))
          {
            localObject = WInputMethod.access$200(this.this$0).getTextLocation(TextHitInfo.leading(0));
            i = ((Rectangle)localObject).x;
            j = ((Rectangle)localObject).y + ((Rectangle)localObject).height;
          }
          else
          {
            localObject = localComponent.getLocationOnScreen();
            Dimension localDimension = localComponent.getSize();
            i = ((Point)localObject).x;
            j = ((Point)localObject).y + localDimension.height;
          }
        }
        WInputMethod.access$400(this.this$0, WInputMethod.access$300(this.this$0), i, j);
      }
    });
  }

  private WComponentPeer getNearestNativePeer(Component paramComponent)
  {
    if (paramComponent == null)
      return null;
    ComponentPeer localComponentPeer = paramComponent.getPeer();
    if (localComponentPeer == null)
      return null;
    do
    {
      if (!(localComponentPeer instanceof LightweightPeer))
        break label46;
      paramComponent = paramComponent.getParent();
      if (paramComponent == null)
        return null;
      localComponentPeer = paramComponent.getPeer();
    }
    while (localComponentPeer != null);
    return null;
    if (localComponentPeer instanceof WComponentPeer)
      label46: return ((WComponentPeer)localComponentPeer);
    return null;
  }

  private native int createNativeContext();

  private native void destroyNativeContext(int paramInt);

  private native void enableNativeIME(WComponentPeer paramWComponentPeer, int paramInt, boolean paramBoolean);

  private native void disableNativeIME(WComponentPeer paramWComponentPeer);

  private native void handleNativeIMEEvent(WComponentPeer paramWComponentPeer, AWTEvent paramAWTEvent);

  private native void endCompositionNative(int paramInt, boolean paramBoolean);

  private native void setConversionStatus(int paramInt1, int paramInt2);

  private native int getConversionStatus(int paramInt);

  private native void setOpenStatus(int paramInt, boolean paramBoolean);

  private native boolean getOpenStatus(int paramInt);

  private native void setStatusWindowVisible(WComponentPeer paramWComponentPeer, boolean paramBoolean);

  private native String getNativeIMMDescription();

  static native Locale getNativeLocale();

  static native boolean setNativeLocale(String paramString, boolean paramBoolean);

  private native void openCandidateWindow(WComponentPeer paramWComponentPeer, int paramInt1, int paramInt2);

  static
  {
    Map[] arrayOfMap = new Map[4];
    HashMap localHashMap = new HashMap(1);
    localHashMap.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_DOTTED);
    arrayOfMap[0] = Collections.unmodifiableMap(localHashMap);
    localHashMap = new HashMap(1);
    localHashMap.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_GRAY);
    arrayOfMap[1] = Collections.unmodifiableMap(localHashMap);
    localHashMap = new HashMap(1);
    localHashMap.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_DOTTED);
    arrayOfMap[2] = Collections.unmodifiableMap(localHashMap);
    localHashMap = new HashMap(4);
    Color localColor = new Color(0, 0, 128);
    localHashMap.put(TextAttribute.FOREGROUND, localColor);
    localHashMap.put(TextAttribute.BACKGROUND, Color.white);
    localHashMap.put(TextAttribute.SWAP_COLORS, TextAttribute.SWAP_COLORS_ON);
    localHashMap.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
    arrayOfMap[3] = Collections.unmodifiableMap(localHashMap);
    highlightStyles = arrayOfMap;
  }
}