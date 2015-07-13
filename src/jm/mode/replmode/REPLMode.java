package jm.mode.replmode;

import java.io.File;

import processing.app.Base;
import processing.app.ui.Editor;
import processing.app.ui.EditorState;
import processing.app.Mode;
import processing.app.RunnerListener;
import processing.app.Sketch;
import processing.app.SketchException;
import processing.mode.java.JavaBuild;
import processing.mode.java.JavaMode;
import processing.mode.java.runner.Runner;

/**
 * REPL Mode for Processing.
 * 
 */
public class REPLMode extends JavaMode {
  
  File srcFolder;
  File binFolder;
  
  boolean isRunning;
  
  public static boolean firstEditorShown;
  
  public REPLMode(Base base, File folder) {
    super(base, folder);      
    
    /*
     * Do this to use the JavaMode's examples, libraries and reference, since
     * all of them are perfectly applicable to the REPL Mode
     */
    File javamodeFolder = Base.getContentFile("modes/java");
    examplesFolder = new File(javamodeFolder, "examples");
    librariesFolder = new File(javamodeFolder, "libraries");
    referenceFolder = new File(javamodeFolder, "reference");
    
    srcFolder = null;
    binFolder = null;        
    isRunning = false;
    firstEditorShown = false;
  }

  /**
   * Return the pretty/printable/menu name for this mode. This is separate from
   * the single word name of the folder that contains this mode. It could even
   * have spaces, though that might result in sheer madness or total mayhem.
   */
  @Override
  public String getTitle() {
    return "REPL Mode";
  }

  /**
   * Create a new editor associated with this mode.
   */

  @Override
  public Editor createEditor(Base base, String path, EditorState state) {
    /*
     * Teensy little hack to show the welcome screen when the first time an
     * Editor is created, but not after that. Required since an REPLMode object
     * is created right at startup time (even if another mode is the active
     * one), and this method is called each time a new window is opened.
     */
    if (!firstEditorShown) {
      REPLWelcomeDialog.showWelcome();
      firstEditorShown = true;
    }
    return new REPLEditor(base, path, state, this);
  }

  /**
   * Returns the default extension for this editor setup.
   */
  /*
   * @Override public String getDefaultExtension() { return null; }
   */

  /**
   * Returns a String[] array of proper extensions.
   */
  @Override
  public String[] getExtensions() {
    return new String[] { "pde", "java", "repl" };
  }

  /**
   * Get array of file/directory names that needn't be copied during "Save As".
   */
  /*
   * @Override public String[] getIgnorable() { return null; }
   */

  /**
   * Retrieve the ClassLoader for JavaMode. This is used by Compiler to load ECJ
   * classes. Thanks to Ben Fry.
   * 
   * @return the class loader from java mode
   */
  @Override
  public ClassLoader getClassLoader() {
    for (Mode m : base.getModeList()) {
      if (m.getClass().getName().equals(JavaMode.class.getName())) {
//				JavaMode jMode = (JavaMode) m;
        return m.getClassLoader();
      }
    }
    return null; // badness
  }
  
  /** Handles the standard Java "Run" or "Present" */
  @Override
  public Runner handleLaunch(Sketch sketch, RunnerListener listener,
                             final boolean present) throws SketchException {
    
    if (srcFolder == null) {
      srcFolder = this.base.getActiveEditor().getSketch().makeTempFolder();
      System.out.println(srcFolder.getAbsolutePath());
    }
    
    if (binFolder == null) {
      binFolder = this.base.getActiveEditor().getSketch().makeTempFolder();
      System.out.println(binFolder.getAbsolutePath());
    }
    JavaBuild build = new JavaBuild(sketch);
//    String appletClassName = build.build(false);
    String appletClassName = build.build(srcFolder, binFolder, true);
    if (appletClassName != null) {
      final REPLRunner runtime = new REPLRunner(build, listener);
      new Thread(new Runnable() {
        public void run() {
          isRunning = true;
          runtime.launch(present);  // this blocks until finished
          isRunning = false;
        }
      }).start();
      return runtime;
    }
    return null;
  }

  // @Override
  // public Editor createEditor(Base base, String path, EditorState state) {
  // return new REPLEditor(base, path, state, this);
  // }

}
