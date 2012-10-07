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

/**
 * Interface for classes wanting to be notified when a member
 * of a workspace was selected (e.g. by clicking on a row in
 * a table etc.)
 *
 * TODO: should we also support the selection of multiple members
 *       at once ?
 * 
 * @author holzner
 */
public interface WorkspaceMemberSelectionListener
{
  /** this is called when a member was selected */
  public void workspaceMemberSelected(GenericWorkspaceMember member);
}
