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
import java.util.Comparator;

/**
 *
 * @author holzner
 */
public abstract class WorkspaceMemberComparator implements Comparator<GenericWorkspaceMember>
{

  private static class ComparatorByName extends WorkspaceMemberComparator
  {
    /** assume that there are no null pointers... */    
    public int compare(GenericWorkspaceMember m1, GenericWorkspaceMember m2)
    {
      return m1.getVarName().compareTo(m2.getVarName());
    }
  }
  
  
  private static class ComparatorByAddress extends WorkspaceMemberComparator
  {
    /** assume that there are no null pointers... */    
    public int compare(GenericWorkspaceMember m1, GenericWorkspaceMember m2)
    {
      return m1.getAddress().compareTo(m2.getAddress());
    }
  }
  
  /** factory method */
  public static WorkspaceMemberComparator make()
  {
    if (Parameters.useAddress)
      return new ComparatorByAddress();
    else
      return new ComparatorByName();
  }
}
