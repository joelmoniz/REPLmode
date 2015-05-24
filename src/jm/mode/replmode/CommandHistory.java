package jm.mode.replmode;

import java.util.ArrayList;

/**
 * Class to store the list of commands that the user has entered so far, and to
 * perform operations related to the commands themselves.
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
  ArrayList<String> commandList;
  
  /**
   * Command user is currently entering
   */
  String currentCommand;
  
  public static final String CLEAR_COMMAND = "clear";
  public static final int UNDEFINED_COMMAND_STEP = -1;
  
  public CommandHistory() {
    currentCycleCommand = UNDEFINED_COMMAND_STEP;
    previousClearLine = -1;
    currentCommand = "";
    
    commandList = new ArrayList<>();
  }
  
  public String getPreviousCommand(String currCommand) {
    currentCycleCommand--;
    if (currentCycleCommand < 0) {
      if (commandList.size() == 0) {
        currentCycleCommand = UNDEFINED_COMMAND_STEP;
        return "";
      }
      else if (currentCycleCommand == UNDEFINED_COMMAND_STEP - 1) {
        currentCycleCommand = commandList.size() - 1;
        this.currentCommand = currCommand;
        return commandList.get(currentCycleCommand);
      }
      else {
        // avoid multiple up-arrow pressing after reaching 
        // top of list from causing problems
        currentCycleCommand = 0;
        return commandList.get(0);
      }
    }
    else {
      return commandList.get(currentCycleCommand);
    }
  }
  
  public String getNextCommand(String currCommand) {
    currentCycleCommand++;
    if (currentCycleCommand >= commandList.size()) {
      if (commandList.size() == 0) {
        currentCycleCommand = UNDEFINED_COMMAND_STEP;
        return "";
      }
      else {
        // avoid multiple down-arrow pressing after reaching 
        // bottom of list from causing problems
        currentCycleCommand = commandList.size();
        return currentCommand;
      }
    }
    else if (currentCycleCommand == 0) { // first step of cycle, non-empty list
      this.currentCommand = currCommand;
      currentCycleCommand = commandList.size();
      return currCommand;
    }
    else {
      return commandList.get(currentCycleCommand);
    }
  }
  
  public void resetCommandCycle() {
    currentCycleCommand = UNDEFINED_COMMAND_STEP;
    currentCommand = "";
  }
  
  public void insertCommand(String cmd) {
    resetCommandCycle();
    commandList.add(cmd);
    
    if (cmd.equals(CLEAR_COMMAND)) {
      previousClearLine = commandList.size();
    }
  }
  
  public String extractCommandBlock() {
    StringBuilder cmdList = new StringBuilder();
    
    for (int i=previousClearLine; i<commandList.size(); i++) {
      cmdList.append(commandList.get(i));
      
      if (i != commandList.size()) {
        cmdList.append('\n');
      }
    }
    
    return cmdList.toString();
  }
}
