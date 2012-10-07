/*
 * Copyright 2011 University of California, San Diego.
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

/**
 * class containing information about a user profile 
 * (path to ROOT etc.)
 * @author holzner
 */
public class UserProfileData
{
  //----------------------------------------------------------------------
  /** commands to be executed before starting the ROOT session */
  private String preRootShellCommands = "";

  /** the base directory of the ROOT installation */
  private String rootSys = "";
  
  //----------------------------------------------------------------------
  public String getPreRootShellCommands()
  {
    return preRootShellCommands;
  }

  //----------------------------------------------------------------------
  
  public void setPreRootShellCommands(String preRootShellCommands)
  {
    this.preRootShellCommands = preRootShellCommands;
  }

  //----------------------------------------------------------------------

  public String getRootSys()
  {
    return rootSys;
  }
  //----------------------------------------------------------------------

  public void setRootSys(String rootSys)
  {
    this.rootSys = rootSys;
  }
  //----------------------------------------------------------------------

}
