package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.webenvironment.webinterface;

/**
 * Created by aleksandrdakhno on 4/27/17.
 */

class JavascriptResponse {
    private ResponseType type;
    private String operation;
    private Object parameters;

    enum ResponseType {
        CALLBACK,
        LISTENER
    }

    JavascriptResponse(ResponseType type, String operation, Object parameters) {
        this.type = type;
        this.operation = operation;
        this.parameters = parameters;
    }

    ResponseType getType() {
        return type;
    }

    String getOperation() {
        return operation;
    }

    Object getParameter() {
        return parameters;
    }

    @Override
    public String toString() {
        return "JavascriptResponse{" +
                "type='" + type + '\'' +
                ", operation='" + operation + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
