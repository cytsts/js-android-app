package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment;

import android.annotation.SuppressLint;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.jaspersoft.android.jaspermobile.webview.WebInterface;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aleksandrdakhno on 4/21/17.
 */

class VisualizeWebInterface extends WebInterface {

    private VisualizeWebEnvironment.Listener listener;

    private static VisualizeWebInterface sharedInstance;
    public static VisualizeWebInterface getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new VisualizeWebInterface();
        }
        return sharedInstance;
    }

    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    @Override
    public void exposeJavascriptInterface(WebView webView) {
        webView.addJavascriptInterface(this, "Android");
    }

    void addListener(VisualizeWebEnvironment.Listener listener) {
        sharedInstance.listener = listener;
    }

    void removeListener(VisualizeWebEnvironment.Listener listener) {
        sharedInstance.listener = null;
    }

    /*
     * Adhoc Callback Interface
     */

    @JavascriptInterface
    public void postMessage(final String message) {
        Log.d("VisualizeWebInterface", "message: " + message);
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(JavascriptResponse.class, new JsonDeserializer<JavascriptResponse>() {
            @Override
            public JavascriptResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                return new JavascriptResponse(
                        parseMessageType(jsonObject),
                        parseCommand(jsonObject),
                        parseParameters(jsonObject)
                );
            }

            private ResponseType parseMessageType(JsonObject jsonObject) {
                JsonElement typeElement = jsonObject.get("type");
                return ResponseType.valueOf(typeElement.getAsString().toUpperCase());
            }

            private String parseCommand(JsonObject jsonObject) {
                JsonElement commandElement = jsonObject.get("operation");
                return commandElement.getAsString();
            }

            private Object parseParameters(JsonObject jsonObject) {
                JsonElement parametersElement = jsonObject.get("parameters");
                if ( parametersElement.isJsonNull() ) {
                    return null;
                } else if (parametersElement.isJsonPrimitive()) {
                    return parametersElement.getAsString();
                } else if (parametersElement.isJsonArray()) {
                    // TODO: implement
                } else if (parametersElement.isJsonObject()) {
                    String parametersAsString = parametersElement.toString();
                    Map<String, Object> parametersAsMap = new Gson().fromJson(parametersAsString, new TypeToken<HashMap<String, Object>>() {}.getType());
                    return parametersAsMap;
                }
                return null;
            }
        });

        JavascriptResponse response = builder.create().fromJson(message, JavascriptResponse.class);
        switch (response.getType()) {
            case CALLBACK: {
                processCallback(response.getOperation(), response.getParameter());
                break;
            }
            case EVENT: {
                processEvent(response.getOperation(), response.getParameter());
                break;
            }
        }
    }

    /*
     * Events Methods
     */

    private void processEvent(final String event, final Object parameters) {
        handleCallback(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onEventReceived(responseFromDataForEvent(event, parameters));
                }
            }
        });
    }

    private VisualizeWebResponse responseFromDataForEvent(String event, Object objectData) {
        if (objectData instanceof Map) {
            Map<String, Object> parametersAsMap = (Map<String, Object>) objectData;
            Object dataObject = parametersAsMap.get("dataObject");
            Object errorObject = parametersAsMap.get("errorObject");
            return new VisualizeWebResponse(
                    VisualizeWebResponse.Type.Operation,
                    event,
                    dataObject,
                    errorObject
            );
        } else {
            return new VisualizeWebResponse(
                    VisualizeWebResponse.Type.Operation,
                    event,
                    null,
                    null
            );
        }
    }

    /*
     * Callbacks Methods
     */

    private void processCallback(final String operation, final Object parameters) {
        handleCallback(new Runnable() {
            @Override
            public void run() {
                switch (operation) {
                    case "onOperationDone":
                        if (listener != null) {
                            listener.onOperationDone(responseFromDataForOperation(parameters));
                        }
                        break;
                    default:
                        throw new RuntimeException("Unsupported command");
                }
            }
        });
    }

    private VisualizeWebResponse responseFromDataForOperation(Object objectData) {
        if (objectData instanceof Map) {
            Map<String, Object> parametersAsMap = (Map<String, Object>) objectData;
            String operationType = (String) parametersAsMap.get("operationType");
            Object dataObject = parametersAsMap.get("dataObject");
            Object errorObject = parametersAsMap.get("errorObject");
            return new VisualizeWebResponse(
                    VisualizeWebResponse.Type.Operation,
                    operationType,
                    dataObject,
                    errorObject
            );
        } else {
            // TODO: may be throw error?
        }
        return null;
    }

    /*
     * JavascriptResponse
     */

    private enum ResponseType {
        CALLBACK,
        EVENT
    }

    private class JavascriptResponse {
        private ResponseType type;
        private String operation;
        private Object parameters;

        JavascriptResponse(ResponseType type, String operation, Object parameters) {
            this.type = type;
            this.operation = operation;
            this.parameters = parameters;
        }

        ResponseType getType() {
            return type;
        }

        String getOperation() {
            return operation;
        }

        Object getParameter() {
            return parameters;
        }

        @Override
        public String toString() {
            return "JavascriptResponse{" +
                    "type='" + type + '\'' +
                    ", operation='" + operation + '\'' +
                    ", parameters=" + parameters +
                    '}';
        }
    }
}
