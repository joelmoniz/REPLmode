package jm.mode.replmode;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;

import processing.mode.java.AutoFormat;

/**
 * Class to store the valid commands the user has entered, and to perform 
 * operations related to them.
 * 
 * @author Joel Moniz
 *
 */
public class CommandList {
  /**
   * The command prompt pane
   */
  CommandPromptPane promptPane;

  /**
   * The list of valid commands the user has entered
   */
  ArrayList<String> commandList;

  /**
   * A list used as a "holding area" of sorts for a continuing command that
   * the user is in the process of entering  
   */
  ArrayList<String> continuingCommandList;
  
  /**
   * A list of libraries that the user would like to import
   */
  ArrayList<String> importsList;

  /**
   * Stack to store commands that the user wants undone. Can be used to
   * redo undos.
   */
  Stack<String> undoStack;

  /**
   * Boolean representing whether an undo operation (or a volley of 
   * undos and redos) is in progress.
   */
  boolean isUndoing;

  /**
   * An instance of processing's auto-formatter. Used to pretty up code before
   * appending it at the end of the current sketch when the user types
   * <code>print</code>
   */
  AutoFormat formatter;

  /**
   * An instance of the {@link Size} class
   */
  Size size;

  /**
   * Command word used to clear the REPL Console
   */
  public static final String CLEAR_COMMAND = "clear";

  /**
   * Command word used to (re)initialize the REPL Console
   */
  public static final String INIT_COMMAND = "init";

  /**
   * Command word used to resize the REPL Console's sketch window
   */
  public static final String RESIZE_COMMAND = "resize";

  /**
   * Command word used to undo the last/ a set of last few (non-command
   * word) action(s) done in the REPL Console
   */
  public static final String UNDO_COMMAND = "undo";

  /**
   * Command word used to redo the last/ a set of last few undo(s)
   */
  public static final String REDO_COMMAND = "redo";

  /**
   * Command word used to print the code responsible for generating the
   * current sketch output into a function in the active sketch
   */
  public static final String PRINT_COMMAND = "print";

  /**
   * Command word used to print a help menu 
   */
  public static final String HELP_COMMAND = "help";

  /**
   * Command word used to ???
   */
  public static final String MAN_COMMAND = "man";

  /**
   * Contains all the command words that can be used in the REPL Mode
   */
  public static final String[] REPL_COMMAND_SET = {
    CLEAR_COMMAND, INIT_COMMAND, RESIZE_COMMAND, UNDO_COMMAND, REDO_COMMAND,
    PRINT_COMMAND, HELP_COMMAND, MAN_COMMAND};

  public static final String SIZE_PD2 = "P2D";

  public static final String SIZE_PD3 = "P3D";

  public static final String SIZE_PDF = "PDF";

  /**
   * Contains all the renderers that can be provided as a third argument 
   * to the <code>size()</code> method
   */
  public static final String[] SIZE_RENDERERS = { SIZE_PD2, SIZE_PD3, SIZE_PDF };

  /**
   * Class representing the <code>size()</code> method, and used to store 
   * its parameters (namely width, height, and renderer to be used) 
   * @author Joel Moniz
   */
  class Size {

    private int w;
    private int h;
    private String renderer;

    private Size() {
      this.w = 100;
      this.h = 100;
    }

    private Size(int w, int h) {
      this.w = w;
      this.h = h;
    }

    private Size(int w, int h, String renderer) {
      this.w = w;
      this.h = h;

      if (Arrays.asList(SIZE_RENDERERS).contains(renderer.toUpperCase())) {
        this.renderer = renderer.toUpperCase();
      }
    }

    private String getSizeStatement() {
      StringBuilder s = new StringBuilder();
      s.append("size(");
      s.append(w);
      s.append(", ");
      s.append(h);

      if (renderer != null && !renderer.isEmpty()) {
        s.append(", ");
        s.append(renderer);
      }

      s.append(");");
      return s.toString();
    }
  }

  public CommandList(CommandPromptPane promptPane) {
    this.promptPane = promptPane;
    commandList = new ArrayList<>();
    continuingCommandList = new ArrayList<>();
    importsList = new ArrayList<>();
    undoStack = new Stack<>();
    isUndoing = false;
    formatter = new AutoFormat();
    size = null;
  }

  /**
   * Method to resize the REPL Console's sketch.
   * Sets the size to the default 100x100.
   */
  public void resize() {
    continuingCommandList.clear();
    size = new Size();
  }

  /**
   * Method to resize the REPL Console's sketch.
   * @param w The width of the sketch 
   * @param h The height of the sketch
   */
  public void resize(int w, int h) {
    continuingCommandList.clear();
    size = new Size(w, h);
  }

  /**
   * Method to resize the REPL Console's sketch.
   * 
   * @param w
   *          The width of the sketch
   * @param h
   *          The height of the sketch
   * @param renderer
   *          The renderer to be used. May take the value <code>P2D</code>,
   *          <code>P3D</code> or <code>PDF</code>
   */
  public void resize(int w, int h, String renderer) {
    continuingCommandList.clear();
    size = new Size(w, h, renderer);
  }

  /**
   * Method to initialize the REPL Console's sketch.
   * Sets the size to the default 100x100.
   */
  public void init() {
    commandList.clear();
    continuingCommandList.clear();
    clearUndoStack();
    size = new Size();
  }

  /**
   * Method to initialize the REPL Console's sketch.
   * @param w The width of the sketch 
   * @param h The height of the sketch
   */
  public void init(int w, int h) {
    commandList.clear();
    continuingCommandList.clear();
    clearUndoStack();
    size = new Size(w, h);
  }

  /**
   * Method to initialize the REPL Console's sketch.
   * 
   * @param w
   *          The width of the sketch
   * @param h
   *          The height of the sketch
   * @param renderer
   *          The renderer to be used. May take the value <code>P2D</code>,
   *          <code>P3D</code> or <code>PDF</code>
   */
  public void init(int w, int h, String renderer) {
    commandList.clear();
    continuingCommandList.clear();
    clearUndoStack();
    size = new Size(w, h, renderer);
  }

  /**
   * Add a statement (a line or block of code) to the command list. Assumes 
   * that the statement is valid, and already checked for errors.
   * @param stmt the statement the user just typed in
   * @return true if init has not yet been run
   */
  public boolean addStatement(String stmt) {
    boolean error = false;
    if (stmt.trim().equals("")) {
      ;
    } else if (size != null) {
      commandList.add(stmt);
    } else {
      error = true;
      promptPane.printStatusMessage("Nope. You'll need to run `init` first.");
    }
    clearUndoStack();
    return error;
  }

  /**
   * Remove the previous statement (a line or block of code) added to the 
   * command list
   */
  public void removePreviousStatement() {
    if (commandList != null && !commandList.isEmpty()) {
      commandList.remove(commandList.size() - 1);
    }
  }

  /**
   * Adds a library to the list of libraries to be imported. Assumes 
   * that the library is valid. 
   * @param stmt the library to be added, of the form <code>a.b.c.*</code>
   */
  public void addImportStatement(String stmt) {
    if (size == null) {
      promptPane.printStatusMessage("Nope. You'll need to run `init` first.");
    } else {
      importsList.add(stmt);
    }
    clearUndoStack();
  }

  /**
   * Removes the last import statement added. 
   */
  public void removePreviousImportStatement() {
    if (importsList != null && !importsList.isEmpty()) {
      importsList.remove(importsList.size() - 1);
    }
  }

  /**
   * Add a line of code (or part of a single line) to the continuing command list.
   * @param stmt the statement the user just typed in
   * @return true if init has not yet been run
   */
  public boolean addContinuingStatement(String stmt) {
    boolean error = false;
    if (stmt.trim().equals("")) {
      ;
    } else if (size != null) {
      continuingCommandList.add(stmt);
    } else {
      error = true;
      promptPane.printStatusMessage("Nope. You'll need to run `init` first.");
    }
    clearUndoStack();
    return error;
  }

  /**
   * Used to signal the end of a continuing block. Makes everything in the
   * continuing block into a single line, and adds it into the command list.
   */
  public void endContinuingStatement() {
    // TODO: Check errors
    // No need to check for uninitialized size here, since this function 
    // won't be called unless addContinuing statement succeeds
    Iterator<String> it = continuingCommandList.iterator();
    StringBuilder contCmd = new StringBuilder();
    while (it.hasNext()) {
      contCmd.append(it.next());
    }
    // Need to squish it into one line for undo to work without
    // having things either inefficient or overly complex
    commandList.add(contCmd.toString());

    continuingCommandList.clear();
    clearUndoStack();
  }

  /**
   * Used to clear everything except the parameters to be passed to the 
   * <code>size()</code> method.
   * @deprecated No longer used
   */
  public void clear() {
    commandList.clear();
    clearUndoStack();
  }

  /**
   * Lets the CommandList object know that the user is no longer 
   * undo/redo-ing things
   */
  public void clearUndoStack() {
    undoStack.clear();
    isUndoing = false;
  }

  /**
   * Undo the last x non-command word statements
   * 
   * @param x
   *          : The number of statements to be undone
   * @return The number of statements actually undone. This will be less than
   *         <code>x</code> iff there weren't <code>x</code> statements to undo.
   */
  public int undo(int x) {
    int n = min(x, commandList.size());
    isUndoing = true;
    for (int i = commandList.size() - 1, j = 0; j < n; i--, j++) {
      undoStack.push(commandList.get(i));
      commandList.remove(i);
    }
    return n;
  }

  /**
   * Redo the last x undos
   * 
   * @param x
   *          : The number of statements to be redone
   * @return The number of statements actually redone. This will be less than
   *         <code>x</code> iff there weren't <code>x</code> statements to redo.
   */
  public int redo(int x) {
    if (!isUndoing) {
      return 0;
    }
    int n = min(x, undoStack.size());
    for (int i = 0; i < n; i++) {
      commandList.add(undoStack.pop());
    }
    return n;
  }

  /**
   * Gets a syntactically correct sketch which consists of all the code in
   * the command list, along with the appropriate code in the {@link Size} 
   * object
   * @return The code of the sketch
   */
  public String getREPLSketchCode() {
    // TODO: Think of the best minimalistic sketch to write once issues 
    //       with size(), draw(), etc. are sorted. Till then, this may 
    //       seem a little hacky. 
    if (size == null) {
      return null;
    }
    StringBuilder code = new StringBuilder();
    
    Iterator<String> it = importsList.iterator();
    while (it.hasNext()) {
      code.append(it.next());
      code.append('\n');
    }
    
    if (!code.toString().isEmpty())
      code.append('\n');
    
    code.append("void setup() {\n");
    code.append(size.getSizeStatement());
    code.append("\n}\n\n");

//    if (!commandList.isEmpty()) {
      code.append("void draw() {\n");
      it = commandList.iterator();
      while (it.hasNext()) {
        code.append(it.next());
        code.append('\n');
      }
      code.append('}');
//    }
//    System.out.println(formatter.format(code.toString()));
    return formatter.format(code.toString());
  }

  /**
   * @return True if the user has entered legitimate code
   */
  public boolean hasStuffToPrint() {
    return (commandList != null && !commandList.isEmpty());
  }

  /**
   * "Packages" all the valid code that the user has entered so far into the
   * form of a nice little function that takes no arguments, and that has a 
   * <code>void</code> return type. Calling this function from Processing's
   * <code>draw()</code> method will display a sketch exactly as the REPL Mode
   * displayed when this (<code>getCodeFunction</code>) method was called,
   * provided <code>size()</code> is called appropriately from the 
   * <code>setup()</code> method.
   * @param functionName The name of the function
   * @return A string that contains the function, pre-formatted with 
   * Processing's formatter
   */
  public String getCodeFunction(String functionName) {

    clearUndoStack();

    StringBuilder code = new StringBuilder();

    code.append("void ");
    code.append(functionName);
    code.append("() {\n");
    Iterator<String> it = commandList.iterator();
    while (it.hasNext()) {
      code.append(it.next());
      code.append('\n');
    }
    code.append('}');

    // may as well have the code look pretty
    return formatter.format(code.toString());
  }

}
