package jm.mode.replmode;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.text.Utilities;

/**
 * Class responsible for setting up a NavigationFilter that makes a JTextArea
 * have command-prompt-esque properties.
 * 
 * Code adapted from 
 * <a href=http://www.coderanch.com/t/508726/GUI/java/creating-custom-command-prompt-java#post_text_2299445>here</a>.
 */
public class CommandPromptPane extends NavigationFilter {
  private int prefixLength;

  private Action deletePrevious;

  private Action shiftLine;

  JTextArea consoleArea;
  CommandHistory commandHistManager;
  CommandList commandListManager;
  REPLEditor replEditor;

  String prompt;
  String promptContinuation;

  boolean isContinuing;
  int openLeftCurlies;
  int rowStartPosition;

  public CommandPromptPane(String prompt, String promptContinuation, REPLEditor editor, JTextArea component) {
    consoleArea = component;
    commandHistManager = new CommandHistory();
    commandListManager = new CommandList(this);
    replEditor = editor;
    this.prompt = prompt;
    this.promptContinuation = promptContinuation;
    this.prefixLength = prompt.length();
    isContinuing = false;
    openLeftCurlies = 0;
    rowStartPosition = 0;

    // TODO: Check these next 4 lines out. Refactor later if necessary.
    deletePrevious = component.getActionMap().get("delete-previous");
    shiftLine = component.getActionMap().get("insert-break");
    component.getActionMap().put("delete-previous", new BackspaceAction());
    component.getActionMap().put("insert-break", new EnterAction());

    component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
    component.getActionMap().put("up", new KeyAction("up"));
    component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
    component.getActionMap().put("down", new KeyAction("down"));
    
//    component.addKeyListener(new CommandKeyListener());
    component.setCaretPosition(prefixLength);
//    component.setWrapStyleWord(true);
    component.setLineWrap(true);
  }

  public void setDot(NavigationFilter.FilterBypass fb, int dot,
                     Position.Bias bias) {
    fb.setDot(Math.max(dot, rowStartPosition + prefixLength), bias);
  }

  public void moveDot(NavigationFilter.FilterBypass fb, int dot,
                      Position.Bias bias) {
    fb.moveDot(Math.max(dot, rowStartPosition + prefixLength), bias);
  }

  class BackspaceAction extends AbstractAction {
    private static final long serialVersionUID = -116059028248053840L;

    public void actionPerformed(ActionEvent e) {
      JTextArea component = (JTextArea) e.getSource();

      if (getColumn(component) > prefixLength + 1) {
        deletePrevious.actionPerformed(null);
      }
    }
  }

  class EnterAction extends AbstractAction {
    private static final long serialVersionUID = 2813908067205522536L;

    public void actionPerformed(ActionEvent e) {
      JTextArea component = (JTextArea) e.getSource();
      component.setCaretPosition(component.getText().length());
      String command = getLastLine();
      String trimmedCommand = command.trim();
      String firstCommandWord = command.split(" ")[0];
      shiftLine.actionPerformed(null);
      commandHistManager.insertCommand(command);
//      printStatusMessage("Done.");
//      System.out.println("Position: "+component.getCaretPosition());
      
      if (Arrays.asList(CommandList.REPL_COMMAND_SET).contains(firstCommandWord)) {
        boolean isDone = true;
        if (command.equals(CommandList.CLEAR_COMMAND)) {
          // TODO: Or is selecting everything and then using replaceSelection() better?
//        component.select(0, component.getText().length());
//        component.replaceSelection(prompt);

          openLeftCurlies = 0;
          isContinuing = false;
          component.setText(prompt);
          
          commandListManager.clear();
          rowStartPosition = 0;

        } else if (firstCommandWord.equals(CommandList.INIT_COMMAND)) {
          isDone = handleInit(trimmedCommand, false);
          component.setText(prompt + trimmedCommand + '\n' + prompt);
          try {
            int cp = consoleArea.getCaretPosition();
            rowStartPosition = Utilities.getRowStart(consoleArea, cp);
            System.out.println(rowStartPosition);
          } catch (BadLocationException e1) {
            e1.printStackTrace();
          }
        } else if (firstCommandWord.equals(CommandList.REINIT_COMMAND)) {
          if (isContinuing) {
            printStatusMessage("Oops! REPL Mode is in the midst of another command (block)");
            isDone = false;
            component.replaceSelection(promptContinuation);
          }
          else {
            isDone = handleInit(trimmedCommand, true);
            component.replaceSelection(prompt);
          }
          try {
            rowStartPosition = Math.max(rowStartPosition, Utilities
                .getRowStart(consoleArea, consoleArea.getCaretPosition()));
          } catch (BadLocationException e1) {
            e1.printStackTrace();
          }
        } else if (firstCommandWord.equals(CommandList.UNDO_COMMAND)) {
          if (isContinuing) {
            printStatusMessage("Oops! REPL Mode is in the midst of another command (block)");
            isDone = false;
            component.replaceSelection(promptContinuation);
          }
          else {
            isDone = handleUndo(trimmedCommand, false);
            component.replaceSelection(prompt);
          }
          try {
            rowStartPosition = Math.max(rowStartPosition, Utilities
                .getRowStart(consoleArea, consoleArea.getCaretPosition()));
            System.out.println(rowStartPosition);
          } catch (BadLocationException e1) {
            e1.printStackTrace();
          }
        } else if (firstCommandWord.equals(CommandList.REDO_COMMAND)) {
          if (isContinuing) {
            printStatusMessage("Oops! REPL Mode is in the midst of another command (block)");
            isDone = false;
            component.replaceSelection(promptContinuation);
          }
          else {
            isDone = handleUndo(trimmedCommand, true);
            component.replaceSelection(prompt);
          }
          try {
            rowStartPosition = Math.max(rowStartPosition, Utilities
                .getRowStart(consoleArea, consoleArea.getCaretPosition()));
            System.out.println(rowStartPosition);
          } catch (BadLocationException e1) {
            e1.printStackTrace();
          }
        }

        prefixLength = prompt.length();
        
        if (replEditor != null && isDone) {
          String temp = commandListManager.getREPLSketchCode();
          try {
            if (temp != null) {
              replEditor.handleREPLRun(temp);
            }
          } catch (Exception exc) {
            exc.printStackTrace();
          }
        }

      } else {

        if (isContinuing || trimmedCommand.endsWith("{")
            || trimmedCommand.endsWith(",")) {
          commandListManager.addContinuingStatement(command);
          
          if (trimmedCommand.endsWith("}") || trimmedCommand.endsWith(";")) {
            if (trimmedCommand.endsWith("}")) {
              openLeftCurlies--;
            }
            if (openLeftCurlies == 0) {
              commandListManager.endContinuingStatement();
              component.replaceSelection(prompt);
              prefixLength = prompt.length();
              isContinuing = false;
              String temp = commandListManager.getREPLSketchCode();
              if (replEditor != null && temp != null) {
                try {
                  replEditor.handleREPLRun(temp);
                } catch (Exception exc) {
                  exc.printStackTrace();
                }
//              System.out.println("Here");
              }
            } else {
              component.replaceSelection(promptContinuation);
              prefixLength = promptContinuation.length();
              isContinuing = true;
            }
          } else {
            component.replaceSelection(promptContinuation);
            prefixLength = promptContinuation.length();
            isContinuing = true;

            if (trimmedCommand.endsWith("{")) {
              openLeftCurlies++;
            }
          }
        } else {
          commandListManager.addStatement(command);
          String temp = commandListManager.getREPLSketchCode();
          component.replaceSelection(prompt);
          prefixLength = prompt.length();

          if (replEditor != null && temp != null) {
            try {
//              String temp2 = commandHistManager.toSketch();
              replEditor.handleREPLRun(temp);
            } catch (Exception exc) {
              exc.printStackTrace();
            }
          }
        }
        
        try {
          rowStartPosition = Math.max(rowStartPosition, Utilities
              .getRowStart(consoleArea, consoleArea.getCaretPosition()));
          System.out.println(rowStartPosition);
        } catch (BadLocationException e1) {
          e1.printStackTrace();
        }
      }
      
    }
  }
  
  private boolean handleInit(String arg, boolean isReInit) {
    String args[] = arg.split("\\s+");
    boolean wasSuccess = false;
    if (args.length == 1) {
      if (isReInit) {
        commandListManager.reinit();
      }
      else {
        commandListManager.init();
      }
      wasSuccess = true;
    }
    else if (args.length == 3 || args.length == 4) {
      int w=100, h=100, errCount=0;
      wasSuccess = true;
      String err = "Error: ";
      
      try {
        w = Integer.parseInt(args[1]);
      } catch (NumberFormatException nfe) {
        err += "w=" + args[1];
        wasSuccess = false;
        errCount++;
      }
      
      try {
        h = Integer.parseInt(args[2]);
      } catch (NumberFormatException nfe) {
        errCount++;
        if (!wasSuccess) {
          err += " and ";
        }
        err += "h=" + args[2];
        wasSuccess = false;
      }
      
      if (!wasSuccess) {
        if (errCount == 1) {
          err += " is not an integer";
        }
        else if (errCount == 2) {
          err += " are not integers";
        }
      }
      
      if (args.length == 4
          && !Arrays.asList(CommandList.SIZE_RENDERERS).contains(args[3])) {
        if (!wasSuccess) {
          err += " and ";
        }
        wasSuccess = false;
        err += "\"" + args[3] + "\" renderer is undefined (only ";
        for (int i=0; i<CommandList.SIZE_RENDERERS.length; i++) {
          if (i != CommandList.SIZE_RENDERERS.length-1) {
          err += "\"" + CommandList.SIZE_RENDERERS[i] + "\", ";
          }
          else {
            err += "and \"" + CommandList.SIZE_RENDERERS[i] + "\"";
          }
        }
        err += " renderers may be used)";
      }
      
      if (wasSuccess) {
        if (args.length == 3) {
          if (isReInit) {
            commandListManager.reinit(w, h);
          } else {
            commandListManager.init(w, h);
          }
        } else {
          if (isReInit) {
            commandListManager.reinit(w, h, args[3]);
          } else {
            commandListManager.init(w, h, args[3]);
          }
        }
      } else {
        printStatusMessage(err);
      }
    }
    
    return wasSuccess;
  }

  private boolean handleUndo(String arg, boolean isRedo) {
    String[] undo = arg.split("\\s+");
    boolean wasSuccess = true;
    int k = 0;
    if (undo.length == 1) {
      if (!isRedo) {
        k = commandListManager.undo(1);
      }
      else {
        k = commandListManager.redo(1);
      }
    }
    else if (undo.length == 2) {
      int n;
      try {
        n = Integer.parseInt(undo[1]);
        if (!isRedo) {
          k = commandListManager.undo(n);
        }
        else {
          k = commandListManager.redo(n);
        }
      } catch (NumberFormatException nfe) {
        printStatusMessage("Error: n=" + undo[1] + " is not an integer");
        wasSuccess = false;
      }
    }
    else {
      wasSuccess = false;
      printStatusMessage("Error: undo command should have only 0 or 1 arguments");
    }
    
    if (wasSuccess) {
      if (k==0){
        printStatusMessage("Nothing to " + (!isRedo?"undo":"redo"));
        wasSuccess = false;
      }
      else if (k==1) {
        printStatusMessage("1 statement " + (!isRedo?"undone":"redone"));
      }
      else {
        printStatusMessage(k + " statements " + (!isRedo?"undone":"redone"));
      }
    }
    return wasSuccess;
  }
  
  class KeyAction extends AbstractAction {

    private static final long serialVersionUID = 3382543935199626852L;
    private String key;
    
    public KeyAction(String string) {
      key = string;
    }

    public void actionPerformed(ActionEvent e) {
      String cycledCommand = "";
      JTextArea component = (JTextArea) e.getSource();
      String prevCommand = getLastLine();
      if (key.equals("up")) {
        cycledCommand = commandHistManager.getPreviousCommand(prevCommand);
//        System.out.println(getLastLine());
//        System.out.println(commandHistManager.getPreviousCommand(getLastLine()));
      }
      else if (key.equals("down")) {
        cycledCommand = commandHistManager.getNextCommand(prevCommand);
//        System.out.println(getLastLine());
//        System.out.println(commandHistManager.getNextCommand(getLastLine()));        
      }
//      System.out.println(cycledCommand);
//      System.out.println(component.getText().lastIndexOf(prompt) + prompt.length()
//                       + " to " + component.getText().length() + " with " + cycledCommand);
//      component.select(component.getText().lastIndexOf(prompt) + prompt.length()
//                       , component.getText().length());
//      component.requestFocus();
//      component.setCaretPosition(component.getText().lastIndexOf(prompt) + prompt.length());
//      component.moveCaretPosition(component.getText().length());
//      component.replaceSelection(cycledCommand);
      
      if (isContinuing) {
        component.replaceRange(cycledCommand, 
                               component.getText().lastIndexOf(promptContinuation) + 
                               promptContinuation.length(), 
                               component.getText().length());
      }
      else {
      component.replaceRange(cycledCommand, 
                             component.getText().lastIndexOf(prompt) + prompt.length(), 
                             component.getText().length());
      }
    }
  }

  /**
   * Prints a status message on a new line
   * @param msg The status message
   */
  public void printStatusMessage(String msg) {
    consoleArea.replaceSelection(msg + "\n");// + prompt);
//    prefixLength = prompt.length();
//    try {
//      rowStartPosition = Math.max(rowStartPosition, Utilities
//          .getRowStart(consoleArea, consoleArea.getCaretPosition()));
//    } catch (BadLocationException e1) {
//      e1.printStackTrace();
//    }
  }

/*//  class CommandKeyListener implements KeyListener {
//
//    @Override
//    public void keyTyped(KeyEvent e) {
//      switch (e.getKeyCode()) {
//      case KeyEvent.VK_UP:
//        System.out.println("Here");
//        System.out.println(commandHistManager.getPreviousCommand(getLastLine()));
//      }
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//    }
//
//  }*/
  
  public String getLastLine() {
    // TODO: Is there a more efficient way of extracting the last line of code?
    int lineStartLocation;
    if (isContinuing) {
      lineStartLocation = consoleArea.getText().lastIndexOf(promptContinuation) 
          + promptContinuation.length();
    }
    else {
      lineStartLocation = consoleArea.getText().lastIndexOf(prompt) 
        + prompt.length();
    }
    return consoleArea.getText().substring(lineStartLocation);
  }

  // Refer : http://stackoverflow.com/a/2750099/2427542
  // Refer : http://stackoverflow.com/a/13375811/2427542
  /**
   * Use to get line number at which caret is placed.
   * 
   * Code adapted from http://java-sl.com/tip_row_column.html
   * 
   * @param console
   *          : The JTextArea console
   * @return Row number
   */
  public static int getRow(JTextArea console) {
    int pos = console.getCaretPosition();
    int rn = (pos == 0) ? 1 : 0;
    try {
      int offs = pos;
      while (offs > 0) {
        offs = Utilities.getRowStart(console, offs) - 1;
        rn++;
      }
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    return rn;
  }

  /**
   * Use to get location of column at which caret is placed. The column number
   * starts with 1.
   * 
   * Code adapted from http://java-sl.com/tip_row_column.html
   * 
   * @param console
   *          : The JTextArea console
   * @return Column number
   */
  public static int getColumn(JTextArea console) {
    int pos = console.getCaretPosition();
    try {
      return pos - Utilities.getRowStart(console, pos) + 1;
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    return -1;
  }

  /*
   * Convenience main() function for easy debugging.
   */
  public static void main(String args[]) throws Exception {

    JTextArea textField = new JTextArea(">> ", 20, 40);
    CommandPromptPane cmdPromptPane = new CommandPromptPane(">> ", "...  ", null, textField);
    textField.setNavigationFilter(cmdPromptPane);

    JFrame frame = new JFrame("Navigation Filter Example");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(textField);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}