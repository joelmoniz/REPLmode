/*
  An REPL/Live Coding Mode for Processing- https://github.com/joelmoniz/REPLmode
  
  A mode for Processing - http://processing.org
  Developed during Google Summer of Code 2015
  
  Copyright (c) 2015 Joel Moniz
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
  USA.
 */
package jm.mode.replmode;

import java.util.ArrayList;

/**
 * Class to store the list of commands that the user has entered so far.
 * 
 * @author Joel Moniz
 */
public class CommandHistory {

  /**
   * Stores the line number of the command currently being displayed when the
   * user is cycling through commands.
   */
  int currentCycleCommand;

  /**
   * Store the line number of the previous <code>clear</code> command entered by
   * the user.
   * @deprecated This variable is no longer needed, 
   * but is kept for old time's sake.
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

  /**
   * Returns the previous command. Remembers where the user is in the 
   * cycling process.
   * @param currCommand What the user typed in just before cycling
   * @return The previous command in the cycling process
   */
  public String getPreviousCommand(String currCommand) {
    currentCycleCommand--;
    
    /*
     * either there is nothing to cycle through, or the user has reached the
     * very first command, or the user has just begun cycling
     */
    if (currentCycleCommand < 0) {
      if (commandHistList.size() == 0) {
        /* no commands to cycle through */
        currentCycleCommand = UNDEFINED_COMMAND_STEP;
        return "";
      } else if (currentCycleCommand == UNDEFINED_COMMAND_STEP - 1) {
        /*
         * this happens the first time the user tries to access a previous
         * command- at the start of the cycling process
         */
        currentCycleCommand = commandHistList.size() - 1;
        this.currentCommand = currCommand;
        return commandHistList.get(currentCycleCommand);
      } else {
        /*
         * avoid multiple up-arrow pressing after reaching top of list from
         * causing problems
         */
        currentCycleCommand = 0;
        return commandHistList.get(0);
      }
    } else {
      if (currentCycleCommand == commandHistList.size() - 1) {
        /*
         * done so that when the user modifies something, and decides that 
         * (s)he wants to cycle again, the modifications aren't lost
         */
        this.currentCommand = currCommand;
      }
      return commandHistList.get(currentCycleCommand);
    }
  }

  /**
   * Returns the next command. Remembers where the user is in the 
   * cycling process.
   * @param currCommand What the user typed in just before cycling
   * @return The next command in the cycling process
   */
  public String getNextCommand(String currCommand) {
    currentCycleCommand++;
    /*
     * when crossing the last command in history
     */
    if (currentCycleCommand >= commandHistList.size()) {
      if (commandHistList.size() == 0) {
        /* no commands entered yet */
        currentCycleCommand = UNDEFINED_COMMAND_STEP;
        return "";
      } else {
        if (currentCycleCommand > commandHistList.size()) {
          /*
           * done so that when the user modifies something, and presses the 
           * down arrow key, the modifications aren't lost
           */
          this.currentCommand = currCommand;
        }
        /*
         * avoid multiple down-arrow pressing after reaching bottom of list from
         * causing problems
         */
        currentCycleCommand = commandHistList.size();
        return currentCommand;
      }
    } else if (currentCycleCommand == 0) {
      /* first step of cycle, non-empty list */
      this.currentCommand = currCommand;
      currentCycleCommand = commandHistList.size();
      return currCommand;
    } else {
      /* cycling somewhere in the middle of command history */
      return commandHistList.get(currentCycleCommand);
    }
  }

  /**
   * Resets the cycling process, so that the previous command in the cycle will
   * be the last command entered by the user.
   */
  public void resetCommandCycle() {
    currentCycleCommand = UNDEFINED_COMMAND_STEP;
    currentCommand = "";
  }

  /**
   * Inserts a command into the command history. To be called, for example, when
   * the user hits the enter key.
   * 
   * @param cmd The command/code to be inserted into the history
   */
  public void insertCommand(String cmd) {
    resetCommandCycle();

    if (cmd != null && !cmd.isEmpty()) {
      commandHistList.add(cmd);
    }

    if (cmd.equals(CommandList.CLEAR_COMMAND)) {
      previousClearLine = commandHistList.size();
    }
  }

}
