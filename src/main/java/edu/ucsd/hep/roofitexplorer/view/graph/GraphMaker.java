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
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.ucsd.hep.roofitexplorer.WorkspaceData;
import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for generating various types of graphs
 * 
 * @author holzner
 */
public class GraphMaker
{
  private final WorkspaceData workspace;

  public GraphMaker(WorkspaceData workspace)
  {
    this.workspace = workspace;
  }
  
  //----------------------------------------------------------------------

  /** creates and returns a graph with all vertices (and all edges) */
  public DirectedGraph<VertexType, EdgeType> makeFullGraph()
  {
    //
    // TODO: at some point we want to display only the tree underneath
    // a given root node
 
    // create the graph object
    DirectedSparseGraph<VertexType, EdgeType> graph = new DirectedSparseGraph<VertexType, EdgeType>();

    Map<String, VertexType> addressToVertex = new HashMap<String, VertexType>();
    
    // first create the vertices
    for (GenericWorkspaceMember obj : workspace.getMembers())
    {
      VertexType vertex = new VertexType(obj);
      graph.addVertex(vertex);
      addressToVertex.put(obj.getAddress(), vertex);
    } // loop over all objects
    
    // then create the edges (make sure to add an edge each edge only
    // once
    for (GenericWorkspaceMember obj : workspace.getMembers())
    {
      VertexType vertex1 = addressToVertex.get(obj.getAddress());
      
      // System.out.println("GETTING SERVERS OF " + obj.getVarName());
      for (GenericWorkspaceMember child : obj.getServers())
      {
        // original convention: arrow from object to a server 
        //                      (i.e. the arrow goes from the object
        //                       to the component used in the object)
        // graph.addEdge(new EdgeType(), vertex1, addressToVertex.get(child.getAddress()));
      
        // new convention (Bayesian network like): arrow goes from an object
        //            to the server it influences / is an ingredient of
        graph.addEdge(new EdgeType(), addressToVertex.get(child.getAddress()), vertex1);
      
      }
    } // loop over all objects
  
    return graph;
  }
  
  //----------------------------------------------------------------------

  /** produces a 'tree' with 'root' as root. Useful for looking at
   *  a particular node.
   * 
   *  Note that this is NOT a tree (a 'lower' node can be used
   *  several times by a parent node).
   * 
   * @param root
   * @return 
   */
  public DirectedGraph<VertexType, EdgeType> makeSingleNodeGraph(GenericWorkspaceMember root)
  {
    DirectedSparseGraph<VertexType, EdgeType> retval = new DirectedSparseGraph<VertexType, EdgeType>();
    
    Map<String, VertexType> addressToVertex = new HashMap<String, VertexType>();
    
    List<GenericWorkspaceMember> nodesToVisit = new ArrayList<GenericWorkspaceMember>();
    Set<GenericWorkspaceMember> visitedNodes = new HashSet<GenericWorkspaceMember>();
    
    nodesToVisit.add(root);
    
    while (! nodesToVisit.isEmpty())
    {
      GenericWorkspaceMember node = nodesToVisit.remove(0);

      if (visitedNodes.contains(node))
        // node already visited
        continue;

      VertexType vertex = addressToVertex.get(node.getAddress());
      if (vertex == null)
      {
        vertex = new VertexType(node);
        addressToVertex.put(node.getAddress(), vertex);
        retval.addVertex(vertex);
      }
    
      // loop over the children
      for (GenericWorkspaceMember child : node.getServers())
      {
        VertexType childVertex = addressToVertex.get(child.getAddress());
        if (childVertex == null)
        {
        // add the child vertex
          childVertex = new VertexType(child);
          addressToVertex.put(child.getAddress(), childVertex);
          retval.addVertex(childVertex);
        }

        // add an edge to the child
        retval.addEdge(new EdgeType(), vertex, childVertex);
        
        // add this child to the list of nodes to be visited
        nodesToVisit.add(child);
        
      } // loop over all children
      
    } // while nodes to add
    
    return retval;
  }
  
  //----------------------------------------------------------------------
  
}
