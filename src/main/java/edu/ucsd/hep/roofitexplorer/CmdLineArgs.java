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

import java.util.ArrayList;
import java.util.List;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 *
 * @author holzner
 */
public class CmdLineArgs
{
  //----------

  @Option(name = "--root-window",
          usage = "show a window with the communication to the ROOT process")
  boolean showRootTerminal = false;

  //----------
  
  @Option(name = "--lib",
          metaVar = "LIB",
          usage = "load the given shared object after ROOT startup. "+
          "This is typically used to load rootcint generated compiled dictionaries for custom data types " +
          "used in the workspace. Can be specified multiple times.")
  List<String> librariesToLoad = new ArrayList<String>();
  
  //----------
  
  @Option(name = "--save",
          metaVar = "file.xgz",
          usage = "read the given root file, serialize it as gzipped xml to the given file and exit." +
                  "this can be useful to run remotely (e.g. where one has additional " +
                  "libraries ROOT needs to load to know the objects in the workspace), then transfer " + 
                  "the serialized file and run the GUI version locally. Note that when running with this " +
                  "option, one can still be shown a dialog to select a workspace etc. " + 
                  "The following extensions are supported: .xml , .xgz (gzipped xml), .xstream (xstream binary format), " + 
                  ".obj (java serialization, not guaranteed to be compatible with future releases)"
                  )
  String saveFileName;

  //----------
  
  @Option(name = "-p",
          metaVar = "profile",
          usage = "name of profile to load at startup (default is '" + UserProfileManager.defaultProfileName + "'). This is the name (without the .ini" + 
                  "extension in the directory ~/.roofitexplorer/profiles/ on Unix/OSX"
          )
  String profileName = UserProfileManager.defaultProfileName;
  
  
  
  @Option(name = "--show-profiles",
          usage = "Show the names of the known profiles and exit"
          )
  boolean showProfiles = false;
  
  
  //----------
  
  
  // positional arguments                                                                                                     
  @Argument(index = 0)
  String rootInputFname;
  
  @Argument(index = 1)
  String workspaceName;
  
}
