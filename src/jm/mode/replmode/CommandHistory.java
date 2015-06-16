package jm.mode.replmode;

import java.util.ArrayList;

/**
 * Class to store the list of commands that the user has entered so far.
 * 
 * @author Joel
 *
 */
public class CommandHistory {

  /**
   * Stores the line number of the command currently being displayed when the
   * user is cycling through commands.
   */
  int currentCycleCommand;
  
  /**
   * Store the line number of the previous <b>clear</b> command entered by the user.
   */
  int previousClearLine;
  
  /**
   * Store all the commands themselves.
   */
  ArrayList<String> commandHistList;
  
  /**
   * Command user is currently entering
   */
  String currentCommand;

  public static final int UNDEFINED_COMMAND_STEP = -1;

  
  public CommandHistory() {
    currentCycleCommand = UNDEFINED_COMMAND_STEP;
    previousClearLine = 0;
    currentCommand = "";
    
    commandHistList = new ArrayList<>();
  }
  
  public String getPreviousCommand(String currCommand) {
    currentCycleCommand--;
    if (currentCycleCommand < 0) {
      if (commandHistList.size() == 0) {
        currentCycleCommand = UNDEFINED_COMMAND_STEP;
        return "";
      }
      else if (currentCycleCommand == UNDEFINED_COMMAND_STEP - 1) {
        currentCycleCommand = commandHistList.size() - 1;
        this.currentCommand = currCommand;
//        System.out.println(commandHistList.get(currentCycleCommand));
        return commandHistList.get(currentCycleCommand);
      }
      else {
        // avoid multiple up-arrow pressing after reaching 
        // top of list from causing problems
        currentCycleCommand = 0;
        return commandHistList.get(0);
      }
    }
    else {
      if (currentCycleCommand == commandHistList.size() - 1) {
        this.currentCommand = currCommand;
      }
      return commandHistList.get(currentCycleCommand);
    }
  }
  
  public String getNextCommand(String currCommand) {
    currentCycleCommand++;
    if (currentCycleCommand >= commandHistList.size()) {
      if (commandHistList.size() == 0) {
        currentCycleCommand = UNDEFINED_COMMAND_STEP;
        return "";
      }
      else {
        if (currentCycleCommand > commandHistList.size()) {
          this.currentCommand = currCommand;
        }
        // avoid multiple down-arrow pressing after reaching 
        // bottom of list from causing problems
        currentCycleCommand = commandHistList.size();
        return currentCommand;
      }
    }
    else if (currentCycleCommand == 0) { // first step of cycle, non-empty list
      this.currentCommand = currCommand;
      currentCycleCommand = commandHistList.size();
      return currCommand;
    }
    else {
      return commandHistList.get(currentCycleCommand);
    }
  }
  
  public void resetCommandCycle() {
    currentCycleCommand = UNDEFINED_COMMAND_STEP;
    currentCommand = "";
  }
  
  public void insertCommand(String cmd) {
    resetCommandCycle();
    
    if (cmd != null && !cmd.isEmpty()) {
      commandHistList.add(cmd);
    }    
    
    if (cmd.equals(CommandList.CLEAR_COMMAND)) {
      previousClearLine = commandHistList.size();
    }
  }
  
  public String extractCommandBlock() {
    StringBuilder cmdList = new StringBuilder();
    
    for (int i=previousClearLine; i<commandHistList.size(); i++) {
      cmdList.append(commandHistList.get(i));
      
      if (i != commandHistList.size()) {
        cmdList.append('\n');
      }
    }
    return cmdList.toString();
  }
  
  public String toSketch() {
    StringBuilder sketchCode = new StringBuilder();
    sketchCode.append("void setup() {\n");
    sketchCode.append(extractCommandBlock());
    sketchCode.append("\n}\n\nvoid draw() {}");
    System.out.println(sketchCode.toString());
    return sketchCode.toString();
  }
}
