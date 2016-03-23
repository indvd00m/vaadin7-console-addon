package org.vaadin7.console.client;

import com.google.gwt.core.client.JavaScriptObject;

public class TextConsoleConfig extends JavaScriptObject {

    public static TextConsoleConfig newInstance() {
        return JavaScriptObject.createObject().cast();
    }

    protected TextConsoleConfig() {
    }

    public final native int getCols() /*-{
                                        return this.cols;
                                        }-*/;

    public final native void setCols(int cols) /*-{
                                                this.cols = cols;
                                                }-*/;

    public final native int getRows() /*-{
                                        return this.rows;
                                        }-*/;

    public final native void setRows(int rows) /*-{
                                                this.rows = rows;
                                                }-*/;

    public final native void setGreeting(String greeting) /*-{
                                                            this.greeting = greeting;
                                                            }-*/;

    public final native String getGreeting() /*-{
                                                return this.greeting;
                                                }-*/;

    public final native void setWrap(boolean w) /*-{
                                                this.wrap = w;
                                                }-*/;

    public final native boolean isWrap() /*-{
                                             return this.wrap;
                                             }-*/;

    public final native void setPrintPromptOnInput(boolean p) /*-{
                                                                this.PrintPromptOnInput = p;
                                                                }-*/;

    public final native boolean isPrintPromptOnInput() /*-{
                                                        return this.PrintPromptOnInput;
                                                        }-*/;

    public final native void setScrollLock(boolean s) /*-{
                                                            this.isScrollLock = s;
                                                            }-*/;

    public final native boolean isScrollLock() /*-{
                                                        return this.isScrollLock;
                                                        }-*/;

    public final native void setScrolledToEnd(boolean s) /*-{
                                                            this.isScrolledToEnd = s;
                                                            }-*/;

    public final native boolean isScrolledToEnd() /*-{
                                                    return this.isScrolledToEnd;
                                                    }-*/;

    public final native void setPs(String ps) /*-{
                                                this.ps = ps;
                                                }-*/;

    public final native String getPs() /*-{
                                        return this.ps;
                                        }-*/;

    public final native int getMaxBufferSize() /*-{
                                                return this.bufferRows;
                                                }-*/;

    public final native void setMaxBufferSize(int rows) /*-{
                                                        this.bufferRows = rows;
                                                        }-*/;

    public final native void setShouldTrim(boolean s) /*-{
                                                        this.shouldTrim = s;
                                                        }-*/;

    public final native boolean shouldTrim() /*-{
                                                return this.shouldTrim;
                                                }-*/;

}
