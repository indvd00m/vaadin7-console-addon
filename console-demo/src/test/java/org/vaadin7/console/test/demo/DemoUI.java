package org.vaadin7.console.test.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import org.vaadin7.console.Console;
import org.vaadin7.console.Console.Command;
import org.vaadin7.console.Console.CommandProvider;
import org.vaadin7.console.ObjectInspector;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

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
		Console chat = new Console();
		vl1.addComponent(chat);

		// Size and greeting
		chat.setImmediate(true);
		chat.addStyleName("chat");
		chat.setSizeFull();
		chat.setMaxBufferSize(400);
		chat.setPs(" > ");
		final String username = "%username%";
		chat.setGreeting("Welcome, " + username);
		chat.setWrap(true);
		chat.setPrintPromptOnInput(false);
		chat.setSmartScrollToEnd(true);
		chat.setConvertANSIToCSS(true);
		chat.reset();
		chat.println("\033[01;31mThis \033[01;32m console \033[01;33m element \033[01;34m used \033[01;35m as \033[01;36m a \033[01;37m fake \033[01;31m chat");
		chat.prompt();
		
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
