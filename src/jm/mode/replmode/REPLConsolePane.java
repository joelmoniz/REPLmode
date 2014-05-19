package jm.mode.replmode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Utilities;

import processing.app.Editor;

/**
 * Console part of the REPL: The JPanel that is displayed when the user clicks
 * on the REPL tab.
 * 
 * @author Joel Ruben Antony Moniz
 */

// TODO : Add colorization
// DONE : Add prompt
// DONE : Ensure only appropriate parts are modifiable
// TODO: Set font based on user preference

@SuppressWarnings("serial")
public class REPLConsolePane extends JPanel implements KeyListener,
		CaretListener, MouseListener {

	private static final String PROMPT = "REPL>> ";

	protected JScrollPane replScrollPane;

	protected JEditorPane replInputPane;
	protected JEditorPane replPromptPane;

	private int previousCaretLine;
	private int previousCaretPosition;

	private boolean isClearing;

	public REPLConsolePane(Editor editor) {

		replInputPane = new JEditorPane();

		// Appearance-related
		replInputPane.setBackground(Color.BLACK);
		replInputPane.setForeground(Color.LIGHT_GRAY);
		this.replInputPane.setCaretColor(Color.LIGHT_GRAY);

		// Listener-related
		// Removing mouse listeners, adding my own
		for (MouseListener m : replInputPane.getMouseListeners())
			replInputPane.removeMouseListener(m);
		for (MouseMotionListener m : replInputPane.getMouseMotionListeners())
			replInputPane.removeMouseMotionListener(m);
		replInputPane.addMouseListener(this);
		replInputPane.addKeyListener(this);
		replInputPane.addCaretListener(this);

		previousCaretLine = 1;
		previousCaretPosition = 0;
		isClearing = false;

		replPromptPane = new JEditorPane();

		replPromptPane.setEditable(false);
		replPromptPane.setText("REPL>> ");
		replPromptPane.setPreferredSize(new Dimension(getFontMetrics(getFont())
				.stringWidth(PROMPT + "  "), this.getHeight()));

		replPromptPane.setBackground(Color.BLACK);
		replPromptPane.setForeground(Color.LIGHT_GRAY);

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
		promptInputPanel.add(replInputPane, c);// , BorderLayout.EAST);

		replScrollPane = new JScrollPane();
		replScrollPane.setBorder(new EtchedBorder());

		replScrollPane.setViewportView(promptInputPanel);
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
		replInputPane.grabFocus();
	}

	protected void clear() {
		isClearing = true;
		replPromptPane.setText("");
		replPromptPane.setText(PROMPT);
		replInputPane.setText("");
		previousCaretLine = 1;
		previousCaretPosition = 0;
		isClearing = false;
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
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (isClearing)
			return;
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			int row = getRow(replInputPane);
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
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void caretUpdate(CaretEvent c) {
		if (isClearing)
			return;
		if (getRow(replInputPane) < previousCaretLine)
			replInputPane.setCaretPosition(previousCaretPosition);
		else
			previousCaretPosition = c.getDot();

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (!replInputPane.hasFocus())
			requestFocus();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}
}
