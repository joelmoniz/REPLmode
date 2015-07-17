package jm.mode.replmode;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.text.Utilities;

import processing.app.Library;
import processing.app.SketchException;

/**
 * Class responsible for setting up a NavigationFilter that makes a JTextArea
 * have command-prompt-esque properties.
 * 
 * UI Code adapted from <a
 * href=http://www.coderanch.com/t/508726/GUI/java/creating
 * -custom-command-prompt-java#post_text_2299445>here</a>.
 * 
 * @author Joel Moniz
 */
public class CommandPromptPane extends NavigationFilter {
  /**
   * Represents the length of the prompt prefix string(<code>>></code> and
   * <code>...</code>). So this takes a value of 3 ("<code>>> </code>") or 7 ("
   * <code>...    </code>")
   */
  private int prefixLength;

  /**
   * Represents the action done when the user tries to delete something
   */
  private Action deletePrevious;

  /**
   * Represents the action done when the user tries to move to another line
   */
  private Action shiftLine;

  /**
   * The console
   */
  JTextArea consoleArea;

  CommandHistory commandHistManager;

  CommandList commandListManager;

  REPLEditor replEditor;

  /**
   * The prompt string (<code>">> "</code>)
   */
  String prompt;

  /**
   * The prompt continuation string (<code>"...    "</code>)
   */
  String promptContinuation;

  /**
   * Whether or not the command to be entered by the user will be a continuation
   * of the previous command
   */
  boolean isContinuing;

  /**
   * Represents the number of open curly braces yet to be closed in a continuing
   * block of code
   */
  int openLeftCurlies;

  /**
   * The starting of the current row (with respect to the very first character,
   * not with respect to the starting of the line)
   */
  int rowStartPosition;
  
  /**
   * Regex Pattern representing a single import in the statement
   */
  final Pattern importPattern 
    = Pattern.compile("((?:^\\s*))(import\\s+)((?:static\\s+)?\\S+)(\\s*;(?:\\s*$))");

  /**
   * Regex Pattern representing the presence of one or more imports in the
   * statement
   */
  final Pattern importInLinePattern
    = Pattern.compile("((?:^|;)\\s*)(import\\s+)((?:static\\s+)?\\S+)(\\s*;)");

  public CommandPromptPane(String prompt, String promptContinuation,
                           REPLEditor editor, JTextArea component) {
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

    component.getInputMap()
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
    component.getActionMap().put("up", new KeyAction("up"));
    component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                                "down");
    component.getActionMap().put("down", new KeyAction("down"));

    component.setCaretPosition(prefixLength);
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

  /**
   * Handles what happens when the user hits the backspace key. 
   * More specifically, ensures that the user does not delete the prompt prefix. 
   * @author Joel Moniz
   */
  class BackspaceAction extends AbstractAction {
    private static final long serialVersionUID = -116059028248053840L;

    public void actionPerformed(ActionEvent e) {
      JTextArea component = (JTextArea) e.getSource();

      /*
       * Delete the previous character (or the selection) only when the cursor
       * location from the start of the line is after the prefix. Prevents the
       * user from deleting the prompt prefix.
       * 
       * The second part of the condition (the part after the ||) is to ensure
       * that selecting the entire line (or a part of the line from anywhere to
       * the starting point) and then pressing backspace deletes the selection.
       * The cursor is just after the prompt, and while a normal backspace will
       * delete only the selection (and not touch the location before it), if
       * this part after the || is not provided, all that the action handler
       * knows is that the cursor is right in front of the prompt, and does not
       * backspace.
       */
      if ((getColumn(component) > prefixLength + 1)
          || ((component.getSelectionEnd() != component.getSelectionStart()) 
          && (getColumn(component) > prefixLength))) {
        deletePrevious.actionPerformed(null);
      }
    }
  }

  /**
   * Handles what happens when the user hits the enter key. 
   * @author Joel Moniz
   */
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

      if (Arrays.asList(CommandList.REPL_COMMAND_SET)
          .contains(firstCommandWord)) {
        /*
         * If the string entered is a command word
         */
        handleREPLModeCommand(trimmedCommand, component);
      } else {
        if (importInLinePattern.matcher(trimmedCommand).find()) {
          /*
           * If the string entered contains an import statement
           */
          handleImportStatement(trimmedCommand, component);
        }
        else if (isContinuing || trimmedCommand.endsWith("{")
            || trimmedCommand.endsWith(",")) {
          /*
           * If the string entered is a continuation of a previous statement,
           * or if it hints at a continuation (by ending with a '{' or ',')  
           */
          handleContinuingStatement(trimmedCommand, component);
        } else {
          /*
           * If the string entered is a single, non-command word statement 
           */
          boolean error = commandListManager.addStatement(command);
          component.replaceSelection(prompt);
          prefixLength = prompt.length();
          runTempSketch(error, false);
        }

        try {
          /*
           * Set the rowStartPosition to where the caret is at right now
           * (which will be at the start of the new line, just after the prompt)
           */
          rowStartPosition = Math.max(rowStartPosition, Utilities
              .getRowStart(consoleArea, consoleArea.getCaretPosition()));
        } catch (BadLocationException e1) {
          e1.printStackTrace();
        }
      }

    }
  }

  /**
   * Used to handle a line entered by the user that contains a valid <code>import</code>
   * statement within it
   * 
   * @param stmt The line entered by the user
   * @param component The text area component
   */
  protected void handleImportStatement(String stmt, JTextArea component) {
    if (isContinuing) {
      /*
       * Don't allow an import while in the midst of a statement block
       */
      printStatusMessage("Oops! REPL Mode is in the midst of another "
          + "command (block). Can't import until that is done.");
      component.replaceSelection(promptContinuation);
      prefixLength = promptContinuation.length();
    }
    else if (importPattern.matcher(stmt).find()) {
      if (importExists(stmt)) {
        /*
         * If the statement represents a single import statement, and the
         * import statement an existing library, add it as an import
         */
        commandListManager.addImportStatement(stmt);
      }
      else {
        printStatusMessage("Cannot find the library that import statement "
            + "corresponds with.");
      }
      component.replaceSelection(prompt);
      prefixLength = prompt.length();
    }
    else {
      /*
       * If the user has tried to mix the import statement with another
       * statement, or tried to import multiple things in one go via several
       * import statements
       */
      printStatusMessage("The import is a complex thing." +
          " Please import a library in a stand-alone statement");
      component.replaceSelection(prompt);
      prefixLength = prompt.length();
    }
  }

  /**
   * Check whether the library corresponding to <code>imprt</code> exists or
   * not
   * @param imprt The library name
   * @return True if such a library exists, false otherwise
   */
  protected boolean importExists(String imprt) {
    int dot = imprt.lastIndexOf('.');
    String entry = (dot == -1) ? imprt : imprt.substring(0, dot);

    entry = entry.trim().substring(6).trim();

    // Try to get the library classpath and add it to the list
      try {
        Library library = replEditor.getMode().getLibrary(entry);
        
        if (library == null) {
          return false;
        }
      } catch (SketchException e) {
        e.printStackTrace();
        return false;
      }
    return true;
  }

  /**
   * Handles a statement entered when the user is in the process of entering
   * a statement block.
   * @param command The line just entered by the user
   * @param component The text area component
   */
  protected void handleContinuingStatement(String command, 
                                           JTextArea component) {
    boolean error = commandListManager.addContinuingStatement(command);

    if (command.endsWith("}") || command.endsWith(";")) {
      /*
       * Represents the possibility of the continuing block being closed,
       * "}" for a block indicated by a line ending with a "{", and ";"
       * for a function call indicated by a line ending with a "," 
       */
      if (command.endsWith("}")) {
        /*
         * That's one more open brace closed
         */
        openLeftCurlies--;
      }
      if (openLeftCurlies == 0) {
        /*
         * No open braces to close, either because we're all done, or because
         * there never were any to close in the first place.
         */
        commandListManager.endContinuingStatement();
        component.replaceSelection(prompt);
        prefixLength = prompt.length();
        isContinuing = false;
        runTempSketch(error, false);
      } else {
        /*
         * Set the prompt and prompt length appropriately, etc.
         * And the continuing block continues...
         */
        component.replaceSelection(promptContinuation);
        prefixLength = promptContinuation.length();
        isContinuing = true;
      }
    } else {
      /*
       * And continues...
       */
      component.replaceSelection(promptContinuation);
      prefixLength = promptContinuation.length();
      isContinuing = true;

      if (command.endsWith("{")) {
        openLeftCurlies++;
      }
    }
  }

  /**
   * Handles an REPL command word entered by the user.
   * @param command The line containing the command word just entered by
   * the user
   * @param component The text area component
   */
  protected void handleREPLModeCommand(String command, JTextArea component) {
    boolean isDone = true;
    boolean refresh = false;
    String firstCommandWord = command.split(" ")[0];
    if (command.equals(CommandList.CLEAR_COMMAND)) {
      // TODO: Or is selecting everything and then using 
      // replaceSelection() better?
//    component.select(0, component.getText().length());
//    component.replaceSelection(prompt);

      openLeftCurlies = 0;
      isContinuing = false;
      component.setText(prompt);

      // Don't clear the screen and undo stack any more
//      commandListManager.clear();
    } else if (firstCommandWord.equals(CommandList.INIT_COMMAND)) {
      isDone = handleInit(command, false);
      if (isDone) {
        /*
         * handleInit() succeeded
         */
        component.setText(prompt + command + '\n' + prompt);
        openLeftCurlies = 0;
        isContinuing = false;
      }
      else if (isContinuing) {
        /*
         * handleInit() failed, and the user was entering a code block before
         * just before
         */
        component.replaceSelection(promptContinuation);
      }
      else {
        component.replaceSelection(prompt);
      }
    } else if (firstCommandWord.equals(CommandList.RESIZE_COMMAND)) {
      if (isContinuing) {
        /*
         * Don't permit the user to resize the sketch in the midst of a command
         * block
         */
        printStatusMessage("Oops! REPL Mode is in the midst of another "
            + "command (block)");
        isDone = false;
        component.replaceSelection(promptContinuation);
      } else {
        isDone = handleInit(command, true);
        component.replaceSelection(prompt);
        refresh = true;
      }
    } else if (firstCommandWord.equals(CommandList.UNDO_COMMAND)) {
      if (isContinuing) {
        /*
         * Don't permit the user to undo something in the midst of a command
         * block
         */
        printStatusMessage("Oops! REPL Mode is in the midst of another "
            + "command (block)");
        isDone = false;
        component.replaceSelection(promptContinuation);
      } else {
        isDone = handleUndo(command, false);
        component.replaceSelection(prompt);
        /*
         * Undo needs to refresh, since otherwise, a shape already drawn
         * persists in the sketch window, and we don't want to force a clear()
         * since the user may want and expect persistence 
         */
        refresh = true;
      }
    } else if (firstCommandWord.equals(CommandList.REDO_COMMAND)) {
      if (isContinuing) {
        /*
         * Don't permit the user to redo an undo in the midst of a command
         * block (even if this was permitted, what would the user redo, 
         * though?)
         */
        printStatusMessage("Oops! REPL Mode is in the midst of another "
            + "command (block)");
        isDone = false;
        component.replaceSelection(promptContinuation);
      } else {
        isDone = handleUndo(command, true);
        component.replaceSelection(prompt);
      }
    } else if (firstCommandWord.equals(CommandList.PRINT_COMMAND)) {
      // Always have isDone as false, since we really don't want anything 
      // to get updated
      isDone = false;
      if (isContinuing) {
        /*
         * Don't permit the user to print code as a function in the midst of a
         * command block
         */
        printStatusMessage("Oops! REPL Mode is in the midst of another "
            + "command (block)");
        component.replaceSelection(promptContinuation);
      } else {
        handlePrintCode(command);
        component.replaceSelection(prompt);
      }
    } else if (firstCommandWord.equals(CommandList.HELP_COMMAND)) {
      // Always have isDone as false, since we really don't want anything
      // to get updated
      isDone = false;
      handleHelp(command);
      /*
       * Asking for help is always reasonable, even if it's in the midst of a
       * command block :p
       */
      if (isContinuing) {
        component.replaceSelection(promptContinuation);
      } else {
        component.replaceSelection(prompt);
      }
    }
    else if (command.equals(CommandList.MAN_COMMAND)) {
      /*
       * Although the second sentence is the truth, the first is, well, a 
       * poor little joke.
       * And the only person who's likely to so much as smile at this little
       *  pun is, well... me. Maybe. 
       */
      printStatusMessage("Awwww `man`! This humble little mode is not worthy "
          + "of having its own man pages. Maybe try `help` instead?");
      if (isContinuing) {
        component.replaceSelection(promptContinuation);
      } else {
        component.replaceSelection(prompt);
      }
    }

    if (isContinuing) {
      prefixLength = promptContinuation.length();
    }
    else {
      prefixLength = prompt.length();
    }

    if (command.equals(CommandList.CLEAR_COMMAND)) {
      /*
       * When cleared, the staring of the row is at position 0. 
       */
      rowStartPosition = 0;
    } else if (firstCommandWord.equals(CommandList.INIT_COMMAND) && isDone) {
      try {
        int cp = consoleArea.getCaretPosition();
        rowStartPosition = Utilities.getRowStart(consoleArea, cp);
        /*
         * Refresh (close and reopen) the sketch window, since the size might
         * have changed, and everything has to be cleared.
         */
        refresh = true;
      } catch (BadLocationException e1) {
        e1.printStackTrace();
      }
    } else {
      try {
        rowStartPosition = Math.max(rowStartPosition, Utilities
            .getRowStart(consoleArea, consoleArea.getCaretPosition()));
      } catch (BadLocationException e1) {
        e1.printStackTrace();
      }
    }

    runTempSketch(!isDone, refresh); /* since !isDone ==> isError */

  }

  /**
   * Handles the <code>init</code> and the <code>resize</code> command words.
   * Along with loads of error handling. And I mean LOADS. Just to get that
   * sentence well-formed and gramaticalie correct (who said anything about
   * spelling, though?)
   * 
   * @param arg The string entered by the user which represents the 
   * <code>init</code>/<code>resize</code> command word
   * @param isReInit Whether or not the mode console's sketch is simply being
   * resize (if true), and not re-initialized from scratch (if false)
   * @return True iff the statement entered by the user was in proper form- 
   * things that were supposed to be <code>int</code>s were indeed
   * <code>int</code>s, the user didn't try to enter a renderer unknown to
   * processing, etc. etc. 
   */
  private boolean handleInit(String arg, boolean isReInit) {
    String args[] = arg.split("\\s+");
    boolean wasSuccess = false;
    if (args.length == 1) {
      if (isReInit) {
        commandListManager.resize();
      } else {
        commandListManager.init();
      }
      wasSuccess = true;
    } else if (args.length == 3 || args.length == 4) {
      int w = 100, h = 100, errCount = 0;
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
        } else if (errCount == 2) {
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
        for (int i = 0; i < CommandList.SIZE_RENDERERS.length; i++) {
          if (i != CommandList.SIZE_RENDERERS.length - 1) {
            err += "\"" + CommandList.SIZE_RENDERERS[i] + "\", ";
          } else {
            err += "and \"" + CommandList.SIZE_RENDERERS[i] + "\"";
          }
        }
        err += " renderers may be used)";
      }

      if (wasSuccess) {
        if (args.length == 3) {
          if (isReInit) {
            commandListManager.resize(w, h);
          } else {
            commandListManager.init(w, h);
          }
        } else {
          if (isReInit) {
            commandListManager.resize(w, h, args[3]);
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

  /**
   * Handles the <code>undo</code> and the <code>redo</code> command words.
   * Along with a fair share of error handling.
   * @param arg The string entered by the user which represents the 
   * <code>undo</code>/<code>redo</code> command word
   * @param isRedo Whether the line passed corresponds to an <code>undo</code>
   * operation, or to a <code>redo</code> one.
   * @return True if the <code>undo</code>/<code>redo</code>operation was
   * successfully completed
   */
  private boolean handleUndo(String arg, boolean isRedo) {
    String[] undo = arg.split("\\s+");
    boolean wasSuccess = true;
    int k = 0;
    if (undo.length == 1) {
      if (!isRedo) {
        k = commandListManager.undo(1);
      } else {
        k = commandListManager.redo(1);
      }
    } else if (undo.length == 2) {
      int n;
      try {
        n = Integer.parseInt(undo[1]);
        if (!isRedo) {
          k = commandListManager.undo(n);
        } else {
          k = commandListManager.redo(n);
        }
      } catch (NumberFormatException nfe) {
        printStatusMessage("Error: n=" + undo[1] + " is not an integer");
        wasSuccess = false;
      }
    } else {
      wasSuccess = false;
      printStatusMessage("Error: undo command should have only 0 or 1 "
          + "arguments");
    }

    if (wasSuccess) {
      if (k == 0) {
        printStatusMessage("Nothing to " + (!isRedo ? "undo" : "redo"));
        wasSuccess = false;
      } else if (k == 1) {
        printStatusMessage("1 statement " + (!isRedo ? "undone" : "redone"));
      } else {
        printStatusMessage(k + " statements "
            + (!isRedo ? "undone" : "redone"));
      }
    }
    return wasSuccess;
  }

  /**
   * Handles the <code>print</code> command word. Also ensures that a 
   * parameter representing the function name is passed along with 
   * <code>print</code>, and that the parameter is a valid function name.
   * @param command The string entered by the user which contains the
   * <code>print</code> command word  
   */
  private void handlePrintCode(String command) {
    String[] args = command.split("\\s+");
    if (args.length != 2) {
      if (args.length > 2) {
        printStatusMessage("Error: print should have only "
            + "a single function name as argument");
      } else {
        printStatusMessage("Error: print should have only "
            + "a single function name as argument");
      }
    } else if (!commandListManager.hasStuffToPrint()) {
      printStatusMessage("Nothing to print into a function yet.");
    } else {
      if (!isValidFunctionName(args[1])) {
        printStatusMessage("Error: \"" + args[1] + "\""
            + " is not a valid function name");
      } else {
        String code = commandListManager.getCodeFunction(args[1]);
        replEditor.setText(replEditor.getText() + "\n" + code);
      }
    }
  }

  /**
   * Handles the <code>help</code> command word. If a parameter representing
   * another command valid word is passed along with <code>help</code>,  
   * it prints detailed information pertaining to that command word and its
   * usage. If not, it prints a list of valid command words. 
   * @param command The string entered by the user which contains the
   * <code>help</code> command word  
   */
  private void handleHelp(String command) {
    String[] args = command.split("\\s+");
    if (args.length == 1) {
      consoleArea.setTabSize(2);
      printStatusMessage("The following command words are available. "
          + "Type `help <commandword>` for more information on "
          + "each command word:\n"
          + "* init\t* resize\t* clear\n"
          + "* undo\t* redo  \t* print\n"
          + "* help\t* man");
    }
    else if (args.length == 2) {
      if (Arrays.asList(CommandList.REPL_COMMAND_SET)
          .contains(args[1])) {
        if (args[1].equals(CommandList.CLEAR_COMMAND)) {
          printStatusMessage("\nclear\n-----\nUsed to clear the REPL Console "
              + "without affecting anything else");
        } else if (args[1].equals(CommandList.INIT_COMMAND)) {
          printStatusMessage("\ninit\n----\n"
              + "Represents the first command issued to the REPL Mode "
              + "Console, to initialize the it. Also used to pass in details "
              + "about width and height of the sketch the console has to "
              + "display. May take the following forms:\n"
              + "* init: Initialize the console to display a 100x100 sketch \n"
              + "* init w h: Initialize the console to display a sketch of "
              + "width w and height h\n"
              + "* init w h r: Initialize the console to display a sketch of "
              + "width w and height h, and to use a renderer of type r "
              + "(r = P2D or P3D)");
        } else if (args[1].equals(CommandList.RESIZE_COMMAND)) {
          printStatusMessage("\nresize\n------\n"
              + "Allows the user to resize sketch that the REPL console "
              + "displays without losing the contents of the sketch. Can be "
              + "run in one of the following 3 ways, each similiar to their "
              + "`init` counterparts:\n  "
              + "* resize\n  "
              + "* resize w h\n  "
              + "* resize w h r");
        } else if (args[1].equals(CommandList.UNDO_COMMAND)) {
          printStatusMessage("\nundo\n----\n"
              + "Used to undo a (set of) statement(s). Command statements "
              + "cannot be undone. Can be called in one of 2 ways:\n"
              + "* undo: Undoes the last valid statment.\n"
              + "* undo x: Undoes the last x statements.");
        } else if (args[1].equals(CommandList.REDO_COMMAND)) {
          printStatusMessage("\nredo\n----\n"
              + "Used to redo a (set of) statement(s). A redo can only be "
              + "performed immendiately after an undo. Can be called in one "
              + "of 2 ways:\n"
              + "* redo: \"Redoes\" the last undo\n"
              + "* redo x: \"Redoes\" the last x statements undone by an "
              + "undo");
        } else if (args[1].equals(CommandList.PRINT_COMMAND)) {
          printStatusMessage("\nprint\n-----\n"
              + "Adds a method of the void return type to the current tab, "
              + "the method body consisting of all statements used to display "
              + "the output visible at present (i.e., all statements from the "
              + "last init, excluding those undone). Takes the format "
              + "`print x`, where x is a string representing the method "
              + "name.");
        } else if (args[1].equals(CommandList.HELP_COMMAND)) {
          printStatusMessage("\nhelp(noun): something or someone that helps.\n"
              + "  (Cambridge Dictionary)"
              + "\nOK, so that wasn't very helpful...");
        } else if (args[1].equals(CommandList.MAN_COMMAND)) {
          printStatusMessage("\nman\n---\n???");
        }
      }
      else {
        printStatusMessage("Invalid command word `"+ args[1] + "`");
      }
    }
    else {
      printStatusMessage("Invalid `help` query: please enter `help` to "
          + "get a list of command words, or `help <commandword>` to "
          + "get more information about each command word.");
    }
  }

  /**
   * Function to check whether the string passed is a valid function name
   * or not.
   * @param fn The string whose validity as a function name is to be checked
   * @return True iff <code>fn</code> is a valid name, false otherwise
   */
  private boolean isValidFunctionName(String fn) {
    // TODO: Is this correct?
    Pattern FN_NAME_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
    return FN_NAME_PATTERN.matcher(fn).find();
  }

  /**
   * Run the sketch corresponding to this REPL Console
   * @param error Represents whether this function does anything (if 
   * <code>error</code> is false) or not. 
   * True in two cases:<ul>
   * <li>There was an error when running a command word (such as 
   * <code>init</code> not yet being run, or an attempted <code>undo</code> 
   * in the middle of a continuing command block</li>
   * <li>Recompiling and running the code is simply not necessary (such
   * as when a command word like <code>help</code> is entered)</li>
   * @param refresh Whether or not the REPL Console's sketch window has to be
   * closed and re-opened (such as if the size of the sketch window changes) 
   */
  protected void runTempSketch(boolean error, boolean refresh) {
    if (replEditor != null && !error) {
      try {
        String code = commandListManager.getREPLSketchCode();
        replEditor.handleREPLRun(code, refresh);
      } catch (Exception exc) {
        exc.printStackTrace();
      }
    }
  }

  /**
   * Handles key presses. In particular, responsible for catching the up and
   * down arrow key presses and causing them to cycle through the command
   * history
   * @author Joel Moniz
   */
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
      } else if (key.equals("down")) {
        cycledCommand = commandHistManager.getNextCommand(prevCommand);
      }

      /*
       * Replace everything from the end of the last prompt to the end of the
       * last line with text from the appropriate point in command history
       */
      if (isContinuing) {
        component.replaceRange(cycledCommand,
                               component.getText()
                                   .lastIndexOf(promptContinuation)
                                   + promptContinuation.length(), component
                                   .getText().length());
      } else {
        component.replaceRange(cycledCommand,
                               component.getText().lastIndexOf(prompt)
                                   + prompt.length(), component.getText()
                                   .length());
      }
    }
  }

  /**
   * Prints a status message on a new line
   * 
   * @param msg
   *          The status message
   */
  public void printStatusMessage(String msg) {
    consoleArea.replaceSelection(msg + "\n");// + prompt);
  }

  /**
   * Prints the exception in the REPL Pane. Based on the Editor's statusError()
   * method.
   * 
   * @param e The exception
   */
  public void printStatusException(Exception e) {
    e.printStackTrace();
    // TODO: Print line number
    /*
     * Sketch sketch = replEditor.getREPLTempSketch(); if (e instanceof
     * SketchException) { SketchException re = (SketchException) e; if
     * (re.hasCodeLine()) { int line = re.getCodeLine(); // subtract one from
     * the end so that the \n ain't included if (line >=
     * textarea.getLineCount()) { // The error is at the end of this current
     * chunk of code, // so the last line needs to be selected. line =
     * textarea.getLineCount() - 1; if (textarea.getLineText(line).length() ==
     * 0) { // The last line may be zero length, meaning nothing to select. //
     * If so, back up one more line. line--; } } if (line < 0 || line >=
     * textarea.getLineCount()) { System.err.println("Bad error line: " + line);
     * } else { textarea.select(textarea.getLineStartOffset(line),
     * textarea.getLineStopOffset(line) - 1); } } }
     */

    /*
     * Since this will catch all Exception types, spend some time figuring out
     * which kind and try to give a better error message to the user.
     */
    String mess = e.getMessage();
    if (mess != null) {
      String javaLang = "java.lang.";
      if (mess.indexOf(javaLang) == 0) {
        mess = mess.substring(javaLang.length());
      }
      // The phrase "RuntimeException" isn't useful for most users
      String rxString = "RuntimeException: ";
      if (mess.startsWith(rxString)) {
        mess = mess.substring(rxString.length());
      }
      // This is just confusing for most PDE users (save it for Eclipse users)
      String illString = "IllegalArgumentException: ";
      if (mess.startsWith(illString)) {
        mess = mess.substring(illString.length());
      }

      printStatusError(mess);
    }
  }

  /**
   * Prints out an error message on the last line of the REPL Console,
   * deleting the prompt in the process. The prompt is then re-printed on the
   * next line. This is required since oftentimes, an error may be a run-time
   * error, which shows up only after the user has hit the enter key and when
   * the statement just entered is trying to be run.
   * @param mess The error message to print out
   */
  public void printStatusError(String mess) {
    int currPrefixLength = prefixLength;
    prefixLength = 0;
    int currPos = consoleArea.getCaretPosition();
    isContinuing = false;
    consoleArea.setSelectionStart(currPos - currPrefixLength);
    consoleArea.setSelectionEnd(currPos);
    printStatusMessage("Error: " + mess);
    consoleArea.setCaretPosition(consoleArea.getText().length());
    consoleArea.replaceSelection(prompt);
    consoleArea.setCaretPosition(consoleArea.getText().length());
    prefixLength = prompt.length();
    try {
      rowStartPosition = Math.max(rowStartPosition, Utilities
          .getRowStart(consoleArea, consoleArea.getCaretPosition()));
    } catch (BadLocationException e1) {
      e1.printStackTrace();
    }
  }

  /**
   * Handles an exception by printing the appropriate (simplified) message 
   * corresponding to an exception in the REPL Console and undoing the
   * statement that caused the exception (which, by virtue of how
   * things are structured in the REPL Mode, is always the last statement).
   * @param e The exception
   */
  public void handleException(Exception e) {
    printStatusException(e);
    undoLastStatement();
  }

  /**
   * Handles an error by printing the error in the REPL Console and undoing the
   * statement that caused the error (which, by virtue of how things are
   * structured in the REPL Mode, is always the last statement).
   * @param err The error
   */
  public void handleException(String err) {
    printStatusError(err);
    undoLastStatement();
  }

  /**
   * @return The last line in the REPL Console, i.e., the line which the cursor
   * is on
   */
  public String getLastLine() {
    // TODO: Is there a more efficient way of extracting the last line of code?
    int lineStartLocation;
    if (isContinuing) {
      lineStartLocation = consoleArea.getText().lastIndexOf(promptContinuation)
          + promptContinuation.length();
    } else {
      lineStartLocation = consoleArea.getText().lastIndexOf(prompt)
          + prompt.length();
    }
    return consoleArea.getText().substring(lineStartLocation);
  }

  /**
   * Convenience method to undo the last statement
   */
  public void undoLastStatement() {
    commandListManager.removePreviousStatement();
  }

  /*
   * Refer : http://stackoverflow.com/a/2750099/2427542 
   * Refer : http://stackoverflow.com/a/13375811/2427542
   */
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

  /**
   * Convenience <code>main()</code> method for easy debugging.
   */
  public static void main(String args[]) throws Exception {

    JTextArea textField = new JTextArea(">> ", 20, 40);
    CommandPromptPane cmdPromptPane = new CommandPromptPane(">> ", "...  ",
                                                            null, textField);
    textField.setNavigationFilter(cmdPromptPane);

    JFrame frame = new JFrame("Navigation Filter Example");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(textField);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}