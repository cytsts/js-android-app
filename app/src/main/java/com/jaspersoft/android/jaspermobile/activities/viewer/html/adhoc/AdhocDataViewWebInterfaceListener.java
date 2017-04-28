package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc;

/**
 * Created by aleksandrdakhno on 4/21/17.
 */

public interface AdhocDataViewWebInterfaceListener {
    void onEnvironmentReady();
    void onVisualizeReady();
    void onVisualizeFailed(String error);
    void onOperationDone();
    void onOperationError(String error);
}
