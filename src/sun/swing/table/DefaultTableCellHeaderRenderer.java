package sun.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import sun.swing.DefaultLookup;

public class DefaultTableCellHeaderRenderer extends DefaultTableCellRenderer
  implements UIResource
{
  private boolean horizontalTextPositionSet;
  private Icon sortArrow;
  private EmptyIcon emptyIcon = new EmptyIcon(this, null);

  public DefaultTableCellHeaderRenderer()
  {
    setHorizontalAlignment(0);
  }

  public void setHorizontalTextPosition(int paramInt)
  {
    this.horizontalTextPositionSet = true;
    super.setHorizontalTextPosition(paramInt);
  }

  public Component getTableCellRendererComponent(JTable paramJTable, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, int paramInt1, int paramInt2)
  {
    Icon localIcon = null;
    boolean bool = false;
    if (paramJTable != null)
    {
      Object localObject2;
      localObject1 = paramJTable.getTableHeader();
      if (localObject1 != null)
      {
        localObject2 = null;
        Color localColor = null;
        if (paramBoolean2)
        {
          localObject2 = DefaultLookup.getColor(this, this.ui, "TableHeader.focusCellForeground");
          localColor = DefaultLookup.getColor(this, this.ui, "TableHeader.focusCellBackground");
        }
        if (localObject2 == null)
          localObject2 = ((JTableHeader)localObject1).getForeground();
        if (localColor == null)
          localColor = ((JTableHeader)localObject1).getBackground();
        setForeground((Color)localObject2);
        setBackground(localColor);
        setFont(((JTableHeader)localObject1).getFont());
        bool = ((JTableHeader)localObject1).isPaintingForPrint();
      }
      if ((!(bool)) && (paramJTable.getRowSorter() != null))
      {
        if (!(this.horizontalTextPositionSet))
          setHorizontalTextPosition(10);
        localObject2 = getColumnSortOrder(paramJTable, paramInt2);
        if (localObject2 != null)
          switch (1.$SwitchMap$javax$swing$SortOrder[localObject2.ordinal()])
          {
          case 1:
            localIcon = DefaultLookup.getIcon(this, this.ui, "Table.ascendingSortIcon");
            break;
          case 2:
            localIcon = DefaultLookup.getIcon(this, this.ui, "Table.descendingSortIcon");
            break;
          case 3:
            localIcon = DefaultLookup.getIcon(this, this.ui, "Table.naturalSortIcon");
          }
      }
    }
    setText((paramObject == null) ? "" : paramObject.toString());
    setIcon(localIcon);
    this.sortArrow = localIcon;
    Object localObject1 = null;
    if (paramBoolean2)
      localObject1 = DefaultLookup.getBorder(this, this.ui, "TableHeader.focusCellBorder");
    if (localObject1 == null)
      localObject1 = DefaultLookup.getBorder(this, this.ui, "TableHeader.cellBorder");
    setBorder((Border)localObject1);
    return ((Component)(Component)this);
  }

  public static SortOrder getColumnSortOrder(JTable paramJTable, int paramInt)
  {
    SortOrder localSortOrder = null;
    if (paramJTable.getRowSorter() == null)
      return localSortOrder;
    List localList = paramJTable.getRowSorter().getSortKeys();
    if ((localList.size() > 0) && (((RowSorter.SortKey)localList.get(0)).getColumn() == paramJTable.convertColumnIndexToModel(paramInt)))
      localSortOrder = ((RowSorter.SortKey)localList.get(0)).getSortOrder();
    return localSortOrder;
  }

  public void paintComponent(Graphics paramGraphics)
  {
    boolean bool = DefaultLookup.getBoolean(this, this.ui, "TableHeader.rightAlignSortArrow", false);
    if ((bool) && (this.sortArrow != null))
    {
      this.emptyIcon.width = this.sortArrow.getIconWidth();
      this.emptyIcon.height = this.sortArrow.getIconHeight();
      setIcon(this.emptyIcon);
      super.paintComponent(paramGraphics);
      Point localPoint = computeIconPosition(paramGraphics);
      this.sortArrow.paintIcon(this, paramGraphics, localPoint.x, localPoint.y);
    }
    else
    {
      super.paintComponent(paramGraphics);
    }
  }

  private Point computeIconPosition(Graphics paramGraphics)
  {
    FontMetrics localFontMetrics = paramGraphics.getFontMetrics();
    Rectangle localRectangle1 = new Rectangle();
    Rectangle localRectangle2 = new Rectangle();
    Rectangle localRectangle3 = new Rectangle();
    Insets localInsets = getInsets();
    localRectangle1.x = localInsets.left;
    localRectangle1.y = localInsets.top;
    localRectangle1.width = (getWidth() - localInsets.left + localInsets.right);
    localRectangle1.height = (getHeight() - localInsets.top + localInsets.bottom);
    SwingUtilities.layoutCompoundLabel(this, localFontMetrics, getText(), this.sortArrow, getVerticalAlignment(), getHorizontalAlignment(), getVerticalTextPosition(), getHorizontalTextPosition(), localRectangle1, localRectangle3, localRectangle2, getIconTextGap());
    int i = getWidth() - localInsets.right - this.sortArrow.getIconWidth();
    int j = localRectangle3.y;
    return new Point(i, j);
  }

  private class EmptyIcon
  implements Icon
  {
    int width = 0;
    int height = 0;

    public void paintIcon(, Graphics paramGraphics, int paramInt1, int paramInt2)
    {
    }

    public int getIconWidth()
    {
      return this.width;
    }

    public int getIconHeight()
    {
      return this.height;
    }
  }
}