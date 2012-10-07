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

import javax.swing.JPanel;

/**
 * Interface for WorkspaceMemberFilter classes which can be configured
 * through a JPanel
 * 
 * @author holzner
 */
public interface EditableWorkspaceMemberFilter extends WorkspaceMemberFilter
{
  /** must produce an object which reflects the current state of the
   *  filter settings.
   */
  public Object getState();
  
  /** must set the state of the filter to the given state */
  public void setState(Object state);
  
  /** must produce a JPanel (which will be used in a Dialog) to modify
   *  the given state. */
  public JPanel makeEditorPanel(Object state);
  
}
