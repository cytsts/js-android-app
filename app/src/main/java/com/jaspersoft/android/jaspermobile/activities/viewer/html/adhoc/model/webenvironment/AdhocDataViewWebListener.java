package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.webenvironment;

/**
 * Created by aleksandrdakhno on 4/21/17.
 */

public interface AdhocDataViewWebListener {
    void onEnvironmentReady();
    void onVisualizeReady();
    void onVisualizeFailed(String error);
    void onOperationDone();
    void onOperationError(String error);
}
