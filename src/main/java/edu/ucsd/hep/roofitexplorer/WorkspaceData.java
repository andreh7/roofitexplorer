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
import com.thoughtworks.xstream.io.binary.BinaryStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;
import edu.ucsd.hep.roofitexplorer.datatypes.WorkspaceMemberList;
import edu.ucsd.hep.rootrunnerutil.AHUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

/**
 * Keeps information about a RooFit workspace
 * @author holzner
 */
public class WorkspaceData implements Serializable
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

  /** deserializes from an XML file */
  public static WorkspaceData readFromXMLfile(String fname) throws IOException
  {
    XStream xstream = new XStream(new DomDriver());
    return (WorkspaceData) xstream.fromXML(AHUtils.readFile(fname));
  }

  //----------------------------------------------------------------------

  public static WorkspaceData readFromGzippedXMLfile(String fname) throws IOException
  {
    XStream xstream = new XStream(new DomDriver());
    return (WorkspaceData) xstream.fromXML(AHUtils.readGzippedFile(fname));
  }

  //----------------------------------------------------------------------

  /** serializes to xml. Can easily run out of memory with medium to
   *  large sizes workspaces.
   */
  String toXML()
  {
    XStream xstream = new XStream(new DomDriver());
    return xstream.toXML(this);
  }
  //----------------------------------------------------------------------
  
  void writeXMLToStream(OutputStream os) throws IOException
  {
    XStream xstream = new XStream(new DomDriver());
    

    // xstream.toXML(this, os);
    
    OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("UTF-8"));
    
    // xstream does not produce an xml header, but xstream does not
    // need one (see http://xstream.codehaus.org/faq.html#XML_write_XML_declaration

    // produce an XML declaration ourselves
    writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    
    xstream.toXML(this, writer);
    writer.flush();
  }

  //----------------------------------------------------------------------

  /** serialize to an XML file */
  void writeXMLToFile(String fname) throws IOException
  {
    writeXMLToStream(new FileOutputStream(fname));
  }
  
  //----------------------------------------------------------------------

  void writeXMLToGzippedFile(String fname) throws IOException
  {
    GZIPOutputStream os = new GZIPOutputStream(new FileOutputStream(fname));
    writeXMLToStream(os);
    
    // looks like we need to make sure that this is closed,
    // otherwise we may get an 'gzip: <FNAME>: unexpected end of file
    os.close();
  }
  
  //----------------------------------------------------------------------

  /** serializes to an xstream binary file.
   
      TODO: should add compression as the files can be compressed 
            very heavily.
   */
  public void writeXMLToBinaryFile(String fname) throws IOException
  {
    XStream xstream = new XStream(new BinaryStreamDriver());
    xstream.toXML(this, new FileOutputStream(fname));
  }
  
  //----------------------------------------------------------------------

  public static WorkspaceData readFromBinaryfile(String fname) throws FileNotFoundException
  {
    XStream xstream = new XStream(new BinaryStreamDriver());
    return (WorkspaceData) xstream.fromXML(new FileInputStream(fname));
  }
  
  //----------------------------------------------------------------------

  public void writeToJavaSerializationFile(String fname) throws IOException
  {
    FileOutputStream os = new FileOutputStream(fname);
    ObjectOutputStream objOut = new ObjectOutputStream(os);
    
    objOut.writeObject(this);

    objOut.close();
    os.close();
  }

  //----------------------------------------------------------------------

  public static WorkspaceData readJavaSerializationfile(String fname) throws IOException, ClassNotFoundException
  {
    FileInputStream is = new FileInputStream(fname);
    ObjectInputStream objIn = new ObjectInputStream(is);
    
    WorkspaceData retval = (WorkspaceData) objIn.readObject();
         
    objIn.close();
    is.close();
    
    return retval;
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
  
  //----------------------------------------------------------------------

}
