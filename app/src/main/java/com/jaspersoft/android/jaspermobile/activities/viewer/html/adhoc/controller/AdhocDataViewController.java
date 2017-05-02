package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller;

import com.jaspersoft.android.sdk.widget.report.renderer.ChartType;

import java.util.List;

/**
 * Created by aleksandrdakhno on 4/28/17.
 */

public interface AdhocDataViewController {
    enum Operation {
        PREPARE,
        RUN,
        REFRESH,
        ASK_AVAILABLE_CANVAS_TYPES,
        CHANGE_CANVAS_TYPE
    }
    void subscribe(AdhocDataViewModelListener listener);
    void unsubscribe(AdhocDataViewModelListener listener);
    void run();
    void refresh();
    void askAvailableChartTypes();
    void changeCanvasType(ChartType canvasType);
    List<ChartType> getCanvasTypes();
    ChartType getCurrentCanvasType();
    void destroy();
}
