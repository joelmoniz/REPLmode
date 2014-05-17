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

	// protected void prepareFrame() {
	// Toolkit.setIcon(this);
	// setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	// // Default size: setBounds(100, 100, 458, 160);
	// setBounds(100, 100, 458, 160); // Yeah, I hardcode such things sometimes.
	// Hate me.
	//
	// contentPane = new JPanel();
	// contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	// setContentPane(contentPane);
	// contentPane.setLayout(new BorderLayout(0, 0));
	//
	// scrollPane = new JScrollPane();
	// contentPane.add(scrollPane);
	//
	// try {
	// Docker = new DockTool2Base();
	// addListeners();
	// } catch (Exception e) {
	// System.out.println("addListeners() failed :(");
	// e.printStackTrace();
	// }
	//
	// if (editor != null) {
	// setLocation(new Point(editor.getLocation().x
	// + editor.getWidth(), editor.getLocation().y));
	// }
	//
	// }
	//
	//
	// /**
	// * Adds various listeners to components of EditorWindow and to the Editor
	// * window
	// */
	// protected void addListeners() {
	//
	// if (thisREPLWindow == null)
	// System.out.println("REPL null");
	//
	// thisREPLWindow.addComponentListener(new ComponentListener() {
	//
	// @Override
	// public void componentShown(ComponentEvent e) {
	//
	// }
	//
	// @Override
	// public void componentResized(ComponentEvent e) {
	// Docker.tryDocking();
	// }
	//
	// @Override
	// public void componentMoved(ComponentEvent e) {
	// Docker.tryDocking();
	// }
	//
	// @Override
	// public void componentHidden(ComponentEvent e) {
	//
	// }
	// });
	//
	// thisREPLWindow.addWindowListener(new WindowAdapter() {
	//
	// @Override
	// public void windowClosing(WindowEvent e) {
	// }
	//
	// @Override
	// public void windowDeiconified(WindowEvent e) {
	// editor.setExtendedState(Frame.NORMAL);
	// }
	//
	// });
	//
	// if (editor == null) {
	// System.out.println("Editor null");
	// return;
	// }
	//
	//
	// editor.addComponentListener(new ComponentListener() {
	//
	// @Override
	// public void componentShown(ComponentEvent e) {
	//
	// }
	//
	// @Override
	// public void componentResized(ComponentEvent e) {
	// if (Docker.isDocked()) {
	// Docker.dock();
	// } else {
	// Docker.tryDocking();
	// }
	// }
	//
	// @Override
	// public void componentMoved(ComponentEvent e) {
	//
	// if (Docker.isDocked()) {
	// Docker.dock();
	// } else {
	// Docker.tryDocking();
	// }
	//
	// }
	//
	// @Override
	// public void componentHidden(ComponentEvent e) {
	// // System.out.println("ed hidden");
	// }
	// });
	//
	// }
	//
	//
	// /**
	// * Implements the docking feature of the tool - The frame sticks to the
	// * editor and once docked, moves along with it as the editor is resized,
	// * moved, or closed.
	// *
	// * This class has been borrowed from Tab Manager tool by Thomas Diewald.
	// It
	// * has been slightly modified and used here.
	// *
	// * @author Thomas Diewald , http://thomasdiewald.com
	// */
	// private class DockTool2Base {
	//
	// private int docking_border = 0;
	// private int dock_on_editor_y_offset_ = 0;
	// private int dock_on_editor_x_offset_ = 0;
	//
	// // ///////////////////////////////
	// // ____2____
	// // | |
	// // | |
	// // 0 | editor | 1
	// // | |
	// // |_________|
	// // 3
	// // ///////////////////////////////
	//
	// // public void reset() {
	// // dock_on_editor_y_offset_ = 0;
	// // dock_on_editor_x_offset_ = 0;
	// // docking_border = 0;
	// // }
	//
	// public boolean isDocked() {
	// return (docking_border >= 0);
	// }
	//
	// private final int MAX_GAP_ = 20;
	//
	// //
	// public void tryDocking() {
	// if (editor == null)
	// return;
	//
	// Frame frame = thisREPLWindow;
	//
	// int ex = editor.getX();
	// int ey = editor.getY();
	// int ew = editor.getWidth();
	// int eh = editor.getHeight();
	//
	// int fx = frame.getX();
	// int fy = frame.getY();
	// int fw = frame.getWidth();
	// int fh = frame.getHeight();
	//
	// if (((fy > ey) && (fy < ey + eh))
	// || ((fy + fh > ey) && (fy + fh < ey + eh))) {
	// int dis_border_left = Math.abs(ex - (fx + fw));
	// int dis_border_right = Math.abs((ex + ew) - (fx));
	//
	// if (dis_border_left < MAX_GAP_ || dis_border_right < MAX_GAP_) {
	// docking_border = (dis_border_left < dis_border_right) ? 0
	// : 1;
	// dock_on_editor_y_offset_ = fy - ey;
	// dock();
	// return;
	// }
	// }
	//
	// if (((fx > ex) && (fx < ex + ew))
	// || ((fx + fw > ey) && (fx + fw < ex + ew))) {
	// int dis_border_top = Math.abs(ey - (fy + fh));
	// int dis_border_bot = Math.abs((ey + eh) - (fy));
	//
	// if (dis_border_top < MAX_GAP_ || dis_border_bot < MAX_GAP_) {
	// docking_border = (dis_border_top < dis_border_bot) ? 2 : 3;
	// dock_on_editor_x_offset_ = fx - ex;
	// dock();
	// return;
	// }
	// }
	// docking_border = -1;
	// }
	//
	// public void dock() {
	// if (editor == null)
	// return;
	//
	// Frame frame = thisREPLWindow;
	//
	// int ex = editor.getX();
	// int ey = editor.getY();
	// int ew = editor.getWidth();
	// int eh = editor.getHeight();
	//
	// // int fx = frame.getX();
	// // int fy = frame.getY();
	// int fw = frame.getWidth();
	// int fh = frame.getHeight();
	//
	// int x = 0, y = 0;
	// if (docking_border == -1) {
	// return;
	// }
	//
	// if (docking_border == 0) {
	// x = ex - fw;
	// y = ey + dock_on_editor_y_offset_;
	// }
	// if (docking_border == 1) {
	// x = ex + ew;
	// y = ey + dock_on_editor_y_offset_;
	// }
	//
	// if (docking_border == 2) {
	// x = ex + dock_on_editor_x_offset_;
	// y = ey - fh;
	// }
	// if (docking_border == 3) {
	// x = ex + dock_on_editor_x_offset_;
	// y = ey + eh;
	// }
	// frame.setLocation(x, y);
	// }
	//
	// }
}
