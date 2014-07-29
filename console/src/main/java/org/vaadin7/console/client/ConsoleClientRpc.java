package org.vaadin7.console.client;

import com.vaadin.shared.communication.ClientRpc;

/**
 * ClientRpc is used to pass events from server to client. For sending
 * information about the changes to component state, use State instead.
 * 
 * @author indvdum (gotoindvdum[at]gmail[dot]com)
 * @since 22.05.2014 11:01:38
 * 
 */
public interface ConsoleClientRpc extends ClientRpc {

	public void setGreeting(String greeting);

	public void setPs(String ps);

	public void setWrap(boolean wrap);

	public void setRows(int rows);

	public void setCols(int cols);

	public void print(String text);

	public void printWithClass(String text, String className);

	public void println(String text);

	public void printlnWithClass(String text, String className);

	public void append(String text);

	public void appendWithClass(String text, String className);

	public void prompt();

	public void prompt(String inputText);

	public void ff();

	public void cr();

	public void lf();

	public void clearBuffer();

	public void reset();

	public void newLine();

	public void newLineIfNotEndsWithNewLine();

	public void scrollToEnd();

	public void focusInput();

	public void bell();

	public void setMaxBufferSize(int bufferSize);

	public void clearHistory();

	public void setPrintPromptOnInput(boolean printPromptOnInput);

	public void setScrollLock(boolean scrollLock);

}