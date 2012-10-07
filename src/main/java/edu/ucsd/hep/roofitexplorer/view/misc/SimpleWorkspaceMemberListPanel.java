/*
 * Copyright 2012 University of California, San Diego.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ucsd.hep.roofitexplorer.view.misc;

import edu.ucsd.hep.roofitexplorer.ExcelWorkbookProducer;
import edu.ucsd.hep.roofitexplorer.WorkspaceMemberSelectionListener;
import edu.ucsd.hep.roofitexplorer.WorkspaceMemberSelectionListenerList;
import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;
import edu.ucsd.hep.roofitexplorer.datatypes.WorkspaceMemberList;
import edu.ucsd.hep.roofitexplorer.filters.WorkspaceMemberFilter;
import edu.ucsd.hep.roofitexplorer.view.textsearch.TextSearchRowFilterUtil;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;
import org.apache.poi.ss.usermodel.Workbook;

  //----------------------------------------------------------------------
  
/**
 * A simple panel for showing information about the members of a workspace
 * @author holzner
 */
public class SimpleWorkspaceMemberListPanel extends JPanel
{
  private final JTable table;
  private final SimpleWorkspaceMemberTableModel tableModel;
  private final TableRowSorter<SimpleWorkspaceMemberTableModel> tableSorter;

  /** the objects which will be notified when the user selects
   *  a new workspace member */
  private final WorkspaceMemberSelectionListenerList listeners = new WorkspaceMemberSelectionListenerList();
  
  private JFileChooser saveAsXLSFileChooser;
  
  /** filter for workspace members */
  private WorkspaceMemberFilter memberFilter;
  
  //----------------------------------------
  // for searching through the table
  //----------------------------------------
  
  JTextField searchTextField = new JTextField();
  private final TextSearchRowFilterUtil textSearchRowFilterUtil;
  private JLabel numItemsLabel;
  private final CombinedRowFilter combinedRowFilter;

  //----------------------------------------------------------------------

  public SimpleWorkspaceMemberListPanel()
  {
    this.setLayout(new BorderLayout());
       
    this.add(makeTopPanel(),BorderLayout.NORTH);
    //-----

    JButton saveAsXLSbutton = new JButton("Save as xls...");
    saveAsXLSbutton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        saveAsXLSWithDialog();
      }
    });
      
    //-----
    this.table = new JTable();

    this.tableModel = new SimpleWorkspaceMemberTableModel();
    table.setModel(tableModel);
    
    // make table sortable
    tableSorter = new TableRowSorter<SimpleWorkspaceMemberTableModel>(tableModel);
    table.setRowSorter(tableSorter);

    // add a listener for double clicks
    table.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent ev)
      {
        if (ev.getClickCount() == 2)
        {
          // TODO: should we also require that this was the left (primary) mouse button ?
          
          // find out which object the selection corresponds to
          int viewRow = table.getSelectedRow(); // this is actually just the first row -- should we insist that exactly one row is seleced ?
          
          if (viewRow == -1)
            return;
          
          // convert from view space to model space 
          int modelRow = table.convertRowIndexToModel(viewRow);
          
          GenericWorkspaceMember selectedMember = tableModel.getMember(modelRow);
          listeners.workspaceMemberSelected(selectedMember);
          
          ev.consume();
        }
      }
    });
 
    this.add(new JScrollPane(table), BorderLayout.CENTER);
    //----------
    // install popup mouse handler for table
    //----------
    this.table.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent ev)
      {
        if (ev.isPopupTrigger())
        {
          // TODO: first select the corresponding line we left-clicked
          // to avoid confusion ?!
          showPopupMenu(ev);
        }
      }

    });

    //-----
    
    JPanel buttonPanel = new JPanel(new GridBagLayout());
    this.add(buttonPanel, BorderLayout.SOUTH);

    GridBagConstraints gbc = new GridBagConstraints();
    // leave gridx at its default value RELATIVE (which
    // places it on the right of the previous component)
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    buttonPanel.add(saveAsXLSbutton, gbc);
    
    //-----
    buttonPanel.add(new JLabel("Search:"), gbc);
    
    // the text field should expand if we expand the panel
    gbc.weightx = 1; buttonPanel.add(this.searchTextField, gbc); gbc.weightx = 0;
    
    textSearchRowFilterUtil = new TextSearchRowFilterUtil(this.tableModel, this.searchTextField);
    buttonPanel.add(textSearchRowFilterUtil.getClearTextFieldButton(), gbc);
 
    //-----
    // the combined row filter: search text plus all the member filters added
    // by the user
    
    combinedRowFilter = new CombinedRowFilter(null, textSearchRowFilterUtil.getRowFilter());
    this.tableSorter.setRowFilter(combinedRowFilter);
  }
  
  //----------------------------------------------------------------------

  private JPanel makeTopPanel()
  {
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new GridBagLayout());
    
    GridBagConstraints gbc = new GridBagConstraints();
    // leave gridx at its default value RELATIVE (which
    // places it on the right of the previous component)
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // the menu expands as necessary
    // gbc.weightx = 1; topPanel.add(makeTopMenuBar(), gbc); gbc.weightx = 0;
    
    numItemsLabel = new JLabel();
    topPanel.add(numItemsLabel, gbc);
    
    setNumItemsLabel(null);
    
    return topPanel;
  }
  
  //----------------------------------------------------------------------

  public JFrame makeFrame(String title)
  {
    // String title = workspace.getFile() + ":" + workspace.getName();
    JFrame retval = new JFrame(title);
    
    // JScrollPane sc = new JScrollPane(this);
    
    retval.getContentPane().add(this);
    retval.pack();
    
    retval.setSize(500,500);
    
    return retval;
  }
  
  //----------------------------------------------------------------------

  /** convenience method */
  public static JFrame makeFrame(String title, WorkspaceMemberList members, WorkspaceMemberSelectionListenerList listeners)
  {
    SimpleWorkspaceMemberListPanel panel = new SimpleWorkspaceMemberListPanel();
    panel.setData(members);
    
    // copy all the listeners 
    if (listeners != null)
    {
      for (WorkspaceMemberSelectionListener listener : listeners)
        panel.addMemberSelectionListener(listener);
    }
    
    JFrame retval = panel.makeFrame(title);

    return retval;
  }

  //----------------------------------------------------------------------
  
  /** convenience method */
  public static JInternalFrame makeInternalFrame(String title, WorkspaceMemberList members, 
          WorkspaceMemberSelectionListenerList listeners, JDesktopPane desktop)
  {
    SimpleWorkspaceMemberListPanel panel = new SimpleWorkspaceMemberListPanel();
    panel.setData(members);
    
    // copy all the listeners 
    if (listeners != null)
    {
      for (WorkspaceMemberSelectionListener listener : listeners)
        panel.addMemberSelectionListener(listener);
    }
    
    JInternalFrame retval = panel.makeInternalFrame(title);
    
    if (desktop != null)
      desktop.add(retval);
    
    return retval;
  }

  //----------------------------------------------------------------------

  public JInternalFrame makeInternalFrame(String title)
  {
    JInternalFrame retval = new JInternalFrame(title,
            true, // resizable
            true, // closable
            true, // maximizable
            true // iconifiable
            );
    retval.getContentPane().add(this);
    retval.pack();

    retval.setSize(500, 500);

    return retval;
  }

  //----------------------------------------------------------------------

  public void setData(WorkspaceMemberList data)
  {
    tableModel.setData(data);
    this.setNumItemsLabel(data.size());
  }

  //----------------------------------------------------------------------

  public boolean addMemberSelectionListener(WorkspaceMemberSelectionListener listener)
  {
    return listeners.addListener(listener);
  }

  //----------------------------------------------------------------------

  public boolean removeMemberSelectionListener(WorkspaceMemberSelectionListener listener)
  {
    return listeners.removeListener(listener);
  }

  //----------------------------------------------------------------------

  private void saveAsXLSWithDialog()
  {
    if (saveAsXLSFileChooser == null)
    {
      saveAsXLSFileChooser = new JFileChooser();
      FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "XLS files", "xls");
      saveAsXLSFileChooser.setFileFilter(filter);
    }

    int result  = saveAsXLSFileChooser.showSaveDialog(this);
    if (result != JFileChooser.APPROVE_OPTION)
      return;

    try
    {
      File file = saveAsXLSFileChooser.getSelectedFile();

      // create a new file
      FileOutputStream out = new FileOutputStream(file);
      
      // this method should take into account filtering and sorting
      Workbook wb = ExcelWorkbookProducer.makeHSSFWorkbook(this.table, "members of workspace");

      wb.write(out);
      out.close();

    } catch (Exception ex)
    {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
      JOptionPane.showMessageDialog(this, "error saving spreadsheet file: " + ex);
    }

  }

  //----------------------------------------------------------------------

  private void showPopupMenu(MouseEvent ev)
  {
    this.makePopupMenu(ev).show(this.table, ev.getX(), ev.getY());
  }
 
  //----------------------------------------------------------------------

  private JPopupMenu makePopupMenu(final MouseEvent ev)
  {
    JPopupMenu popupMenu = new JPopupMenu();

    // get the row the mouse was on
    final int table_row = table.rowAtPoint(new Point(ev.getX(), ev.getY()));
    
    // TODO: do we need to handle the case where no row is selected ?
    //    if (table_row < 0)
    //      return ;

    final int model_row = table.convertRowIndexToModel(table_row);
    
    //----------
    final GenericWorkspaceMember member = tableModel.getMember(model_row);

    JMenuItem menuItem;
    //----------
    // copy variable name to clipboard
    //----------
    {
      menuItem = new JMenuItem("copy variable name");
      
        menuItem.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent ae)
          {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection sel = new StringSelection(member.getVarName());
            cb.setContents(sel, null);
          }
        });
      
      popupMenu.add(menuItem);
    }
    //----------
    // show direct clients
    //----------
    {
      menuItem = new JMenuItem("show direct clients");
      
      if (member.hasClients())
      {
        menuItem.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent ae)
          {
            makeInternalFrame("direct clients of " + member.getVarName(), member.getClients(),
              listeners, getDesktop()).setVisible(true);
          }
        });
      }
      else
        menuItem.setEnabled(false);
      
      popupMenu.add(menuItem);
    }
    //----------
    // show direct servers
    //----------
    {
      menuItem = new JMenuItem("show direct servers");
      
      if (member.hasServers())
      {
        menuItem.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent ae)
          {
            makeFrame("direct servers of " + member.getVarName(), member.getServers(),
              listeners).setVisible(true);
          }
        });
      }
      else
        menuItem.setEnabled(false);
      
      popupMenu.add(menuItem);
    }

    return popupMenu;
  }

  //----------------------------------------------------------------------
/*
  private JMenuBar makeTopMenuBar()
  {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new javax.swing.JMenu("New sublist");

    //----------


    menuBar.add(menu);
    
  
    //----------
 
    
    return menuBar;

  }
  */
  //----------------------------------------------------------------------
 
  /** @return the list of members which are selected by the text search field */
  private WorkspaceMemberList getSelectedMembers()
  {
    WorkspaceMemberList retval = new WorkspaceMemberList();

    // TODO: make sure the table model does not change during this loop
    for (int modelRow : textSearchRowFilterUtil.getSelectedRows())
    {
      retval.add(this.tableModel.getMember(modelRow));
    }
    
    return retval;
  }
  
  //----------------------------------------------------------------------

  private void setNumItemsLabel(Integer num)
  {
    String text = "total number of objects in list: ";
    if (num != null)
      text += num;
    else
      text += '-';
    
    this.numItemsLabel.setText(text);
  }

  //----------------------------------------------------------------------

  /** convenience method: tries to find an owning desktop */
  protected JDesktopPane getDesktop()
  {
    Container parent = this.getParent();
    while (parent != null)
    {
      if (parent instanceof JInternalFrame)
        return ((JInternalFrame)parent).getDesktopPane();

      parent = parent.getParent();
    }
    
    // no JInternalFrame found on the way up
    return null;
    
  }
 
  //----------------------------------------------------------------------

  /** @return an adapter from a WorkspaceMemberFilter to a RowFilter
   *  (which can be used with a JTable) */
/*  public static RowFilter<GenericWorkspaceMember, Integer> rowFilterFromMemberFilter(final WorkspaceMemberFilter filter)
  {
    return new RowFilter<GenericWorkspaceMember, Integer>(){

      @Override
      public boolean include(Entry<? extends GenericWorkspaceMember, ? extends Integer> entry)
      {
        GenericWorkspaceMember member = entry.getModel();
        return filter.accept(member);
      }
      
    };
  }
*/
  //----------------------------------------------------------------------

  /** sets a new display filter */
  public void setMemberFilter(WorkspaceMemberFilter memberFilter)
  {
    this.memberFilter = memberFilter;
  
    this.combinedRowFilter.setMemberFilter(memberFilter);
    
    // trigger redrawing of the table
    this.tableModel.fireTableDataChanged();
  }

  //----------------------------------------------------------------------
  
}
