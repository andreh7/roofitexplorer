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

import edu.uci.ics.jung.graph.DirectedGraph;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * calculates the 'minimal depth' (minimal with respect to all possible
 * 'root nodes' in a directed, acyclic graph) of a graph.
 * 
 * @author holzner
 */
public class MinimalDepthCalculator<V,E>
{
  
  /** maps from a given vertex to its minimal depth */
  private final Map<V, Integer> depths = new HashMap<V, Integer>();
  private final DirectedGraph<V, E> graph;

  //----------------------------------------------------------------------
  
  public MinimalDepthCalculator(DirectedGraph<V, E> graph)
  {
    this.graph = graph;
    for (V vertex : graph.getVertices())
    {
      calculateMinimalDepth(vertex);
    } // loop over all vertices
    
    // make sure we didn't miss any vertex
    assert(graph.getVertexCount() == depths.size());
  }
  
  //----------------------------------------------------------------------

  private int calculateMinimalDepth(V vertex)
  {
    Integer depth = depths.get(vertex);
    if (depth != null)
      // already calculated
      return depth;
    
    // look at each 'predecessor' (vertex having an edge
    // leading to 'vertex') and take the minimum value + 1
    // of the predecessor's depths.
    //
    // This will potentially call itself recursively 
    // if the depth of a predecessor was not calculated yet.
    //
    // (note that there are several 'roots' which can 
    // lead to this vertex)
    //
    // this also updates the map 'depths'.
    
    Collection<V> preds = this.graph.getPredecessors(vertex);
    
    if (preds.isEmpty())
    {
      // this is a 'root' vertex
      this.depths.put(vertex, 0);
      return 0;
    } 
    
    // loop over predecessors
    int bestVal = Integer.MAX_VALUE;
    
    for (V pred : preds)
    {
      bestVal = Math.min(bestVal, calculateMinimalDepth(pred));
    }
    
    bestVal += 1;
    this.depths.put(vertex, bestVal);
    return bestVal;
    
  }
  
  //----------------------------------------------------------------------

  public Map<V, Integer> getDepths()
  {
    return Collections.unmodifiableMap(depths);
  }
  //----------------------------------------------------------------------
  
}
