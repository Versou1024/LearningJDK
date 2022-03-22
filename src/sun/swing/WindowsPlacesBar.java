package sun.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import sun.awt.shell.ShellFolder;

public class WindowsPlacesBar extends JToolBar
  implements ActionListener, PropertyChangeListener
{
  JFileChooser fc;
  JToggleButton[] buttons;
  ButtonGroup buttonGroup;
  File[] files;
  final Dimension buttonSize;

  public WindowsPlacesBar(JFileChooser paramJFileChooser, boolean paramBoolean)
  {
    super(1);
    this.fc = paramJFileChooser;
    setFloatable(false);
    putClientProperty("JToolBar.isRollover", Boolean.TRUE);
    int i = ((java.lang.System.getProperty("os.name").startsWith("Windows")) && (java.lang.System.getProperty("os.version").compareTo("5.1") >= 0)) ? 1 : 0;
    if (paramBoolean)
    {
      this.buttonSize = new Dimension(83, 69);
      putClientProperty("XPStyle.subAppName", "placesbar");
      setBorder(new EmptyBorder(1, 1, 1, 1));
    }
    else
    {
      this.buttonSize = new Dimension(83, 54);
      setBorder(new BevelBorder(1, UIManager.getColor("ToolBar.highlight"), UIManager.getColor("ToolBar.background"), UIManager.getColor("ToolBar.darkShadow"), UIManager.getColor("ToolBar.shadow")));
    }
    Color localColor = new Color(UIManager.getColor("ToolBar.shadow").getRGB());
    setBackground(localColor);
    FileSystemView localFileSystemView = paramJFileChooser.getFileSystemView();
    this.files = ((File[])(File[])ShellFolder.get("fileChooserShortcutPanelFolders"));
    this.buttons = new JToggleButton[this.files.length];
    this.buttonGroup = new ButtonGroup();
    for (int j = 0; j < this.files.length; ++j)
    {
      Object localObject2;
      if (localFileSystemView.isFileSystemRoot(this.files[j]))
        this.files[j] = localFileSystemView.createFileObject(this.files[j].getAbsolutePath());
      String str = localFileSystemView.getSystemDisplayName(this.files[j]);
      int k = str.lastIndexOf(File.separatorChar);
      if ((k >= 0) && (k < str.length() - 1))
        str = str.substring(k + 1);
      Object localObject1 = null;
      if (this.files[j] instanceof ShellFolder)
      {
        localObject2 = (ShellFolder)this.files[j];
        localObject1 = new ImageIcon(((ShellFolder)localObject2).getIcon(true), ((ShellFolder)localObject2).getFolderType());
      }
      else
      {
        localObject1 = localFileSystemView.getSystemIcon(this.files[j]);
      }
      this.buttons[j] = new JToggleButton(str, (Icon)localObject1);
      if (i != 0)
      {
        this.buttons[j].setIconTextGap(2);
        this.buttons[j].setMargin(new Insets(2, 2, 2, 2));
        this.buttons[j].setText("<html><center>" + str + "</center></html>");
      }
      if (paramBoolean)
      {
        this.buttons[j].putClientProperty("XPStyle.subAppName", "placesbar");
      }
      else
      {
        localObject2 = new Color(UIManager.getColor("List.selectionForeground").getRGB());
        this.buttons[j].setContentAreaFilled(false);
        this.buttons[j].setForeground((Color)localObject2);
      }
      this.buttons[j].setHorizontalTextPosition(0);
      this.buttons[j].setVerticalTextPosition(3);
      this.buttons[j].setAlignmentX(0.5F);
      this.buttons[j].setPreferredSize(this.buttonSize);
      this.buttons[j].setMaximumSize(this.buttonSize);
      this.buttons[j].addActionListener(this);
      add(this.buttons[j]);
      if ((j < this.files.length - 1) && (paramBoolean))
        add(Box.createRigidArea(new Dimension(1, 1)));
      this.buttonGroup.add(this.buttons[j]);
    }
    doDirectoryChanged(paramJFileChooser.getCurrentDirectory());
  }

  protected void doDirectoryChanged(File paramFile)
  {
    for (int i = 0; i < this.buttons.length; ++i)
    {
      JToggleButton localJToggleButton = this.buttons[i];
      if (this.files[i].equals(paramFile))
      {
        localJToggleButton.setSelected(true);
        return;
      }
      if (localJToggleButton.isSelected())
      {
        this.buttonGroup.remove(localJToggleButton);
        localJToggleButton.setSelected(false);
        this.buttonGroup.add(localJToggleButton);
      }
    }
  }

  public void propertyChange(PropertyChangeEvent paramPropertyChangeEvent)
  {
    String str = paramPropertyChangeEvent.getPropertyName();
    if (str == "directoryChanged")
      doDirectoryChanged(this.fc.getCurrentDirectory());
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    JToggleButton localJToggleButton = (JToggleButton)paramActionEvent.getSource();
    for (int i = 0; i < this.buttons.length; ++i)
      if (localJToggleButton == this.buttons[i])
      {
        this.fc.setCurrentDirectory(this.files[i]);
        return;
      }
  }

  public Dimension getPreferredSize()
  {
    Dimension localDimension1 = super.getMinimumSize();
    Dimension localDimension2 = super.getPreferredSize();
    int i = localDimension1.height;
    if ((this.buttons != null) && (this.buttons.length > 0) && (this.buttons.length < 5))
    {
      JToggleButton localJToggleButton = this.buttons[0];
      if (localJToggleButton != null)
      {
        int j = 5 * (localJToggleButton.getPreferredSize().height + 1);
        if (j > i)
          i = j;
      }
    }
    if (i > localDimension2.height)
      localDimension2 = new Dimension(localDimension2.width, i);
    return localDimension2;
  }
}