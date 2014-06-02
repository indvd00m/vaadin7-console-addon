package org.vaadin7.console.ansi;

/**
 * Interface for ANSI-code-TO-CSS converter.
 * 
 * @author indvdum
 * 31.05.2011 13:45:28
 *
 */
public interface ANSICodeConverter {
	/**
	 * Pattern for one ANSI escape sequence.
	 */
	public String ANSI_PATTERN = "(\033\\[)([0-9a-zA-Z;]+)(m)";

	/**
	 * Converting ANSI escape sequence to space-separated CSS classes.
	 * 
	 * @param ansiEscapeSequence ANSI escape sequence
	 * @return CSS classes separated by spaces for this ANSI escape sequence
	 */
	public String convertANSIToCSS(String ansiEscapeSequence);
}
