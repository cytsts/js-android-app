package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model;

/**
 * Created by aleksandrdakhno on 4/28/17.
 */

public interface AdhocDataViewModelListener {
    void onPreparingStart();
    void onPreparingEnd();
    void onPreparingFailed();
    void onOperationStart();
    void onOperationEnd();
    void onOperationFailed();
}
