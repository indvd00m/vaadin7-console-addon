package org.vaadin7.console;

import org.vaadin7.console.client.ConsoleClientRpc;
import org.vaadin7.console.client.ConsoleServerRpc;
import org.vaadin7.console.client.ConsoleState;

import com.vaadin.shared.MouseEventDetails;

// This is the server-side UI component that provides public API 
// for Console
public class Console extends com.vaadin.ui.AbstractComponent {

	private int clickCount = 0;

	// To process events from the client, we implement ServerRpc
	private ConsoleServerRpc rpc = new ConsoleServerRpc() {

		// Event received from client - user clicked our widget
		public void clicked(MouseEventDetails mouseDetails) {
			
			// Send nag message every 5:th click with ClientRpc
			if (++clickCount % 5 == 0) {
				getRpcProxy(ConsoleClientRpc.class)
						.alert("Ok, that's enough!");
			}
			
			// Update shared state. This state update is automatically 
			// sent to the client. 
			getState().text = "You have clicked " + clickCount + " times";
		}
	};

	public Console() {

		// To receive events from the client, we register ServerRpc
		registerRpc(rpc);
	}

	// We must override getState() to cast the state to ConsoleState
	@Override
	public ConsoleState getState() {
		return (ConsoleState) super.getState();
	}
}
