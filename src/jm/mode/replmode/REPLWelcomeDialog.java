package jm.mode.replmode;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Singleton class responsible for displaying a modal dialog that gives the user
 * a quick overview of the REPL Mode, and allows the user to choose whether or
 * not to show the dialog each time the Mode starts.
 * 
 * @author Joel Moniz
 */
public class REPLWelcomeDialog {

  private JCheckBox dontShowEachStartupCheckbox;
  private JPanel cbPanel;
  private String msg;
  private JLabel msgLabel;
  private boolean showEachStartup;
  
  private static REPLWelcomeDialog dialog;
  
  /**
   * Name of the file used to keep track of whether or not the user wants the
   * Welcome dialog to be shown at startup time.
   */
  public static final String DONT_SHOW_AT_STARTUP_FILE = "noshow.repl";
  
  /**
   * Display the welcome dialog at startup time
   */
  public static void showWelcome() {
    Thread t = new Thread (new Runnable() {
      
      @Override
      public void run() {
        
        try {
          // Time for PDE to setup
          Thread.sleep(2500);
          if (dialog == null) {
            dialog = new REPLWelcomeDialog();
          }    
          dialog.displayWelcomeDialog();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    t.start();
  }
  
  /**
   * Display the welcome dialog from the Help menu, post startup
   */
  public static void showHelp() {
    if (dialog == null) {
      dialog = new REPLWelcomeDialog();
    }    
    dialog.displayHelpDialog();
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
    dontShowEachStartupCheckbox = 
        new JCheckBox("Do not show this message at startup");
    cbPanel.add(dontShowEachStartupCheckbox);
    
    showEachStartup = isShowingEachStartup();
    dontShowEachStartupCheckbox.setSelected(!showEachStartup);
  }
  
  /**
   * @return <code>true</code> if the welcome dialog is to be displayed to the
   * user each time the REPL Mode is started, false otherwise 
   */
  private boolean isShowingEachStartup() {
    URL url = 
        REPLWelcomeDialog.class.getProtectionDomain().getCodeSource().getLocation();
    File dir = null;
    try {
      dir = new File(url.toURI());
      dir = dir.getParentFile().getParentFile();
//      List<String> dirList = Arrays.asList(dir.list());
      return 
          !(new File(dir, DONT_SHOW_AT_STARTUP_FILE).exists());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return true;
  }
  
  /**
   * Function to display the welcome dialog at startup time.
   * Ensures that the dialog is displayed on the EDT.
   */
  private void displayWelcomeDialog() {
    if (!isShowingEachStartup()) {
      return;
    }
    javax.swing.SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        JOptionPane.showMessageDialog(new JFrame(), dialog.cbPanel, 
                                      "Welcome to the REPL Mode",
                                      JOptionPane.PLAIN_MESSAGE, null);
        if (dontShowEachStartupCheckbox.isSelected()) {
          handleDontShowCheckbox(false);
          showEachStartup = false;
        }
      }
    });
  }

  /**
   * Function to display the welcome dialog from the Help menu, post startup.
   * Ensures that the dialog is displayed on the EDT.
   */
  private void displayHelpDialog() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        JOptionPane.showMessageDialog(new JFrame(), dialog.cbPanel,
                                      "REPL Mode- A quick guide",
                                      JOptionPane.PLAIN_MESSAGE, null);
        if (dontShowEachStartupCheckbox.isSelected() == showEachStartup) {
          showEachStartup = !dontShowEachStartupCheckbox.isSelected();
          handleDontShowCheckbox(showEachStartup);
        }
      }
    });
  }

  /**
   * Handles the creation or removal of the marker file responsible for telling
   * the Mode whether or not to display the welcome screen at each startup.
   * Since this is generally called once the modal welcome dialog is closed on
   * the EDT, the creation/deletion of the file is done on a new thread.
   * 
   * @param showEachStartup
   *          Whether the marker file is to be created (to indicate that the
   *          welcome screen is not be be shown at startup time), or deleted.
   */
  private void handleDontShowCheckbox(boolean showEachStartup) {
    Thread t = new Thread(new Runnable() {
      
      @Override
      public void run() {
        if (showEachStartup) {
          unsetDontShowEachStartupFile();
        }
        else {
          setDontShowEachStartupFile();
        }
      }
    });
    t.start();
  }

  /**
   * Creates the marker file which indicates that the welcome screen is 
   * not be be shown at startup time
   */
  private void setDontShowEachStartupFile() {
    URL url = 
        REPLWelcomeDialog.class.getProtectionDomain().getCodeSource().getLocation();
    File dir = null;
    try {
      dir = new File(url.toURI());
      dir = dir.getParentFile().getParentFile();
      File noshow = new File(dir, DONT_SHOW_AT_STARTUP_FILE);
      noshow.createNewFile();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Deletes the marker file which indicates that the welcome screen is 
   * not be be shown at startup time
   */
  private void unsetDontShowEachStartupFile() {
    URL url = 
        REPLWelcomeDialog.class.getProtectionDomain().getCodeSource().getLocation();
    File dir = null;
    try {
      dir = new File(url.toURI());
      dir = dir.getParentFile().getParentFile();
      File noshow = new File(dir, DONT_SHOW_AT_STARTUP_FILE);
      noshow.delete();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args) {
    showWelcome();
  }

}
