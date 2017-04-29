package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.webenvironment.javascriptmessage;

/**
 * Created by aleksandrdakhno on 4/27/17.
 */

public class JavascriptMessage {
    private MessageType type;
    private String command;
    private Object parameters;

    public enum MessageType {
        CALLBACK,
        LISTENER
    }

    public JavascriptMessage(MessageType type, String command, Object parameters) {
        this.type = type;
        this.command = command;
        this.parameters = parameters;
    }

    public MessageType getType() {
        return type;
    }

    public String getCommand() {
        return command;
    }

    public Object getParameter() {
        return parameters;
    }

    @Override
    public String toString() {
        return "JavascriptMessage{" +
                "type='" + type + '\'' +
                ", command='" + command + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
