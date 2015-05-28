package jm.mode.replmode;

import java.awt.CardLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import processing.app.Base;
import processing.app.EditorFooter;
import processing.app.EditorState;
import processing.app.Mode;
import processing.app.Sketch;
import processing.mode.java.JavaEditor;

/**
 * Main View Class. Handles the editor window including tool bar and menu. Has
 * access to the Sketch. Primarily used to display the REPL/Console toggle
 * buttons and to display the console/REPL pane appropriately. Adapted from
 * DebugEditor class of processing-experimental.
 * 
 * @author Martin Leopold <m@martinleopold.com>
 * @author Manindra Moharana &lt;me@mkmoharana.com&gt;
 * @author Joel Ruben Antony Moniz
 * 
 */

@SuppressWarnings("serial")
public class REPLEditor extends JavaEditor {

	/**
	 * Panel with card layout which contains the p5 console and REPL panes
	 */
	protected JPanel consoleREPLPane;

	/**
	 * Show Console button
	 */
	protected REPLConsoleToggle btnShowConsole;

	/**
	 * Show REPL button
	 */
	protected REPLConsoleToggle btnShowREPL;

	/**
	 * REPL/Console Pane
	 */
	protected REPLConsolePane replConsole;

	/**
	 * Clear REPL/Console panes button
	 */
	protected ConsoleButtons consoleOptions;
	
	protected Sketch replTempSketch;
	protected File untitledFolderLocation;

	protected REPLEditor(Base base, String path, EditorState state, Mode mode) {
		super(base, path, state, mode);
		
		try {
      untitledFolderLocation = Base.createTempFolder("untitled", "repl", null);
      
//      final String temp = path.substring(path.substring(0, path.lastIndexOf('\\'))
//      .lastIndexOf('\\'), path.lastIndexOf('\\')+1);
      final File tempFile = File.createTempFile("tmp", ".repl", untitledFolderLocation);
      replTempSketch = new Sketch(tempFile.getAbsolutePath(), this);
      
      Thread one = new Thread() {
        public void run() {
          try {
            Thread.sleep(2000);
            System.out.println(sketch.getFolder());
            System.out.println(tempFile.getAbsolutePath());
            System.out.println(sketch.getCode(0).getFileName());
            System.out.println(replTempSketch.getCodeFolder().getAbsolutePath());
            System.out.println(replTempSketch.getCodeCount());
//            for (String f : replTempSketch.getCodeFolder().list()) {
//              System.out.println(f);
//            }
          } catch (InterruptedException v) {
            System.out.println(v);
          }
        }
      };
      one.start();
    } catch (IOException e) {
      e.printStackTrace();
    }

//		replConsole = new REPLConsolePane(this);
//		addREPLConsoleUI();
	}

  /**
   * Method to add a footer at the base of the editor with tabs to display the
   * Console, Errors pane and the REPL Console.
   */
  @Override
  public EditorFooter createFooter() {
    replConsole = new REPLConsolePane(this);
    
    EditorFooter footer = super.createFooter();
    footer.addPanel("REPL", replConsole);

    replConsole.addComponentListener(new ComponentAdapter() {
        @Override
        public void componentShown(ComponentEvent e) {
          replConsole.requestFocus();
        }
      });

    return footer;
  }
	/*
	private void addREPLConsoleUI() {

		// Adding toggle console button
		consolePanel.remove(2);
		JPanel lineStatusPanel = new JPanel();
		lineStatusPanel.setLayout(new BorderLayout());
		btnShowConsole = new REPLConsoleToggle(this, REPLConsoleToggle.CONSOLE,
				lineStatus.getHeight());
		btnShowREPL = new REPLConsoleToggle(this, REPLConsoleToggle.REPL,
				lineStatus.getHeight());
		btnShowConsole.addMouseListener(btnShowConsole);
		btnShowREPL.addMouseListener(btnShowREPL);

		consoleOptions = new ConsoleButtons(ConsoleButtons.CLEAR,
				lineStatus.getHeight(), console, replConsole);
		consoleOptions.addMouseListener(consoleOptions);
		consoleOptions.addMouseMotionListener(consoleOptions);

		JPanel toggleButtonPanel = new JPanel();
		toggleButtonPanel.setLayout(new BoxLayout(toggleButtonPanel,
				BoxLayout.LINE_AXIS));

		toggleButtonPanel.add(consoleOptions);
		toggleButtonPanel.add(btnShowREPL);
		toggleButtonPanel.add(btnShowConsole);
		lineStatusPanel.add(toggleButtonPanel, BorderLayout.EAST);

		lineStatus.setBounds(0, 0, toggleButtonPanel.getX() - 1,
				toggleButtonPanel.getHeight());
		lineStatusPanel.add(lineStatus);
		consolePanel.add(lineStatusPanel, BorderLayout.SOUTH);
		lineStatusPanel.repaint();

		// Adding JPanel with CardLayout for Console/REPL Toggle
		consolePanel.remove(1);
		consoleREPLPane = new JPanel(new CardLayout());
		consoleREPLPane.add(replConsole, REPLConsoleToggle.REPL);
		consoleREPLPane.add(console, REPLConsoleToggle.CONSOLE);
		consolePanel.add(consoleREPLPane, BorderLayout.CENTER);

		showConsoleOrREPL(REPLConsoleToggle.CONSOLE);
	}
  */

	public void showConsoleOrREPL(String buttonName) {
		CardLayout cl = (CardLayout) consoleREPLPane.getLayout();
		cl.show(consoleREPLPane, buttonName);
		if (REPLConsoleToggle.REPL.equals(buttonName))
			replConsole.requestFocus();
	}

}
