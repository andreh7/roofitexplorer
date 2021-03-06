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
 *
 * @author holzner
 */
public class TopLevelMembersFilter implements WorkspaceMemberFilter
{
  /** @return true for those nodes which do not have any parents (clients)
      (but have children, i.e. are not disconnected) */
  public boolean accept(GenericWorkspaceMember member)
  {
    return ! member.hasClients() && // no clients (nobody is using 'member')
             member.hasServers();   // but is not disconnected
  }
  
}
