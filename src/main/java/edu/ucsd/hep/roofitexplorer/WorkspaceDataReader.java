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

import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;
import edu.ucsd.hep.roofitexplorer.datatypes.RooAbsDataData;
import edu.ucsd.hep.roofitexplorer.datatypes.RooAbsPdfData;
import edu.ucsd.hep.roofitexplorer.datatypes.RooAbsRealData;
import edu.ucsd.hep.roofitexplorer.datatypes.RooConstVarData;
import edu.ucsd.hep.roofitexplorer.datatypes.RooRealVarData;
import edu.ucsd.hep.roofitexplorer.datatypes.VerbosePrintOutput;
import edu.ucsd.hep.roofitexplorer.datatypes.VerbosePrintOutput.ClientServerRecord;
import edu.ucsd.hep.rootrunnerutil.AHUtils;
import edu.ucsd.hep.rootrunnerutil.ROOTRunner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A factory for WorkspaceData objects: sends commands to ROOT
 * 'interactively' to explore the given RooFit workspace
 * @author holzner
 */
public class WorkspaceDataReader
{
  private final ROOTRunner rootRunner;
  private final String workspaceName;

  private final List<RooAbsPdfData>   pdfs = new ArrayList<RooAbsPdfData>();
  private final List<RooAbsRealData>  functions = new ArrayList<RooAbsRealData>();
  private final List<RooRealVarData>  variables = new ArrayList<RooRealVarData>();
  private final List<RooConstVarData> constants = new ArrayList<RooConstVarData>();
  private final List<RooAbsDataData>  datasets = new ArrayList<RooAbsDataData>();
  private final WorkspaceData workspace;
  
  //----------------------------------------------------------------------
  
  /** assumes that the file containing the workspace already has been openened
   *  in the ROOTRunner session.
   * @param rootRunner
   * @param workspaceName
   * @throws IOException 
   */
  public WorkspaceDataReader(ROOTRunner rootRunner, String fname, String workspaceName) throws IOException, MemberVerboseDataParseError
  {
    this.rootRunner = rootRunner;
    this.workspaceName = workspaceName;
    this.workspace = new WorkspaceData(fname, workspaceName);
    readSummary();
    
    if (Parameters.addMissingServerClientLinks)
      this.workspace.addMissingServerClientLinks();
    
  }

  //----------------------------------------------------------------------
  
  private void readSummary() throws IOException, MemberVerboseDataParseError
  {
    // we could actually redirect this to a temporary file
    // and then read it
    String summary = rootRunner.getCommandOutput(workspaceName + ".Print();");

//    System.out.println("GOT SUMMARY:");
//    System.out.println(summary);
    
    //----------------------------------------
    // parse the summary
    //----------------------------------------

    // for the moment, for the sake of simplicity, read the entire
    // output into memory
    List<String> lines = AHUtils.splitToLines(summary);
    //-----
    // ignore lines about errors for the moment
    // e.g.
    // [#0] ERROR:InputArguments -- RooArgSet::checkForDup: ERROR argument with name ...
    Pattern patError = Pattern.compile("\\[#\\d+\\] ERROR:");
    Iterator<String> it = lines.iterator();
    while (it.hasNext())
    {
      String l = it.next();
      if (patError.matcher(l).lookingAt())
      { 
        it.remove();
      
      }
    }
    
    //-----
    
    
    // ignore everything before the first line
    boolean found = false;
    while (! lines.isEmpty())
    {
      String line = lines.remove(0).trim();
    
      // wait for the title line
      if (Pattern.matches("RooWorkspace\\(" + Pattern.quote(this.workspaceName) + "\\)" +
        // seems to be the title here (but in some cases is empty)  
        "\\s+.*\\s+contents$", line))
      {
        found = true;
        break;
      }
    }
    
    if (! found)
      throw new Error("could not find title line in workspace summary description");

    List<String> thisGroup = new ArrayList<String>();
    
    while (!lines.isEmpty())
    {
      String line = lines.remove(0).trim();
      if (line.isEmpty())
      {
        // process the group
        if (!thisGroup.isEmpty())
        {
          // empty groups can happen e.g. immediately after reading the title line      
          processLineGroup(thisGroup);
          thisGroup.clear();
        }
      } else
        // just add the line
        thisGroup.add(line);
      
    } // while the workspace is not empty   

    // process the last group
    if (!thisGroup.isEmpty())
      processLineGroup(thisGroup);
    
    //----------
    // fix RooConstVars which are not printed in the summary
    // i.e. loop over all known objects and see whether they
    // have a RooConstVar as server
    // (Looking at the source code of RooWorkspace::Print(..)
    // it looks like they are explicitly excluded from printing)
    //
    // we make a copy of the list in order not to get a ConcurrentModificationException
    // because we're eventually adding members to the list inside the loop
    for (GenericWorkspaceMember obj : new ArrayList<GenericWorkspaceMember>(this.workspace.getMembers().getList()))
    {
      for (ClientServerRecord server : obj.getServerRecords())
      {
        if (! "RooConstVar".equals(server.getClassName()))
          continue;
        
        // we've found a RooConstVar
        // TODO: FIX CONCURRENT MODIFICATION WHILE LOOPING
        //       OVER THE LIST
        readSingleRooConstVar(server.getVarName());
      }
    }
    
  }

  //----------------------------------------------------------------------
  
  /** parses one group of line (corresponding to one type of objects,
   *  e.g. pdfs, functions etc.) */
  private void processLineGroup(List<String> thisGroup) throws IOException, MemberVerboseDataParseError
  {
    if (thisGroup.size() < 2)
      throw new Error("expected at least two lines in workspace summary");
    
    String firstLine = thisGroup.remove(0);
    String nextLine = thisGroup.remove(0);

    if (! Pattern.matches("-+\\s*$", nextLine))
      throw new Error("expected a line consisting only of dashes here but got '" + nextLine + "'");
    
    if (firstLine.equals("variables"))
      readVariables(thisGroup);
    else if (firstLine.equals("p.d.f.s"))
      readPdfs(thisGroup);
    else if (firstLine.equals("functions"))
      readFunctions(thisGroup);
    else if (firstLine.equals("datasets"))
      readDatasets(thisGroup);
    else
      throw new Error("unexpected line '" + firstLine + "' in workspace summary output");

  }
  
  //----------------------------------------------------------------------
  
  /** reads the list of variables from the summary list.
   *  The first line must be the line immediately after the line
   *  consisting only of dashes)
   * 
   * @param subList 
   */
  private void readVariables(List<String> lines) throws IOException
  {
    // the variable summary is special: it seems to consist of
    // one single line with the names of all variables
    
    // TODO: not clear how the printout looks like when there are NO variables at all 
    if (lines.size() != 1)
      throw new Error("exactly one line expected here");
    
    String line = lines.get(0);
    
    if (! line.startsWith("(") || ! line.endsWith(")"))
      throw new Error("line should start with ( and end with )");
    
    line = line.substring(1,line.length()-1);
    
    for (String varName : line.split(","))
    {
      readSingleVariable(varName);      
      
    } // loop over lines
  }

  //----------------------------------------------------------------------

  /** parses the lines in the pdf section of the workspace summary output */
  private void readPdfs(List<String> lines) throws IOException, MemberVerboseDataParseError
  {
    boolean batchMode = true;

    List<String> varNames = new ArrayList<String>();
    List<String> classNames = new ArrayList<String>();
    
    
    Pattern pattern = Pattern.compile("(.*)::([a-zA-Z0-9_]*)\\[");
    for (String line : lines)
    {
      Matcher matcher = pattern.matcher(line);
      if (! matcher.lookingAt())
        throw new Error("unexpected line '" + line + "' in workspace summary output");

      varNames.add(matcher.group(2));
      classNames.add(matcher.group(1));
      
      // System.out.println("inserted pdf " + pdfs.get(pdfs.size()-1).varName );
      
    } // loop over lines

    //----------
    int numElements = varNames.size();
    
    if (batchMode)
    {
      List<String> cmds = new ArrayList<String>();
    
      for (String varName : varNames)
      {
        // System.out.println("reading pdf " + varName);
        cmds.add(GenericWorkspaceMember.getMemberVerboseDataRootCommand(workspaceName, varName));
      }
      
      // run all commands in batch mode
      List<String> outputs = rootRunner.getMultipleCommandsOutputBatch(cmds);

      // System.out.println("GOT PDF outputs, starting to parse");
      // parse the outputs
      for (int i = 0; i < numElements; ++i)
      {
        String output = outputs.get(i);
        String varName = varNames.get(i);
        String className = classNames.get(i);
        
        VerbosePrintOutput detailedData = new VerbosePrintOutput(output);
        try
        {
          RooAbsPdfData pdf = RooAbsPdfData.make(workspace, varName, className, detailedData);
          this.pdfs.add(pdf);
        } catch (Throwable ex)
        {
          throw new MemberVerboseDataParseError(varName, detailedData, ex);
        }
      }
      
    }
    else
    {
      //----------
      // read one by one
      //----------
      for (int i = 0; i < numElements; ++i)
      {
        readSinglePdf(varNames.get(i), classNames.get(i));
      }
    }
    
    
  }
  //----------------------------------------------------------------------

  private void readFunctions(List<String> lines) throws IOException
  {
    Pattern pattern = Pattern.compile("(.*)\\::([a-zA-Z0-9_]*)\\[");
    for (String line : lines)
    {
      Matcher matcher = pattern.matcher(line);
      if (! matcher.lookingAt())
        throw new Error("unexpected line '" + line + "' in workspace summary output");

      readSingleFunction(matcher.group(2), matcher.group(1));
      
      // System.out.println("inserted pdf " + pdfs.get(pdfs.size()-1).varName );
      
    } // loop over lines
  }

  //----------------------------------------------------------------------

  private void readDatasets(List<String> lines) throws IOException
  {
    // it turns out that RooFit also allows dots in the object
    // names (and probably also other characters), so we just
    // take all the names up to the first parenthesis
    // (assuming there will always be one)

    // original pattern
    // Pattern pattern = Pattern.compile("(.*)\\::([a-zA-Z0-9_]*)\\(");

    Pattern pattern = Pattern.compile("(.*)\\::(.*)\\(");
    for (String line : lines)
    {
      Matcher matcher = pattern.matcher(line);
      if (! matcher.lookingAt())
        throw new Error("unexpected line '" + line + "' in workspace summary output in datasets section");

      readSingleDataSet(matcher.group(2), matcher.group(1));
      
    } // loop over lines
  }

  //----------------------------------------------------------------------

  private void readSingleVariable(String varName) throws IOException
  {
    VerbosePrintOutput detailedData = GenericWorkspaceMember.getMemberVerboseData(rootRunner, workspaceName, varName);

    // assume they're all RooRealVar's
    RooRealVarData retval = new RooRealVarData(workspace, varName, "RooRealVar", detailedData);
    this.variables.add(retval);
    
  }

  //----------------------------------------------------------------------
  
  private void readSinglePdf(String varName, String className) throws IOException, MemberVerboseDataParseError
  {
    System.out.println("reading pdf " + varName);
        
    VerbosePrintOutput detailedData = GenericWorkspaceMember.getMemberVerboseData(rootRunner, workspaceName, varName);
    
    try
    {
      RooAbsPdfData retval = RooAbsPdfData.make(workspace, varName, className, detailedData);
      this.pdfs.add(retval);
    }
    catch (Throwable ex)
    {
      throw new MemberVerboseDataParseError(varName, detailedData, ex);
    }
  }
  
  //----------------------------------------------------------------------

  private void readSingleFunction(String varName, String className) throws IOException
  {
    System.out.println("reading function " + varName);
        
    VerbosePrintOutput detailedData = GenericWorkspaceMember.getMemberVerboseData(rootRunner, workspaceName, varName);
    
    
    RooAbsRealData retval = RooAbsRealData.make(workspace, varName, className, detailedData);
    this.functions.add(retval);
    

  }
  
  //----------------------------------------------------------------------

  private void readSingleRooConstVar(String varName) throws IOException
  {
    VerbosePrintOutput detailedData = GenericWorkspaceMember.getMemberVerboseData(rootRunner, workspaceName, varName);
    
    RooConstVarData retval = new RooConstVarData(workspace, varName, "RooConstVar", detailedData);
    this.constants.add(retval);
  }

  //----------------------------------------------------------------------

  private void readSingleDataSet(String varName, String className) throws IOException
  {
    VerbosePrintOutput detailedData = GenericWorkspaceMember.getMemberVerboseData(rootRunner, workspaceName, varName);
    
    RooAbsDataData retval = new RooAbsDataData(workspace, varName, "RooDataset", detailedData);
    this.datasets.add(retval);
  }
  
  //----------------------------------------------------------------------

  WorkspaceData getWorkspace()
  {
    return this.workspace;
  }

  //----------------------------------------------------------------------



}
