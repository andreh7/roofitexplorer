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
import edu.ucsd.hep.roofitexplorer.view.graph.GraphPanel;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * a panel for browsing a workspace, combining a list on the left
 * with the graph display on the right
 * @author holzner
 */
public class BrowserPanel extends JPanel
{
  //----------------------------------------------------------------------

  private final WorkspaceData workspace;
  private final GraphPanel graphPanel;
  
  //----------------------------------------------------------------------

  public BrowserPanel(WorkspaceData workspace)
  {
    this.workspace = workspace;
    
    this.setLayout(new BorderLayout());

    // add the graph panel as main panel
    graphPanel = new GraphPanel();
    this.add(new JScrollPane(graphPanel), BorderLayout.CENTER);
    
    // add groups of items to browse on the left
    this.add(new JScrollPane(new WorkspaceListBrowser(workspace, graphPanel)), BorderLayout.WEST);
    
  }

  //----------------------------------------------------------------------
  
  public JFrame makeFrame()
  {
    String title = workspace.getFile() + ":" + workspace.getName();
    JFrame retval = new JFrame(title);
    
    JScrollPane sc = new JScrollPane(this);
    
    retval.getContentPane().add(sc);
    retval.pack();
    
    retval.setSize(500,500);
    
    return retval;
  }
  
  //----------------------------------------------------------------------

}
