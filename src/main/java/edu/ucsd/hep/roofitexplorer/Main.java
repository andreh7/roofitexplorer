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
package edu.ucsd.hep.roofitexplorer;

import edu.ucsd.hep.roofitexplorer.datatypes.GenericWorkspaceMember;
import edu.ucsd.hep.roofitexplorer.filters.MemberFilterFactoryList;
import edu.ucsd.hep.roofitexplorer.view.graph.GraphMaker;
import edu.ucsd.hep.roofitexplorer.view.graph.GraphPanel;
import edu.ucsd.hep.roofitexplorer.view.graph.GraphPanelAdapter;
import edu.ucsd.hep.roofitexplorer.view.misc.AboutDialog;
import edu.ucsd.hep.roofitexplorer.view.misc.BrowserPanel;
import edu.ucsd.hep.roofitexplorer.view.misc.WorkspaceMemberListWithFilterPanel;
import edu.ucsd.hep.rootrunnerutil.PipeCommandRunnerListener;
import edu.ucsd.hep.rootrunnerutil.ROOTRunner;
import edu.ucsd.hep.rootrunnerutil.ROOTRunnerImpl;
import edu.ucsd.hep.rootrunnerutil.view.CommandDisplayPanel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Main class for RooFitExplorer
 *
 */
public class Main 
{
  //----------------------------------------------------------------------
  private UserProfileData userProfileData;

  private CmdLineArgs options = new CmdLineArgs();

  /** the desktop pane */
  private JDesktopPane desktop;
  
  /** variable name used to assign the opened input file */
  private final String inputFileVariableName = "fin";
  
  private final static Main instance = new Main();
  
  //----------------------------------------------------------------------

  public static void main(String[] args) throws IOException, CmdLineException, ClassNotFoundException
  {
    // enable assertions
    ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

    // set dock icon name for OSX
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "rooFitExplorer"/* + fname + ":" + workspaceName */);
    
    // make the menu bar appear at the top of the screen on OSX
    // (see http://stackoverflow.com/questions/8955638/how-do-i-move-my-jmenubar-to-the-screen-menu-bar-on-mac-os-x)
    System.setProperty("apple.laf.useScreenMenuBar","true");
    
    instance.run(args);
  }
  
  //----------------------------------------------------------------------

  private static void usage(CmdLineParser parser, Exception ex)
  {
    if (ex != null)
      System.err.println(ex.getMessage());

    System.err.println();
    System.err.println("usage: rooFitExplorer.jar [ filename.root [ workspacename ] ]");
  
    parser.printUsage(System.err);
    System.err.println();

    // System.err.println("  Example: CMSEgammaHLTDQMMultiComparison " + parser.printExample(ALL));

    System.exit(1);
  }
  private WorkspaceMemberSelectionListener nodeSelectedCallbackForGraph;
  private WorkspaceData ws = null;
  private JFileChooser openRootFileChooser;
  
  private ROOTRunner root_runner;
  
  //----------------------------------------------------------------------

  private void processCommandLineOptions(String[] argv) throws IOException, CmdLineException
  {
    CmdLineParser parser = new CmdLineParser(options);
    try
    {
      parser.parseArgument(argv);
    } catch (CmdLineException ex)
    {
      // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
      usage(parser, ex);
    }

    //----------------------------------------
    // check consistency of options
    //----------------------------------------
  }
  
  //----------------------------------------------------------------------
  
  private void run(String[] args) throws IOException, CmdLineException, ClassNotFoundException
  {
    checkOperatingSystem();
    checkROOTexecutable();
    
    processCommandLineOptions(args);
      
    // check whether we should just list/show the known profiles and exit
    if (options.showProfiles)
    {
      System.err.println("known profiles:");
      for (String profileName : UserProfileManager.getKnownProfiles())
        System.err.println("  " + profileName);
      System.exit(0);
    }
    
    
    // get the selected user profile
    // this will throw an exception if the profile does not exist
    // (and is not the default profile)
    userProfileData = UserProfileManager.getProfile(options.profileName);
    
    // perform some checks:
    //  - check that that the specified ROOT installation actually exists
    try
    {
      openRootFile(options.rootInputFname, options.workspaceName);
    } catch (MemberVerboseDataParseError ex)
    {
      // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
      ex.printVerboseMessage(System.err);
      throw new Error(ex);

    }
  }

  //----------------------------------------------------------------------

  private void openRootFile(String fname, String workspaceName) throws IOException, MemberVerboseDataParseError, ClassNotFoundException
  {
    // check arguments given:
    //
    //   - if fname is null, open a dialog to let the user choose 
    //     a ROOT file
    //
    //   - if workspaceName is null, try to find all instances
    //     of RooWorkspace in the input file and let the user
    //     choose select one
    if (fname == null)
    {
      if (openRootFileChooser == null)
      {
        openRootFileChooser = new JFileChooser();
        
        // note that it is important to specify the extension
        // as 'root' and not as '.root'
        openRootFileChooser.setFileFilter(new FileNameExtensionFilter("ROOT files","root"));
      }
      
      int res = openRootFileChooser.showOpenDialog(desktop);
      if (res != JFileChooser.APPROVE_OPTION)
        return;
      
      fname = openRootFileChooser.getSelectedFile().getAbsolutePath();
    }
    
    //--------------------
    // TODO: check if file exists and is readable
    
    
    //--------------------
    
    // start a ROOT process to look at the file
    PipeCommandRunnerListener windowListener = null;
    if (options.showRootTerminal)
    {
      CommandDisplayPanel panel = new CommandDisplayPanel(null);
      panel.makeFrame().setVisible(true);
      windowListener = panel;
    }
    {
      ROOTRunnerImpl rootRunnerImpl = new ROOTRunnerImpl(
                           windowListener,null,
                     
                     // add a newline to avoid merging the root command with previous commands
                     this.userProfileData.getPreRootShellCommands() + "\n",
                     null,
                     this.userProfileData.getRootCmd());
      root_runner = rootRunnerImpl;  
      
    // set some parameters
    rootRunnerImpl.setTransferRemoteFileToLocalCmd(userProfileData.getTransferRemoteFileToLocalCmd());
    rootRunnerImpl.setCreateTempFileCmd(userProfileData.getCreateTempFileCmd());
    }
    
    // disable special ROOT signal handlers
    root_runner.writeLine("gSystem->ResetSignals();");
    
    // first load the libraries specified in the profile
    for (String libraryToLoad : userProfileData.getStartupLibs())
    {
      root_runner.writeLine(".L " + libraryToLoad);
    }
    
    // if loading of external shared libraries was requested on the command
    // line, do it now
    for (String libraryToLoad : options.librariesToLoad)
    {
      root_runner.writeLine(".L " + libraryToLoad);
    }
    
    // root_runner.addCommandPipeListener(new StreamPrinterCommandListener());
    
    root_runner.writeLine("TFile *" + this.inputFileVariableName + " = new TFile(\"" + fname + "\");");
    root_runner.waitForCompletion();
    
    ws = null;
    
    if (fname.toLowerCase().endsWith((".root")))
    {
      // check whether the workspaceName was specified or not
      if (workspaceName == null)
      {
        workspaceName = userSelectWorkspace(root_runner);
                
        // no workspace selected or no workspace present
        if (workspaceName == null)
          return;
      }
      
      // read a ROOT file
      GenericWorkspaceDataReader reader = GenericWorkspaceDataReader.makeReader(root_runner, fname, workspaceName);

      ws = reader.getWorkspace();
    }
    else if (fname.endsWith(".xgz"))
      // assume gzipped xml file
      ws = WorkspaceData.readFromGzippedXMLfile(fname);
    else if (fname.endsWith(".xstream"))
      ws = WorkspaceData.readFromBinaryfile(fname);
    else if (fname.endsWith(".obj"))
      ws = WorkspaceData.readJavaSerializationfile(fname);
    else if (fname.endsWith(".xml"))
        ws = WorkspaceData.readFromXMLfile(fname);
    else
    {
      System.err.println("unknown extension in the input file name '" + fname +"', exiting.");
      System.exit(1);
    }

    System.out.println("done reading input file");
    
    //----------
    // serialize to a file if requested to do so
    //----------

    if (options.saveFileName != null)
    {
      if (options.saveFileName.endsWith(".xstream"))
      {
       System.err.println("saving workspace to xstream file " + options.saveFileName);
        ws.writeXMLToBinaryFile(options.saveFileName);
      } else if (options.saveFileName.endsWith(".xgz"))
      {
        // gzipped xml file
        System.err.println("saving workspace to gzipped xml file " + options.saveFileName);
        ws.writeXMLToGzippedFile(options.saveFileName);
      } else if (options.saveFileName.endsWith(".obj"))
      {
        System.err.println("saving workspace to java serialization file " + options.saveFileName);
        ws.writeToJavaSerializationFile(options.saveFileName);
      }else if  (options.saveFileName.endsWith(".xml"))
      { 
        // write xml uncompressed
        System.err.println("saving workspace to xml file " + options.saveFileName);
        ws.writeXMLToFile(options.saveFileName);
      } else
      {
        System.err.println("unknown extension in the output file name '" + options.saveFileName +"', exiting.");
        System.exit(1);
      }
      
      // exit now
      System.exit(0);
    }    
    //----------

      // print top nodes
    // System.out.println("top nodes=" + ws.getMembers().filter(new TopLevelMembersFilter()));
    
      
    //----------
    // produce the windows
    //----------
    
    final MainWindow mainWindow = new MainWindow("rooFitExplorer (" + fname + ":" + workspaceName + ")");
    
    mainWindow.setJMenuBar(this.makeTopMenuBar(mainWindow));
    
    desktop = new JDesktopPane();
    mainWindow.setContentPane(desktop);
    mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainWindow.pack();
    mainWindow.setSize(500,500);
    
    mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
    mainWindow.setSize(Toolkit.getDefaultToolkit().getScreenSize());
    
   //----------
 
    
    // older version, probably inconvenient for
    // large number of objects
    if (false)
    {
      BrowserPanel panel = new BrowserPanel(ws);
      JFrame frame = panel.makeFrame();
    
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
    }
    //----------
    // produce a callback to update the graphs
    nodeSelectedCallbackForGraph = new WorkspaceMemberSelectionListener(){

      public void workspaceMemberSelected(GenericWorkspaceMember member)
      {
        showMemberInGraph(member);
      }

      
    };
    
    //----------
    // newer version with separate windows
    if (true)
    {      
      // panel for displaying the graphs
      newGraphPanel();

      // another frame to select the workspace members
      newListPanel();
     
    }
    
    mainWindow.setVisible(true);
    
  }

  //----------------------------------------------------------------------
  private JMenuBar makeTopMenuBar(final MainWindow frame)
  {
    JMenuBar menuBar = new JMenuBar();

    JMenuItem menuItem;
    //----------
    JMenu menu = new JMenu("New");
    menuItem = new JMenuItem("Graph panel");
    menuItem.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent ae)
      {
        newGraphPanel();
      }

    });
    
    menu.add(menuItem);
    //-----
    // make a new list with ALL members of the workspace
    menuItem = new JMenuItem("List panel");

    menuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        // TODO: add file and workspace name here
        // TODO: watch out for empty workspaces...
        newListPanel();
      }
    });
    
    menu.add(menuItem);
    

    // add an 'About' menu item here
    // (TODO: should move to a different menu)
    // but only if we're not on OSX
    if (! OSXtools.isOSX())
    {
      menu.addSeparator();

      menuItem = new JMenuItem("About...");

      menuItem.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent ae)
        {
          new AboutDialog(frame).setVisible(true);
        }
      });

      menu.add(menuItem);
    }

    //-----
    menuBar.add(menu);
    
    
    return menuBar;

  }
  
  //----------------------------------------------------------------------

  private final List<GraphPanel> graphPanels = new ArrayList<GraphPanel>();

  /** opens a new graph panel and makes it the 'current' one 
   *  in which the current graphs are displayed */
  public GraphPanel newGraphPanel()
  {
      final GraphPanel graphPanel = new GraphPanel();
      JInternalFrame iframe = graphPanel.makeInternalFrame("graph");
      iframe.setVisible(true);
      desktop.add(iframe);
      
      
      //-----
      // object for generating a new graph and updating the GraphPanel
      // when a workspace member is selected
      GraphPanelAdapter gpa = new GraphPanelAdapter(ws, graphPanel);

      //-----
      // add the graph panel itself (useful when clicking on vertices)
      graphPanel.addMemberSelectionListener(gpa);
  
      graphPanels.add(graphPanel);
            
      graphPanel.getOwningInternalFrame().addInternalFrameListener(new InternalFrameAdapter()
      {
        @Override
        public void internalFrameActivated(InternalFrameEvent ife)
        {
          // move this panel to the 'end' of the list
          // so we will use it next time to display a graph
          synchronized (graphPanels)
          {
            graphPanels.remove(graphPanel);
            graphPanels.add(graphPanel);
          }
        }
      });
      
    return graphPanel;
  }
  
  //----------------------------------------------------------------------
  
  /** this is called when a new member should be shown in the most
   *  recent graph window 
   */
  private void showMemberInGraph(GenericWorkspaceMember member)
  {
    // go through the list of graphs
    // TODO: we should remove a graph when the corresponding window
    // was closed

    // TODO: take the one which most recently has gotten the focus / 
    // been selected
    
    synchronized (this.graphPanels)
    {
      for (int i = this.graphPanels.size() - 1; i >= 0; --i)
      {
        GraphPanel graphPanel = this.graphPanels.get(i); 
        if (graphPanel.isShowing())
        {
          // TODO: this should become a field, set whenever the workspace
          // is set
          GraphMaker graphMaker = new GraphMaker(ws);

          // create a graph for the selected member
          graphPanel.setGraph(graphMaker.makeSingleNodeGraph(member), member.getVarName());
          return;
        }
      }
    }
    
    
  
  }

  //----------------------------------------------------------------------

  private void newListPanel()
  {
    //-----

    // SimpleWorkspaceMemberListPanel listPanel = new SimpleWorkspaceMemberListPanel();
    WorkspaceMemberListWithFilterPanel listPanel = new WorkspaceMemberListWithFilterPanel(root_runner, ws.getName());
    
    listPanel.setFilterFactories(MemberFilterFactoryList.makeStandardFilterFactory());
    
    listPanel.setMembers(ws.getMembers());
    listPanel.addMemberSelectionListener(this.nodeSelectedCallbackForGraph);
    JInternalFrame iframe = listPanel.makeInternalFrame("member list");
    desktop.add(iframe);
    iframe.setVisible(true);
  }

  //----------------------------------------------------------------------

  /** given the ROOT session (assuming that there is an open file)
   *  looks through the ROOT file for instances of RooWorkspace and
   *  asks the user to select a workspace. 
   
      @return the name of the selected workspace or null if the user
              canceled the selection or no workspace was found
              in the file.
   */
  private String userSelectWorkspace(ROOTRunner root_runner) throws IOException
  {
    ROOTObjectsFinder finder = new ROOTObjectsFinder(root_runner, this.inputFileVariableName);
    List<String> workspaceNames = finder.findInstancesOf("RooWorkspace");
    
    // assumes that there are no funny characters in the keys, such
    // as newlines
    
    if (workspaceNames.isEmpty())
    {
      JOptionPane.showMessageDialog(desktop, "no instances of RooWorkspace found");
      return null;
    }
    
    // if exactly one workspace was found, take this one
    if (workspaceNames.size() == 1)
      return workspaceNames.get(0);
    
    // show a dialog with the workspaces we found
    String selectedWorkspace = (String) 
            JOptionPane.showInputDialog(null, "Please select a workspace", "Workspaces",
            JOptionPane.INFORMATION_MESSAGE, null,
            workspaceNames.toArray(), // values to select from
            workspaceNames.get(0)     // default choice
            );
    
    System.out.println("selection was: " + selectedWorkspace);
  
    return selectedWorkspace;
  }
 
  //----------------------------------------------------------------------

  /** checks whether this is a known operating system 
   *  and shows a dialog box if it is one where this software
   *  was not tested */
  private void checkOperatingSystem()
  {
    String osname = System.getProperty("os.name");
    if ("Mac OS X".equals(osname))
      return;
    if ("Linux".equals(osname))
      return;
    
    JOptionPane.showMessageDialog(null, "Warning: this software was not tested on this platform (" + osname + ")");
  }

  //----------------------------------------------------------------------

  /** runs the given command, waits for completion and returns its exit status */
  public static int getCommandExitStatus(String cmd) throws IOException
  {
    CommandLine parsedCmd = CommandLine.parse(cmd);
    DefaultExecutor exec = new DefaultExecutor();

    // hack to disable printing of stdout on this process' stdout
    ByteArrayOutputStream devNull = new ByteArrayOutputStream();
    exec.setStreamHandler(new PumpStreamHandler(devNull));
    
    try
    {
      // note that this will throw an ExecuteException if the exit
      // value is not what is expected (which is zero)

      int res = exec.execute(parsedCmd);
      return res;
    } catch (ExecuteException ex)
    {
      return ex.getExitValue();
    }
  }
  
  //----------------------------------------------------------------------

  /** checks whether 'root' is a command which is known to the (unix) shell */
  private void checkROOTexecutable()
  {
    try
    {
      // this only works on Unix like systems
      if (getCommandExitStatus("which root") == 0)
        return;

    } catch (IOException ex)
    {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }

    JOptionPane.showMessageDialog(null, "can't find a root executable");
    System.exit(1);
   
  }
 
  //----------------------------------------------------------------------

  public static Main getInstance()
  {
    return instance;
  }
  
  //----------------------------------------------------------------------

}
