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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List of factories for creating WorkspaceMemberFilter objects.
 * Main use is to create new instances of filters in the GUI.
 * @author holzner
 */
public class MemberFilterFactoryList 
{
  private final Map<String, MemberFilterFactory> factories = new HashMap<String, MemberFilterFactory>();
  
  //----------------------------------------------------------------------

  public MemberFilterFactory add(String name, MemberFilterFactory factory)
  {
    // make sure we don't already have 
    if (factories.containsKey(name))
      throw new IllegalArgumentException("a filter factory with name " + name + " exists already");
    
    return factories.put(name, factory);
  }

  //----------------------------------------------------------------------

  public WorkspaceMemberFilter createInstance(String factoryName)
  {
    return this.factories.get(factoryName).createInstance();
  }
  
  //----------------------------------------------------------------------

  public List<String> getFactoryNames()
  {
    return new ArrayList<String>(this.factories.keySet());
  }

  //----------------------------------------------------------------------

  /** creates a list with a few standard filters */
  public static MemberFilterFactoryList makeStandardFilterFactory()
  {
    MemberFilterFactoryList retval = new MemberFilterFactoryList();
    
    retval.add("top level members", 
            new MemberFilterFactory()
            { public WorkspaceMemberFilter createInstance()
              { return new TopLevelMembersFilter(); }});

    retval.add("leaf members", new MemberFilterFactory() {
      public WorkspaceMemberFilter createInstance() { return new LeafMembersFilter(); }});
    
    
    retval.add("disconnected members", new MemberFilterFactory(){
      public WorkspaceMemberFilter createInstance() { return new ConnectedMembersFilter(false);}});
      
    
    retval.add("connected members", new MemberFilterFactory(){
      public WorkspaceMemberFilter createInstance() { return new ConnectedMembersFilter(true);}});
    
    
    return retval;
    
  }

  //----------------------------------------------------------------------
  
}
