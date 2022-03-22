package sun.org.mozilla.javascript.internal;

public class NativeJavaConstructor extends BaseFunction
{
  static final long serialVersionUID = -8149253217482668463L;
  MemberBox ctor;

  public NativeJavaConstructor(MemberBox paramMemberBox)
  {
    this.ctor = paramMemberBox;
  }

  public Object call(Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    return NativeJavaClass.constructSpecific(paramContext, paramScriptable1, paramArrayOfObject, this.ctor);
  }

  public String getFunctionName()
  {
    String str = JavaMembers.liveConnectSignature(this.ctor.argTypes);
    return "<init>".concat(str);
  }

  public String toString()
  {
    return "[JavaConstructor " + this.ctor.getName() + "]";
  }
}