/*
 * Copyright 2014 University of California, San Diego.
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

package edu.ucsd.hep.roofitexplorer.view.misc;

import edu.ucsd.hep.roofitexplorer.WorkspaceMemberModifiedListener;
import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;
import edu.ucsd.hep.roofitexplorer.datatypes.RooAbsRealData;
import edu.ucsd.hep.roofitexplorer.datatypes.RooRealVarData;
import edu.ucsd.hep.rootrunnerutil.AHUtils;
import edu.ucsd.hep.rootrunnerutil.ROOTRunner;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * class to display the value of a RooRealVar (the display updates
 * if any of its servers is modified)
 * 
 * @author holzner
 */
public class RooAbsRealValueDisplay extends JPanel implements WorkspaceMemberModifiedListener
{
  private RooAbsRealData func;
  private JLabel valueLabel;
  private ROOTRunner rootRunner;
  private String workspaceName;
  
  public RooAbsRealValueDisplay(ROOTRunner rootRunner, String workspaceName, RooAbsRealData func) throws IOException
  {
    this.rootRunner = rootRunner;
    this.workspaceName = workspaceName;
    this.func = func;
    
    this.setLayout(new BorderLayout());
    
    this.valueLabel = new JLabel("-");
    
    add(valueLabel, BorderLayout.CENTER);

    modifed(func);
  
    // request a notification if the corresponding function is modified
    func.getWorkspace().getModificationDispatcher().addListener(this, func);

  }
  
  //----------------------------------------------------------------------
  
  JInternalFrame makeInternalFrame()
  {
    JInternalFrame internalFrame = SimpleWorkspaceMemberListPanel.makeInternalFrame("value of " + func.getVarName() + "(" + 
            func.getVarName()+ ")", 
            this);

    internalFrame.setSize(100, 200);
    
    return internalFrame;
  }
  
  //----------------------------------------------------------------------
  
  public void modifed(GenericWorkspaceMember member)
  {
     String cmd = 
      "{ RooAbsReal *func = " + workspaceName + "->function(\"" + func.getVarName() + "\");\n" +
      "  cout << func->getVal() << endl;\n" +
      "}";
   
    // we actually ignore the return value
    String output;
    try
    {
      output = rootRunner.getCommandOutput(cmd);
      
      // only keep the first line, the second line is something like
      // (class ostream)47168996317056
      output = AHUtils.splitToLines(output).get(0);
   
    } catch (IOException ex)
    {
      Logger.getLogger(RooAbsRealValueDisplay.class.getName()).log(Level.SEVERE, null, ex);
      output = "error retrieving value";
    }
    this.valueLabel.setText(output); 
  }
  //----------------------------------------------------------------------
  
}
