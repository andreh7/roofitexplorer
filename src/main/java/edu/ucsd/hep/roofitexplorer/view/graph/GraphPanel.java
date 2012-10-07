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
package edu.ucsd.hep.roofitexplorer.view.graph;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.ucsd.hep.roofitexplorer.WorkspaceMemberSelectionListener;
import edu.ucsd.hep.roofitexplorer.WorkspaceMemberSelectionListenerList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author holzner
 */
public class GraphPanel extends JPanel
{
  private DirectedGraph<VertexType, EdgeType> graph = null;
  private VisualizationViewer<VertexType, EdgeType> viewer = null;
  // private final DAGLayout<VertexType, EdgeType> layout;
  
  private List<GraphMouseListener<VertexType> > graphMouseListeners = new ArrayList<GraphMouseListener<VertexType> >();
  
  private final WorkspaceMemberSelectionListenerList listeners = new WorkspaceMemberSelectionListenerList();

  
  
  private List<DirectedGraph<VertexType, EdgeType> > graphHistory = new ArrayList<DirectedGraph<VertexType, EdgeType>>();
  
  /** index of the graph in the history currently shown or -1 if no graph
   *  is set */
  private int currentGraphHistoryIndex = -1;
  private boolean bringToFrontWhenGraphSet = true;
  private JFrame owningFrame;
  private JInternalFrame owningInternalFrame;
  
  //----------------------------------------------------------------------

  public GraphPanel()
  {
    this.setLayout(new BorderLayout());
    
     // listen to clicks on vertices
    addGraphMouseListener(    
    new GraphMouseListener<VertexType>(){

      public void graphClicked(VertexType v, MouseEvent me)
      {
        System.out.println("clicked: " + v);
        
        // check for right-click
        
        //if (me.isPopupTrigger())
        if (me.getButton() != MouseEvent.BUTTON1)
        {
          showVertexPopupMenu(v, me);
          me.consume();
          return;
        }
        
        if (me.getClickCount() >= 2)
        {
          // double click
          listeners.workspaceMemberSelected(v.getObj());
          
          me.consume();
        }
      }

      public void graphPressed(VertexType v, MouseEvent me)
      {
      }

      public void graphReleased(VertexType v, MouseEvent me)
      {
      }

    });
    
  }

  //----------------------------------------------------------------------

  /** to be called from outside when a graph is selected for any reason
      but moving around in the history. */
  public void setGraph(DirectedGraph<VertexType, EdgeType> graph, String frameTitle)
  {
    // truncate the history
    if (currentGraphHistoryIndex == -1)
    {
      // this is the first graph ever
      assert(this.graphHistory.isEmpty());
    }
    else
    {
      // truncate the graph history: keep the currently displayed
      // graph but not newer ones
      if (currentGraphHistoryIndex < this.graphHistory.size() - 1)
      {
        List<DirectedGraph<VertexType, EdgeType>> historyToKeep = graphHistory.subList(0, currentGraphHistoryIndex + 1);
        graphHistory.clear();
        graphHistory.addAll(historyToKeep);
      }      
    }
    
    // add to the history
    
    // actually ++currentGraphHistoryIndex should do it as well ?
    currentGraphHistoryIndex = graphHistory.size();
    
    graphHistory.add(graph);
    
    this.showGraph(graph);
    
    this.setFrameTitle(frameTitle);
    
    if (bringToFrontWhenGraphSet && owningFrame != null)
    {
      // eventually see also http://stackoverflow.com/a/387365/288875
      owningFrame.toFront();
      owningFrame.repaint();
    } 
    else if (bringToFrontWhenGraphSet && owningInternalFrame != null)
    {
      owningInternalFrame.toFront();
      owningInternalFrame.repaint();
    }
    
  }
  
  //----------------------------------------------------------------------

  private void showGraph(DirectedGraph<VertexType, EdgeType> graph)
  {
    this.graph = graph;
    this.removeAll();
    
    // see DAGLayout at http://jung.cvs.sourceforge.net/viewvc/jung/jung2/jung-algorithms/src/main/java/edu/uci/ics/jung/algorithms/layout/DAGLayout.java?view=markup
    // DAGLayout<VertexType, EdgeType> layout = new DAGLayout<VertexType,EdgeType>(graph);
    
    SimpleDagLayout<VertexType, EdgeType> layout = new SimpleDagLayout<VertexType,EdgeType>();
    layout.setGraph(graph);
    
    viewer = new VisualizationViewer<VertexType,EdgeType>(layout, new Dimension(400,400));
    
    viewer.setBackground(Color.white);
    viewer.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
    
    // see http://jung.cvs.sourceforge.net/viewvc/jung/jung2/jung-visualization/src/main/java/edu/uci/ics/jung/visualization/decorators/ToStringLabeller.java?view=markup
    viewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());

    // define how to draw the vertices
    viewer.getRenderContext().setVertexShapeTransformer(new MyVertexShapeTransformer());
    
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(viewer);
    this.add(panel, BorderLayout.CENTER);

    final DefaultModalGraphMouse mouse = new DefaultModalGraphMouse();
    viewer.setGraphMouse(mouse);
    mouse.setMode(ModalGraphMouse.Mode.PICKING);
  
    this.add(mouse.getModeComboBox(), BorderLayout.NORTH);

    
    for (GraphMouseListener<VertexType> listener : this.graphMouseListeners)
    {
      viewer.addGraphMouseListener(listener);
    }
    
    viewer.addKeyListener(new KeyListener(){

      public void keyTyped(KeyEvent e)
      {
        System.out.println("key typed:" + e);
      }

      public void keyPressed(KeyEvent e)
      {
        System.out.println("key pressed:" + e);
        
        if (e.getKeyCode() == 37)
        {  // left
          showPreviousGraph();
          
        }
      }

      public void keyReleased(KeyEvent e)
      {
      }
    });
    
//    viewer.addMouseListener(new MouseAdapter(){
//
//      @Override
//      public void mouseClicked(MouseEvent e)
//      {
//        
//        System.out.println("button=" + e.getButton());
//        if (e.getButton() == 3)
//        {
//          // int oldMode = mouse.getM
//          
//          e.consume();
//          return;
//        }
//        super.mouseClicked(e);
//      }
//    
//    });
  
    
    
    // see e.g. http://stackoverflow.com/questions/6784615/java-swing-redrawing-buttons
    revalidate();
    repaint();
  }

  
  //----------------------------------------------------------------------

  public JFrame makeFrame(String title)
  {
    JFrame retval = new JFrame(title);
    
    JScrollPane sc = new JScrollPane(this);
    
    retval.getContentPane().add(sc);
    retval.pack();
    
    retval.setSize(500,500);

    this.owningFrame = retval;
    
    return retval;
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
    
    JScrollPane sc = new JScrollPane(this);
    
    retval.getContentPane().add(sc);
    retval.pack();
    
    retval.setSize(500,500);

    this.owningInternalFrame = retval;
    
    return retval;
  }
  
  //----------------------------------------------------------------------
  public void addGraphMouseListener(GraphMouseListener<VertexType> listener)
  {
    this.graphMouseListeners.add(listener);
    if (viewer != null)
      viewer.addGraphMouseListener(listener);
  }
  //----------------------------------------------------------------------

  private void showPreviousGraph()
  {
    
//    if (this.graphHistory.isEmpty())
//      return;
//    
    if (this.currentGraphHistoryIndex <= 0)
      return;
    
    --currentGraphHistoryIndex;

    System.out.println("showing previous graph: " + currentGraphHistoryIndex + " of " +graphHistory.size());

    this.showGraph(graphHistory.get(currentGraphHistoryIndex));
  }
  
  
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

  /** this is called when the user clicks the right mouse button
   *  on a vertex 
   */
  private void showVertexPopupMenu(VertexType vertex, MouseEvent me)
  {
    System.out.println("showing popup menu on vertex " + vertex.getObj().getVarName());

    System.out.println();
    System.out.println("original data:");
    System.out.println(vertex.getObj().getDetailedData().getOriginalOutput());
  }

  //----------------------------------------------------------------------

  private void setFrameTitle(String frameTitle)
  {
    String title;
    if (frameTitle == null)
      title = "graph";
    else
      title = "graph: " + frameTitle;
    
    if (this.owningFrame != null)
      owningFrame.setTitle(title);
    
    if (this.owningInternalFrame != null)
      owningInternalFrame.setTitle(title);    
  }

  //----------------------------------------------------------------------
  
  public JInternalFrame getOwningInternalFrame()
  {
    return owningInternalFrame;
  }

  //----------------------------------------------------------------------

}
