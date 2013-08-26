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
package edu.ucsd.hep.roofitexplorer.view.graph;

import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;
import edu.ucsd.hep.roofitexplorer.datatypes.RooConstVarData;
import edu.ucsd.hep.roofitexplorer.datatypes.RooFormulaVarData;
import edu.ucsd.hep.roofitexplorer.datatypes.RooRealVarData;
import java.util.List;

/**
 *
 * @author holzner
 */
public class VertexType implements Comparable<VertexType>
{
  //----------------------------------------------------------------------

  private final GenericWorkspaceMember obj;
  private final String address;
  //----------------------------------------------------------------------

  VertexType(GenericWorkspaceMember obj)
  {
    this.obj = obj;
    this.address = obj.getAddress();
  }
  //----------------------------------------------------------------------

  public int compareTo(VertexType other)
  {
    return this.address.compareTo(other.address);
  }
 
  //----------------------------------------------------------------------

  /** this is currently also used to label the edges on the displayed graph */
  @Override
  public String toString()
  {
    // return this.obj.getClassName() + "::" + this.obj.getVarName();
  
    String retval = "<html>";
    
    retval +=
        this.obj.getClassName() + "<br/>" + 
        this.obj.getVarName() + "<br/>"; 

    // type specific information
    // note that we should test for the most specific types first...
    if (this.obj instanceof RooFormulaVarData)
      retval += this.getRooFormulaVarSpecificString((RooFormulaVarData)(this.obj));
    else if (this.obj instanceof RooConstVarData)
      retval += this.getRooConstVarSpecificString((RooConstVarData)(this.obj));
    else if (this.obj instanceof RooRealVarData)
      retval += this.getRooRealVarSpecificString((RooRealVarData)(this.obj));
    
    retval += "</html>";
    return retval;
  }
 
  //----------------------------------------------------------------------
  private String getRooRealVarSpecificString(RooRealVarData data)
  {
    String retval = "";
    if (data.value != null)
      retval += "value: " + data.value + "<br/>";
    
    if (data.isConstant != null)
    {
      if (data.isConstant)
        retval += "CONSTANT" + "<br/>";
      else
        retval += "not constant" + "<br/>";
    }
    
    return retval;
  }
  //----------------------------------------------------------------------

  
  private String getRooFormulaVarSpecificString(RooFormulaVarData data)
  {
    // template expression
    String retval = data.getFormulaTemplate() + "<br/>";
      
    // explain the variables
    List<String> varnames = data.getVariableNames();
    for (int i = 0; i < varnames.size(); ++i)
      retval += "@" + i + ": " + varnames.get(i) + "<br/>";

    // add expanded expressions
    retval += data.getExpandedFormulaTemplate() + "<br/>";
    
    return retval;
  }
  
  //----------------------------------------------------------------------
  private String getRooConstVarSpecificString(RooConstVarData data)
  {
    String retval = "";
    if (data.value != null)
      retval += "value: " + data.value + "<br/>";
    
    return retval;
  }

  //----------------------------------------------------------------------

  public GenericWorkspaceMember getObj()
  {
    return obj;
  }

}
