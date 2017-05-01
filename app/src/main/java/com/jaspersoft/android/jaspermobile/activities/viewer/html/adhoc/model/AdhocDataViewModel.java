package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model;

import android.content.Context;
import android.os.Handler;
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
    private Context context;

    public AdhocDataViewModel(Context context, WebView webView, ResourceLookup resourceLookup) {
        getComponent(context).inject(this);
        this.context = context;
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
        notifyListenerOnOperationStartOnUiThread();
        executor.prepare(mServer.getBaseUrl(), new VisualizeExecutor.Completion() {
            @Override
            public void success() {
                if (listener != null) {
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onPreparingEnd();
                        }
                    });
                }
            }

            @Override
            public void failed(final String error) {
                if (listener != null) {
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onPreparingFailed(error);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void run() {
        notifyListenerOnOperationStartOnUiThread();
        executor.run(completionForOperation());
    }

    @Override
    public void refresh() {
        notifyListenerOnOperationStartOnUiThread();
        executor.refresh(completionForOperation());
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

    private VisualizeExecutor.Completion completionForOperation() {
        return new VisualizeExecutor.Completion() {
            @Override
            public void success() {
                notifyListenerOnOperationEndOnUiThread();
            }

            @Override
            public void failed(String error) {
                notifyListenerOnOperationFailedOnUiThread(error);
            }
        };
    }

    private void notifyListenerOnOperationStartOnUiThread() {
        if (listener == null) {
            return;
        }
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                listener.onOperationStart();
            }
        });
    }

    private void notifyListenerOnOperationEndOnUiThread() {
        if (listener == null) {
            return;
        }
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                listener.onOperationEnd();
            }
        });
    }

    private void notifyListenerOnOperationFailedOnUiThread(final String error) {
        if (listener == null) {
            return;
        }
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                listener.onOperationFailed(error);
            }
        });
    }
}
