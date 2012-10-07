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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections15.Transformer;

/**
 * A simple layout, like TreeLayout, but for DAGs
 * (i.e. where edges can go 'towards the root' again)
 * 
 * See also the following classes:
 * 
 *   DAGLayout:  http://jung.cvs.sourceforge.net/viewvc/jung/jung2/jung-algorithms/src/main/java/edu/uci/ics/jung/algorithms/layout/DAGLayout.java?view=markup
 * 
 *   TreeLayout: http://jung.cvs.sourceforge.net/viewvc/jung/jung2/jung-algorithms/src/main/java/edu/uci/ics/jung/algorithms/layout/TreeLayout.java?view=markup
 * 
 *   Layout:     http://jung.cvs.sourceforge.net/viewvc/jung/jung2/jung-algorithms/src/main/java/edu/uci/ics/jung/algorithms/layout/Layout.java?view=markup
 * 
 * and the list of layouts: http://jung.sourceforge.net/doc/api/edu/uci/ics/jung/algorithms/layout/package-summary.html
 * 
 * @author holzner
 */
public class SimpleDagLayout<V,E> implements Layout<V,E>
{
  private DirectedGraph<V, E> graph;

  
  private final int VERTEX_HORIZONTAL_DISTANCE = 60;
  
  private final int VERTEX_VERTICAL_DISTANCE = 60;
  
  private Map<V, Point2D> vertexToPosition;
  
  private GraphPositionData<V, E> graphPositionData;
  
  // calculate minimum depth of each vertex (note that in contrast to
  // a tree or forest, the depth is not unique).
  //
  // Position each vertex at its minimum depth.
  //
  // We do NOT support moving the vertices by the user (and re-doing the
  // layout)

  //----------------------------------------------------------------------

  /** contains information related to a graph */
  private static class GraphPositionData<V,E>
  {
    /** maps from the minimal depth to the list of vertices at this depth */
    private final ListMultimap<Integer, V> depthToVertices;

    private final Map<V,Integer> minimalDepths;
    private final int maxNumVerticesPerDepth;

    /** the highest 'minimal' depth found */
    private final int maxDepth;
    
    //----------------------------------------

    public GraphPositionData(DirectedGraph<V, E> graph)
    {
      // calculate the (minimal) depths
      minimalDepths = new MinimalDepthCalculator(graph).getDepths();
    
      // inverse mapping
      depthToVertices = ArrayListMultimap.create();
      
      for (Entry<V, Integer> entry : minimalDepths.entrySet())
      {
        depthToVertices.put(entry.getValue(), entry.getKey());
      }

      assert(depthToVertices.size() == graph.getVertexCount());
      
      int maxNumVerticesPerDepth = -1;

      int tmpMaxDepth = -1;
      
      // find the maximum number of elements over each depth
      for (Integer depth : depthToVertices.keySet())
      {
        maxNumVerticesPerDepth = Math.max(maxNumVerticesPerDepth, numVerticesAtDepth(depth));
        
        // not very elegant but not clear in which order we get the 
        // depths
        tmpMaxDepth = Math.max(tmpMaxDepth, depth);
     
      } // loop over all depths
    
      this.maxNumVerticesPerDepth = maxNumVerticesPerDepth;
      this.maxDepth = tmpMaxDepth;
      
    }
    
    //----------------------------------------
    
    private int numVerticesAtDepth(int depth)
    {
      return depthToVertices.get(depth).size();
    }

    //----------------------------------------
  }
  
  
  //----------------------------------------------------------------------

  public void initialize()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  //----------------------------------------------------------------------

  public void setInitializer(Transformer<V, Point2D> t)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  //----------------------------------------------------------------------

  public void setGraph(Graph<V, E> graph)
  {
    this.graph = (DirectedGraph<V, E>) graph;
    
    
    // calculate the positions:
    // count how many vertices there are at each depth
    //
    // note that the ordering of the vertices at each (minimal) depth
    // could be optimized (for our purposes, we don't care about
    // that for the moment)
    this.graphPositionData = new GraphPositionData(this.graph);
    
    // now calculate the positions for each vertex based on this
    // ordering
    vertexToPosition = new HashMap<V, Point2D>();
    
    // choose the middle using the maximum number of vertices
    // found at any depth and the horizontal distance required
    // between vertices (which is also assumed to be the distance
    // to the left border)
    //
    // this assumes that there is a margin of 0.5 * VERTEX_HORIZONTAL_DISTANCE on
    // the left
    int centerX = Math.round(this.graphPositionData.maxNumVerticesPerDepth / 2.0f * VERTEX_HORIZONTAL_DISTANCE);
    
    // calculate the first and last position for the line with the 
    // highest number of vertices
    int leftX = Math.round(0.5f * VERTEX_HORIZONTAL_DISTANCE);
    int rightX = Math.round((this.graphPositionData.maxNumVerticesPerDepth + 0.5f) * VERTEX_HORIZONTAL_DISTANCE);
    
    assert(this.graphPositionData.depthToVertices.size() == graph.getVertexCount());
        
    for (int depth = 0; depth <= this.graphPositionData.maxDepth; ++depth)
    {
      int centerY = Math.round((depth + 0.5f) * VERTEX_VERTICAL_DISTANCE); 

      
      // spread all points over the range leftX..rightX
      // where the first point is at leftX and the last point is at rightX
      //
      // i.e. if there is only one point, put it in the center at
      // (leftX + rightX) / 2
      //
      // if there are at least two points, put the ith point at
      //  i * (rightX - leftX) / (num_points - 1) + leftX
      int numElementsThisDepth = this.graphPositionData.numVerticesAtDepth(depth);
        
      int index = 0;
        
      for (V vertex : this.graphPositionData.depthToVertices.get(depth))
      {
        float thisXfloat;

        if (numElementsThisDepth == 1)
          thisXfloat = 0.5f * (leftX + rightX);
        else
          thisXfloat = index * (rightX - leftX) / (numElementsThisDepth - 1) + leftX;

        int thisX = Math.round(thisXfloat);
        
        // insert this vertex
        vertexToPosition.put(vertex, new Point2D.Double(thisX, centerY));
        
        // prepare next iteration        
        ++index;

      } // loop over all vertices at this depth

    }
    
    assert(vertexToPosition.size() == graph.getVertexCount());
    
    
  }

  //----------------------------------------------------------------------

  public Graph<V, E> getGraph()
  {
    return this.graph;
  }
  //----------------------------------------------------------------------

  public void reset()
  {
    // what should this do ?
    //
    // seems to be empty in TreeLayout
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  //----------------------------------------------------------------------

  public void setSize(Dimension dim)
  {
    throw new UnsupportedOperationException("size is determined from other parameters");
  }
  //----------------------------------------------------------------------

  public Dimension getSize()
  {
    // TODO: implement this properly
    return new Dimension(600,600);
    
    // throw new UnsupportedOperationException("Not supported yet.");
  }
  //----------------------------------------------------------------------

  public void lock(V vertex, boolean lock)
  {
    // this is empty in TreeLayout
 
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  //----------------------------------------------------------------------

  public boolean isLocked(V vertex)
  {
    return false;
    //throw new UnsupportedOperationException("Not supported yet.");
  }
  //----------------------------------------------------------------------

  public void setLocation(V vertex, Point2D point)
  {
    this.vertexToPosition.put(vertex, point);
    // throw new UnsupportedOperationException("Not supported yet.");
  }
  //----------------------------------------------------------------------

  public Point2D transform(V vertex)
  {
    Point2D retval = this.vertexToPosition.get(vertex); 
    
    if (retval == null)
    {   //throw new Error("internal error: vertex " + vertex + " not found in map");
      System.err.println("internal error: vertex " + vertex + " not found in map");
    return new Point2D.Double(10,10);
    }
    return retval;
    //throw new UnsupportedOperationException("Not supported yet.");
  }
  //----------------------------------------------------------------------
}
