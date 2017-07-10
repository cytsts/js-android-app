package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment.VisualizeWebEnvironment;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

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
    public void applyFilters(List<ReportParameter> filterParams, Completion completion) {
        completions.put("applyFilters", completion);
        completion.before();
        executeJavascriptCode(javascriptCodeForApplyFilters(configFiltersString(filterParams)));
    }

    /*
     * Private
     */

    private String javascriptCodeForAskIsReady() {
        return "javascript:JasperMobile.Environment.askIsReady()";
    }

    private String javascriptCodeForSetScale(float scale) {
        return String.format("javascript:Environment.setScale('%.2f')", scale);
    }

    private String javascriptCodeForRun() {
        return String.format("javascript:JasperMobile.AdhocDataView.API.run('%s')", resourceUri);
    }

    private String javascriptCodeForRefresh() {
        return "javascript:JasperMobile.AdhocDataView.API.refresh()";
    }

    private String javascriptCodeForAvailableChartTypes() {
        return "javascript:JasperMobile.AdhocDataView.API.availableTypes()";
    }

    private String javascriptCodeForChangeCanvasType(String canvasType) {
        return String.format("javascript:JasperMobile.AdhocDataView.API.changeCanvasType('%s')", canvasType);
    }

    private String javascriptCodeForDestroy() {
        return "javascript:JasperMobile.AdhocDataView.API.destroy()";
    }

    private String javascriptCodeForApplyFilters(String params) {
        return String.format("javascript:JasperMobile.AdhocDataView.API.applyFilters(%s)", params);
    }

    /*
     * Work with filters
     */

    private String configFiltersString(List<ReportParameter> filterParams) {
        StringBuilder paramsBuilder = new StringBuilder("");
        paramsBuilder.append("{");
        for (ReportParameter parameter : filterParams) {
            paramsBuilder.append(String.format("%s : %s, ", parameter.getName(), parameter.getValues()));
        }
        paramsBuilder.append("}");
        return paramsBuilder.toString();
    }
}
