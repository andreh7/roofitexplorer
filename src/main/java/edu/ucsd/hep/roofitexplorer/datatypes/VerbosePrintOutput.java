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
package edu.ucsd.hep.roofitexplorer.datatypes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import edu.ucsd.hep.rootrunnerutil.AHUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Serializable;

/**
 * contains information and associated helper methods for the 
 * output lines of Print("V") called on a workspace member
 * @author holzner
 */
public class VerbosePrintOutput implements Serializable
{
  //----------------------------------------------------------------------
  
  /** maps from the sections to the lines in this section */
  private final ListMultimap<String, String> data = ArrayListMultimap.create();
  
  /** the original output of the Print("V") command. Kept for debugging
   *  purposes */
  private final String originalOutput;
  
  /** the original printout split into lines */
  private final List<String> lines;

  //----------------------------------------------------------------------

  /** performs some splitting of the output */
  public VerbosePrintOutput(String output)
  {
    this.originalOutput = output;
    lines = AHUtils.splitToLines(output);
    
    // split into different sections
    String currentSectionName  = "";
    
    Pattern pattern = Pattern.compile("--- (\\S+) ---");
    for (String line : lines)
    {
      if (line.trim().isEmpty())
        continue;
      
      Matcher mo = pattern.matcher(line);
      if (mo.matches())
      {
        // new section found
        currentSectionName = mo.group(1);
        continue;
      }
      
      data.put(currentSectionName, line);

    }
  }

  //----------------------------------------------------------------------

  /** looks for a line of the form ' key: ...' in the given section
   *  and returns the ...
   * @param section
   * @param key
   * @return null if not found
   */
  public String findValue(String section, String key)
  {
    Pattern pattern = Pattern.compile("\\s*" + Pattern.quote(key) + ":\\s+(.*)");
    for (String line : this.data.get(section))
    {
      Matcher mo = pattern.matcher(line);
      if (! mo.matches())
        continue;
      
      return mo.group(1);
    }
    
    // not found
    return null;
  }
  
  //----------------------------------------------------------------------

  /** for things which span multiple lines, e.g. the 'Servers:' and
   *  'Clients:' section
   * @param section    to be specified without the three dashes (---) on either side
   * @param subsection to be specified without the trailing colon 
   * @return 
   */
  public List<String> findSubSection(String section, String subsection)
  {
    Pattern pattern = Pattern.compile("(\\s*)" + Pattern.quote(subsection) + ":\\s*");
    
    int numSpacesAtBeginning = -1;
    List<String> lines = this.data.get(section);
    int numLines = lines.size();
    int i = 0;
    while (i < numLines)
    {
      String line = lines.get(i++);
      Matcher mo = pattern.matcher(line);
      if (mo.matches())
      {
        numSpacesAtBeginning = mo.group(1).length();
        break;
      }
      
    }
    
    if (numSpacesAtBeginning == -1)
      // not found
      return null;
    
    // add all following lines which have at least this amount of spaces
    List<String> retval = new ArrayList<String>();
    
    // require at least the given amount of spaces (note the comma)
    // note also the +1
    Pattern pattern2 = Pattern.compile("\\s{" + (numSpacesAtBeginning + 1) + ",}\\S+");
    while (i < numLines)
    {
      String line = lines.get(i++);
      if (! pattern2.matcher(line).lookingAt())
        break;
      
      retval.add(line);
      
    }
    
    return retval;
  }
  //----------------------------------------------------------------------

  public static class ClientServerRecord implements Serializable
  {
    final String address;
    final String className;
    final String varName;

    public ClientServerRecord(String address, String className, String varName)
    {
      this.address = address;
      this.className = className;
      this.varName = varName;
    }
    
    private static ClientServerRecord parse(String line)
    {
      // example line:
      //    (0x2fc1330,V-) RooHistFunc::funcf2cat1 "funcf2cat1"
      //    (0x1c34b80,V-) RooAddPdf::hggpdf_cat2 ""
      //    (0x18433f0,V-) RooRecursiveFraction::combhminslidecat2_recursive_fraction_g2slidecat2 "Recursive Fraction"
      Pattern pattern = Pattern.compile(
        "\\s*\\((0x\\S+),\\S+\\) (\\S+)::(\\S+) \\\"([^\\\"]*)\\\"\\s*"
        );
      Matcher mo = pattern.matcher(line);
      
      mo.matches();
      
      
      return new ClientServerRecord(mo.group(1),
                                    mo.group(2),
                                    mo.group(3));
    }

    public String getAddress()
    {
      return address;
    }

    public String getClassName()
    {
      return className;
    }

    public String getVarName()
    {
      return varName;
    }
    
    
  }

  //----------------------------------------------------------------------
  
  public List<ClientServerRecord> parseClientsOrServers(String section, boolean servers)
  {
    List<String> lines;
    if (servers)
      lines = findSubSection(section, "Servers");  
    else
      lines = findSubSection(section, "Clients");
    
    List<ClientServerRecord> retval = new ArrayList<ClientServerRecord>();
    
    // note that RooAbsData objects seem not to have clients nor
    // servers. In that case, lines is a null pointer and for convenience
    // we just make retval the empty list
    
    if (lines != null)
    {
      for (String line : lines)
      {
        retval.add(ClientServerRecord.parse(line));
      }
    }
    
    return retval;
      
  
  }
  
  //----------------------------------------------------------------------
  

  @Override
  public String toString()
  {
    return "VerbosePrintOutput{" + "data=" + data + '}';
  }
  //----------------------------------------------------------------------

  public String getOriginalOutput()
  {
    return originalOutput;
  }
  
  //----------------------------------------------------------------------

  public List<String> getLines()
  {
    return Collections.unmodifiableList(lines);
  }
  //----------------------------------------------------------------------

}
