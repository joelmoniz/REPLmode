package jm.mode.replmode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

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
  AutoFormat formatter;
  Size size;  
  
  public static final String CLEAR_COMMAND = "clear";
  public static final String INIT_COMMAND = "init";
  public static final String REINIT_COMMAND = "reinit";
  public static final String UNDO_COMMAND = "undo";
  public static final String REDO_COMMAND = "redo";
  public static final String PRINT_COMMAND = "print";
  
  public static final String[] REPL_COMMAND_SET = {
    CLEAR_COMMAND, INIT_COMMAND, REINIT_COMMAND, UNDO_COMMAND, REDO_COMMAND,
    PRINT_COMMAND };
  
  public static final String SIZE_PD2 = "P2D";
  public static final String SIZE_PD3 = "P3D";
  public static final String SIZE_PDF = "PDF";
  public static final String[] SIZE_RENDERERS = {SIZE_PD2, SIZE_PD3, SIZE_PDF};
  
  class Size {
    
    private int w,h;
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
    size = new Size();
  }
  
  public void init(int w, int h) {
    commandList.clear();
    continuingCommandList.clear();
    size = new Size(w, h);
  }
  
  public void init(int w, int h, String renderer) {
    commandList.clear();
    continuingCommandList.clear();
    size = new Size(w, h, renderer);
  }
  
  public void addStatement(String stmt) {
    if (size != null) {
      commandList.add(stmt);
    }
  }
  
  public void addContinuingStatement(String stmt) {
    if (size != null) {
      continuingCommandList.add(stmt);
    }
  }
  
  public void endContinuingStatement() {
    // TODO: Check errors
    Iterator<String> it = continuingCommandList.iterator();
    
    while (it.hasNext()) {
      String n = it.next();
      
      while (n.trim().endsWith(",") && it.hasNext()) {
        n += it.next(); // don't expect this to be too many operations, 
                        // so using a String instead of a StringBuilder
      }
      
      commandList.add(n);
    }
    
//    commandList.addAll(continuingCommandList);
    continuingCommandList.clear();
  }
  
  public void clear() {
    commandList.clear();
  }
  
  // TODO: Think of the best minimalistic sketch to write once issues 
  //       with size(), draw(), etc. are sorted. Till then, this may 
  //       seem a little hacky. 
  public String getREPLSketchCode() {
    if (size == null) {
      promptPane.printStatusMessage("Nope. You'll need to run `init` first.");
      return null;
    }
    StringBuilder code = new StringBuilder();
    
    code.append("void setup() {\n");
    code.append(size.getSizeStatement());
    code.append("\n}\n\n");
    
    if (!commandList.isEmpty()) {
      code.append("void draw() {\n");
      Iterator<String> it = commandList.iterator();
      while (it.hasNext()) {
        code.append(it.next());
        code.append('\n');
      }
      code.append('}');
    }
    System.out.println(formatter.format(code.toString()));
    return formatter.format(code.toString());
  }
  
  public String getCodeFunction(String functionName) {

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
