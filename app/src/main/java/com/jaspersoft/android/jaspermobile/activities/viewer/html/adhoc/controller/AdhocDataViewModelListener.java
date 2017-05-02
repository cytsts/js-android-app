package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller.AdhocDataViewController.Operation;

/**
 * Created by aleksandrdakhno on 4/28/17.
 */

public interface AdhocDataViewModelListener {
    void onOperationStart(Operation operation);
    void onOperationEnd(Operation operation);
    void onOperationFailed(Operation operation, String error);
}
