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
package edu.ucsd.hep.roofitexplorer.view.misc;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Dialog to show used libraries
 *
 * (see also e.g. http://stackoverflow.com/questions/8587291 on bunding LPGL libraries with an application)
 *
 * @author holzner
 */
public class AboutDialog extends JDialog
{
  
  // TODO: ADD LINKS TO THE SOURCE CODES
  private Object libraryNames[] = 
  {
     // name, url, license
    // rootrunnerutil
    
    new String[]{ "args4j",
                  "http://github.com/kohsuke/args4j", 
                  "MIT License"},
    
    new String[]{ "Guava collections", 
                  "https://code.google.com/p/guava-libraries/",  
                  "Apache License 2.0"},
    
    new String[]{ "JUNG",                      
                  "http://jung.sourceforge.net/", 
                  "BSD License" },
    
    new String[]{ "Apache POI",                
                  "http://poi.apache.org/", 
                  "Apache License 2.0"},

    new String[]{ "Apache Commons Exec",                
                  "http://commons.apache.org/exec/", 
                  "Apache License 2.0"},
    
    new String[]{ "SwingX", 
                  "http://swingx.java.net/", 
                  "LGPL 2.1"}, // used by WorkspaceListBrowser (which could go away ?)
    
    new String[]{ "XStream", 
                  "http://xstream.codehaus.org/", 
                  "BSD License"},
    
    new String[]{ "Jakarta Commons-Collections with Generics", 
                  "http://collections.sourceforge.net/", 
                  "Apache License 2.0"},
    
    // used by Jung
    new String[]{ "Colt",
                  "http://acs.lbl.gov/software/colt/",
                  "LGPL 2.1 (hep.aida.*) / other (cern.colt* , cern.jet*, cern.clhep)"},
    
    // used by Colt
    new String[]{ "Dough Lea's util.concurrent package",
                  "http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html",
                  "Public domain, Sun Microsoystems"},
    
    // filters, used by swinglabs
    new String[]{ "JHLabs Image Processing Filters",
                  "http://www.jhlabs.com/ip/index.html",
                  "Apache License 2.0"},
    
    // used by swingx
    new String[]{ "Swing Layout Extensions",
                  "http://java.net/projects/swingworker/",
                  "LGPL 2.1"},
    
    // used by XStream
    new String[]{ "XML Pull Parsing API",
                  "http://www.xmlpull.org/",
                  "public domain"},

    // used by XStream
    new String[]{ "MXP1: Xml Pull Parser 3rd Edition (XPP3)",
                  "http://www.extreme.indiana.edu/xgws/xsoap/xpp/mxp1/",
                  "public domain"},
    
    new String[]{ "JUnit", 
                  "http://www.junit.org", 
                  "Common Public License - v 1.0"},
  
    // used at top level
    new String[]{ "Macify", 
                  "http://simplericity.org/macify/", 
                  "Apache License 2.0"},
    
    
  };
  
  //----------------------------------------------------------------------
  
  public AboutDialog(Frame frame)
  {
    super(frame, "About RooFitExplorer", true /* modal */);
    CompoundBorder overallBorder = new CompoundBorder(
            new CompoundBorder(
            BorderFactory.createEmptyBorder(10,10,10,10),
            BorderFactory.createEtchedBorder()),
               BorderFactory.createEmptyBorder(20,20,20,20)

            );
    JPanel textPanel;
    textPanel = makeTextPanelNew();
    textPanel.setBorder(overallBorder);
    getContentPane().add(textPanel, BorderLayout.CENTER);
    
    //----------
    JPanel buttonPanel = new JPanel();
    
    JButton okButton = new JButton("Ok");
    buttonPanel.add(okButton);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    okButton.addActionListener(new ActionListener() 
    {
      public void actionPerformed(ActionEvent evt) 
      {
        setVisible(false);
      }
    });

    pack();
    
    
    setSize(this.getPreferredSize());
    this.setResizable(false);
  }

  //----------------------------------------------------------------------

  private JPanel makeTextPanelNew()
  {
    JPanel textPanel = new JPanel();
    textPanel.setLayout(new BorderLayout());

    JEditorPane editorPane = new JEditorPane();
   
    editorPane.setEditable(false);
  
    
    //-----
    List<String> lines = new ArrayList<String>();
  
    lines.add("<font color=\"red\" size=\"+2\">RooFitExplorer</font> &mdash; \u00A9 2011-2013 University of California, San Diego");
    lines.add("");
    lines.add("distributed under the <a href=\"http://www.apache.org/licenses/LICENSE-2.0.txt\">Apache License 2.0</a>");
    lines.add("");
    lines.add("written by Andr&eacute; Holzner");
 
    //-----
    
    String text = "<html>"
            + "<style type=\"text/css\">"
            + "table.tablestyle {"
            + "border-width: 0px;"
            + "border-spacing: 5px;"
            + "border-collapse: separate;"
            + "}"
            + ""
            + "table.tablestyle td {"
            + "padding: 5px;"
            + "background-color: rgb(255,255,240);"
            + "}"
            + "</style>"
            + "<body>"
            + "<center>";
    
    text += "<div style=\"background-color: rgb(255,255,240); padding: 5px;\">";
    for (String line : lines)
    {
      text += line + "<br/>";
    }
   
    //-----
    // add library information
    //-----
    text += "</div>";
    text += "</center>";

    text += "<br/><br/>Libraries used:<br/><br/>\n";
    text += "<center>";
    text += "<TABLE class=\"tablestyle\">\n";
    
    text += "<tr>"
         + "<th>Library</th>"
         + "<th>Project Website</th>"
       //  + "<th>License</th>"
         + "</tr>"
         ;
    
    for (Object item : libraryNames)
    {
      String line[] = (String[])item;

      text += "<tr>";
      text += "<td >" + line[0] + "</td>";
      text += "<td><a href=\"" + line[1] + "\">" + line[1] + "</a></td>";
      // text += "<td>" + line[2] + "</td>";
      text += "</tr>\n";
    }
    
    text += "</TABLE>";
    text += "</center>";
    
    //-----
    lines.add("</body></html>");

    // System.out.println(text);

   
    editorPane.setContentType("text/html");
    editorPane.setText(text);
    
    editorPane.setBackground(this.getBackground());
    
    //-----
    // add support for clicking the hyperlinks
    editorPane.addHyperlinkListener(new HyperlinkListener() 
    {
      public void hyperlinkUpdate(HyperlinkEvent event) 
      {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
        {
          // TODO: do we need a swingworker here ?
          openURL(event.getURL());
        }
      }
    });
    
    textPanel.add(editorPane, BorderLayout.CENTER);
    return textPanel;
  }

  //----------------------------------------------------------------------

  private void openURL(URL url)
  {
    try
    {
      Desktop.getDesktop().browse(new URI(url.toString()));
    } catch (Exception ex)
    {
      Logger.getLogger(AboutDialog.class.getName()).log(Level.SEVERE, null, ex);
      JOptionPane.showMessageDialog(this, "failed to start browser for " + url + ":" + ex.getMessage());
    }
  }
  
  //----------------------------------------------------------------------

  /** for testing this class */
  public static void main(String argv[])
  {
    new AboutDialog(null).setVisible(true);
    
    // without this, the application seems not to terminate
    // after closing the dialog window
    System.exit(0);
  }

  //----------------------------------------------------------------------

}
