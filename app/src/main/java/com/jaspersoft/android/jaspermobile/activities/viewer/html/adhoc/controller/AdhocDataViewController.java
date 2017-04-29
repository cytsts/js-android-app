package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller;

/**
 * Created by aleksandrdakhno on 4/28/17.
 */

public interface AdhocDataViewController {
    void subscribe(AdhocDataViewModelListener listener);
    void unsubscribe(AdhocDataViewModelListener listener);
    void prepare();
    void run();
}
