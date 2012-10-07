/*
 * Copyright 2011 University of California, San Diego.
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

import edu.ucsd.hep.roofitexplorer.WorkspaceData;
import edu.ucsd.hep.roofitexplorer.WorkspaceMemberSelectionListener;
import edu.ucsd.hep.roofitexplorer.WorkspaceMemberSelectionListenerList;
import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;
import edu.ucsd.hep.roofitexplorer.datatypes.WorkspaceMemberList;
import edu.ucsd.hep.roofitexplorer.filters.ConnectedMembersFilter;
import edu.ucsd.hep.roofitexplorer.filters.LeafMembersFilter;
import edu.ucsd.hep.roofitexplorer.filters.TopLevelMembersFilter;
import edu.ucsd.hep.roofitexplorer.filters.WorkspaceMemberFilter;
import edu.ucsd.hep.roofitexplorer.view.graph.GraphPanel;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

/**
 * A panel allowing to browse a workspace (providing 
 * several groups of items to start from)
 * @author holzner
 */
public class WorkspaceListBrowser extends JXTaskPaneContainer
{
  //----------------------------------------------------------------------
  private final WorkspaceMemberSelectionListenerList listeners = new WorkspaceMemberSelectionListenerList();
  private final WorkspaceMemberList members;

  public WorkspaceListBrowser(WorkspaceData workspace, GraphPanel graphPanel)
  {
    
    members = workspace.getMembers();
    
    this.add(makeTaskPane("top level objects", new TopLevelMembersFilter()));
    this.add(makeTaskPane("leaf objects", new LeafMembersFilter()));

    this.add(makeTaskPane("pdfs", members.getPDFs()));
    this.add(makeTaskPane("functions", members.getFunctions()));

    // objects with neither servers nor clients
    this.add(makeTaskPane("disconnected objects", new ConnectedMembersFilter(false)));

    this.add(makeTaskPane("all objects", members));
    
  }
  
  //----------------------------------------------------------------------
  
  private JXTaskPane makeTaskPane(String title, WorkspaceMemberFilter filter)
  {
    WorkspaceMemberList objects = members.filter(filter);
    return makeTaskPane(title, objects);
  }

  //----------------------------------------------------------------------

  private JXTaskPane makeTaskPane(String title, WorkspaceMemberList objects)
  {
    // add number of objects to the title
    title = title + " (" + objects.size() + " objects)";
    
    // sort the objects by name
    
    // make a copy which we can modify
    List<GenericWorkspaceMember> objList = new ArrayList<GenericWorkspaceMember>(objects.getList());
    Collections.sort(objList, new GenericWorkspaceMember.VarnameComparator());
    
    JXTaskPane retval = new JXTaskPane();
    retval.setTitle(title);
   
    for (final GenericWorkspaceMember member : objList)
    {

      AbstractAction action = new AbstractAction(member.getVarName())
      {

        public void actionPerformed(ActionEvent e)
        {
          // notify the listeners that this member was selected
          listeners.workspaceMemberSelected(member);
        }
      };

      retval.add(action);

    } // loop over all objects

    
   
    return retval;
  }
  
  //----------------------------------------------------------------------
  public JFrame makeFrame(String title)
  {
    JFrame retval = new JFrame(title);
    
    JScrollPane sc = new JScrollPane(this);
    
    retval.getContentPane().add(sc);
    retval.pack();
    
    retval.setSize(500,500);
    
    return retval;
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

}
