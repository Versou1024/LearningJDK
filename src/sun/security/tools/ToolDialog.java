package sun.security.tools;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextField;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.security.InvalidParameterException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.accessibility.AccessibleContext;
import sun.security.provider.PolicyParser.GrantEntry;
import sun.security.provider.PolicyParser.PermissionEntry;
import sun.security.provider.PolicyParser.PrincipalEntry;

class ToolDialog extends Dialog
{
  private static final long serialVersionUID = -372244357011301190L;
  public static final int NOACTION = 0;
  public static final int QUIT = 1;
  public static final int NEW = 2;
  public static final int OPEN = 3;
  public static final String ALL_PERM_CLASS = "java.security.AllPermission";
  public static final String FILE_PERM_CLASS = "java.io.FilePermission";
  public static final String X500_PRIN_CLASS = "javax.security.auth.x500.X500Principal";
  public static final String PERM = PolicyTool.rb.getString("Permission:                                                       ");
  public static final String PRIN_TYPE = PolicyTool.rb.getString("Principal Type:");
  public static final String PRIN_NAME = PolicyTool.rb.getString("Principal Name:");
  public static final String PERM_NAME = PolicyTool.rb.getString("Target Name:                                                    ");
  public static final String PERM_ACTIONS = PolicyTool.rb.getString("Actions:                                                             ");
  public static final int OW_LABEL = 0;
  public static final int OW_OK_BUTTON = 1;
  public static final int OW_CANCEL_BUTTON = 2;
  public static final int PE_CODEBASE_LABEL = 0;
  public static final int PE_CODEBASE_TEXTFIELD = 1;
  public static final int PE_SIGNEDBY_LABEL = 2;
  public static final int PE_SIGNEDBY_TEXTFIELD = 3;
  public static final int PE_PANEL0 = 4;
  public static final int PE_ADD_PRIN_BUTTON = 0;
  public static final int PE_EDIT_PRIN_BUTTON = 1;
  public static final int PE_REMOVE_PRIN_BUTTON = 2;
  public static final int PE_PRIN_LABEL = 5;
  public static final int PE_PRIN_LIST = 6;
  public static final int PE_PANEL1 = 7;
  public static final int PE_ADD_PERM_BUTTON = 0;
  public static final int PE_EDIT_PERM_BUTTON = 1;
  public static final int PE_REMOVE_PERM_BUTTON = 2;
  public static final int PE_PERM_LIST = 8;
  public static final int PE_PANEL2 = 9;
  public static final int PE_CANCEL_BUTTON = 1;
  public static final int PE_DONE_BUTTON = 0;
  public static final int PRD_DESC_LABEL = 0;
  public static final int PRD_PRIN_CHOICE = 1;
  public static final int PRD_PRIN_TEXTFIELD = 2;
  public static final int PRD_NAME_LABEL = 3;
  public static final int PRD_NAME_TEXTFIELD = 4;
  public static final int PRD_CANCEL_BUTTON = 6;
  public static final int PRD_OK_BUTTON = 5;
  public static final int PD_DESC_LABEL = 0;
  public static final int PD_PERM_CHOICE = 1;
  public static final int PD_PERM_TEXTFIELD = 2;
  public static final int PD_NAME_CHOICE = 3;
  public static final int PD_NAME_TEXTFIELD = 4;
  public static final int PD_ACTIONS_CHOICE = 5;
  public static final int PD_ACTIONS_TEXTFIELD = 6;
  public static final int PD_SIGNEDBY_LABEL = 7;
  public static final int PD_SIGNEDBY_TEXTFIELD = 8;
  public static final int PD_CANCEL_BUTTON = 10;
  public static final int PD_OK_BUTTON = 9;
  public static final int EDIT_KEYSTORE = 0;
  public static final int KSD_NAME_LABEL = 0;
  public static final int KSD_NAME_TEXTFIELD = 1;
  public static final int KSD_TYPE_LABEL = 2;
  public static final int KSD_TYPE_TEXTFIELD = 3;
  public static final int KSD_PROVIDER_LABEL = 4;
  public static final int KSD_PROVIDER_TEXTFIELD = 5;
  public static final int KSD_PWD_URL_LABEL = 6;
  public static final int KSD_PWD_URL_TEXTFIELD = 7;
  public static final int KSD_CANCEL_BUTTON = 9;
  public static final int KSD_OK_BUTTON = 8;
  public static final int USC_LABEL = 0;
  public static final int USC_PANEL = 1;
  public static final int USC_YES_BUTTON = 0;
  public static final int USC_NO_BUTTON = 1;
  public static final int USC_CANCEL_BUTTON = 2;
  public static final int CRPE_LABEL1 = 0;
  public static final int CRPE_LABEL2 = 1;
  public static final int CRPE_PANEL = 2;
  public static final int CRPE_PANEL_OK = 0;
  public static final int CRPE_PANEL_CANCEL = 1;
  private static final int PERMISSION = 0;
  private static final int PERMISSION_NAME = 1;
  private static final int PERMISSION_ACTIONS = 2;
  private static final int PERMISSION_SIGNEDBY = 3;
  private static final int PRINCIPAL_TYPE = 4;
  private static final int PRINCIPAL_NAME = 5;
  public static ArrayList<Perm> PERM_ARRAY = new ArrayList();
  public static ArrayList<Prin> PRIN_ARRAY;
  PolicyTool tool;
  ToolWindow tw;

  ToolDialog(String paramString, PolicyTool paramPolicyTool, ToolWindow paramToolWindow, boolean paramBoolean)
  {
    super(paramToolWindow, paramBoolean);
    setTitle(paramString);
    this.tool = paramPolicyTool;
    this.tw = paramToolWindow;
    addWindowListener(new ChildWindowListener(this));
  }

  static Perm getPerm(String paramString, boolean paramBoolean)
  {
    for (int i = 0; i < PERM_ARRAY.size(); ++i)
    {
      Perm localPerm = (Perm)PERM_ARRAY.get(i);
      if (paramBoolean)
      {
        if (!(localPerm.FULL_CLASS.equals(paramString)))
          break label53;
        return localPerm;
      }
      label53: if (localPerm.CLASS.equals(paramString))
        return localPerm;
    }
    return null;
  }

  static Prin getPrin(String paramString, boolean paramBoolean)
  {
    for (int i = 0; i < PRIN_ARRAY.size(); ++i)
    {
      Prin localPrin = (Prin)PRIN_ARRAY.get(i);
      if (paramBoolean)
      {
        if (!(localPrin.FULL_CLASS.equals(paramString)))
          break label53;
        return localPrin;
      }
      label53: if (localPrin.CLASS.equals(paramString))
        return localPrin;
    }
    return null;
  }

  void displayOverWriteFileDialog(String paramString, int paramInt)
  {
    Point localPoint = this.tw.getLocationOnScreen();
    setBounds(localPoint.x + 75, localPoint.y + 100, 400, 150);
    setLayout(new GridBagLayout());
    MessageFormat localMessageFormat = new MessageFormat(PolicyTool.rb.getString("OK to overwrite existing file filename?"));
    Object[] arrayOfObject = { paramString };
    Label localLabel = new Label(localMessageFormat.format(arrayOfObject));
    this.tw.addNewComponent(this, localLabel, 0, 0, 0, 2, 1, 0D, 0D, 1, ToolWindow.TOP_PADDING);
    Button localButton = new Button(PolicyTool.rb.getString("OK"));
    localButton.addActionListener(new OverWriteFileOKButtonListener(this.tool, this.tw, this, paramString, paramInt));
    this.tw.addNewComponent(this, localButton, 1, 0, 1, 1, 1, 0D, 0D, 3, ToolWindow.TOP_PADDING);
    localButton = new Button(PolicyTool.rb.getString("Cancel"));
    localButton.addActionListener(new CancelButtonListener(this));
    this.tw.addNewComponent(this, localButton, 2, 1, 1, 1, 1, 0D, 0D, 3, ToolWindow.TOP_PADDING);
    setVisible(true);
  }

  void displayPolicyEntryDialog(boolean paramBoolean)
  {
    int i = 0;
    PolicyEntry[] arrayOfPolicyEntry = null;
    TaggedList localTaggedList1 = new TaggedList(3, false);
    localTaggedList1.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("Principal List"));
    localTaggedList1.addActionListener(new EditPrinButtonListener(this.tool, this.tw, this, paramBoolean));
    TaggedList localTaggedList2 = new TaggedList(10, false);
    localTaggedList2.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("Permission List"));
    localTaggedList2.addActionListener(new EditPermButtonListener(this.tool, this.tw, this, paramBoolean));
    Point localPoint = this.tw.getLocationOnScreen();
    setBounds(localPoint.x + 75, localPoint.y + 200, 650, 500);
    setLayout(new GridBagLayout());
    setResizable(true);
    if (paramBoolean)
    {
      PolicyParser.PrincipalEntry localPrincipalEntry;
      arrayOfPolicyEntry = this.tool.getEntry();
      localObject1 = (List)this.tw.getComponent(3);
      i = ((List)localObject1).getSelectedIndex();
      localObject2 = arrayOfPolicyEntry[i].getGrantEntry().principals;
      for (int j = 0; j < ((LinkedList)localObject2).size(); ++j)
      {
        Object localObject4 = null;
        localPrincipalEntry = (PolicyParser.PrincipalEntry)((LinkedList)localObject2).get(j);
        localTaggedList1.addTaggedItem(PrincipalEntryToUserFriendlyString(localPrincipalEntry), localPrincipalEntry);
      }
      localObject3 = arrayOfPolicyEntry[i].getGrantEntry().permissionEntries;
      for (int k = 0; k < ((Vector)localObject3).size(); ++k)
      {
        localPrincipalEntry = null;
        PolicyParser.PermissionEntry localPermissionEntry = (PolicyParser.PermissionEntry)((Vector)localObject3).elementAt(k);
        localTaggedList2.addTaggedItem(PermissionEntryToUserFriendlyString(localPermissionEntry), localPermissionEntry);
      }
    }
    Object localObject1 = new Label(PolicyTool.rb.getString("CodeBase:"));
    this.tw.addNewComponent(this, (Component)localObject1, 0, 0, 0, 1, 1, 0D, 0D, 1);
    Object localObject2 = new TextField(60);
    ((TextField)localObject2).getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("Code Base"));
    this.tw.addNewComponent(this, (Component)localObject2, 1, 1, 0, 1, 1, 0D, 0D, 1);
    localObject1 = new Label(PolicyTool.rb.getString("SignedBy:"));
    this.tw.addNewComponent(this, (Component)localObject1, 2, 0, 1, 1, 1, 0D, 0D, 1);
    localObject2 = new TextField(60);
    ((TextField)localObject2).getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("Signed By:"));
    this.tw.addNewComponent(this, (Component)localObject2, 3, 1, 1, 1, 1, 0D, 0D, 1);
    Object localObject3 = new Panel();
    ((Panel)localObject3).setLayout(new GridBagLayout());
    Button localButton = new Button(PolicyTool.rb.getString("Add Principal"));
    localButton.addActionListener(new AddPrinButtonListener(this.tool, this.tw, this, paramBoolean));
    this.tw.addNewComponent((Container)localObject3, localButton, 0, 0, 0, 1, 1, 100.0D, 0D, 2);
    localButton = new Button(PolicyTool.rb.getString("Edit Principal"));
    localButton.addActionListener(new EditPrinButtonListener(this.tool, this.tw, this, paramBoolean));
    this.tw.addNewComponent((Container)localObject3, localButton, 1, 1, 0, 1, 1, 100.0D, 0D, 2);
    localButton = new Button(PolicyTool.rb.getString("Remove Principal"));
    localButton.addActionListener(new RemovePrinButtonListener(this.tool, this.tw, this, paramBoolean));
    this.tw.addNewComponent((Container)localObject3, localButton, 2, 2, 0, 1, 1, 100.0D, 0D, 2);
    this.tw.addNewComponent(this, (Component)localObject3, 4, 1, 2, 1, 1, 0D, 0D, 2);
    localObject1 = new Label(PolicyTool.rb.getString("Principals:"));
    this.tw.addNewComponent(this, (Component)localObject1, 5, 0, 3, 1, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
    this.tw.addNewComponent(this, localTaggedList1, 6, 1, 3, 3, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
    localObject3 = new Panel();
    ((Panel)localObject3).setLayout(new GridBagLayout());
    localButton = new Button(PolicyTool.rb.getString("  Add Permission"));
    localButton.addActionListener(new AddPermButtonListener(this.tool, this.tw, this, paramBoolean));
    this.tw.addNewComponent((Container)localObject3, localButton, 0, 0, 0, 1, 1, 100.0D, 0D, 2);
    localButton = new Button(PolicyTool.rb.getString("  Edit Permission"));
    localButton.addActionListener(new EditPermButtonListener(this.tool, this.tw, this, paramBoolean));
    this.tw.addNewComponent((Container)localObject3, localButton, 1, 1, 0, 1, 1, 100.0D, 0D, 2);
    localButton = new Button(PolicyTool.rb.getString("Remove Permission"));
    localButton.addActionListener(new RemovePermButtonListener(this.tool, this.tw, this, paramBoolean));
    this.tw.addNewComponent((Container)localObject3, localButton, 2, 2, 0, 1, 1, 100.0D, 0D, 2);
    this.tw.addNewComponent(this, (Component)localObject3, 7, 0, 4, 2, 1, 0D, 0D, 2, ToolWindow.LITE_BOTTOM_PADDING);
    this.tw.addNewComponent(this, localTaggedList2, 8, 0, 5, 3, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
    localObject3 = new Panel();
    ((Panel)localObject3).setLayout(new GridBagLayout());
    localButton = new Button(PolicyTool.rb.getString("Done"));
    localButton.addActionListener(new AddEntryDoneButtonListener(this.tool, this.tw, this, paramBoolean));
    this.tw.addNewComponent((Container)localObject3, localButton, 0, 0, 0, 1, 1, 0D, 0D, 3, ToolWindow.LR_PADDING);
    localButton = new Button(PolicyTool.rb.getString("Cancel"));
    localButton.addActionListener(new CancelButtonListener(this));
    this.tw.addNewComponent((Container)localObject3, localButton, 1, 1, 0, 1, 1, 0D, 0D, 3, ToolWindow.LR_PADDING);
    this.tw.addNewComponent(this, (Component)localObject3, 9, 0, 6, 2, 1, 0D, 0D, 3);
    setVisible(true);
  }

  PolicyEntry getPolicyEntryFromDialog()
    throws InvalidParameterException, MalformedURLException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, CertificateException, IOException, Exception
  {
    TextField localTextField = (TextField)getComponent(1);
    String str1 = null;
    if (!(localTextField.getText().trim().equals("")))
      str1 = new String(localTextField.getText().trim());
    localTextField = (TextField)getComponent(3);
    String str2 = null;
    if (!(localTextField.getText().trim().equals("")))
      str2 = new String(localTextField.getText().trim());
    PolicyParser.GrantEntry localGrantEntry = new PolicyParser.GrantEntry(str2, str1);
    LinkedList localLinkedList = new LinkedList();
    TaggedList localTaggedList1 = (TaggedList)getComponent(6);
    for (int i = 0; i < localTaggedList1.getItemCount(); ++i)
      localLinkedList.add(localTaggedList1.getObject(i));
    localGrantEntry.principals = localLinkedList;
    Vector localVector = new Vector();
    TaggedList localTaggedList2 = (TaggedList)getComponent(8);
    for (int j = 0; j < localTaggedList2.getItemCount(); ++j)
      localVector.addElement(localTaggedList2.getObject(j));
    localGrantEntry.permissionEntries = localVector;
    PolicyEntry localPolicyEntry = new PolicyEntry(this.tool, localGrantEntry);
    return localPolicyEntry;
  }

  void keyStoreDialog(int paramInt)
  {
    Point localPoint = this.tw.getLocationOnScreen();
    setBounds(localPoint.x + 25, localPoint.y + 100, 500, 300);
    setLayout(new GridBagLayout());
    if (paramInt == 0)
    {
      Label localLabel = new Label(PolicyTool.rb.getString("KeyStore URL:"));
      this.tw.addNewComponent(this, localLabel, 0, 0, 0, 1, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
      TextField localTextField = new TextField(this.tool.getKeyStoreName(), 30);
      localTextField.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("KeyStore U R L:"));
      this.tw.addNewComponent(this, localTextField, 1, 1, 0, 1, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
      localLabel = new Label(PolicyTool.rb.getString("KeyStore Type:"));
      this.tw.addNewComponent(this, localLabel, 2, 0, 1, 1, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
      localTextField = new TextField(this.tool.getKeyStoreType(), 30);
      localTextField.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("KeyStore Type:"));
      this.tw.addNewComponent(this, localTextField, 3, 1, 1, 1, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
      localLabel = new Label(PolicyTool.rb.getString("KeyStore Provider:"));
      this.tw.addNewComponent(this, localLabel, 4, 0, 2, 1, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
      localTextField = new TextField(this.tool.getKeyStoreProvider(), 30);
      localTextField.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("KeyStore Provider:"));
      this.tw.addNewComponent(this, localTextField, 5, 1, 2, 1, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
      localLabel = new Label(PolicyTool.rb.getString("KeyStore Password URL:"));
      this.tw.addNewComponent(this, localLabel, 6, 0, 3, 1, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
      localTextField = new TextField(this.tool.getKeyStorePwdURL(), 30);
      localTextField.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("KeyStore Password U R L:"));
      this.tw.addNewComponent(this, localTextField, 7, 1, 3, 1, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
      Button localButton1 = new Button(PolicyTool.rb.getString("OK"));
      localButton1.addActionListener(new ChangeKeyStoreOKButtonListener(this.tool, this.tw, this));
      this.tw.addNewComponent(this, localButton1, 8, 0, 4, 1, 1, 0D, 0D, 3);
      Button localButton2 = new Button(PolicyTool.rb.getString("Cancel"));
      localButton2.addActionListener(new CancelButtonListener(this));
      this.tw.addNewComponent(this, localButton2, 9, 1, 4, 1, 1, 0D, 0D, 3);
    }
    setVisible(true);
  }

  void displayPrincipalDialog(boolean paramBoolean1, boolean paramBoolean2)
  {
    PolicyParser.PrincipalEntry localPrincipalEntry = null;
    TaggedList localTaggedList = (TaggedList)getComponent(6);
    int i = localTaggedList.getSelectedIndex();
    if (paramBoolean2)
      localPrincipalEntry = (PolicyParser.PrincipalEntry)localTaggedList.getObject(i);
    ToolDialog localToolDialog = new ToolDialog(PolicyTool.rb.getString("Principals"), this.tool, this.tw, true);
    localToolDialog.addWindowListener(new ChildWindowListener(localToolDialog));
    Point localPoint = getLocationOnScreen();
    localToolDialog.setBounds(localPoint.x + 50, localPoint.y + 100, 650, 190);
    localToolDialog.setLayout(new GridBagLayout());
    localToolDialog.setResizable(true);
    Label localLabel = new Label(PolicyTool.rb.getString("  Add New Principal:"));
    this.tw.addNewComponent(localToolDialog, localLabel, 0, 0, 0, 1, 1, 0D, 0D, 1, ToolWindow.TOP_BOTTOM_PADDING);
    Choice localChoice = new Choice();
    localChoice.add(PRIN_TYPE);
    localChoice.getAccessibleContext().setAccessibleName(PRIN_TYPE);
    for (int j = 0; j < PRIN_ARRAY.size(); ++j)
    {
      localObject2 = (Prin)PRIN_ARRAY.get(j);
      localChoice.add(((Prin)localObject2).CLASS);
    }
    localChoice.addItemListener(new PrincipalTypeMenuListener(localToolDialog));
    if (paramBoolean2)
      if ("WILDCARD_PRINCIPAL_CLASS".equals(localPrincipalEntry.getPrincipalClass()))
      {
        localChoice.select(PRIN_TYPE);
      }
      else
      {
        localObject1 = getPrin(localPrincipalEntry.getPrincipalClass(), true);
        if (localObject1 != null)
          localChoice.select(((Prin)localObject1).CLASS);
      }
    this.tw.addNewComponent(localToolDialog, localChoice, 1, 0, 1, 1, 1, 0D, 0D, 1, ToolWindow.LR_PADDING);
    Object localObject1 = new TextField(30);
    ((TextField)localObject1).getAccessibleContext().setAccessibleName(PRIN_TYPE);
    this.tw.addNewComponent(localToolDialog, (Component)localObject1, 2, 1, 1, 1, 1, 0D, 0D, 1, ToolWindow.LR_PADDING);
    localLabel = new Label(PRIN_NAME);
    localObject1 = new TextField(40);
    ((TextField)localObject1).getAccessibleContext().setAccessibleName(PRIN_NAME);
    this.tw.addNewComponent(localToolDialog, localLabel, 3, 0, 2, 1, 1, 0D, 0D, 1, ToolWindow.LR_PADDING);
    this.tw.addNewComponent(localToolDialog, (Component)localObject1, 4, 1, 2, 1, 1, 0D, 0D, 1, ToolWindow.LR_PADDING);
    Object localObject2 = new Button(PolicyTool.rb.getString("OK"));
    ((Button)localObject2).addActionListener(new NewPolicyPrinOKButtonListener(this.tool, this.tw, this, localToolDialog, paramBoolean2));
    this.tw.addNewComponent(localToolDialog, (Component)localObject2, 5, 0, 3, 1, 1, 0D, 0D, 3, ToolWindow.TOP_BOTTOM_PADDING);
    Button localButton = new Button(PolicyTool.rb.getString("Cancel"));
    localButton.addActionListener(new CancelButtonListener(localToolDialog));
    this.tw.addNewComponent(localToolDialog, localButton, 6, 1, 3, 1, 1, 0D, 0D, 3, ToolWindow.TOP_BOTTOM_PADDING);
    localToolDialog.setVisible(true);
  }

  void displayPermissionDialog(boolean paramBoolean1, boolean paramBoolean2)
  {
    PolicyParser.PermissionEntry localPermissionEntry = null;
    TaggedList localTaggedList = (TaggedList)getComponent(8);
    int i = localTaggedList.getSelectedIndex();
    if (paramBoolean2)
      localPermissionEntry = (PolicyParser.PermissionEntry)localTaggedList.getObject(i);
    ToolDialog localToolDialog = new ToolDialog(PolicyTool.rb.getString("Permissions"), this.tool, this.tw, true);
    localToolDialog.addWindowListener(new ChildWindowListener(localToolDialog));
    Point localPoint = getLocationOnScreen();
    localToolDialog.setBounds(localPoint.x + 50, localPoint.y + 100, 700, 250);
    localToolDialog.setLayout(new GridBagLayout());
    localToolDialog.setResizable(true);
    Label localLabel = new Label(PolicyTool.rb.getString("  Add New Permission:"));
    this.tw.addNewComponent(localToolDialog, localLabel, 0, 0, 0, 1, 1, 0D, 0D, 1, ToolWindow.TOP_BOTTOM_PADDING);
    Choice localChoice = new Choice();
    localChoice.add(PERM);
    localChoice.getAccessibleContext().setAccessibleName(PERM);
    for (int j = 0; j < PERM_ARRAY.size(); ++j)
    {
      localObject = (Perm)PERM_ARRAY.get(j);
      localChoice.add(((Perm)localObject).CLASS);
    }
    localChoice.addItemListener(new PermissionMenuListener(localToolDialog));
    this.tw.addNewComponent(localToolDialog, localChoice, 1, 0, 1, 1, 1, 0D, 0D, 1, ToolWindow.LR_PADDING);
    TextField localTextField = new TextField(30);
    localTextField.getAccessibleContext().setAccessibleName(PERM);
    if (paramBoolean2)
    {
      localObject = getPerm(localPermissionEntry.permission, true);
      if (localObject != null)
        localChoice.select(((Perm)localObject).CLASS);
    }
    this.tw.addNewComponent(localToolDialog, localTextField, 2, 1, 1, 1, 1, 0D, 0D, 1, ToolWindow.LR_PADDING);
    localChoice = new Choice();
    localChoice.add(PERM_NAME);
    localChoice.getAccessibleContext().setAccessibleName(PERM_NAME);
    localChoice.addItemListener(new PermissionNameMenuListener(localToolDialog));
    localTextField = new TextField(40);
    localTextField.getAccessibleContext().setAccessibleName(PERM_NAME);
    if (paramBoolean2)
      setPermissionNames(getPerm(localPermissionEntry.permission, true), localChoice, localTextField);
    this.tw.addNewComponent(localToolDialog, localChoice, 3, 0, 2, 1, 1, 0D, 0D, 1, ToolWindow.LR_PADDING);
    this.tw.addNewComponent(localToolDialog, localTextField, 4, 1, 2, 1, 1, 0D, 0D, 1, ToolWindow.LR_PADDING);
    localChoice = new Choice();
    localChoice.add(PERM_ACTIONS);
    localChoice.getAccessibleContext().setAccessibleName(PERM_ACTIONS);
    localChoice.addItemListener(new PermissionActionsMenuListener(localToolDialog));
    localTextField = new TextField(40);
    localTextField.getAccessibleContext().setAccessibleName(PERM_ACTIONS);
    if (paramBoolean2)
      setPermissionActions(getPerm(localPermissionEntry.permission, true), localChoice, localTextField);
    this.tw.addNewComponent(localToolDialog, localChoice, 5, 0, 3, 1, 1, 0D, 0D, 1, ToolWindow.LR_PADDING);
    this.tw.addNewComponent(localToolDialog, localTextField, 6, 1, 3, 1, 1, 0D, 0D, 1, ToolWindow.LR_PADDING);
    localLabel = new Label(PolicyTool.rb.getString("Signed By:"));
    this.tw.addNewComponent(localToolDialog, localLabel, 7, 0, 4, 1, 1, 0D, 0D, 1, ToolWindow.LR_PADDING);
    localTextField = new TextField(40);
    localTextField.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("Signed By:"));
    this.tw.addNewComponent(localToolDialog, localTextField, 8, 1, 4, 1, 1, 0D, 0D, 1, ToolWindow.LR_PADDING);
    Object localObject = new Button(PolicyTool.rb.getString("OK"));
    ((Button)localObject).addActionListener(new NewPolicyPermOKButtonListener(this.tool, this.tw, this, localToolDialog, paramBoolean2));
    this.tw.addNewComponent(localToolDialog, (Component)localObject, 9, 0, 5, 1, 1, 0D, 0D, 3, ToolWindow.TOP_BOTTOM_PADDING);
    Button localButton = new Button(PolicyTool.rb.getString("Cancel"));
    localButton.addActionListener(new CancelButtonListener(localToolDialog));
    this.tw.addNewComponent(localToolDialog, localButton, 10, 1, 5, 1, 1, 0D, 0D, 3, ToolWindow.TOP_BOTTOM_PADDING);
    localToolDialog.setVisible(true);
  }

  PolicyParser.PrincipalEntry getPrinFromDialog()
    throws Exception
  {
    TextField localTextField = (TextField)getComponent(2);
    String str1 = new String(localTextField.getText().trim());
    localTextField = (TextField)getComponent(4);
    String str2 = new String(localTextField.getText().trim());
    if (str1.equals("*"))
      str1 = "WILDCARD_PRINCIPAL_CLASS";
    if (str2.equals("*"))
      str2 = "WILDCARD_PRINCIPAL_NAME";
    Object localObject = null;
    if ((str1.equals("WILDCARD_PRINCIPAL_CLASS")) && (!(str2.equals("WILDCARD_PRINCIPAL_NAME"))))
      throw new Exception(PolicyTool.rb.getString("Cannot Specify Principal with a Wildcard Class without a Wildcard Name"));
    if (str2.equals(""))
      throw new Exception(PolicyTool.rb.getString("Cannot Specify Principal without a Name"));
    if (str1.equals(""))
    {
      str1 = "PolicyParser.REPLACE_NAME";
      this.tool.warnings.addElement("Warning: Principal name '" + str2 + "' specified without a Principal class.\n" + "\t'" + str2 + "' will be interpreted " + "as a key store alias.\n" + "\tThe final principal class will be " + "javax.security.auth.x500.X500Principal" + ".\n" + "\tThe final principal name will be " + "determined by the following:\n" + "\n" + "\tIf the key store entry identified by '" + str2 + "'\n" + "\tis a key entry, then the principal name will be\n" + "\tthe subject distinguished name from the first\n" + "\tcertificate in the entry's certificate chain.\n" + "\n" + "\tIf the key store entry identified by '" + str2 + "'\n" + "\tis a trusted certificate entry, then the\n" + "\tprincipal name will be the subject distinguished\n" + "\tname from the trusted public key certificate.");
      this.tw.displayStatusDialog(this, "'" + str2 + "' will be interpreted as a key " + "store alias.  View Warning Log for details.");
    }
    return new PolicyParser.PrincipalEntry(str1, str2);
  }

  PolicyParser.PermissionEntry getPermFromDialog()
  {
    TextField localTextField = (TextField)getComponent(2);
    String str1 = new String(localTextField.getText().trim());
    localTextField = (TextField)getComponent(4);
    String str2 = null;
    if (!(localTextField.getText().trim().equals("")))
      str2 = new String(localTextField.getText().trim());
    if ((str1.equals("")) || ((!(str1.equals("java.security.AllPermission"))) && (str2 == null)))
      throw new InvalidParameterException(PolicyTool.rb.getString("Permission and Target Name must have a value"));
    if ((str1.equals("java.io.FilePermission")) && (str2.lastIndexOf("\\\\") > 0))
    {
      int i = this.tw.displayYesNoDialog(this, PolicyTool.rb.getString("Warning"), PolicyTool.rb.getString("Warning: File name may include escaped backslash characters. It is not necessary to escape backslash characters (the tool escapes characters as necessary when writing the policy contents to the persistent store).\n\nClick on Retain to retain the entered name, or click on Edit to edit the name."), PolicyTool.rb.getString("Retain"), PolicyTool.rb.getString("Edit"));
      if (i != 89)
        throw new NoDisplayException();
    }
    localTextField = (TextField)getComponent(6);
    String str3 = null;
    if (!(localTextField.getText().trim().equals("")))
      str3 = new String(localTextField.getText().trim());
    localTextField = (TextField)getComponent(8);
    String str4 = null;
    if (!(localTextField.getText().trim().equals("")))
      str4 = new String(localTextField.getText().trim());
    PolicyParser.PermissionEntry localPermissionEntry = new PolicyParser.PermissionEntry(str1, str2, str3);
    localPermissionEntry.signedBy = str4;
    if (str4 != null)
    {
      String[] arrayOfString = this.tool.parseSigners(localPermissionEntry.signedBy);
      for (int j = 0; j < arrayOfString.length; ++j)
        try
        {
          PublicKey localPublicKey = this.tool.getPublicKeyAlias(arrayOfString[j]);
          if (localPublicKey == null)
          {
            MessageFormat localMessageFormat = new MessageFormat(PolicyTool.rb.getString("Warning: A public key for alias 'signers[i]' does not exist.  Make sure a KeyStore is properly configured."));
            Object[] arrayOfObject = { arrayOfString[j] };
            this.tool.warnings.addElement(localMessageFormat.format(arrayOfObject));
            this.tw.displayStatusDialog(this, localMessageFormat.format(arrayOfObject));
          }
        }
        catch (Exception localException)
        {
          this.tw.displayErrorDialog(this, localException);
        }
    }
    return localPermissionEntry;
  }

  void displayConfirmRemovePolicyEntry()
  {
    List localList = (List)this.tw.getComponent(3);
    int i = localList.getSelectedIndex();
    PolicyEntry[] arrayOfPolicyEntry = this.tool.getEntry();
    Point localPoint = this.tw.getLocationOnScreen();
    setBounds(localPoint.x + 25, localPoint.y + 100, 600, 400);
    setLayout(new GridBagLayout());
    Label localLabel = new Label(PolicyTool.rb.getString("Remove this Policy Entry?"));
    this.tw.addNewComponent(this, localLabel, 0, 0, 0, 2, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
    localLabel = new Label(arrayOfPolicyEntry[i].codebaseToString());
    this.tw.addNewComponent(this, localLabel, 1, 0, 1, 2, 1, 0D, 0D, 1);
    localLabel = new Label(arrayOfPolicyEntry[i].principalsToString().trim());
    this.tw.addNewComponent(this, localLabel, 2, 0, 2, 2, 1, 0D, 0D, 1);
    Vector localVector = arrayOfPolicyEntry[i].getGrantEntry().permissionEntries;
    for (int j = 0; j < localVector.size(); ++j)
    {
      localObject1 = (PolicyParser.PermissionEntry)localVector.elementAt(j);
      localObject2 = PermissionEntryToUserFriendlyString((PolicyParser.PermissionEntry)localObject1);
      localLabel = new Label("    " + ((String)localObject2));
      if (j == localVector.size() - 1)
        this.tw.addNewComponent(this, localLabel, 3 + j, 1, 3 + j, 1, 1, 0D, 0D, 1, ToolWindow.BOTTOM_PADDING);
      else
        this.tw.addNewComponent(this, localLabel, 3 + j, 1, 3 + j, 1, 1, 0D, 0D, 1);
    }
    Panel localPanel = new Panel();
    localPanel.setLayout(new GridBagLayout());
    Object localObject1 = new Button(PolicyTool.rb.getString("OK"));
    ((Button)localObject1).addActionListener(new ConfirmRemovePolicyEntryOKButtonListener(this.tool, this.tw, this));
    this.tw.addNewComponent(localPanel, (Component)localObject1, 0, 0, 0, 1, 1, 0D, 0D, 3, ToolWindow.LR_PADDING);
    Object localObject2 = new Button(PolicyTool.rb.getString("Cancel"));
    ((Button)localObject2).addActionListener(new CancelButtonListener(this));
    this.tw.addNewComponent(localPanel, (Component)localObject2, 1, 1, 0, 1, 1, 0D, 0D, 3, ToolWindow.LR_PADDING);
    this.tw.addNewComponent(this, localPanel, 3 + localVector.size(), 0, 3 + localVector.size(), 2, 1, 0D, 0D, 3, ToolWindow.TOP_BOTTOM_PADDING);
    pack();
    setVisible(true);
  }

  void displaySaveAsDialog(int paramInt)
  {
    Object localObject;
    FileDialog localFileDialog = new FileDialog(this.tw, PolicyTool.rb.getString("Save As"), 1);
    localFileDialog.addWindowListener(new WindowAdapter(this)
    {
      public void windowClosing()
      {
        paramWindowEvent.getWindow().setVisible(false);
      }
    });
    localFileDialog.setVisible(true);
    if ((localFileDialog.getFile() == null) || (localFileDialog.getFile().equals("")))
      return;
    String str = new String(localFileDialog.getDirectory() + localFileDialog.getFile());
    localFileDialog.dispose();
    File localFile = new File(str);
    if (localFile.exists())
    {
      localObject = new ToolDialog(PolicyTool.rb.getString("Overwrite File"), this.tool, this.tw, true);
      ((ToolDialog)localObject).displayOverWriteFileDialog(str, paramInt);
    }
    else
    {
      try
      {
        this.tool.savePolicy(str);
        localObject = new MessageFormat(PolicyTool.rb.getString("Policy successfully written to filename"));
        Object[] arrayOfObject = { str };
        this.tw.displayStatusDialog(null, ((MessageFormat)localObject).format(arrayOfObject));
        TextField localTextField = (TextField)this.tw.getComponent(1);
        localTextField.setText(str);
        this.tw.setVisible(true);
        userSaveContinue(this.tool, this.tw, this, paramInt);
      }
      catch (FileNotFoundException localFileNotFoundException)
      {
        if ((str == null) || (str.equals("")))
          this.tw.displayErrorDialog(null, new FileNotFoundException(PolicyTool.rb.getString("null filename")));
        else
          this.tw.displayErrorDialog(null, localFileNotFoundException);
      }
      catch (Exception localException)
      {
        this.tw.displayErrorDialog(null, localException);
      }
    }
  }

  void displayUserSave(int paramInt)
  {
    if (this.tool.modified == true)
    {
      Point localPoint = this.tw.getLocationOnScreen();
      setBounds(localPoint.x + 75, localPoint.y + 100, 400, 150);
      setLayout(new GridBagLayout());
      Label localLabel = new Label(PolicyTool.rb.getString("Save changes?"));
      this.tw.addNewComponent(this, localLabel, 0, 0, 0, 3, 1, 0D, 0D, 1, ToolWindow.L_TOP_BOTTOM_PADDING);
      Panel localPanel = new Panel();
      localPanel.setLayout(new GridBagLayout());
      Button localButton1 = new Button(PolicyTool.rb.getString("Yes"));
      localButton1.addActionListener(new UserSaveYesButtonListener(this, this.tool, this.tw, paramInt));
      this.tw.addNewComponent(localPanel, localButton1, 0, 0, 0, 1, 1, 0D, 0D, 3, ToolWindow.LR_BOTTOM_PADDING);
      Button localButton2 = new Button(PolicyTool.rb.getString("No"));
      localButton2.addActionListener(new UserSaveNoButtonListener(this, this.tool, this.tw, paramInt));
      this.tw.addNewComponent(localPanel, localButton2, 1, 1, 0, 1, 1, 0D, 0D, 3, ToolWindow.LR_BOTTOM_PADDING);
      Button localButton3 = new Button(PolicyTool.rb.getString("Cancel"));
      localButton3.addActionListener(new UserSaveCancelButtonListener(this));
      this.tw.addNewComponent(localPanel, localButton3, 2, 2, 0, 1, 1, 0D, 0D, 3, ToolWindow.LR_BOTTOM_PADDING);
      this.tw.addNewComponent(this, localPanel, 1, 0, 1, 1, 1, 0D, 0D, 1);
      pack();
      setVisible(true);
    }
    else
    {
      userSaveContinue(this.tool, this.tw, this, paramInt);
    }
  }

  void userSaveContinue(PolicyTool paramPolicyTool, ToolWindow paramToolWindow, ToolDialog paramToolDialog, int paramInt)
  {
    List localList;
    TextField localTextField;
    switch (paramInt)
    {
    case 1:
      paramToolWindow.setVisible(false);
      paramToolWindow.dispose();
      System.exit(0);
    case 2:
      try
      {
        paramPolicyTool.openPolicy(null);
      }
      catch (Exception localException1)
      {
        paramPolicyTool.modified = false;
        paramToolWindow.displayErrorDialog(null, localException1);
      }
      localList = new List(40, false);
      localList.addActionListener(new PolicyListListener(paramPolicyTool, paramToolWindow));
      paramToolWindow.replacePolicyList(localList);
      localTextField = (TextField)paramToolWindow.getComponent(1);
      localTextField.setText("");
      paramToolWindow.setVisible(true);
      break;
    case 3:
      FileDialog localFileDialog = new FileDialog(paramToolWindow, PolicyTool.rb.getString("Open"), 0);
      localFileDialog.addWindowListener(new WindowAdapter(this)
      {
        public void windowClosing()
        {
          paramWindowEvent.getWindow().setVisible(false);
        }
      });
      localFileDialog.setVisible(true);
      if ((localFileDialog.getFile() == null) || (localFileDialog.getFile().equals("")))
        return;
      String str = new String(localFileDialog.getDirectory() + localFileDialog.getFile());
      try
      {
        paramPolicyTool.openPolicy(str);
        localList = new List(40, false);
        localList.addActionListener(new PolicyListListener(paramPolicyTool, paramToolWindow));
        PolicyEntry[] arrayOfPolicyEntry = paramPolicyTool.getEntry();
        if (arrayOfPolicyEntry != null)
          for (int i = 0; i < arrayOfPolicyEntry.length; ++i)
            localList.add(arrayOfPolicyEntry[i].headerToString());
        paramToolWindow.replacePolicyList(localList);
        paramPolicyTool.modified = false;
        localTextField = (TextField)paramToolWindow.getComponent(1);
        localTextField.setText(str);
        paramToolWindow.setVisible(true);
        if (paramPolicyTool.newWarning == true)
          paramToolWindow.displayStatusDialog(null, PolicyTool.rb.getString("Errors have occurred while opening the policy configuration.  View the Warning Log for more information."));
      }
      catch (Exception localException2)
      {
        localList = new List(40, false);
        localList.addActionListener(new PolicyListListener(paramPolicyTool, paramToolWindow));
        paramToolWindow.replacePolicyList(localList);
        paramPolicyTool.setPolicyFileName(null);
        paramPolicyTool.modified = false;
        localTextField = (TextField)paramToolWindow.getComponent(1);
        localTextField.setText("");
        paramToolWindow.setVisible(true);
        MessageFormat localMessageFormat = new MessageFormat(PolicyTool.rb.getString("Could not open policy file: policyFile: e.toString()"));
        Object[] arrayOfObject = { str, localException2.toString() };
        paramToolWindow.displayErrorDialog(null, localMessageFormat.format(arrayOfObject));
      }
    }
  }

  void setPermissionNames(Perm paramPerm, Choice paramChoice, TextField paramTextField)
  {
    paramChoice.removeAll();
    paramChoice.add(PERM_NAME);
    if (paramPerm == null)
    {
      paramTextField.setEditable(true);
    }
    else if (paramPerm.TARGETS == null)
    {
      paramTextField.setEditable(false);
    }
    else
    {
      paramTextField.setEditable(true);
      for (int i = 0; i < paramPerm.TARGETS.length; ++i)
        paramChoice.add(paramPerm.TARGETS[i]);
    }
  }

  void setPermissionActions(Perm paramPerm, Choice paramChoice, TextField paramTextField)
  {
    paramChoice.removeAll();
    paramChoice.add(PERM_ACTIONS);
    if (paramPerm == null)
    {
      paramTextField.setEditable(true);
    }
    else if (paramPerm.ACTIONS == null)
    {
      paramTextField.setEditable(false);
    }
    else
    {
      paramTextField.setEditable(true);
      for (int i = 0; i < paramPerm.ACTIONS.length; ++i)
        paramChoice.add(paramPerm.ACTIONS[i]);
    }
  }

  static String PermissionEntryToUserFriendlyString(PolicyParser.PermissionEntry paramPermissionEntry)
  {
    String str = paramPermissionEntry.permission;
    if (paramPermissionEntry.name != null)
      str = str + " " + paramPermissionEntry.name;
    if (paramPermissionEntry.action != null)
      str = str + ", \"" + paramPermissionEntry.action + "\"";
    if (paramPermissionEntry.signedBy != null)
      str = str + ", signedBy " + paramPermissionEntry.signedBy;
    return str;
  }

  static String PrincipalEntryToUserFriendlyString(PolicyParser.PrincipalEntry paramPrincipalEntry)
  {
    StringWriter localStringWriter = new StringWriter();
    PrintWriter localPrintWriter = new PrintWriter(localStringWriter);
    paramPrincipalEntry.write(localPrintWriter);
    return localStringWriter.toString();
  }

  static
  {
    PERM_ARRAY.add(new AllPerm());
    PERM_ARRAY.add(new AudioPerm());
    PERM_ARRAY.add(new AuthPerm());
    PERM_ARRAY.add(new AWTPerm());
    PERM_ARRAY.add(new DelegationPerm());
    PERM_ARRAY.add(new FilePerm());
    PERM_ARRAY.add(new LogPerm());
    PERM_ARRAY.add(new MgmtPerm());
    PERM_ARRAY.add(new MBeanPerm());
    PERM_ARRAY.add(new MBeanSvrPerm());
    PERM_ARRAY.add(new MBeanTrustPerm());
    PERM_ARRAY.add(new NetPerm());
    PERM_ARRAY.add(new PrivCredPerm());
    PERM_ARRAY.add(new PropPerm());
    PERM_ARRAY.add(new ReflectPerm());
    PERM_ARRAY.add(new RuntimePerm());
    PERM_ARRAY.add(new SecurityPerm());
    PERM_ARRAY.add(new SerialPerm());
    PERM_ARRAY.add(new ServicePerm());
    PERM_ARRAY.add(new SocketPerm());
    PERM_ARRAY.add(new SQLPerm());
    PERM_ARRAY.add(new SSLPerm());
    PERM_ARRAY.add(new SubjDelegPerm());
    PRIN_ARRAY = new ArrayList();
    PRIN_ARRAY.add(new KrbPrin());
    PRIN_ARRAY.add(new X500Prin());
  }
}