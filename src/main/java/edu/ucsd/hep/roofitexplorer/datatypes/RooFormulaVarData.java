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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains information about a RooFit RooAbsReal (e.g. used for functions)
 * @author holzner
 */
public class RooFormulaVarData extends RooAbsRealData
{  
  //----------------------------------------------------------------------

  /** the formula. Note that it looks like this is the string as originally
      given to the RooFormulaVar constructor so does not necessarily
      contain @ signs. */
  private final String formulaTemplate;
  
  /** the list of variable names (take from the names of the servers).
   *  Initialized on demand.
   */
  private List<String> variableNames;

  
  //----------------------------------------------------------------------
  
  /** note that while at a first glance it looks like
   *  the order of the variables is given by the line just after the formula,
   *  this seems not to be the case and it looks like the order of the variables
   *  is given by the order of the servers.
   */
  public RooFormulaVarData(WorkspaceData workspace, String varName, String className, VerbosePrintOutput detailedData)
  {
    super(workspace, varName, className, detailedData);
    // get the actual formula
    //
    // look for lines like the following:
    //
    //    --- RooFormula ---
    //    Formula: "@0*@1"
    //    (var1,var2)
    
    List<String> lines = new ArrayList<String>(detailedData.getLines());

    while (! lines.isEmpty())
    {
      String line = lines.remove(0);
      if (line.contains("--- RooFormula ---"))
        break;
    }
    
    if (lines.isEmpty())
      throw new Error("could not find line with --- RooFormula --- or no lines after");
    
    String formula = null;
    
    Pattern patFormula = Pattern.compile("\\s*Formula: \"([^\"]+)\"\\s*$");
    
    while (! lines.isEmpty())
    {
      String line = lines.remove(0);
      
      Matcher mo = patFormula.matcher(line);
      
      if (mo.matches())
      {
        // get everything between the first and second quote
        formula = mo.group(1);
        break;
      }
      
    } // loop over lines
    
    if (formula == null)
      throw new Error("could not find formula line");
    
    //-----
    this.formulaTemplate = formula;
    
    //-----
    // find variable numbering:
    //    first attempt: take from the line after the formula -> was incorrect
    //    second attempt: take from the order of the servers -> was incorrect
    //    third attempt: take from the lines after 'actualVars ->'

    lines = detailedData.findSubSection("RooAbsArg", "Proxies");
    
    if (lines.isEmpty())
      throw new Error("could not find Proxies subsection");
    
    String line = lines.remove(0).trim();
    if (! "actualVars ->".equals(line))
      throw new Error("expected 'actualVars ->', found '" + line + "'");
    
    Pattern patActualVar = Pattern.compile("\\s*\\d+\\)\\s*(\\S+)\\s*$");

    variableNames = new ArrayList<String>();
    
    for (String line2 : lines)
    {
      // TODO: should we check that the number is as expected ?
      Matcher mo = patActualVar.matcher(line2);
      
      if (! mo.matches())
        throw new Error("unexpected line format '" + line + "' when looking for actual variables");
      
      variableNames.add(mo.group(1));
    }
    
    
  }

  //----------------------------------------------------------------------

  public String getFormulaTemplate()
  {
    return formulaTemplate;
  }

  //----------------------------------------------------------------------

  /** @return the list of variable names for the formula (assumed to 
      be the servers) */
  public List<String> getVariableNames()
  {
    return Collections.unmodifiableList(this.variableNames);
  }
  
  //----------------------------------------------------------------------

  /** @return a string with all occurrences of \@n in the formula template
   *  substituted by the corresponding variable name (n starts at zero).
   */ 
  public String getExpandedFormulaTemplate()
  {
    String copy = formulaTemplate;
    Matcher mo = Pattern.compile("@([0-9]+)").matcher(formulaTemplate);
    List<String> variableNames = this.getVariableNames();
    
    StringBuffer buf = new StringBuffer();
    while (mo.find()) 
    {
      // append up to and including the replacement for this match
      int varIndex = Integer.parseInt(mo.group(1));
      mo.appendReplacement(buf, variableNames.get(varIndex));  
    }

    // append the rest
    mo.appendTail(buf);            

    return buf.toString();
    
  }

  //----------------------------------------------------------------------

}
