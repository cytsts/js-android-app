package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model;

import android.content.Context;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller.AdhocDataViewController;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller.AdhocDataViewModelListener;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.AdhocDataViewExecutorApi;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.AdhocDataViewVisualizeExecutor;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.VisualizeExecutor;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.internal.di.components.AdhocDataViewModelComponent;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import javax.inject.Inject;

/**
 * Created by aleksandrdakhno on 4/28/17.
 */

public class AdhocDataViewModel implements AdhocDataViewController {

    @Inject
    JasperServer mServer;

    private AdhocDataViewExecutorApi executor;
    private AdhocDataViewModelListener listener;

    public AdhocDataViewModel(Context context, WebView webView, ResourceLookup resourceLookup) {
        getComponent(context).inject(this);
        executor = new AdhocDataViewVisualizeExecutor(context, webView, resourceLookup.getUri());
    }

    /*
     * AdhocDataViewController
     */

    @Override
    public void subscribe(AdhocDataViewModelListener listener) {
        this.listener = listener;
    }

    @Override
    public void unsubscribe(AdhocDataViewModelListener listener) {
        this.listener = null;
    }

    @Override
    public void prepare() {
        if (listener != null) {
            listener.onPreparingStart();
        }
        executor.prepare(mServer.getBaseUrl(), new VisualizeExecutor.Completion() {
            @Override
            public void success() {
                if (listener != null) {
                    listener.onPreparingEnd();
                }
            }

            @Override
            public void failed(String error) {
                if (listener != null) {
                    listener.onPreparingFailed(error);
                }
            }
        });
    }

    @Override
    public void run() {
        if (listener != null) {
            listener.onOperationStart();
        }
        executor.run(new VisualizeExecutor.Completion() {
            @Override
            public void success() {
                if (listener != null) {
                    listener.onOperationEnd();
                }
            }

            @Override
            public void failed(String error) {
                if (listener != null) {
                    listener.onOperationFailed(error);
                }
            }
        });
    }

    @Override
    public void destroy() {
        executor.destroy();
    }

    /*
     * Private
     */

    private AdhocDataViewModelComponent getComponent(Context context) {
        return GraphObject.Factory.from(context)
                .getProfileComponent()
                .plusAdhocDataViewModel();
    }

}
