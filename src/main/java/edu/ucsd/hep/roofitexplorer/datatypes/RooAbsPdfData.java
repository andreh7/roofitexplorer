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

/**
 * Contains information about a RooFit pdf (RooAbsPdf)
 * @author holzner
 */
public class RooAbsPdfData extends RooAbsRealData
{

  public RooAbsPdfData(WorkspaceData workspace, String varName, String className, VerbosePrintOutput detailedData)
  {
    super(workspace, varName, className, detailedData);
  }

  public static RooAbsPdfData make(WorkspaceData workspace, String varName, String className, VerbosePrintOutput detailedData)
  {
   //   if (className.equals("RooFormulaVar"))
   //   return new RooFormulaVarData(workspace, varName, className, detailedData);
   // else
    return new RooAbsPdfData(workspace, varName, className, detailedData);
  }


}
