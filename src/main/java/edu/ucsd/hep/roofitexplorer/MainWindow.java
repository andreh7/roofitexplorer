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
package edu.ucsd.hep.roofitexplorer;

import edu.ucsd.hep.roofitexplorer.view.misc.AboutDialog;
import java.awt.HeadlessException;
import javax.swing.JFrame;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;
import org.simplericity.macify.eawt.DefaultApplication;


/**
 * A main window which uses macify
 * @author holzner
 */
public class MainWindow extends JFrame implements ApplicationListener
{

  public MainWindow(String string) throws HeadlessException
  {
    super(string);
    
    Application application = new DefaultApplication();
    application.addApplicationListener(this);
  }

  public void handleAbout(ApplicationEvent ae)
  {
    ae.setHandled(true);
    new AboutDialog(this).setVisible(true);
  }

  public void handleOpenApplication(ApplicationEvent ae)
  {
    // throw new UnsupportedOperationException("Not supported yet.");
  }

  public void handleOpenFile(ApplicationEvent ae)
  {
    // throw new UnsupportedOperationException("Not supported yet.");
  }

  public void handlePreferences(ApplicationEvent ae)
  {
    // throw new UnsupportedOperationException("Not supported yet.");
  }

  public void handlePrintFile(ApplicationEvent ae)
  {
    // throw new UnsupportedOperationException("Not supported yet.");
  }

  public void handleQuit(ApplicationEvent ae)
  {
    // programmatically close the main frame
    this.dispose();
    System.exit(0);
  }

  public void handleReOpenApplication(ApplicationEvent ae)
  {
    // throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
