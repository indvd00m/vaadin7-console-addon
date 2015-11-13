package org.vaadin7.console.ansi;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default converter for numeric-only ANSI codes.
 *
 * @author indvdum
 * 31.05.2011 13:55:47
 *
 */
public class DefaultANSICodeConverter implements ANSICodeConverter {

    protected static final Map<Integer, String> ANSI_CSS = new HashMap<Integer, String>();
    static{
        ANSI_CSS.put(0, "");
        ANSI_CSS.put(1, "term-font-weight-bolder");
        ANSI_CSS.put(2, "term-font-weight-lighter");
        ANSI_CSS.put(3, "term-font-style-italic");
        ANSI_CSS.put(4, "term-text-decoration-underline");
        ANSI_CSS.put(5, "term-text-decoration-blink");
        ANSI_CSS.put(6, "term-text-decoration-blink");

        ANSI_CSS.put(9, "term-text-decoration-line-through");

        ANSI_CSS.put(22, "term-font-weight-normal");

        ANSI_CSS.put(24, "term-text-decoration-none");
        ANSI_CSS.put(25, "term-text-decoration-none");

        ANSI_CSS.put(29, "term-text-decoration-none");

        ANSI_CSS.put(30, "term-color-black");
        ANSI_CSS.put(31, "term-color-red");
        ANSI_CSS.put(32, "term-color-green");
        ANSI_CSS.put(33, "term-color-yellow");
        ANSI_CSS.put(34, "term-color-blue");
        ANSI_CSS.put(35, "term-color-purple");
        ANSI_CSS.put(36, "term-color-teal");
        ANSI_CSS.put(37, "term-color-silver");

        ANSI_CSS.put(40, "term-background-color-black");
        ANSI_CSS.put(41, "term-background-color-red");
        ANSI_CSS.put(42, "term-background-color-green");
        ANSI_CSS.put(43, "term-background-color-yellow");
        ANSI_CSS.put(44, "term-background-color-blue");
        ANSI_CSS.put(45, "term-background-color-purple");
        ANSI_CSS.put(46, "term-background-color-teal");
        ANSI_CSS.put(47, "term-background-color-silver");

        ANSI_CSS.put(53, "term-text-decoration-overline");
        ANSI_CSS.put(55, "term-text-decoration-none");
    }

    public DefaultANSICodeConverter(){
        init();
    }

    protected void init(){

    }

    @Override
    public String convertANSIToCSS(String ansiEscapeSequence) {
        StringBuilder cssClasses = new StringBuilder("");
        Matcher matcher = Pattern.compile(ANSI_PATTERN).matcher(ansiEscapeSequence);
        if(!matcher.matches())
            return cssClasses.toString();
        String codes[] = matcher.group(2).split(";");
        for(int i = 0; i < codes.length; i++){
            if(!codes[i].matches("[0-9]+")) // processing only numeric codes
                continue;
            int nCode = Integer.valueOf(codes[i]);
            if(nCode == 38){ // Set xterm-256 text color, nothing to do
                i+=2;
                continue;
            }
            if(nCode == 48){ // Set xterm-256 background color, nothing to do
                i+=2;
                continue;
            }
            String cssClass = getCSSClass(nCode);
            if(cssClass == null)
                continue;
            cssClass = cssClass.trim();
            if(cssClass.length() > 0)
                cssClasses.append(cssClass).append(" ");
        }
        return cssClasses.toString().trim();
    }

    /**
     * Converting one numeric ANSI code to one or many CSS-classes.
     *
     * @param ansiCode ANSI code
     * @return CSS-class(es).
     */
    protected String getCSSClass(int ansiCode) {
        return ANSI_CSS.get(ansiCode);
    }

    /**
     * Converting one string ANSI code to one or many CSS-classes.
     *
     * @param ansiCode ANSI code
     * @return CSS-class(es).
     */
    protected String getCSSClass(String ansiCode) {
        return "";
    }

}
