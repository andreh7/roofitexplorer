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

import edu.ucsd.hep.roofitexplorer.filters.WorkspaceMemberFilter;
import edu.ucsd.hep.roofitexplorer.view.textsearch.TextSearchRowFilter;
import javax.swing.RowFilter;

/**
 * a RowFilter which allows combining a text search row filter
 * and a WorkspaceMemberFilter.
 * 
 * We're not using RowFilter.andFilter(..) because we use Row Filters
 * for different types. Also the conversion from a WorkspaceMemberFilter
 * to a RowFilter is implicitly done in the include(..) method.
 * 
 * @author holzner
 */
class CombinedRowFilter extends RowFilter<SimpleWorkspaceMemberTableModel, Integer>
{ 
  //----------------------------------------------------------------------

  private WorkspaceMemberFilter memberFilter;
  private final TextSearchRowFilter textFilter;
 
  //----------------------------------------------------------------------

  public CombinedRowFilter(WorkspaceMemberFilter memberFilter, TextSearchRowFilter textFilter)
  {
    this.memberFilter = memberFilter;
    this.textFilter = textFilter;
  }
  
  //----------------------------------------------------------------------

  /** note that this will NOT trigger any re-drawing of the associated
   *  table */
  public void setMemberFilter(WorkspaceMemberFilter memberFilter)
  {
    this.memberFilter = memberFilter;
  }

  //----------------------------------------------------------------------

  @Override
  public boolean include(Entry<? extends SimpleWorkspaceMemberTableModel, ? extends Integer> entry)
  {
    SimpleWorkspaceMemberTableModel model = entry.getModel();
    Integer row = entry.getIdentifier();
    
    // first ask the text filter
    if (textFilter != null && ! textFilter.include(entry))
      return false;
    
    // then try the member filter
    return memberFilter == null || memberFilter.accept(model.getMember(row));
    
  }
  
}
