package sun.swing.plaf.synth;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.basic.BasicDirectoryModel;
import javax.swing.plaf.synth.SynthContext;
import sun.awt.shell.ShellFolder;
import sun.swing.FilePane;
import sun.swing.FilePane.FileChooserUIAccessor;
import sun.swing.SwingUtilities2;

public class SynthFileChooserUIImpl extends SynthFileChooserUI
{
  private JLabel lookInLabel;
  private JComboBox directoryComboBox;
  private DirectoryComboBoxModel directoryComboBoxModel;
  private Action directoryComboBoxAction = new DirectoryComboBoxAction(this);
  private FilterComboBoxModel filterComboBoxModel;
  private JTextField fileNameTextField;
  private FilePane filePane;
  private JToggleButton listViewButton;
  private JToggleButton detailsViewButton;
  private boolean useShellFolder;
  private JPanel buttonPanel;
  private JPanel bottomPanel;
  private JComboBox filterComboBox;
  private static final Dimension hstrut5 = new Dimension(5, 1);
  private static final Dimension vstrut5 = new Dimension(1, 5);
  private static final Insets shrinkwrap = new Insets(0, 0, 0, 0);
  private static Dimension LIST_PREF_SIZE = new Dimension(405, 135);
  private int lookInLabelMnemonic = 0;
  private String lookInLabelText = null;
  private String saveInLabelText = null;
  private int fileNameLabelMnemonic = 0;
  private String fileNameLabelText = null;
  private int filesOfTypeLabelMnemonic = 0;
  private String filesOfTypeLabelText = null;
  private String upFolderToolTipText = null;
  private String upFolderAccessibleName = null;
  private String homeFolderToolTipText = null;
  private String homeFolderAccessibleName = null;
  private String newFolderToolTipText = null;
  private String newFolderAccessibleName = null;
  private String listViewButtonToolTipText = null;
  private String listViewButtonAccessibleName = null;
  private String detailsViewButtonToolTipText = null;
  private String detailsViewButtonAccessibleName = null;
  static final int space = 10;

  public SynthFileChooserUIImpl(JFileChooser paramJFileChooser)
  {
    super(paramJFileChooser);
  }

  public void installComponents(JFileChooser paramJFileChooser)
  {
    super.installComponents(paramJFileChooser);
    SynthContext localSynthContext = getContext(paramJFileChooser, 1);
    updateUseShellFolder();
    paramJFileChooser.setLayout(new BorderLayout(0, 11));
    JPanel localJPanel1 = new JPanel(new BorderLayout(11, 0));
    JPanel localJPanel2 = new JPanel();
    localJPanel2.setLayout(new BoxLayout(localJPanel2, 2));
    localJPanel1.add(localJPanel2, "After");
    paramJFileChooser.add(localJPanel1, "North");
    this.lookInLabel = new JLabel(this.lookInLabelText);
    this.lookInLabel.setDisplayedMnemonic(this.lookInLabelMnemonic);
    localJPanel1.add(this.lookInLabel, "Before");
    this.directoryComboBox = new JComboBox();
    this.directoryComboBox.getAccessibleContext().setAccessibleDescription(this.lookInLabelText);
    this.directoryComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    this.lookInLabel.setLabelFor(this.directoryComboBox);
    this.directoryComboBoxModel = createDirectoryComboBoxModel(paramJFileChooser);
    this.directoryComboBox.setModel(this.directoryComboBoxModel);
    this.directoryComboBox.addActionListener(this.directoryComboBoxAction);
    this.directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(paramJFileChooser));
    this.directoryComboBox.setAlignmentX(0F);
    this.directoryComboBox.setAlignmentY(0F);
    this.directoryComboBox.setMaximumRowCount(8);
    localJPanel1.add(this.directoryComboBox, "Center");
    this.filePane = new FilePane(new SynthFileChooserUIAccessor(this, null));
    paramJFileChooser.addPropertyChangeListener(this.filePane);
    JPopupMenu localJPopupMenu = this.filePane.getComponentPopupMenu();
    if (localJPopupMenu != null)
    {
      localJPopupMenu.insert(getChangeToParentDirectoryAction(), 0);
      if (File.separatorChar == '/')
        localJPopupMenu.insert(getGoHomeAction(), 1);
    }
    FileSystemView localFileSystemView = paramJFileChooser.getFileSystemView();
    JButton localJButton1 = new JButton(getChangeToParentDirectoryAction());
    localJButton1.setText(null);
    localJButton1.setIcon(this.upFolderIcon);
    localJButton1.setToolTipText(this.upFolderToolTipText);
    localJButton1.getAccessibleContext().setAccessibleName(this.upFolderAccessibleName);
    localJButton1.setAlignmentX(0F);
    localJButton1.setAlignmentY(0.5F);
    localJButton1.setMargin(shrinkwrap);
    localJPanel2.add(localJButton1);
    localJPanel2.add(Box.createRigidArea(hstrut5));
    File localFile = localFileSystemView.getHomeDirectory();
    String str = this.homeFolderToolTipText;
    if (localFileSystemView.isRoot(localFile))
      str = getFileView(paramJFileChooser).getName(localFile);
    JButton localJButton2 = new JButton(this.homeFolderIcon);
    localJButton2.setToolTipText(str);
    localJButton2.getAccessibleContext().setAccessibleName(this.homeFolderAccessibleName);
    localJButton2.setAlignmentX(0F);
    localJButton2.setAlignmentY(0.5F);
    localJButton2.setMargin(shrinkwrap);
    localJButton2.addActionListener(getGoHomeAction());
    localJPanel2.add(localJButton2);
    localJPanel2.add(Box.createRigidArea(hstrut5));
    if (!(UIManager.getBoolean("FileChooser.readOnly")))
    {
      localJButton2 = new JButton(this.filePane.getNewFolderAction());
      localJButton2.setText(null);
      localJButton2.setIcon(this.newFolderIcon);
      localJButton2.setToolTipText(this.newFolderToolTipText);
      localJButton2.getAccessibleContext().setAccessibleName(this.newFolderAccessibleName);
      localJButton2.setAlignmentX(0F);
      localJButton2.setAlignmentY(0.5F);
      localJButton2.setMargin(shrinkwrap);
      localJPanel2.add(localJButton2);
      localJPanel2.add(Box.createRigidArea(hstrut5));
    }
    ButtonGroup localButtonGroup = new ButtonGroup();
    this.listViewButton = new JToggleButton(this.listViewIcon);
    this.listViewButton.setToolTipText(this.listViewButtonToolTipText);
    this.listViewButton.getAccessibleContext().setAccessibleName(this.listViewButtonAccessibleName);
    this.listViewButton.setSelected(true);
    this.listViewButton.setAlignmentX(0F);
    this.listViewButton.setAlignmentY(0.5F);
    this.listViewButton.setMargin(shrinkwrap);
    this.listViewButton.addActionListener(this.filePane.getViewTypeAction(0));
    localJPanel2.add(this.listViewButton);
    localButtonGroup.add(this.listViewButton);
    this.detailsViewButton = new JToggleButton(this.detailsViewIcon);
    this.detailsViewButton.setToolTipText(this.detailsViewButtonToolTipText);
    this.detailsViewButton.getAccessibleContext().setAccessibleName(this.detailsViewButtonAccessibleName);
    this.detailsViewButton.setAlignmentX(0F);
    this.detailsViewButton.setAlignmentY(0.5F);
    this.detailsViewButton.setMargin(shrinkwrap);
    this.detailsViewButton.addActionListener(this.filePane.getViewTypeAction(1));
    localJPanel2.add(this.detailsViewButton);
    localButtonGroup.add(this.detailsViewButton);
    this.filePane.addPropertyChangeListener(new PropertyChangeListener(this)
    {
      public void propertyChange()
      {
        if ("viewType".equals(paramPropertyChangeEvent.getPropertyName()))
        {
          int i = SynthFileChooserUIImpl.access$500(this.this$0).getViewType();
          switch (i)
          {
          case 0:
            SynthFileChooserUIImpl.access$600(this.this$0).setSelected(true);
            break;
          case 1:
            SynthFileChooserUIImpl.access$700(this.this$0).setSelected(true);
          }
        }
      }
    });
    paramJFileChooser.add(getAccessoryPanel(), "After");
    JComponent localJComponent = paramJFileChooser.getAccessory();
    if (localJComponent != null)
      getAccessoryPanel().add(localJComponent);
    this.filePane.setPreferredSize(LIST_PREF_SIZE);
    paramJFileChooser.add(this.filePane, "Center");
    this.bottomPanel = new JPanel();
    this.bottomPanel.setLayout(new BoxLayout(this.bottomPanel, 1));
    paramJFileChooser.add(this.bottomPanel, "South");
    JPanel localJPanel3 = new JPanel();
    localJPanel3.setLayout(new BoxLayout(localJPanel3, 2));
    this.bottomPanel.add(localJPanel3);
    this.bottomPanel.add(Box.createRigidArea(new Dimension(1, 5)));
    AlignedLabel localAlignedLabel1 = new AlignedLabel(this, this.fileNameLabelText);
    localAlignedLabel1.setDisplayedMnemonic(this.fileNameLabelMnemonic);
    localJPanel3.add(localAlignedLabel1);
    this.fileNameTextField = new JTextField(this, 35)
    {
      public Dimension getMaximumSize()
      {
        return new Dimension(32767, super.getPreferredSize().height);
      }
    };
    localJPanel3.add(this.fileNameTextField);
    localAlignedLabel1.setLabelFor(this.fileNameTextField);
    this.fileNameTextField.addFocusListener(new FocusAdapter(this)
    {
      public void focusGained()
      {
        if (!(this.this$0.getFileChooser().isMultiSelectionEnabled()))
          SynthFileChooserUIImpl.access$500(this.this$0).clearSelection();
      }
    });
    if (paramJFileChooser.isMultiSelectionEnabled())
      setFileName(fileNameString(paramJFileChooser.getSelectedFiles()));
    else
      setFileName(fileNameString(paramJFileChooser.getSelectedFile()));
    JPanel localJPanel4 = new JPanel();
    localJPanel4.setLayout(new BoxLayout(localJPanel4, 2));
    this.bottomPanel.add(localJPanel4);
    AlignedLabel localAlignedLabel2 = new AlignedLabel(this, this.filesOfTypeLabelText);
    localAlignedLabel2.setDisplayedMnemonic(this.filesOfTypeLabelMnemonic);
    localJPanel4.add(localAlignedLabel2);
    this.filterComboBoxModel = createFilterComboBoxModel();
    paramJFileChooser.addPropertyChangeListener(this.filterComboBoxModel);
    this.filterComboBox = new JComboBox(this.filterComboBoxModel);
    this.filterComboBox.getAccessibleContext().setAccessibleDescription(this.filesOfTypeLabelText);
    localAlignedLabel2.setLabelFor(this.filterComboBox);
    this.filterComboBox.setRenderer(createFilterComboBoxRenderer());
    localJPanel4.add(this.filterComboBox);
    this.buttonPanel = new JPanel();
    this.buttonPanel.setLayout(new ButtonAreaLayout(null));
    this.buttonPanel.add(getApproveButton(paramJFileChooser));
    this.buttonPanel.add(getCancelButton(paramJFileChooser));
    if (paramJFileChooser.getControlButtonsAreShown())
      addControlButtons();
    groupLabels(new AlignedLabel[] { localAlignedLabel1, localAlignedLabel2 });
  }

  private void updateUseShellFolder()
  {
    JFileChooser localJFileChooser = getFileChooser();
    Boolean localBoolean = (Boolean)localJFileChooser.getClientProperty("FileChooser.useShellFolder");
    if (localBoolean != null)
    {
      this.useShellFolder = localBoolean.booleanValue();
    }
    else
    {
      this.useShellFolder = false;
      File[] arrayOfFile1 = localJFileChooser.getFileSystemView().getRoots();
      if ((arrayOfFile1 != null) && (arrayOfFile1.length == 1))
      {
        File[] arrayOfFile2 = (File[])(File[])ShellFolder.get("fileChooserComboBoxFolders");
        if ((arrayOfFile2 != null) && (arrayOfFile2.length > 0) && (arrayOfFile1[0] == arrayOfFile2[0]))
          this.useShellFolder = true;
      }
    }
  }

  private String fileNameString(File paramFile)
  {
    if (paramFile == null)
      return null;
    JFileChooser localJFileChooser = getFileChooser();
    if ((localJFileChooser.isDirectorySelectionEnabled()) && (!(localJFileChooser.isFileSelectionEnabled())))
      return paramFile.getPath();
    return paramFile.getName();
  }

  private String fileNameString(File[] paramArrayOfFile)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; (paramArrayOfFile != null) && (i < paramArrayOfFile.length); ++i)
    {
      if (i > 0)
        localStringBuffer.append(" ");
      if (paramArrayOfFile.length > 1)
        localStringBuffer.append("\"");
      localStringBuffer.append(fileNameString(paramArrayOfFile[i]));
      if (paramArrayOfFile.length > 1)
        localStringBuffer.append("\"");
    }
    return localStringBuffer.toString();
  }

  public void uninstallUI(JComponent paramJComponent)
  {
    paramJComponent.removePropertyChangeListener(this.filterComboBoxModel);
    paramJComponent.removePropertyChangeListener(this.filePane);
    if (this.filePane != null)
    {
      this.filePane.uninstallUI();
      this.filePane = null;
    }
    super.uninstallUI(paramJComponent);
  }

  protected void installStrings(JFileChooser paramJFileChooser)
  {
    super.installStrings(paramJFileChooser);
    Locale localLocale = paramJFileChooser.getLocale();
    this.lookInLabelMnemonic = getMnemonic("FileChooser.lookInLabelMnemonic", localLocale);
    this.lookInLabelText = UIManager.getString("FileChooser.lookInLabelText", localLocale);
    this.saveInLabelText = UIManager.getString("FileChooser.saveInLabelText", localLocale);
    this.fileNameLabelMnemonic = getMnemonic("FileChooser.fileNameLabelMnemonic", localLocale);
    this.fileNameLabelText = UIManager.getString("FileChooser.fileNameLabelText", localLocale);
    this.filesOfTypeLabelMnemonic = getMnemonic("FileChooser.filesOfTypeLabelMnemonic", localLocale);
    this.filesOfTypeLabelText = UIManager.getString("FileChooser.filesOfTypeLabelText", localLocale);
    this.upFolderToolTipText = UIManager.getString("FileChooser.upFolderToolTipText", localLocale);
    this.upFolderAccessibleName = UIManager.getString("FileChooser.upFolderAccessibleName", localLocale);
    this.homeFolderToolTipText = UIManager.getString("FileChooser.homeFolderToolTipText", localLocale);
    this.homeFolderAccessibleName = UIManager.getString("FileChooser.homeFolderAccessibleName", localLocale);
    this.newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText", localLocale);
    this.newFolderAccessibleName = UIManager.getString("FileChooser.newFolderAccessibleName", localLocale);
    this.listViewButtonToolTipText = UIManager.getString("FileChooser.listViewButtonToolTipText", localLocale);
    this.listViewButtonAccessibleName = UIManager.getString("FileChooser.listViewButtonAccessibleName", localLocale);
    this.detailsViewButtonToolTipText = UIManager.getString("FileChooser.detailsViewButtonToolTipText", localLocale);
    this.detailsViewButtonAccessibleName = UIManager.getString("FileChooser.detailsViewButtonAccessibleName", localLocale);
  }

  private int getMnemonic(String paramString, Locale paramLocale)
  {
    return SwingUtilities2.getUIDefaultsInt(paramString, paramLocale);
  }

  public String getFileName()
  {
    if (this.fileNameTextField != null)
      return this.fileNameTextField.getText();
    return null;
  }

  public void setFileName(String paramString)
  {
    if (this.fileNameTextField != null)
      this.fileNameTextField.setText(paramString);
  }

  public void rescanCurrentDirectory(JFileChooser paramJFileChooser)
  {
    this.filePane.rescanCurrentDirectory();
  }

  protected void doSelectedFileChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    super.doSelectedFileChanged(paramPropertyChangeEvent);
    File localFile = (File)paramPropertyChangeEvent.getNewValue();
    JFileChooser localJFileChooser = getFileChooser();
    if ((localFile != null) && ((((localJFileChooser.isFileSelectionEnabled()) && (!(localFile.isDirectory()))) || ((localFile.isDirectory()) && (localJFileChooser.isDirectorySelectionEnabled())))))
      setFileName(fileNameString(localFile));
  }

  protected void doSelectedFilesChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    super.doSelectedFilesChanged(paramPropertyChangeEvent);
    File[] arrayOfFile = (File[])(File[])paramPropertyChangeEvent.getNewValue();
    JFileChooser localJFileChooser = getFileChooser();
    if ((arrayOfFile != null) && (arrayOfFile.length > 0) && (((arrayOfFile.length > 1) || (localJFileChooser.isDirectorySelectionEnabled()) || (!(arrayOfFile[0].isDirectory())))))
      setFileName(fileNameString(arrayOfFile));
  }

  protected void doDirectoryChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    super.doDirectoryChanged(paramPropertyChangeEvent);
    JFileChooser localJFileChooser = getFileChooser();
    FileSystemView localFileSystemView = localJFileChooser.getFileSystemView();
    File localFile = getFileChooser().getCurrentDirectory();
    if (localFile != null)
    {
      JComponent localJComponent = getDirectoryComboBox();
      if (localJComponent instanceof JComboBox)
      {
        ComboBoxModel localComboBoxModel = ((JComboBox)localJComponent).getModel();
        if (localComboBoxModel instanceof DirectoryComboBoxModel)
          ((DirectoryComboBoxModel)localComboBoxModel).addItem(localFile);
      }
      if ((localJFileChooser.isDirectorySelectionEnabled()) && (!(localJFileChooser.isFileSelectionEnabled())))
        if (localFileSystemView.isFileSystem(localFile))
          setFileName(localFile.getPath());
        else
          setFileName(null);
    }
  }

  protected void doFileSelectionModeChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    super.doFileSelectionModeChanged(paramPropertyChangeEvent);
    JFileChooser localJFileChooser = getFileChooser();
    File localFile = localJFileChooser.getCurrentDirectory();
    if ((localFile != null) && (localJFileChooser.isDirectorySelectionEnabled()) && (!(localJFileChooser.isFileSelectionEnabled())) && (localJFileChooser.getFileSystemView().isFileSystem(localFile)))
      setFileName(localFile.getPath());
    else
      setFileName(null);
  }

  protected void doAccessoryChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    if (getAccessoryPanel() != null)
    {
      if (paramPropertyChangeEvent.getOldValue() != null)
        getAccessoryPanel().remove((JComponent)paramPropertyChangeEvent.getOldValue());
      JComponent localJComponent = (JComponent)paramPropertyChangeEvent.getNewValue();
      if (localJComponent != null)
        getAccessoryPanel().add(localJComponent, "Center");
    }
  }

  protected void doControlButtonsChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    super.doControlButtonsChanged(paramPropertyChangeEvent);
    if (getFileChooser().getControlButtonsAreShown())
      addControlButtons();
    else
      removeControlButtons();
  }

  protected void addControlButtons()
  {
    if (this.bottomPanel != null)
      this.bottomPanel.add(this.buttonPanel);
  }

  protected void removeControlButtons()
  {
    if (this.bottomPanel != null)
      this.bottomPanel.remove(this.buttonPanel);
  }

  protected ActionMap createActionMap()
  {
    ActionMapUIResource localActionMapUIResource = new ActionMapUIResource();
    FilePane.addActionsToMap(localActionMapUIResource, this.filePane.getActions());
    localActionMapUIResource.put("fileNameCompletion", getFileNameCompletionAction());
    return localActionMapUIResource;
  }

  protected JComponent getDirectoryComboBox()
  {
    return this.directoryComboBox;
  }

  protected Action getDirectoryComboBoxAction()
  {
    return this.directoryComboBoxAction;
  }

  protected DirectoryComboBoxRenderer createDirectoryComboBoxRenderer(JFileChooser paramJFileChooser)
  {
    return new DirectoryComboBoxRenderer(this, this.directoryComboBox.getRenderer(), null);
  }

  protected DirectoryComboBoxModel createDirectoryComboBoxModel(JFileChooser paramJFileChooser)
  {
    return new DirectoryComboBoxModel(this);
  }

  protected FilterComboBoxRenderer createFilterComboBoxRenderer()
  {
    return new FilterComboBoxRenderer(this, this.filterComboBox.getRenderer(), null);
  }

  protected FilterComboBoxModel createFilterComboBoxModel()
  {
    return new FilterComboBoxModel(this);
  }

  private static void groupLabels(AlignedLabel[] paramArrayOfAlignedLabel)
  {
    for (int i = 0; i < paramArrayOfAlignedLabel.length; ++i)
      AlignedLabel.access$1302(paramArrayOfAlignedLabel[i], paramArrayOfAlignedLabel);
  }

  private class AlignedLabel extends JLabel
  {
    private AlignedLabel[] group;
    private int maxWidth = 0;

    AlignedLabel(, String paramString)
    {
      super(paramString);
      setAlignmentX(0F);
    }

    public Dimension getPreferredSize()
    {
      Dimension localDimension = super.getPreferredSize();
      return new Dimension(getMaxWidth() + 11, localDimension.height);
    }

    private int getMaxWidth()
    {
      if ((this.maxWidth == 0) && (this.group != null))
      {
        int i = 0;
        for (int j = 0; j < this.group.length; ++j)
          i = Math.max(this.group[j].getSuperPreferredWidth(), i);
        for (j = 0; j < this.group.length; ++j)
          this.group[j].maxWidth = i;
      }
      return this.maxWidth;
    }

    private int getSuperPreferredWidth()
    {
      return super.getPreferredSize().width;
    }
  }

  private static class ButtonAreaLayout
  implements LayoutManager
  {
    private int hGap = 5;
    private int topMargin = 17;

    public void addLayoutComponent(String paramString, Component paramComponent)
    {
    }

    public void layoutContainer(Container paramContainer)
    {
      Component[] arrayOfComponent = paramContainer.getComponents();
      if ((arrayOfComponent != null) && (arrayOfComponent.length > 0))
      {
        int i1;
        int i = arrayOfComponent.length;
        Dimension[] arrayOfDimension = new Dimension[i];
        Insets localInsets = paramContainer.getInsets();
        int j = localInsets.top + this.topMargin;
        int k = 0;
        for (int l = 0; l < i; ++l)
        {
          arrayOfDimension[l] = arrayOfComponent[l].getPreferredSize();
          k = Math.max(k, arrayOfDimension[l].width);
        }
        if (paramContainer.getComponentOrientation().isLeftToRight())
        {
          l = paramContainer.getSize().width - localInsets.left - k;
          i1 = this.hGap + k;
        }
        else
        {
          l = localInsets.left;
          i1 = -(this.hGap + k);
        }
        for (int i2 = i - 1; i2 >= 0; --i2)
        {
          arrayOfComponent[i2].setBounds(l, j, k, arrayOfDimension[i2].height);
          l -= i1;
        }
      }
    }

    public Dimension minimumLayoutSize(Container paramContainer)
    {
      if (paramContainer != null)
      {
        Component[] arrayOfComponent = paramContainer.getComponents();
        if ((arrayOfComponent != null) && (arrayOfComponent.length > 0))
        {
          int i = arrayOfComponent.length;
          int j = 0;
          Insets localInsets = paramContainer.getInsets();
          int k = this.topMargin + localInsets.top + localInsets.bottom;
          int l = localInsets.left + localInsets.right;
          int i1 = 0;
          for (int i2 = 0; i2 < i; ++i2)
          {
            Dimension localDimension = arrayOfComponent[i2].getPreferredSize();
            j = Math.max(j, localDimension.height);
            i1 = Math.max(i1, localDimension.width);
          }
          return new Dimension(l + i * i1 + (i - 1) * this.hGap, k + j);
        }
      }
      return new Dimension(0, 0);
    }

    public Dimension preferredLayoutSize(Container paramContainer)
    {
      return minimumLayoutSize(paramContainer);
    }

    public void removeLayoutComponent(Component paramComponent)
    {
    }
  }

  protected class DirectoryComboBoxAction extends AbstractAction
  {
    protected DirectoryComboBoxAction()
    {
      super("DirectoryComboBoxAction");
    }

    public void actionPerformed()
    {
      SynthFileChooserUIImpl.access$1100(this.this$0).hidePopup();
      JComponent localJComponent = this.this$0.getDirectoryComboBox();
      if (localJComponent instanceof JComboBox)
      {
        File localFile = (File)((JComboBox)localJComponent).getSelectedItem();
        this.this$0.getFileChooser().setCurrentDirectory(localFile);
      }
    }
  }

  protected class DirectoryComboBoxModel extends AbstractListModel
  implements ComboBoxModel
  {
    Vector directories = new Vector();
    int[] depths = null;
    File selectedDirectory = null;
    JFileChooser chooser = this.this$0.getFileChooser();
    FileSystemView fsv = this.chooser.getFileSystemView();

    public DirectoryComboBoxModel()
    {
      File localFile = paramSynthFileChooserUIImpl.getFileChooser().getCurrentDirectory();
      if (localFile != null)
        addItem(localFile);
    }

    public void addItem()
    {
      File[] arrayOfFile;
      if (paramFile == null)
        return;
      int i = this.directories.size();
      this.directories.clear();
      if (i > 0)
        fireIntervalRemoved(this, 0, i);
      if (SynthFileChooserUIImpl.access$300(this.this$0))
        arrayOfFile = (File[])(File[])ShellFolder.get("fileChooserComboBoxFolders");
      else
        arrayOfFile = this.fsv.getRoots();
      this.directories.addAll(Arrays.asList(arrayOfFile));
      File localFile1 = null;
      try
      {
        localFile1 = paramFile.getCanonicalFile();
      }
      catch (IOException localIOException)
      {
        localFile1 = paramFile;
      }
      try
      {
        File localFile2 = (SynthFileChooserUIImpl.access$300(this.this$0)) ? ShellFolder.getShellFolder(localFile1) : localFile1;
        File localFile3 = localFile2;
        Vector localVector = new Vector(10);
        do
          localVector.addElement(localFile3);
        while ((localFile3 = localFile3.getParentFile()) != null);
        int j = localVector.size();
        for (int k = 0; k < j; ++k)
        {
          localFile3 = (File)localVector.get(k);
          if (this.directories.contains(localFile3))
          {
            int l = this.directories.indexOf(localFile3);
            for (int i1 = k - 1; i1 >= 0; --i1)
              this.directories.insertElementAt(localVector.get(i1), l + k - i1);
            break;
          }
        }
        calculateDepths();
        setSelectedItem(localFile2);
      }
      catch (FileNotFoundException localFileNotFoundException)
      {
        calculateDepths();
      }
    }

    private void calculateDepths()
    {
      this.depths = new int[this.directories.size()];
      for (int i = 0; i < this.depths.length; ++i)
      {
        File localFile1 = (File)this.directories.get(i);
        File localFile2 = localFile1.getParentFile();
        this.depths[i] = 0;
        if (localFile2 != null)
          for (int j = i - 1; j >= 0; --j)
            if (localFile2.equals((File)this.directories.get(j)))
            {
              this.depths[i] = (this.depths[j] + 1);
              break;
            }
      }
    }

    public int getDepth()
    {
      return (((this.depths != null) && (paramInt >= 0) && (paramInt < this.depths.length)) ? this.depths[paramInt] : 0);
    }

    public void setSelectedItem()
    {
      this.selectedDirectory = ((File)paramObject);
      fireContentsChanged(this, -1, -1);
    }

    public Object getSelectedItem()
    {
      return this.selectedDirectory;
    }

    public int getSize()
    {
      return this.directories.size();
    }

    public Object getElementAt()
    {
      return this.directories.elementAt(paramInt);
    }
  }

  private class DirectoryComboBoxRenderer
  implements ListCellRenderer
  {
    private ListCellRenderer delegate;
    SynthFileChooserUIImpl.IndentIcon ii = new SynthFileChooserUIImpl.IndentIcon(this.this$0);

    private DirectoryComboBoxRenderer(, ListCellRenderer paramListCellRenderer)
    {
      this.delegate = paramListCellRenderer;
    }

    public Component getListCellRendererComponent(, Object paramObject, int paramInt, boolean paramBoolean1, boolean paramBoolean2)
    {
      Component localComponent = this.delegate.getListCellRendererComponent(paramJList, paramObject, paramInt, paramBoolean1, paramBoolean2);
      if ((!($assertionsDisabled)) && (!(localComponent instanceof JLabel)))
        throw new AssertionError();
      JLabel localJLabel = (JLabel)localComponent;
      if (paramObject == null)
      {
        localJLabel.setText("");
        return localJLabel;
      }
      File localFile = (File)paramObject;
      localJLabel.setText(this.this$0.getFileChooser().getName(localFile));
      Icon localIcon = this.this$0.getFileChooser().getIcon(localFile);
      this.ii.icon = localIcon;
      this.ii.depth = SynthFileChooserUIImpl.access$1000(this.this$0).getDepth(paramInt);
      localJLabel.setIcon(this.ii);
      return localJLabel;
    }
  }

  protected class FilterComboBoxModel extends AbstractListModel
  implements ComboBoxModel, PropertyChangeListener
  {
    protected FileFilter[] filters;

    protected FilterComboBoxModel()
    {
      this.filters = paramSynthFileChooserUIImpl.getFileChooser().getChoosableFileFilters();
    }

    public void propertyChange()
    {
      String str = paramPropertyChangeEvent.getPropertyName();
      if (str == "ChoosableFileFilterChangedProperty")
      {
        this.filters = ((FileFilter[])(FileFilter[])paramPropertyChangeEvent.getNewValue());
        fireContentsChanged(this, -1, -1);
      }
      else if (str == "fileFilterChanged")
      {
        fireContentsChanged(this, -1, -1);
      }
    }

    public void setSelectedItem()
    {
      if (paramObject != null)
      {
        this.this$0.getFileChooser().setFileFilter((FileFilter)paramObject);
        fireContentsChanged(this, -1, -1);
      }
    }

    public Object getSelectedItem()
    {
      FileFilter localFileFilter = this.this$0.getFileChooser().getFileFilter();
      int i = 0;
      if (localFileFilter != null)
      {
        for (int j = 0; j < this.filters.length; ++j)
          if (this.filters[j] == localFileFilter)
            i = 1;
        if (i == 0)
          this.this$0.getFileChooser().addChoosableFileFilter(localFileFilter);
      }
      return this.this$0.getFileChooser().getFileFilter();
    }

    public int getSize()
    {
      if (this.filters != null)
        return this.filters.length;
      return 0;
    }

    public Object getElementAt()
    {
      if (paramInt > getSize() - 1)
        return this.this$0.getFileChooser().getFileFilter();
      if (this.filters != null)
        return this.filters[paramInt];
      return null;
    }
  }

  public class FilterComboBoxRenderer
  implements ListCellRenderer
  {
    private ListCellRenderer delegate;

    private FilterComboBoxRenderer(, ListCellRenderer paramListCellRenderer)
    {
      this.delegate = paramListCellRenderer;
    }

    public Component getListCellRendererComponent(, Object paramObject, int paramInt, boolean paramBoolean1, boolean paramBoolean2)
    {
      Component localComponent = this.delegate.getListCellRendererComponent(paramJList, paramObject, paramInt, paramBoolean1, paramBoolean2);
      String str = null;
      if ((paramObject != null) && (paramObject instanceof FileFilter))
        str = ((FileFilter)paramObject).getDescription();
      if ((!($assertionsDisabled)) && (!(localComponent instanceof JLabel)))
        throw new AssertionError();
      if (str != null)
        ((JLabel)localComponent).setText(str);
      return localComponent;
    }
  }

  class IndentIcon
  implements Icon
  {
    Icon icon = null;
    int depth = 0;

    public void paintIcon(, Graphics paramGraphics, int paramInt1, int paramInt2)
    {
      if (this.icon != null)
        if (paramComponent.getComponentOrientation().isLeftToRight())
          this.icon.paintIcon(paramComponent, paramGraphics, paramInt1 + this.depth * 10, paramInt2);
        else
          this.icon.paintIcon(paramComponent, paramGraphics, paramInt1, paramInt2);
    }

    public int getIconWidth()
    {
      return (((this.icon != null) ? this.icon.getIconWidth() : 0) + this.depth * 10);
    }

    public int getIconHeight()
    {
      return ((this.icon != null) ? this.icon.getIconHeight() : 0);
    }
  }

  private class SynthFileChooserUIAccessor
  implements FilePane.FileChooserUIAccessor
  {
    public JFileChooser getFileChooser()
    {
      return this.this$0.getFileChooser();
    }

    public BasicDirectoryModel getModel()
    {
      return this.this$0.getModel();
    }

    public JPanel createList()
    {
      return null;
    }

    public JPanel createDetailsView()
    {
      return null;
    }

    public boolean isDirectorySelected()
    {
      return SynthFileChooserUIImpl.access$000(this.this$0);
    }

    public File getDirectory()
    {
      return SynthFileChooserUIImpl.access$100(this.this$0);
    }

    public Action getChangeToParentDirectoryAction()
    {
      return this.this$0.getChangeToParentDirectoryAction();
    }

    public Action getApproveSelectionAction()
    {
      return this.this$0.getApproveSelectionAction();
    }

    public Action getNewFolderAction()
    {
      return this.this$0.getNewFolderAction();
    }

    public MouseListener createDoubleClickListener()
    {
      return SynthFileChooserUIImpl.access$200(this.this$0, getFileChooser(), paramJList);
    }

    public ListSelectionListener createListSelectionListener()
    {
      return this.this$0.createListSelectionListener(getFileChooser());
    }

    public boolean usesShellFolder()
    {
      return SynthFileChooserUIImpl.access$300(this.this$0);
    }
  }
}