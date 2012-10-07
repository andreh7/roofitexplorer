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
package edu.ucsd.hep.roofitexplorer.view.misc;

import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;
import edu.ucsd.hep.roofitexplorer.datatypes.WorkspaceMemberList;
import edu.ucsd.hep.roofitexplorer.view.textsearch.TextSearchTableModel;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author holzner
 */
public class SimpleWorkspaceMemberTableModel extends AbstractTableModel implements TextSearchTableModel
{
  private WorkspaceMemberList members = new WorkspaceMemberList();

  enum Column
  {
    COL_VARNAME,
    COL_CLASSNAME,
    
    // COL_DESCRIPTION,
    
    COL_NUM_DIRECT_SERVERS,
    
    COL_NUM_DIRECT_CLIENTS,
    
    /** includes the servers' servers etc. */
    COL_OVERALL_NUM_SERVERS,
        
    COL_OVERALL_NUM_CLIENTS,

    // later more: parents (or their number), children (or their number)
    
  }
  
  private final Column activeColumns[] = Column.values();
  
  //----------------------------------------------------------------------

  public int getRowCount()
  {
    return members.size();
  }
  
  //----------------------------------------------------------------------

  public int getColumnCount()
  {
    return activeColumns.length;
  }

  //----------------------------------------------------------------------

  public Object getValueAt(int row, int col)
  {
    GenericWorkspaceMember member = this.members.get(row);
    
    switch (activeColumns[col])
    {
      case COL_VARNAME: return member.getVarName();
      case COL_CLASSNAME:    return member.getClassName();
      // case COL_DESCRIPTION:  return member.
        
      case COL_NUM_DIRECT_CLIENTS: return member.getNumClients();
      
      
      case COL_NUM_DIRECT_SERVERS: return member.getNumServers();
        
        
      case COL_OVERALL_NUM_CLIENTS: return member.getOverallNumClients();

      case COL_OVERALL_NUM_SERVERS: return member.getOverallNumServers();

      default:  return "???";
    }
  }
   
  //----------------------------------------------------------------------

  @Override
  public String getColumnName(int col)
  {
   switch (activeColumns[col])
    {
      case COL_VARNAME: return "variable";
      case COL_CLASSNAME:    return "class";
      // case COL_DESCRIPTION:  return member.
        
      case COL_NUM_DIRECT_CLIENTS: return "# direct clients";
      
      
      case COL_NUM_DIRECT_SERVERS: return "# direct servers";
        
        
      case COL_OVERALL_NUM_CLIENTS: return "# overall clients";

      case COL_OVERALL_NUM_SERVERS: return "# overall servers";

      default:  return "???";
    }
  }
   
  //----------------------------------------------------------------------

  /** needed for table row sorting to work properly */
  @Override
  public Class<?> getColumnClass(int col)
  {
   switch (activeColumns[col])
    {
      // note the fallthrough 
      case COL_VARNAME: 
      case COL_CLASSNAME:    
        return String.class;
      
        // case COL_DESCRIPTION:  return member.
        
      // note the fallthrough
      case COL_NUM_DIRECT_CLIENTS: 
      case COL_NUM_DIRECT_SERVERS:
      case COL_OVERALL_NUM_CLIENTS:
      case COL_OVERALL_NUM_SERVERS:
        return Integer.class;
      
      default:  return super.getColumnClass(col);
    }
  }
  
  //----------------------------------------------------------------------

  /** for the moment does not make a defensive copy,
   *  assumes that the list is never modified
   */
  public void setData(WorkspaceMemberList data)
  {
    this.members = data;
    this.fireTableDataChanged();
  }
  
  //----------------------------------------------------------------------

  GenericWorkspaceMember getMember(int modelRow)
  {
    return this.members.get(modelRow);
  }  
  
  //----------------------------------------------------------------------
  // interface TextSearchTableModel
  //----------------------------------------------------------------------
 
  public boolean isRowEnabled(int row)
  {
    // for the moment, all rows are enabled
    return true;
  }

  //----------------------------------------------------------------------

  public String getRowTextForSearching(int row)
  {
    // GenericWorkspaceMember member = this.members.get(row);
    
    String retval = "";
    
    for (int col = 0; col < this.activeColumns.length; ++col)
    {
      if (col > 0)
        retval += " ";
      
      retval += this.getValueAt(row, col).toString();
    }
    
    return retval;
    
  }
  
  //----------------------------------------------------------------------
  // end of interface TextSearchTableModel
  //----------------------------------------------------------------------

  public WorkspaceMemberList getMembers()
  {
    return members;
  }
 
}
