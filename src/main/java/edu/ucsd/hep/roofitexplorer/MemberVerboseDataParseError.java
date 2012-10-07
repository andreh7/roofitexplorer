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

import edu.ucsd.hep.roofitexplorer.datatypes.VerbosePrintOutput;
import java.io.PrintStream;

/**
 * An exception which is thrown if there is a problem parsing
 * the verbose printout of one member of the workspace.
 * @author holzner
 */
public class MemberVerboseDataParseError extends Exception
{
  /** the name of the variable in the workspace whose verbose
   *  print output was attempted to be read */
  private final String varName;
  private final VerbosePrintOutput detailedData;
  
  /** the underlying exception which was caught */
  private final Throwable originalCause;

  //----------------------------------------------------------------------

  MemberVerboseDataParseError(String varName, VerbosePrintOutput detailedData, Throwable originalCause)
  {
    this.varName = varName;
    this.detailedData = detailedData;
    this.originalCause = originalCause;
  }

  //----------------------------------------------------------------------

  /** print a short description of the problem */
  void printVerboseMessage(PrintStream os)
  {
      os.println("Error parsing output of verbose print of member '" + varName + "'");
      os.println("got exception: " + originalCause);
      originalCause.printStackTrace(os);
    
      String originalOutput = detailedData.getOriginalOutput();
      
      if (originalOutput == null || originalOutput.isEmpty())
        os.println("outptut was empty");
      else
      {
        os.println("outptut was:");
        os.println(detailedData.getOriginalOutput());
      }
  }
  //----------------------------------------------------------------------
  
}
