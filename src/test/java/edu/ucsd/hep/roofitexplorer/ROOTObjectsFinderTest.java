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

import edu.ucsd.hep.rootrunnerutil.PipeCommandRunnerListener;
import edu.ucsd.hep.rootrunnerutil.ROOTRunner;
import edu.ucsd.hep.rootrunnerutil.ROOTRunnerImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author holzner
 */
public class ROOTObjectsFinderTest
{
  final String inputFname = "testdata/ROOTObjectsFinderTest01.root";

  //----------------------------------------------------------------------

  /**
   * Test reading a subdirectory directly 
   */
  @Test
  public void test01() throws Exception
  {
    System.out.println("findInstancesOf");
 
    ROOTRunner rootRunner = new ROOTRunnerImpl(
            (PipeCommandRunnerListener)null,null,
            "",
      null);
    
    rootRunner.writeLine("fin = new TFile(\"" + inputFname + "\");");
    
    String className = "TH1";
    ROOTObjectsFinder instance = new ROOTObjectsFinder(rootRunner, "fin");
    
    List<String> result = instance.findInstancesOf(className,"subdir2", true);

    System.out.println("GOT " + result);

    assertTrue(result.isEmpty());    
  }

  //----------------------------------------------------------------------

  
  /**
   * Test reading a subdirectory with one histogram 
   */
  @Test
  public void test01b() throws Exception
  {
    System.out.println("findInstancesOf");
 
    ROOTRunner rootRunner = new ROOTRunnerImpl(
            (PipeCommandRunnerListener)null,null,
            "",
      null);
    
    rootRunner.writeLine("fin = new TFile(\"" + inputFname + "\");");
    
    String className = "TH1";
    ROOTObjectsFinder instance = new ROOTObjectsFinder(rootRunner, "fin");
    
    List<String> result = instance.findInstancesOf(className,"subdir1", true);

    
    System.out.println("GOT " + result);
    
    assertFalse(result.isEmpty());
   
    List<String> expResult = new ArrayList<String>();
    expResult.add("subdir1/histo2");
    assertEquals(expResult, result);
  }

  //----------------------------------------------------------------------
  /** find a histogram two depths deep */
  @Test
  public void test01c() throws Exception
  {
    System.out.println("findInstancesOf");
 
    ROOTRunner rootRunner = new ROOTRunnerImpl(
            (PipeCommandRunnerListener)null,null,
            "",
      null);
    
    rootRunner.writeLine("fin = new TFile(\"" + inputFname + "\");");
    
    String className = "TH1";
    ROOTObjectsFinder instance = new ROOTObjectsFinder(rootRunner, "fin");
    
    List<String> result = instance.findInstancesOf(className,"subdir3", true);

    
    System.out.println("GOT " + result);
    
    assertFalse(result.isEmpty());
   
    List<String> expResult = new ArrayList<String>();
    expResult.add("subdir3/dirA/histo1");
    assertEquals(expResult, result);
  }

  //----------------------------------------------------------------------
 
  
  /** tests finding all histograms in the hierarchy */
  @Test
  public void test02() throws Exception
  {
    System.out.println("findInstancesOf");
 
    ROOTRunner rootRunner = new ROOTRunnerImpl(
            (PipeCommandRunnerListener)null,null,
            "",
      null);
    
    rootRunner.writeLine("fin = new TFile(\"" + inputFname + "\");");
    
    String className = "TH1";
    ROOTObjectsFinder instance = new ROOTObjectsFinder(rootRunner, "fin");
    
    List result = instance.findInstancesOf(className);

    
    System.out.println("GOT " + result);
    
    List<String> expResult = new ArrayList<String>();
    expResult.add("subdir1/histo2");
    expResult.add("subdir3/dirA/histo1");
    assertEquals(expResult, result);
  }
  
  //----------------------------------------------------------------------

}
