package sun.reflect.generics.parser;

import java.lang.reflect.GenericSignatureFormatError;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import sun.reflect.generics.tree.ArrayTypeSignature;
import sun.reflect.generics.tree.BaseType;
import sun.reflect.generics.tree.BooleanSignature;
import sun.reflect.generics.tree.BottomSignature;
import sun.reflect.generics.tree.ByteSignature;
import sun.reflect.generics.tree.CharSignature;
import sun.reflect.generics.tree.ClassSignature;
import sun.reflect.generics.tree.ClassTypeSignature;
import sun.reflect.generics.tree.DoubleSignature;
import sun.reflect.generics.tree.FieldTypeSignature;
import sun.reflect.generics.tree.FloatSignature;
import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.IntSignature;
import sun.reflect.generics.tree.LongSignature;
import sun.reflect.generics.tree.MethodTypeSignature;
import sun.reflect.generics.tree.ReturnType;
import sun.reflect.generics.tree.ShortSignature;
import sun.reflect.generics.tree.SimpleClassTypeSignature;
import sun.reflect.generics.tree.TypeArgument;
import sun.reflect.generics.tree.TypeSignature;
import sun.reflect.generics.tree.TypeVariableSignature;
import sun.reflect.generics.tree.VoidDescriptor;
import sun.reflect.generics.tree.Wildcard;

public class SignatureParser
{
  private char[] input;
  private int index = 0;
  private static final char EOI = 58;
  private static final boolean DEBUG = 0;

  private char getNext()
  {
    if ((!($assertionsDisabled)) && (this.index > this.input.length))
      throw new AssertionError();
    try
    {
      return this.input[(this.index++)];
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
    }
    return ':';
  }

  private char current()
  {
    if ((!($assertionsDisabled)) && (this.index > this.input.length))
      throw new AssertionError();
    try
    {
      return this.input[this.index];
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
    }
    return ':';
  }

  private void advance()
  {
    if ((!($assertionsDisabled)) && (this.index > this.input.length))
      throw new AssertionError();
    this.index += 1;
  }

  private boolean matches(char paramChar, char[] paramArrayOfChar)
  {
    char[] arrayOfChar = paramArrayOfChar;
    int i = arrayOfChar.length;
    for (int j = 0; j < i; ++j)
    {
      char c = arrayOfChar[j];
      if (paramChar == c)
        return true;
    }
    return false;
  }

  private Error error(String paramString)
  {
    return new GenericSignatureFormatError();
  }

  public static SignatureParser make()
  {
    return new SignatureParser();
  }

  public ClassSignature parseClassSig(String paramString)
  {
    this.input = paramString.toCharArray();
    return parseClassSignature();
  }

  public MethodTypeSignature parseMethodSig(String paramString)
  {
    this.input = paramString.toCharArray();
    return parseMethodTypeSignature();
  }

  public TypeSignature parseTypeSig(String paramString)
  {
    this.input = paramString.toCharArray();
    return parseTypeSignature();
  }

  private ClassSignature parseClassSignature()
  {
    if ((!($assertionsDisabled)) && (this.index != 0))
      throw new AssertionError();
    return ClassSignature.make(parseZeroOrMoreFormalTypeParameters(), parseClassTypeSignature(), parseSuperInterfaces());
  }

  private FormalTypeParameter[] parseZeroOrMoreFormalTypeParameters()
  {
    if (current() == '<')
      return parseFormalTypeParameters();
    return new FormalTypeParameter[0];
  }

  private FormalTypeParameter[] parseFormalTypeParameters()
  {
    ArrayList localArrayList = new ArrayList(3);
    if ((!($assertionsDisabled)) && (current() != '<'))
      throw new AssertionError();
    if (current() != '<')
      throw error("expected <");
    advance();
    localArrayList.add(parseFormalTypeParameter());
    while (current() != '>')
      localArrayList.add(parseFormalTypeParameter());
    advance();
    FormalTypeParameter[] arrayOfFormalTypeParameter = new FormalTypeParameter[localArrayList.size()];
    return ((FormalTypeParameter[])localArrayList.toArray(arrayOfFormalTypeParameter));
  }

  private FormalTypeParameter parseFormalTypeParameter()
  {
    String str = parseIdentifier();
    FieldTypeSignature[] arrayOfFieldTypeSignature = parseZeroOrMoreBounds();
    return FormalTypeParameter.make(str, arrayOfFieldTypeSignature);
  }

  private String parseIdentifier()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    while (!(Character.isWhitespace(current())))
    {
      char c = current();
      switch (c)
      {
      case '.':
      case '/':
      case ':':
      case ';':
      case '<':
      case '>':
      case '[':
        return localStringBuilder.toString();
      }
      localStringBuilder.append(c);
      advance();
    }
    return localStringBuilder.toString();
  }

  private FieldTypeSignature parseFieldTypeSignature()
  {
    switch (current())
    {
    case 'L':
      return parseClassTypeSignature();
    case 'T':
      return parseTypeVariableSignature();
    case '[':
      return parseArrayTypeSignature();
    }
    throw error("Expected Field Type Signature");
  }

  private ClassTypeSignature parseClassTypeSignature()
  {
    if ((!($assertionsDisabled)) && (current() != 'L'))
      throw new AssertionError();
    if (current() != 'L')
      throw error("expected a class type");
    advance();
    ArrayList localArrayList = new ArrayList(5);
    localArrayList.add(parseSimpleClassTypeSignature(false));
    parseClassTypeSignatureSuffix(localArrayList);
    if (current() != ';')
      throw error("expected ';' got '" + current() + "'");
    advance();
    return ClassTypeSignature.make(localArrayList);
  }

  private SimpleClassTypeSignature parseSimpleClassTypeSignature(boolean paramBoolean)
  {
    String str = parseIdentifier();
    int i = current();
    switch (i)
    {
    case 47:
    case 59:
      return SimpleClassTypeSignature.make(str, paramBoolean, new TypeArgument[0]);
    case 60:
      return SimpleClassTypeSignature.make(str, paramBoolean, parseTypeArguments());
    }
    throw error("expected < or ; or /");
  }

  private void parseClassTypeSignatureSuffix(List<SimpleClassTypeSignature> paramList)
  {
    while ((current() == '/') || (current() == '.'))
    {
      boolean bool = current() == '.';
      advance();
      paramList.add(parseSimpleClassTypeSignature(bool));
    }
  }

  private TypeArgument[] parseTypeArgumentsOpt()
  {
    if (current() == '<')
      return parseTypeArguments();
    return new TypeArgument[0];
  }

  private TypeArgument[] parseTypeArguments()
  {
    ArrayList localArrayList = new ArrayList(3);
    if ((!($assertionsDisabled)) && (current() != '<'))
      throw new AssertionError();
    if (current() != '<')
      throw error("expected <");
    advance();
    localArrayList.add(parseTypeArgument());
    while (current() != '>')
      localArrayList.add(parseTypeArgument());
    advance();
    TypeArgument[] arrayOfTypeArgument = new TypeArgument[localArrayList.size()];
    return ((TypeArgument[])localArrayList.toArray(arrayOfTypeArgument));
  }

  private TypeArgument parseTypeArgument()
  {
    FieldTypeSignature[] arrayOfFieldTypeSignature1 = new FieldTypeSignature[1];
    FieldTypeSignature[] arrayOfFieldTypeSignature2 = new FieldTypeSignature[1];
    TypeArgument[] arrayOfTypeArgument = new TypeArgument[0];
    int i = current();
    switch (i)
    {
    case 43:
      advance();
      arrayOfFieldTypeSignature1[0] = parseFieldTypeSignature();
      arrayOfFieldTypeSignature2[0] = BottomSignature.make();
      return Wildcard.make(arrayOfFieldTypeSignature1, arrayOfFieldTypeSignature2);
    case 42:
      advance();
      arrayOfFieldTypeSignature1[0] = SimpleClassTypeSignature.make("java.lang.Object", false, arrayOfTypeArgument);
      arrayOfFieldTypeSignature2[0] = BottomSignature.make();
      return Wildcard.make(arrayOfFieldTypeSignature1, arrayOfFieldTypeSignature2);
    case 45:
      advance();
      arrayOfFieldTypeSignature2[0] = parseFieldTypeSignature();
      arrayOfFieldTypeSignature1[0] = SimpleClassTypeSignature.make("java.lang.Object", false, arrayOfTypeArgument);
      return Wildcard.make(arrayOfFieldTypeSignature1, arrayOfFieldTypeSignature2);
    case 44:
    }
    return parseFieldTypeSignature();
  }

  private TypeVariableSignature parseTypeVariableSignature()
  {
    if ((!($assertionsDisabled)) && (current() != 'T'))
      throw new AssertionError();
    if (current() != 'T')
      throw error("expected a type variable usage");
    advance();
    TypeVariableSignature localTypeVariableSignature = TypeVariableSignature.make(parseIdentifier());
    if (current() != ';')
      throw error("; expected in signature of type variable named" + localTypeVariableSignature.getIdentifier());
    advance();
    return localTypeVariableSignature;
  }

  private ArrayTypeSignature parseArrayTypeSignature()
  {
    if (current() != '[')
      throw error("expected array type signature");
    advance();
    return ArrayTypeSignature.make(parseTypeSignature());
  }

  private TypeSignature parseTypeSignature()
  {
    switch (current())
    {
    case 'B':
    case 'C':
    case 'D':
    case 'F':
    case 'I':
    case 'J':
    case 'S':
    case 'Z':
      return parseBaseType();
    case 'E':
    case 'G':
    case 'H':
    case 'K':
    case 'L':
    case 'M':
    case 'N':
    case 'O':
    case 'P':
    case 'Q':
    case 'R':
    case 'T':
    case 'U':
    case 'V':
    case 'W':
    case 'X':
    case 'Y':
    }
    return parseFieldTypeSignature();
  }

  private BaseType parseBaseType()
  {
    switch (current())
    {
    case 'B':
      advance();
      return ByteSignature.make();
    case 'C':
      advance();
      return CharSignature.make();
    case 'D':
      advance();
      return DoubleSignature.make();
    case 'F':
      advance();
      return FloatSignature.make();
    case 'I':
      advance();
      return IntSignature.make();
    case 'J':
      advance();
      return LongSignature.make();
    case 'S':
      advance();
      return ShortSignature.make();
    case 'Z':
      advance();
      return BooleanSignature.make();
    case 'E':
    case 'G':
    case 'H':
    case 'K':
    case 'L':
    case 'M':
    case 'N':
    case 'O':
    case 'P':
    case 'Q':
    case 'R':
    case 'T':
    case 'U':
    case 'V':
    case 'W':
    case 'X':
    case 'Y':
    }
    if (!($assertionsDisabled))
      throw new AssertionError();
    throw error("expected primitive type");
  }

  private FieldTypeSignature[] parseZeroOrMoreBounds()
  {
    ArrayList localArrayList = new ArrayList(3);
    if (current() == ':')
    {
      advance();
      switch (current())
      {
      case ':':
        break;
      default:
        localArrayList.add(parseFieldTypeSignature());
      }
      while (current() == ':')
      {
        advance();
        localArrayList.add(parseFieldTypeSignature());
      }
    }
    FieldTypeSignature[] arrayOfFieldTypeSignature = new FieldTypeSignature[localArrayList.size()];
    return ((FieldTypeSignature[])localArrayList.toArray(arrayOfFieldTypeSignature));
  }

  private ClassTypeSignature[] parseSuperInterfaces()
  {
    ArrayList localArrayList = new ArrayList(5);
    while (current() == 'L')
      localArrayList.add(parseClassTypeSignature());
    ClassTypeSignature[] arrayOfClassTypeSignature = new ClassTypeSignature[localArrayList.size()];
    return ((ClassTypeSignature[])localArrayList.toArray(arrayOfClassTypeSignature));
  }

  private MethodTypeSignature parseMethodTypeSignature()
  {
    if ((!($assertionsDisabled)) && (this.index != 0))
      throw new AssertionError();
    return MethodTypeSignature.make(parseZeroOrMoreFormalTypeParameters(), parseFormalParameters(), parseReturnType(), parseZeroOrMoreThrowsSignatures());
  }

  private TypeSignature[] parseFormalParameters()
  {
    if (current() != '(')
      throw error("expected (");
    advance();
    TypeSignature[] arrayOfTypeSignature = parseZeroOrMoreTypeSignatures();
    if (current() != ')')
      throw error("expected )");
    advance();
    return arrayOfTypeSignature;
  }

  private TypeSignature[] parseZeroOrMoreTypeSignatures()
  {
    ArrayList localArrayList = new ArrayList();
    int i = 0;
    while (true)
    {
      while (true)
      {
        if (i != 0)
          break label155;
        switch (current())
        {
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'L':
        case 'S':
        case 'T':
        case 'Z':
        case '[':
          localArrayList.add(parseTypeSignature());
        case 'E':
        case 'G':
        case 'H':
        case 'K':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        }
      }
      i = 1;
    }
    label155: TypeSignature[] arrayOfTypeSignature = new TypeSignature[localArrayList.size()];
    return ((TypeSignature[])localArrayList.toArray(arrayOfTypeSignature));
  }

  private ReturnType parseReturnType()
  {
    if (current() == 'V')
    {
      advance();
      return VoidDescriptor.make();
    }
    return parseTypeSignature();
  }

  private FieldTypeSignature[] parseZeroOrMoreThrowsSignatures()
  {
    ArrayList localArrayList = new ArrayList(3);
    while (current() == '^')
      localArrayList.add(parseThrowsSignature());
    FieldTypeSignature[] arrayOfFieldTypeSignature = new FieldTypeSignature[localArrayList.size()];
    return ((FieldTypeSignature[])localArrayList.toArray(arrayOfFieldTypeSignature));
  }

  private FieldTypeSignature parseThrowsSignature()
  {
    if ((!($assertionsDisabled)) && (current() != '^'))
      throw new AssertionError();
    if (current() != '^')
      throw error("expected throws signature");
    advance();
    return parseFieldTypeSignature();
  }
}