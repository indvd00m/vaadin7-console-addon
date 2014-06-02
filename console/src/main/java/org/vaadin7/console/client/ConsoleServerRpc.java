package org.vaadin7.console.client;

import com.vaadin.shared.communication.ServerRpc;

/**
 * ServerRpc is used to pass events from client to server.
 * 
 * @author indvdum (gotoindvdum[at]gmail[dot]com)
 * @since 22.05.2014 15:30:06
 * 
 */
public interface ConsoleServerRpc extends ServerRpc {

	public void setHeight(String height);

	public void setWidth(String width);

	public void setCols(int cols);

	public void setRows(int rows);

	public void input(String input);

	public void suggest(String input);

}
