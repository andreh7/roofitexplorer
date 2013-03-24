/*
 * Copyright 2013 University of California, San Diego.
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;

/**
 * Class which keeps track of objects which want to be notified
 * when a workspace member was modified and dispatches the corresponding
 * events to listeners.
 * 
 * @author holzner
 */
public class WorkspaceMemberModificationDispatcher
{
  /** maps from object to be watched to a list of listeners */
  final Multimap<GenericWorkspaceMember, WorkspaceMemberModifiedListener> listeners = ArrayListMultimap.create();
          
  //----------------------------------------------------------------------
  
  /** register a listener for a given object */
  public void addListener(WorkspaceMemberModifiedListener listener, GenericWorkspaceMember watchedMember)
  {
    synchronized (this.listeners)
    {
      this.listeners.put(watchedMember, listener);
    }
  }
  
  //----------------------------------------------------------------------
  
  /** this is called if an object is modified. Note that this also
   *  takes care of propagating the notification to the watched objects
   *  clients (i.e. those which depend on watchedMember) if requested */
  public void modified(GenericWorkspaceMember watchedMember, boolean propagate)
  {
    synchronized (this.listeners)
    {
      // notify the listeners
      for (WorkspaceMemberModifiedListener listener : listeners.get(watchedMember))
        listener.modifed(watchedMember);
      
      // propagate to clients
      if (propagate)
      {
        // note that we do NOT prevent loops here if the workspace has loops
        // (which it should not have)
        for (GenericWorkspaceMember server : watchedMember.getClients())
        {
          modified(server, true);
        }
      }
    }         
  }
  
  //----------------------------------------------------------------------
  
  /** equivalent to the other 'modified' function with propagate = true */
  public void modified(GenericWorkspaceMember watchedMember)
  {
    modified(watchedMember, true);
  }
  
  //----------------------------------------------------------------------
  
}
