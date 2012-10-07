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

import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Keeps a list of WorkspaceMemberSelectionListener objects
 * @author holzner
 */
public class WorkspaceMemberSelectionListenerList implements WorkspaceMemberSelectionListener,
   Iterable<WorkspaceMemberSelectionListener>
{
  private final List<WorkspaceMemberSelectionListener> listeners = new ArrayList<WorkspaceMemberSelectionListener>();
  
  //----------------------------------------------------------------------

  /** propagate to the listeners */
  public void workspaceMemberSelected(GenericWorkspaceMember member)
  {
    synchronized (this.listeners)
    {
      for (WorkspaceMemberSelectionListener listener : this.listeners)
      {
        listener.workspaceMemberSelected(member);
      }
    }
  }

  //----------------------------------------------------------------------

  public boolean addListener(WorkspaceMemberSelectionListener listener)
  {
    synchronized (this.listeners)
    {
      return listeners.add(listener);
    }
  }

  //----------------------------------------------------------------------

  public boolean removeListener(WorkspaceMemberSelectionListener listener)
  {
    synchronized (this.listeners)
    {
      return listeners.remove(listener);
    }
  }

  //----------------------------------------------------------------------

  /** @return an iterator to a readonly copy of this list */
  public Iterator<WorkspaceMemberSelectionListener> iterator()
  {
    return Collections.unmodifiableList(this.listeners).iterator();
  }
  //----------------------------------------------------------------------

}
