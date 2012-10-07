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
package edu.ucsd.hep.roofitexplorer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;
import edu.ucsd.hep.roofitexplorer.datatypes.WorkspaceMemberList;
import edu.ucsd.hep.rootrunnerutil.AHUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Keeps information about a RooFit workspace
 * @author holzner
 */
public class WorkspaceData
{
  private final String filename;

  /** name of the workspace */
  private final String wsName;

  /** maps from address to the information about the member of the workspace */
  private Map<String, GenericWorkspaceMember> membersByAddress = new HashMap<String, GenericWorkspaceMember>();

  /** maps from name to information about the workspace member */
  private Map<String, GenericWorkspaceMember> membersByName = new HashMap<String, GenericWorkspaceMember>();

  private WorkspaceMemberList membersList = new WorkspaceMemberList();
  //----------------------------------------------------------------------

  WorkspaceData(String filename, String wsName)
  {
    this.wsName = wsName;
    this.filename = filename;
  }
  
  //----------------------------------------------------------------------

  public String getName()
  {
    return this.wsName;
  }
  
  //----------------------------------------------------------------------

  /** @return the filename this workspace was read from. This 
   *  is mostly useful for printing error messages.
   */
  public String getFile()
  {
    return this.filename;
  }
  
  //----------------------------------------------------------------------

  public GenericWorkspaceMember findByAddress(String address)
  {
    return this.membersByAddress.get(address);
  }
  
  //----------------------------------------------------------------------

  public GenericWorkspaceMember findByName(String name)
  {
    return this.membersByName.get(name);
  }
  
  //----------------------------------------------------------------------

  public void register(GenericWorkspaceMember member, String address)
  {
    // TODO: should we check that each address is only registered once ?
    this.membersByAddress.put(address, member);
    this.membersByName.put(member.getVarName(), member);
    this.membersList.add(member);
  }

  //----------------------------------------------------------------------

  /** serialize to an XML file */
  public void writeToXMLfile(String fname) throws FileNotFoundException
  {
    AHUtils.writeStringToFile(new File(fname), toXML());

  }
  
  //----------------------------------------------------------------------

  /** deserializes from an XML file */
  public static WorkspaceData readFromXMLfile(String fname) throws IOException
  {
    XStream xstream = new XStream(new DomDriver());
    return (WorkspaceData) xstream.fromXML(AHUtils.readFile(fname));
  }

  //----------------------------------------------------------------------

  String toXML()
  {
    XStream xstream = new XStream(new DomDriver());
    return xstream.toXML(this);
  }
  //----------------------------------------------------------------------

  /** TODO: should make sure that no modifications happen to the
   *  returned object ? */
  public WorkspaceMemberList getMembers()
  {
    return this.membersList;
  }
  //----------------------------------------------------------------------

  /** this is for testing only (where we in some cases need an empty workspace) */
  public static WorkspaceData makeDummyWorkspace()
  {
    return new WorkspaceData("/dev/null","dummyws");
  }

  //----------------------------------------------------------------------

  /** fixes server <-> client links of which only one side knows about... */
  void addMissingServerClientLinks()
  {
    WorkspaceMemberComparator comp = WorkspaceMemberComparator.make();
    
    // this will compare according to variable name or address, according 
    // to the settings
    for (GenericWorkspaceMember member : this.membersList)
    {
      for (GenericWorkspaceMember client : member.getClients())
      {
        SortedSet<GenericWorkspaceMember> servers = new TreeSet<GenericWorkspaceMember>(comp);
        servers.addAll(client.getServers().getList());
        if (! servers.contains(member))
        {  // missing link
          System.out.println("adding link from client " + client.getVarName() + " to server " + member.getVarName());

          client.addServer(member);
        
        }
      } // loop over the current member's clients
      
      for (GenericWorkspaceMember server : member.getServers())
      {
        SortedSet<GenericWorkspaceMember> clients = new TreeSet<GenericWorkspaceMember>(comp);
        clients.addAll(server.getClients().getList());
        if (! clients.contains(member))
        { // missing link
          System.out.println("adding link from server " + server.getVarName() + " to client " + member.getVarName());
          server.addClient(member);
        }
      } // loop over the current member's clients
      
      
    }
  
  }
}
