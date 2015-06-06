package jm.mode.replmode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import processing.app.Editor;
import processing.app.Preferences;

/**
 * Console part of the REPL: The JPanel that is displayed when the user clicks
 * on the REPL tab.
 * 
 * @author Joel Ruben Antony Moniz
 */

// TODO : Add colorization
// DONE : Add prompt
// DONE : Ensure only appropriate parts are modifiable
// DONE: Set font based on user preference
// TODO: Update font as soon as user changes it in preferences window

public class REPLConsolePane extends JPanel {
  private static final long serialVersionUID = -7546489577830751456L;

  private static final String PROMPT = ">> ";
  private static final String PROMPT_CONTINUATION = "…    ";

  protected JScrollPane replScrollPane;
  protected JTextArea replInputArea;
  protected CommandHistory command;
  protected CommandPromptPane replInputPaneFilter;

  public REPLConsolePane(REPLEditor editor) {

    replInputArea = new JTextArea(PROMPT);
    command = new CommandHistory();
    
    // Set navigation filter
    replInputPaneFilter = new CommandPromptPane(PROMPT, PROMPT_CONTINUATION, editor, replInputArea, command);
    replInputArea.setNavigationFilter(replInputPaneFilter);

    // Appearance-related
    String fontName = Preferences.get("editor.font.family");
    int fontSize = Preferences.getInteger("editor.font.size");
    replInputArea.setBackground(Color.BLACK);
    replInputArea.setForeground(Color.LIGHT_GRAY);
    replInputArea.setCaretColor(Color.LIGHT_GRAY);
    replInputArea.setFont(new Font(fontName, Font.PLAIN, fontSize));

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

  protected void clear() {
    replInputArea.setText(PROMPT);
  }
}
