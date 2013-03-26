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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A newer version of WorkspaceDataReader, not relying on the output
 * of RooWorkspace::Print(..) (which can contain error messages in the middle)
 * but running a small macro to get the names and classes of the members 
 * in the workspace.
 * 
 * A factory for WorkspaceData objects: sends commands to ROOT
 * 'interactively' to explore the given RooFit workspace
 * @author holzner
 */
public class WorkspaceDataReader2 extends GenericWorkspaceDataReader
{
  private final ROOTRunner rootRunner;
  private final String workspaceName;

  private final List<RooAbsPdfData>   pdfs = new ArrayList<RooAbsPdfData>();
  private final List<RooConstVarData> constants = new ArrayList<RooConstVarData>();
  private final List<RooRealVarData>  variables = new ArrayList<RooRealVarData>();
  private final List<RooAbsDataData>  datasets = new ArrayList<RooAbsDataData>();

  private final List<RooAbsRealData>  functions = new ArrayList<RooAbsRealData>();
  private final WorkspaceData workspace;
  
  //----------------------------------------------------------------------
  
  /** assumes that the file containing the workspace already has been openened
   *  in the ROOTRunner session.
   * @param rootRunner
   * @param workspaceName
   * @throws IOException 
   */
  public WorkspaceDataReader2(ROOTRunner rootRunner, String fname, String workspaceName) throws IOException, MemberVerboseDataParseError
  {
    this.rootRunner = rootRunner;
    this.workspaceName = workspaceName;
    this.workspace = new WorkspaceData(fname, workspaceName, rootRunner);
    readSummary();
    
    if (Parameters.addMissingServerClientLinks)
      this.workspace.addMissingServerClientLinks();
    
  }

  //----------------------------------------------------------------------
  
  private void readSummary() throws IOException, MemberVerboseDataParseError
  {
    // we could actually redirect this to a temporary file
    // and then read it

    String cmd = "{TIterator *it = " + workspaceName + "->componentIterator(); " +
      "TObject *obj; " + 
      "while ((obj = it->Next()) != NULL) " +
        "{ cout " +
        "<< obj->ClassName() << \",\" " + 
        
        // need the following to know which attributes should be read
        "<< obj->IsA()->InheritsFrom(RooAbsPdf::Class()) << \",\" " +
        "<< obj->IsA()->InheritsFrom(RooConstVar::Class()) << \",\" " +
        "<< obj->IsA()->InheritsFrom(RooRealVar::Class()) << \",\" " +
        "<< obj->IsA()->InheritsFrom(RooAbsData::Class()) << \",\" " +
        "<< obj->IsA()->InheritsFrom(RooAbsReal::Class()) << \",\" " +
        "<< obj->IsA()->InheritsFrom(RooAbsCategory::Class()) << \",\" " +
        
        "<< obj->GetName() " + 
        "<< endl; } }";
    
    String summary = rootRunner.getCommandOutput(cmd);

//    System.out.println("GOT SUMMARY:");
//    System.out.println(summary);
    
    //----------------------------------------
    // parse the summary
    //----------------------------------------

    // for the moment, for the sake of simplicity, read the entire
    // output into memory
    List<String> lines = AHUtils.splitToLines(summary);

    // the output format of the above macro is ClassName,InstanceName
    // we assume that the class name does not have any commas in it
    // (but the instance name actually may have...)
    while (!lines.isEmpty())
    {
      String line = lines.remove(0).trim();

      // System.out.println("line='" + line + "'");

      if (line.isEmpty())
        continue;
      
      String parts[] = line.split(",",8);
      
      String className = parts[0];
      boolean isAbsPdf = "1".equals(parts[1]);
      boolean isConstVar = "1".equals(parts[2]);
      boolean isRealVar = "1".equals(parts[3]);
      boolean isAbsData = "1".equals(parts[4]);
      boolean isAbsReal = "1".equals(parts[5]);
      boolean isAbsCategory = "1".equals(parts[6]);
      
      String varname = parts[7];
      
      // note the order of the following comparisons: almost everything is also 
      // a RooAbsReal so we check more specific types before
      if (isAbsPdf)
        this.readSinglePdf(varname, className);
      else if (isConstVar)
        this.readSingleRooConstVar(varname);
      else if (isRealVar)
        this.readSingleVariable(varname);
      else if (isAbsData)
        this.readSingleDataSet(varname, className);
      else if (isAbsReal)
        this.readSingleFunction(varname, className);
      else if (isAbsCategory)
        this.readSingleCategory(varname, className);
      else
        System.err.println("don't know what kind of type '" + varname + "' is, ignoring it");
      
    } // loop over all lines of the workspace members printout

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

  private void readAdditionalRooRealVarData(String varname, RooRealVarData data) throws IOException
  {
      String cmd = "{ RooRealVar *obj = " + workspaceName + "->var(\"" + varname + "\"); " +
        " cout " +
        "<< obj->getVal() << \",\" " + 
        
        "<< obj->isConstant() " +
        "<< endl; }";
    
    // System.out.println("cmd=" + cmd);
    String summary = rootRunner.getCommandOutput(cmd);
    // split into lines as ROOT seems to insist to append a line
    // with the last return value (e.g. "(class ostream)139784828968064" )
    
    // avoid having an empty first line due to a newline at the beginning...
    summary = summary.trim();
    
    List<String> lines = AHUtils.splitToLines(summary);
    
    String line = lines.get(0);
    
    // System.out.println("line=" + line);
    line = line.trim();
    
    String parts[] = line.split(",",2);
    
    // TODO: protect against bad variable names, object not found in the workspace
    // (should not happen), badly formatted return value (wrong number of fields,
    // unparseable double values)
    
    data.value = Double.parseDouble(parts[0]);
    
    data.isConstant = Integer.parseInt(parts[1]) != 0;
    
    
  }
  //----------------------------------------------------------------------
  
  private void readSingleVariable(String varName) throws IOException
  {
    VerbosePrintOutput detailedData = GenericWorkspaceMember.getMemberVerboseData(rootRunner, workspaceName, varName);

    // assume they're all RooRealVar's
    RooRealVarData retval = new RooRealVarData(workspace, varName, "RooRealVar", detailedData);

    this.readAdditionalRooRealVarData(varName, retval);
    
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
  
  private void readSingleCategory(String varName, String className) throws IOException
  {
    System.out.println("reading category " + varName);
        
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
