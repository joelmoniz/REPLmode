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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import processing.app.Preferences;

/**
 * Console part of the REPL: The JPanel that is displayed when the user clicks
 * on the REPL tab.
 * 
 * @author Joel Ruben Antony Moniz
 */
public class REPLConsolePane extends JPanel {
  private static final long serialVersionUID = -7546489577830751456L;

  /**
   * The string representing the prompt
   */
  private static final String PROMPT = ">> ";

  /**
   * The string representing a continuing prompt for when the user is
   * in the process of entering 
   */
  private static final String PROMPT_CONTINUATION = "...    ";

  protected JScrollPane replScrollPane;

  protected JTextArea replInputArea;

  /**
   * The REPL Console Pane's Navigation Filter, responsible for handling
   * keystrokes- ensuring the up/down keys cycle through command history,
   * the backspace/delete keys don't delete the prompt away, and the Enter
   * key takes the user to the next line (with the appropriate action being
   * performed). 
   */
  protected CommandPromptPane replInputPaneFilter;

  public REPLConsolePane(REPLEditor editor) {

    replInputArea = new JTextArea(PROMPT);

    /* Set navigation filter */
    replInputPaneFilter = new CommandPromptPane(PROMPT, PROMPT_CONTINUATION,
                                                editor, replInputArea);
    replInputArea.setNavigationFilter(replInputPaneFilter);

    /* Appearance-related */
    String fontName = Preferences.get("editor.font.family");
    int fontSize = Preferences.getInteger("editor.font.size");
    replInputArea.setBackground(Color.BLACK);
    replInputArea.setForeground(Color.LIGHT_GRAY);
    replInputArea.setCaretColor(Color.LIGHT_GRAY);
    replInputArea.setFont(new Font(fontName, Font.PLAIN, fontSize));

    /* Setup scroll pane */
    replScrollPane = new JScrollPane(replInputArea);
    replScrollPane.setBorder(new EtchedBorder());
    replScrollPane
        .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    this.setLayout(new BorderLayout());
    this.add(replScrollPane);
  }

  /**
   * Method to make the cursor automatically appear when the REPL Console is
   * selected
   */
  public void requestFocus() {
    replInputArea.requestFocusInWindow();
  }

  /**
   * 
   * @return Returns the NavigationFilter associated with this REPLConsolePane
   */
  public CommandPromptPane getCommandPromptPane() {
    return replInputPaneFilter;
  }
}
