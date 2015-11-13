package org.vaadin7.console;

import java.util.HashSet;
import java.util.Set;

import org.vaadin7.console.Console.Command;
import org.vaadin7.console.Console.Handler;

/**
 * Default handler for console.
 *
 */
public class DefaultConsoleHandler implements Handler {

    private static final long serialVersionUID = 1L;

    public void handleException(final Console console, final Exception e, final Command cmd, final String[] argv) {
        e.printStackTrace();
        console.println(e.getClass().getSimpleName() + ": " + e.getMessage());
    }

    public Set<String> getSuggestions(final Console console, final String input) {

        final String prefix = console.parseCommandPrefix(input);
        if (prefix != null) {
            final Set<String> matches = new HashSet<String>();
            final Set<String> cmds = console.getCommands();
            for (final String cmd : cmds) {
                if (cmd.startsWith(prefix)) {
                    matches.add(cmd);
                }
            }
            return matches;
        }
        return null;
    }

    public void inputReceived(final Console console, final String lastInput) {
        console.parseAndExecuteCommand(lastInput);
        console.prompt();
    }

    public void commandNotFound(final Console console, final String[] argv) {
        console.print("ERROR: " + argv[0] + ": command not found.");
    }

}
