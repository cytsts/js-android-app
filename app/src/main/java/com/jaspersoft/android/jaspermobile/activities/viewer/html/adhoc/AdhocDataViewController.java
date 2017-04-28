package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.AdhocDataViewModelListener;

/**
 * Created by aleksandrdakhno on 4/28/17.
 */

public interface AdhocDataViewController {
    void subscribe(AdhocDataViewModelListener listener);
    void unsubscribe(AdhocDataViewModelListener listener);
    void run();
}
