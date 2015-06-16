package jm.mode.replmode;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import processing.app.Base;
import processing.app.EditorFooter;
import processing.app.EditorState;
import processing.app.Mode;
import processing.app.Preferences;
import processing.app.RunnerListener;
import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.mode.java.JavaBuild;
import processing.mode.java.JavaEditor;
import processing.mode.java.runner.Runner;

/**
 * Main View Class. Handles the editor window including tool bar and menu. Has
 * access to the Sketch. Primarily used to display the REPL/Console toggle
 * buttons and to display the console/REPL pane appropriately. Adapted from
 * DebugEditor class of processing-experimental.
 * 
 * @author Martin Leopold <m@martinleopold.com>
 * @author Manindra Moharana &lt;me@mkmoharana.com&gt;
 * @author Joel Ruben Antony Moniz
 * 
 */

@SuppressWarnings("serial")
public class REPLEditor extends JavaEditor {

  /**
   * Panel with card layout which contains the p5 console and REPL panes
   */
  protected JPanel consoleREPLPane;

  /**
   * REPL/Console Pane
   */
  protected REPLConsolePane replConsole;

  protected Sketch replTempSketch;

  protected File untitledFolderLocation;

  Runner replRuntime;

  REPLMode replMode;

  protected REPLEditor(Base base, String path, EditorState state, Mode mode) {
    super(base, path, state, mode);

    replMode = (REPLMode) mode;

    try {
      untitledFolderLocation = Base.createTempFolder("untitled", "repl", null);

      (new File(untitledFolderLocation, sketch.getFolder().getName())).mkdirs();
      File subdir = new File(untitledFolderLocation, sketch.getFolder()
          .getName());

      final File tempFile = new File(subdir, subdir.getName() + ".pde");//File.createTempFile("tmp", ".pde", subdir);
      tempFile.createNewFile();
      replTempSketch = new Sketch(tempFile.getAbsolutePath(), this);

      // These few lines are needed to added back the document listeners, since otherwise
      // the line creating a new sketch for replTempSketch messes with the main sketch's
      // document listeners- the PDE thinks that it has switched to a new editor, so
      // the document listeners get added there
      for (final SketchCode sc : REPLEditor.this.getSketch().getCode()) {
        setCode(sc);
      }

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

  protected void prepareInitialREPLRun(String replCode) {
    handleREPLStop();
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

  public void handleREPLRun(String code) {
    new Thread(new Runnable() {
      public void run() {
        // TODO: Check how this is to be called, and where
        prepareInitialREPLRun(code);
        try {
          replRuntime = handleREPLRun(replTempSketch, REPLEditor.this);
        } catch (Exception e) {
          replConsole.getCommandPromptPane().printStatusException(e);
          replConsole.getCommandPromptPane().undoLastStatement();
          replConsole.getCommandPromptPane().runTempSketch(false);
        }
      }
    }).start();
  }

  public Runner handleREPLRun(Sketch sketch, RunnerListener listener)
      throws SketchException {
    return handleLaunch(sketch, listener, false);
  }

  /** Handles the standard Java "Run" or "Present" */
  public Runner handleLaunch(Sketch sketch, RunnerListener listener,
                             final boolean present) throws SketchException {
    JavaBuild build = new JavaBuild(sketch);
    String appletClassName = build.build(false);
    if (appletClassName != null) {
      final Runner runtime = new Runner(build, listener);
      new Thread(new Runnable() {
        public void run() {
          runtime.launch(present); // this blocks until finished
//          replConsole.requestFocus();
        }
      }).start();
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

  @Override
  public void internalCloseRunner() {
    super.internalCloseRunner();
    handleREPLStop();
  }

  @Override
  protected void setCode(SketchCode code) {
    super.setCode(code);
  }

}
