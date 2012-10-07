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
package edu.ucsd.hep.roofitexplorer.filters;

import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * implements the AND of several filters
 * @author holzner
 */
public class WorkspaceMemberFilterList implements WorkspaceMemberFilter, Iterable<WorkspaceMemberFilter>
{
  private final List<WorkspaceMemberFilter> filters = new ArrayList<WorkspaceMemberFilter>();

  //----------------------------------------------------------------------
  
  public boolean accept(GenericWorkspaceMember member)
  {
    synchronized (this.filters)
    {
      for (WorkspaceMemberFilter filter : filters)
      {
        if (!filter.accept(member))
        {
          return false;
        }
      }
    }
    return true;
  }

  //----------------------------------------------------------------------

  public boolean add(WorkspaceMemberFilter e)
  {
    synchronized (this.filters)
    {
      return filters.add(e);
    }
  }
  
  //----------------------------------------------------------------------

  public boolean remove(WorkspaceMemberFilter o)
  {
    synchronized (this.filters)
    {
      return filters.remove(o);
    }
  }
  
  //----------------------------------------------------------------------

  public Iterator<WorkspaceMemberFilter> iterator()
  {
    return this.filters.iterator();
  }

  //----------------------------------------------------------------------

  public int size()
  {
    return filters.size();
  }
  
  //----------------------------------------------------------------------

  public int indexOf(Object filter)
  {
    return filters.indexOf(filter);
  }

  //----------------------------------------------------------------------

  public WorkspaceMemberFilter remove(int index)
  {
    return filters.remove(index);
  }
  
  

}
