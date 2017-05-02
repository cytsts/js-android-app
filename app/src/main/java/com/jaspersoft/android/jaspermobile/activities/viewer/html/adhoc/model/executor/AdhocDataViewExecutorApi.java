package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.VisualizeExecutor.Completion;

/**
 * Created by aleksandrdakhno on 4/29/17.
 */

public interface AdhocDataViewExecutorApi {
    void prepare(String baseUrl, Completion completion);
    void setScale(float scale);
    void run(Completion completion);
    void refresh(Completion completion);
    void askAvailableCanvasTypes(Completion completion);
    void changeCanvasType(String canvasType, Completion completion);
    void destroy();
}
