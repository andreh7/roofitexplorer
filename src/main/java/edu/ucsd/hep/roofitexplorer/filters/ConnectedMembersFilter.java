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

/**
 * A filter accepting only those workspace members which are disconnected
 * (have no links to other members of the workspace) or connected 
 * (have at least one link to another member of the workspace)
 * 
 * @author holzner
 */
public class ConnectedMembersFilter implements WorkspaceMemberFilter
{
  private final boolean requireConnection;
  
  //----------------------------------------------------------------------

  public ConnectedMembersFilter(boolean requireConnection)
  {
    this.requireConnection = requireConnection;
  }

  //----------------------------------------------------------------------
 /** @return true for objects with neither clients nor servers 
   *  (i.e. which are most likely not used in the workspace)
   *  if requireConnection was set to false.
   * 
   */
  public boolean accept(GenericWorkspaceMember member)
  {
    // assumes that there are no self-loops
    boolean hasConnection = (member.hasClients() || member.hasServers());
    
    return hasConnection == requireConnection;
  }
  
  //----------------------------------------------------------------------

}
