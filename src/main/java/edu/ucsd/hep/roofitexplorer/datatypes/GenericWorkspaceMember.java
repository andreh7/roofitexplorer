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
package edu.ucsd.hep.roofitexplorer.datatypes;

import edu.ucsd.hep.roofitexplorer.Parameters;
import edu.ucsd.hep.roofitexplorer.WorkspaceData;
import edu.ucsd.hep.roofitexplorer.datatypes.VerbosePrintOutput.ClientServerRecord;
import edu.ucsd.hep.rootrunnerutil.ROOTRunner;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Parent class of classes which represent members of RooFit workspaces.
 * 
 * This is sort of equivalent to the RooAbsArg class ??
 * @author holzner
 */
public class GenericWorkspaceMember
{

  //----------------------------------------------------------------------
  private final WorkspaceData workspace;

  protected final String varName;
  protected final String className;
  protected final String address;
  
  /** the workspace members which depend on this member ('use this member') */
  private final List<VerbosePrintOutput.ClientServerRecord> clients;
  
  /** the workspace members on which this member depends */
  private final List<VerbosePrintOutput.ClientServerRecord> servers;
  
  private Integer numOverallClients;
  private Integer numOverallServers;
  private final VerbosePrintOutput detailedData;
  
  /** list of servers (this is filled on demand from the text output
   *  of the Print("V") call */
  private WorkspaceMemberList serversList;
  private WorkspaceMemberList clientsList;


  //----------------------------------------------------------------------

  public GenericWorkspaceMember(WorkspaceData workspace, String varName, String className, VerbosePrintOutput detailedData)
  {
    this.detailedData = detailedData;
    
    this.workspace = workspace;
    this.varName = varName;
    this.className = className;
    
    // assumes that all members' classes of the workspace inherit from RooAbsArg
    this.address = detailedData.findValue("RooAbsArg", "Address");
    
    // note that RooAbsData objects seem not to have clients nor
    // servers
    clients = detailedData.parseClientsOrServers("RooAbsArg", false);
    servers = detailedData.parseClientsOrServers("RooAbsArg", true);
    
    // register in this workspace container
    workspace.register(this, address);
  }
  
  //----------------------------------------------------------------------

  public String getClassName()
  {
    return className;
  }
  //----------------------------------------------------------------------

  public String getVarName()
  {
    return varName;
  }
  
  //----------------------------------------------------------------------

  /** runs Print("V") on the given variable and returns the string output */
  public static VerbosePrintOutput getMemberVerboseData(ROOTRunner rootRunner, String workspaceName, String varname) throws IOException
  {
    String text = rootRunner.getCommandOutput(getMemberVerboseDataRootCommand(workspaceName, varname));
    return new VerbosePrintOutput(text);
  }

  //----------------------------------------------------------------------

  /** this and the following function can be used to batch process
   *  multiple objects */
  public static String getMemberVerboseDataRootCommand(String workspaceName, String varname)
  {
    return workspaceName + "->obj(\"" + varname + "\")->Print(\"V\");";
  }
  
  //----------------------------------------------------------------------
  
  public int getNumServers()
  {
    return this.servers.size();
  }
  
  //----------------------------------------------------------------------
  
  /** get the 'children' (those objects which are use in this objects) */
  public WorkspaceMemberList getServers()
  {
    if (this.serversList == null)
    {
     // get the objects on demand
      serversList = new WorkspaceMemberList();

      for (ClientServerRecord server : this.servers)
      {
        GenericWorkspaceMember serverObj;

        if (Parameters.useAddress)
          serverObj = this.workspace.findByAddress(server.address);
        else
          serverObj = this.workspace.findByName(server.varName);

        if (serverObj == null)
          throw new Error("could not find server (child) " + server.address + " (" + server.varName + ") in workspace " + workspace.getName() + " in file " + workspace.getFile());

        serversList.add(serverObj);
      }
    }
    
    // make a copy
    return new WorkspaceMemberList(serversList);
  }
   //----------------------------------------------------------------------
  
  /** get the 'parents' (those objects in which this object is used) */
  public WorkspaceMemberList getClients()
  {
    if (clientsList == null)
    {
      // get the objects on demand
      clientsList = new WorkspaceMemberList();

      for (ClientServerRecord client : this.clients)
      {
        GenericWorkspaceMember clientObj;

        if (Parameters.useAddress)
          clientObj = this.workspace.findByAddress(client.address);
        else
          clientObj = this.workspace.findByName(client.varName);

        if (clientObj == null)
          throw new Error("could not find client (parent) " + client.address + " (" + client.varName + ") in workspace " + workspace.getName() + " in file " + workspace.getFile());

        clientsList.add(clientObj);

      }
    }
    
    // make a copy
    return new WorkspaceMemberList(clientsList);
  }
  
  //----------------------------------------------------------------------

  public int getNumClients()
  {
    return this.clients.size();
  }
  
 
  //----------------------------------------------------------------------

  public String getAddress()
  {
    return address;
  }
  //----------------------------------------------------------------------

  /** this is mainly for internal use... */
  public List<VerbosePrintOutput.ClientServerRecord> getServerRecords()
  {
    return Collections.unmodifiableList(this.servers);
  }
  
  //----------------------------------------------------------------------

  /** @return true iff this has at least one client (parent node) */
  public boolean hasClients()
  {
    return ! this.clients.isEmpty();
  }

  //----------------------------------------------------------------------

  /** @return true iff this has at least one server (child node) */
  public boolean hasServers()
  {
    return ! this.servers.isEmpty();
  }

  //----------------------------------------------------------------------

  /** compares essentially by address (as obtained from the summary list ?!) */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final GenericWorkspaceMember other = (GenericWorkspaceMember) obj;
    if ((this.address == null) ? (other.address != null) : !this.address.equals(other.address))
      return false;
    return true;
  }
  //----------------------------------------------------------------------

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 17 * hash + (this.address != null ? this.address.hashCode() : 0);
    return hash;
  }
  //----------------------------------------------------------------------

  @Override
  public String toString()
  {
    return "GenericWorkspaceMember{" + "varName=" + varName + ", className=" + className + ", address=" + address + '}';
  }
  
  //----------------------------------------------------------------------
  
  /** @return the number of servers and their servers etc. (avoiding
   *  double counting of nodes). Assumes that the workspace does
   *  not change (i.e. uses caching) and assumes that there are no
   *  cycles in the graph.
   */
  public int getOverallNumClients()
  {
    // TODO: could we do this in a more efficient way 
    //       by keeping the number per node ? Not sure
    //       because a node can have multiple parents...
    
    if (numOverallClients == null)
    {

      // TODO: should we also honour the option in Parameters which determines
      //       whether one should search by address or name ?
      Set<GenericWorkspaceMember> todo = new HashSet<GenericWorkspaceMember>();
      todo.addAll(this.getClients().getList());

      Set<GenericWorkspaceMember> visited = new HashSet<GenericWorkspaceMember>();

      while (!todo.isEmpty())
      {
        // get an item (and remove it)
        Iterator<GenericWorkspaceMember> it = todo.iterator();
        GenericWorkspaceMember element = it.next();
        it.remove();
        
        if (visited.contains(element))
          continue;

        visited.add(element);

        todo.addAll(element.getClients().getList());
      }

      numOverallClients = visited.size();
    }
    return numOverallClients;
  }

  //----------------------------------------------------------------------
    
  public int getOverallNumServers()
  {
    if (numOverallServers == null)
    {
      // TODO: should we also honour the option in Parameters which determines
      //       whether one should search by address or name ?
      Set<GenericWorkspaceMember> todo = new HashSet<GenericWorkspaceMember>();
      todo.addAll(this.getServers().getList());

      Set<GenericWorkspaceMember> visited = new HashSet<GenericWorkspaceMember>();

      while (!todo.isEmpty())
      {
        // get an item (and remove it)
        Iterator<GenericWorkspaceMember> it = todo.iterator();
        GenericWorkspaceMember element = it.next();
        it.remove();
        
        if (visited.contains(element))
          continue;

        visited.add(element);

        todo.addAll(element.getServers().getList());
      }

      numOverallServers = visited.size();
    }
    return numOverallServers;
  }

  /** adds a server by hand, to be used only by WorkspaceDataReader */
  public void addServer(GenericWorkspaceMember member)
  {
    // make sure the 'servers' field is initialized
    this.getServers();
    this.serversList.add(member);
  }

  /** adds a server by hand, to be used only by WorkspaceDataReader */
  public void addClient(GenericWorkspaceMember member)
  {
    // make sure the 'clients' field is initialized
    this.getClients();
    this.clientsList.add(member);
  }

  //----------------------------------------------------------------------

  public static class VarnameComparator implements Comparator<GenericWorkspaceMember>
  {

    public int compare(GenericWorkspaceMember o1, GenericWorkspaceMember o2)
    {
      return o1.getVarName().compareTo(o2.getVarName());
    }
  }
  
  //----------------------------------------------------------------------

  public VerbosePrintOutput getDetailedData()
  {
    return detailedData;
  }

  //----------------------------------------------------------------------

  public WorkspaceData getWorkspace()
  {
    return workspace;
  }

  //----------------------------------------------------------------------

}
