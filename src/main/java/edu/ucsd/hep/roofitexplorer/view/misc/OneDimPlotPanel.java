/*
 * Copyright 2013 University of California, San Diego.
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
import edu.ucsd.hep.rootrunnerutil.ROOTRunner;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A panel do display one dimensional plots
 * @author holzner
 */
public class OneDimPlotPanel extends JPanel implements WorkspaceMemberModifiedListener
{
  private final ROOTRunner rootRunner;
  //----------------------------------------------------------------------
  private final String workspaceName;
  private final RooAbsRealData func;
  private final RooRealVarData xvariable;
  private final JLabel imageLabel;
  
  //----------------------------------------------------------------------

  public OneDimPlotPanel(ROOTRunner rootRunner, String workspaceName, final RooAbsRealData func,
          RooRealVarData xvariable) throws IOException
  {
    // TODO: if we ever wanted to serialize this, how would we 
    //       deal with the ROOTRunner object ?
    this.rootRunner = rootRunner;
    this.workspaceName = workspaceName;
    this.func = func;
    this.xvariable = xvariable;
  
    // needed to make the scrollbars appear
    this.setLayout(new BorderLayout());
    
    imageLabel = new JLabel("no plot available", JLabel.CENTER);

    // TODO: do we actually need a panel here ? Can't we directly
    // add the JLabel ?
    JPanel imagePanel = new JPanel(new BorderLayout());
    imagePanel.add(imageLabel, BorderLayout.CENTER );
    JScrollPane scrollPane = new JScrollPane(imagePanel);
    
    add(scrollPane);
    
    this.updatePlot();
  
    // request a notification if the corresponding function is modified
    func.getWorkspace().getModificationDispatcher().addListener(this, func);
  }
  
  //----------------------------------------------------------------------

  public final void updatePlot() throws IOException
  {
    String imageFname = rootRunner.createTempFile("rooFitExplorer",".png");
    
    String cmd = 
      "{ RooRealVar *xvar = " + workspaceName + "->var(\"" + xvariable.getVarName() + "\");\n" +
      "  RooPlot *frame = xvar->frame();\n" + 
      "  " + workspaceName + "->function(\"" + func.getVarName() + "\")->plotOn(frame);\n" +
      "  frame->Draw();\n" + 
      "  gPad->SaveAs(\"" + imageFname + "\");\n" +
      "}";
   
    // we actually ignore the return value
    rootRunner.getCommandOutput(cmd);
    
    byte[] imageData = rootRunner.readFile(imageFname);
    
    this.imageLabel.setIcon(new ImageIcon(imageData));
    
    // clear the text, otherwise it is displayed next to the image
    this.imageLabel.setText(""); 
  }
  
  //----------------------------------------------------------------------

  JInternalFrame makeInternalFrame()
  {
    JInternalFrame internalFrame = SimpleWorkspaceMemberListPanel.makeInternalFrame("plot of " + func.getVarName() + "(" + xvariable.getVarName() + ")", 
            this);

    return internalFrame;
  }
  
  //----------------------------------------------------------------------

  /** this is called whenever the corresponding pdf (or one of it's
   *  servers) is modified.
   * @param member 
   */
  public void modifed(GenericWorkspaceMember member)
  {
    try
    {
      // redraw the plot using ROOT
      updatePlot();
    } catch (IOException ex)
    {
      //Logger.getLogger(OneDimPlotPanel.class.getName()).log(Level.SEVERE, null, ex);
      JOptionPane.showMessageDialog(null, "failed to update plot for function " + func.getVarName(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  //----------------------------------------------------------------------

}
