package jm.mode.replmode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import processing.app.Editor;

/**
 * Console part of the REPL: The JPanel that is displayed when the user clicks
 * on the REPL tab.
 * 
 * @author Joel Ruben Antony Moniz
 */

// TODO : Add colorization
// TODO : Add prompt
// TODO : Ensure only appropriate parts are modifiable

@SuppressWarnings("serial")
public class REPLConsolePane extends JPanel implements KeyListener,
		CaretListener {

	private static final String PROMPT = "REPL>> ";

	private static final String SPACE = "\t";

	protected JScrollPane replScrollPane;

	protected JEditorPane replInputPane;
	protected JEditorPane replPromptPane;

	private int previousHorizCaretPosition;

	private int previousCaretLine;

	private boolean isClearing;

	private int height;

	public REPLConsolePane(Editor editor) {

		replInputPane = new JEditorPane();
		// replInputPane.addMouseListener(this);
		// replInputPane.setEditorKit(new WrapEditorKit());
		replInputPane.setBackground(Color.BLACK);
		Border paneBorder = BorderFactory.createLineBorder(Color.WHITE);
		// replInputPane.setBorder(BorderFactory.createCompoundBorder(paneBorder,
		// BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		replInputPane.setForeground(Color.LIGHT_GRAY);
		// replInputPane.addKeyListener(this);
		// replInputPane.addCaretListener(this);
		// previousHorizCaretPosition = 7;
		// previousCaretLine = 0;
		// isClearing = false;
		// showPrompt();

		replPromptPane = new JEditorPane();
		replPromptPane.setEditable(false);
		replPromptPane.setText("REPL>> ");
		/*
		 * {
		 * 
		 * @Override public Dimension getPreferredSize() { return new Dimension
		 * (getFontMetrics(getFont()).stringWidth (PROMPT),this.getHeight()); }
		 * 
		 * @Override public Dimension getMaximumSize() { return
		 * getPreferredSize(); }
		 * 
		 * @Override public Dimension getMinimumSize() { return
		 * getPreferredSize(); }
		 * 
		 * };
		 */
		// replPromptPane.setEditorKit(new WrapEditorKit());
		replPromptPane.setPreferredSize(new Dimension(getFontMetrics(getFont())
				.stringWidth(PROMPT + "  "), this.getHeight()));
		// replPromptPane.setMaximumSize(new
		// Dimension(getFontMetrics(getFont()).stringWidth(PROMPT),this.getHeight()));
		// replPromptPane.setMinimumSize(new
		// Dimension(getFontMetrics(getFont()).stringWidth(PROMPT),this.getHeight()));
		replPromptPane.setBackground(Color.BLACK);
		replPromptPane.setForeground(Color.LIGHT_GRAY);

		JPanel replPromptPanel = new JPanel();
		replPromptPanel.setLayout(new BoxLayout(replPromptPanel,
				BoxLayout.PAGE_AXIS));
		replPromptPanel.add(replPromptPane);

		JPanel replInputPanel = new JPanel();
		replInputPanel.setLayout(new BoxLayout(replInputPanel,
				BoxLayout.PAGE_AXIS));
		replInputPanel.add(replInputPane);

		JPanel j = new JPanel(new GridBagLayout());
		// j.setLayout(new BoxLayout(j, BoxLayout.LINE_AXIS));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.VERTICAL;
		c.weighty = 1.0;
		c.weightx = 0;
		j.add(replPromptPane, c);// , BorderLayout.WEST);

		c.anchor = GridBagConstraints.LINE_END;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.BOTH;
		j.add(replInputPane, c);// , BorderLayout.EAST);

		replScrollPane = new JScrollPane();
		replScrollPane.setBorder(new EtchedBorder());
		// replScrollPane.setViewportView(replInputPane);
		replScrollPane.setViewportView(j);
		replScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		this.setLayout(new BorderLayout());
		this.add(replScrollPane);

		this.replInputPane.setCaretColor(Color.LIGHT_GRAY);
	}

	/**
	 * Method to make the cursor automatically appear when the REPL Console is
	 * selected
	 */
	public void requestFocus() {
		// requestFocusInWindow(true);
		replInputPane.grabFocus();
	}

	// The following code has been taken from
	// http://stackoverflow.com/a/13375811/2427542 in order to enable wrapping
	// around of text in the JTextPane
	class WrapEditorKit extends StyledEditorKit {
		ViewFactory defaultFactory = new WrapColumnFactory();

		public ViewFactory getViewFactory() {
			return defaultFactory;
		}
	}

	class WrapColumnFactory implements ViewFactory {
		public View create(Element elem) {
			String kind = elem.getName();
			if (kind != null) {
				if (kind.equals(AbstractDocument.ContentElementName)) {
					return new WrapLabelView(elem);
				} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
					return new ParagraphView(elem);
				} else if (kind.equals(AbstractDocument.SectionElementName)) {
					return new BoxView(elem, View.Y_AXIS);
				} else if (kind.equals(StyleConstants.ComponentElementName)) {
					return new ComponentView(elem);
				} else if (kind.equals(StyleConstants.IconElementName)) {
					return new IconView(elem);
				}
			}

			// default to text display
			return new LabelView(elem);
		}

	}

	class WrapLabelView extends LabelView {
		public WrapLabelView(Element elem) {
			super(elem);
		}

		public float getMinimumSpan(int axis) {
			switch (axis) {
			case View.X_AXIS:
				return 0;
			case View.Y_AXIS:
				return super.getMinimumSpan(axis);
			default:
				throw new IllegalArgumentException("Invalid axis: " + axis);
			}
		}

	}

	protected void clear() {
		isClearing = true;
		replInputPane.setText(PROMPT);
		isClearing = false;
	}

	//
	// public void showPrompt() {
	// StyledDocument document = (StyledDocument) replInputPane.getDocument();
	// try {
	// document.insertString(document.getLength(), PROMPT, null);
	// } catch (BadLocationException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public void showSpace(int location) {
	// StyledDocument document = (StyledDocument) replInputPane.getDocument();
	// try {
	// document.insertString(location, SPACE, null);
	// } catch (BadLocationException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// /**
	// * Use to get line number at which caret is placed
	// *
	// * Code from http://stackoverflow.com/a/2750099/2427542
	// *
	// * @param comp
	// * @param offset
	// * @return
	// * @throws BadLocationException
	// */
	// static int getLineOffset(JTextComponent comp, int offset)
	// throws BadLocationException {
	// Document doc = comp.getDocument();
	// if (offset < 0) {
	// throw new BadLocationException("Can't translate offset to line", -1);
	// } else if (offset > doc.getLength()) {
	// throw new BadLocationException("Can't translate offset to line",
	// doc.getLength() + 1);
	// } else {
	// Element map = doc.getDefaultRootElement();
	// return map.getElementIndex(offset);
	// }
	// }
	//
	// /**
	// * Use to get location at which caret is placed
	// *
	// * Code from http://stackoverflow.com/a/2750099/2427542
	// *
	// * @param comp
	// * @param offset
	// * @return
	// * @throws BadLocationException
	// */
	// static int getHorizontalOffset(JTextComponent comp, int line)
	// throws BadLocationException {
	// Element map = comp.getDocument().getDefaultRootElement();
	// if (line < 0) {
	// throw new BadLocationException("Negative line", -1);
	// } else if (line >= map.getElementCount()) {
	// throw new BadLocationException("No such line", comp.getDocument()
	// .getLength() + 1);
	// } else {
	// Element lineElem = map.getElement(line);
	// return lineElem.getStartOffset();
	// }
	// }
	//
	// public void caretUpdate(CaretEvent e) {
	// if (isClearing)
	// return;
	// int dot = e.getDot();
	// int line;
	// int positionInLine;
	// try {
	// line = getLineOffset(replInputPane, dot);
	// positionInLine = dot - getHorizontalOffset(replInputPane, line);
	//
	// if (positionInLine < 7) {
	// if (line == previousCaretLine + 1) {
	// showSpace(dot-positionInLine+1);
	// replInputPane.setCaretPosition(7);
	// } else
	// replInputPane.setCaretPosition(previousHorizCaretPosition);
	// } else if (line == previousCaretLine + 1) {
	// showSpace(dot-positionInLine+1);
	// replInputPane.setCaretPosition(7);
	// } else {
	// previousHorizCaretPosition = dot;
	// previousCaretLine = line;
	// }
	//
	// } catch (BadLocationException e1) {
	// e1.printStackTrace();
	// }
	// }

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// System.out.println(replInputPane.getCaretPosition());
	}

	@Override
	public void caretUpdate(CaretEvent arg0) {
		// TODO Auto-generated method stub

	}

}
