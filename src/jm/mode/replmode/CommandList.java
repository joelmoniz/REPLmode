package jm.mode.replmode;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;

import processing.mode.java.AutoFormat;

/**
 * Class to store the valid commands the user has entered, to check them for
 * errors, and to perform other operation related to them.
 * 
 * @author Joel
 *
 */
public class CommandList {
  CommandPromptPane promptPane;

  ArrayList<String> commandList;

  ArrayList<String> continuingCommandList;
  
  ArrayList<String> importsList;

  Stack<String> undoStack;

  boolean isUndoing;

  AutoFormat formatter;

  Size size;

  public static final String CLEAR_COMMAND = "clear";

  public static final String INIT_COMMAND = "init";

  public static final String RESIZE_COMMAND = "resize";

  public static final String UNDO_COMMAND = "undo";

  public static final String REDO_COMMAND = "redo";

  public static final String PRINT_COMMAND = "print";

  public static final String[] REPL_COMMAND_SET = {
    CLEAR_COMMAND, INIT_COMMAND, RESIZE_COMMAND, UNDO_COMMAND, REDO_COMMAND,
    PRINT_COMMAND };

  public static final String SIZE_PD2 = "P2D";

  public static final String SIZE_PD3 = "P3D";

  public static final String SIZE_PDF = "PDF";

  public static final String[] SIZE_RENDERERS = { SIZE_PD2, SIZE_PD3, SIZE_PDF };

  class Size {

    private int w, h;

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
        s.append(", \"");
        s.append(renderer);
        s.append("\"");
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

  public void reinit() {
    continuingCommandList.clear();
    size = new Size();
  }

  public void reinit(int w, int h) {
    continuingCommandList.clear();
    size = new Size(w, h);
  }

  public void reinit(int w, int h, String renderer) {
    continuingCommandList.clear();
    size = new Size(w, h, renderer);
  }

  public void init() {
    commandList.clear();
    continuingCommandList.clear();
    clearUndoStack();
    size = new Size();
  }

  public void init(int w, int h) {
    commandList.clear();
    continuingCommandList.clear();
    clearUndoStack();
    size = new Size(w, h);
  }

  public void init(int w, int h, String renderer) {
    commandList.clear();
    continuingCommandList.clear();
    clearUndoStack();
    size = new Size(w, h, renderer);
  }

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

  public void removePreviousStatement() {
    if (commandList != null && !commandList.isEmpty()) {
      commandList.remove(commandList.size() - 1);
    }
  }

  public void addImportStatement(String stmt) {
    if (size == null) {
      promptPane.printStatusMessage("Nope. You'll need to run `init` first.");
    } else {
      importsList.add(stmt);
    }
    clearUndoStack();
  }

  public void removePreviousImportStatement() {
    if (importsList != null && !importsList.isEmpty()) {
      importsList.remove(importsList.size() - 1);
    }
  }

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

  public void clear() {
    commandList.clear();
    clearUndoStack();
  }

  public void clearUndoStack() {
    undoStack.clear();
    isUndoing = false;
  }

  public int undo(int x) {
    int n = min(x, commandList.size());
    isUndoing = true;
    for (int i = commandList.size() - 1, j = 0; j < n; i--, j++) {
      undoStack.push(commandList.get(i));
      commandList.remove(i);
    }
    return n;
  }

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

  // TODO: Think of the best minimalistic sketch to write once issues 
  //       with size(), draw(), etc. are sorted. Till then, this may 
  //       seem a little hacky. 
  public String getREPLSketchCode() {
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
    System.out.println(formatter.format(code.toString()));
    return formatter.format(code.toString());
  }

  public boolean hasStuffToPrint() {
    return (commandList != null && !commandList.isEmpty());
  }

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
