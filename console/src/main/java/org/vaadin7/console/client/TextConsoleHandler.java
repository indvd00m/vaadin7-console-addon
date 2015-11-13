package org.vaadin7.console.client;

public interface TextConsoleHandler {

    void terminalInput(TextConsole term, String input);

    void suggest(String input);

    void colsChanged(int cols);

    void rowsChanged(int rows);

    void paintableSizeChanged();

}
