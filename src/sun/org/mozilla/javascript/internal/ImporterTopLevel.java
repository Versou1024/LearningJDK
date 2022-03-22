package sun.org.mozilla.javascript.internal;

public class ImporterTopLevel extends IdScriptableObject
{
  static final long serialVersionUID = -9095380847465315412L;
  private static final Object IMPORTER_TAG = new Object();
  private static final int Id_constructor = 1;
  private static final int Id_importClass = 2;
  private static final int Id_importPackage = 3;
  private static final int MAX_PROTOTYPE_ID = 3;
  private ObjArray importedPackages;
  private boolean topScopeFlag;

  public ImporterTopLevel()
  {
    this.importedPackages = new ObjArray();
  }

  public ImporterTopLevel(Context paramContext)
  {
    this(paramContext, false);
  }

  public ImporterTopLevel(Context paramContext, boolean paramBoolean)
  {
    this.importedPackages = new ObjArray();
    initStandardObjects(paramContext, paramBoolean);
  }

  public String getClassName()
  {
    return ((this.topScopeFlag) ? "global" : "JavaImporter");
  }

  public static void init(Context paramContext, Scriptable paramScriptable, boolean paramBoolean)
  {
    ImporterTopLevel localImporterTopLevel = new ImporterTopLevel();
    localImporterTopLevel.exportAsJSClass(3, paramScriptable, paramBoolean);
  }

  public void initStandardObjects(Context paramContext, boolean paramBoolean)
  {
    paramContext.initStandardObjects(this, paramBoolean);
    this.topScopeFlag = true;
    IdFunctionObject localIdFunctionObject = exportAsJSClass(3, this, false);
    if (paramBoolean)
      localIdFunctionObject.sealObject();
    delete("constructor");
  }

  public boolean has(String paramString, Scriptable paramScriptable)
  {
    return ((super.has(paramString, paramScriptable)) || (getPackageProperty(paramString, paramScriptable) != NOT_FOUND));
  }

  public Object get(String paramString, Scriptable paramScriptable)
  {
    Object localObject = super.get(paramString, paramScriptable);
    if (localObject != NOT_FOUND)
      return localObject;
    localObject = getPackageProperty(paramString, paramScriptable);
    return localObject;
  }

  private Object getPackageProperty(String paramString, Scriptable paramScriptable)
  {
    Object[] arrayOfObject;
    Object localObject1 = NOT_FOUND;
    synchronized (this.importedPackages)
    {
      arrayOfObject = this.importedPackages.toArray();
    }
    for (int i = 0; i < arrayOfObject.length; ++i)
    {
      NativeJavaPackage localNativeJavaPackage = (NativeJavaPackage)arrayOfObject[i];
      Object localObject3 = localNativeJavaPackage.getPkgProperty(paramString, paramScriptable, false);
      if ((localObject3 != null) && (!(localObject3 instanceof NativeJavaPackage)))
        if (localObject1 == NOT_FOUND)
          localObject1 = localObject3;
        else
          throw Context.reportRuntimeError2("msg.ambig.import", localObject1.toString(), localObject3.toString());
    }
    return localObject1;
  }

  /**
   * @deprecated
   */
  public void importPackage(Context paramContext, Scriptable paramScriptable, Object[] paramArrayOfObject, Function paramFunction)
  {
    js_importPackage(paramArrayOfObject);
  }

  private Object js_construct(Scriptable paramScriptable, Object[] paramArrayOfObject)
  {
    ImporterTopLevel localImporterTopLevel = new ImporterTopLevel();
    for (int i = 0; i != paramArrayOfObject.length; ++i)
    {
      Object localObject = paramArrayOfObject[i];
      if (localObject instanceof NativeJavaClass)
        localImporterTopLevel.importClass((NativeJavaClass)localObject);
      else if (localObject instanceof NativeJavaPackage)
        localImporterTopLevel.importPackage((NativeJavaPackage)localObject);
      else
        throw Context.reportRuntimeError1("msg.not.class.not.pkg", Context.toString(localObject));
    }
    localImporterTopLevel.setParentScope(paramScriptable);
    localImporterTopLevel.setPrototype(this);
    return localImporterTopLevel;
  }

  private Object js_importClass(Object[] paramArrayOfObject)
  {
    for (int i = 0; i != paramArrayOfObject.length; ++i)
    {
      Object localObject = paramArrayOfObject[i];
      if (!(localObject instanceof NativeJavaClass))
        throw Context.reportRuntimeError1("msg.not.class", Context.toString(localObject));
      importClass((NativeJavaClass)localObject);
    }
    return Undefined.instance;
  }

  private Object js_importPackage(Object[] paramArrayOfObject)
  {
    for (int i = 0; i != paramArrayOfObject.length; ++i)
    {
      Object localObject = paramArrayOfObject[i];
      if (!(localObject instanceof NativeJavaPackage))
        throw Context.reportRuntimeError1("msg.not.pkg", Context.toString(localObject));
      importPackage((NativeJavaPackage)localObject);
    }
    return Undefined.instance;
  }

  private void importPackage(NativeJavaPackage paramNativeJavaPackage)
  {
    synchronized (this.importedPackages)
    {
      for (int i = 0; i != this.importedPackages.size(); ++i)
        if (paramNativeJavaPackage == this.importedPackages.get(i))
        {
          paramNativeJavaPackage = null;
          break;
        }
      if (paramNativeJavaPackage != null)
        this.importedPackages.add(paramNativeJavaPackage);
    }
  }

  private void importClass(NativeJavaClass paramNativeJavaClass)
  {
    String str1 = paramNativeJavaClass.getClassObject().getName();
    String str2 = str1.substring(str1.lastIndexOf(46) + 1);
    Object localObject = get(str2, this);
    if ((localObject != NOT_FOUND) && (localObject != paramNativeJavaClass))
      throw Context.reportRuntimeError1("msg.prop.defined", str2);
    put(str2, this, paramNativeJavaClass);
  }

  protected void initPrototypeId(int paramInt)
  {
    String str;
    int i;
    switch (paramInt)
    {
    case 1:
      i = 0;
      str = "constructor";
      break;
    case 2:
      i = 1;
      str = "importClass";
      break;
    case 3:
      i = 1;
      str = "importPackage";
      break;
    default:
      throw new IllegalArgumentException(String.valueOf(paramInt));
    }
    initPrototypeMethod(IMPORTER_TAG, paramInt, str, i);
  }

  public Object execIdCall(IdFunctionObject paramIdFunctionObject, Context paramContext, Scriptable paramScriptable1, Scriptable paramScriptable2, Object[] paramArrayOfObject)
  {
    if (!(paramIdFunctionObject.hasTag(IMPORTER_TAG)))
      return super.execIdCall(paramIdFunctionObject, paramContext, paramScriptable1, paramScriptable2, paramArrayOfObject);
    int i = paramIdFunctionObject.methodId();
    switch (i)
    {
    case 1:
      return js_construct(paramScriptable1, paramArrayOfObject);
    case 2:
      return realThis(paramScriptable2, paramIdFunctionObject).js_importClass(paramArrayOfObject);
    case 3:
      return realThis(paramScriptable2, paramIdFunctionObject).js_importPackage(paramArrayOfObject);
    }
    throw new IllegalArgumentException(String.valueOf(i));
  }

  private ImporterTopLevel realThis(Scriptable paramScriptable, IdFunctionObject paramIdFunctionObject)
  {
    if (this.topScopeFlag)
      return this;
    if (!(paramScriptable instanceof ImporterTopLevel))
      throw incompatibleCallError(paramIdFunctionObject);
    return ((ImporterTopLevel)paramScriptable);
  }

  protected int findPrototypeId(String paramString)
  {
    int i = 0;
    String str = null;
    int k = paramString.length();
    if (k == 11)
    {
      int j = paramString.charAt(0);
      if (j == 99)
      {
        str = "constructor";
        i = 1;
      }
      else if (j == 105)
      {
        str = "importClass";
        i = 2;
      }
    }
    else if (k == 13)
    {
      str = "importPackage";
      i = 3;
    }
    if ((str != null) && (str != paramString) && (!(str.equals(paramString))))
      i = 0;
    return i;
  }
}