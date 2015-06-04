package jm.mode.replmode;

import java.io.File;

import processing.app.Base;
import processing.app.Editor;
import processing.app.EditorState;
import processing.app.Mode;
import processing.mode.java.JavaMode;

/**
 * REPL Mode for Processing.
 * 
 */
public class REPLMode extends JavaMode {
	public REPLMode(Base base, File folder) {
		super(base, folder);

		ConsoleButtons.setFolder(folder);
	}

	/**
	 * Return the pretty/printable/menu name for this mode. This is separate
	 * from the single word name of the folder that contains this mode. It could
	 * even have spaces, though that might result in sheer madness or total
	 * mayhem.
	 */
	@Override
	public String getTitle() {
		return "REPL Mode";
	}

	/**
	 * Create a new editor associated with this mode.
	 */

	@Override
	public Editor createEditor(Base base, String path, EditorState state) {
		return new REPLEditor(base, path, state, this);
	}

	/**
	 * Returns the default extension for this editor setup.
	 */
	/*
	 * @Override public String getDefaultExtension() { return null; }
	 */

	/**
	 * Returns a String[] array of proper extensions.
	 */
	/*
	 * @Override public String[] getExtensions() { return null; }
	 */

	/**
	 * Get array of file/directory names that needn't be copied during "Save
	 * As".
	 */
	/*
	 * @Override public String[] getIgnorable() { return null; }
	 */

	/**
	 * Retrieve the ClassLoader for JavaMode. This is used by Compiler to load
	 * ECJ classes. Thanks to Ben Fry.
	 * 
	 * @return the class loader from java mode
	 */
	@Override
	public ClassLoader getClassLoader() {
		for (Mode m : base.getModeList()) {
			if (m.getClass().getName().equals(JavaMode.class.getName())) {
//				JavaMode jMode = (JavaMode) m;
				return m.getClassLoader();
			}
		}
		return null; // badness
	}

	// @Override
	// public Editor createEditor(Base base, String path, EditorState state) {
	// return new REPLEditor(base, path, state, this);
	// }

}
