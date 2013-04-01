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

import edu.ucsd.hep.roofitexplorer.WorkspaceData;
import edu.ucsd.hep.rootrunnerutil.ROOTRunner;
import java.io.IOException;

/**
 *
 * @author holzner
 */
public class RooRealVarData extends RooAbsRealData
{
  public Double value;
  public Boolean isConstant;

  //----------------------------------------------------------------------

  public RooRealVarData(WorkspaceData workspace, String varName, String className, VerbosePrintOutput detailedData)
  {
    super(workspace, varName, className, detailedData);
  }

  //----------------------------------------------------------------------

  /** will update the value also in the corresponding object in the ROOT session
   *  and notify the listeners about the modification */
  public void setValue(Double doubleValue) throws IOException
  {
    ROOTRunner rootRunner = this.getRootRunner();
    String workspaceName = this.getWorkspace().getName();
            
    String cmd = 
      "{ RooRealVar *xvar = " + workspaceName + "->var(\"" + getVarName() + "\");\n" +
      "  xvar->setVal(" + doubleValue + ");\n" +
      "}\n";
      
    // we actually ignore the return value
    rootRunner.getCommandOutput(cmd);
    
    this.value = doubleValue;
    
    // notify listeners of this object
    this.getWorkspace().getModificationDispatcher().modified(this);
  
  }
  
  //----------------------------------------------------------------------

  public Double getValue()
  {
    return value;
  }

  //----------------------------------------------------------------------

}
