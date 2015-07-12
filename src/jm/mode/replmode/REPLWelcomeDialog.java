package jm.mode.replmode;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class REPLWelcomeDialog {

  private JCheckBox showEachStartup;
  private JPanel cbPanel;
  private String msg;
  private JLabel msgLabel;
  
  private static REPLWelcomeDialog dialog;
  
  public static void show() {
    if (dialog == null) {
      dialog = new REPLWelcomeDialog();
    }    
    dialog.displayWelcomeDialog();
  }

  private REPLWelcomeDialog() {
    cbPanel = new JPanel();
    cbPanel.setLayout(new BoxLayout(cbPanel, BoxLayout.Y_AXIS));
    msg = "<html><h3><strong>About the REPL Mode</strong>"
        + "</h3><p>The REPL Mode is&nbsp;very similar to the Java Mode, "
        + "and supports everything that the Java Mode does.<br>However, it "
        + "aims to bring 2 new features to the table:</p><ul><li>"
        + "<strong>REPL Console</strong>: This mode contains an REPL "
        + "Console for easy, convenient prototyping</li>"
        + "<li><strong>Hot swapping</strong>: This mode permits restartless "
        + "hot-swapping of a sketch, saving developer time</li></ul>"
        + "<h3><strong>Using the REPL Mode- a 2 minute guide</strong></h3>"
        + "<p>Using the additional features of the mode is simple:</p><ul>"
        + "<li><strong>REPL Console</strong><ul><li>First, initialize "
        + "the console with the init keyword.</li><li>Next, type in "
        + "absolutely anything into the console, and watch the output come "
        + "alive!</li><li>To split a single function call into multiple "
        + "lines, press the Enter key after a comma.</li><li>For more "
        + "information about the init keyword, and for a list of keywords "
        + "available, type help.</li></ul></li><li><strong>Hot swapping"
        + "</strong><ul><li>Create a new sketch, type stuff in, and save it. "
        + "Alternately, open an existing sketch.</li><li>Run the sketch, and "
        + "leave the sketch window open.</li><li>Make required changes to the "
        + "sketch, and voil&agrave;! Hot swapped!</li></ul></li></ul></html>";
    msgLabel = new JLabel(msg);
    Font msgFont = msgLabel.getFont();
    msgLabel.setFont(new Font(msgFont.getFontName(), Font.PLAIN, 
                              msgFont.getSize()));
    cbPanel.add(msgLabel);
    showEachStartup = new JCheckBox("Do not show this message at startup");
    cbPanel.add(showEachStartup);
  }
  
  private void displayWelcomeDialog() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        JOptionPane.showMessageDialog(new JFrame(), dialog.cbPanel, 
                                      "Welcome to the REPL Mode",
                                      JOptionPane.PLAIN_MESSAGE, null);        
      }
    });
  }
  
  public static void main(String[] args) {
    show();
  }

}
