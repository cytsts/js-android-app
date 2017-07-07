package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment.VisualizeWebEnvironment;

/**
 * Created by aleksandrdakhno on 4/29/17.
 */

public class AdhocDataViewVisualizeExecutor extends VisualizeExecutor implements AdhocDataViewExecutorApi {

    private String resourceUri;

    public AdhocDataViewVisualizeExecutor(VisualizeWebEnvironment webEnvironment, String resourceUri) {
        super(webEnvironment);
        this.resourceUri = resourceUri;
    }

    /*
     * AdhocDataViewExecutorApi
     */

    @Override
    public void askIsReady(Completion completion) {
        completions.put("askIsReady", completion);
        completion.before();
        executeJavascriptCode(javascriptCodeForAskIsReady());
    }

    @Override
    public void prepare(Completion completion) {
        completion.before();
        super.prepare(completion);
    }

    @Override
    public void setScale(float scale) {
        executeJavascriptCode(javascriptCodeForSetScale(scale));
    }

    @Override
    public void run(Completion completion) {
        completions.put("run", completion);
        completion.before();
        executeJavascriptCode(javascriptCodeForRun());
    }

    @Override
    public void refresh(Completion completion) {
        completions.put("refresh", completion);
        completion.before();
        executeJavascriptCode(javascriptCodeForRefresh());
    }

    @Override
    public void askAvailableCanvasTypes(Completion completion) {
        completions.put("availableTypes", completion);
        completion.before();
        executeJavascriptCode(javascriptCodeForAvailableChartTypes());
    }

    @Override
    public void changeCanvasType(String chartType, Completion completion) {
        completions.put("changeCanvasType", completion);
        completion.before();
        executeJavascriptCode(javascriptCodeForChangeCanvasType(chartType));
    }

    @Override
    public void destroy() {
        executeJavascriptCode(javascriptCodeForDestroy());
        super.destroy();
    }

    @Override
    public void applyFilters() {

    }

    /*
     * Private
     */

    private String javascriptCodeForAskIsReady() {
        String executeScript = String.format("javascript:JasperMobile.Environment.askIsReady()");
        return executeScript;
    }

    private String javascriptCodeForSetScale(float scale) {
        String executeScript = String.format("javascript:Environment.setScale('%.2f')", scale);
        return executeScript;
    }

    private String javascriptCodeForRun() {
        String executeScript = String.format("javascript:JasperMobile.AdhocDataView.API.run('%s')", resourceUri);
        return executeScript;
    }

    private String javascriptCodeForRefresh() {
        String executeScript = String.format("javascript:JasperMobile.AdhocDataView.API.refresh()");
        return executeScript;
    }

    private String javascriptCodeForAvailableChartTypes() {
        String executeScript = String.format("javascript:JasperMobile.AdhocDataView.API.availableTypes()");
        return executeScript;
    }

    private String javascriptCodeForChangeCanvasType(String canvasType) {
        String executeScript = String.format("javascript:JasperMobile.AdhocDataView.API.changeCanvasType('%s')", canvasType);
        return executeScript;
    }

    private String javascriptCodeForDestroy() {
        String executeScript = String.format("javascript:JasperMobile.AdhocDataView.API.destroy()");
        return executeScript;
    }
}
