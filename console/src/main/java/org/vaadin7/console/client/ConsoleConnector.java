package org.vaadin7.console.client;

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

import org.vaadin7.console.Console;

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
            public void clearHistory() {
                getWidget().clearCommandHistory();
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
        });

    }

    // We must implement createWidget() to create correct type of widget
    @Override
    protected Widget createWidget() {
        return GWT.<ConsoleWidget>create(ConsoleWidget.class);
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
        log("inside onStateChanged()");
        ConsoleWidget widget = getWidget();
        if (!getState().ps.equals(widget.getConfig().getPs()))
            widget.setPs(getState().ps);
        if (getState().maxBufferSize != widget.getConfig().getMaxBufferSize()) {
            widget.getConfig().setMaxBufferSize(getState().maxBufferSize);
            widget.setMaxBufferSize(getState().maxBufferSize);
        }
        if (getState().cols != widget.getConfig().getCols()) {
            widget.getConfig().setCols(getState().cols);
            widget.setCols(getState().cols);
        }
        if (getState().rows != widget.getConfig().getRows()) {
            widget.getConfig().setRows(getState().rows);
            widget.setRows(getState().rows);
        }
        if (getState().wrap != widget.getConfig().isWrap())
            widget.getConfig().setWrap(getState().wrap);
        if (getState().isPrintPromptOnInput != widget.getConfig().isPrintPromptOnInput())
            widget.getConfig().setPrintPromptOnInput(getState().isPrintPromptOnInput);
        if (getState().isScrollLock != widget.getConfig().isScrollLock())
            widget.getConfig().setScrollLock(getState().isScrollLock);
        if (!getState().greeting.equals(widget.getConfig().getGreeting()))
            widget.getConfig().setGreeting(getState().greeting);
        if (!getState().history.isEmpty())
            widget.addPreviousHistory(getState().history);
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

    private native int log(String msg)/*-{
        console.log(msg);
    }-*/;
}
