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
package edu.ucsd.hep.roofitexplorer.filters.view;

import edu.ucsd.hep.roofitexplorer.filters.EditableWorkspaceMemberFilter;
import edu.ucsd.hep.roofitexplorer.filters.MemberFilterFactoryList;
import edu.ucsd.hep.roofitexplorer.filters.WorkspaceMemberFilter;
import edu.ucsd.hep.roofitexplorer.filters.WorkspaceMemberFilterList;
import edu.ucsd.hep.roofitexplorer.view.misc.SimpleWorkspaceMemberListPanel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A panel displaying a list of filters and some controls to edit them
 * @author holzner
 */
public class FilterListPanel extends JPanel
{
  private WorkspaceMemberFilterList filterList;
  
  /** where the filters are displayed */
  private JPanel mainPanel;
  private MemberFilterFactoryList filterFactoryList;
  private final JMenu addFilterMenu;
  private SimpleWorkspaceMemberListPanel memberListPanel;
  
  /** keeps objects related to one row */
  private static class RowData
  {
    private JLabel filterNameLabel;
    private JButton editButton;
    private JButton removeButton;
    
  }
  
  /** maps from filter to the graphical components used for this row */
  Map<WorkspaceMemberFilter, RowData> filterToComponents = 
          new HashMap<WorkspaceMemberFilter,RowData>();
  
  //----------------------------------------------------------------------

  public FilterListPanel()
  {
    this.setLayout(new BorderLayout());

    mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());

    this.add(new JScrollPane(mainPanel), BorderLayout.CENTER);
    
    // make a menu
    
    JMenuBar menuBar = new JMenuBar();
    addFilterMenu = new javax.swing.JMenu("Add filter");
    menuBar.add(addFilterMenu);
    
    fillAddFilterMenu();
    
    this.add(menuBar, BorderLayout.NORTH);
    
    // start with an empty filter list
    this.setFilterList(new WorkspaceMemberFilterList());
  }
  
  
  //----------------------------------------------------------------------

  private void addRowComponents(int gridy, RowData rowData)
  {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridy = gridy;
    gbc.gridx = 0;

    mainPanel.add(rowData.filterNameLabel, gbc);
    ++gbc.gridx;

    if (rowData.editButton != null)
      mainPanel.add(rowData.editButton, gbc);
    ++gbc.gridx;

    mainPanel.add(rowData.removeButton, gbc);
    ++gbc.gridx;

  }
  
  //----------------------------------------------------------------------
  
  /** puts one row in the panel with the list of filters */
  private RowData makeFilterControls(final WorkspaceMemberFilter filter)
  {
    // the components we add in this row
    RowData rowData = new RowData();
    

    String name = filter.getClass().getSimpleName();

    rowData.filterNameLabel = new JLabel(name);
    rowData.filterNameLabel.setBorder(BorderFactory.createEtchedBorder());
    
    if (filter instanceof EditableWorkspaceMemberFilter)
    {
      rowData.editButton = new JButton("Edit...");
    }

    //-----
    
    {
      rowData.removeButton = new JButton("Remove");
      
      rowData.removeButton.addActionListener(new ActionListener()
      {

        public void actionPerformed(ActionEvent ae)
        {
          if (JOptionPane.showConfirmDialog(null, "Do you want to remove this filter ?", "Confirmation",
                  JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
          
          // note that we remove by the object, not by row (index),
          // the row might change due to filter insertions/removals
          removeFilter(filter);
        }

      
      });

      
    }
    
    // TODO: implement this (need also toggling of the button label)
    /*
    {
      JButton removeButton = new JButton("Disable");
      mainPanel.add(removeButton, gbc);
      
      removeButton.addActionListener(new ActionListener(){
      
      });

      ++gbc.gridx;
      
    }
    */
    
    //-----
    return rowData;
    
  }
  
  //----------------------------------------------------------------------
  
  private void removeFilter(WorkspaceMemberFilter filter)
  {
    synchronized (this.filterList)
    {
      
      if (! this.filterList.remove(filter))
        // this filter was actually not present
        // (should we throw an exception instead ?)
        return;
      
      // Note that when removing filters we create a 'hole' in the
      // row number space. Should we try to use a JTable instead 
      // (once we want to allow reordering of the rows ?)
      
      // for the moment, we just remove all components
      // and then re-add all but the those for the filter
      // which should be removed
      
      // a JTable has one drawback: one needs to figure out how
      // to automatically size the columns
      
      this.mainPanel.removeAll();
      this.filterToComponents.remove(filter);
      
      // add all remaining components
      int row = 0;
      for (WorkspaceMemberFilter flt : this.filterList)
      {
        this.addRowComponents(row++, this.filterToComponents.get(flt));
      } // loop over remaining filters

      // see http://stackoverflow.com/a/2236813/288875
      mainPanel.validate();
      mainPanel.repaint();
      
      // notify the list that the filter has changed
      notifyFilterListChanged();
    }
  }

  //----------------------------------------------------------------------

  /** assumes that no modifications to list are made outside this class */
  public final void setFilterList(WorkspaceMemberFilterList list)
  {
    this.filterList = list;
    
    // fill the panel
    mainPanel.removeAll();
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridy = 0;

    int row = 0;
    for (WorkspaceMemberFilter filter : list)
    {
      RowData rowData = this.makeFilterControls(filter);
      this.filterToComponents.put(filter, rowData);
      this.addRowComponents(row++, rowData);
    }
    
    // see http://stackoverflow.com/a/2236813/288875
    mainPanel.validate();
    mainPanel.repaint();
   
    // notify the list that the filter has changed
    notifyFilterListChanged();
  }
  
  //----------------------------------------------------------------------

  public void setFilterFactories(MemberFilterFactoryList filterFactoryList)
  {
    this.filterFactoryList = filterFactoryList;
    
    // update the 'add' menu
    this.fillAddFilterMenu();
  }

  //----------------------------------------------------------------------

  private void fillAddFilterMenu()
  {
    this.addFilterMenu.removeAll();
    
    if (this.filterFactoryList == null)
      // no factory set yet
      return;
    
    for (final String factoryName : filterFactoryList.getFactoryNames())
    {
      // TODO: how to keep a hierarchical structure ?
      JMenuItem menuItem = new JMenuItem(factoryName);
      
      menuItem.addActionListener(new ActionListener(){

        public void actionPerformed(ActionEvent ae)
        {
          addFilter(factoryName);
        }
      });
      
      addFilterMenu.add(menuItem);
    }
    
  }
          
  //----------------------------------------------------------------------

  private void addFilter(String factoryName)
  {
    int row = this.filterList.size();
    
    WorkspaceMemberFilter filter = this.filterFactoryList.createInstance(factoryName);
    
    // add the filter to the list of filters
    this.filterList.add(filter);
    // add the visual representation
    RowData rowData = this.makeFilterControls(filter);
    this.filterToComponents.put(filter, rowData);
    this.addRowComponents(row, rowData);
    
    // see http://stackoverflow.com/a/2236813/288875
    mainPanel.validate();
    mainPanel.repaint();

    this.notifyFilterListChanged();
  }
  
  //----------------------------------------------------------------------

  public void setMemberListPanel(SimpleWorkspaceMemberListPanel memberListPanel)
  {
    this.memberListPanel = memberListPanel;
  }

  //----------------------------------------------------------------------

  /** this is called when the list of filters was changed */
  private void notifyFilterListChanged()
  {
    if (this.memberListPanel != null)
      memberListPanel.setMemberFilter(this.filterList);
  }
  
  //----------------------------------------------------------------------

}
