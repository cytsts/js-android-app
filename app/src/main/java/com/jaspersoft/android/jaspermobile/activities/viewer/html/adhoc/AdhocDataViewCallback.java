package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc;

/**
 * Created by aleksandrdakhno on 4/21/17.
 */

public interface AdhocDataViewCallback {
    void onScriptLoaded();
    void onLoadStart();
    void onLoadDone();
    void onLoadError(String error);
}
