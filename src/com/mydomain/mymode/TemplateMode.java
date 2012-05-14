package com.mydomain.mymode;

import java.io.File;
import processing.app.Base;
import processing.app.Editor;
import processing.app.EditorState;
import processing.mode.java.JavaMode;

/**
 * Mode Template for extending Java mode in Processing IDE 2.0a5 or later.
 *
 */
public class TemplateMode extends JavaMode {
    public TemplateMode(Base base, File folder) {
        super(base, folder);
    }

    /**
     * Return the pretty/printable/menu name for this mode. This is separate
     * from the single word name of the folder that contains this mode. It could
     * even have spaces, though that might result in sheer madness or total
     * mayhem.
     */
    @Override
    public String getTitle() {
        return "TemplateMode";
    }

    /**
     * Create a new editor associated with this mode.
     */
    /*
    @Override
    public Editor createEditor(Base base, String path, EditorState state) {
        return null;
    }
    */

    /**
     * Returns the default extension for this editor setup.
     */
    /*
    @Override
    public String getDefaultExtension() {
        return null;
    }
    */

    /**
     * Returns a String[] array of proper extensions.
     */
    /*
    @Override
    public String[] getExtensions() {
        return null;
    }
    */

    /**
     * Get array of file/directory names that needn't be copied during "Save
     * As".
     */
    /*
    @Override
    public String[] getIgnorable() {
        return null;
    }
    */
}
