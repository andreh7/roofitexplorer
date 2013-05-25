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
import edu.ucsd.hep.rootrunnerutil.AHUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.ini4j.Ini;
import org.ini4j.Wini;

/**
 * class containing information about a user profile 
 * (path to ROOT etc.)
 * @author holzner
 */
public class UserProfileData
{
  //----------------------------------------------------------------------
  /** commands to be executed before starting the ROOT session.
    
      TODO: should we not merge this into a single command ? 
      e.g. we want to be able to run ROOT via ssh.
   */
  private String preRootShellCommands = "";

  /** command to run root. This can actually also be something
   *  like 'ssh remotehost root' or
   
      ssh user@remotehost -t 'source /etc/zprofile ; cd workdir && root -b'
   
   */
  private String rootCmd = "root";
  
  
  //----------------------------------------------------------------------
  public String getPreRootShellCommands()
  {
    return preRootShellCommands;
  }

  //----------------------------------------------------------------------
  
  public void setPreRootShellCommands(String preRootShellCommands)
  {
    this.preRootShellCommands = preRootShellCommands;
  }

  public String getRootCmd()
  {
    // System.out.println("rootCmd=" + rootCmd);
    return rootCmd;
  }

  //----------------------------------------------------------------------
/** reads profile data from a .ini style file */
  static UserProfileData readFromIniFile(File path) throws IOException
  {
    UserProfileData retval = new UserProfileData();
    
    Ini ini = new Ini();

    ini.load(new FileReader(path));

    // set the fields
    // section for ROOT
    Ini.Section section;
    section = ini.get("ROOT");
    
    if (section != null)
    {
      retval.preRootShellCommands = section.get("preRootShellCommands","");
      retval.rootCmd = section.get("rootCmd","root");
    }
    
    return retval;
  }

  //----------------------------------------------------------------------

  static UserProfileData readFromXmlFile(File path) throws IOException
  {
    // read (deserialize) the profile
    XStream xstream = new XStream(new DomDriver());
    return (UserProfileData) xstream.fromXML(AHUtils.readFile(path));
  }
  
  //----------------------------------------------------------------------
  
  void writeToXMLfile(File path) throws IOException
  {
    XStream xstream = new XStream(new DomDriver());
    String xmlData = xstream.toXML(this);

    AHUtils.writeStringToFile(path, xmlData);
  }
  
  //----------------------------------------------------------------------

  /** write the configuration in .ini format */
  void writeToIniFile(File file) throws IOException
  {
    Wini ini = new Wini(file);

    ini.put("ROOT","preRootShellCommands", preRootShellCommands);
  
    ini.store();
  }
  //----------------------------------------------------------------------

}
