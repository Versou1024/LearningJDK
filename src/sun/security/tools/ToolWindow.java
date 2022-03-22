package sun.security.tools;

import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.security.AccessController;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.accessibility.AccessibleContext;
import sun.security.action.GetPropertyAction;

class ToolWindow extends Frame
{
  private static final long serialVersionUID = 5682568601210376777L;
  public static final Insets TOP_PADDING = new Insets(25, 0, 0, 0);
  public static final Insets BOTTOM_PADDING = new Insets(0, 0, 25, 0);
  public static final Insets LITE_BOTTOM_PADDING = new Insets(0, 0, 10, 0);
  public static final Insets LR_PADDING = new Insets(0, 10, 0, 10);
  public static final Insets TOP_BOTTOM_PADDING = new Insets(15, 0, 15, 0);
  public static final Insets L_TOP_BOTTOM_PADDING = new Insets(5, 10, 15, 0);
  public static final Insets LR_BOTTOM_PADDING = new Insets(0, 10, 5, 10);
  public static final Insets L_BOTTOM_PADDING = new Insets(0, 10, 5, 0);
  public static final Insets R_BOTTOM_PADDING = new Insets(0, 0, 5, 10);
  public static final String NEW_POLICY_FILE = PolicyTool.rb.getString("New");
  public static final String OPEN_POLICY_FILE = PolicyTool.rb.getString("Open");
  public static final String SAVE_POLICY_FILE = PolicyTool.rb.getString("Save");
  public static final String SAVE_AS_POLICY_FILE = PolicyTool.rb.getString("Save As");
  public static final String VIEW_WARNINGS = PolicyTool.rb.getString("View Warning Log");
  public static final String QUIT = PolicyTool.rb.getString("Exit");
  public static final String ADD_POLICY_ENTRY = PolicyTool.rb.getString("Add Policy Entry");
  public static final String EDIT_POLICY_ENTRY = PolicyTool.rb.getString("Edit Policy Entry");
  public static final String REMOVE_POLICY_ENTRY = PolicyTool.rb.getString("Remove Policy Entry");
  public static final String EDIT_KEYSTORE = PolicyTool.rb.getString("Edit");
  public static final String ADD_PUBKEY_ALIAS = PolicyTool.rb.getString("Add Public Key Alias");
  public static final String REMOVE_PUBKEY_ALIAS = PolicyTool.rb.getString("Remove Public Key Alias");
  public static final int MW_FILENAME_LABEL = 0;
  public static final int MW_FILENAME_TEXTFIELD = 1;
  public static final int MW_PANEL = 2;
  public static final int MW_ADD_BUTTON = 0;
  public static final int MW_EDIT_BUTTON = 1;
  public static final int MW_REMOVE_BUTTON = 2;
  public static final int MW_POLICY_LIST = 3;
  private PolicyTool tool;

  ToolWindow(PolicyTool paramPolicyTool)
  {
    this.tool = paramPolicyTool;
  }

  private void initWindow()
  {
    Object localObject1;
    Object localObject2;
    Object localObject3;
    MenuBar localMenuBar = new MenuBar();
    Menu localMenu = new Menu(PolicyTool.rb.getString("File"));
    localMenu.add(NEW_POLICY_FILE);
    localMenu.add(OPEN_POLICY_FILE);
    localMenu.add(SAVE_POLICY_FILE);
    localMenu.add(SAVE_AS_POLICY_FILE);
    localMenu.add(VIEW_WARNINGS);
    localMenu.add(QUIT);
    localMenu.addActionListener(new FileMenuListener(this.tool, this));
    localMenuBar.add(localMenu);
    setMenuBar(localMenuBar);
    localMenu = new Menu(PolicyTool.rb.getString("KeyStore"));
    localMenu.add(EDIT_KEYSTORE);
    localMenu.addActionListener(new MainWindowListener(this.tool, this));
    localMenuBar.add(localMenu);
    setMenuBar(localMenuBar);
    Label localLabel = new Label(PolicyTool.rb.getString("Policy File:"));
    addNewComponent(this, localLabel, 0, 0, 0, 1, 1, 0D, 0D, 1, TOP_BOTTOM_PADDING);
    TextField localTextField = new TextField(50);
    localTextField.getAccessibleContext().setAccessibleName(PolicyTool.rb.getString("Policy File:"));
    localTextField.setEditable(false);
    addNewComponent(this, localTextField, 1, 1, 0, 1, 1, 0D, 0D, 1, TOP_BOTTOM_PADDING);
    Panel localPanel = new Panel();
    localPanel.setLayout(new GridBagLayout());
    Button localButton = new Button(ADD_POLICY_ENTRY);
    localButton.addActionListener(new MainWindowListener(this.tool, this));
    addNewComponent(localPanel, localButton, 0, 0, 0, 1, 1, 0D, 0D, 1, LR_PADDING);
    localButton = new Button(EDIT_POLICY_ENTRY);
    localButton.addActionListener(new MainWindowListener(this.tool, this));
    addNewComponent(localPanel, localButton, 1, 1, 0, 1, 1, 0D, 0D, 1, LR_PADDING);
    localButton = new Button(REMOVE_POLICY_ENTRY);
    localButton.addActionListener(new MainWindowListener(this.tool, this));
    addNewComponent(localPanel, localButton, 2, 2, 0, 1, 1, 0D, 0D, 1, LR_PADDING);
    addNewComponent(this, localPanel, 2, 0, 2, 2, 1, 0D, 0D, 1, BOTTOM_PADDING);
    String str = this.tool.getPolicyFileName();
    if (str == null)
    {
      localObject1 = (String)AccessController.doPrivileged(new GetPropertyAction("user.home"));
      str = ((String)localObject1) + File.separatorChar + ".java.policy";
    }
    try
    {
      this.tool.openPolicy(str);
      localObject1 = new List(40, false);
      ((List)localObject1).addActionListener(new PolicyListListener(this.tool, this));
      localObject2 = this.tool.getEntry();
      if (localObject2 != null)
        for (int i = 0; i < localObject2.length; ++i)
          ((List)localObject1).add(localObject2[i].headerToString());
      localObject3 = (TextField)getComponent(1);
      ((TextField)localObject3).setText(str);
      initPolicyList((List)localObject1);
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      localObject2 = new List(40, false);
      ((List)localObject2).addActionListener(new PolicyListListener(this.tool, this));
      initPolicyList((List)localObject2);
      this.tool.setPolicyFileName(null);
      this.tool.modified = false;
      setVisible(true);
      this.tool.warnings.addElement(localFileNotFoundException.toString());
    }
    catch (Exception localException)
    {
      localObject2 = new List(40, false);
      ((List)localObject2).addActionListener(new PolicyListListener(this.tool, this));
      initPolicyList((List)localObject2);
      this.tool.setPolicyFileName(null);
      this.tool.modified = false;
      setVisible(true);
      localObject3 = new MessageFormat(PolicyTool.rb.getString("Could not open policy file: policyFile: e.toString()"));
      Object[] arrayOfObject = { str, localException.toString() };
      displayErrorDialog(null, ((MessageFormat)localObject3).format(arrayOfObject));
    }
  }

  void addNewComponent(Container paramContainer, Component paramComponent, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, double paramDouble1, double paramDouble2, int paramInt6, Insets paramInsets)
  {
    paramContainer.add(paramComponent, paramInt1);
    GridBagLayout localGridBagLayout = (GridBagLayout)paramContainer.getLayout();
    GridBagConstraints localGridBagConstraints = new GridBagConstraints();
    localGridBagConstraints.gridx = paramInt2;
    localGridBagConstraints.gridy = paramInt3;
    localGridBagConstraints.gridwidth = paramInt4;
    localGridBagConstraints.gridheight = paramInt5;
    localGridBagConstraints.weightx = paramDouble1;
    localGridBagConstraints.weighty = paramDouble2;
    localGridBagConstraints.fill = paramInt6;
    if (paramInsets != null)
      localGridBagConstraints.insets = paramInsets;
    localGridBagLayout.setConstraints(paramComponent, localGridBagConstraints);
  }

  void addNewComponent(Container paramContainer, Component paramComponent, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, double paramDouble1, double paramDouble2, int paramInt6)
  {
    addNewComponent(paramContainer, paramComponent, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramDouble1, paramDouble2, paramInt6, null);
  }

  void initPolicyList(List paramList)
  {
    addNewComponent(this, paramList, 3, 0, 3, 2, 1, 1D, 1D, 1);
  }

  void replacePolicyList(List paramList)
  {
    List localList = (List)getComponent(3);
    localList.removeAll();
    String[] arrayOfString = paramList.getItems();
    for (int i = 0; i < arrayOfString.length; ++i)
      localList.add(arrayOfString[i]);
  }

  void displayToolWindow(String[] paramArrayOfString)
  {
    setTitle(PolicyTool.rb.getString("Policy Tool"));
    setResizable(true);
    addWindowListener(new ToolWindowListener(this));
    setBounds(135, 80, 500, 500);
    setLayout(new GridBagLayout());
    initWindow();
    setVisible(true);
    if (this.tool.newWarning == true)
      displayStatusDialog(this, PolicyTool.rb.getString("Errors have occurred while opening the policy configuration.  View the Warning Log for more information."));
  }

  void displayErrorDialog(Window paramWindow, String paramString)
  {
    ToolDialog localToolDialog = new ToolDialog(PolicyTool.rb.getString("Error"), this.tool, this, true);
    Point localPoint = (paramWindow == null) ? getLocationOnScreen() : paramWindow.getLocationOnScreen();
    localToolDialog.setBounds(localPoint.x + 50, localPoint.y + 50, 600, 100);
    localToolDialog.setLayout(new GridBagLayout());
    Label localLabel = new Label(paramString);
    addNewComponent(localToolDialog, localLabel, 0, 0, 0, 1, 1, 0D, 0D, 1);
    Button localButton = new Button(PolicyTool.rb.getString("OK"));
    localButton.addActionListener(new ErrorOKButtonListener(localToolDialog));
    addNewComponent(localToolDialog, localButton, 1, 0, 1, 1, 1, 0D, 0D, 3);
    localToolDialog.pack();
    localToolDialog.setVisible(true);
  }

  void displayErrorDialog(Window paramWindow, Throwable paramThrowable)
  {
    if (paramThrowable instanceof NoDisplayException)
      return;
    displayErrorDialog(paramWindow, paramThrowable.toString());
  }

  void displayStatusDialog(Window paramWindow, String paramString)
  {
    ToolDialog localToolDialog = new ToolDialog(PolicyTool.rb.getString("Status"), this.tool, this, true);
    Point localPoint = (paramWindow == null) ? getLocationOnScreen() : paramWindow.getLocationOnScreen();
    localToolDialog.setBounds(localPoint.x + 50, localPoint.y + 50, 500, 100);
    localToolDialog.setLayout(new GridBagLayout());
    Label localLabel = new Label(paramString);
    addNewComponent(localToolDialog, localLabel, 0, 0, 0, 1, 1, 0D, 0D, 1);
    Button localButton = new Button(PolicyTool.rb.getString("OK"));
    localButton.addActionListener(new StatusOKButtonListener(localToolDialog));
    addNewComponent(localToolDialog, localButton, 1, 0, 1, 1, 1, 0D, 0D, 3);
    localToolDialog.pack();
    localToolDialog.setVisible(true);
  }

  void displayWarningLog(Window paramWindow)
  {
    ToolDialog localToolDialog = new ToolDialog(PolicyTool.rb.getString("Warning"), this.tool, this, true);
    Point localPoint = (paramWindow == null) ? getLocationOnScreen() : paramWindow.getLocationOnScreen();
    localToolDialog.setBounds(localPoint.x + 50, localPoint.y + 50, 500, 100);
    localToolDialog.setLayout(new GridBagLayout());
    TextArea localTextArea = new TextArea();
    localTextArea.setEditable(false);
    for (int i = 0; i < this.tool.warnings.size(); ++i)
    {
      localTextArea.append((String)this.tool.warnings.elementAt(i));
      localTextArea.append(PolicyTool.rb.getString("\n"));
    }
    addNewComponent(localToolDialog, localTextArea, 0, 0, 0, 1, 1, 0D, 0D, 1, BOTTOM_PADDING);
    localTextArea.setFocusable(false);
    Button localButton = new Button(PolicyTool.rb.getString("OK"));
    localButton.addActionListener(new CancelButtonListener(localToolDialog));
    addNewComponent(localToolDialog, localButton, 1, 0, 1, 1, 1, 0D, 0D, 3, LR_PADDING);
    localToolDialog.pack();
    localToolDialog.setVisible(true);
  }

  char displayYesNoDialog(Window paramWindow, String paramString1, String paramString2, String paramString3, String paramString4)
  {
    ToolDialog localToolDialog = new ToolDialog(paramString1, this.tool, this, true);
    Point localPoint = (paramWindow == null) ? getLocationOnScreen() : paramWindow.getLocationOnScreen();
    localToolDialog.setBounds(localPoint.x + 75, localPoint.y + 100, 400, 150);
    localToolDialog.setLayout(new GridBagLayout());
    TextArea localTextArea = new TextArea(paramString2, 10, 50, 1);
    localTextArea.setEditable(false);
    addNewComponent(localToolDialog, localTextArea, 0, 0, 0, 1, 1, 0D, 0D, 1);
    localTextArea.setFocusable(false);
    Panel localPanel = new Panel();
    localPanel.setLayout(new GridBagLayout());
    StringBuffer localStringBuffer = new StringBuffer();
    Button localButton = new Button(paramString3);
    localButton.addActionListener(new ActionListener(this, localStringBuffer, localToolDialog)
    {
      public void actionPerformed()
      {
        this.val$chooseResult.append('Y');
        this.val$tw.setVisible(false);
        this.val$tw.dispose();
      }
    });
    addNewComponent(localPanel, localButton, 0, 0, 0, 1, 1, 0D, 0D, 3, LR_PADDING);
    localButton = new Button(paramString4);
    localButton.addActionListener(new ActionListener(this, localStringBuffer, localToolDialog)
    {
      public void actionPerformed()
      {
        this.val$chooseResult.append('N');
        this.val$tw.setVisible(false);
        this.val$tw.dispose();
      }
    });
    addNewComponent(localPanel, localButton, 1, 1, 0, 1, 1, 0D, 0D, 3, LR_PADDING);
    addNewComponent(localToolDialog, localPanel, 1, 0, 1, 1, 1, 0D, 0D, 3);
    localToolDialog.pack();
    localToolDialog.setVisible(true);
    if (localStringBuffer.length() > 0)
      return localStringBuffer.charAt(0);
    return 'N';
  }
}