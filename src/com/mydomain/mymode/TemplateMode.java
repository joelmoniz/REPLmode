package com.mydomain.mymode;

import java.io.File;
import processing.app.Base;
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
     * Called by PDE
     */
    @Override
    public String getTitle() {
        return "TemplateMode";
    }
}
