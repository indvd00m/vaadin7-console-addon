package org.vaadin7.console.client;

import com.google.gwt.user.client.ui.Label;

// Extend any GWT Widget
public class ConsoleWidget extends Label {

	public ConsoleWidget() {

		// CSS class-name should not be v- prefixed
		setStyleName("console");

		// State is set to widget in ConsoleConnector		
	}

}