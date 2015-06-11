package jm.mode.replmode;

import java.awt.CardLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.event.DocumentListener;

import processing.app.Base;
import processing.app.EditorFooter;
import processing.app.EditorState;
import processing.app.Mode;
import processing.app.Preferences;
import processing.app.RunnerListener;
import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.SketchException;
import processing.app.syntax.SyntaxDocument;
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
	 * Show Console button
	 */
	protected REPLConsoleToggle btnShowConsole;

	/**
	 * Show REPL button
	 */
	protected REPLConsoleToggle btnShowREPL;

	/**
	 * REPL/Console Pane
	 */
	protected REPLConsolePane replConsole;

	/**
	 * Clear REPL/Console panes button
	 */
	protected ConsoleButtons consoleOptions;
	
	protected Sketch replTempSketch;
	protected File untitledFolderLocation;
	
	Runner replRuntime;
	REPLMode replMode;

	protected REPLEditor(Base base, String path, EditorState state, Mode mode) {
		super(base, path, state, mode);
		
		replMode = (REPLMode)mode;
		
		try {
      untitledFolderLocation = Base.createTempFolder("untitled", "repl", null);
      
      (new File(untitledFolderLocation, sketch.getFolder().getName())).mkdirs();
      File subdir = new File(untitledFolderLocation, sketch.getFolder().getName());
      
//      final String temp = path.substring(path.substring(0, path.lastIndexOf('\\'))
//      .lastIndexOf('\\'), path.lastIndexOf('\\')+1);
      final File tempFile = new File(subdir, subdir.getName() + ".pde");//File.createTempFile("tmp", ".pde", subdir);
      tempFile.createNewFile();
//      System.out.println(tempFile.getAbsolutePath());
//      System.out.println(sketch.getMainFilePath());
      replTempSketch = new Sketch(tempFile.getAbsolutePath(), this);
      
      // These few lines are needed to added back the document listeners, since otherwise
      // the line creating a new sketch for replTempSketch messes with the main sketch's
      // document listeners- the PDE thinks that it has switched to a new editor, so
      // the document listeners get added there
      for (final SketchCode sc : REPLEditor.this.getSketch().getCode()) {
        setCode(sc);
      }
      
//      Thread one = new Thread() {
//        public void run() {
//          try {
//            Thread.sleep(7000);
//            for (final SketchCode sc : REPLEditor.this.getSketch().getCode()) {
//              REPLEditor.this.setCode(sc);
//              int i=0;
//              for (DocumentListener dl : ((SyntaxDocument) sc.getDocument())
//                  .getDocumentListeners()) {
//                System.out.println((i++) + ":  " + dl.getClass().getName());
//              }
//            }
//            System.out.println("Here");
//            System.out.println(sketch.getFolder().getName());
//            System.out.println(replTempSketch.getFolder().getName());
//            System.out.println(tempFile.getAbsolutePath());
//            System.out.println(sketch.getCodeFolder().getAbsolutePath());
//            System.out.println(replTempSketch.getCodeFolder().getAbsolutePath());
//            System.out.println(sketch.getCodeCount());
//            System.out.println(replTempSketch.getCodeCount());
//            prepareInitialREPLRun();
//            handleREPLRun();
//            Thread.sleep(5000);
//            prepareInitialREPLRun2();
//            handleREPLRun();
//            
////            for (String f : replTempSketch.getCodeFolder().list()) {
////              System.out.println(f);
////            }
//          } catch (InterruptedException v) {
//            System.out.println(v);
//          } catch (NullPointerException v) {
//            v.printStackTrace();
//          }
//        }
//      };
//      one.start();
    } catch (IOException e) {
      e.printStackTrace();
    }

//		replConsole = new REPLConsolePane(this);
//		addREPLConsoleUI();
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
	/*
	private void addREPLConsoleUI() {

		// Adding toggle console button
		consolePanel.remove(2);
		JPanel lineStatusPanel = new JPanel();
		lineStatusPanel.setLayout(new BorderLayout());
		btnShowConsole = new REPLConsoleToggle(this, REPLConsoleToggle.CONSOLE,
				lineStatus.getHeight());
		btnShowREPL = new REPLConsoleToggle(this, REPLConsoleToggle.REPL,
				lineStatus.getHeight());
		btnShowConsole.addMouseListener(btnShowConsole);
		btnShowREPL.addMouseListener(btnShowREPL);

		consoleOptions = new ConsoleButtons(ConsoleButtons.CLEAR,
				lineStatus.getHeight(), console, replConsole);
		consoleOptions.addMouseListener(consoleOptions);
		consoleOptions.addMouseMotionListener(consoleOptions);

		JPanel toggleButtonPanel = new JPanel();
		toggleButtonPanel.setLayout(new BoxLayout(toggleButtonPanel,
				BoxLayout.LINE_AXIS));

		toggleButtonPanel.add(consoleOptions);
		toggleButtonPanel.add(btnShowREPL);
		toggleButtonPanel.add(btnShowConsole);
		lineStatusPanel.add(toggleButtonPanel, BorderLayout.EAST);

		lineStatus.setBounds(0, 0, toggleButtonPanel.getX() - 1,
				toggleButtonPanel.getHeight());
		lineStatusPanel.add(lineStatus);
		consolePanel.add(lineStatusPanel, BorderLayout.SOUTH);
		lineStatusPanel.repaint();

		// Adding JPanel with CardLayout for Console/REPL Toggle
		consolePanel.remove(1);
		consoleREPLPane = new JPanel(new CardLayout());
		consoleREPLPane.add(replConsole, REPLConsoleToggle.REPL);
		consoleREPLPane.add(console, REPLConsoleToggle.CONSOLE);
		consolePanel.add(consoleREPLPane, BorderLayout.CENTER);

		showConsoleOrREPL(REPLConsoleToggle.CONSOLE);
	}
  */

	public void showConsoleOrREPL(String buttonName) {
		CardLayout cl = (CardLayout) consoleREPLPane.getLayout();
		cl.show(consoleREPLPane, buttonName);
		if (REPLConsoleToggle.REPL.equals(buttonName))
			replConsole.requestFocus();
	}
	
//	public void runREPL(String code) {
//    prepareInitialREPLRun(code);
//    handleREPLRun();
//	}

	/**
	 * Test method to prototype a prepare run method for the REPL Mode. 
	 * <br /><b>Warning:</b> Prototype test method only. Pointless 
	 * in the real world.
	 */
  public void prepareInitialREPLRun() {
    handleREPLStop();
//    internalCloseRunner();
    statusEmpty();

    // do this to advance/clear the terminal window / dos prompt / etc
    for (int i = 0; i < 10; i++) System.out.println();

    // clear the console on each run, unless the user doesn't want to
    if (Preferences.getBoolean("console.auto_clear")) {
      console.clear();
    }

    // make sure any edits have been stored
    //current.setProgram(editor.getText());
    String tempTestCode = "void setup() { size(200, 200);}  \nvoid draw() {rect(20, 20, 80, 80);}";
    replTempSketch.getCurrentCode().setProgram(tempTestCode);
  }
  
  public void prepareInitialREPLRun2() {
    handleREPLStop();
//    internalCloseRunner();
    statusEmpty();

    // do this to advance/clear the terminal window / dos prompt / etc
    for (int i = 0; i < 10; i++) System.out.println("");

    // clear the console on each run, unless the user doesn't want to
    if (Preferences.getBoolean("console.auto_clear")) {
      console.clear();
    }

    // make sure any edits have been stored
    //current.setProgram(editor.getText());
    String tempTestCode = "void setup() { size(200, 200);}  \nvoid draw() {rect(20, 20, 80, 80);rect(20, 20, 40, 120);}";
    replTempSketch.getCurrentCode().setProgram(tempTestCode);
  }
  
  protected void prepareInitialREPLRun(String replCode) {
    handleREPLStop();
//    internalCloseRunner();
    statusEmpty();

    // do this to advance/clear the terminal window / dos prompt / etc
    for (int i = 0; i < 10; i++) System.out.println("");

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
            statusError(e);
          }
        }
      }).start();
  }
  
  public Runner handleREPLRun(Sketch sketch,
                          RunnerListener listener) throws SketchException {
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
          runtime.launch(present);  // this blocks until finished
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
//      toolbar.activate(JavaToolbar.STOP);

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
