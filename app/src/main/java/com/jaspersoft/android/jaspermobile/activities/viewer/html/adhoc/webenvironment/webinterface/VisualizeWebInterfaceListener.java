package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment.webinterface;

/**
 * Created by aleksandrdakhno on 4/21/17.
 */

public interface VisualizeWebInterfaceListener {
    void onEnvironmentReady();
    void onVisualizeReady();
    void onVisualizeFailed(String error);
    void onOperationDone(VisualizeWebResponse response);
    void onOperationError(VisualizeWebResponse response);
}
