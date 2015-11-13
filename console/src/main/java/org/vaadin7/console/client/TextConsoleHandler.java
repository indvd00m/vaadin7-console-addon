package org.vaadin7.console.client;

public interface TextConsoleHandler {

    public void terminalInput(TextConsole term, String input);

    public void suggest(String input);

    public void colsChanged(int cols);

    public void rowsChanged(int rows);

    public void paintableSizeChanged();

}
