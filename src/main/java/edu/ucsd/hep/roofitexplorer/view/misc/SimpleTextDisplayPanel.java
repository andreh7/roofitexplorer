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

import edu.ucsd.hep.rootrunnerutil.AHUtils;
import java.awt.BorderLayout;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author holzner
 */
public class SimpleTextDisplayPanel extends JPanel
{
  private JFileChooser saveAsFileChooser;
 
  private JTextArea textArea;

  //----------------------------------------------------------------------

  public SimpleTextDisplayPanel()
  {
    setLayout(new BorderLayout());
    
    textArea = new JTextArea();
    textArea.setFont(new java.awt.Font("Monospaced", 0, 13));

    add(new JScrollPane(textArea));

    JButton buttonSaveAs = new JButton("Save as...");
    
    buttonSaveAs.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonSaveAsActionPerformed(evt);
      }
    });
  
    add(buttonSaveAs, BorderLayout.SOUTH);
  }

  //----------------------------------------------------------------------
  
  private void buttonSaveAsActionPerformed(java.awt.event.ActionEvent evt)
  {
    if (this.saveAsFileChooser == null)
      this.saveAsFileChooser = new JFileChooser();

    int result = this.saveAsFileChooser.showSaveDialog(this);
    if (result != JFileChooser.APPROVE_OPTION)
      return;

    try
    {
      AHUtils.writeStringToFile(this.saveAsFileChooser.getSelectedFile(), this.textArea.getText());
    } catch (Exception ex)
    {
      Logger.getLogger(SimpleTextDisplayPanel.class.getName()).severe(ex.toString());
      JOptionPane.showMessageDialog(this, "error saving text to " + this.saveAsFileChooser.getSelectedFile() + ": " + ex.toString());
    }
  }
  
  //----------------------------------------------------------------------
   
  /** set the text content of the panel */
  public void setText(String text)
  {
    this.textArea.setText(text);
  }

  //----------------------------------------------------------------------
 public JInternalFrame makeInternalFrame(String title)
  {
    JInternalFrame retval = new JInternalFrame(title,
            true, // resizable
            true, // closable
            true, // maximizable
            true // iconifiable
            );
    retval.getContentPane().add(this);
    retval.pack();

    retval.setSize(500, 500);

    return retval;
  }

  //----------------------------------------------------------------------
  public static JFrame makeFrame(String title, String text_content)
  {
    JFrame frame = new JFrame(title);

    SimpleTextDisplayPanel panel = new SimpleTextDisplayPanel();
    panel.setText(text_content);

    frame.getContentPane().add(panel);
    frame.pack();

    return frame;
  }

  //----------------------------------------------------------------------
  
  /** for testing */
  public static void main(String argv[])
  {
    JFrame frame = makeFrame("title","TEST\nTEST");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);

  }
  
  //----------------------------------------------------------------------

}
