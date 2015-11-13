package org.vaadin7.console;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.vaadin7.console.ansi.ANSICodeConverter;
import org.vaadin7.console.ansi.DefaultANSICodeConverter;
import org.vaadin7.console.client.ConsoleClientRpc;
import org.vaadin7.console.client.ConsoleServerRpc;
import org.vaadin7.console.client.ConsoleState;

import com.vaadin.ui.Component;

/**
 * This is the server-side UI component that provides public API for Console.
 *
 * @author Sami Ekblad / Vaadin
 * @author indvdum (gotoindvdum[at]gmail[dot]com)
 * @since 22.05.2014 17:30:06
 *
 */
public class Console extends com.vaadin.ui.AbstractComponent implements Component.Focusable {

    private Console console = this;

    // To process events from the client, we implement ServerRpc
    private ConsoleServerRpc rpc = new ConsoleServerRpc() {

        private static final long serialVersionUID = 443398479527027435L;

        @Override
        public void setHeight(String height) {
            console.setHeight(height);
        }

        @Override
        public void setWidth(String width) {
            console.setWidth(width);
        }

        @Override
        public void setCols(int cols) {
            config.cols = cols;
        }

        @Override
        public void setRows(int rows) {
            config.rows = rows;
        }

        @Override
        public void input(String input) {
            handleInput(input);
        }

        @Override
        public void suggest(String input) {
            handleSuggest(input);
        }
    };

    public Console(final Console.Handler handler) {
        this();
        setHandler(handler);
    }

    public Console() {
        setImmediate(true);
        setHandler(new DefaultConsoleHandler());
        setANSIToCSSConverter(new DefaultANSICodeConverter());
        // To receive events from the client, we register ServerRpc
        registerRpc(rpc);

        // setCols(getCols());
        // setRows(getRows());
        setMaxBufferSize(getMaxBufferSize());
        setWrap(isWrap());
        setPrintPromptOnInput(isPrintPromptOnInput());
        setScrollLock(isScrollLock());
        setGreeting(getGreeting());
        setPs(getPs());
        reset();
    }

    // We must override getState() to cast the state to ConsoleState
    @Override
    public ConsoleState getState() {
        return (ConsoleState) super.getState();
    }

    private static final long serialVersionUID = 590258219352859644L;
    private Handler handler;
    private ANSICodeConverter ansiToCSSconverter;
    private boolean isConvertANSIToCSS = false;
    private final HashMap<String, Command> commands = new HashMap<String, Command>();
    private final Config config = new Config();

    private static final String DEFAULT_PS = "}> ";
    private static final String DEFAULT_GREETING = "Console ready.";
    private static final int DEFAULT_BUFFER = 0;
    private static final int DEFAULT_COLS = -1;
    private static final int DEFAULT_ROWS = -1;
    private static final boolean DEFAULT_WRAP = true;
    private static final boolean DEFAULT_PRINT_PROMPT_ON_INPUT = true;
    private static final boolean DEFAULT_SMART_SCROLL_TO_END = false;
    private static final int MAX_COLS = 500;
    private static final int MAX_ROWS = 200;

    public boolean isWrap() {
        return config.wrap;
    }

    public void setWrap(final boolean wrap) {
        config.wrap = wrap;
        getRpcProxy(ConsoleClientRpc.class).setWrap(wrap);
    }

    /**
     * @return true, if entered by user text immediately will print to console,
     *         false otherwise
     */
    public boolean isPrintPromptOnInput() {
        return config.isPrintPromptOnInput;
    }

    /**
     * @param isPrintPromptOnInput
     *            if true - entered by user text immediately will be printed to
     *            console, nothing happens otherwise
     */
    public void setPrintPromptOnInput(final boolean isPrintPromptOnInput) {
        config.isPrintPromptOnInput = isPrintPromptOnInput;
        getRpcProxy(ConsoleClientRpc.class).setPrintPromptOnInput(isPrintPromptOnInput);
    }

    /**
     * @return true, if method scrollToEnd will only work if last scroll state
     *         was "end"
     */
    public boolean isScrollLock() {
        return config.isScrollLock;
    }

    /**
     * @param isScrollLock
     *            if true - method scrollToEnd will only work if last scroll
     *            state was "end"
     */
    public void setScrollLock(final boolean isScrollLock) {
        config.isScrollLock = isScrollLock;
        getRpcProxy(ConsoleClientRpc.class).setScrollLock(isScrollLock);
    }

    /**
     * The tab order number of this field.
     */
    private int tabIndex = 0;
    @SuppressWarnings("unused")
    private Integer fontw;
    @SuppressWarnings("unused")
    private Integer fonth;
    private PrintStream printStream;
    private String lastSuggestInput;
    private List<CommandProvider> commandProviders;

    /**
     * An inner class for holding the configuration data.
     */
    public static class Config implements Serializable {

        private static final long serialVersionUID = -812601232248504108L;

        int maxBufferSize = DEFAULT_BUFFER;
        int cols = DEFAULT_COLS;
        int rows = DEFAULT_ROWS;
        boolean wrap = DEFAULT_WRAP;
        boolean isPrintPromptOnInput = DEFAULT_PRINT_PROMPT_ON_INPUT;
        boolean isScrollLock = DEFAULT_SMART_SCROLL_TO_END;
        String ps = DEFAULT_PS;
        String greeting = DEFAULT_GREETING;

    }

    /**
     * Console Handler interface.
     *
     * Handler provides a hook to handle various console related events and
     * override the default processing.
     *
     */
    public interface Handler extends Serializable {

        /**
         * Called when user uses TAB to complete the command entered into the
         * Console input.
         *
         * @param console
         * @param lastInput
         * @return
         */
        Set<String> getSuggestions(Console console, String lastInput);

        /**
         * Called when user has entered input to the Console and presses enter
         * to execute it.
         *
         * @param console
         * @param lastInput
         */
        void inputReceived(Console console, String lastInput);

        /**
         * Handle an exception during a Command execution.
         *
         * @param console
         * @param e
         * @param cmd
         * @param argv
         */
        void handleException(Console console, Exception e, Command cmd, String[] argv);

        /**
         * Handle situation where a command could not be found.
         *
         * @param console
         * @param argv
         */
        void commandNotFound(Console console, String[] argv);

    }

    /**
     * Commands that can be executed against the Component instance. They
     * provide convenient string to method mapping. Basically, a Command is a
     * method that that can be executed in Component. It can have parameters or
     * not.
     *
     */
    public interface Command extends Serializable {

        /**
         * Execute a Command with arguments.
         *
         * @param console
         * @param argv
         * @return
         * @throws Exception
         */
        public Object execute(Console console, String[] argv) throws Exception;

        /**
         * Get usage information about this command.
         *
         * @param console
         * @param argv
         * @return
         */
        public String getUsage(Console console, String[] argv);
    }

    /**
     * Interface for providing Commands to the console. One can register a
     * command providers to console instead of individual commands to provide a
     * lot of commands.
     *
     */
    public interface CommandProvider extends Serializable {

        /**
         * List all available command from this provider.
         *
         * @param console
         * @return
         */
        Set<String> getAvailableCommands(Console console);

        /**
         * Get Command instance based on command name.
         *
         * @param console
         * @param commandName
         * @return
         */
        Command getCommand(Console console, String commandName);

    }

    public void addCommandProvider(final CommandProvider commandProvider) {
        if (commandProviders == null) {
            commandProviders = new ArrayList<CommandProvider>();
        }
        commandProviders.add(commandProvider);
    }

    public void removeCommandProvider(final CommandProvider commandProvider) {
        if (commandProviders == null) {
            return;
        }
        commandProviders.remove(commandProvider);
    }

    public void removeAllCommandProviders() {
        if (commandProviders == null) {
            return;
        }
        commandProviders.clear();
    }

    /**
     * Overridden to filter client-side calculation/changes and avoid loops.
     *
     */
    @Override
    public void setWidth(float width, Unit unit) {
        if (width != getWidth() || unit != getWidthUnits()) {
            super.setWidth(width, unit);
            getState().width = width + unit.getSymbol();
        }
    }

    /**
     * Overridden to filter client-side calculation/changes and avoid loops.
     *
     */
    @Override
    public void setHeight(float height, Unit unit) {
        if (height != getHeight() || unit != getHeightUnits()) {
            super.setHeight(height, unit);
            getState().height = height + unit.getSymbol();
        }
    }

    protected void handleSuggest(final String input) {

        final boolean cancelIfNotASingleMatch = (input != null && !input.equals(lastSuggestInput));
        lastSuggestInput = input;

        final Set<String> matches = handler.getSuggestions(this, input);

        if (matches == null || matches.size() == 0) {
            bell();
            return;
        }

        // Output the original
        final String prefix = parseCommandPrefix(input);
        String output = input.substring(0, input.lastIndexOf(prefix));
        if (matches.size() == 1) {
            // Output the only match
            output += matches.iterator().next() + " "; // append the single
            // match
        } else {

            // We output until the common prefix
            StringBuilder commonPrefix = new StringBuilder(prefix);
            final int maxLen = matches.iterator().next().length();
            for (int i = prefix.length(); i < maxLen; i++) {
                char c = 0;
                boolean charMatch = true;
                for (final String m : matches) {
                    if (c == 0) {
                        c = m.charAt(i);
                    } else if (i < m.length()) {
                        charMatch &= m.charAt(i) == c;
                        if (!charMatch) {
                            break;
                        }
                    } else {
                        charMatch = false;
                        break;
                    }
                }
                if (charMatch) {
                    commonPrefix.append(c);
                }
            }
            output += commonPrefix.toString();
            if (prefix.equals(commonPrefix.toString()) && !cancelIfNotASingleMatch) {
                final StringBuffer suggestions = new StringBuffer("\n");
                for (final String m : matches) {
                    suggestions.append(" " + m);
                }
                print(suggestions.toString());
            } else {
                bell();
                lastSuggestInput = output; // next suggest will not beep
            }
        }
        prompt(output);
        focus();

    }

    public void bell() {
        getRpcProxy(ConsoleClientRpc.class).bell();
    }

    protected void handleInput(final String input) {

        // Ask registered handler
        handler.inputReceived(this, input);

    }

    protected void parseAndExecuteCommand(final String input) {
        final String[] argv = parseInput(input);
        if (argv != null && argv.length > 0) {
            final Command c = getCommand(argv[0]);
            if (c != null) {
                final String result = executeCommand(c, argv);
                if (result != null) {
                    print(result);
                }
            } else {
                handler.commandNotFound(this, argv);
            }
        }
    }

    protected String executeCommand(final Command cmd, final String[] argv) {
        try {
            final Object r = cmd.execute(this, argv);
            return r != null ? "" + r : null;
        } catch (final Exception e) {
            handler.handleException(this, e, cmd, argv);
        }
        return null;
    }

    protected String parseCommandPrefix(final String input) {
        if (input == null) {
            return null;
        }
        if (!input.endsWith(" ")) {
            final String[] argv = parseInput(input);
            if (argv != null && argv.length > 0) {
                return argv[argv.length - 1];
            }
        }
        return "";
    }

    protected static String[] parseInput(final String input) {
        if (input != null && !"".equals(input.trim())) {
            final String[] temp = input.split(" ");
            if (temp != null && temp.length > 0) {
                final List<String> parsed = new ArrayList<String>(temp.length);
                String current = null;
                for (final String element : temp) {
                    final int quotCount = count(element, '\"');
                    if (quotCount > 0 && quotCount % 2 != 0) {
                        // uneven number of quotes star or end combining params
                        if (current != null) {
                            parsed.add(current + " " + element.replaceAll("\"", "")); // end
                            current = null;
                        } else {
                            current = element.replaceAll("\"", ""); // start
                        }
                    } else if (current != null) {
                        current += " " + element.replaceAll("\"", "");
                    } else {
                        parsed.add(element.replaceAll("\"", ""));
                    }
                }

                // TODO: actually this is not quite right: We have an open quote
                // somewhere. Exception maybe?
                if (current != null) {
                    parsed.add(current.replaceAll("\"", ""));
                }
                return parsed.toArray(new String[] {});
            }
        }
        return new String[] {};
    }

    protected static int count(final String sourceString, final char lookFor) {
        if (sourceString == null) {
            return -1;
        }
        int count = 0;
        for (int i = 0; i < sourceString.length(); i++) {
            final char c = sourceString.charAt(i);
            if (c == lookFor) {
                count++;
            }
        }
        return count;
    }

    public void print(final String output) {
        if (isConvertANSIToCSS) {
            getRpcProxy(ConsoleClientRpc.class).print("");
            appendWithProcessingANSICodes(output);
        } else
            getRpcProxy(ConsoleClientRpc.class).print(output);
    }

    /**
     * Print text with predefined in theme CSS class.
     *
     * @param output
     * @param className
     *            CSS class name for string
     */
    public void print(final String output, final String className) {
        if (className == null) {
            print(output);
            return;
        }
        getRpcProxy(ConsoleClientRpc.class).printWithClass(output, className);
    }

    public String getGreeting() {
        return config.greeting;
    }

    public String getPs() {
        return config.ps;
    }

    public int getMaxBufferSize() {
        return config.maxBufferSize;
    }

    public int getRows() {
        return config.rows;
    }

    public void setGreeting(final String greeting) {
        config.greeting = greeting;
        getRpcProxy(ConsoleClientRpc.class).setGreeting(greeting);
    }

    public void setPs(final String ps) {
        config.ps = ps == null ? DEFAULT_PS : ps;
        getRpcProxy(ConsoleClientRpc.class).setPs(config.ps);
    }

    public void setMaxBufferSize(final int lines) {
        config.maxBufferSize = lines > 0 ? lines : 0;
        getRpcProxy(ConsoleClientRpc.class).setMaxBufferSize(config.maxBufferSize);
    }

    public void setRows(final int rows) {
        config.rows = rows;
        if (config.rows < 1) {
            config.rows = 1;
        }
        if (config.rows > MAX_ROWS) {
            config.rows = MAX_ROWS;
        }
        getRpcProxy(ConsoleClientRpc.class).setRows(rows);
    }

    public int getCols() {
        return config.cols;
    }

    public void setCols(final int cols) {
        config.cols = cols;
        if (config.cols < 1) {
            config.cols = 1;
        }
        if (config.cols > MAX_COLS) {
            config.cols = MAX_COLS;
        }
        getRpcProxy(ConsoleClientRpc.class).setCols(config.cols);
    }

    public void prompt() {
        getRpcProxy(ConsoleClientRpc.class).prompt();
    }

    public void prompt(final String initialInput) {
        getRpcProxy(ConsoleClientRpc.class).prompt(initialInput);
    }

    public void println(final String string) {
        if (isConvertANSIToCSS) {
            getRpcProxy(ConsoleClientRpc.class).print("");
            appendWithProcessingANSICodes(string + "\n");
        } else
            getRpcProxy(ConsoleClientRpc.class).println(string);
    }

    /**
     * Print text with predefined in theme CSS class.
     *
     * @param string
     * @param className
     *            CSS class name for string
     */
    public void println(final String string, final String className) {
        if (className == null) {
            println(string);
            return;
        }
        getRpcProxy(ConsoleClientRpc.class).printlnWithClass(string, className);
    }

    /**
     * @param string
     *            text to append to the last printed line
     * @return this Console object
     */
    public Console append(final String string) {
        if (isConvertANSIToCSS)
            appendWithProcessingANSICodes(string);
        else
            getRpcProxy(ConsoleClientRpc.class).append(string);
        return this;
    }

    private void appendWithProcessingANSICodes(String sOutput) {
        String splitted[] = sOutput.split(ANSICodeConverter.ANSI_PATTERN);
        String notPrintedYet = new String(sOutput);
        for (int i = 0; i < splitted.length; i++) {
            String nextStr = splitted[i];
            if (i == 0 && nextStr.length() == 0)
                continue;
            String cssClasses = "";
            Pattern firstAnsi = Pattern.compile("^(" + ANSICodeConverter.ANSI_PATTERN + ")+\\Q" + nextStr + "\\E.*", Pattern.DOTALL);
            if (firstAnsi.matcher(notPrintedYet).matches()) {
                while (firstAnsi.matcher(notPrintedYet).matches()) {
                    String ansi = notPrintedYet.replaceAll("\\Q" + notPrintedYet.replaceAll("^(" + ANSICodeConverter.ANSI_PATTERN + "){1}", "") + "\\E", "");
                    cssClasses += ansiToCSSconverter.convertANSIToCSS(ansi) + " ";
                    notPrintedYet = notPrintedYet.replaceAll("^(" + ANSICodeConverter.ANSI_PATTERN + "){1}", "");
                }
                notPrintedYet = notPrintedYet.replaceAll("^\\Q" + nextStr + "\\E", "");
            } else
                notPrintedYet = notPrintedYet.replaceFirst("\\Q" + nextStr + "\\E", "");
            cssClasses = cssClasses.trim();
            if (cssClasses.length() > 0)
                getRpcProxy(ConsoleClientRpc.class).appendWithClass(nextStr, cssClasses);
            else
                getRpcProxy(ConsoleClientRpc.class).append(nextStr);
        }
    }

    /**
     * Append text with predefined in theme CSS class.
     *
     * @param string
     *            text to append to the last printed line
     * @param className
     *            CSS class name for string
     * @return this Console object
     */
    public Console append(final String string, final String className) {
        if (className == null)
            return append(string);
        getRpcProxy(ConsoleClientRpc.class).appendWithClass(string, className);
        return this;
    }

    public void newLine() {
        getRpcProxy(ConsoleClientRpc.class).newLine();
    }

    /**
     * Print new line only if new line not exists at the end of console
     */
    public void newLineIfNotEndsWithNewLine() {
        getRpcProxy(ConsoleClientRpc.class).newLineIfNotEndsWithNewLine();
    }

    public void reset() {
        getRpcProxy(ConsoleClientRpc.class).reset();
    }

    public void clear() {
        formFeed();
    }

    public void formFeed() {
        getRpcProxy(ConsoleClientRpc.class).ff();
    }

    public void carriageReturn() {
        getRpcProxy(ConsoleClientRpc.class).cr();
    }

    public void lineFeed() {
        getRpcProxy(ConsoleClientRpc.class).lf();
    }

    public void clearCommandHistory() {
        getRpcProxy(ConsoleClientRpc.class).clearHistory();
    }

    public void clearBuffer() {
        getRpcProxy(ConsoleClientRpc.class).clearBuffer();
    }

    public void scrollToEnd() {
        getRpcProxy(ConsoleClientRpc.class).scrollToEnd();
    }

    /**
     * Focus input element of console.
     */
    public void focusInput() {
        getRpcProxy(ConsoleClientRpc.class).focusInput();
    }

    /**
     * Gets the Tabulator index of this Focusable component.
     *
     * @see com.vaadin.ui.Component.Focusable#getTabIndex()
     */
    public int getTabIndex() {
        return tabIndex;
    }

    /**
     * Sets the Tabulator index of this Focusable component.
     *
     * @see com.vaadin.ui.Component.Focusable#setTabIndex(int)
     */
    public void setTabIndex(final int tabIndex) {
        this.tabIndex = tabIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void focus() {
        super.focus();
    }

    /* PrintStream implementation for console output. */

    public PrintStream getPrintStream() {
        if (printStream == null) {
            printStream = new PrintStream(new OutputStream() {

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                @Override
                public void write(final int b) throws IOException {
                    buffer.write(b);
                    // Line buffering
                    if (13 == b) {
                        flush();
                    }
                }

                @Override
                public void flush() throws IOException {
                    super.flush();
                    buffer.flush();
                    Console.this.print(buffer.toString());
                    buffer.reset();
                }
            }, true);
        }
        return printStream;
    }

    /* Generic command handling */

    /**
     * Add a Command to this Console.
     *
     * This will override the any commands of the same name available via
     * {@link CommandProvider}.
     */
    public void addCommand(final String name, final Command cmd) {
        commands.put(name, cmd);
    }

    /**
     * Remove a command from this console.
     *
     * This does not remove Command available from {@link CommandProvider}.
     *
     * @param cmdName
     */
    public void removeCommand(final String cmdName) {
        commands.remove(cmdName);
    }

    /**
     * Get a Command by its name.
     *
     * @param cmdName
     * @return
     */
    public Command getCommand(final String cmdName) {

        // Try directly registered command first
        Command cmd = commands.get(cmdName);
        if (cmd != null) {
            return cmd;
        }

        // Ask from the providers
        if (commandProviders != null) {
            for (final CommandProvider cp : commandProviders) {
                cmd = cp.getCommand(this, cmdName);
                if (cmd != null) {
                    return cmd;
                }
            }
        }

        // Not found
        return null;
    }

    /**
     * Get the current Console Handler.
     *
     * @return
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * Set the handler for this console.
     *
     * @see Handler
     * @param handler
     */
    public void setHandler(final Handler handler) {
        this.handler = handler != null ? handler : new DefaultConsoleHandler();
    }

    public ANSICodeConverter getANSIToCSSConverter() {
        return ansiToCSSconverter;
    }

    public void setANSIToCSSConverter(ANSICodeConverter converter) {
        this.ansiToCSSconverter = converter != null ? converter : new DefaultANSICodeConverter();
    }

    /**
     * Converting raw output with ANSI escape sequences to output with
     * CSS-classes.
     *
     * @return
     */
    public boolean isConvertANSIToCSS() {
        return isConvertANSIToCSS;
    }

    /**
     * Converting raw output with ANSI escape sequences to output with
     * CSS-classes.
     *
     * @param isConvertANSIToCSS
     */
    public void setConvertANSIToCSS(boolean isConvertANSIToCSS) {
        this.isConvertANSIToCSS = isConvertANSIToCSS;
    }

    /**
     * Get map of available commands in this Console.
     *
     * @return
     */
    public Set<String> getCommands() {
        final Set<String> res = new HashSet<String>();
        if (commandProviders != null) {
            for (final CommandProvider cp : commandProviders) {
                if (cp.getAvailableCommands(this) != null)
                    res.addAll(cp.getAvailableCommands(this));
            }
        }
        res.addAll(commands.keySet());
        return Collections.unmodifiableSet(res);
    }
}
