package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.VisualizeExecutor.Completion;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

/**
 * Created by aleksandrdakhno on 4/29/17.
 */

public interface AdhocDataViewExecutorApi {
    void askIsReady(Completion completion);
    void prepare(Completion completion);
    void setScale(float scale);
    void run(Completion completion);
    void refresh(Completion completion);
    void askAvailableCanvasTypes(Completion completion);
    void changeCanvasType(String canvasType, Completion completion);
    void destroy();
    void applyFilters(List<ReportParameter> filterParams, Completion completion);
}
