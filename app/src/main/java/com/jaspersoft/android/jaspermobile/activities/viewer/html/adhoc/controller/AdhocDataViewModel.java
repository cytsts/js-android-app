package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller;

import com.jaspersoft.android.sdk.widget.report.renderer.ChartType;

import java.util.List;

/**
 * Created by aleksandrdakhno on 4/28/17.
 */

public interface AdhocDataViewModel {
    enum Operation {
        ASK_IS_READY,
        PREPARE,
        RUN,
        REFRESH,
        ASK_AVAILABLE_CANVAS_TYPES,
        CHANGE_CANVAS_TYPE
    }

    interface OperationListener {
        void onOperationStart(Operation operation);
        void onOperationEnd(Operation operation);
        void onOperationFailed(Operation operation, String error);
    }

    void subscribe(OperationListener listener);
    void unsubscribe(OperationListener listener);
    void run();
    void refresh();
    void askAvailableChartTypes();
    void changeCanvasType(ChartType canvasType);
    List<ChartType> getCanvasTypes();
    ChartType getCurrentCanvasType();
    void destroy();
}
