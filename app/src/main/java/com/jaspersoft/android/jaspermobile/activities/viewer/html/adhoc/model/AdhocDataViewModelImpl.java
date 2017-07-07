package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model;

import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller.AdhocDataViewModel;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.AdhocDataViewExecutorApi;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.AdhocDataViewVisualizeExecutor;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.model.executor.VisualizeExecutor;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment.VisualizeWebEnvironment;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.widget.report.renderer.ChartType;

import java.util.List;
import java.util.Map;

/**
 * Created by aleksandrdakhno on 4/28/17.
 */

public class AdhocDataViewModelImpl implements AdhocDataViewModel {

    private AdhocDataViewExecutorApi executor;
    private VisualizeWebEnvironment webEnvironment;
    private Notifier notifier;
    private CanvasTypesStore canvasTypesStore;

    private OperationListener operationListener;
    private EventListener eventListener;

    private boolean isPrepared = false;

    private ResourceLookup resourceLookup;

    public ResourceLookup getResourceLookup() {
        return resourceLookup;
    }

    private interface Completion {
        void execute(Object data);
    }

    public AdhocDataViewModelImpl(VisualizeWebEnvironment webEnvironment, ResourceLookup resourceLookup) {
        this.webEnvironment = webEnvironment;
        this.resourceLookup = resourceLookup;
        executor = new AdhocDataViewVisualizeExecutor(webEnvironment, resourceLookup.getUri());
        notifier = new Notifier();
        canvasTypesStore = new CanvasTypesStore();
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
            executor.askIsReady(new VisualizeExecutor.Completion() {
                @Override
                public void before() {
                    notifier.notifyListenerOnEventOnUiThread(new Event(Event.EventType.ENVIRONMENT_PREPARING, null));
                }

                @Override
                public void success(Object data) {
                    Map<String, Boolean> response = new Gson().fromJson((String) data, new TypeToken<Map<String, Boolean>>() {}.getType());
                    boolean isReady = response.get("isReady");
                    Completion completion = new Completion() {
                        @Override
                        public void execute(Object data) {
                            isPrepared = true;
                            notifier.notifyListenerOnEventOnUiThread(new Event(Event.EventType.ENVIRONMENT_READY, null));
                        }
                    };
                    if (!isReady) {
                        prepare(completion);
                    } else {
                        completion.execute(null);
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
        executor.run(
                completionForOperation(Operation.RUN, null)
        );
    }

    @Override
    public void refresh() {
        executor.refresh(
                completionForOperation(Operation.REFRESH, null)
        );
    }

    @Override
    public void askAvailableChartTypes() {
        if (canvasTypesStore.canvasTypes != null) {
            notifier.notifyListenerOnOperationStartOnUiThread(Operation.ASK_AVAILABLE_CANVAS_TYPES);
            notifier.notifyListenerOnOperationEndOnUiThread(Operation.ASK_AVAILABLE_CANVAS_TYPES);
        } else {
            executor.askAvailableCanvasTypes(completionForOperation(Operation.ASK_AVAILABLE_CANVAS_TYPES, new Completion() {
                @Override
                public void execute(Object data) {
                    canvasTypesStore.canvasTypes = new Gson().fromJson((String) data, new TypeToken<List<ChartType>>() {}.getType());
                }
            }));
        }
    }

    @Override
    public List<ChartType> getCanvasTypes() {
        return canvasTypesStore.canvasTypes;
    }

    @Override
    public void changeCanvasType(final ChartType canvasType) {
        if (canvasType.equals(canvasTypesStore.currentCanvasType)) {
            return;
        }
        executor.changeCanvasType(
                canvasType.getName(),
                completionForOperation(Operation.CHANGE_CANVAS_TYPE, new Completion() {
                    @Override
                    public void execute(Object data) {
                        canvasTypesStore.currentCanvasType = canvasType;
                    }
                })
        );
    }

    @Override
    public ChartType getCurrentCanvasType() {
        return canvasTypesStore.currentCanvasType;
    }

    @Override
    public void destroy() {
        executor.destroy();
    }

    @Override
    public void applyFilters() {
        executor.applyFilters();
    }

    /*
     * Private
     */

    public void prepare(final Completion completion) {
        executor.prepare(new VisualizeExecutor.Completion() {
            @Override
            public void before() {

            }

            @Override
            public void success(Object data) {
                completion.execute(data);
            }

            @Override
            public void failed(String error) {
                notifier.notifyListenerOnEventOnUiThread(new Event(Event.EventType.ERROR, error));
            }
        });
    }

    private VisualizeExecutor.Completion completionForOperation(final Operation operation, final Completion completion) {
        return new VisualizeExecutor.Completion() {
            @Override
            public void before() {
                notifier.notifyListenerOnOperationStartOnUiThread(operation);
            }

            @Override
            public void success(Object data) {
                if (completion != null) {
                    completion.execute(data);
                }
                notifier.notifyListenerOnOperationEndOnUiThread(operation);
            }

            @Override
            public void failed(String error) {
                notifier.notifyListenerOnOperationFailedOnUiThread(operation, error);
            }
        };
    }

    private class Notifier {
        /*
         * Event notifying
         */

        private void notifyListenerOnEventOnUiThread(final Event event) {
            if (eventListener == null) {
                return;
            }
            runOnUiThread(new Runnable() {
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
            runOnUiThread(new Runnable() {
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
            runOnUiThread(new Runnable() {
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    operationListener.onOperationFailed(operation, error);
                }
            });
        }

        private void runOnUiThread(Runnable runnable) {
            new Handler(webEnvironment.getContext().getMainLooper()).post(runnable);
        }
    }

    private class CanvasTypesStore {
        private List<ChartType> canvasTypes;
        private ChartType currentCanvasType;
    }
}
