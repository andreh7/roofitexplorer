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

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import edu.ucsd.hep.roofitexplorer.view.misc.AboutDialog;
import javax.swing.JFrame;

/**
 * code specific to OSX (which uses classes which are not available
 * on other platforms)
 * @author holzner
 */
public class OSXtools
{

  public static boolean isOSX()
  {
    return "Darwin".equals(System.getProperty("os.name"));
  }
  
  static void addAboutHandler(final JFrame mainWindow)
  {
    if (! isOSX())
      return;      
    
    // TODO: what happens when this runs on Linux ?
    Application osxApp = Application.getApplication();
    osxApp.setAboutHandler(new AboutHandler() 
    {
      public void handleAbout(AppEvent.AboutEvent ae)
      {
        new AboutDialog(mainWindow).setVisible(true);
      }
    });  
  
  }

}
