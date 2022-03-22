package sun.org.mozilla.javascript.internal;

class BeanProperty
{
  MemberBox getter;
  MemberBox setter;
  NativeJavaMethod setters;

  BeanProperty(MemberBox paramMemberBox1, MemberBox paramMemberBox2, NativeJavaMethod paramNativeJavaMethod)
  {
    this.getter = paramMemberBox1;
    this.setter = paramMemberBox2;
    this.setters = paramNativeJavaMethod;
  }
}