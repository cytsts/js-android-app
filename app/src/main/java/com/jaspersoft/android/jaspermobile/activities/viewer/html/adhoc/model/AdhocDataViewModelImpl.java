package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model;

import android.content.Context;
import android.os.Handler;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller.AdhocDataViewModel;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.AdhocDataViewExecutorApi;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.AdhocDataViewVisualizeExecutor;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.VisualizeExecutor;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.internal.di.components.AdhocDataViewModelComponent;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.widget.report.renderer.ChartType;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by aleksandrdakhno on 4/28/17.
 */

public class AdhocDataViewModelImpl implements AdhocDataViewModel {

    @Inject
    JasperServer mServer;

    private AdhocDataViewExecutorApi executor;
    private Context context;
    private OperationListener operationListener;
    private EventListener eventListener;

    // TODO: move to separate storage
    private List<ChartType> canvasTypes;
    private ChartType currentCanvasType;
    private boolean isPrepared = false;

    private interface SuccessCompletion {
        void onSuccess(Object data);
    }
    private interface FailedCompletion {
        void onFailed();
    }

    public AdhocDataViewModelImpl(Context context, WebView webView, ResourceLookup resourceLookup) {
        getComponent(context).inject(this);
        this.context = context;
        executor = new AdhocDataViewVisualizeExecutor(context, webView, mServer.getBaseUrl(), resourceLookup.getUri());
    }

    /*
     * AdhocDataViewModel
     */

    @Override
    public void subscribeOperationListener(OperationListener operationListener) {
        this.operationListener = operationListener;
    }

    @Override
    public void unsubscribeOperationListener(OperationListener operationListener) {
        this.operationListener = null;
    }

    @Override
    public void subscribeEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
        if (!isPrepared) {
            notifyListenerOnEventOnUiThread(new Event(Event.EventType.ENVIRONMENT_PREPARING, null));
            executor.askIsReady(new VisualizeExecutor.Completion() {
                @Override
                public void success(Object data) {
                    Map<String, Boolean> response = new Gson().fromJson((String) data, new TypeToken<Map<String, Boolean>>() {}.getType());
                    boolean isReady = response.get("isReady");
                    SuccessCompletion completion = new SuccessCompletion() {
                        @Override
                        public void onSuccess(Object data) {
                            isPrepared = true;
                            notifyListenerOnEventOnUiThread(new Event(Event.EventType.ENVIRONMENT_READY, null));
                        }
                    };
                    if (!isReady) {
                        prepare(completion);
                    } else {
                        completion.onSuccess(null);
                    }
                }

                @Override
                public void failed(String error) {

                }
            });
        }
    }

    @Override
    public void unsubscribeEventListener(EventListener eventListener) {
        this.eventListener = null;
    }

    @Override
    public void run() {
        notifyListenerOnOperationStartOnUiThread(Operation.RUN);
        executor.run(completionForOperation(Operation.RUN));
    }

    @Override
    public void refresh() {
        notifyListenerOnOperationStartOnUiThread(Operation.REFRESH);
        executor.refresh(completionForOperation(Operation.REFRESH));
    }

    @Override
    public void askAvailableChartTypes() {
        notifyListenerOnOperationStartOnUiThread(Operation.ASK_AVAILABLE_CANVAS_TYPES);
        if (canvasTypes != null) {
            notifyListenerOnOperationEndOnUiThread(Operation.ASK_AVAILABLE_CANVAS_TYPES);
        } else {
            executor.askAvailableCanvasTypes(completionForOperation(Operation.ASK_AVAILABLE_CANVAS_TYPES, new SuccessCompletion() {
                @Override
                public void onSuccess(Object data) {
                    canvasTypes = new Gson().fromJson((String) data, new TypeToken<List<ChartType>>() {}.getType());
                }
            }));
        }
    }

    @Override
    public List<ChartType> getCanvasTypes() {
        return canvasTypes;
    }

    @Override
    public void changeCanvasType(ChartType canvasType) {
        if (canvasType.equals(currentCanvasType)) {
            return;
        }
        currentCanvasType = canvasType;
        notifyListenerOnOperationStartOnUiThread(Operation.CHANGE_CANVAS_TYPE);
        executor.changeCanvasType(canvasType.getName(), completionForOperation(Operation.CHANGE_CANVAS_TYPE));
    }

    public ChartType getCurrentCanvasType() {
        return currentCanvasType;
    }

    @Override
    public void destroy() {
        executor.destroy();
    }

    /*
     * Private
     */

    public void prepare(final SuccessCompletion completion) {
        executor.prepare(new VisualizeExecutor.Completion() {
            @Override
            public void success(Object data) {
                completion.onSuccess(data);
            }

            @Override
            public void failed(String error) {
                notifyListenerOnEventOnUiThread(new Event(Event.EventType.ERROR, error));
            }
        });
    }

    private AdhocDataViewModelComponent getComponent(Context context) {
        return GraphObject.Factory.from(context)
                .getProfileComponent()
                .plusAdhocDataViewModel();
    }

    private VisualizeExecutor.Completion completionForOperation(final Operation operation) {
        return new VisualizeExecutor.Completion() {
            @Override
            public void success(Object data) {
                notifyListenerOnOperationEndOnUiThread(operation);
            }

            @Override
            public void failed(String error) {
                notifyListenerOnOperationFailedOnUiThread(operation, error);
            }
        };
    }

    private VisualizeExecutor.Completion completionForOperation(final Operation operation, final SuccessCompletion completion) {
        return new VisualizeExecutor.Completion() {
            @Override
            public void success(Object data) {
                completion.onSuccess(data);
                notifyListenerOnOperationEndOnUiThread(operation);
            }

            @Override
            public void failed(String error) {
                notifyListenerOnOperationFailedOnUiThread(operation, error);
            }
        };
    }

    private VisualizeExecutor.Completion completionForOperation(final Operation operation, final FailedCompletion completion) {
        return new VisualizeExecutor.Completion() {
            @Override
            public void success(Object data) {
                notifyListenerOnOperationEndOnUiThread(operation);
            }

            @Override
            public void failed(String error) {
                completion.onFailed();
                notifyListenerOnOperationFailedOnUiThread(operation, error);
            }
        };
    }

    /*
     * Event notifying
     */

    private void notifyListenerOnEventOnUiThread(final Event event) {
        if (eventListener == null) {
            return;
        }
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                eventListener.onEventReceived(event);
            }
        });
    }

    /*
     * Operation notifying
     */

    private void notifyListenerOnOperationStartOnUiThread(final Operation operation) {
        if (operationListener == null) {
            return;
        }
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                operationListener.onOperationStart(operation);
            }
        });
    }

    private void notifyListenerOnOperationEndOnUiThread(final Operation operation) {
        if (operationListener == null) {
            return;
        }
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                operationListener.onOperationEnd(operation);
            }
        });
    }

    private void notifyListenerOnOperationFailedOnUiThread(final Operation operation, final String error) {
        if (operationListener == null) {
            return;
        }
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                operationListener.onOperationFailed(operation, error);
            }
        });
    }
}
