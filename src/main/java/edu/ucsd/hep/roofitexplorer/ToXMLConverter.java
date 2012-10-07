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
package edu.ucsd.hep.roofitexplorer;

import edu.ucsd.hep.rootrunnerutil.AHUtils;
import edu.ucsd.hep.rootrunnerutil.PipeCommandRunnerListener;
import edu.ucsd.hep.rootrunnerutil.ROOTRunner;
import edu.ucsd.hep.rootrunnerutil.ROOTRunnerImpl;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Helper class to implement a command line utility which
 * reads a workspace in a ROOT file and writes out the serialized
 * objects in XML format
 * @author holzner
 */
public class ToXMLConverter
{
  //----------------------------------------------------------------------

  public static void main(String[] args) throws IOException
  {
    new ToXMLConverter().run(args);
  }
  
  //----------------------------------------------------------------------

  private void run(String[] args) throws IOException
  {
    assert(args.length == 3);
    try
    {
      openRootFile(args[0], args[1], args[2]);
    } catch (MemberVerboseDataParseError ex)
    {
      // Logger.getLogger(ToXMLConverter.class.getName()).log(Level.SEVERE, null, ex);
      ex.printVerboseMessage(System.err);
      throw new Error(ex);
    }
  }

  //----------------------------------------------------------------------

  private void openRootFile(String fname, String workspaceName, String xmlOutputFname) throws IOException, MemberVerboseDataParseError
  {
    if (new File(xmlOutputFname).exists())
    {
      System.err.println("refusing to overwrite existing xml output file " + xmlOutputFname);
      System.exit(1);
    }
    
    
    // start a ROOT process to look at the file
    ROOTRunner root_runner = new ROOTRunnerImpl((List<PipeCommandRunnerListener>)null,null,
      null,
      null);
        
    root_runner.writeLine("new TFile(\"" + fname + "\");");
    root_runner.waitForCompletion();
    
    WorkspaceData ws = null;
    
    // read a ROOT file
    WorkspaceDataReader reader = new WorkspaceDataReader(root_runner, fname, workspaceName);

    ws = reader.getWorkspace();

    // serialize to a temporary file
    AHUtils.writeStringToFile(new File(xmlOutputFname), ws.toXML());
    System.err.println("wrote to " + xmlOutputFname);
    
    // TODO: there seems to be at least one other thread which
    //       has not finished here...
  }

  //----------------------------------------------------------------------

}
