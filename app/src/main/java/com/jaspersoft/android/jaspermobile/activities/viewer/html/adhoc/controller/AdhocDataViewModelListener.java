package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller;

/**
 * Created by aleksandrdakhno on 4/28/17.
 */

public interface AdhocDataViewModelListener {
    void onPreparingStart();
    void onPreparingEnd();
    void onPreparingFailed(String error);
    void onOperationStart();
    void onOperationEnd();
    void onOperationFailed(String error);
}
