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

import java.io.File;
import java.io.IOException;

/**
 * Provides some utilities for handling user profiles.
 * @author holzner
 */
public class UserProfileManager
{

  public static final String defaultProfileName = "default";  
  
  //----------------------------------------------------------------------

  /** @return the directory where the user profiles are stored */
  private static File getUserProfileDirectory()
  {
    // does this works on platforms other than Unix ?
    
    String path = System.getProperty("user.home") + File.separatorChar 
      + ".roofitexplorer" + File.separatorChar 
      + "profiles";
    
    return new File(path);
  }
  
  //----------------------------------------------------------------------

  private static void ensureUserProfileDirectoryExists()
  {
    File path = getUserProfileDirectory();
    if (path.exists())
      return;
    
    path.mkdirs();
  }
  
  //----------------------------------------------------------------------

  private static File getUserProfileAbsolutePath(String profileName, String suffix)
  {
     return new File(getUserProfileDirectory().getAbsolutePath() + File.separatorChar + profileName + suffix);
  }
  
  //----------------------------------------------------------------------
  
  /** @return the given profile or a default (empty) if it does not exist */
  public static UserProfileData getProfile(String profileName) throws IOException
  {
    File path = getUserProfileAbsolutePath(profileName, ".ini");
    
    // System.err.println("trying to read initialization file " + path);
    
    if (! path.exists())
    {
      // return a default profile if we looked for the default profile
      if (profileName.equals(defaultProfileName))
        return new UserProfileData();
      else
        throw new Error("could not read user profile '" + profileName + "' (file " + path.getAbsolutePath() + " does not exist)");
    
    }
    return UserProfileData.readFromIniFile(path);
  }
  
  //----------------------------------------------------------------------

  /** writes (serializes) the user profile data to the given file.
   *  If it exists already, it is overwritten.
   * @param profileName
   * @param profileData 
   */
  public static void writeProfile(String profileName, UserProfileData profileData) throws IOException
  {
    ensureUserProfileDirectoryExists();
    
    profileData.writeToIniFile(getUserProfileAbsolutePath(profileName, ".ini"));
    
  }
  
  //----------------------------------------------------------------------
  public static boolean profileExists(String profileName)
  {
    return getUserProfileAbsolutePath(profileName, ".ini").exists();
  }
  
  //----------------------------------------------------------------------
}
