package jm.mode.replmode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;

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

@SuppressWarnings("serial")
public class REPLConsolePane extends JPanel/* implements KeyListener,
		CaretListener, MouseListener */{

	private static final String PROMPT = ">> ";

	protected JScrollPane replScrollPane;
	
	protected JTextArea replInputArea;
	protected CommandPromptPane replInputPaneFilter;
//	protected JEditorPane replPromptPane;
/*
	private int previousCaretLine;
	private int previousCaretPosition;

	private boolean isClearing;
*/
	public REPLConsolePane(Editor editor) {

	  replInputArea  = new JTextArea(PROMPT);
		replInputPaneFilter = new CommandPromptPane(PROMPT, replInputArea);
		replInputArea.setNavigationFilter(replInputPaneFilter);
		
		String fontName = Preferences.get("editor.font.family");
		int fontSize = Preferences.getInteger("editor.font.size");

		// Appearance-related
		replInputArea.setBackground(Color.BLACK);
		replInputArea.setForeground(Color.LIGHT_GRAY);
		replInputArea.setCaretColor(Color.LIGHT_GRAY);
		replInputArea.setFont(new Font(fontName,
				Font.PLAIN, fontSize));

		// Listener-related
		// Removing mouse listeners, adding my own
		/*		for (MouseListener m : replInputArea.getMouseListeners())
			replInputArea.removeMouseListener(m);
		for (MouseMotionListener m : replInputArea.getMouseMotionListeners())
			replInputArea.removeMouseMotionListener(m);
		replInputArea.addMouseListener(this);
		replInputArea.addKeyListener(this);
		replInputArea.addCaretListener(this);

		previousCaretLine = 1;
		previousCaretPosition = 0;
		isClearing = false;

		replPromptPane = new JEditorPane();

		replPromptPane.setEditable(false);
		replPromptPane.setText(PROMPT);

		replPromptPane.setBackground(Color.BLACK);
		replPromptPane.setForeground(Color.LIGHT_GRAY);
		replPromptPane.setFont(new Font(fontName,
				Font.PLAIN, fontSize));
		
		replPromptPane.setPreferredSize(new Dimension((int)(getFontMetrics(getFont())
				.stringWidth(PROMPT)*fontSize/6.0f), this.getHeight()));
		replPromptPane.setMinimumSize(new Dimension((int)(getFontMetrics(getFont())
				.stringWidth(PROMPT)*fontSize/6.0f), this.getHeight()));
		replPromptPane.setMaximumSize(new Dimension((int)(getFontMetrics(getFont())
				.stringWidth(PROMPT)*fontSize/6.0f), this.getHeight()));*/
//		replPromptPane.setBorder(BorderFactory.createLineBorder(Color.WHITE));
/*
		JPanel promptInputPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.VERTICAL;
		c.weighty = 1.0;
		c.weightx = 0;
		promptInputPanel.add(replPromptPane, c);// , BorderLayout.WEST);

		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.BOTH;
		promptInputPanel.add(replInputArea, c);// , BorderLayout.EAST);
*/
		replScrollPane = new JScrollPane(replInputArea);
		replScrollPane.setBorder(new EtchedBorder());

//		replScrollPane.setViewportView(promptInputPanel);
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
		replInputArea.grabFocus();
	}

	protected void clear() {
//		isClearing = true;
//		replPromptPane.setText("");
//		replPromptPane.setText(PROMPT);
		replInputArea.setText(PROMPT);
//		previousCaretLine = 1;
//		previousCaretPosition = 0;
//		isClearing = false;
	}

	// Refer : http://stackoverflow.com/a/2750099/2427542
	// Refer : http://stackoverflow.com/a/13375811/2427542

	/**
	 * Use to get line number at which caret is placed.
	 * 
	 * Code adapted from http://java-sl.com/tip_row_column.html
	 * 
	 * @param editor
	 *            : The JEditorPane
	 * @return Row number
	 */
	public static int getRow(JEditorPane editor) {
		int pos = editor.getCaretPosition();
		int rn = (pos == 0) ? 1 : 0;
		try {
			int offs = pos;
			while (offs > 0) {
				offs = Utilities.getRowStart(editor, offs) - 1;
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
	 * @param editor
	 *            : The JEditorPane
	 * @return Column number
	 */
	public static int getColumn(JEditorPane editor) {
		int pos = editor.getCaretPosition();
		try {
			return pos - Utilities.getRowStart(editor, pos) + 1;
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}
/*
	private void appendText(JEditorPane editor, String string) {
		Document doc = editor.getDocument();

		// SimpleAttributeSet keyWord = new SimpleAttributeSet();
		// StyleConstants.setForeground(keyWord, Color.WHITE);
		// StyleConstants.setBackground(keyWord, Color.BLACK);

		try {
			doc.insertString(doc.getLength(), string, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	} */
/*
	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (isClearing)
			return;
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			int row = getRow(replInputArea);
			System.out.println(row);
			for (int i = previousCaretLine; i < row; i++) {
				appendText(replPromptPane, "\n");
				System.out.println("\n");
			}
			previousCaretLine = row;
			appendText(replPromptPane, PROMPT);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void caretUpdate(CaretEvent c) {
		if (isClearing)
			return;
		if (getRow(replInputArea) < previousCaretLine)
			replInputArea.setCaretPosition(previousCaretPosition);
		else
			previousCaretPosition = c.getDot();

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (!replInputArea.hasFocus())
			requestFocus();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}*/
}
