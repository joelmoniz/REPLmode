package jm.mode.replmode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

/**
 * Toggle Button displayed in the editor line status panel for toggling bewtween
 * console and REPL.
 * 
 * Code adapted from XQConsoleToggle class in processing-experimental.
 * 
 * @author Manindra Moharana &lt;me@mkmoharana.com&gt;
 * @author Joel Ruben Antony Moniz
 * 
 */

@SuppressWarnings("serial")
public class REPLConsoleToggle extends JPanel implements MouseListener {
	public static final String CONSOLE = "Console", REPL = "REPL";

	private boolean toggleText = true;
	private boolean toggleBG = true;

	/**
	 * Height of the component
	 */
	protected int height;
	protected REPLEditor editor;
	protected String buttonName;

	public REPLConsoleToggle(REPLEditor editor, String buttonName, int height) {
		this.editor = editor;
		this.height = height;
		this.buttonName = buttonName;
	}

	public Dimension getPreferredSize() {
		return new Dimension(70, height);
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// On mouse hover, text and background color are changed.
		if (toggleBG) {
			g.setColor(new Color(0xff9DA7B0));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			g.setColor(new Color(0xff29333D));
			g.fillRect(0, 0, 4, this.getHeight());
			g.setColor(Color.BLACK);
		} else {
			g.setColor(Color.DARK_GRAY);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			g.setColor(new Color(0xff29333D));
			g.fillRect(0, 0, 4, this.getHeight());
			g.setColor(Color.WHITE);
		}

		g.drawString(buttonName, getWidth() / 2 + 2 // + 2 is a offset
				- getFontMetrics(getFont()).stringWidth(buttonName) / 2,
				this.getHeight() - 6);
		if (drawMarker) {
			g.setColor(markerColor);
			g.fillRect(4, 0, 2, this.getHeight());
		}
	}

	boolean drawMarker = false;
	protected Color markerColor;

	public void updateMarker(boolean value, Color color) {
		drawMarker = value;
		markerColor = color;
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

		this.repaint();
		try {
			editor.showConsoleOrREPL(buttonName);
		} catch (Exception e) {
			System.out.println(e);
			// e.printStackTrace();
		}
		toggleText = !toggleText;
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		toggleBG = !toggleBG;
		this.repaint();
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		toggleBG = !toggleBG;
		this.repaint();
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
}
