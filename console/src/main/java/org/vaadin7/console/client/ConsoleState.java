package org.vaadin7.console.client;

import java.util.ArrayList;

/**
 * @author indvdum (gotoindvdum[at]gmail[dot]com)
 * @since 16.06.2014 17:44:05
 *
 */
public class ConsoleState extends com.vaadin.shared.AbstractComponentState {

    private static final long serialVersionUID = -5576147144891328552L;
    // State can have both public variable and bean properties
    public String text = "Console";

    public int maxBufferSize = 0;
    public int cols = -1;
    public int rows = -1;
    public boolean wrap = true;
    public boolean isPrintPromptOnInput = true;
    public boolean isScrollLock = false;
    public boolean shouldTrim = true;
    public String greeting = "Console ready.";
    public ArrayList<String> history = new ArrayList<String>();
    public String ps = "}> ";
}
