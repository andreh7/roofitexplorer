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
  @Option(name = "--root-window",
          usage = "show a window with the communication to the ROOT process")
  boolean showRootTerminal = false;

  @Option(name = "--lib",
          metaVar = "LIB",
          usage = "load the given shared object after ROOT startup. "+
          "This is typically used to load rootcint generated compiled dictionaries for custom data types " +
          "used in the workspace. Can be specified multiple times.")
  List<String> librariesToLoad = new ArrayList<String>();
  
  // positional arguments                                                                                                     
  @Argument(index = 0)
  String rootInputFname;
  
  @Argument(index = 1)
  String workspaceName;
  
}
