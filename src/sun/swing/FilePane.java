package sun.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.security.AccessControlException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.DefaultRowSorter.ModelWrapper;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.basic.BasicDirectoryModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Position.Bias;
import sun.awt.shell.ShellFolder;
import sun.awt.shell.ShellFolder.Invoker;
import sun.awt.shell.ShellFolderColumnInfo;

public class FilePane extends JPanel
  implements java.beans.PropertyChangeListener
{
  public static final String ACTION_APPROVE_SELECTION = "approveSelection";
  public static final String ACTION_CANCEL = "cancelSelection";
  public static final String ACTION_EDIT_FILE_NAME = "editFileName";
  public static final String ACTION_REFRESH = "refresh";
  public static final String ACTION_CHANGE_TO_PARENT_DIRECTORY = "Go Up";
  public static final String ACTION_NEW_FOLDER = "New Folder";
  public static final String ACTION_VIEW_LIST = "viewTypeList";
  public static final String ACTION_VIEW_DETAILS = "viewTypeDetails";
  private Action[] actions;
  public static final int VIEWTYPE_LIST = 0;
  public static final int VIEWTYPE_DETAILS = 1;
  private static final int VIEWTYPE_COUNT = 2;
  private int viewType = -1;
  private JPanel[] viewPanels = new JPanel[2];
  private JPanel currentViewPanel;
  private String[] viewTypeActionNames;
  private JPopupMenu contextMenu;
  private JMenu viewMenu;
  private String viewMenuLabelText;
  private String refreshActionLabelText;
  private String newFolderActionLabelText;
  private String kiloByteString;
  private String megaByteString;
  private String gigaByteString;
  private static final Cursor waitCursor = Cursor.getPredefinedCursor(3);
  private FocusListener editorFocusListener = new FocusAdapter(this)
  {
    public void focusLost()
    {
      if (!(paramFocusEvent.isTemporary()))
        FilePane.access$000(this.this$0);
    }
  };
  private static FocusListener repaintListener = new FocusListener()
  {
    public void focusGained(FocusEvent paramFocusEvent)
    {
      repaintSelection(paramFocusEvent.getSource());
    }

    public void focusLost(FocusEvent paramFocusEvent)
    {
      repaintSelection(paramFocusEvent.getSource());
    }

    private void repaintSelection(Object paramObject)
    {
      if (paramObject instanceof JList)
        repaintListSelection((JList)paramObject);
      else if (paramObject instanceof javax.swing.JTable)
        repaintTableSelection((javax.swing.JTable)paramObject);
    }

    private void repaintListSelection(JList paramJList)
    {
      int[] arrayOfInt1 = paramJList.getSelectedIndices();
      int[] arrayOfInt2 = arrayOfInt1;
      int i = arrayOfInt2.length;
      for (int j = 0; j < i; ++j)
      {
        int k = arrayOfInt2[j];
        Rectangle localRectangle = paramJList.getCellBounds(k, k);
        paramJList.repaint(localRectangle);
      }
    }

    private void repaintTableSelection(javax.swing.JTable paramJTable)
    {
      int i = paramJTable.getSelectionModel().getMinSelectionIndex();
      int j = paramJTable.getSelectionModel().getMaxSelectionIndex();
      if ((i == -1) || (j == -1))
        return;
      int k = paramJTable.convertColumnIndexToView(0);
      Rectangle localRectangle1 = paramJTable.getCellRect(i, k, false);
      Rectangle localRectangle2 = paramJTable.getCellRect(j, k, false);
      Rectangle localRectangle3 = localRectangle1.union(localRectangle2);
      paramJTable.repaint(localRectangle3);
    }
  };
  private boolean smallIconsView = false;
  private Border listViewBorder;
  private Color listViewBackground;
  private boolean listViewWindowsStyle;
  private boolean readOnly;
  private boolean fullRowSelection = false;
  private ListSelectionModel listSelectionModel;
  private JList list;
  private javax.swing.JTable detailsTable;
  private static final int COLUMN_FILENAME = 0;
  private File newFolderFile;
  private FileChooserUIAccessor fileChooserUIAccessor;
  private DetailsTableModel detailsTableModel;
  private DetailsTableRowSorter rowSorter;
  private DetailsTableCellEditor tableCellEditor;
  int lastIndex = -1;
  File editFile = null;
  int editX = 20;
  JTextField editCell = null;
  protected Action newFolderAction;
  private Handler handler;

  public FilePane(FileChooserUIAccessor paramFileChooserUIAccessor)
  {
    super(new BorderLayout());
    this.fileChooserUIAccessor = paramFileChooserUIAccessor;
    installDefaults();
    createActionMap();
  }

  public void uninstallUI()
  {
    if (getModel() != null)
      getModel().removePropertyChangeListener(this);
  }

  protected JFileChooser getFileChooser()
  {
    return this.fileChooserUIAccessor.getFileChooser();
  }

  protected BasicDirectoryModel getModel()
  {
    return this.fileChooserUIAccessor.getModel();
  }

  public int getViewType()
  {
    return this.viewType;
  }

  public void setViewType(int paramInt)
  {
    int i = this.viewType;
    if (paramInt == i)
      return;
    this.viewType = paramInt;
    switch (paramInt)
    {
    case 0:
      if (this.viewPanels[paramInt] == null)
      {
        localJPanel = this.fileChooserUIAccessor.createList();
        if (localJPanel == null)
          localJPanel = createList();
        setViewPanel(paramInt, localJPanel);
      }
      this.list.setLayoutOrientation(1);
      break;
    case 1:
      if (this.viewPanels[paramInt] == null)
      {
        localJPanel = this.fileChooserUIAccessor.createDetailsView();
        if (localJPanel == null)
          localJPanel = createDetailsView();
        setViewPanel(paramInt, localJPanel);
      }
    }
    JPanel localJPanel = this.currentViewPanel;
    this.currentViewPanel = this.viewPanels[paramInt];
    if (this.currentViewPanel != localJPanel)
    {
      if (localJPanel != null)
        remove(localJPanel);
      add(this.currentViewPanel, "Center");
      revalidate();
      repaint();
    }
    updateViewMenu();
    firePropertyChange("viewType", i, paramInt);
  }

  public Action getViewTypeAction(int paramInt)
  {
    return new ViewTypeAction(this, paramInt);
  }

  private static void recursivelySetInheritsPopupMenu(Container paramContainer, boolean paramBoolean)
  {
    if (paramContainer instanceof JComponent)
      ((JComponent)paramContainer).setInheritsPopupMenu(paramBoolean);
    int i = paramContainer.getComponentCount();
    for (int j = 0; j < i; ++j)
      recursivelySetInheritsPopupMenu((Container)paramContainer.getComponent(j), paramBoolean);
  }

  public void setViewPanel(int paramInt, JPanel paramJPanel)
  {
    this.viewPanels[paramInt] = paramJPanel;
    recursivelySetInheritsPopupMenu(paramJPanel, true);
    switch (paramInt)
    {
    case 0:
      this.list = ((JList)findChildComponent(this.viewPanels[paramInt], JList.class));
      if (this.listSelectionModel == null)
      {
        this.listSelectionModel = this.list.getSelectionModel();
        if (this.detailsTable != null)
          this.detailsTable.setSelectionModel(this.listSelectionModel);
      }
      else
      {
        this.list.setSelectionModel(this.listSelectionModel);
      }
      break;
    case 1:
      this.detailsTable = ((javax.swing.JTable)findChildComponent(this.viewPanels[paramInt], javax.swing.JTable.class));
      this.detailsTable.setRowHeight(Math.max(this.detailsTable.getFont().getSize() + 4, 17));
      if (this.listSelectionModel != null)
        this.detailsTable.setSelectionModel(this.listSelectionModel);
    }
    if (this.viewType == paramInt)
    {
      if (this.currentViewPanel != null)
        remove(this.currentViewPanel);
      this.currentViewPanel = paramJPanel;
      add(this.currentViewPanel, "Center");
      revalidate();
      repaint();
    }
  }

  protected void installDefaults()
  {
    Locale localLocale = getFileChooser().getLocale();
    this.listViewBorder = UIManager.getBorder("FileChooser.listViewBorder");
    this.listViewBackground = UIManager.getColor("FileChooser.listViewBackground");
    this.listViewWindowsStyle = UIManager.getBoolean("FileChooser.listViewWindowsStyle");
    this.readOnly = UIManager.getBoolean("FileChooser.readOnly");
    this.viewMenuLabelText = UIManager.getString("FileChooser.viewMenuLabelText", localLocale);
    this.refreshActionLabelText = UIManager.getString("FileChooser.refreshActionLabelText", localLocale);
    this.newFolderActionLabelText = UIManager.getString("FileChooser.newFolderActionLabelText", localLocale);
    this.viewTypeActionNames = new String[2];
    this.viewTypeActionNames[0] = UIManager.getString("FileChooser.listViewActionLabelText", localLocale);
    this.viewTypeActionNames[1] = UIManager.getString("FileChooser.detailsViewActionLabelText", localLocale);
    this.kiloByteString = UIManager.getString("FileChooser.fileSizeKiloBytes", localLocale);
    this.megaByteString = UIManager.getString("FileChooser.fileSizeMegaBytes", localLocale);
    this.gigaByteString = UIManager.getString("FileChooser.fileSizeGigaBytes", localLocale);
    this.fullRowSelection = UIManager.getBoolean("FileView.fullRowSelection");
  }

  public Action[] getActions()
  {
    if (this.actions == null)
    {
      ArrayList localArrayList = new ArrayList(8);
      localArrayList.add(new AbstractAction(this, "cancelSelection")
      {
        public void actionPerformed()
        {
          String str = (String)getValue("ActionCommandKey");
          if (str == "cancelSelection")
          {
            if (this.this$0.editFile != null)
              FilePane.access$200(this.this$0);
            else
              this.this$0.getFileChooser().cancelSelection();
          }
          else if (str == "editFileName")
          {
            JFileChooser localJFileChooser = this.this$0.getFileChooser();
            int i = FilePane.access$300(this.this$0).getMinSelectionIndex();
            if ((i >= 0) && (this.this$0.editFile == null) && (((!(localJFileChooser.isMultiSelectionEnabled())) || (localJFileChooser.getSelectedFiles().length <= 1))))
              FilePane.access$400(this.this$0, i);
          }
          else if (str == "refresh")
          {
            this.this$0.getFileChooser().rescanCurrentDirectory();
          }
        }

        public boolean isEnabled()
        {
          String str = (String)getValue("ActionCommandKey");
          if (str == "cancelSelection")
            return this.this$0.getFileChooser().isEnabled();
          if (str == "editFileName")
            return ((!(FilePane.access$500(this.this$0))) && (this.this$0.getFileChooser().isEnabled()));
          return true;
        }
      });
      localArrayList.add(new AbstractAction(this, "editFileName")
      {
        public void actionPerformed()
        {
          String str = (String)getValue("ActionCommandKey");
          if (str == "cancelSelection")
          {
            if (this.this$0.editFile != null)
              FilePane.access$200(this.this$0);
            else
              this.this$0.getFileChooser().cancelSelection();
          }
          else if (str == "editFileName")
          {
            JFileChooser localJFileChooser = this.this$0.getFileChooser();
            int i = FilePane.access$300(this.this$0).getMinSelectionIndex();
            if ((i >= 0) && (this.this$0.editFile == null) && (((!(localJFileChooser.isMultiSelectionEnabled())) || (localJFileChooser.getSelectedFiles().length <= 1))))
              FilePane.access$400(this.this$0, i);
          }
          else if (str == "refresh")
          {
            this.this$0.getFileChooser().rescanCurrentDirectory();
          }
        }

        public boolean isEnabled()
        {
          String str = (String)getValue("ActionCommandKey");
          if (str == "cancelSelection")
            return this.this$0.getFileChooser().isEnabled();
          if (str == "editFileName")
            return ((!(FilePane.access$500(this.this$0))) && (this.this$0.getFileChooser().isEnabled()));
          return true;
        }
      });
      localArrayList.add(new AbstractAction(this, this.refreshActionLabelText, "refresh")
      {
        public void actionPerformed()
        {
          String str = (String)getValue("ActionCommandKey");
          if (str == "cancelSelection")
          {
            if (this.this$0.editFile != null)
              FilePane.access$200(this.this$0);
            else
              this.this$0.getFileChooser().cancelSelection();
          }
          else if (str == "editFileName")
          {
            JFileChooser localJFileChooser = this.this$0.getFileChooser();
            int i = FilePane.access$300(this.this$0).getMinSelectionIndex();
            if ((i >= 0) && (this.this$0.editFile == null) && (((!(localJFileChooser.isMultiSelectionEnabled())) || (localJFileChooser.getSelectedFiles().length <= 1))))
              FilePane.access$400(this.this$0, i);
          }
          else if (str == "refresh")
          {
            this.this$0.getFileChooser().rescanCurrentDirectory();
          }
        }

        public boolean isEnabled()
        {
          String str = (String)getValue("ActionCommandKey");
          if (str == "cancelSelection")
            return this.this$0.getFileChooser().isEnabled();
          if (str == "editFileName")
            return ((!(FilePane.access$500(this.this$0))) && (this.this$0.getFileChooser().isEnabled()));
          return true;
        }
      });
      Action localAction = this.fileChooserUIAccessor.getApproveSelectionAction();
      if (localAction != null)
        localArrayList.add(localAction);
      localAction = this.fileChooserUIAccessor.getChangeToParentDirectoryAction();
      if (localAction != null)
        localArrayList.add(localAction);
      localAction = getNewFolderAction();
      if (localAction != null)
        localArrayList.add(localAction);
      localAction = getViewTypeAction(0);
      if (localAction != null)
        localArrayList.add(localAction);
      localAction = getViewTypeAction(1);
      if (localAction != null)
        localArrayList.add(localAction);
      this.actions = ((Action[])localArrayList.toArray(new Action[localArrayList.size()]));
    }
    return this.actions;
  }

  protected void createActionMap()
  {
    addActionsToMap(super.getActionMap(), getActions());
  }

  public static void addActionsToMap(ActionMap paramActionMap, Action[] paramArrayOfAction)
  {
    if ((paramActionMap != null) && (paramArrayOfAction != null))
      for (int i = 0; i < paramArrayOfAction.length; ++i)
      {
        Action localAction = paramArrayOfAction[i];
        String str = (String)localAction.getValue("ActionCommandKey");
        if (str == null)
          str = (String)localAction.getValue("Name");
        paramActionMap.put(str, localAction);
      }
  }

  private void updateListRowCount(JList paramJList)
  {
    if (this.smallIconsView)
      paramJList.setVisibleRowCount(getModel().getSize() / 3);
    else
      paramJList.setVisibleRowCount(-1);
  }

  public JPanel createList()
  {
    JPanel localJPanel = new JPanel(new BorderLayout());
    JFileChooser localJFileChooser = getFileChooser();
    3 local3 = new JList(this, localJFileChooser)
    {
      public int getNextMatch(, int paramInt, Position.Bias paramBias)
      {
        ListModel localListModel = getModel();
        int i = localListModel.getSize();
        if ((paramString == null) || (paramInt < 0) || (paramInt >= i))
          throw new IllegalArgumentException();
        int j = (paramBias == Position.Bias.Backward) ? 1 : 0;
        int k = paramInt;
        while (true)
        {
          if (j != 0)
            if (k < 0)
              break;
          else
            if (k >= i)
              break;
          String str = this.val$fileChooser.getName((File)localListModel.getElementAt(k));
          if (str.regionMatches(true, 0, paramString, 0, paramString.length()))
            return k;
          k += ((j != 0) ? -1 : 1);
        }
        return -1;
      }
    };
    local3.setCellRenderer(new FileRenderer(this));
    local3.setLayoutOrientation(1);
    local3.putClientProperty("List.isFileList", Boolean.TRUE);
    if (this.listViewWindowsStyle)
      local3.addFocusListener(repaintListener);
    updateListRowCount(local3);
    getModel().addListDataListener(new ListDataListener(this, local3)
    {
      public void intervalAdded()
      {
        FilePane.access$600(this.this$0, this.val$list);
      }

      public void intervalRemoved()
      {
        FilePane.access$600(this.this$0, this.val$list);
      }

      public void contentsChanged()
      {
        if (this.this$0.isShowing())
          this.this$0.clearSelection();
        FilePane.access$600(this.this$0, this.val$list);
      }
    });
    getModel().addPropertyChangeListener(this);
    if (localJFileChooser.isMultiSelectionEnabled())
      local3.setSelectionMode(2);
    else
      local3.setSelectionMode(0);
    local3.setModel(new SortableListModel(this));
    local3.addListSelectionListener(createListSelectionListener());
    local3.addMouseListener(getMouseHandler());
    JScrollPane localJScrollPane = new JScrollPane(local3);
    if (this.listViewBackground != null)
      local3.setBackground(this.listViewBackground);
    if (this.listViewBorder != null)
      localJScrollPane.setBorder(this.listViewBorder);
    localJPanel.add(localJScrollPane, "Center");
    return localJPanel;
  }

  private DetailsTableModel getDetailsTableModel()
  {
    if (this.detailsTableModel == null)
      this.detailsTableModel = new DetailsTableModel(this, getFileChooser());
    return this.detailsTableModel;
  }

  private void updateDetailsColumnModel(javax.swing.JTable paramJTable)
  {
    if (paramJTable != null)
    {
      ShellFolderColumnInfo[] arrayOfShellFolderColumnInfo = this.detailsTableModel.getColumns();
      DefaultTableColumnModel localDefaultTableColumnModel = new DefaultTableColumnModel();
      for (int i = 0; i < arrayOfShellFolderColumnInfo.length; ++i)
      {
        ShellFolderColumnInfo localShellFolderColumnInfo = arrayOfShellFolderColumnInfo[i];
        TableColumn localTableColumn = new TableColumn(i);
        Object localObject1 = localShellFolderColumnInfo.getTitle();
        if ((localObject1 != null) && (((String)localObject1).startsWith("FileChooser.")) && (((String)localObject1).endsWith("HeaderText")))
        {
          localObject2 = UIManager.getString(localObject1, paramJTable.getLocale());
          if (localObject2 != null)
            localObject1 = localObject2;
        }
        localTableColumn.setHeaderValue(localObject1);
        Object localObject2 = localShellFolderColumnInfo.getWidth();
        if (localObject2 != null)
          localTableColumn.setPreferredWidth(((Integer)localObject2).intValue());
        localDefaultTableColumnModel.addColumn(localTableColumn);
      }
      if ((!(this.readOnly)) && (localDefaultTableColumnModel.getColumnCount() > 0))
        localDefaultTableColumnModel.getColumn(0).setCellEditor(getDetailsTableCellEditor());
      paramJTable.setColumnModel(localDefaultTableColumnModel);
    }
  }

  private DetailsTableRowSorter getRowSorter()
  {
    if (this.rowSorter == null)
      this.rowSorter = new DetailsTableRowSorter(this);
    return this.rowSorter;
  }

  private DetailsTableCellEditor getDetailsTableCellEditor()
  {
    if (this.tableCellEditor == null)
      this.tableCellEditor = new DetailsTableCellEditor(this, new JTextField());
    return this.tableCellEditor;
  }

  public JPanel createDetailsView()
  {
    JFileChooser localJFileChooser = getFileChooser();
    JPanel localJPanel = new JPanel(new BorderLayout());
    5 local5 = new javax.swing.JTable(this, getDetailsTableModel(), localJFileChooser)
    {
      protected boolean processKeyBinding(, KeyEvent paramKeyEvent, int paramInt, boolean paramBoolean)
      {
        if ((paramKeyEvent.getKeyCode() == 27) && (getCellEditor() == null))
        {
          this.val$chooser.dispatchEvent(paramKeyEvent);
          return true;
        }
        return super.processKeyBinding(paramKeyStroke, paramKeyEvent, paramInt, paramBoolean);
      }

      public void tableChanged()
      {
        super.tableChanged(paramTableModelEvent);
        if (paramTableModelEvent.getFirstRow() == -1)
          FilePane.access$2200(this.this$0, this);
      }
    };
    local5.setRowSorter(getRowSorter());
    local5.setAutoCreateColumnsFromModel(false);
    local5.setComponentOrientation(localJFileChooser.getComponentOrientation());
    local5.setAutoResizeMode(0);
    local5.setShowGrid(false);
    local5.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
    Font localFont = this.list.getFont();
    local5.setFont(localFont);
    local5.setIntercellSpacing(new Dimension(0, 0));
    AlignableTableHeaderRenderer localAlignableTableHeaderRenderer = new AlignableTableHeaderRenderer(this, local5.getTableHeader().getDefaultRenderer());
    local5.getTableHeader().setDefaultRenderer(localAlignableTableHeaderRenderer);
    DetailsTableCellRenderer localDetailsTableCellRenderer = new DetailsTableCellRenderer(this, localJFileChooser);
    local5.setDefaultRenderer(Object.class, localDetailsTableCellRenderer);
    local5.getColumnModel().getSelectionModel().setSelectionMode(0);
    local5.addMouseListener(getMouseHandler());
    local5.putClientProperty("Table.isFileList", Boolean.TRUE);
    if (this.listViewWindowsStyle)
      local5.addFocusListener(repaintListener);
    ActionMap localActionMap = SwingUtilities.getUIActionMap(local5);
    localActionMap.remove("selectNextRowCell");
    localActionMap.remove("selectPreviousRowCell");
    localActionMap.remove("selectNextColumnCell");
    localActionMap.remove("selectPreviousColumnCell");
    local5.setFocusTraversalKeys(0, null);
    local5.setFocusTraversalKeys(1, null);
    JScrollPane localJScrollPane = new JScrollPane(local5);
    localJScrollPane.setComponentOrientation(localJFileChooser.getComponentOrientation());
    LookAndFeel.installColors(localJScrollPane.getViewport(), "Table.background", "Table.foreground");
    localJScrollPane.addComponentListener(new ComponentAdapter(this)
    {
      public void componentResized()
      {
        JScrollPane localJScrollPane = (JScrollPane)paramComponentEvent.getComponent();
        FilePane.access$2300(this.this$0, localJScrollPane.getViewport().getSize().width);
        localJScrollPane.removeComponentListener(this);
      }
    });
    localJScrollPane.addMouseListener(new MouseAdapter(this)
    {
      public void mousePressed()
      {
        JScrollPane localJScrollPane = (JScrollPane)paramMouseEvent.getComponent();
        javax.swing.JTable localJTable = (javax.swing.JTable)localJScrollPane.getViewport().getView();
        if ((!(paramMouseEvent.isShiftDown())) || (localJTable.getSelectionModel().getSelectionMode() == 0))
        {
          this.this$0.clearSelection();
          TableCellEditor localTableCellEditor = localJTable.getCellEditor();
          if (localTableCellEditor != null)
            localTableCellEditor.stopCellEditing();
        }
      }
    });
    local5.setForeground(this.list.getForeground());
    local5.setBackground(this.list.getBackground());
    if (this.listViewBorder != null)
      localJScrollPane.setBorder(this.listViewBorder);
    localJPanel.add(localJScrollPane, "Center");
    this.detailsTableModel.fireTableStructureChanged();
    return localJPanel;
  }

  private void fixNameColumnWidth(int paramInt)
  {
    TableColumn localTableColumn = this.detailsTable.getColumnModel().getColumn(0);
    int i = this.detailsTable.getPreferredSize().width;
    if (i < paramInt)
      localTableColumn.setPreferredWidth(localTableColumn.getPreferredWidth() + paramInt - i);
  }

  public ListSelectionListener createListSelectionListener()
  {
    return this.fileChooserUIAccessor.createListSelectionListener();
  }

  private int getEditIndex()
  {
    return this.lastIndex;
  }

  private void setEditIndex(int paramInt)
  {
    this.lastIndex = paramInt;
  }

  private void resetEditIndex()
  {
    this.lastIndex = -1;
  }

  private void cancelEdit()
  {
    if (this.editFile != null)
    {
      this.editFile = null;
      this.list.remove(this.editCell);
      repaint();
    }
    else if ((this.detailsTable != null) && (this.detailsTable.isEditing()))
    {
      this.detailsTable.getCellEditor().cancelCellEditing();
    }
  }

  private void editFileName(int paramInt)
  {
    File localFile = getFileChooser().getCurrentDirectory();
    if ((this.readOnly) || (!(canWrite(localFile))))
      return;
    ensureIndexIsVisible(paramInt);
    switch (this.viewType)
    {
    case 0:
      this.editFile = ((File)getModel().getElementAt(getRowSorter().convertRowIndexToModel(paramInt)));
      Rectangle localRectangle = this.list.getCellBounds(paramInt, paramInt);
      if (this.editCell == null)
      {
        this.editCell = new JTextField();
        this.editCell.setName("Tree.cellEditor");
        this.editCell.addActionListener(new EditActionListener(this));
        this.editCell.addFocusListener(this.editorFocusListener);
        this.editCell.setNextFocusableComponent(this.list);
      }
      this.list.add(this.editCell);
      this.editCell.setText(getFileChooser().getName(this.editFile));
      ComponentOrientation localComponentOrientation = this.list.getComponentOrientation();
      this.editCell.setComponentOrientation(localComponentOrientation);
      if (localComponentOrientation.isLeftToRight())
        this.editCell.setBounds(this.editX + localRectangle.x, localRectangle.y, localRectangle.width - this.editX, localRectangle.height);
      else
        this.editCell.setBounds(localRectangle.x, localRectangle.y, localRectangle.width - this.editX, localRectangle.height);
      this.editCell.requestFocus();
      this.editCell.selectAll();
      break;
    case 1:
      this.detailsTable.editCellAt(paramInt, 0);
    }
  }

  private void applyEdit()
  {
    if ((this.editFile != null) && (this.editFile.exists()))
    {
      JFileChooser localJFileChooser = getFileChooser();
      String str1 = localJFileChooser.getName(this.editFile);
      String str2 = this.editFile.getName();
      String str3 = this.editCell.getText().trim();
      if (!(str3.equals(str1)))
      {
        String str4 = str3;
        int i = str2.length();
        int j = str1.length();
        if ((i > j) && (str2.charAt(j) == '.'))
          str4 = str3 + str2.substring(j);
        FileSystemView localFileSystemView = localJFileChooser.getFileSystemView();
        File localFile = localFileSystemView.createFileObject(this.editFile.getParentFile(), str4);
        if ((!(localFile.exists())) && (getModel().renameFile(this.editFile, localFile)) && (localFileSystemView.isParent(localJFileChooser.getCurrentDirectory(), localFile)))
          if (localJFileChooser.isMultiSelectionEnabled())
            localJFileChooser.setSelectedFiles(new File[] { localFile });
          else
            localJFileChooser.setSelectedFile(localFile);
      }
    }
    if ((this.detailsTable != null) && (this.detailsTable.isEditing()))
      this.detailsTable.getCellEditor().stopCellEditing();
    cancelEdit();
  }

  public Action getNewFolderAction()
  {
    if ((!(this.readOnly)) && (this.newFolderAction == null))
      this.newFolderAction = new AbstractAction(this, this.newFolderActionLabelText)
      {
        private Action basicNewFolderAction;

        public void actionPerformed()
        {
          if (this.basicNewFolderAction == null)
            this.basicNewFolderAction = FilePane.access$900(this.this$0).getNewFolderAction();
          JFileChooser localJFileChooser = this.this$0.getFileChooser();
          File localFile1 = localJFileChooser.getSelectedFile();
          this.basicNewFolderAction.actionPerformed(paramActionEvent);
          File localFile2 = localJFileChooser.getSelectedFile();
          if ((localFile2 != null) && (!(localFile2.equals(localFile1))) && (localFile2.isDirectory()))
            FilePane.access$1102(this.this$0, localFile2);
        }
      };
    return this.newFolderAction;
  }

  void setFileSelected()
  {
    Object localObject1;
    Object localObject2;
    int i;
    int j;
    if ((getFileChooser().isMultiSelectionEnabled()) && (!(isDirectorySelected())))
    {
      localObject1 = getFileChooser().getSelectedFiles();
      localObject2 = this.list.getSelectedValues();
      this.listSelectionModel.setValueIsAdjusting(true);
      try
      {
        i = this.listSelectionModel.getLeadSelectionIndex();
        j = this.listSelectionModel.getAnchorSelectionIndex();
        Arrays.sort(localObject1);
        Arrays.sort(localObject2);
        int k = 0;
        int l = 0;
        while ((k < localObject1.length) && (l < localObject2.length))
        {
          int i1 = localObject1[k].compareTo((File)localObject2[l]);
          if (i1 < 0)
          {
            doSelectFile(localObject1[(k++)]);
          }
          else if (i1 > 0)
          {
            doDeselectFile(localObject2[(l++)]);
          }
          else
          {
            ++k;
            ++l;
          }
        }
        while (k < localObject1.length)
          doSelectFile(localObject1[(k++)]);
        while (l < localObject2.length)
          doDeselectFile(localObject2[(l++)]);
        if (this.listSelectionModel instanceof DefaultListSelectionModel)
        {
          ((DefaultListSelectionModel)this.listSelectionModel).moveLeadSelectionIndex(i);
          ((DefaultListSelectionModel)this.listSelectionModel).setAnchorSelectionIndex(j);
        }
      }
      finally
      {
        this.listSelectionModel.setValueIsAdjusting(false);
      }
    }
    else
    {
      localObject1 = getFileChooser();
      localObject2 = null;
      if (isDirectorySelected())
        localObject2 = getDirectory();
      else
        localObject2 = ((JFileChooser)localObject1).getSelectedFile();
      if (localObject2 != null)
        if ((i = getModel().indexOf(localObject2)) >= 0)
        {
          j = getRowSorter().convertRowIndexToView(i);
          this.listSelectionModel.setSelectionInterval(j, j);
          ensureIndexIsVisible(j);
        }
      else
        clearSelection();
    }
  }

  private void doSelectFile(File paramFile)
  {
    int i = getModel().indexOf(paramFile);
    if (i >= 0)
    {
      i = getRowSorter().convertRowIndexToView(i);
      this.listSelectionModel.addSelectionInterval(i, i);
    }
  }

  private void doDeselectFile(Object paramObject)
  {
    int i = getRowSorter().convertRowIndexToView(getModel().indexOf(paramObject));
    this.listSelectionModel.removeSelectionInterval(i, i);
  }

  private void doSelectedFileChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    applyEdit();
    File localFile = (File)paramPropertyChangeEvent.getNewValue();
    JFileChooser localJFileChooser = getFileChooser();
    if ((localFile != null) && ((((localJFileChooser.isFileSelectionEnabled()) && (!(localFile.isDirectory()))) || ((localFile.isDirectory()) && (localJFileChooser.isDirectorySelectionEnabled())))))
      setFileSelected();
  }

  private void doSelectedFilesChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    applyEdit();
    File[] arrayOfFile = (File[])(File[])paramPropertyChangeEvent.getNewValue();
    JFileChooser localJFileChooser = getFileChooser();
    if ((arrayOfFile != null) && (arrayOfFile.length > 0) && (((arrayOfFile.length > 1) || (localJFileChooser.isDirectorySelectionEnabled()) || (!(arrayOfFile[0].isDirectory())))))
      setFileSelected();
  }

  private void doDirectoryChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    getDetailsTableModel().updateColumnInfo();
    JFileChooser localJFileChooser = getFileChooser();
    FileSystemView localFileSystemView = localJFileChooser.getFileSystemView();
    applyEdit();
    resetEditIndex();
    ensureIndexIsVisible(0);
    File localFile = localJFileChooser.getCurrentDirectory();
    if (localFile != null)
    {
      if (!(this.readOnly))
        getNewFolderAction().setEnabled(canWrite(localFile));
      this.fileChooserUIAccessor.getChangeToParentDirectoryAction().setEnabled(!(localFileSystemView.isRoot(localFile)));
    }
  }

  private void doFilterChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    applyEdit();
    resetEditIndex();
    clearSelection();
  }

  private void doFileSelectionModeChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    applyEdit();
    resetEditIndex();
    clearSelection();
  }

  private void doMultiSelectionChanged(PropertyChangeEvent paramPropertyChangeEvent)
  {
    if (getFileChooser().isMultiSelectionEnabled())
    {
      this.listSelectionModel.setSelectionMode(2);
    }
    else
    {
      this.listSelectionModel.setSelectionMode(0);
      clearSelection();
      getFileChooser().setSelectedFiles(null);
    }
  }

  public void propertyChange(PropertyChangeEvent paramPropertyChangeEvent)
  {
    if (this.viewType == -1)
      setViewType(0);
    String str = paramPropertyChangeEvent.getPropertyName();
    if (str.equals("SelectedFileChangedProperty"))
    {
      doSelectedFileChanged(paramPropertyChangeEvent);
    }
    else if (str.equals("SelectedFilesChangedProperty"))
    {
      doSelectedFilesChanged(paramPropertyChangeEvent);
    }
    else if (str.equals("directoryChanged"))
    {
      doDirectoryChanged(paramPropertyChangeEvent);
    }
    else if (str.equals("fileFilterChanged"))
    {
      doFilterChanged(paramPropertyChangeEvent);
    }
    else if (str.equals("fileSelectionChanged"))
    {
      doFileSelectionModeChanged(paramPropertyChangeEvent);
    }
    else if (str.equals("MultiSelectionEnabledChangedProperty"))
    {
      doMultiSelectionChanged(paramPropertyChangeEvent);
    }
    else if (str.equals("CancelSelection"))
    {
      applyEdit();
    }
    else if (str.equals("busy"))
    {
      setCursor((((Boolean)paramPropertyChangeEvent.getNewValue()).booleanValue()) ? waitCursor : null);
    }
    else if (str.equals("componentOrientation"))
    {
      ComponentOrientation localComponentOrientation = (ComponentOrientation)paramPropertyChangeEvent.getNewValue();
      JFileChooser localJFileChooser = (JFileChooser)paramPropertyChangeEvent.getSource();
      if (localComponentOrientation != (ComponentOrientation)paramPropertyChangeEvent.getOldValue())
        localJFileChooser.applyComponentOrientation(localComponentOrientation);
      if (this.detailsTable != null)
      {
        this.detailsTable.setComponentOrientation(localComponentOrientation);
        this.detailsTable.getParent().getParent().setComponentOrientation(localComponentOrientation);
      }
    }
  }

  private void ensureIndexIsVisible(int paramInt)
  {
    if (paramInt >= 0)
    {
      if (this.list != null)
        this.list.ensureIndexIsVisible(paramInt);
      if (this.detailsTable != null)
        this.detailsTable.scrollRectToVisible(this.detailsTable.getCellRect(paramInt, 0, true));
    }
  }

  public void ensureFileIsVisible(JFileChooser paramJFileChooser, File paramFile)
  {
    int i = getModel().indexOf(paramFile);
    if (i >= 0)
      ensureIndexIsVisible(getRowSorter().convertRowIndexToView(i));
  }

  public void rescanCurrentDirectory()
  {
    getModel().validateFileCache();
  }

  public void clearSelection()
  {
    if (this.listSelectionModel != null)
    {
      this.listSelectionModel.clearSelection();
      if (this.listSelectionModel instanceof DefaultListSelectionModel)
      {
        ((DefaultListSelectionModel)this.listSelectionModel).moveLeadSelectionIndex(0);
        ((DefaultListSelectionModel)this.listSelectionModel).setAnchorSelectionIndex(0);
      }
    }
  }

  public JMenu getViewMenu()
  {
    if (this.viewMenu == null)
    {
      this.viewMenu = new JMenu(this.viewMenuLabelText);
      ButtonGroup localButtonGroup = new ButtonGroup();
      for (int i = 0; i < 2; ++i)
      {
        JRadioButtonMenuItem localJRadioButtonMenuItem = new JRadioButtonMenuItem(new ViewTypeAction(this, i));
        localButtonGroup.add(localJRadioButtonMenuItem);
        this.viewMenu.add(localJRadioButtonMenuItem);
      }
      updateViewMenu();
    }
    return this.viewMenu;
  }

  private void updateViewMenu()
  {
    if (this.viewMenu != null)
    {
      Component[] arrayOfComponent = this.viewMenu.getMenuComponents();
      for (int i = 0; i < arrayOfComponent.length; ++i)
        if (arrayOfComponent[i] instanceof JRadioButtonMenuItem)
        {
          JRadioButtonMenuItem localJRadioButtonMenuItem = (JRadioButtonMenuItem)arrayOfComponent[i];
          if (ViewTypeAction.access$2400((ViewTypeAction)localJRadioButtonMenuItem.getAction()) == this.viewType)
            localJRadioButtonMenuItem.setSelected(true);
        }
    }
  }

  public JPopupMenu getComponentPopupMenu()
  {
    JPopupMenu localJPopupMenu = getFileChooser().getComponentPopupMenu();
    if (localJPopupMenu != null)
      return localJPopupMenu;
    JMenu localJMenu = getViewMenu();
    if (this.contextMenu == null)
    {
      this.contextMenu = new JPopupMenu();
      if (localJMenu != null)
      {
        this.contextMenu.add(localJMenu);
        if (this.listViewWindowsStyle)
          this.contextMenu.addSeparator();
      }
      ActionMap localActionMap = getActionMap();
      Action localAction1 = localActionMap.get("refresh");
      Action localAction2 = localActionMap.get("New Folder");
      if (localAction1 != null)
      {
        this.contextMenu.add(localAction1);
        if ((this.listViewWindowsStyle) && (localAction2 != null))
          this.contextMenu.addSeparator();
      }
      if (localAction2 != null)
        this.contextMenu.add(localAction2);
    }
    if (localJMenu != null)
      localJMenu.getPopupMenu().setInvoker(localJMenu);
    return this.contextMenu;
  }

  protected Handler getMouseHandler()
  {
    if (this.handler == null)
      this.handler = new Handler(this, null);
    return this.handler;
  }

  protected boolean isDirectorySelected()
  {
    return this.fileChooserUIAccessor.isDirectorySelected();
  }

  protected File getDirectory()
  {
    return this.fileChooserUIAccessor.getDirectory();
  }

  private Component findChildComponent(Container paramContainer, Class paramClass)
  {
    int i = paramContainer.getComponentCount();
    for (int j = 0; j < i; ++j)
    {
      Component localComponent1 = paramContainer.getComponent(j);
      if (paramClass.isInstance(localComponent1))
        return localComponent1;
      if (localComponent1 instanceof Container)
      {
        Component localComponent2 = findChildComponent((Container)localComponent1, paramClass);
        if (localComponent2 != null)
          return localComponent2;
      }
    }
    return null;
  }

  public static boolean canWrite(File paramFile)
  {
    boolean bool = false;
    if (paramFile != null)
      try
      {
        bool = paramFile.canWrite();
      }
      catch (AccessControlException localAccessControlException)
      {
        bool = false;
      }
    return bool;
  }

  private class AlignableTableHeaderRenderer
  implements TableCellRenderer
  {
    TableCellRenderer wrappedRenderer;

    public AlignableTableHeaderRenderer(, TableCellRenderer paramTableCellRenderer)
    {
      this.wrappedRenderer = paramTableCellRenderer;
    }

    public Component getTableCellRendererComponent(, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, int paramInt1, int paramInt2)
    {
      Component localComponent = this.wrappedRenderer.getTableCellRendererComponent(paramJTable, paramObject, paramBoolean1, paramBoolean2, paramInt1, paramInt2);
      int i = paramJTable.convertColumnIndexToModel(paramInt2);
      ShellFolderColumnInfo localShellFolderColumnInfo = FilePane.access$1400(this.this$0).getColumns()[i];
      Integer localInteger = localShellFolderColumnInfo.getAlignment();
      if (localInteger == null)
        localInteger = Integer.valueOf(0);
      if (localComponent instanceof JLabel)
        ((JLabel)localComponent).setHorizontalAlignment(localInteger.intValue());
      return localComponent;
    }
  }

  private class DelayedSelectionUpdater
  implements Runnable
  {
    File editFile;

    DelayedSelectionUpdater()
    {
      this(paramFilePane, null);
    }

    DelayedSelectionUpdater(, File paramFile)
    {
      this.editFile = paramFile;
      if (paramFilePane.isShowing())
        SwingUtilities.invokeLater(this);
    }

    public void run()
    {
      this.this$0.setFileSelected();
      if (this.editFile != null)
      {
        FilePane.access$400(this.this$0, FilePane.access$800(this.this$0).convertRowIndexToView(this.this$0.getModel().indexOf(this.editFile)));
        this.editFile = null;
      }
    }
  }

  private class DetailsTableCellEditor extends DefaultCellEditor
  {
    private final JTextField tf;

    public DetailsTableCellEditor(, JTextField paramJTextField)
    {
      super(paramJTextField);
      this.tf = paramJTextField;
      paramJTextField.setName("Table.editor");
      paramJTextField.addFocusListener(FilePane.access$1600(paramFilePane));
    }

    public Component getTableCellEditorComponent(, Object paramObject, boolean paramBoolean, int paramInt1, int paramInt2)
    {
      Component localComponent = super.getTableCellEditorComponent(paramJTable, paramObject, paramBoolean, paramInt1, paramInt2);
      if (paramObject instanceof File)
      {
        this.tf.setText(this.this$0.getFileChooser().getName((File)paramObject));
        this.tf.selectAll();
      }
      return localComponent;
    }
  }

  class DetailsTableCellRenderer extends DefaultTableCellRenderer
  {
    JFileChooser chooser;
    DateFormat df;

    DetailsTableCellRenderer(, JFileChooser paramJFileChooser)
    {
      this.chooser = paramJFileChooser;
      this.df = DateFormat.getDateTimeInstance(3, 3, paramJFileChooser.getLocale());
    }

    public void setBounds(, int paramInt2, int paramInt3, int paramInt4)
    {
      if ((getHorizontalAlignment() == 10) && (!(FilePane.access$1700(this.this$0))))
        paramInt3 = Math.min(paramInt3, getPreferredSize().width + 4);
      super.setBounds(paramInt1 -= 4, paramInt2, paramInt3, paramInt4);
    }

    public Insets getInsets()
    {
      paramInsets = super.getInsets(paramInsets);
      paramInsets.left += 4;
      paramInsets.right += 4;
      return paramInsets;
    }

    public Component getTableCellRendererComponent(, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, int paramInt1, int paramInt2)
    {
      if ((((paramJTable.convertColumnIndexToModel(paramInt2) != 0) || ((FilePane.access$1800(this.this$0)) && (!(paramJTable.isFocusOwner()))))) && (!(FilePane.access$1700(this.this$0))))
        paramBoolean1 = false;
      super.getTableCellRendererComponent(paramJTable, paramObject, paramBoolean1, paramBoolean2, paramInt1, paramInt2);
      setIcon(null);
      int i = paramJTable.convertColumnIndexToModel(paramInt2);
      ShellFolderColumnInfo localShellFolderColumnInfo = FilePane.access$1400(this.this$0).getColumns()[i];
      Integer localInteger = localShellFolderColumnInfo.getAlignment();
      if (localInteger == null)
        localInteger = Integer.valueOf((paramObject instanceof java.lang.Number) ? 4 : 10);
      setHorizontalAlignment(localInteger.intValue());
      String str = null;
      if (paramObject == null)
      {
        str = "";
      }
      else if (paramObject instanceof File)
      {
        File localFile = (File)paramObject;
        str = this.chooser.getName(localFile);
        Icon localIcon = this.chooser.getIcon(localFile);
        setIcon(localIcon);
      }
      else if (paramObject instanceof Long)
      {
        long l = ((Long)paramObject).longValue() / 1024L;
        if (FilePane.access$1800(this.this$0))
        {
          str = MessageFormat.format(FilePane.access$1900(this.this$0), new Object[] { Long.valueOf(l + 3412043047825833985L) });
        }
        else if (l < 1024L)
        {
          str = MessageFormat.format(FilePane.access$1900(this.this$0), new Object[] { Long.valueOf((l == 3412043047825833984L) ? 3412043013466095617L : l) });
        }
        else
        {
          l /= 1024L;
          if (l < 1024L)
          {
            str = MessageFormat.format(FilePane.access$2000(this.this$0), new Object[] { Long.valueOf(l) });
          }
          else
          {
            l /= 1024L;
            str = MessageFormat.format(FilePane.access$2100(this.this$0), new Object[] { Long.valueOf(l) });
          }
        }
      }
      else if (paramObject instanceof Date)
      {
        str = this.df.format((Date)paramObject);
      }
      else
      {
        str = paramObject.toString();
      }
      setText(str);
      return this;
    }
  }

  class DetailsTableModel extends AbstractTableModel
  implements ListDataListener
  {
    JFileChooser chooser;
    BasicDirectoryModel directoryModel;
    ShellFolderColumnInfo[] columns;
    int[] columnMap;

    DetailsTableModel(, JFileChooser paramJFileChooser)
    {
      this.chooser = paramJFileChooser;
      this.directoryModel = paramFilePane.getModel();
      this.directoryModel.addListDataListener(this);
      updateColumnInfo();
    }

    void updateColumnInfo()
    {
      Object localObject = this.chooser.getCurrentDirectory();
      if ((localObject != null) && (FilePane.access$900(this.this$0).usesShellFolder()))
        try
        {
          localObject = ShellFolder.getShellFolder((File)localObject);
        }
        catch (FileNotFoundException localFileNotFoundException)
        {
        }
      ShellFolderColumnInfo[] arrayOfShellFolderColumnInfo = ShellFolder.getFolderColumns((File)localObject);
      ArrayList localArrayList = new ArrayList();
      this.columnMap = new int[arrayOfShellFolderColumnInfo.length];
      for (int i = 0; i < arrayOfShellFolderColumnInfo.length; ++i)
      {
        ShellFolderColumnInfo localShellFolderColumnInfo = arrayOfShellFolderColumnInfo[i];
        if (localShellFolderColumnInfo.isVisible())
        {
          this.columnMap[localArrayList.size()] = i;
          localArrayList.add(localShellFolderColumnInfo);
        }
      }
      this.columns = new ShellFolderColumnInfo[localArrayList.size()];
      localArrayList.toArray(this.columns);
      this.columnMap = Arrays.copyOf(this.columnMap, this.columns.length);
      List localList = (FilePane.access$1000(this.this$0) == null) ? null : FilePane.access$1000(this.this$0).getSortKeys();
      fireTableStructureChanged();
      restoreSortKeys(localList);
    }

    private void restoreSortKeys()
    {
      if (paramList != null)
      {
        for (int i = 0; i < paramList.size(); ++i)
        {
          RowSorter.SortKey localSortKey = (RowSorter.SortKey)paramList.get(i);
          if (localSortKey.getColumn() >= this.columns.length)
          {
            paramList = null;
            break;
          }
        }
        if (paramList != null)
          FilePane.access$1000(this.this$0).setSortKeys(paramList);
      }
    }

    public int getRowCount()
    {
      return this.directoryModel.getSize();
    }

    public int getColumnCount()
    {
      return this.columns.length;
    }

    public Object getValueAt(, int paramInt2)
    {
      return getFileColumnValue((File)this.directoryModel.getElementAt(paramInt1), paramInt2);
    }

    private Object getFileColumnValue(, int paramInt)
    {
      return ((paramInt == 0) ? paramFile : ShellFolder.getFolderColumnValue(paramFile, this.columnMap[paramInt]));
    }

    public void setValueAt(, int paramInt1, int paramInt2)
    {
      if (paramInt2 == 0)
      {
        JFileChooser localJFileChooser = this.this$0.getFileChooser();
        File localFile1 = (File)getValueAt(paramInt1, paramInt2);
        if (localFile1 != null)
        {
          String str1 = localJFileChooser.getName(localFile1);
          String str2 = localFile1.getName();
          String str3 = ((String)paramObject).trim();
          if (!(str3.equals(str1)))
          {
            String str4 = str3;
            int i = str2.length();
            int j = str1.length();
            if ((i > j) && (str2.charAt(j) == '.'))
              str4 = str3 + str2.substring(j);
            FileSystemView localFileSystemView = localJFileChooser.getFileSystemView();
            File localFile2 = localFileSystemView.createFileObject(localFile1.getParentFile(), str4);
            if ((!(localFile2.exists())) && (this.this$0.getModel().renameFile(localFile1, localFile2)) && (localFileSystemView.isParent(localJFileChooser.getCurrentDirectory(), localFile2)))
              if (localJFileChooser.isMultiSelectionEnabled())
                localJFileChooser.setSelectedFiles(new File[] { localFile2 });
              else
                localJFileChooser.setSelectedFile(localFile2);
          }
        }
      }
    }

    public boolean isCellEditable(, int paramInt2)
    {
      File localFile = this.this$0.getFileChooser().getCurrentDirectory();
      return ((!(FilePane.access$500(this.this$0))) && (paramInt2 == 0) && (FilePane.canWrite(localFile)));
    }

    public void contentsChanged()
    {
      new FilePane.DelayedSelectionUpdater(this.this$0);
      fireTableDataChanged();
    }

    public void intervalAdded()
    {
      int i = paramListDataEvent.getIndex0();
      int j = paramListDataEvent.getIndex1();
      if (i == j)
      {
        File localFile = (File)this.this$0.getModel().getElementAt(i);
        if (localFile.equals(FilePane.access$1100(this.this$0)))
        {
          new FilePane.DelayedSelectionUpdater(this.this$0, localFile);
          FilePane.access$1102(this.this$0, null);
        }
      }
      fireTableRowsInserted(paramListDataEvent.getIndex0(), paramListDataEvent.getIndex1());
    }

    public void intervalRemoved()
    {
      fireTableRowsDeleted(paramListDataEvent.getIndex0(), paramListDataEvent.getIndex1());
    }

    public ShellFolderColumnInfo[] getColumns()
    {
      return this.columns;
    }
  }

  private class DetailsTableRowSorter extends TableRowSorter
  {
    public DetailsTableRowSorter()
    {
      setModelWrapper(new SorterModelWrapper(this, null));
    }

    public void updateComparators()
    {
      for (int i = 0; i < paramArrayOfShellFolderColumnInfo.length; ++i)
      {
        Object localObject = paramArrayOfShellFolderColumnInfo[i].getComparator();
        if (localObject != null)
          localObject = new FilePane.DirectoriesFirstComparatorWrapper(this.this$0, i, (Comparator)localObject);
        setComparator(i, (Comparator)localObject);
      }
    }

    public void sort()
    {
      ShellFolder.getInvoker().invoke(new Callable(this)
      {
        public Void call()
          throws Exception
        {
          FilePane.DetailsTableRowSorter.access$1301(this.this$1);
          return null;
        }
      });
    }

    public void modelStructureChanged()
    {
      super.modelStructureChanged();
      updateComparators(FilePane.access$1400(this.this$0).getColumns());
    }

    private class SorterModelWrapper extends DefaultRowSorter.ModelWrapper
    {
      public Object getModel()
      {
        return FilePane.access$700(this.this$1.this$0);
      }

      public int getColumnCount()
      {
        return FilePane.access$700(this.this$1.this$0).getColumnCount();
      }

      public int getRowCount()
      {
        return FilePane.access$700(this.this$1.this$0).getRowCount();
      }

      public Object getValueAt(, int paramInt2)
      {
        return this.this$1.this$0.getModel().getElementAt(paramInt1);
      }

      public Object getIdentifier()
      {
        return Integer.valueOf(paramInt);
      }
    }
  }

  private class DirectoriesFirstComparatorWrapper
  implements Comparator<File>
  {
    private Comparator comparator;
    private int column;

    public DirectoriesFirstComparatorWrapper(, int paramInt, Comparator paramComparator)
    {
      this.column = paramInt;
      this.comparator = paramComparator;
    }

    public int compare(, File paramFile2)
    {
      if ((paramFile1 != null) && (paramFile2 != null))
      {
        boolean bool1 = this.this$0.getFileChooser().isTraversable(paramFile1);
        boolean bool2 = this.this$0.getFileChooser().isTraversable(paramFile2);
        if ((bool1) && (!(bool2)))
          return -1;
        if ((!(bool1)) && (bool2))
          return 1;
      }
      if (FilePane.access$1400(this.this$0).getColumns()[this.column].isCompareByColumn())
        return this.comparator.compare(FilePane.DetailsTableModel.access$1500(FilePane.access$700(this.this$0), paramFile1, this.column), FilePane.DetailsTableModel.access$1500(FilePane.access$700(this.this$0), paramFile2, this.column));
      return this.comparator.compare(paramFile1, paramFile2);
    }
  }

  class EditActionListener
  implements ActionListener
  {
    public void actionPerformed()
    {
      FilePane.access$000(this.this$0);
    }
  }

  public static abstract interface FileChooserUIAccessor
  {
    public abstract JFileChooser getFileChooser();

    public abstract BasicDirectoryModel getModel();

    public abstract JPanel createList();

    public abstract JPanel createDetailsView();

    public abstract boolean isDirectorySelected();

    public abstract File getDirectory();

    public abstract Action getApproveSelectionAction();

    public abstract Action getChangeToParentDirectoryAction();

    public abstract Action getNewFolderAction();

    public abstract MouseListener createDoubleClickListener(JList paramJList);

    public abstract ListSelectionListener createListSelectionListener();

    public abstract boolean usesShellFolder();
  }

  protected class FileRenderer extends DefaultListCellRenderer
  {
    public Component getListCellRendererComponent(, Object paramObject, int paramInt, boolean paramBoolean1, boolean paramBoolean2)
    {
      if ((FilePane.access$1800(this.this$0)) && (!(paramJList.isFocusOwner())))
        paramBoolean1 = false;
      super.getListCellRendererComponent(paramJList, paramObject, paramInt, paramBoolean1, paramBoolean2);
      File localFile = (File)paramObject;
      String str = this.this$0.getFileChooser().getName(localFile);
      setText(str);
      setFont(paramJList.getFont());
      Icon localIcon = this.this$0.getFileChooser().getIcon(localFile);
      if (localIcon != null)
      {
        setIcon(localIcon);
        if (paramBoolean1)
          this.this$0.editX = (localIcon.getIconWidth() + 4);
      }
      else if (this.this$0.getFileChooser().getFileSystemView().isTraversable(localFile).booleanValue())
      {
        setText(str + File.separator);
      }
      return this;
    }
  }

  private class Handler
  implements MouseListener
  {
    private MouseListener doubleClickListener;

    public void mouseClicked()
    {
      int i;
      Object localObject;
      JComponent localJComponent = (JComponent)paramMouseEvent.getSource();
      if (localJComponent instanceof JList)
      {
        i = SwingUtilities2.loc2IndexFileList(FilePane.access$2600(this.this$0), paramMouseEvent.getPoint());
      }
      else if (localJComponent instanceof javax.swing.JTable)
      {
        localObject = (javax.swing.JTable)localJComponent;
        Point localPoint = paramMouseEvent.getPoint();
        i = ((javax.swing.JTable)localObject).rowAtPoint(localPoint);
        boolean bool = SwingUtilities2.pointOutsidePrefSize((javax.swing.JTable)localObject, i, ((javax.swing.JTable)localObject).columnAtPoint(localPoint), localPoint);
        if ((bool) && (!(FilePane.access$1700(this.this$0))))
          return;
        if ((i >= 0) && (FilePane.access$2600(this.this$0) != null) && (FilePane.access$300(this.this$0).isSelectedIndex(i)))
        {
          Rectangle localRectangle = FilePane.access$2600(this.this$0).getCellBounds(i, i);
          paramMouseEvent = new MouseEvent(FilePane.access$2600(this.this$0), paramMouseEvent.getID(), paramMouseEvent.getWhen(), paramMouseEvent.getModifiers(), localRectangle.x + 1, localRectangle.y + localRectangle.height / 2, paramMouseEvent.getXOnScreen(), paramMouseEvent.getYOnScreen(), paramMouseEvent.getClickCount(), paramMouseEvent.isPopupTrigger(), paramMouseEvent.getButton());
        }
      }
      else
      {
        return;
      }
      if ((i >= 0) && (SwingUtilities.isLeftMouseButton(paramMouseEvent)))
      {
        localObject = this.this$0.getFileChooser();
        if ((paramMouseEvent.getClickCount() == 1) && (localJComponent instanceof JList))
          if ((((!(((JFileChooser)localObject).isMultiSelectionEnabled())) || (((JFileChooser)localObject).getSelectedFiles().length <= 1))) && (i >= 0) && (FilePane.access$300(this.this$0).isSelectedIndex(i)) && (FilePane.access$2700(this.this$0) == i) && (this.this$0.editFile == null))
            FilePane.access$400(this.this$0, i);
          else if (i >= 0)
            FilePane.access$2800(this.this$0, i);
          else
            FilePane.access$2900(this.this$0);
        else if (paramMouseEvent.getClickCount() == 2)
          FilePane.access$2900(this.this$0);
      }
      if (getDoubleClickListener() != null)
        getDoubleClickListener().mouseClicked(paramMouseEvent);
    }

    public void mouseEntered()
    {
      JComponent localJComponent = (JComponent)paramMouseEvent.getSource();
      if (localJComponent instanceof javax.swing.JTable)
      {
        javax.swing.JTable localJTable = (javax.swing.JTable)paramMouseEvent.getSource();
        TransferHandler localTransferHandler1 = this.this$0.getFileChooser().getTransferHandler();
        TransferHandler localTransferHandler2 = localJTable.getTransferHandler();
        if (localTransferHandler1 != localTransferHandler2)
          localJTable.setTransferHandler(localTransferHandler1);
        boolean bool = this.this$0.getFileChooser().getDragEnabled();
        if (bool != localJTable.getDragEnabled())
          localJTable.setDragEnabled(bool);
      }
      else if ((localJComponent instanceof JList) && (getDoubleClickListener() != null))
      {
        getDoubleClickListener().mouseEntered(paramMouseEvent);
      }
    }

    public void mouseExited()
    {
      if ((paramMouseEvent.getSource() instanceof JList) && (getDoubleClickListener() != null))
        getDoubleClickListener().mouseExited(paramMouseEvent);
    }

    public void mousePressed()
    {
      if ((paramMouseEvent.getSource() instanceof JList) && (getDoubleClickListener() != null))
        getDoubleClickListener().mousePressed(paramMouseEvent);
    }

    public void mouseReleased()
    {
      if ((paramMouseEvent.getSource() instanceof JList) && (getDoubleClickListener() != null))
        getDoubleClickListener().mouseReleased(paramMouseEvent);
    }

    private MouseListener getDoubleClickListener()
    {
      if ((this.doubleClickListener == null) && (FilePane.access$2600(this.this$0) != null))
        this.doubleClickListener = FilePane.access$900(this.this$0).createDoubleClickListener(FilePane.access$2600(this.this$0));
      return this.doubleClickListener;
    }
  }

  private class SortableListModel extends AbstractListModel
  implements TableModelListener, RowSorterListener
  {
    public SortableListModel()
    {
      FilePane.access$700(paramFilePane).addTableModelListener(this);
      FilePane.access$800(paramFilePane).addRowSorterListener(this);
    }

    public int getSize()
    {
      return this.this$0.getModel().getSize();
    }

    public Object getElementAt()
    {
      return this.this$0.getModel().getElementAt(FilePane.access$800(this.this$0).convertRowIndexToModel(paramInt));
    }

    public void tableChanged()
    {
      fireContentsChanged(this, 0, getSize());
    }

    public void sorterChanged()
    {
      fireContentsChanged(this, 0, getSize());
    }
  }

  class ViewTypeAction extends AbstractAction
  {
    private int viewType;

    ViewTypeAction(, int paramInt)
    {
      super(FilePane.access$100(paramFilePane)[paramInt]);
      this.viewType = paramInt;
      switch (paramInt)
      {
      case 0:
        str = "viewTypeList";
        break;
      case 1:
        str = "viewTypeDetails";
        break;
      default:
        str = (String)getValue("Name");
      }
      putValue("ActionCommandKey", str);
    }

    public void actionPerformed()
    {
      this.this$0.setViewType(this.viewType);
    }
  }
}