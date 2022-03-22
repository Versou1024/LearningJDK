package sun.print;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FilePermission;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.accessibility.AccessibleContext;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.CopiesSupported;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.JobPriority;
import javax.print.attribute.standard.JobSheets;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterInfo;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.PrinterMakeAndModel;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.attribute.standard.SheetCollate;
import javax.print.attribute.standard.Sides;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.NumberFormatter;

public class ServiceDialog extends JDialog
  implements ActionListener
{
  public static final int WAITING = 0;
  public static final int APPROVE = 1;
  public static final int CANCEL = 2;
  private static final String strBundle = "sun.print.resources.serviceui";
  private static final Insets panelInsets = new Insets(6, 6, 6, 6);
  private static final Insets compInsets = new Insets(3, 6, 3, 6);
  private static ResourceBundle messageRB;
  private JTabbedPane tpTabs;
  private JButton btnCancel;
  private JButton btnApprove;
  private PrintService[] services;
  private int defaultServiceIndex;
  private PrintRequestAttributeSet asOriginal;
  private HashPrintRequestAttributeSet asCurrent;
  private PrintService psCurrent;
  private DocFlavor docFlavor;
  private int status;
  private ValidatingFileChooser jfc;
  private GeneralPanel pnlGeneral;
  private PageSetupPanel pnlPageSetup;
  private AppearancePanel pnlAppearance;
  private boolean isAWT = false;

  public ServiceDialog(GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2, PrintService[] paramArrayOfPrintService, int paramInt3, DocFlavor paramDocFlavor, PrintRequestAttributeSet paramPrintRequestAttributeSet, Dialog paramDialog)
  {
    super(paramDialog, getMsg("dialog.printtitle"), true, paramGraphicsConfiguration);
    initPrintDialog(paramInt1, paramInt2, paramArrayOfPrintService, paramInt3, paramDocFlavor, paramPrintRequestAttributeSet);
  }

  public ServiceDialog(GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2, PrintService[] paramArrayOfPrintService, int paramInt3, DocFlavor paramDocFlavor, PrintRequestAttributeSet paramPrintRequestAttributeSet, Frame paramFrame)
  {
    super(paramFrame, getMsg("dialog.printtitle"), true, paramGraphicsConfiguration);
    initPrintDialog(paramInt1, paramInt2, paramArrayOfPrintService, paramInt3, paramDocFlavor, paramPrintRequestAttributeSet);
  }

  void initPrintDialog(int paramInt1, int paramInt2, PrintService[] paramArrayOfPrintService, int paramInt3, DocFlavor paramDocFlavor, PrintRequestAttributeSet paramPrintRequestAttributeSet)
  {
    this.services = paramArrayOfPrintService;
    this.defaultServiceIndex = paramInt3;
    this.asOriginal = paramPrintRequestAttributeSet;
    this.asCurrent = new HashPrintRequestAttributeSet(paramPrintRequestAttributeSet);
    this.psCurrent = paramArrayOfPrintService[paramInt3];
    this.docFlavor = paramDocFlavor;
    SunPageSelection localSunPageSelection = (SunPageSelection)paramPrintRequestAttributeSet.get(SunPageSelection.class);
    if (localSunPageSelection != null)
      this.isAWT = true;
    Container localContainer = getContentPane();
    localContainer.setLayout(new BorderLayout());
    this.tpTabs = new JTabbedPane();
    this.tpTabs.setBorder(new EmptyBorder(5, 5, 5, 5));
    String str1 = getMsg("tab.general");
    int i = getVKMnemonic("tab.general");
    this.pnlGeneral = new GeneralPanel(this);
    this.tpTabs.add(str1, this.pnlGeneral);
    this.tpTabs.setMnemonicAt(0, i);
    String str2 = getMsg("tab.pagesetup");
    int j = getVKMnemonic("tab.pagesetup");
    this.pnlPageSetup = new PageSetupPanel(this);
    this.tpTabs.add(str2, this.pnlPageSetup);
    this.tpTabs.setMnemonicAt(1, j);
    String str3 = getMsg("tab.appearance");
    int k = getVKMnemonic("tab.appearance");
    this.pnlAppearance = new AppearancePanel(this);
    this.tpTabs.add(str3, this.pnlAppearance);
    this.tpTabs.setMnemonicAt(2, k);
    localContainer.add(this.tpTabs, "Center");
    updatePanels();
    JPanel localJPanel = new JPanel(new FlowLayout(4));
    this.btnApprove = createExitButton("button.print", this);
    localJPanel.add(this.btnApprove);
    getRootPane().setDefaultButton(this.btnApprove);
    this.btnCancel = createExitButton("button.cancel", this);
    handleEscKey(this.btnCancel);
    localJPanel.add(this.btnCancel);
    localContainer.add(localJPanel, "South");
    addWindowListener(new WindowAdapter(this)
    {
      public void windowClosing()
      {
        this.this$0.dispose(2);
      }
    });
    getAccessibleContext().setAccessibleDescription(getMsg("dialog.printtitle"));
    setResizable(false);
    setLocation(paramInt1, paramInt2);
    pack();
  }

  public ServiceDialog(GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2, PrintService paramPrintService, DocFlavor paramDocFlavor, PrintRequestAttributeSet paramPrintRequestAttributeSet, Dialog paramDialog)
  {
    super(paramDialog, getMsg("dialog.pstitle"), true, paramGraphicsConfiguration);
    initPageDialog(paramInt1, paramInt2, paramPrintService, paramDocFlavor, paramPrintRequestAttributeSet);
  }

  public ServiceDialog(GraphicsConfiguration paramGraphicsConfiguration, int paramInt1, int paramInt2, PrintService paramPrintService, DocFlavor paramDocFlavor, PrintRequestAttributeSet paramPrintRequestAttributeSet, Frame paramFrame)
  {
    super(paramFrame, getMsg("dialog.pstitle"), true, paramGraphicsConfiguration);
    initPageDialog(paramInt1, paramInt2, paramPrintService, paramDocFlavor, paramPrintRequestAttributeSet);
  }

  void initPageDialog(int paramInt1, int paramInt2, PrintService paramPrintService, DocFlavor paramDocFlavor, PrintRequestAttributeSet paramPrintRequestAttributeSet)
  {
    this.psCurrent = paramPrintService;
    this.docFlavor = paramDocFlavor;
    this.asOriginal = paramPrintRequestAttributeSet;
    this.asCurrent = new HashPrintRequestAttributeSet(paramPrintRequestAttributeSet);
    Container localContainer = getContentPane();
    localContainer.setLayout(new BorderLayout());
    this.pnlPageSetup = new PageSetupPanel(this);
    localContainer.add(this.pnlPageSetup, "Center");
    this.pnlPageSetup.updateInfo();
    JPanel localJPanel = new JPanel(new FlowLayout(4));
    this.btnApprove = createExitButton("button.ok", this);
    localJPanel.add(this.btnApprove);
    getRootPane().setDefaultButton(this.btnApprove);
    this.btnCancel = createExitButton("button.cancel", this);
    handleEscKey(this.btnCancel);
    localJPanel.add(this.btnCancel);
    localContainer.add(localJPanel, "South");
    addWindowListener(new WindowAdapter(this)
    {
      public void windowClosing()
      {
        this.this$0.dispose(2);
      }
    });
    getAccessibleContext().setAccessibleDescription(getMsg("dialog.pstitle"));
    setResizable(false);
    setLocation(paramInt1, paramInt2);
    pack();
  }

  private void handleEscKey(JButton paramJButton)
  {
    3 local3 = new AbstractAction(this)
    {
      public void actionPerformed()
      {
        this.this$0.dispose(2);
      }
    };
    KeyStroke localKeyStroke = KeyStroke.getKeyStroke('\27', false);
    InputMap localInputMap = paramJButton.getInputMap(2);
    ActionMap localActionMap = paramJButton.getActionMap();
    if ((localInputMap != null) && (localActionMap != null))
    {
      localInputMap.put(localKeyStroke, "cancel");
      localActionMap.put("cancel", local3);
    }
  }

  public int getStatus()
  {
    return this.status;
  }

  public PrintRequestAttributeSet getAttributes()
  {
    if (this.status == 1)
      return this.asCurrent;
    return this.asOriginal;
  }

  public PrintService getPrintService()
  {
    if (this.status == 1)
      return this.psCurrent;
    return null;
  }

  public void dispose(int paramInt)
  {
    this.status = paramInt;
    super.dispose();
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    Object localObject = paramActionEvent.getSource();
    boolean bool = false;
    if (localObject == this.btnApprove)
    {
      bool = true;
      if (this.pnlGeneral != null)
        if (this.pnlGeneral.isPrintToFileRequested())
          bool = showFileChooser();
        else
          this.asCurrent.remove(Destination.class);
    }
    dispose((bool) ? 1 : 2);
  }

  private boolean showFileChooser()
  {
    java.io.File localFile;
    Destination localDestination1 = Destination.class;
    Destination localDestination2 = (Destination)this.asCurrent.get(localDestination1);
    if (localDestination2 == null)
    {
      localDestination2 = (Destination)this.asOriginal.get(localDestination1);
      if (localDestination2 == null)
      {
        localDestination2 = (Destination)this.psCurrent.getDefaultAttributeValue(localDestination1);
        if (localDestination2 == null)
          try
          {
            localDestination2 = new Destination(new URI("file:out.prn"));
          }
          catch (URISyntaxException localURISyntaxException)
          {
          }
      }
    }
    if (localDestination2 != null)
      try
      {
        localFile = new java.io.File(localDestination2.getURI());
      }
      catch (Exception localException1)
      {
        localFile = new java.io.File("out.prn");
      }
    else
      localFile = new java.io.File("out.prn");
    ValidatingFileChooser localValidatingFileChooser = new ValidatingFileChooser(this, null);
    localValidatingFileChooser.setApproveButtonText(getMsg("button.ok"));
    localValidatingFileChooser.setDialogTitle(getMsg("dialog.printtofile"));
    localValidatingFileChooser.setSelectedFile(localFile);
    int i = localValidatingFileChooser.showDialog(this, null);
    if (i == 0)
    {
      localFile = localValidatingFileChooser.getSelectedFile();
      try
      {
        this.asCurrent.add(new Destination(localFile.toURI()));
      }
      catch (Exception localException2)
      {
        this.asCurrent.remove(localDestination1);
      }
    }
    else
    {
      this.asCurrent.remove(localDestination1);
    }
    return (i == 0);
  }

  private void updatePanels()
  {
    this.pnlGeneral.updateInfo();
    this.pnlPageSetup.updateInfo();
    this.pnlAppearance.updateInfo();
  }

  public static void initResource()
  {
    AccessController.doPrivileged(new PrivilegedAction()
    {
      public Object run()
      {
        try
        {
          ServiceDialog.access$102(ResourceBundle.getBundle("sun.print.resources.serviceui"));
          return null;
        }
        catch (MissingResourceException localMissingResourceException)
        {
          throw new Error("Fatal: Resource for ServiceUI is missing");
        }
      }
    });
  }

  public static String getMsg(String paramString)
  {
    try
    {
      return messageRB.getString(paramString);
    }
    catch (MissingResourceException localMissingResourceException)
    {
      throw new Error("Fatal: Resource for ServiceUI is broken; there is no " + paramString + " key in resource");
    }
  }

  private static char getMnemonic(String paramString)
  {
    String str = getMsg(paramString + ".mnemonic");
    if ((str != null) && (str.length() > 0))
      return str.charAt(0);
    return ';
  }

  private static int getVKMnemonic(String paramString)
  {
    String str = getMsg(paramString + ".vkMnemonic");
    if ((str != null) && (str.length() > 0))
      try
      {
        return Integer.parseInt(str);
      }
      catch (NumberFormatException localNumberFormatException)
      {
      }
    return 0;
  }

  private static URL getImageResource(String paramString)
  {
    URL localURL = (URL)AccessController.doPrivileged(new PrivilegedAction(paramString)
    {
      public Object run()
      {
        URL localURL = ServiceDialog.class.getResource("resources/" + this.val$key);
        return localURL;
      }
    });
    if (localURL == null)
      throw new Error("Fatal: Resource for ServiceUI is broken; there is no " + paramString + " key in resource");
    return localURL;
  }

  private static JButton createButton(String paramString, ActionListener paramActionListener)
  {
    JButton localJButton = new JButton(getMsg(paramString));
    localJButton.setMnemonic(getMnemonic(paramString));
    localJButton.addActionListener(paramActionListener);
    return localJButton;
  }

  private static JButton createExitButton(String paramString, ActionListener paramActionListener)
  {
    String str = getMsg(paramString);
    JButton localJButton = new JButton(str);
    localJButton.addActionListener(paramActionListener);
    localJButton.getAccessibleContext().setAccessibleDescription(str);
    return localJButton;
  }

  private static JCheckBox createCheckBox(String paramString, ActionListener paramActionListener)
  {
    JCheckBox localJCheckBox = new JCheckBox(getMsg(paramString));
    localJCheckBox.setMnemonic(getMnemonic(paramString));
    localJCheckBox.addActionListener(paramActionListener);
    return localJCheckBox;
  }

  private static JRadioButton createRadioButton(String paramString, ActionListener paramActionListener)
  {
    JRadioButton localJRadioButton = new JRadioButton(getMsg(paramString));
    localJRadioButton.setMnemonic(getMnemonic(paramString));
    localJRadioButton.addActionListener(paramActionListener);
    return localJRadioButton;
  }

  public static void showNoPrintService(GraphicsConfiguration paramGraphicsConfiguration)
  {
    Frame localFrame = new Frame(paramGraphicsConfiguration);
    JOptionPane.showMessageDialog(localFrame, getMsg("dialog.noprintermsg"));
    localFrame.dispose();
  }

  private static void addToGB(Component paramComponent, Container paramContainer, GridBagLayout paramGridBagLayout, GridBagConstraints paramGridBagConstraints)
  {
    paramGridBagLayout.setConstraints(paramComponent, paramGridBagConstraints);
    paramContainer.add(paramComponent);
  }

  private static void addToBG(AbstractButton paramAbstractButton, Container paramContainer, ButtonGroup paramButtonGroup)
  {
    paramButtonGroup.add(paramAbstractButton);
    paramContainer.add(paramAbstractButton);
  }

  static
  {
    initResource();
  }

  private class AppearancePanel extends JPanel
  {
    private ServiceDialog.ChromaticityPanel pnlChromaticity;
    private ServiceDialog.QualityPanel pnlQuality;
    private ServiceDialog.JobAttributesPanel pnlJobAttributes;
    private ServiceDialog.SidesPanel pnlSides;

    public AppearancePanel()
    {
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      setLayout(localGridBagLayout);
      localGridBagConstraints.fill = 1;
      localGridBagConstraints.insets = ServiceDialog.access$200();
      localGridBagConstraints.weightx = 1D;
      localGridBagConstraints.weighty = 1D;
      localGridBagConstraints.gridwidth = -1;
      this.pnlChromaticity = new ServiceDialog.ChromaticityPanel(paramServiceDialog);
      ServiceDialog.access$300(this.pnlChromaticity, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = 0;
      this.pnlQuality = new ServiceDialog.QualityPanel(paramServiceDialog);
      ServiceDialog.access$300(this.pnlQuality, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = 1;
      this.pnlSides = new ServiceDialog.SidesPanel(paramServiceDialog);
      ServiceDialog.access$300(this.pnlSides, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = 0;
      this.pnlJobAttributes = new ServiceDialog.JobAttributesPanel(paramServiceDialog);
      ServiceDialog.access$300(this.pnlJobAttributes, this, localGridBagLayout, localGridBagConstraints);
    }

    public void updateInfo()
    {
      this.pnlChromaticity.updateInfo();
      this.pnlQuality.updateInfo();
      this.pnlSides.updateInfo();
      this.pnlJobAttributes.updateInfo();
    }
  }

  private class ChromaticityPanel extends JPanel
  implements ActionListener
  {
    private final String strTitle = ServiceDialog.getMsg("border.chromaticity");
    private JRadioButton rbMonochrome;
    private JRadioButton rbColor;

    public ChromaticityPanel()
    {
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      setLayout(localGridBagLayout);
      setBorder(BorderFactory.createTitledBorder(this.strTitle));
      localGridBagConstraints.fill = 1;
      localGridBagConstraints.gridwidth = 0;
      localGridBagConstraints.weighty = 1D;
      ButtonGroup localButtonGroup = new ButtonGroup();
      this.rbMonochrome = ServiceDialog.access$1400("radiobutton.monochrome", this);
      this.rbMonochrome.setSelected(true);
      localButtonGroup.add(this.rbMonochrome);
      ServiceDialog.access$300(this.rbMonochrome, this, localGridBagLayout, localGridBagConstraints);
      this.rbColor = ServiceDialog.access$1400("radiobutton.color", this);
      localButtonGroup.add(this.rbColor);
      ServiceDialog.access$300(this.rbColor, this, localGridBagLayout, localGridBagConstraints);
    }

    public void actionPerformed()
    {
      Object localObject = paramActionEvent.getSource();
      if (localObject == this.rbMonochrome)
        ServiceDialog.access$1200(this.this$0).add(Chromaticity.MONOCHROME);
      else if (localObject == this.rbColor)
        ServiceDialog.access$1200(this.this$0).add(Chromaticity.COLOR);
    }

    public void updateInfo()
    {
      Chromaticity localChromaticity1 = Chromaticity.class;
      boolean bool1 = false;
      boolean bool2 = false;
      if (ServiceDialog.access$1500(this.this$0))
      {
        bool1 = true;
        bool2 = true;
      }
      else if (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localChromaticity1))
      {
        localObject = ServiceDialog.access$400(this.this$0).getSupportedAttributeValues(localChromaticity1, ServiceDialog.access$1600(this.this$0), ServiceDialog.access$1200(this.this$0));
        if (localObject instanceof Chromaticity[])
        {
          Chromaticity[] arrayOfChromaticity = (Chromaticity[])(Chromaticity[])localObject;
          for (int i = 0; i < arrayOfChromaticity.length; ++i)
          {
            Chromaticity localChromaticity2 = arrayOfChromaticity[i];
            if (localChromaticity2 == Chromaticity.MONOCHROME)
              bool1 = true;
            else if (localChromaticity2 == Chromaticity.COLOR)
              bool2 = true;
          }
        }
      }
      this.rbMonochrome.setEnabled(bool1);
      this.rbColor.setEnabled(bool2);
      Object localObject = (Chromaticity)ServiceDialog.access$1200(this.this$0).get(localChromaticity1);
      if (localObject == null)
      {
        localObject = (Chromaticity)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localChromaticity1);
        if (localObject == null)
          localObject = Chromaticity.MONOCHROME;
      }
      if (localObject == Chromaticity.MONOCHROME)
        this.rbMonochrome.setSelected(true);
      else
        this.rbColor.setSelected(true);
    }
  }

  private class CopiesPanel extends JPanel
  implements ActionListener, ChangeListener
  {
    private final String strTitle = ServiceDialog.getMsg("border.copies");
    private SpinnerNumberModel snModel;
    private JSpinner spinCopies;
    private JLabel lblCopies;
    private JCheckBox cbCollate;
    private boolean scSupported;

    public CopiesPanel()
    {
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      setLayout(localGridBagLayout);
      setBorder(BorderFactory.createTitledBorder(this.strTitle));
      localGridBagConstraints.fill = 2;
      localGridBagConstraints.insets = ServiceDialog.access$700();
      this.lblCopies = new JLabel(ServiceDialog.getMsg("label.numcopies"), 11);
      this.lblCopies.setDisplayedMnemonic(ServiceDialog.access$800("label.numcopies"));
      this.lblCopies.getAccessibleContext().setAccessibleName(ServiceDialog.getMsg("label.numcopies"));
      ServiceDialog.access$300(this.lblCopies, this, localGridBagLayout, localGridBagConstraints);
      this.snModel = new SpinnerNumberModel(1, 1, 999, 1);
      this.spinCopies = new JSpinner(this.snModel);
      this.lblCopies.setLabelFor(this.spinCopies);
      ((JSpinner.NumberEditor)this.spinCopies.getEditor()).getTextField().setColumns(3);
      this.spinCopies.addChangeListener(this);
      localGridBagConstraints.gridwidth = 0;
      ServiceDialog.access$300(this.spinCopies, this, localGridBagLayout, localGridBagConstraints);
      this.cbCollate = ServiceDialog.access$1000("checkbox.collate", this);
      this.cbCollate.setEnabled(false);
      ServiceDialog.access$300(this.cbCollate, this, localGridBagLayout, localGridBagConstraints);
    }

    public void actionPerformed()
    {
      if (this.cbCollate.isSelected())
        ServiceDialog.access$1200(this.this$0).add(SheetCollate.COLLATED);
      else
        ServiceDialog.access$1200(this.this$0).add(SheetCollate.UNCOLLATED);
    }

    public void stateChanged()
    {
      updateCollateCB();
      ServiceDialog.access$1200(this.this$0).add(new Copies(this.snModel.getNumber().intValue()));
    }

    private void updateCollateCB()
    {
      int i = this.snModel.getNumber().intValue();
      if (ServiceDialog.access$1500(this.this$0))
        this.cbCollate.setEnabled(true);
      else
        this.cbCollate.setEnabled((i > 1) && (this.scSupported));
    }

    public void updateInfo()
    {
      int i;
      int j;
      Copies localCopies1 = Copies.class;
      CopiesSupported localCopiesSupported1 = CopiesSupported.class;
      SheetCollate localSheetCollate1 = SheetCollate.class;
      boolean bool = false;
      this.scSupported = false;
      if (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localCopies1))
        bool = true;
      CopiesSupported localCopiesSupported2 = (CopiesSupported)ServiceDialog.access$400(this.this$0).getSupportedAttributeValues(localCopies1, null, null);
      if (localCopiesSupported2 == null)
        localCopiesSupported2 = new CopiesSupported(1, 999);
      Copies localCopies2 = (Copies)ServiceDialog.access$1200(this.this$0).get(localCopies1);
      if (localCopies2 == null)
      {
        localCopies2 = (Copies)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localCopies1);
        if (localCopies2 == null)
          localCopies2 = new Copies(1);
      }
      this.spinCopies.setEnabled(bool);
      this.lblCopies.setEnabled(bool);
      int[][] arrayOfInt = localCopiesSupported2.getMembers();
      if ((arrayOfInt.length > 0) && (arrayOfInt[0].length > 0))
      {
        i = arrayOfInt[0][0];
        j = arrayOfInt[0][1];
      }
      else
      {
        i = 1;
        j = 2147483647;
      }
      this.snModel.setMinimum(new Integer(i));
      this.snModel.setMaximum(new Integer(j));
      int k = localCopies2.getValue();
      if ((k < i) || (k > j))
        k = i;
      this.snModel.setValue(new Integer(k));
      if (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localSheetCollate1))
        this.scSupported = true;
      SheetCollate localSheetCollate2 = (SheetCollate)ServiceDialog.access$1200(this.this$0).get(localSheetCollate1);
      if (localSheetCollate2 == null)
      {
        localSheetCollate2 = (SheetCollate)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localSheetCollate1);
        if (localSheetCollate2 == null)
          localSheetCollate2 = SheetCollate.UNCOLLATED;
      }
      this.cbCollate.setSelected(localSheetCollate2 == SheetCollate.COLLATED);
      updateCollateCB();
    }
  }

  private class GeneralPanel extends JPanel
  {
    private ServiceDialog.PrintServicePanel pnlPrintService;
    private ServiceDialog.PrintRangePanel pnlPrintRange;
    private ServiceDialog.CopiesPanel pnlCopies;

    public GeneralPanel()
    {
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      setLayout(localGridBagLayout);
      localGridBagConstraints.fill = 1;
      localGridBagConstraints.insets = ServiceDialog.access$200();
      localGridBagConstraints.weightx = 1D;
      localGridBagConstraints.weighty = 1D;
      localGridBagConstraints.gridwidth = 0;
      this.pnlPrintService = new ServiceDialog.PrintServicePanel(paramServiceDialog);
      ServiceDialog.access$300(this.pnlPrintService, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = -1;
      this.pnlPrintRange = new ServiceDialog.PrintRangePanel(paramServiceDialog);
      ServiceDialog.access$300(this.pnlPrintRange, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = 0;
      this.pnlCopies = new ServiceDialog.CopiesPanel(paramServiceDialog);
      ServiceDialog.access$300(this.pnlCopies, this, localGridBagLayout, localGridBagConstraints);
    }

    public boolean isPrintToFileRequested()
    {
      return this.pnlPrintService.isPrintToFileSelected();
    }

    public void updateInfo()
    {
      this.pnlPrintService.updateInfo();
      this.pnlPrintRange.updateInfo();
      this.pnlCopies.updateInfo();
    }
  }

  private class IconRadioButton extends JPanel
  {
    private JRadioButton rb;
    private JLabel lbl;

    public IconRadioButton(, String paramString1, String paramString2, boolean paramBoolean, ButtonGroup paramButtonGroup, ActionListener paramActionListener)
    {
      super(new FlowLayout(3));
      URL localURL = ServiceDialog.access$1700(paramString2);
      Icon localIcon = (Icon)AccessController.doPrivileged(new PrivilegedAction(this, paramServiceDialog, localURL)
      {
        public Object run()
        {
          ImageIcon localImageIcon = new ImageIcon(this.val$imgURL);
          return localImageIcon;
        }
      });
      this.lbl = new JLabel(localIcon);
      add(this.lbl);
      this.rb = ServiceDialog.access$1400(paramString1, paramActionListener);
      this.rb.setSelected(paramBoolean);
      ServiceDialog.access$1800(this.rb, this, paramButtonGroup);
    }

    public void addActionListener()
    {
      this.rb.addActionListener(paramActionListener);
    }

    public boolean isSameAs()
    {
      return (this.rb == paramObject);
    }

    public void setEnabled()
    {
      this.rb.setEnabled(paramBoolean);
      this.lbl.setEnabled(paramBoolean);
    }

    public boolean isSelected()
    {
      return this.rb.isSelected();
    }

    public void setSelected()
    {
      this.rb.setSelected(paramBoolean);
    }
  }

  private class JobAttributesPanel extends JPanel
  implements ActionListener, ChangeListener, FocusListener
  {
    private final String strTitle = ServiceDialog.getMsg("border.jobattributes");
    private JLabel lblPriority;
    private JLabel lblJobName;
    private JLabel lblUserName;
    private JSpinner spinPriority;
    private SpinnerNumberModel snModel;
    private JCheckBox cbJobSheets;
    private JTextField tfJobName;
    private JTextField tfUserName;

    public JobAttributesPanel()
    {
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      setLayout(localGridBagLayout);
      setBorder(BorderFactory.createTitledBorder(this.strTitle));
      localGridBagConstraints.fill = 0;
      localGridBagConstraints.insets = ServiceDialog.access$700();
      localGridBagConstraints.weighty = 1D;
      this.cbJobSheets = ServiceDialog.access$1000("checkbox.jobsheets", this);
      localGridBagConstraints.anchor = 21;
      ServiceDialog.access$300(this.cbJobSheets, this, localGridBagLayout, localGridBagConstraints);
      JPanel localJPanel = new JPanel();
      this.lblPriority = new JLabel(ServiceDialog.getMsg("label.priority"), 11);
      this.lblPriority.setDisplayedMnemonic(ServiceDialog.access$800("label.priority"));
      localJPanel.add(this.lblPriority);
      this.snModel = new SpinnerNumberModel(1, 1, 100, 1);
      this.spinPriority = new JSpinner(this.snModel);
      this.lblPriority.setLabelFor(this.spinPriority);
      ((JSpinner.NumberEditor)this.spinPriority.getEditor()).getTextField().setColumns(3);
      this.spinPriority.addChangeListener(this);
      localJPanel.add(this.spinPriority);
      localGridBagConstraints.anchor = 22;
      localGridBagConstraints.gridwidth = 0;
      localJPanel.getAccessibleContext().setAccessibleName(ServiceDialog.getMsg("label.priority"));
      ServiceDialog.access$300(localJPanel, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.fill = 2;
      localGridBagConstraints.anchor = 10;
      localGridBagConstraints.weightx = 0D;
      localGridBagConstraints.gridwidth = 1;
      char c1 = ServiceDialog.access$800("label.jobname");
      this.lblJobName = new JLabel(ServiceDialog.getMsg("label.jobname"), 11);
      this.lblJobName.setDisplayedMnemonic(c1);
      ServiceDialog.access$300(this.lblJobName, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.weightx = 1D;
      localGridBagConstraints.gridwidth = 0;
      this.tfJobName = new JTextField();
      this.lblJobName.setLabelFor(this.tfJobName);
      this.tfJobName.addFocusListener(this);
      this.tfJobName.setFocusAccelerator(c1);
      this.tfJobName.getAccessibleContext().setAccessibleName(ServiceDialog.getMsg("label.jobname"));
      ServiceDialog.access$300(this.tfJobName, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.weightx = 0D;
      localGridBagConstraints.gridwidth = 1;
      char c2 = ServiceDialog.access$800("label.username");
      this.lblUserName = new JLabel(ServiceDialog.getMsg("label.username"), 11);
      this.lblUserName.setDisplayedMnemonic(c2);
      ServiceDialog.access$300(this.lblUserName, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = 0;
      this.tfUserName = new JTextField();
      this.lblUserName.setLabelFor(this.tfUserName);
      this.tfUserName.addFocusListener(this);
      this.tfUserName.setFocusAccelerator(c2);
      this.tfUserName.getAccessibleContext().setAccessibleName(ServiceDialog.getMsg("label.username"));
      ServiceDialog.access$300(this.tfUserName, this, localGridBagLayout, localGridBagConstraints);
    }

    public void actionPerformed()
    {
      if (this.cbJobSheets.isSelected())
        ServiceDialog.access$1200(this.this$0).add(JobSheets.STANDARD);
      else
        ServiceDialog.access$1200(this.this$0).add(JobSheets.NONE);
    }

    public void stateChanged()
    {
      ServiceDialog.access$1200(this.this$0).add(new JobPriority(this.snModel.getNumber().intValue()));
    }

    public void focusLost()
    {
      Object localObject = paramFocusEvent.getSource();
      if (localObject == this.tfJobName)
        ServiceDialog.access$1200(this.this$0).add(new JobName(this.tfJobName.getText(), Locale.getDefault()));
      else if (localObject == this.tfUserName)
        ServiceDialog.access$1200(this.this$0).add(new RequestingUserName(this.tfUserName.getText(), Locale.getDefault()));
    }

    public void focusGained()
    {
    }

    public void updateInfo()
    {
      JobSheets localJobSheets1 = JobSheets.class;
      JobPriority localJobPriority1 = JobPriority.class;
      JobName localJobName1 = JobName.class;
      RequestingUserName localRequestingUserName1 = RequestingUserName.class;
      boolean bool1 = false;
      boolean bool2 = false;
      boolean bool3 = false;
      boolean bool4 = false;
      if (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localJobSheets1))
        bool1 = true;
      JobSheets localJobSheets2 = (JobSheets)ServiceDialog.access$1200(this.this$0).get(localJobSheets1);
      if (localJobSheets2 == null)
      {
        localJobSheets2 = (JobSheets)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localJobSheets1);
        if (localJobSheets2 == null)
          localJobSheets2 = JobSheets.NONE;
      }
      this.cbJobSheets.setSelected(localJobSheets2 != JobSheets.NONE);
      this.cbJobSheets.setEnabled(bool1);
      if ((!(ServiceDialog.access$1500(this.this$0))) && (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localJobPriority1)))
        bool2 = true;
      JobPriority localJobPriority2 = (JobPriority)ServiceDialog.access$1200(this.this$0).get(localJobPriority1);
      if (localJobPriority2 == null)
      {
        localJobPriority2 = (JobPriority)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localJobPriority1);
        if (localJobPriority2 == null)
          localJobPriority2 = new JobPriority(1);
      }
      int i = localJobPriority2.getValue();
      if ((i < 1) || (i > 100))
        i = 1;
      this.snModel.setValue(new Integer(i));
      this.lblPriority.setEnabled(bool2);
      this.spinPriority.setEnabled(bool2);
      if (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localJobName1))
        bool3 = true;
      JobName localJobName2 = (JobName)ServiceDialog.access$1200(this.this$0).get(localJobName1);
      if (localJobName2 == null)
      {
        localJobName2 = (JobName)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localJobName1);
        if (localJobName2 == null)
          localJobName2 = new JobName("", Locale.getDefault());
      }
      this.tfJobName.setText(localJobName2.getValue());
      this.tfJobName.setEnabled(bool3);
      this.lblJobName.setEnabled(bool3);
      if ((!(ServiceDialog.access$1500(this.this$0))) && (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localRequestingUserName1)))
        bool4 = true;
      RequestingUserName localRequestingUserName2 = (RequestingUserName)ServiceDialog.access$1200(this.this$0).get(localRequestingUserName1);
      if (localRequestingUserName2 == null)
      {
        localRequestingUserName2 = (RequestingUserName)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localRequestingUserName1);
        if (localRequestingUserName2 == null)
          localRequestingUserName2 = new RequestingUserName("", Locale.getDefault());
      }
      this.tfUserName.setText(localRequestingUserName2.getValue());
      this.tfUserName.setEnabled(bool4);
      this.lblUserName.setEnabled(bool4);
    }
  }

  private class MarginsPanel extends JPanel
  implements ActionListener, FocusListener
  {
    private final String strTitle = ServiceDialog.getMsg("border.margins");
    private JFormattedTextField leftMargin;
    private JFormattedTextField rightMargin;
    private JFormattedTextField topMargin;
    private JFormattedTextField bottomMargin;
    private JLabel lblLeft;
    private JLabel lblRight;
    private JLabel lblTop;
    private JLabel lblBottom;
    private int units = 1000;
    private float lmVal = -1.0F;
    private float rmVal = -1.0F;
    private float tmVal = -1.0F;
    private float bmVal = -1.0F;
    private Float lmObj;
    private Float rmObj;
    private Float tmObj;
    private Float bmObj;

    public MarginsPanel()
    {
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      localGridBagConstraints.fill = 2;
      localGridBagConstraints.weightx = 1D;
      localGridBagConstraints.weighty = 0D;
      localGridBagConstraints.insets = ServiceDialog.access$700();
      setLayout(localGridBagLayout);
      setBorder(BorderFactory.createTitledBorder(this.strTitle));
      String str1 = "label.millimetres";
      String str2 = Locale.getDefault().getCountry();
      if ((str2 != null) && (((str2.equals("")) || (str2.equals(Locale.US.getCountry())) || (str2.equals(Locale.CANADA.getCountry())))))
      {
        str1 = "label.inches";
        this.units = 25400;
      }
      String str3 = ServiceDialog.getMsg(str1);
      if (this.units == 1000)
      {
        localDecimalFormat = new DecimalFormat("###.##");
        localDecimalFormat.setMaximumIntegerDigits(3);
      }
      else
      {
        localDecimalFormat = new DecimalFormat("##.##");
        localDecimalFormat.setMaximumIntegerDigits(2);
      }
      localDecimalFormat.setMinimumFractionDigits(1);
      localDecimalFormat.setMaximumFractionDigits(2);
      localDecimalFormat.setMinimumIntegerDigits(1);
      localDecimalFormat.setParseIntegerOnly(false);
      localDecimalFormat.setDecimalSeparatorAlwaysShown(true);
      NumberFormatter localNumberFormatter = new NumberFormatter(localDecimalFormat);
      localNumberFormatter.setMinimum(new Float(0F));
      localNumberFormatter.setMaximum(new Float(999.0F));
      localNumberFormatter.setAllowsInvalid(true);
      localNumberFormatter.setCommitsOnValidEdit(true);
      this.leftMargin = new JFormattedTextField(localNumberFormatter);
      this.leftMargin.addFocusListener(this);
      this.leftMargin.addActionListener(this);
      this.leftMargin.getAccessibleContext().setAccessibleName(ServiceDialog.getMsg("label.leftmargin"));
      this.rightMargin = new JFormattedTextField(localNumberFormatter);
      this.rightMargin.addFocusListener(this);
      this.rightMargin.addActionListener(this);
      this.rightMargin.getAccessibleContext().setAccessibleName(ServiceDialog.getMsg("label.rightmargin"));
      this.topMargin = new JFormattedTextField(localNumberFormatter);
      this.topMargin.addFocusListener(this);
      this.topMargin.addActionListener(this);
      this.topMargin.getAccessibleContext().setAccessibleName(ServiceDialog.getMsg("label.topmargin"));
      this.topMargin = new JFormattedTextField(localNumberFormatter);
      this.bottomMargin = new JFormattedTextField(localNumberFormatter);
      this.bottomMargin.addFocusListener(this);
      this.bottomMargin.addActionListener(this);
      this.bottomMargin.getAccessibleContext().setAccessibleName(ServiceDialog.getMsg("label.bottommargin"));
      this.topMargin = new JFormattedTextField(localNumberFormatter);
      localGridBagConstraints.gridwidth = -1;
      this.lblLeft = new JLabel(ServiceDialog.getMsg("label.leftmargin") + " " + str3, 10);
      this.lblLeft.setDisplayedMnemonic(ServiceDialog.access$800("label.leftmargin"));
      this.lblLeft.setLabelFor(this.leftMargin);
      ServiceDialog.access$300(this.lblLeft, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = 0;
      this.lblRight = new JLabel(ServiceDialog.getMsg("label.rightmargin") + " " + str3, 10);
      this.lblRight.setDisplayedMnemonic(ServiceDialog.access$800("label.rightmargin"));
      this.lblRight.setLabelFor(this.rightMargin);
      ServiceDialog.access$300(this.lblRight, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = -1;
      ServiceDialog.access$300(this.leftMargin, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = 0;
      ServiceDialog.access$300(this.rightMargin, this, localGridBagLayout, localGridBagConstraints);
      ServiceDialog.access$300(new JPanel(), this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = -1;
      this.lblTop = new JLabel(ServiceDialog.getMsg("label.topmargin") + " " + str3, 10);
      this.lblTop.setDisplayedMnemonic(ServiceDialog.access$800("label.topmargin"));
      this.lblTop.setLabelFor(this.topMargin);
      ServiceDialog.access$300(this.lblTop, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = 0;
      this.lblBottom = new JLabel(ServiceDialog.getMsg("label.bottommargin") + " " + str3, 10);
      this.lblBottom.setDisplayedMnemonic(ServiceDialog.access$800("label.bottommargin"));
      this.lblBottom.setLabelFor(this.bottomMargin);
      ServiceDialog.access$300(this.lblBottom, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = -1;
      ServiceDialog.access$300(this.topMargin, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = 0;
      ServiceDialog.access$300(this.bottomMargin, this, localGridBagLayout, localGridBagConstraints);
    }

    public void actionPerformed()
    {
      Object localObject = paramActionEvent.getSource();
      updateMargins(localObject);
    }

    public void focusLost()
    {
      Object localObject = paramFocusEvent.getSource();
      updateMargins(localObject);
    }

    public void focusGained()
    {
    }

    public void updateMargins()
    {
      float f5;
      MediaPrintableArea localMediaPrintableArea;
      if (!(paramObject instanceof JFormattedTextField))
        return;
      Object localObject = (JFormattedTextField)paramObject;
      Float localFloat1 = (Float)((JFormattedTextField)localObject).getValue();
      if (localFloat1 == null)
        return;
      if ((localObject == this.leftMargin) && (localFloat1.equals(this.lmObj)))
        return;
      if ((localObject == this.rightMargin) && (localFloat1.equals(this.rmObj)))
        return;
      if ((localObject == this.topMargin) && (localFloat1.equals(this.tmObj)))
        return;
      if ((localObject == this.bottomMargin) && (localFloat1.equals(this.bmObj)))
        return;
      localObject = (Float)this.leftMargin.getValue();
      localFloat1 = (Float)this.rightMargin.getValue();
      Float localFloat2 = (Float)this.topMargin.getValue();
      Float localFloat3 = (Float)this.bottomMargin.getValue();
      float f1 = ((Float)localObject).floatValue();
      float f2 = localFloat1.floatValue();
      float f3 = localFloat2.floatValue();
      float f4 = localFloat3.floatValue();
      OrientationRequested localOrientationRequested1 = OrientationRequested.class;
      OrientationRequested localOrientationRequested2 = (OrientationRequested)ServiceDialog.access$1200(this.this$0).get(localOrientationRequested1);
      if (localOrientationRequested2 == null)
        localOrientationRequested2 = (OrientationRequested)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localOrientationRequested1);
      if (localOrientationRequested2 == OrientationRequested.REVERSE_PORTRAIT)
      {
        f5 = f1;
        f1 = f2;
        f2 = f5;
        f5 = f3;
        f3 = f4;
        f4 = f5;
      }
      else if (localOrientationRequested2 == OrientationRequested.LANDSCAPE)
      {
        f5 = f1;
        f1 = f3;
        f3 = f2;
        f2 = f4;
        f4 = f5;
      }
      else if (localOrientationRequested2 == OrientationRequested.REVERSE_LANDSCAPE)
      {
        f5 = f1;
        f1 = f4;
        f4 = f2;
        f2 = f3;
        f3 = f5;
      }
      if ((localMediaPrintableArea = validateMargins(f1, f2, f3, f4)) != null)
      {
        ServiceDialog.access$1200(this.this$0).add(localMediaPrintableArea);
        this.lmVal = f1;
        this.rmVal = f2;
        this.tmVal = f3;
        this.bmVal = f4;
        this.lmObj = ((Float)localObject);
        this.rmObj = localFloat1;
        this.tmObj = localFloat2;
        this.bmObj = localFloat3;
      }
      else
      {
        if ((this.lmObj == null) || (this.rmObj == null) || (this.tmObj == null) || (this.rmObj == null))
          return;
        this.leftMargin.setValue(this.lmObj);
        this.rightMargin.setValue(this.rmObj);
        this.topMargin.setValue(this.tmObj);
        this.bottomMargin.setValue(this.bmObj);
      }
    }

    private MediaPrintableArea validateMargins(, float paramFloat2, float paramFloat3, float paramFloat4)
    {
      Object localObject1;
      MediaPrintableArea localMediaPrintableArea1 = MediaPrintableArea.class;
      MediaPrintableArea localMediaPrintableArea2 = null;
      MediaSize localMediaSize = null;
      Media localMedia = (Media)ServiceDialog.access$1200(this.this$0).get(Media.class);
      if ((localMedia == null) || (!(localMedia instanceof MediaSizeName)))
        localMedia = (Media)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(Media.class);
      if ((localMedia != null) && (localMedia instanceof MediaSizeName))
      {
        localObject1 = (MediaSizeName)localMedia;
        localMediaSize = MediaSize.getMediaSizeForName((MediaSizeName)localObject1);
      }
      if (localMediaSize == null)
        localMediaSize = new MediaSize(8.5F, 11.0F, 25400);
      if (localMedia != null)
      {
        localObject1 = new HashPrintRequestAttributeSet(ServiceDialog.access$1200(this.this$0));
        ((PrintRequestAttributeSet)localObject1).add(localMedia);
        Object localObject2 = ServiceDialog.access$400(this.this$0).getSupportedAttributeValues(localMediaPrintableArea1, ServiceDialog.access$1600(this.this$0), (AttributeSet)localObject1);
        if ((localObject2 instanceof MediaPrintableArea[]) && (((MediaPrintableArea[])(MediaPrintableArea[])localObject2).length > 0))
          localMediaPrintableArea2 = ((MediaPrintableArea[])(MediaPrintableArea[])localObject2)[0];
      }
      if (localMediaPrintableArea2 == null)
        localMediaPrintableArea2 = new MediaPrintableArea(0F, 0F, localMediaSize.getX(this.units), localMediaSize.getY(this.units), this.units);
      float f1 = localMediaSize.getX(this.units);
      float f2 = localMediaSize.getY(this.units);
      float f3 = paramFloat1;
      float f4 = paramFloat3;
      float f5 = f1 - paramFloat1 - paramFloat2;
      float f6 = f2 - paramFloat3 - paramFloat4;
      if ((f5 <= 0F) || (f6 <= 0F) || (f3 < 0F) || (f4 < 0F) || (f3 < localMediaPrintableArea2.getX(this.units)) || (f5 > localMediaPrintableArea2.getWidth(this.units)) || (f4 < localMediaPrintableArea2.getY(this.units)) || (f6 > localMediaPrintableArea2.getHeight(this.units)))
        return null;
      return ((MediaPrintableArea)new MediaPrintableArea(paramFloat1, paramFloat3, f5, f6, this.units));
    }

    public void updateInfo()
    {
      Object localObject1;
      float f4;
      float f5;
      Float localFloat;
      if (ServiceDialog.access$1500(this.this$0))
      {
        this.leftMargin.setEnabled(false);
        this.rightMargin.setEnabled(false);
        this.topMargin.setEnabled(false);
        this.bottomMargin.setEnabled(false);
        this.lblLeft.setEnabled(false);
        this.lblRight.setEnabled(false);
        this.lblTop.setEnabled(false);
        this.lblBottom.setEnabled(false);
        return;
      }
      MediaPrintableArea localMediaPrintableArea1 = MediaPrintableArea.class;
      MediaPrintableArea localMediaPrintableArea2 = (MediaPrintableArea)ServiceDialog.access$1200(this.this$0).get(localMediaPrintableArea1);
      MediaPrintableArea localMediaPrintableArea3 = null;
      MediaSize localMediaSize = null;
      Media localMedia = (Media)ServiceDialog.access$1200(this.this$0).get(Media.class);
      if ((localMedia == null) || (!(localMedia instanceof MediaSizeName)))
        localMedia = (Media)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(Media.class);
      if ((localMedia != null) && (localMedia instanceof MediaSizeName))
      {
        localObject1 = (MediaSizeName)localMedia;
        localMediaSize = MediaSize.getMediaSizeForName((MediaSizeName)localObject1);
      }
      if (localMediaSize == null)
        localMediaSize = new MediaSize(8.5F, 11.0F, 25400);
      if (localMedia != null)
      {
        localObject1 = new HashPrintRequestAttributeSet(ServiceDialog.access$1200(this.this$0));
        ((PrintRequestAttributeSet)localObject1).add(localMedia);
        Object localObject2 = ServiceDialog.access$400(this.this$0).getSupportedAttributeValues(localMediaPrintableArea1, ServiceDialog.access$1600(this.this$0), (AttributeSet)localObject1);
        if ((localObject2 instanceof MediaPrintableArea[]) && (((MediaPrintableArea[])(MediaPrintableArea[])localObject2).length > 0))
          localMediaPrintableArea3 = ((MediaPrintableArea[])(MediaPrintableArea[])localObject2)[0];
        else if (localObject2 instanceof MediaPrintableArea)
          localMediaPrintableArea3 = (MediaPrintableArea)localObject2;
      }
      if (localMediaPrintableArea3 == null)
        localMediaPrintableArea3 = new MediaPrintableArea(0F, 0F, localMediaSize.getX(this.units), localMediaSize.getY(this.units), this.units);
      float f1 = localMediaSize.getX(25400);
      float f2 = localMediaSize.getY(25400);
      float f3 = 5.0F;
      if (f1 > f3)
        f4 = 1F;
      else
        f4 = f1 / f3;
      if (f2 > f3)
        f5 = 1F;
      else
        f5 = f2 / f3;
      if (localMediaPrintableArea2 == null)
      {
        localMediaPrintableArea2 = new MediaPrintableArea(f4, f5, f1 - 2F * f4, f2 - 2F * f5, 25400);
        ServiceDialog.access$1200(this.this$0).add(localMediaPrintableArea2);
      }
      float f6 = localMediaPrintableArea2.getX(this.units);
      float f7 = localMediaPrintableArea2.getY(this.units);
      float f8 = localMediaPrintableArea2.getWidth(this.units);
      float f9 = localMediaPrintableArea2.getHeight(this.units);
      float f10 = localMediaPrintableArea3.getX(this.units);
      float f11 = localMediaPrintableArea3.getY(this.units);
      float f12 = localMediaPrintableArea3.getWidth(this.units);
      float f13 = localMediaPrintableArea3.getHeight(this.units);
      int i = 0;
      f1 = localMediaSize.getX(this.units);
      f2 = localMediaSize.getY(this.units);
      if (this.lmVal >= 0F)
      {
        i = 1;
        if (this.lmVal + this.rmVal > f1)
        {
          if (f8 > f12)
            f8 = f12;
          f6 = (f1 - f8) / 2F;
        }
        else
        {
          f6 = (this.lmVal >= f10) ? this.lmVal : f10;
          f8 = f1 - f6 - this.rmVal;
        }
        if (this.tmVal + this.bmVal > f2)
        {
          if (f9 > f13)
            f9 = f13;
          f7 = (f2 - f9) / 2F;
        }
        else
        {
          f7 = (this.tmVal >= f11) ? this.tmVal : f11;
          f9 = f2 - f7 - this.bmVal;
        }
      }
      if (f6 < f10)
      {
        i = 1;
        f6 = f10;
      }
      if (f7 < f11)
      {
        i = 1;
        f7 = f11;
      }
      if (f8 > f12)
      {
        i = 1;
        f8 = f12;
      }
      if (f9 > f13)
      {
        i = 1;
        f9 = f13;
      }
      if ((f6 + f8 > f10 + f12) || (f8 <= 0F))
      {
        i = 1;
        f6 = f10;
        f8 = f12;
      }
      if ((f7 + f9 > f11 + f13) || (f9 <= 0F))
      {
        i = 1;
        f7 = f11;
        f9 = f13;
      }
      if (i != 0)
      {
        localMediaPrintableArea2 = new MediaPrintableArea(f6, f7, f8, f9, this.units);
        ServiceDialog.access$1200(this.this$0).add(localMediaPrintableArea2);
      }
      this.lmVal = f6;
      this.tmVal = f7;
      this.rmVal = (localMediaSize.getX(this.units) - f6 - f8);
      this.bmVal = (localMediaSize.getY(this.units) - f7 - f9);
      this.lmObj = new Float(this.lmVal);
      this.rmObj = new Float(this.rmVal);
      this.tmObj = new Float(this.tmVal);
      this.bmObj = new Float(this.bmVal);
      OrientationRequested localOrientationRequested1 = OrientationRequested.class;
      OrientationRequested localOrientationRequested2 = (OrientationRequested)ServiceDialog.access$1200(this.this$0).get(localOrientationRequested1);
      if (localOrientationRequested2 == null)
        localOrientationRequested2 = (OrientationRequested)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localOrientationRequested1);
      if (localOrientationRequested2 == OrientationRequested.REVERSE_PORTRAIT)
      {
        localFloat = this.lmObj;
        this.lmObj = this.rmObj;
        this.rmObj = localFloat;
        localFloat = this.tmObj;
        this.tmObj = this.bmObj;
        this.bmObj = localFloat;
      }
      else if (localOrientationRequested2 == OrientationRequested.LANDSCAPE)
      {
        localFloat = this.lmObj;
        this.lmObj = this.bmObj;
        this.bmObj = this.rmObj;
        this.rmObj = this.tmObj;
        this.tmObj = localFloat;
      }
      else if (localOrientationRequested2 == OrientationRequested.REVERSE_LANDSCAPE)
      {
        localFloat = this.lmObj;
        this.lmObj = this.tmObj;
        this.tmObj = this.rmObj;
        this.rmObj = this.bmObj;
        this.bmObj = localFloat;
      }
      this.leftMargin.setValue(this.lmObj);
      this.rightMargin.setValue(this.rmObj);
      this.topMargin.setValue(this.tmObj);
      this.bottomMargin.setValue(this.bmObj);
    }
  }

  private class MediaPanel extends JPanel
  implements ItemListener
  {
    private final String strTitle = ServiceDialog.getMsg("border.media");
    private JLabel lblSize;
    private JLabel lblSource;
    private JComboBox cbSize;
    private JComboBox cbSource;
    private Vector sizes = new Vector();
    private Vector sources = new Vector();
    private ServiceDialog.MarginsPanel pnlMargins = null;

    public MediaPanel()
    {
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      setLayout(localGridBagLayout);
      setBorder(BorderFactory.createTitledBorder(this.strTitle));
      this.cbSize = new JComboBox();
      this.cbSource = new JComboBox();
      localGridBagConstraints.fill = 1;
      localGridBagConstraints.insets = ServiceDialog.access$700();
      localGridBagConstraints.weighty = 1D;
      localGridBagConstraints.weightx = 0D;
      this.lblSize = new JLabel(ServiceDialog.getMsg("label.size"), 11);
      this.lblSize.setDisplayedMnemonic(ServiceDialog.access$800("label.size"));
      this.lblSize.setLabelFor(this.cbSize);
      ServiceDialog.access$300(this.lblSize, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.weightx = 1D;
      localGridBagConstraints.gridwidth = 0;
      ServiceDialog.access$300(this.cbSize, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.weightx = 0D;
      localGridBagConstraints.gridwidth = 1;
      this.lblSource = new JLabel(ServiceDialog.getMsg("label.source"), 11);
      this.lblSource.setDisplayedMnemonic(ServiceDialog.access$800("label.source"));
      this.lblSource.setLabelFor(this.cbSource);
      ServiceDialog.access$300(this.lblSource, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = 0;
      ServiceDialog.access$300(this.cbSource, this, localGridBagLayout, localGridBagConstraints);
    }

    private String getMediaName()
    {
      String str;
      try
      {
        str = paramString.replace(' ', '-');
        str = str.replace('#', 'n');
        return ServiceDialog.access$100().getString(str);
      }
      catch (MissingResourceException localMissingResourceException)
      {
      }
      return paramString;
    }

    public void itemStateChanged()
    {
      Object localObject = paramItemEvent.getSource();
      if (paramItemEvent.getStateChange() == 1)
      {
        int i;
        int j;
        if (localObject == this.cbSize)
        {
          i = this.cbSize.getSelectedIndex();
          if ((i >= 0) && (i < this.sizes.size()))
          {
            if ((this.cbSource.getItemCount() > 1) && (this.cbSource.getSelectedIndex() >= 1))
            {
              j = this.cbSource.getSelectedIndex() - 1;
              MediaTray localMediaTray = (MediaTray)this.sources.get(j);
              ServiceDialog.access$1200(this.this$0).add(new SunAlternateMedia(localMediaTray));
            }
            ServiceDialog.access$1200(this.this$0).add((MediaSizeName)this.sizes.get(i));
          }
        }
        else if (localObject == this.cbSource)
        {
          i = this.cbSource.getSelectedIndex();
          if ((i >= 1) && (i < this.sources.size() + 1))
          {
            ServiceDialog.access$1200(this.this$0).remove(SunAlternateMedia.class);
            ServiceDialog.access$1200(this.this$0).add((MediaTray)this.sources.get(i - 1));
          }
          else if (i == 0)
          {
            ServiceDialog.access$1200(this.this$0).remove(SunAlternateMedia.class);
            if (this.cbSize.getItemCount() > 0)
            {
              j = this.cbSize.getSelectedIndex();
              ServiceDialog.access$1200(this.this$0).add((MediaSizeName)this.sizes.get(j));
            }
          }
        }
        if (this.pnlMargins != null)
          this.pnlMargins.updateInfo();
      }
    }

    public void addMediaListener()
    {
      this.pnlMargins = paramMarginsPanel;
    }

    public void updateInfo()
    {
      Object localObject2;
      Object localObject4;
      Media localMedia = Media.class;
      SunAlternateMedia localSunAlternateMedia = SunAlternateMedia.class;
      boolean bool1 = false;
      this.cbSize.removeItemListener(this);
      this.cbSize.removeAllItems();
      this.cbSource.removeItemListener(this);
      this.cbSource.removeAllItems();
      this.cbSource.addItem(getMediaName("auto-select"));
      this.sizes.clear();
      this.sources.clear();
      if (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localMedia))
      {
        bool1 = true;
        Object localObject1 = ServiceDialog.access$400(this.this$0).getSupportedAttributeValues(localMedia, ServiceDialog.access$1600(this.this$0), ServiceDialog.access$1200(this.this$0));
        if (localObject1 instanceof Media[])
        {
          localObject2 = (Media[])(Media[])localObject1;
          for (int i = 0; i < localObject2.length; ++i)
          {
            localObject4 = localObject2[i];
            if (localObject4 instanceof MediaSizeName)
            {
              this.sizes.add(localObject4);
              this.cbSize.addItem(getMediaName(((Media)localObject4).toString()));
            }
            else if (localObject4 instanceof MediaTray)
            {
              this.sources.add(localObject4);
              this.cbSource.addItem(getMediaName(((Media)localObject4).toString()));
            }
          }
        }
      }
      boolean bool2 = (bool1) && (this.sizes.size() > 0);
      this.lblSize.setEnabled(bool2);
      this.cbSize.setEnabled(bool2);
      if (ServiceDialog.access$1500(this.this$0))
      {
        this.cbSource.setEnabled(false);
        this.lblSource.setEnabled(false);
      }
      else
      {
        this.cbSource.setEnabled(bool1);
      }
      if (bool1)
      {
        localObject2 = (Media)ServiceDialog.access$1200(this.this$0).get(localMedia);
        if ((localObject2 == null) || (!(ServiceDialog.access$400(this.this$0).isAttributeValueSupported((Attribute)localObject2, ServiceDialog.access$1600(this.this$0), ServiceDialog.access$1200(this.this$0)))))
        {
          localObject2 = (Media)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localMedia);
          if ((localObject2 == null) && (this.sizes.size() > 0))
            localObject2 = (Media)this.sizes.get(0);
          if (localObject2 != null)
            ServiceDialog.access$1200(this.this$0).add((Attribute)localObject2);
        }
        if (localObject2 != null)
        {
          if (localObject2 instanceof MediaSizeName)
          {
            localObject3 = (MediaSizeName)localObject2;
            this.cbSize.setSelectedIndex(this.sizes.indexOf(localObject3));
          }
          else if (localObject2 instanceof MediaTray)
          {
            localObject3 = (MediaTray)localObject2;
            this.cbSource.setSelectedIndex(this.sources.indexOf(localObject3) + 1);
          }
        }
        else
        {
          this.cbSize.setSelectedIndex((this.sizes.size() > 0) ? 0 : -1);
          this.cbSource.setSelectedIndex(0);
        }
        Object localObject3 = (SunAlternateMedia)ServiceDialog.access$1200(this.this$0).get(localSunAlternateMedia);
        if (localObject3 != null)
        {
          localObject4 = ((SunAlternateMedia)localObject3).getMedia();
          if (localObject4 instanceof MediaTray)
          {
            MediaTray localMediaTray = (MediaTray)localObject4;
            this.cbSource.setSelectedIndex(this.sources.indexOf(localMediaTray) + 1);
          }
        }
        int j = this.cbSize.getSelectedIndex();
        if ((j >= 0) && (j < this.sizes.size()))
          ServiceDialog.access$1200(this.this$0).add((MediaSizeName)this.sizes.get(j));
        j = this.cbSource.getSelectedIndex();
        if ((j >= 1) && (j < this.sources.size() + 1))
          ServiceDialog.access$1200(this.this$0).add((MediaTray)this.sources.get(j - 1));
      }
      this.cbSize.addItemListener(this);
      this.cbSource.addItemListener(this);
    }
  }

  private class OrientationPanel extends JPanel
  implements ActionListener
  {
    private final String strTitle = ServiceDialog.getMsg("border.orientation");
    private ServiceDialog.IconRadioButton rbPortrait;
    private ServiceDialog.IconRadioButton rbLandscape;
    private ServiceDialog.IconRadioButton rbRevPortrait;
    private ServiceDialog.IconRadioButton rbRevLandscape;
    private ServiceDialog.MarginsPanel pnlMargins = null;

    public OrientationPanel()
    {
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      setLayout(localGridBagLayout);
      setBorder(BorderFactory.createTitledBorder(this.strTitle));
      localGridBagConstraints.fill = 1;
      localGridBagConstraints.insets = ServiceDialog.access$700();
      localGridBagConstraints.weighty = 1D;
      localGridBagConstraints.gridwidth = 0;
      ButtonGroup localButtonGroup = new ButtonGroup();
      this.rbPortrait = new ServiceDialog.IconRadioButton(paramServiceDialog, "radiobutton.portrait", "orientPortrait.png", true, localButtonGroup, this);
      this.rbPortrait.addActionListener(this);
      ServiceDialog.access$300(this.rbPortrait, this, localGridBagLayout, localGridBagConstraints);
      this.rbLandscape = new ServiceDialog.IconRadioButton(paramServiceDialog, "radiobutton.landscape", "orientLandscape.png", false, localButtonGroup, this);
      this.rbLandscape.addActionListener(this);
      ServiceDialog.access$300(this.rbLandscape, this, localGridBagLayout, localGridBagConstraints);
      this.rbRevPortrait = new ServiceDialog.IconRadioButton(paramServiceDialog, "radiobutton.revportrait", "orientRevPortrait.png", false, localButtonGroup, this);
      this.rbRevPortrait.addActionListener(this);
      ServiceDialog.access$300(this.rbRevPortrait, this, localGridBagLayout, localGridBagConstraints);
      this.rbRevLandscape = new ServiceDialog.IconRadioButton(paramServiceDialog, "radiobutton.revlandscape", "orientRevLandscape.png", false, localButtonGroup, this);
      this.rbRevLandscape.addActionListener(this);
      ServiceDialog.access$300(this.rbRevLandscape, this, localGridBagLayout, localGridBagConstraints);
    }

    public void actionPerformed()
    {
      Object localObject = paramActionEvent.getSource();
      if (this.rbPortrait.isSameAs(localObject))
        ServiceDialog.access$1200(this.this$0).add(OrientationRequested.PORTRAIT);
      else if (this.rbLandscape.isSameAs(localObject))
        ServiceDialog.access$1200(this.this$0).add(OrientationRequested.LANDSCAPE);
      else if (this.rbRevPortrait.isSameAs(localObject))
        ServiceDialog.access$1200(this.this$0).add(OrientationRequested.REVERSE_PORTRAIT);
      else if (this.rbRevLandscape.isSameAs(localObject))
        ServiceDialog.access$1200(this.this$0).add(OrientationRequested.REVERSE_LANDSCAPE);
      if (this.pnlMargins != null)
        this.pnlMargins.updateInfo();
    }

    void addOrientationListener()
    {
      this.pnlMargins = paramMarginsPanel;
    }

    public void updateInfo()
    {
      OrientationRequested localOrientationRequested = OrientationRequested.class;
      boolean bool1 = false;
      boolean bool2 = false;
      boolean bool3 = false;
      boolean bool4 = false;
      if (ServiceDialog.access$1500(this.this$0))
      {
        bool1 = true;
        bool2 = true;
      }
      else if (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localOrientationRequested))
      {
        Object localObject1 = ServiceDialog.access$400(this.this$0).getSupportedAttributeValues(localOrientationRequested, ServiceDialog.access$1600(this.this$0), ServiceDialog.access$1200(this.this$0));
        if (localObject1 instanceof OrientationRequested[])
        {
          localObject2 = (OrientationRequested[])(OrientationRequested[])localObject1;
          for (int i = 0; i < localObject2.length; ++i)
          {
            Object localObject3 = localObject2[i];
            if (localObject3 == OrientationRequested.PORTRAIT)
              bool1 = true;
            else if (localObject3 == OrientationRequested.LANDSCAPE)
              bool2 = true;
            else if (localObject3 == OrientationRequested.REVERSE_PORTRAIT)
              bool3 = true;
            else if (localObject3 == OrientationRequested.REVERSE_LANDSCAPE)
              bool4 = true;
          }
        }
        this.rbPortrait.setEnabled(bool1);
        this.rbLandscape.setEnabled(bool2);
        this.rbRevPortrait.setEnabled(bool3);
        this.rbRevLandscape.setEnabled(bool4);
        Object localObject2 = (OrientationRequested)ServiceDialog.access$1200(this.this$0).get(localOrientationRequested);
        if ((localObject2 == null) || (!(ServiceDialog.access$400(this.this$0).isAttributeValueSupported((Attribute)localObject2, ServiceDialog.access$1600(this.this$0), ServiceDialog.access$1200(this.this$0)))))
        {
          localObject2 = (OrientationRequested)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localOrientationRequested);
          if (!(ServiceDialog.access$400(this.this$0).isAttributeValueSupported((Attribute)localObject2, ServiceDialog.access$1600(this.this$0), ServiceDialog.access$1200(this.this$0))))
          {
            localObject2 = null;
            localObject1 = ServiceDialog.access$400(this.this$0).getSupportedAttributeValues(localOrientationRequested, ServiceDialog.access$1600(this.this$0), ServiceDialog.access$1200(this.this$0));
            if (localObject1 instanceof OrientationRequested[])
            {
              OrientationRequested[] arrayOfOrientationRequested = (OrientationRequested[])(OrientationRequested[])localObject1;
              if (arrayOfOrientationRequested.length > 1)
                localObject2 = arrayOfOrientationRequested[0];
            }
          }
          if (localObject2 == null)
            localObject2 = OrientationRequested.PORTRAIT;
          ServiceDialog.access$1200(this.this$0).add((Attribute)localObject2);
        }
        if (localObject2 == OrientationRequested.PORTRAIT)
          this.rbPortrait.setSelected(true);
        else if (localObject2 == OrientationRequested.LANDSCAPE)
          this.rbLandscape.setSelected(true);
        else if (localObject2 == OrientationRequested.REVERSE_PORTRAIT)
          this.rbRevPortrait.setSelected(true);
        else
          this.rbRevLandscape.setSelected(true);
      }
      else
      {
        this.rbPortrait.setEnabled(bool1);
        this.rbLandscape.setEnabled(bool2);
        this.rbRevPortrait.setEnabled(bool3);
        this.rbRevLandscape.setEnabled(bool4);
      }
    }
  }

  private class PageSetupPanel extends JPanel
  {
    private ServiceDialog.MediaPanel pnlMedia;
    private ServiceDialog.OrientationPanel pnlOrientation;
    private ServiceDialog.MarginsPanel pnlMargins;

    public PageSetupPanel()
    {
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      setLayout(localGridBagLayout);
      localGridBagConstraints.fill = 1;
      localGridBagConstraints.insets = ServiceDialog.access$200();
      localGridBagConstraints.weightx = 1D;
      localGridBagConstraints.weighty = 1D;
      localGridBagConstraints.gridwidth = 0;
      this.pnlMedia = new ServiceDialog.MediaPanel(paramServiceDialog);
      ServiceDialog.access$300(this.pnlMedia, this, localGridBagLayout, localGridBagConstraints);
      this.pnlOrientation = new ServiceDialog.OrientationPanel(paramServiceDialog);
      localGridBagConstraints.gridwidth = -1;
      ServiceDialog.access$300(this.pnlOrientation, this, localGridBagLayout, localGridBagConstraints);
      this.pnlMargins = new ServiceDialog.MarginsPanel(paramServiceDialog);
      this.pnlOrientation.addOrientationListener(this.pnlMargins);
      this.pnlMedia.addMediaListener(this.pnlMargins);
      localGridBagConstraints.gridwidth = 0;
      ServiceDialog.access$300(this.pnlMargins, this, localGridBagLayout, localGridBagConstraints);
    }

    public void updateInfo()
    {
      this.pnlMedia.updateInfo();
      this.pnlOrientation.updateInfo();
      this.pnlMargins.updateInfo();
    }
  }

  private class PrintRangePanel extends JPanel
  implements ActionListener, FocusListener
  {
    private final String strTitle = ServiceDialog.getMsg("border.printrange");
    private final PageRanges prAll = new PageRanges(1, 2147483647);
    private JRadioButton rbAll;
    private JRadioButton rbPages;
    private JRadioButton rbSelect;
    private JFormattedTextField tfRangeFrom;
    private JFormattedTextField tfRangeTo;
    private JLabel lblRangeTo;
    private boolean prSupported;

    public PrintRangePanel()
    {
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      setLayout(localGridBagLayout);
      setBorder(BorderFactory.createTitledBorder(this.strTitle));
      localGridBagConstraints.fill = 1;
      localGridBagConstraints.insets = ServiceDialog.access$700();
      localGridBagConstraints.gridwidth = 0;
      ButtonGroup localButtonGroup = new ButtonGroup();
      JPanel localJPanel1 = new JPanel(new FlowLayout(3));
      this.rbAll = ServiceDialog.access$1400("radiobutton.rangeall", this);
      this.rbAll.setSelected(true);
      localButtonGroup.add(this.rbAll);
      localJPanel1.add(this.rbAll);
      ServiceDialog.access$300(localJPanel1, this, localGridBagLayout, localGridBagConstraints);
      JPanel localJPanel2 = new JPanel(new FlowLayout(3));
      this.rbPages = ServiceDialog.access$1400("radiobutton.rangepages", this);
      localButtonGroup.add(this.rbPages);
      localJPanel2.add(this.rbPages);
      DecimalFormat localDecimalFormat = new DecimalFormat("####0");
      localDecimalFormat.setMinimumFractionDigits(0);
      localDecimalFormat.setMaximumFractionDigits(0);
      localDecimalFormat.setMinimumIntegerDigits(0);
      localDecimalFormat.setMaximumIntegerDigits(5);
      localDecimalFormat.setParseIntegerOnly(true);
      localDecimalFormat.setDecimalSeparatorAlwaysShown(false);
      NumberFormatter localNumberFormatter1 = new NumberFormatter(localDecimalFormat);
      localNumberFormatter1.setMinimum(new Integer(1));
      localNumberFormatter1.setMaximum(new Integer(2147483647));
      localNumberFormatter1.setAllowsInvalid(true);
      localNumberFormatter1.setCommitsOnValidEdit(true);
      this.tfRangeFrom = new JFormattedTextField(localNumberFormatter1);
      this.tfRangeFrom.setColumns(4);
      this.tfRangeFrom.setEnabled(false);
      this.tfRangeFrom.addActionListener(this);
      this.tfRangeFrom.addFocusListener(this);
      this.tfRangeFrom.setFocusLostBehavior(3);
      this.tfRangeFrom.getAccessibleContext().setAccessibleName(ServiceDialog.getMsg("radiobutton.rangepages"));
      localJPanel2.add(this.tfRangeFrom);
      this.lblRangeTo = new JLabel(ServiceDialog.getMsg("label.rangeto"));
      this.lblRangeTo.setEnabled(false);
      localJPanel2.add(this.lblRangeTo);
      try
      {
        localNumberFormatter2 = (NumberFormatter)localNumberFormatter1.clone();
      }
      catch (CloneNotSupportedException localCloneNotSupportedException)
      {
        localNumberFormatter2 = new NumberFormatter();
      }
      this.tfRangeTo = new JFormattedTextField(localNumberFormatter2);
      this.tfRangeTo.setColumns(4);
      this.tfRangeTo.setEnabled(false);
      this.tfRangeTo.addFocusListener(this);
      this.tfRangeTo.getAccessibleContext().setAccessibleName(ServiceDialog.getMsg("label.rangeto"));
      localJPanel2.add(this.tfRangeTo);
      ServiceDialog.access$300(localJPanel2, this, localGridBagLayout, localGridBagConstraints);
    }

    public void actionPerformed()
    {
      Object localObject = paramActionEvent.getSource();
      SunPageSelection localSunPageSelection = SunPageSelection.ALL;
      setupRangeWidgets();
      if (localObject == this.rbAll)
      {
        ServiceDialog.access$1200(this.this$0).add(this.prAll);
      }
      else if (localObject == this.rbSelect)
      {
        localSunPageSelection = SunPageSelection.SELECTION;
      }
      else if ((localObject == this.rbPages) || (localObject == this.tfRangeFrom) || (localObject == this.tfRangeTo))
      {
        updateRangeAttribute();
        localSunPageSelection = SunPageSelection.RANGE;
      }
      if (ServiceDialog.access$1500(this.this$0))
        ServiceDialog.access$1200(this.this$0).add(localSunPageSelection);
    }

    public void focusLost()
    {
      Object localObject = paramFocusEvent.getSource();
      if ((localObject == this.tfRangeFrom) || (localObject == this.tfRangeTo))
        updateRangeAttribute();
    }

    public void focusGained()
    {
    }

    private void setupRangeWidgets()
    {
      boolean bool = (this.rbPages.isSelected()) && (this.prSupported);
      this.tfRangeFrom.setEnabled(bool);
      this.tfRangeTo.setEnabled(bool);
      this.lblRangeTo.setEnabled(bool);
    }

    private void updateRangeAttribute()
    {
      int i;
      int j;
      String str1 = this.tfRangeFrom.getText();
      String str2 = this.tfRangeTo.getText();
      try
      {
        i = Integer.parseInt(str1);
      }
      catch (NumberFormatException localNumberFormatException1)
      {
        i = 1;
      }
      try
      {
        j = Integer.parseInt(str2);
      }
      catch (NumberFormatException localNumberFormatException2)
      {
        j = i;
      }
      if (i < 1)
      {
        i = 1;
        this.tfRangeFrom.setValue(new Integer(1));
      }
      if (j < i)
      {
        j = i;
        this.tfRangeTo.setValue(new Integer(i));
      }
      PageRanges localPageRanges = new PageRanges(i, j);
      ServiceDialog.access$1200(this.this$0).add(localPageRanges);
    }

    public void updateInfo()
    {
      PageRanges localPageRanges1 = PageRanges.class;
      this.prSupported = false;
      if ((ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localPageRanges1)) || (ServiceDialog.access$1500(this.this$0)))
        this.prSupported = true;
      SunPageSelection localSunPageSelection = SunPageSelection.ALL;
      int i = 1;
      int j = 1;
      PageRanges localPageRanges2 = (PageRanges)ServiceDialog.access$1200(this.this$0).get(localPageRanges1);
      if ((localPageRanges2 != null) && (!(localPageRanges2.equals(this.prAll))))
      {
        localSunPageSelection = SunPageSelection.RANGE;
        int[][] arrayOfInt = localPageRanges2.getMembers();
        if ((arrayOfInt.length > 0) && (arrayOfInt[0].length > 1))
        {
          i = arrayOfInt[0][0];
          j = arrayOfInt[0][1];
        }
      }
      if (ServiceDialog.access$1500(this.this$0))
        localSunPageSelection = (SunPageSelection)ServiceDialog.access$1200(this.this$0).get(SunPageSelection.class);
      if (localSunPageSelection == SunPageSelection.ALL)
      {
        this.rbAll.setSelected(true);
      }
      else
      {
        if (localSunPageSelection == SunPageSelection.SELECTION)
          break label186:
        this.rbPages.setSelected(true);
      }
      label186: this.tfRangeFrom.setValue(new Integer(i));
      this.tfRangeTo.setValue(new Integer(j));
      this.rbAll.setEnabled(this.prSupported);
      this.rbPages.setEnabled(this.prSupported);
      setupRangeWidgets();
    }
  }

  private class PrintServicePanel extends JPanel
  implements ActionListener, ItemListener, PopupMenuListener
  {
    private final String strTitle = ServiceDialog.getMsg("border.printservice");
    private FilePermission printToFilePermission;
    private JButton btnProperties;
    private JCheckBox cbPrintToFile;
    private JComboBox cbName;
    private JLabel lblType;
    private JLabel lblStatus;
    private JLabel lblInfo;
    private ServiceUIFactory uiFactory;
    private boolean changedService = false;
    private boolean filePermission;

    public PrintServicePanel()
    {
      this.uiFactory = ServiceDialog.access$400(paramServiceDialog).getServiceUIFactory();
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      setLayout(localGridBagLayout);
      setBorder(BorderFactory.createTitledBorder(this.strTitle));
      String[] arrayOfString = new String[ServiceDialog.access$500(paramServiceDialog).length];
      for (int i = 0; i < arrayOfString.length; ++i)
        arrayOfString[i] = ServiceDialog.access$500(paramServiceDialog)[i].getName();
      this.cbName = new JComboBox(arrayOfString);
      this.cbName.setSelectedIndex(ServiceDialog.access$600(paramServiceDialog));
      this.cbName.addItemListener(this);
      this.cbName.addPopupMenuListener(this);
      localGridBagConstraints.fill = 1;
      localGridBagConstraints.insets = ServiceDialog.access$700();
      localGridBagConstraints.weightx = 0D;
      JLabel localJLabel = new JLabel(ServiceDialog.getMsg("label.psname"), 11);
      localJLabel.setDisplayedMnemonic(ServiceDialog.access$800("label.psname"));
      localJLabel.setLabelFor(this.cbName);
      ServiceDialog.access$300(localJLabel, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.weightx = 1D;
      localGridBagConstraints.gridwidth = -1;
      ServiceDialog.access$300(this.cbName, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.weightx = 0D;
      localGridBagConstraints.gridwidth = 0;
      this.btnProperties = ServiceDialog.access$900("button.properties", this);
      ServiceDialog.access$300(this.btnProperties, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.weighty = 1D;
      this.lblStatus = addLabel(ServiceDialog.getMsg("label.status"), localGridBagLayout, localGridBagConstraints);
      this.lblStatus.setLabelFor(null);
      this.lblType = addLabel(ServiceDialog.getMsg("label.pstype"), localGridBagLayout, localGridBagConstraints);
      this.lblType.setLabelFor(null);
      localGridBagConstraints.gridwidth = 1;
      ServiceDialog.access$300(new JLabel(ServiceDialog.getMsg("label.info"), 11), this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = -1;
      this.lblInfo = new JLabel();
      this.lblInfo.setLabelFor(null);
      ServiceDialog.access$300(this.lblInfo, this, localGridBagLayout, localGridBagConstraints);
      localGridBagConstraints.gridwidth = 0;
      this.cbPrintToFile = ServiceDialog.access$1000("checkbox.printtofile", this);
      ServiceDialog.access$300(this.cbPrintToFile, this, localGridBagLayout, localGridBagConstraints);
      this.filePermission = allowedToPrintToFile();
    }

    public boolean isPrintToFileSelected()
    {
      return this.cbPrintToFile.isSelected();
    }

    private JLabel addLabel(, GridBagLayout paramGridBagLayout, GridBagConstraints paramGridBagConstraints)
    {
      paramGridBagConstraints.gridwidth = 1;
      ServiceDialog.access$300(new JLabel(paramString, 11), this, paramGridBagLayout, paramGridBagConstraints);
      paramGridBagConstraints.gridwidth = 0;
      JLabel localJLabel = new JLabel();
      ServiceDialog.access$300(localJLabel, this, paramGridBagLayout, paramGridBagConstraints);
      return localJLabel;
    }

    public void actionPerformed()
    {
      Object localObject = paramActionEvent.getSource();
      if ((localObject == this.btnProperties) && (this.uiFactory != null))
      {
        JDialog localJDialog = (JDialog)this.uiFactory.getUI(3, "javax.swing.JDialog");
        if (localJDialog != null)
          localJDialog.show();
        else
          this.btnProperties.setEnabled(false);
      }
    }

    public void itemStateChanged()
    {
      if (paramItemEvent.getStateChange() == 1)
      {
        int i = this.cbName.getSelectedIndex();
        if ((i >= 0) && (i < ServiceDialog.access$500(this.this$0).length) && (!(ServiceDialog.access$500(this.this$0)[i].equals(ServiceDialog.access$400(this.this$0)))))
        {
          ServiceDialog.access$402(this.this$0, ServiceDialog.access$500(this.this$0)[i]);
          this.uiFactory = ServiceDialog.access$400(this.this$0).getServiceUIFactory();
          this.changedService = true;
          Destination localDestination = (Destination)ServiceDialog.access$1100(this.this$0).get(Destination.class);
          if ((((localDestination != null) || (isPrintToFileSelected()))) && (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(Destination.class)))
            if (localDestination != null)
            {
              ServiceDialog.access$1200(this.this$0).add(localDestination);
            }
            else
            {
              localDestination = (Destination)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(Destination.class);
              if (localDestination == null)
                try
                {
                  localDestination = new Destination(new URI("file:out.prn"));
                }
                catch (URISyntaxException localURISyntaxException)
                {
                }
              if (localDestination != null)
                ServiceDialog.access$1200(this.this$0).add(localDestination);
            }
          else
            ServiceDialog.access$1200(this.this$0).remove(Destination.class);
        }
      }
    }

    public void popupMenuWillBecomeVisible()
    {
      this.changedService = false;
    }

    public void popupMenuWillBecomeInvisible()
    {
      if (this.changedService)
      {
        this.changedService = false;
        ServiceDialog.access$1300(this.this$0);
      }
    }

    public void popupMenuCanceled()
    {
    }

    private boolean allowedToPrintToFile()
    {
      try
      {
        throwPrintToFile();
        return true;
      }
      catch (SecurityException localSecurityException)
      {
      }
      return false;
    }

    private void throwPrintToFile()
    {
      SecurityManager localSecurityManager = System.getSecurityManager();
      if (localSecurityManager != null)
      {
        if (this.printToFilePermission == null)
          this.printToFilePermission = new FilePermission("<<ALL FILES>>", "read,write");
        localSecurityManager.checkPermission(this.printToFilePermission);
      }
    }

    public void updateInfo()
    {
      Destination localDestination1 = Destination.class;
      int i = 0;
      int j = 0;
      int k = (this.filePermission) ? allowedToPrintToFile() : 0;
      if (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localDestination1))
        i = 1;
      Destination localDestination2 = (Destination)ServiceDialog.access$1200(this.this$0).get(localDestination1);
      if (localDestination2 != null)
        j = 1;
      this.cbPrintToFile.setEnabled((i != 0) && (k != 0));
      this.cbPrintToFile.setSelected((j != 0) && (k != 0) && (i != 0));
      javax.print.attribute.PrintServiceAttribute localPrintServiceAttribute1 = ServiceDialog.access$400(this.this$0).getAttribute(PrinterMakeAndModel.class);
      if (localPrintServiceAttribute1 != null)
        this.lblType.setText(localPrintServiceAttribute1.toString());
      javax.print.attribute.PrintServiceAttribute localPrintServiceAttribute2 = ServiceDialog.access$400(this.this$0).getAttribute(PrinterIsAcceptingJobs.class);
      if (localPrintServiceAttribute2 != null)
        this.lblStatus.setText(ServiceDialog.getMsg(localPrintServiceAttribute2.toString()));
      javax.print.attribute.PrintServiceAttribute localPrintServiceAttribute3 = ServiceDialog.access$400(this.this$0).getAttribute(PrinterInfo.class);
      if (localPrintServiceAttribute3 != null)
        this.lblInfo.setText(localPrintServiceAttribute3.toString());
      this.btnProperties.setEnabled(this.uiFactory != null);
    }
  }

  private class QualityPanel extends JPanel
  implements ActionListener
  {
    private final String strTitle = ServiceDialog.getMsg("border.quality");
    private JRadioButton rbDraft;
    private JRadioButton rbNormal;
    private JRadioButton rbHigh;

    public QualityPanel()
    {
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      setLayout(localGridBagLayout);
      setBorder(BorderFactory.createTitledBorder(this.strTitle));
      localGridBagConstraints.fill = 1;
      localGridBagConstraints.gridwidth = 0;
      localGridBagConstraints.weighty = 1D;
      ButtonGroup localButtonGroup = new ButtonGroup();
      this.rbDraft = ServiceDialog.access$1400("radiobutton.draftq", this);
      localButtonGroup.add(this.rbDraft);
      ServiceDialog.access$300(this.rbDraft, this, localGridBagLayout, localGridBagConstraints);
      this.rbNormal = ServiceDialog.access$1400("radiobutton.normalq", this);
      this.rbNormal.setSelected(true);
      localButtonGroup.add(this.rbNormal);
      ServiceDialog.access$300(this.rbNormal, this, localGridBagLayout, localGridBagConstraints);
      this.rbHigh = ServiceDialog.access$1400("radiobutton.highq", this);
      localButtonGroup.add(this.rbHigh);
      ServiceDialog.access$300(this.rbHigh, this, localGridBagLayout, localGridBagConstraints);
    }

    public void actionPerformed()
    {
      Object localObject = paramActionEvent.getSource();
      if (localObject == this.rbDraft)
        ServiceDialog.access$1200(this.this$0).add(PrintQuality.DRAFT);
      else if (localObject == this.rbNormal)
        ServiceDialog.access$1200(this.this$0).add(PrintQuality.NORMAL);
      else if (localObject == this.rbHigh)
        ServiceDialog.access$1200(this.this$0).add(PrintQuality.HIGH);
    }

    public void updateInfo()
    {
      PrintQuality localPrintQuality1 = PrintQuality.class;
      boolean bool1 = false;
      boolean bool2 = false;
      boolean bool3 = false;
      if (ServiceDialog.access$1500(this.this$0))
      {
        bool1 = true;
        bool2 = true;
        bool3 = true;
      }
      else if (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localPrintQuality1))
      {
        localObject = ServiceDialog.access$400(this.this$0).getSupportedAttributeValues(localPrintQuality1, ServiceDialog.access$1600(this.this$0), ServiceDialog.access$1200(this.this$0));
        if (localObject instanceof PrintQuality[])
        {
          PrintQuality[] arrayOfPrintQuality = (PrintQuality[])(PrintQuality[])localObject;
          for (int i = 0; i < arrayOfPrintQuality.length; ++i)
          {
            PrintQuality localPrintQuality2 = arrayOfPrintQuality[i];
            if (localPrintQuality2 == PrintQuality.DRAFT)
              bool1 = true;
            else if (localPrintQuality2 == PrintQuality.NORMAL)
              bool2 = true;
            else if (localPrintQuality2 == PrintQuality.HIGH)
              bool3 = true;
          }
        }
      }
      this.rbDraft.setEnabled(bool1);
      this.rbNormal.setEnabled(bool2);
      this.rbHigh.setEnabled(bool3);
      Object localObject = (PrintQuality)ServiceDialog.access$1200(this.this$0).get(localPrintQuality1);
      if (localObject == null)
      {
        localObject = (PrintQuality)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localPrintQuality1);
        if (localObject == null)
          localObject = PrintQuality.NORMAL;
      }
      if (localObject == PrintQuality.DRAFT)
        this.rbDraft.setSelected(true);
      else if (localObject == PrintQuality.NORMAL)
        this.rbNormal.setSelected(true);
      else
        this.rbHigh.setSelected(true);
    }
  }

  private class SidesPanel extends JPanel
  implements ActionListener
  {
    private final String strTitle = ServiceDialog.getMsg("border.sides");
    private ServiceDialog.IconRadioButton rbOneSide;
    private ServiceDialog.IconRadioButton rbTumble;
    private ServiceDialog.IconRadioButton rbDuplex;

    public SidesPanel()
    {
      GridBagLayout localGridBagLayout = new GridBagLayout();
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      setLayout(localGridBagLayout);
      setBorder(BorderFactory.createTitledBorder(this.strTitle));
      localGridBagConstraints.fill = 1;
      localGridBagConstraints.insets = ServiceDialog.access$700();
      localGridBagConstraints.weighty = 1D;
      localGridBagConstraints.gridwidth = 0;
      ButtonGroup localButtonGroup = new ButtonGroup();
      this.rbOneSide = new ServiceDialog.IconRadioButton(paramServiceDialog, "radiobutton.oneside", "oneside.png", true, localButtonGroup, this);
      this.rbOneSide.addActionListener(this);
      ServiceDialog.access$300(this.rbOneSide, this, localGridBagLayout, localGridBagConstraints);
      this.rbTumble = new ServiceDialog.IconRadioButton(paramServiceDialog, "radiobutton.tumble", "tumble.png", false, localButtonGroup, this);
      this.rbTumble.addActionListener(this);
      ServiceDialog.access$300(this.rbTumble, this, localGridBagLayout, localGridBagConstraints);
      this.rbDuplex = new ServiceDialog.IconRadioButton(paramServiceDialog, "radiobutton.duplex", "duplex.png", false, localButtonGroup, this);
      this.rbDuplex.addActionListener(this);
      localGridBagConstraints.gridwidth = 0;
      ServiceDialog.access$300(this.rbDuplex, this, localGridBagLayout, localGridBagConstraints);
    }

    public void actionPerformed()
    {
      Object localObject = paramActionEvent.getSource();
      if (this.rbOneSide.isSameAs(localObject))
        ServiceDialog.access$1200(this.this$0).add(Sides.ONE_SIDED);
      else if (this.rbTumble.isSameAs(localObject))
        ServiceDialog.access$1200(this.this$0).add(Sides.TUMBLE);
      else if (this.rbDuplex.isSameAs(localObject))
        ServiceDialog.access$1200(this.this$0).add(Sides.DUPLEX);
    }

    public void updateInfo()
    {
      Sides localSides1 = Sides.class;
      boolean bool1 = false;
      boolean bool2 = false;
      boolean bool3 = false;
      if (ServiceDialog.access$400(this.this$0).isAttributeCategorySupported(localSides1))
      {
        localObject = ServiceDialog.access$400(this.this$0).getSupportedAttributeValues(localSides1, ServiceDialog.access$1600(this.this$0), ServiceDialog.access$1200(this.this$0));
        if (localObject instanceof Sides[])
        {
          Sides[] arrayOfSides = (Sides[])(Sides[])localObject;
          for (int i = 0; i < arrayOfSides.length; ++i)
          {
            Sides localSides2 = arrayOfSides[i];
            if (localSides2 == Sides.ONE_SIDED)
              bool1 = true;
            else if (localSides2 == Sides.TUMBLE)
              bool2 = true;
            else if (localSides2 == Sides.DUPLEX)
              bool3 = true;
          }
        }
      }
      this.rbOneSide.setEnabled(bool1);
      this.rbTumble.setEnabled(bool2);
      this.rbDuplex.setEnabled(bool3);
      Object localObject = (Sides)ServiceDialog.access$1200(this.this$0).get(localSides1);
      if (localObject == null)
      {
        localObject = (Sides)ServiceDialog.access$400(this.this$0).getDefaultAttributeValue(localSides1);
        if (localObject == null)
          localObject = Sides.ONE_SIDED;
      }
      if (localObject == Sides.ONE_SIDED)
        this.rbOneSide.setSelected(true);
      else if (localObject == Sides.TUMBLE)
        this.rbTumble.setSelected(true);
      else
        this.rbDuplex.setSelected(true);
    }
  }

  private class ValidatingFileChooser extends JFileChooser
  {
    public void approveSelection()
    {
      boolean bool;
      java.io.File localFile1 = getSelectedFile();
      try
      {
        bool = localFile1.exists();
      }
      catch (SecurityException localSecurityException1)
      {
        bool = false;
      }
      if (bool)
      {
        int i = JOptionPane.showConfirmDialog(this, ServiceDialog.getMsg("dialog.overwrite"), ServiceDialog.getMsg("dialog.owtitle"), 0);
        if (i != 0)
          return;
      }
      try
      {
        if (localFile1.createNewFile())
          localFile1.delete();
      }
      catch (IOException localIOException)
      {
        JOptionPane.showMessageDialog(this, ServiceDialog.getMsg("dialog.writeerror") + " " + localFile1, ServiceDialog.getMsg("dialog.owtitle"), 2);
        return;
      }
      catch (SecurityException localSecurityException2)
      {
      }
      java.io.File localFile2 = localFile1.getParentFile();
      if (((localFile1.exists()) && (((!(localFile1.isFile())) || (!(localFile1.canWrite()))))) || ((localFile2 != null) && (((!(localFile2.exists())) || ((localFile2.exists()) && (!(localFile2.canWrite())))))))
      {
        JOptionPane.showMessageDialog(this, ServiceDialog.getMsg("dialog.writeerror") + " " + localFile1, ServiceDialog.getMsg("dialog.owtitle"), 2);
        return;
      }
      super.approveSelection();
    }
  }
}