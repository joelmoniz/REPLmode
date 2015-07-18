package jm.mode.replmode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import processing.app.Base;
import processing.app.Mode;
import processing.app.Preferences;
import processing.app.RunnerListener;
import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.app.ui.EditorFooter;
import processing.app.ui.EditorState;
import processing.mode.java.JavaBuild;
import processing.mode.java.JavaEditor;
import processing.mode.java.runner.Runner;

/**
 * Handles the editor window including tool bar and menu. Has
 * access to the Sketch. Primarily used to add in the REPL toggle
 * button and to display the REPL pane when the toggle button is 
 * pressed.
 */

public class REPLEditor extends JavaEditor {

  private static final long serialVersionUID = 5121439110477282724L;

  /**
   * Panel with card layout which contains the p5 console and REPL panes
   */
  protected JPanel consoleREPLPane;

  /**
   * REPL/Console Pane
   */
  protected REPLConsolePane replConsole;

  /**
   * Temporary dummy sketch used by the REPL Mode's Console to store, compile
   * and run the code typed in by the user 
   */
  protected Sketch replTempSketch;

  protected File untitledFolderLocation;

  Runner replRuntime;
  REPLRunner runtime;

  /**
   * The folder containing the intermediate .java files obtained by 
   * pre-processing the dummy .pde file created by the REPL Console
   */
  File replSrcFolder;

  /**
   * The folder containing the pre-processed .java (created by the REPL 
   * Console) files in their compiled .class form
   */
  File replBinFolder;

  REPLMode replMode;

  protected REPLEditor(Base base, String path, EditorState state, Mode mode) {
    super(base, path, state, mode);

    replMode = (REPLMode) mode;
    replRuntime = null;
    runtime = null;

    try {
      untitledFolderLocation = Base.createTempFolder("untitled", "repl", null);

      (new File(untitledFolderLocation, sketch.getFolder().getName())).mkdirs();
      File subdir = new File(untitledFolderLocation, sketch.getFolder()
          .getName());

      final File tempFile = new File(subdir, subdir.getName() + ".pde");
      //File.createTempFile("tmp", ".pde", subdir);
      tempFile.createNewFile();
      replTempSketch = new Sketch(tempFile.getAbsolutePath(), this);
      
      replSrcFolder = replTempSketch.makeTempFolder();
      replBinFolder = replTempSketch.makeTempFolder();
      
      /*
       * This is needed to add back the document listeners and make the editor
       * show the correct code, since otherwise the line creating a new sketch
       * for replTempSketch make the PDE think that it has switched to a new 
       * editor
       */
      this.sketch.reload();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Method to add a footer at the base of the editor with tabs to display the
   * Console, Errors pane and the REPL Console.
   */
  @Override
  public EditorFooter createFooter() {
    replConsole = new REPLConsolePane(this);

    EditorFooter footer = super.createFooter();
    footer.addPanel("REPL", replConsole);

    replConsole.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(ComponentEvent e) {
        replConsole.requestFocus();
      }
    });

    return footer;
  }
  
  //---------------------------------------------------------------------------

  /**
   * Similar to prepareInitialRun() method, but with a few minor modifications
   * to suit the running of an REPL Sketch (such as no longer closing the 
   * window, not requiring to save, etc. 
   * @param replCode The code to be run
   */
  protected void prepareInitialREPLRun(String replCode) {
    // We no longer want the window to close
//    handleREPLStop();
//    internalCloseRunner();
    statusEmpty();

    // do this to advance/clear the terminal window / dos prompt / etc
    for (int i = 0; i < 10; i++)
      System.out.println("");

    // clear the console on each run, unless the user doesn't want to
    if (Preferences.getBoolean("console.auto_clear")) {
      console.clear();
    }

    replTempSketch.getCurrentCode().setProgram(replCode);
  }

  /**
   * Handles the running of the REPL Console's "dummy" sketch
   * @param code The code to be run
   * @param refresh Whether the updated code can simply be hot swapped in (if
   * false), or whether the sketch window needs to be closed and re-opened
   * (if true)
   */
  public void handleREPLRun(String code, boolean refresh) {
    new Thread(new Runnable() {
      public void run() {
        // TODO: Check how this is to be called, and where
        prepareInitialREPLRun(code);
        try {
          replRuntime = handleREPLLaunch(replTempSketch, 
                                         REPLEditor.this, refresh);
        } catch (Exception e) {
          replConsole.getCommandPromptPane().handleException(e);
//          No longer needed, since window doesn't close
//          replConsole.getCommandPromptPane().runTempSketch(false, false);
        }
      }
    }).start();
  }

  /**
   * 
   * @return Returns the NavigationFilter associated with this 
   * REPLEditor's REPLConsolePane
   */
  public CommandPromptPane getCommandPromptPane() {
    return replConsole.getCommandPromptPane();
  }

  /** Handles the standard Java "Run" or "Present" */
  public REPLRunner handleREPLLaunch(Sketch sketch, RunnerListener listener,
                             final boolean refresh) throws SketchException {
    JavaBuild build = new JavaBuild(sketch);
    String appletClassName = build.build(replSrcFolder, replBinFolder, false);
    if (appletClassName != null) {
      if (runtime == null || refresh || runtime.isFailedLoad()) {
//        System.out.println("VM status at start: " + (runtime.vm() == null));
        handleREPLStop();
        runtime = new REPLRunner(build, listener);
      }
      new Thread(new Runnable() {
        public void run() {
          runtime.launchREPL(refresh); // this blocks until finished
        }
      }).start();
   /*   else {
        new Thread(new Runnable() {
          public void run() {
            runtime.recompileREPL(); // this blocks until finished
  //          replConsole.requestFocus();
          }
        }).start();
      }*/
      return runtime;
    }
    return null;
  }

  /**
   * Event handler called when hitting the stop button. Stops a running debug
   * session or performs standard stop action if not currently debugging.
   */
  public void handleREPLStop() {

    try {
      if (replRuntime != null) {
        replRuntime.close(); // kills the window
        replRuntime = null;
      }
    } catch (Exception e) {
      statusError(e);
    }
  }
  
  /**
   * Basically prepareRun(), but without the internalCloseRunner() call
   */
  private void customPrepareRun() {
    statusEmpty();

    // do this to advance/clear the terminal window / dos prompt / etc
    for (int i = 0; i < 10; i++) System.out.println();

    // clear the console on each run, unless the user doesn't want to
    if (Preferences.getBoolean("console.auto_clear")) {
      console.clear();
    }

    // make sure the user didn't hide the sketch folder
    sketch.ensureExistence();

    // make sure any edits have been stored
    //current.setProgram(editor.getText());
    sketch.getCurrentCode().setProgram(getText());
  }

  /**
   * Used to, in addition to the function that the handleSave() method normally
   * performs, recompile the sketch code, so that the hot swapper kicks in and
   * the contents of the sketch window get updated accordingly.
   */
  @Override
  public boolean handleSave(boolean immediately) {
    boolean res = super.handleSave(immediately);
    
    if (replMode.srcFolder != null && replMode.binFolder != null) {
      customPrepareRun();
//      handleRun();
      JavaBuild build = new JavaBuild(sketch);
      try {
        build.build(replMode.srcFolder, replMode.binFolder, true);
      } catch (SketchException e) {
        e.printStackTrace();
      }
    }
    
    return res;
  };

  /**
   * Now not only close the PDE's sketch window, but close the sketch window
   * associated with the REPL Mode's Console too
   */
  @Override
  public void internalCloseRunner() {
    super.internalCloseRunner();
    handleREPLStop();
  }

  /**
   * Sets this Editor's code
   */
  @Override
  public void setCode(SketchCode code) {
    super.setCode(code);
  }
  
  //---------------------------------------------------------------------------
  
  /**
   * In addition to the normal Help menu options, add in a few extra options
   * for the REPL Mode too.
   */
  @Override
  public JMenu buildHelpMenu() {
    JMenu replHelpMenu = super.buildHelpMenu();
    JMenuItem item = null;
    
    replHelpMenu.addSeparator();
    
    item = new JMenuItem("REPL Mode");
    item.setEnabled(false);
    replHelpMenu.add(item);

    item = new JMenuItem("Getting started");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        REPLWelcomeDialog.showHelp();
      }
    });
    replHelpMenu.add(item);

    item = new JMenuItem("REPL Mode- A Guide");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showReferenceFile(replMode.getREPLReference());
      }
    });
    replHelpMenu.add(item);

    item = new JMenuItem("Report a bug");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Base.openURL("https://github.com/joelmoniz/REPLmode/issues/new");
      }
    });
    replHelpMenu.add(item);
    
    return replHelpMenu;
  }

}
