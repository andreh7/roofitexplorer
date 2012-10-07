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
package edu.ucsd.hep.roofitexplorer.view.graph;

import edu.ucsd.hep.roofitexplorer.WorkspaceData;
import edu.ucsd.hep.roofitexplorer.WorkspaceMemberSelectionListener;
import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;

/**
 * a bridge between a class which generates events when a workspace member
 * is selected and a GraphPanel which can display the corresponding graph
 * for this member
 * 
 * @author holzner
 */
public class GraphPanelAdapter implements WorkspaceMemberSelectionListener
{
  //----------------------------------------------------------------------

  private final GraphMaker graphMaker;
  private final GraphPanel graphPanel;
  
  //----------------------------------------------------------------------

  public GraphPanelAdapter(WorkspaceData workspace, GraphPanel graphPanel)
  {
    this.graphMaker = new GraphMaker(workspace);
    this.graphPanel = graphPanel;
  }

  //----------------------------------------------------------------------

  public void workspaceMemberSelected(GenericWorkspaceMember member)
  {
    // create a graph for the selected member
    graphPanel.setGraph(graphMaker.makeSingleNodeGraph(member), member.getVarName());
  }

  //----------------------------------------------------------------------

}
