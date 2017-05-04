package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment;

/**
 * Created by aleksandrdakhno on 4/29/17.
 */

public class VisualizeWebResponse {
    private Type type;
    private String operation;
    private Object dataParameters;
    private Object errorParameters;

    public enum Type {
        Event, Operation
    }

    public VisualizeWebResponse(Type type, String operation, Object dataParameters, Object errorParameters) {
        this.type = type;
        this.operation = operation;
        this.dataParameters = dataParameters;
        this.errorParameters = errorParameters;
    }

    public Type getType() {
        return type;
    }

    public String getOperation() {
        return operation;
    }

    public Object getDataParameters() {
        return dataParameters;
    }

    public Object getErrorParameters() {
        return errorParameters;
    }
}
