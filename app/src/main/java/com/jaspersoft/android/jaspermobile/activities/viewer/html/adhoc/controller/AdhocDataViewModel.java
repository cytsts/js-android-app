package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.controller;

import com.jaspersoft.android.sdk.widget.report.renderer.ChartType;

import java.util.List;

/**
 * Created by aleksandrdakhno on 4/28/17.
 */

public interface AdhocDataViewModel {
    // Operations
    enum Operation {
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

    void subscribeOperationListener(OperationListener operationListener);
    void unsubscribeOperationListener(OperationListener operationListener);

    // Events
    class Event {
        private EventType type;
        private Object data;

        public Event(EventType type, Object data) {
            this.type = type;
            this.data = data;
        }

        public EventType getType() {
            return type;
        }

        public Object getData() {
            return data;
        }

        public enum EventType {
            ENVIRONMENT_PREPARING,
            ENVIRONMENT_READY,
            ERROR
        }
    }

    interface EventListener {
        void onEventReceived(Event event);
    }

    void subscribeEventListener(EventListener eventListener);
    void unsubscribeEventListener(EventListener eventListener);

    // Generals operations
    void run();
    void refresh();
    void destroy();
    void applyFilters();

    // Canvas operations
    void askAvailableChartTypes();
    void changeCanvasType(ChartType canvasType);
    List<ChartType> getCanvasTypes();
    ChartType getCurrentCanvasType();
}
