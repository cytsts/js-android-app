package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc;

import com.google.gson.annotations.SerializedName;

/**
 * Created by aleksandrdakhno on 4/27/17.
 */

public class JavascriptMessage {
    private MessageType type;
    private String command;
    private Object parameter;

    enum MessageType {
        @SerializedName("callback") CALLBACK,
        @SerializedName("listener") LISTENER
    }

    public MessageType getType() {
        return type;
    }

    public String getCommand() {
        return command;
    }

    public Object getParameters() {
        return parameter;
    }

    @Override
    public String toString() {
        return "JavascriptMessage{" +
                "type='" + type + '\'' +
                ", command='" + command + '\'' +
                ", parameter=" + parameter +
                '}';
    }
}
