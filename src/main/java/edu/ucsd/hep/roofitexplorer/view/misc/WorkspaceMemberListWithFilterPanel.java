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

import edu.ucsd.hep.roofitexplorer.WorkspaceMemberSelectionListener;
import edu.ucsd.hep.roofitexplorer.datatypes.WorkspaceMemberList;
import edu.ucsd.hep.roofitexplorer.filters.MemberFilterFactoryList;
import edu.ucsd.hep.roofitexplorer.filters.view.FilterListPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * A combined panel with a filter editing list on the left and
 * a list of workspace members on the right
 * @author holzner
 */
public class WorkspaceMemberListWithFilterPanel extends JPanel
{
  private final JSplitPane splitPane;
  
  /** the left part */
  private final FilterListPanel filterListPanel;
  
  /** the right part */
  private final SimpleWorkspaceMemberListPanel memberListPanel;

  //----------------------------------------------------------------------

  public WorkspaceMemberListWithFilterPanel()
  {
    this.setLayout(new BorderLayout());
    
    this.filterListPanel = new FilterListPanel();
    this.memberListPanel = new SimpleWorkspaceMemberListPanel();
    
    // connect the memberListPanel to the filterListPanel
    // such that when the user changes the list of filters,
    // the memberListPanel can be notified
    this.filterListPanel.setMemberListPanel(memberListPanel);
    
    
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                           this.filterListPanel, 
                           memberListPanel);
    splitPane.setDividerLocation(160);
    splitPane.setOneTouchExpandable(true);

    Dimension minimumSize = new Dimension(80, 100);
    filterListPanel.setMinimumSize(minimumSize);
    memberListPanel.setMinimumSize(minimumSize);
    
    this.add(splitPane, BorderLayout.CENTER);
  }
  
  //----------------------------------------------------------------------
  
  public void setMembers(WorkspaceMemberList data)
  {
    memberListPanel.setData(data);
  }

  //----------------------------------------------------------------------

  public boolean addMemberSelectionListener(WorkspaceMemberSelectionListener listener)
  {
    return memberListPanel.addMemberSelectionListener(listener);
  }

  //----------------------------------------------------------------------

  public JInternalFrame makeInternalFrame(String title)
  {
    JInternalFrame retval = new JInternalFrame(title,
            true, // resizable
            true, // closable
            true, // maximizable
            true  // iconifiable
            );
    retval.getContentPane().add(this);
    retval.pack();

    retval.setSize(600, 500);

    return retval;
  }

  //----------------------------------------------------------------------

  public void setFilterFactories(MemberFilterFactoryList filterFactoryList)
  {
    filterListPanel.setFilterFactories(filterFactoryList);
  }

  
}
