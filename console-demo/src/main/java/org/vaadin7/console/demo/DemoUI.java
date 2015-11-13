package org.vaadin7.console.demo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import org.vaadin7.console.Console;
import org.vaadin7.console.Console.Command;
import org.vaadin7.console.Console.CommandProvider;
import org.vaadin7.console.ObjectInspector;

@Theme("demo")
@Title("Console Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {

    protected static final String HELP = "Sample Vaadin shell. Following command are available:\n";
    private ObjectInspector inspector;

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "org.vaadin7.console.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {
        getPage().setTitle("Vaadin Console Demo");

        TabSheet ts = new TabSheet();
        ts.setSizeFull();
        setContent(ts);

        VerticalLayout vl1 = new VerticalLayout();
        vl1.setWidth("100%");
        ts.addTab(vl1, "Console");

        vl1.addComponent(new Label(
                "This console implements a test environment for itself.<br> All methods in console class are exposed as commands in the console itself.",
                ContentMode.HTML));
        vl1.addComponent(new Label("Type 'help' to list all available commands and 'help <command>' to get parameter help.'"));

        // # 1

        // Create a console
        final Console console = new Console();
        console.setImmediate(true);
        vl1.addComponent(console);

        // Size and greeting
        console.setPs("}> ");
        console.setCols(80);
        console.setRows(24);
        console.setMaxBufferSize(24);
        console.setGreeting("Welcome to Vaadin console demo.");
        console.reset();
        console.focus();

        // Publish the methods in the Console class itself for testing purposes.
        console.addCommandProvider(inspector = new ObjectInspector(console));

        // Add help command
        Command helpCommand = new Console.Command() {
            private static final long serialVersionUID = 2838665604270727844L;

            public String getUsage(Console console, String[] argv) {
                return argv[0] + " <command>";
            }

            public Object execute(Console console, String[] argv) throws Exception {
                if (argv.length == 2) {
                    Command hc = console.getCommand(argv[1]);
                    ArrayList<String> cmdArgv = new ArrayList<String>(Arrays.asList(argv));
                    cmdArgv.remove(0);
                    return "Usage: " + hc.getUsage(console, cmdArgv.toArray(new String[] {}));
                }
                return listAvailableCommands();
            }
        };

        // Bind the same command with multiple names
        console.addCommand("help", helpCommand);
        console.addCommand("info", helpCommand);
        console.addCommand("man", helpCommand);
        // #

        // # 2
        Command systemCommand = new Command() {
            private static final long serialVersionUID = -5733237166568671987L;

            public Object execute(Console console, String[] argv) throws Exception {
                Process p = Runtime.getRuntime().exec(argv);
                InputStream in = p.getInputStream();
                StringBuilder o = new StringBuilder();
                InputStreamReader r = new InputStreamReader(in);
                int c = -1;
                try {
                    while ((c = r.read()) != -1) {
                        o.append((char) c);
                    }
                } catch (IOException e) {
                    o.append("[truncated]");
                } finally {
                    if (r != null) {
                        r.close();
                    }
                }
                return o.toString();
            }

            public String getUsage(Console console, String[] argv) {
                // TODO Auto-generated method stub
                return null;
            }
        };

        // #
        console.addCommand("ls", systemCommand);

        // Add sample command
        DummyCmd dummy = new DummyCmd();
        console.addCommand("dir", dummy);
        console.addCommand("cd", dummy);
        console.addCommand("mkdir", dummy);
        console.addCommand("rm", dummy);
        console.addCommand("pwd", dummy);
        console.addCommand("more", dummy);
        console.addCommand("less", dummy);
        console.addCommand("exit", dummy);

        HorizontalLayout pl = new HorizontalLayout();
        pl.setSpacing(true);
        vl1.addComponent(pl);
        final TextField input = new TextField(null, "print this");
        pl.addComponent(input);
        pl.addComponent(new Button("print", new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {
                console.print("" + input.getValue());
            }
        }));

        pl.addComponent(new Button("println", new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {
                console.println("" + input.getValue());
            }
        }));

        VerticalLayout vl2 = new VerticalLayout();
        vl2.setWidth("100%");
        ts.addTab(vl2, "Chat");

        Console chat = new Console();
        chat.setImmediate(true);
        chat.addStyleName("chat");
        chat.setSizeFull();
        chat.setMaxBufferSize(400);
        chat.setPs(" > ");
        final String username = "username";
        chat.setGreeting("Welcome, " + username);
        chat.setWrap(true);
        chat.setPrintPromptOnInput(false);
        chat.setScrollLock(true);
        chat.setConvertANSIToCSS(true);
        chat.reset();
        chat.println("\033[01;31mThis \033[01;32m console \033[01;33m element \033[01;34m used \033[01;35m as \033[01;36m a \033[01;37m fake \033[01;31m chat");
        chat.prompt();
        chat.focusInput();
        final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        chat.addCommandProvider(new CommandProvider() {

            @Override
            public Command getCommand(Console console, String commandName) {
                return new Command() {

                    @Override
                    public Object execute(Console console, String[] argv) throws Exception {
                        String cmd = "";
                        for (int i = 0; i < argv.length; i++)
                            if (argv[i].trim().length() != 0)
                                cmd += (i == 0 ? "" : " ") + argv[i];

                        console.newLineIfNotEndsWithNewLine();
                        console.append("[" + dateFormat.format(new Date()) + "] ", "chat-time");
                        console.append(username, "chat-my-nick");
                        console.append(": ");
                        console.append(cmd);
                        console.scrollToEnd();
                        return null;
                    }

                    @Override
                    public String getUsage(Console console, String[] argv) {
                        return null;
                    }

                };
            }

            @Override
            public Set<String> getAvailableCommands(Console console) {
                return null;
            }
        });
        vl2.addComponent(chat);
    }

    protected String readToString(InputStream in) {
        StringBuilder o = new StringBuilder();
        InputStreamReader r = new InputStreamReader(in);
        int c = -1;
        try {
            while ((c = r.read()) != -1) {
                o.append((char) c);
            }
        } catch (IOException e) {
            o.append("[truncated]");
        }
        return o.toString();
    }

    public static class DummyCmd implements Console.Command {
        private static final long serialVersionUID = -7725047596507450670L;

        public Object execute(Console console, String[] argv) throws Exception {
            return "Sorry, this is not a real shell and '" + argv[0] + "' is unsupported. Try 'help' instead.";
        }

        public String getUsage(Console console, String[] argv) {
            return "Sorry, this is not a real shell and '" + argv[0] + "' is unsupported. Try 'help' instead.";
        }
    }

    protected String listAvailableCommands() {
        StringBuilder res = new StringBuilder();
        for (String cmd : inspector.getAvailableCommands()) {
            res.append(" ");
            res.append(cmd);
        }
        return res.toString().trim();
    }

}
