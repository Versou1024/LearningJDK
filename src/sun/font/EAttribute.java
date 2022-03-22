package sun.font;

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator.Attribute;

public enum EAttribute
{
  EFAMILY, EWEIGHT, EWIDTH, EPOSTURE, ESIZE, ETRANSFORM, ESUPERSCRIPT, EFONT, ECHAR_REPLACEMENT, EFOREGROUND, EBACKGROUND, EUNDERLINE, ESTRIKETHROUGH, ERUN_DIRECTION, EBIDI_EMBEDDING, EJUSTIFICATION, EINPUT_METHOD_HIGHLIGHT, EINPUT_METHOD_UNDERLINE, ESWAP_COLORS, ENUMERIC_SHAPING, EKERNING, ELIGATURES, ETRACKING, EBASELINE_TRANSFORM;

  final int mask = 1 << ordinal();
  final TextAttribute att;
  static final EAttribute[] atts;

  public static EAttribute forAttribute(AttributedCharacterIterator.Attribute paramAttribute)
  {
    EAttribute[] arrayOfEAttribute = atts;
    int i = arrayOfEAttribute.length;
    for (int j = 0; j < i; ++j)
    {
      EAttribute localEAttribute = arrayOfEAttribute[j];
      if (localEAttribute.att == paramAttribute)
        return localEAttribute;
    }
    return null;
  }

  public String toString()
  {
    return name().substring(1).toLowerCase();
  }

  static
  {
    atts = (EAttribute[])EAttribute.class.getEnumConstants();
  }
}