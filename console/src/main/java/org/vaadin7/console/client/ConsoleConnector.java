package org.vaadin7.console.client;

import org.vaadin7.console.Console;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.communication.FieldRpc.FocusAndBlurServerRpc;
import com.vaadin.shared.ui.Connect;

/**
 * Connector binds client-side widget class to server-side component class.
 * Connector lives in the client and the @Connect annotation specifies the
 * corresponding server-side component.
 * 
 * @author indvdum (gotoindvdum[at]gmail[dot]com)
 * @since 22.05.2014 11:20:45
 * 
 */
@Connect(Console.class)
public class ConsoleConnector extends AbstractComponentConnector implements FocusHandler {

	private static final long serialVersionUID = 2829157055722482839L;

	// ServerRpc is used to send events to server. Communication implementation
	// is automatically created here
	ConsoleServerRpc rpc = RpcProxy.create(ConsoleServerRpc.class, this);

	public ConsoleConnector() {

		// To receive RPC events from server, we register ClientRpc
		// implementation
		registerRpc(ConsoleClientRpc.class, new ConsoleClientRpc() {

			@Override
			public void setGreeting(String greeting) {
				getWidget().getConfig().setGreeting(greeting);
			}

			@Override
			public void setPs(String ps) {
				getWidget().setPs(ps);
			}

			@Override
			public void setWrap(boolean wrap) {
				getWidget().getConfig().setWrap(wrap);
			}

			@Override
			public void setRows(int rows) {
				getWidget().getConfig().setRows(rows);
				getWidget().setRows(rows);
			}

			@Override
			public void setCols(int cols) {
				getWidget().getConfig().setCols(cols);
				getWidget().setCols(cols);
			}

			@Override
			public void print(String text) {
				getWidget().print(text);
			}

			@Override
			public void printWithClass(String text, String className) {
				getWidget().printWithClass(text, className);
			}

			@Override
			public void println(String text) {
				getWidget().println(text);
			}

			@Override
			public void printlnWithClass(String text, String className) {
				getWidget().printlnWithClass(text, className);
			}

			@Override
			public void append(String text) {
				getWidget().append(text);
			}

			@Override
			public void appendWithClass(String text, String className) {
				getWidget().appendWithClass(text, className);
			}

			@Override
			public void prompt() {
				getWidget().prompt();
			}

			@Override
			public void prompt(String inputText) {
				getWidget().prompt(inputText);
			}

			@Override
			public void ff() {
				getWidget().formFeed();
			}

			@Override
			public void cr() {
				getWidget().carriageReturn();
			}

			@Override
			public void lf() {
				getWidget().carriageReturn();
			}

			@Override
			public void clearBuffer() {
				getWidget().clearBuffer();
			}

			@Override
			public void reset() {
				getWidget().reset();
			}

			@Override
			public void newLine() {
				getWidget().newLine();
			}

			@Override
			public void newLineIfNotEndsWithNewLine() {
				getWidget().newLineIfNotEndsWithNewLine();
			}

			@Override
			public void scrollToEnd() {
				getWidget().scrollToEnd();
			}

			@Override
			public void focusInput() {
				getWidget().focusInput();
			}

			@Override
			public void bell() {
				getWidget().bell();
			}

			@Override
			public void setMaxBufferSize(int bufferSize) {
				getWidget().getConfig().setMaxBufferSize(bufferSize);
				getWidget().setMaxBufferSize(bufferSize);
			}

			@Override
			public void clearHistory() {
				getWidget().clearCommandHistory();
			}

			@Override
			public void setPrintPromptOnInput(boolean printPromptOnInput) {
				getWidget().getConfig().setPrintPromptOnInput(printPromptOnInput);
			}

			@Override
			public void setSmartScrollToEnd(boolean smartScrollToEnd) {
				getWidget().getConfig().setSmartScrollToEnd(smartScrollToEnd);
			}

		});

		getWidget().setHandler(new TextConsoleHandler() {

			@Override
			public void terminalInput(TextConsole term, String input) {
				rpc.input(input);
			}

			@Override
			public void suggest(String input) {
				rpc.suggest(input);
			}

			@Override
			public void paintableSizeChanged() {
				notifyPaintableSizeChange();
				rpc.setHeight(getWidget().getHeight());
				rpc.setWidth(getWidget().getWidth());
			}

			@Override
			public void rowsChanged(int rows) {
				rpc.setRows(rows);
			}

			@Override
			public void colsChanged(int cols) {
				rpc.setCols(cols);
			}
		});

	}

	// We must implement createWidget() to create correct type of widget
	@Override
	protected Widget createWidget() {
		ConsoleWidget widget = GWT.create(ConsoleWidget.class);
		return widget;
	}

	// We must implement getWidget() to cast to correct type
	@Override
	public ConsoleWidget getWidget() {
		return (ConsoleWidget) super.getWidget();
	}

	// We must implement getState() to cast to correct type
	@Override
	public ConsoleState getState() {
		return (ConsoleState) super.getState();
	}

	// Whenever the state changes in the server-side, this method is called
	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		// GWT.log("onStateChanged() width = " + getState().width);
		super.onStateChanged(stateChangeEvent);
	}

	@Override
	public void onFocus(FocusEvent event) {
		// EventHelper.updateFocusHandler ensures that this is called only when
		// there is a listener on server side
		getRpcProxy(FocusAndBlurServerRpc.class).focus();
	}

	public void notifyPaintableSizeChange() {
		getLayoutManager().setNeedsMeasure(this);
	}

	@OnStateChange("width")
	void widthChanged() {
		// GWT.log("widthChanged to " + getState().width);
		getWidget().setWidth(getState().width);
	}

	@OnStateChange("height")
	void heightChanged() {
		// GWT.log("heightChanged to " + getState().height);
		getWidget().setHeight(getState().height);
	}

}
