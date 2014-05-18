package jm.mode.replmode;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
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
public class REPLConsolePane extends JPanel {

	protected JScrollPane replScrollPane;

	protected JTextPane replInputPane;

	public REPLConsolePane(Editor editor) {

		replInputPane = new JTextPane();
		replInputPane.setEditorKit(new WrapEditorKit());

		replScrollPane = new JScrollPane();
		replScrollPane.setBorder(new EtchedBorder());
		replScrollPane.setViewportView(replInputPane);
		replScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		this.setLayout(new BorderLayout());
		this.add(replScrollPane);
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
		replInputPane.setText("");
	}

}
