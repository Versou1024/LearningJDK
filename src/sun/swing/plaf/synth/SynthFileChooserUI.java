package sun.swing.plaf.synth;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicDirectoryModel;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;
import sun.swing.FilePane;

public abstract class SynthFileChooserUI extends BasicFileChooserUI
  implements SynthUI
{
  private JButton approveButton;
  private JButton cancelButton;
  private SynthStyle style;
  private Action fileNameCompletionAction = new FileNameCompletionAction(this);
  private FileFilter actualFileFilter = null;
  private GlobFilter globFilter = null;
  private boolean readOnly;
  private String fileNameCompletionString;

  public static ComponentUI createUI(JComponent paramJComponent)
  {
    return new SynthFileChooserUIImpl((JFileChooser)paramJComponent);
  }

  public SynthFileChooserUI(JFileChooser paramJFileChooser)
  {
    super(paramJFileChooser);
  }

  public SynthContext getContext(JComponent paramJComponent)
  {
    return new SynthContext(paramJComponent, Region.FILE_CHOOSER, this.style, getComponentState(paramJComponent));
  }

  protected SynthContext getContext(JComponent paramJComponent, int paramInt)
  {
    Region localRegion = SynthLookAndFeel.getRegion(paramJComponent);
    return new SynthContext(paramJComponent, Region.FILE_CHOOSER, this.style, paramInt);
  }

  private Region getRegion(JComponent paramJComponent)
  {
    return SynthLookAndFeel.getRegion(paramJComponent);
  }

  private int getComponentState(JComponent paramJComponent)
  {
    if (paramJComponent.isEnabled())
    {
      if (paramJComponent.isFocusOwner())
        return 257;
      return 1;
    }
    return 8;
  }

  private void updateStyle(JComponent paramJComponent)
  {
    SynthStyle localSynthStyle = SynthLookAndFeel.getStyleFactory().getStyle(paramJComponent, Region.FILE_CHOOSER);
    if (localSynthStyle != this.style)
    {
      if (this.style != null)
        this.style.uninstallDefaults(getContext(paramJComponent, 1));
      this.style = localSynthStyle;
      SynthContext localSynthContext = getContext(paramJComponent, 1);
      this.style.installDefaults(localSynthContext);
      Border localBorder = paramJComponent.getBorder();
      if ((localBorder == null) || (localBorder instanceof UIResource))
        paramJComponent.setBorder(new UIBorder(this, this.style.getInsets(localSynthContext, null)));
      this.directoryIcon = this.style.getIcon(localSynthContext, "FileView.directoryIcon");
      this.fileIcon = this.style.getIcon(localSynthContext, "FileView.fileIcon");
      this.computerIcon = this.style.getIcon(localSynthContext, "FileView.computerIcon");
      this.hardDriveIcon = this.style.getIcon(localSynthContext, "FileView.hardDriveIcon");
      this.floppyDriveIcon = this.style.getIcon(localSynthContext, "FileView.floppyDriveIcon");
      this.newFolderIcon = this.style.getIcon(localSynthContext, "FileChooser.newFolderIcon");
      this.upFolderIcon = this.style.getIcon(localSynthContext, "FileChooser.upFolderIcon");
      this.homeFolderIcon = this.style.getIcon(localSynthContext, "FileChooser.homeFolderIcon");
      this.detailsViewIcon = this.style.getIcon(localSynthContext, "FileChooser.detailsViewIcon");
      this.listViewIcon = this.style.getIcon(localSynthContext, "FileChooser.listViewIcon");
    }
  }

  public void installUI(JComponent paramJComponent)
  {
    super.installUI(paramJComponent);
    SwingUtilities.replaceUIActionMap(paramJComponent, createActionMap());
  }

  public void installComponents(JFileChooser paramJFileChooser)
  {
    SynthContext localSynthContext = getContext(paramJFileChooser, 1);
    this.cancelButton = new JButton(this.cancelButtonText);
    this.cancelButton.setName("SynthFileChooser.cancelButton");
    this.cancelButton.setIcon(localSynthContext.getStyle().getIcon(localSynthContext, "FileChooser.cancelIcon"));
    this.cancelButton.setMnemonic(this.cancelButtonMnemonic);
    this.cancelButton.setToolTipText(this.cancelButtonToolTipText);
    this.cancelButton.addActionListener(getCancelSelectionAction());
    this.approveButton = new JButton(getApproveButtonText(paramJFileChooser));
    this.approveButton.setName("SynthFileChooser.approveButton");
    this.approveButton.setIcon(localSynthContext.getStyle().getIcon(localSynthContext, "FileChooser.okIcon"));
    this.approveButton.setMnemonic(getApproveButtonMnemonic(paramJFileChooser));
    this.approveButton.setToolTipText(getApproveButtonToolTipText(paramJFileChooser));
    this.approveButton.addActionListener(getApproveSelectionAction());
  }

  public void uninstallComponents(JFileChooser paramJFileChooser)
  {
    paramJFileChooser.removeAll();
  }

  protected void installListeners(JFileChooser paramJFileChooser)
  {
    super.installListeners(paramJFileChooser);
    getModel().addListDataListener(new ListDataListener(this)
    {
      public void contentsChanged()
      {
        new SynthFileChooserUI.DelayedSelectionUpdater(this.this$0);
      }

      public void intervalAdded()
      {
        new SynthFileChooserUI.DelayedSelectionUpdater(this.this$0);
      }

      public void intervalRemoved()
      {
      }
    });
  }

  protected abstract ActionMap createActionMap();

  protected void installDefaults(JFileChooser paramJFileChooser)
  {
    super.installDefaults(paramJFileChooser);
    updateStyle(paramJFileChooser);
    this.readOnly = UIManager.getBoolean("FileChooser.readOnly");
  }

  protected void uninstallDefaults(JFileChooser paramJFileChooser)
  {
    super.uninstallDefaults(paramJFileChooser);
    SynthContext localSynthContext = getContext(getFileChooser(), 1);
    this.style.uninstallDefaults(localSynthContext);
    this.style = null;
  }

  protected void installIcons(JFileChooser paramJFileChooser)
  {
  }

  public void update(Graphics paramGraphics, JComponent paramJComponent)
  {
    SynthContext localSynthContext = getContext(paramJComponent);
    if (paramJComponent.isOpaque())
    {
      paramGraphics.setColor(this.style.getColor(localSynthContext, ColorType.BACKGROUND));
      paramGraphics.fillRect(0, 0, paramJComponent.getWidth(), paramJComponent.getHeight());
    }
    this.style.getPainter(localSynthContext).paintFileChooserBackground(localSynthContext, paramGraphics, 0, 0, paramJComponent.getWidth(), paramJComponent.getHeight());
    paint(localSynthContext, paramGraphics);
  }

  public void paintBorder(SynthContext paramSynthContext, Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
  }

  public void paint(Graphics paramGraphics, JComponent paramJComponent)
  {
    SynthContext localSynthContext = getContext(paramJComponent);
    paint(localSynthContext, paramGraphics);
  }

  protected void paint(SynthContext paramSynthContext, Graphics paramGraphics)
  {
  }

  public abstract void setFileName(String paramString);

  public abstract String getFileName();

  protected void doSelectedFileChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
  }

  protected void doSelectedFilesChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
  }

  protected void doDirectoryChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    File localFile = getFileChooser().getCurrentDirectory();
    if ((!(this.readOnly)) && (localFile != null))
      getNewFolderAction().setEnabled(FilePane.canWrite(localFile));
  }

  protected void doAccessoryChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
  }

  protected void doFileSelectionModeChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
  }

  protected void doMultiSelectionChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    if (!(getFileChooser().isMultiSelectionEnabled()))
      getFileChooser().setSelectedFiles(null);
  }

  protected void doControlButtonsChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    if (getFileChooser().getControlButtonsAreShown())
    {
      this.approveButton.setText(getApproveButtonText(getFileChooser()));
      this.approveButton.setToolTipText(getApproveButtonToolTipText(getFileChooser()));
    }
  }

  protected void doAncestorChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
  }

  public PropertyChangeListener createPropertyChangeListener(JFileChooser paramJFileChooser)
  {
    return new SynthFCPropertyChangeListener(this, null);
  }

  private void updateFileNameCompletion()
  {
    if ((this.fileNameCompletionString != null) && (this.fileNameCompletionString.equals(getFileName())))
    {
      File[] arrayOfFile = (File[])(File[])getModel().getFiles().toArray(new File[0]);
      String str = getCommonStartString(arrayOfFile);
      if ((str != null) && (str.startsWith(this.fileNameCompletionString)))
        setFileName(str);
      this.fileNameCompletionString = null;
    }
  }

  private String getCommonStartString(File[] paramArrayOfFile)
  {
    Object localObject = null;
    String str1 = null;
    int i = 0;
    if (paramArrayOfFile.length == 0)
      return null;
    while (true)
    {
      for (int j = 0; j < paramArrayOfFile.length; ++j)
      {
        String str2 = paramArrayOfFile[j].getName();
        if (j == 0)
        {
          if (str2.length() == i)
            return localObject;
          str1 = str2.substring(0, i + 1);
        }
        if (!(str2.startsWith(str1)))
          return localObject;
      }
      localObject = str1;
      ++i;
    }
  }

  private void resetGlobFilter()
  {
    if (this.actualFileFilter != null)
    {
      JFileChooser localJFileChooser = getFileChooser();
      FileFilter localFileFilter = localJFileChooser.getFileFilter();
      if ((localFileFilter != null) && (localFileFilter.equals(this.globFilter)))
      {
        localJFileChooser.setFileFilter(this.actualFileFilter);
        localJFileChooser.removeChoosableFileFilter(this.globFilter);
      }
      this.actualFileFilter = null;
    }
  }

  private static boolean isGlobPattern(String paramString)
  {
    return (((File.separatorChar == '\\') && (paramString.indexOf(42) >= 0)) || ((File.separatorChar == '/') && (((paramString.indexOf(42) >= 0) || (paramString.indexOf(63) >= 0) || (paramString.indexOf(91) >= 0)))));
  }

  public Action getFileNameCompletionAction()
  {
    return this.fileNameCompletionAction;
  }

  protected JButton getApproveButton(JFileChooser paramJFileChooser)
  {
    return this.approveButton;
  }

  protected JButton getCancelButton(JFileChooser paramJFileChooser)
  {
    return this.cancelButton;
  }

  public void clearIconCache()
  {
  }

  private class DelayedSelectionUpdater
  implements Runnable
  {
    DelayedSelectionUpdater()
    {
      SwingUtilities.invokeLater(this);
    }

    public void run()
    {
      SynthFileChooserUI.access$000(this.this$0);
    }
  }

  private class FileNameCompletionAction extends AbstractAction
  {
    protected FileNameCompletionAction()
    {
      super("fileNameCompletion");
    }

    public void actionPerformed()
    {
      JFileChooser localJFileChooser = this.this$0.getFileChooser();
      String str = this.this$0.getFileName();
      if (str != null)
        str = str.trim();
      SynthFileChooserUI.access$200(this.this$0);
      if ((str == null) || (str.equals("")) || ((localJFileChooser.isMultiSelectionEnabled()) && (str.startsWith("\""))))
        return;
      FileFilter localFileFilter = localJFileChooser.getFileFilter();
      if (SynthFileChooserUI.access$300(this.this$0) == null)
        SynthFileChooserUI.access$302(this.this$0, new SynthFileChooserUI.GlobFilter(this.this$0));
      try
      {
        SynthFileChooserUI.access$300(this.this$0).setPattern((!(SynthFileChooserUI.access$400(str))) ? str + "*" : str);
        if (!(localFileFilter instanceof SynthFileChooserUI.GlobFilter))
          SynthFileChooserUI.access$502(this.this$0, localFileFilter);
        localJFileChooser.setFileFilter(null);
        localJFileChooser.setFileFilter(SynthFileChooserUI.access$300(this.this$0));
        SynthFileChooserUI.access$602(this.this$0, str);
      }
      catch (PatternSyntaxException localPatternSyntaxException)
      {
      }
    }
  }

  class GlobFilter extends FileFilter
  {
    Pattern pattern;
    String globPattern;

    public void setPattern()
    {
      int l;
      char[] arrayOfChar1 = paramString.toCharArray();
      char[] arrayOfChar2 = new char[arrayOfChar1.length * 2];
      int i = (File.separatorChar == '\\') ? 1 : 0;
      int j = 0;
      StringBuffer localStringBuffer = new StringBuffer();
      int k = 0;
      this.globPattern = paramString;
      if (i != 0)
      {
        l = arrayOfChar1.length;
        if (paramString.endsWith("*.*"))
          l -= 2;
        for (int i1 = 0; i1 < l; ++i1)
        {
          if (arrayOfChar1[i1] == '*')
            arrayOfChar2[(k++)] = '.';
          arrayOfChar2[(k++)] = arrayOfChar1[i1];
        }
      }
      else
      {
        for (l = 0; l < arrayOfChar1.length; ++l)
          switch (arrayOfChar1[l])
          {
          case '*':
            if (j == 0)
              arrayOfChar2[(k++)] = '.';
            arrayOfChar2[(k++)] = '*';
            break;
          case '?':
            arrayOfChar2[(k++)] = ((j != 0) ? 63 : '.');
            break;
          case '[':
            j = 1;
            arrayOfChar2[(k++)] = arrayOfChar1[l];
            if (l < arrayOfChar1.length - 1)
              switch (arrayOfChar1[(l + 1)])
              {
              case '!':
              case '^':
                arrayOfChar2[(k++)] = '^';
                ++l;
                break;
              case ']':
                arrayOfChar2[(k++)] = arrayOfChar1[(++l)];
              }
            break;
          case ']':
            arrayOfChar2[(k++)] = arrayOfChar1[l];
            j = 0;
            break;
          case '\\':
            if ((l == 0) && (arrayOfChar1.length > 1) && (arrayOfChar1[1] == '~'))
            {
              arrayOfChar2[(k++)] = arrayOfChar1[(++l)];
            }
            else
            {
              arrayOfChar2[(k++)] = '\\';
              if ((l < arrayOfChar1.length - 1) && ("*?[]".indexOf(arrayOfChar1[(l + 1)]) >= 0))
                arrayOfChar2[(k++)] = arrayOfChar1[(++l)];
              else
                arrayOfChar2[(k++)] = '\\';
            }
            break;
          default:
            if (!(Character.isLetterOrDigit(arrayOfChar1[l])))
              arrayOfChar2[(k++)] = '\\';
            arrayOfChar2[(k++)] = arrayOfChar1[l];
          }
      }
      this.pattern = Pattern.compile(new String(arrayOfChar2, 0, k), 2);
    }

    public boolean accept()
    {
      if (paramFile == null)
        return false;
      if (paramFile.isDirectory())
        return true;
      return this.pattern.matcher(paramFile.getName()).matches();
    }

    public String getDescription()
    {
      return this.globPattern;
    }
  }

  private class SynthFCPropertyChangeListener
  implements PropertyChangeListener
  {
    public void propertyChange()
    {
      String str = paramPropertyChangeEvent.getPropertyName();
      if (str.equals("fileSelectionChanged"))
      {
        this.this$0.doFileSelectionModeChanged(paramPropertyChangeEvent);
      }
      else if (str.equals("SelectedFileChangedProperty"))
      {
        this.this$0.doSelectedFileChanged(paramPropertyChangeEvent);
      }
      else if (str.equals("SelectedFilesChangedProperty"))
      {
        this.this$0.doSelectedFilesChanged(paramPropertyChangeEvent);
      }
      else if (str.equals("directoryChanged"))
      {
        this.this$0.doDirectoryChanged(paramPropertyChangeEvent);
      }
      else if (str == "MultiSelectionEnabledChangedProperty")
      {
        this.this$0.doMultiSelectionChanged(paramPropertyChangeEvent);
      }
      else if (str == "AccessoryChangedProperty")
      {
        this.this$0.doAccessoryChanged(paramPropertyChangeEvent);
      }
      else if ((str == "ApproveButtonTextChangedProperty") || (str == "ApproveButtonToolTipTextChangedProperty") || (str == "DialogTypeChangedProperty") || (str == "ControlButtonsAreShownChangedProperty"))
      {
        this.this$0.doControlButtonsChanged(paramPropertyChangeEvent);
      }
      else if (str.equals("componentOrientation"))
      {
        ComponentOrientation localComponentOrientation = (ComponentOrientation)paramPropertyChangeEvent.getNewValue();
        JFileChooser localJFileChooser = (JFileChooser)paramPropertyChangeEvent.getSource();
        if (localComponentOrientation != (ComponentOrientation)paramPropertyChangeEvent.getOldValue())
          localJFileChooser.applyComponentOrientation(localComponentOrientation);
      }
      else if (str.equals("ancestor"))
      {
        this.this$0.doAncestorChanged(paramPropertyChangeEvent);
      }
    }
  }

  private class UIBorder extends AbstractBorder
  implements UIResource
  {
    private Insets _insets;

    UIBorder(, Insets paramInsets)
    {
      if (paramInsets != null)
        this._insets = new Insets(paramInsets.top, paramInsets.left, paramInsets.bottom, paramInsets.right);
      else
        this._insets = null;
    }

    public void paintBorder(, Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
      JComponent localJComponent = (JComponent)paramComponent;
      SynthContext localSynthContext = this.this$0.getContext(localJComponent);
      SynthStyle localSynthStyle = localSynthContext.getStyle();
      if (localSynthStyle != null)
        localSynthStyle.getPainter(localSynthContext).paintFileChooserBorder(localSynthContext, paramGraphics, paramInt1, paramInt2, paramInt3, paramInt4);
    }

    public Insets getBorderInsets()
    {
      return getBorderInsets(paramComponent, null);
    }

    public Insets getBorderInsets(, Insets paramInsets)
    {
      if (paramInsets == null)
        paramInsets = new Insets(0, 0, 0, 0);
      if (this._insets != null)
      {
        paramInsets.top = this._insets.top;
        paramInsets.bottom = this._insets.bottom;
        paramInsets.left = this._insets.left;
        paramInsets.right = this._insets.right;
      }
      else
      {
        paramInsets.top = (paramInsets.bottom = paramInsets.right = paramInsets.left = 0);
      }
      return paramInsets;
    }

    public boolean isBorderOpaque()
    {
      return false;
    }
  }
}