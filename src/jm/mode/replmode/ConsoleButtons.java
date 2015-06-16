package jm.mode.replmode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import processing.app.EditorConsole;

/**
 * Various buttons (such as clear) displayed on the Line Status Bar. Some code
 * is from the XQConsoleToggle class in processing-experimental.
 * 
 * @author Joel Ruben Antony Moniz
 * 
 */
// TODO: Make it easier to add buttons!
@SuppressWarnings("serial")
public class ConsoleButtons extends JPanel implements MouseListener,
		MouseMotionListener {

	public static final String CLEAR = "Clear";

	protected Image clear_bn_reg, clear_bn_hov;

	private static File folder;

	protected static final String BASE_LOCN = "console_buttons/";
	protected static final String CLEAR_REG = BASE_LOCN + "clear_icon_reg.png";
	protected static final String CLEAR_HOV = BASE_LOCN + "clear_icon_hov.png";

	/**
	 * Boolean to see if the mouse is hovered on the button
	 */
	private boolean toggleBG = true;

	protected int height, width, totalWidth;

	protected String buttonName;

	protected EditorConsole console;
	protected REPLConsolePane replConsole;

	public ConsoleButtons(String buttonName, int height, EditorConsole console,
			REPLConsolePane replConsole) {
		this.height = height;
		width = height;
		totalWidth = height + 4 + getFontMetrics(getFont()).stringWidth(CLEAR)
				+ 4;
		this.buttonName = buttonName;

		this.console = console;
		this.replConsole = replConsole;
	}

	public Dimension getPreferredSize() {
		return new Dimension(totalWidth, height); // buttons are perfectly
													// square
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

		loadIcons();
		if (toggleBG) {
			if (clear_bn_reg != null) {
				g.drawImage(clear_bn_reg, 0, 0, height, width, null);
				g.setColor(new Color(0xff29333D));
				g.fillRect(width, 0, totalWidth - width, height);
			} else {
				System.out.println("clear_bn_reg null!");
			}
		} else {
			if (clear_bn_hov != null) {
				g.drawImage(clear_bn_hov, 0, 0, height, width, null);
				g.setColor(new Color(0xff29333D));
				g.fillRect(width, 0, totalWidth - width, height);
				g.setColor(Color.WHITE);
				g.drawString(CLEAR, width + 5, height - height / 2
						+ getFontMetrics(getFont()).getHeight() / 3);
			} else {
				System.out.println("clear_bn_hov null!");
			}
		}
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

	private void loadIcons() {
		try {
			clear_bn_reg = loadImage(CLEAR_REG);
			clear_bn_hov = loadImage(CLEAR_HOV);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get an image object from the theme folder. Taken as is from
	 * processing.app.Mode
	 */
	public Image loadImage(String filename) {
		File file = new File(folder, filename);
		if (!file.exists()) {
			return null;
		}
		return new ImageIcon(file.getAbsolutePath()).getImage();
	}

	public static void setFolder(File folder_in) {
		folder = folder_in;
	}

	private void clearConsoles() {
		console.clear();
		replConsole.clear();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		if (x < width && x > 0 && y < height && y > 0)
			clearConsoles();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		if (x < width && x > 0 && y < height && y > 0) {
			toggleBG = false;
		}
		this.repaint();
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		toggleBG = true;
		this.repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		int x = arg0.getX();
		int y = arg0.getY();
		if (x < width && x > 0 && y < height && y > 0) {
			toggleBG = false;
		} else
			toggleBG = true;

		this.repaint();
	}

}
