package org.vaadin7.console.client;

/**
 * Extend any GWT Widget.
 * 
 * @author indvdum (gotoindvdum[at]gmail[dot]com)
 * @since 22.05.2014 17:22:44
 * 
 */
public class ConsoleWidget extends TextConsole {

	private static final String CSS_CLASS_NAME = "console";

	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public ConsoleWidget() {
		super();
		getElement().addClassName(CSS_CLASS_NAME);
		getConfig().setScrolledToEnd(true);
	}

}