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
package edu.ucsd.hep.roofitexplorer.datatypes;

import edu.ucsd.hep.roofitexplorer.WorkspaceData;
import edu.ucsd.hep.roofitexplorer.filters.WorkspaceMemberFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A list of workspace members with some additional functionality
 * @author holzner
 */
public class WorkspaceMemberList implements Iterable<GenericWorkspaceMember>
{
  private final List<GenericWorkspaceMember> members = new ArrayList<GenericWorkspaceMember>();

  //----------------------------------------------------------------------
  
  /** empty constructor */
  public WorkspaceMemberList()
  {
  }

  //----------------------------------------------------------------------
  
  /** copy constructor */
  public WorkspaceMemberList(Iterable<GenericWorkspaceMember> other)
  {
    for (GenericWorkspaceMember member : other)
    {
      members.add(member);
    }
  }
  
  //----------------------------------------------------------------------
  
  public int size()
  {
    return members.size();
  }

  //----------------------------------------------------------------------
  public boolean isEmpty()
  {
    return members.isEmpty();
  }

  //----------------------------------------------------------------------
  
  public boolean add(GenericWorkspaceMember e)
  {
    return members.add(e);
  }

  //----------------------------------------------------------------------
  public GenericWorkspaceMember get(int i)
  {
    return members.get(i);
  }

  //----------------------------------------------------------------------
  
  /** @return an unmodifiable list of all entries */
  public List<GenericWorkspaceMember> getList()
  {
    return Collections.unmodifiableList(this.members);
  }

  //----------------------------------------------------------------------

  public WorkspaceMemberList getPDFs()
  {
    WorkspaceMemberList retval = new WorkspaceMemberList();
    
    for (GenericWorkspaceMember obj : this.members)
    {
      if (obj instanceof RooAbsPdfData)
        retval.add(obj);
    }
    return retval;
  }

  //----------------------------------------------------------------------

  public WorkspaceMemberList getFunctions()
  {
    WorkspaceMemberList retval = new WorkspaceMemberList();
    
    for (GenericWorkspaceMember obj : this.members)
    {
      // ugly, should find a better way
      if (obj instanceof RooAbsRealData && !(obj instanceof RooAbsPdfData))
        retval.add(obj);
    }
    return retval;
  }

  //----------------------------------------------------------------------

  /** for being able to use a WorkspaceMemberList in a for statement.
   * 
   * @return an unmodifiable iterator */
  public Iterator<GenericWorkspaceMember> iterator()
  {
    return Collections.unmodifiableList(this.members).iterator();
  }

  //----------------------------------------------------------------------

  /** @return a new list with only the members which pass the filter */
  public WorkspaceMemberList filter(WorkspaceMemberFilter filter)
  {
    WorkspaceMemberList retval = new WorkspaceMemberList();
    for (GenericWorkspaceMember member : this)
    {
      if (filter.accept(member))
        retval.add(member);
    }
    
    return retval;
  }
  
  //----------------------------------------------------------------------

  @Override
  public String toString()
  {
    return this.members.toString();
  }

  //----------------------------------------------------------------------
 
  public WorkspaceData getFirstMembersWorkspace()
  {
    if (this.members.isEmpty())
      return null;
    
    return this.members.get(0).getWorkspace();
  }
  
  //----------------------------------------------------------------------
   
}
