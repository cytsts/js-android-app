package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment.webinterface;

/**
 * Created by aleksandrdakhno on 4/29/17.
 */

public class VisualizeWebResponse {
    private String operation;
    private Object parameters;
    private String error;

    public VisualizeWebResponse(String operation, Object parameters, String error) {
        this.operation = operation;
        this.parameters = parameters;
        this.error = error;
    }

    public String getOperation() {
        return operation;
    }

    public Object getParameters() {
        return parameters;
    }

    public String getError() {
        return error;
    }
}
