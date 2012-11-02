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

import edu.ucsd.hep.rootrunnerutil.AHUtils;
import edu.ucsd.hep.rootrunnerutil.ROOTRunner;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Class to find certain types of objects in a ROOT file which
 * has been opened already.
 * 
 * @author holzner
 */
public class ROOTObjectsFinder
{
  private final ROOTRunner rootRunner;
  private final String topLevelObjectVariableName;
  /** 
   * @param topLevelVariableName should be the name of a variable 
   * 
   * 
   * ('->Get(...)' and '->GetListOfKeys(..)' will be used with it).
   */ 
  public ROOTObjectsFinder(ROOTRunner rootRunner, String topLevelObjectVariableName)
  {
    this.rootRunner = rootRunner;
    this.topLevelObjectVariableName = topLevelObjectVariableName;
  }
  
  //----------------------------------------------------------------------

  /** does not recurse into subdirectories. The return value has names
   *  which have the top directory prepended. 
   *
   *  set topdir to null if this we should look at the top directory
   *  of the given object (i.e. use GetListOfKeys(..) directly as opposed to Get(..))
   * 
   *  TODO: should build a public test case to test this routine.
   */
  protected List<String> findInstancesOf(String className, String topdir, boolean recurse) throws IOException
  {
    // TODO: should explicitly declare the variables
    String cmds;
    
    if (topdir == null)
      cmds = "it = " + this.topLevelObjectVariableName + "->GetListOfKeys().MakeIterator();";
    else
      cmds = "it = " + 
            "((TDirectory *)(" + this.topLevelObjectVariableName + "->Get(\"" + topdir + "\")))->GetListOfKeys().MakeIterator();";
    
    
    cmds += "while ((key = ((TKey*) (it.Next()))) != NULL) {";
    
    // obj is a TKey
    String absPath;
    if (topdir == null)
      absPath = "key->GetName()";
    else
      absPath = "TString(\"" + topdir + "/\") + key->GetName()";
    
    cmds += "  TObject *obj = " + this.topLevelObjectVariableName + "->Get(" + absPath + "); \n";
    
    cmds += "  if (obj->IsA()->InheritsFrom(\"" + className + "\")) {";
    
    cmds += "    cout << key->GetName() << endl; }";
    cmds += "}\n";

    String output = rootRunner.getCommandOutput(cmds);
    
    List<String> retval = AHUtils.splitToLines(output);
    
    // ignore empty strings
    Iterator<String> it = retval.iterator();
    while (it.hasNext())
    {
      String value = it.next();
      if (value == null || value.isEmpty())
        it.remove();
    }
    
    if (topdir != null)
      for (int i = 0; i < retval.size(); ++i)
        retval.set(i, topdir + "/" + retval.get(i));
    
    if (recurse)
    {
      List<String> subDirNames = findInstancesOf("TDirectory", topdir, false);
      
      for (String subdir : subDirNames)
      {
        // note that subdir already contains the name of topdir, so there
        // is no need to prepend topdir to subdir
       
        retval.addAll(findInstancesOf(className, subdir, true));
      }
    }
    
    return retval;
  }
  
  //----------------------------------------------------------------------

  /** recurses into subdirectories */
  public List<String> findInstancesOf(String className) throws IOException
  {
    return findInstancesOf(className, null, true);
  }

  //----------------------------------------------------------------------
  
}
