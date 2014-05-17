package jm.mode.replmode;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JPanel;

import processing.app.Base;
import processing.app.EditorState;
import processing.app.Mode;
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

	protected REPLConsolePane replConsole;

	protected REPLEditor(Base base, String path, EditorState state, Mode mode) {
		super(base, path, state, mode);

		replConsole = new REPLConsolePane(this);

		addREPLConsoleUI();
	}

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

		JPanel toggleButtonPanel = new JPanel(new BorderLayout());
		toggleButtonPanel.add(btnShowConsole, BorderLayout.EAST);
		toggleButtonPanel.add(btnShowREPL, BorderLayout.WEST);
		lineStatusPanel.add(toggleButtonPanel, BorderLayout.EAST);
		lineStatus.setBounds(0, 0, toggleButtonPanel.getX() - 1,
				toggleButtonPanel.getHeight());
		lineStatusPanel.add(lineStatus);
		consolePanel.add(lineStatusPanel, BorderLayout.SOUTH);
		lineStatusPanel.repaint();

		// replScrollPane.add(replConsole);

		// Adding JPanel with CardLayout for Console/REPL Toggle
		consolePanel.remove(1);
		consoleREPLPane = new JPanel(new CardLayout());
		consoleREPLPane.add(replConsole, REPLConsoleToggle.REPL);
		consoleREPLPane.add(console, REPLConsoleToggle.CONSOLE);
		consolePanel.add(consoleREPLPane, BorderLayout.CENTER);

		showConsoleOrREPL(REPLConsoleToggle.CONSOLE);
	}

	public void showConsoleOrREPL(String buttonName) {
		CardLayout cl = (CardLayout) consoleREPLPane.getLayout();
		cl.show(consoleREPLPane, buttonName);
	}

}
