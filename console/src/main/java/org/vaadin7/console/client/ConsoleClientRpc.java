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

    void print(String text);

    void printWithClass(String text, String className);

    void println(String text);

    void printlnWithClass(String text, String className);

    void append(String text);

    void appendWithClass(String text, String className);

    void prompt();

    void prompt(String inputText);

    void ff();

    void cr();

    void lf();

    void clearBuffer();

    void reset();

    void newLine();

    void newLineIfNotEndsWithNewLine();

    void scrollToEnd();

    void focusInput();

    void bell();

    void clearHistory();

}
