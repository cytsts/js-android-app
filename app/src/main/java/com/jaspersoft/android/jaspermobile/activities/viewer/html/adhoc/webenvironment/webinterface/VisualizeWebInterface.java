package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.webenvironment.webinterface;

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

public class VisualizeWebInterface extends WebInterface {

    private VisualizeWebInterfaceListener listener;

    private static VisualizeWebInterface sharedInstance;

    public static WebInterface from(VisualizeWebInterfaceListener listener) {
        if (sharedInstance == null) {
            sharedInstance = new VisualizeWebInterface();
        }
        sharedInstance.listener = listener;
        return sharedInstance;
    }

    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface"})
    @Override
    public void exposeJavascriptInterface(WebView webView) {
        webView.addJavascriptInterface(this, "Android");
    }

    /*
     * Adhoc Callback Interface
     */

    @JavascriptInterface
    public void postMessage(final String message) {
        Log.d("ADViewWebInterface", "message: " + message);
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
            case LISTENER: {
                Log.d("ADViewWebInterface", "listener for: " + response.getOperation());
                break;
            }
        }
    }

    /*
     * Private Methods
     */

    private void processCallback(final String operation, final Object parameters) {
        handleCallback(new Runnable() {
            @Override
            public void run() {
                switch (operation) {
                    case "onEnvironmentReady":
                        listener.onEnvironmentReady();
                        break;
                    case "onVisualizeReady":
                        listener.onVisualizeReady();
                        break;
                    case "onVisualizeFailed": {
                        listener.onVisualizeFailed(failResponseFromData(parameters).getError());
                        break;
                    }
                    case "onOperationDone":
                        listener.onOperationDone(successResponseFromData(parameters));
                        break;
                    case "onOperationError": {
                        listener.onOperationError(failResponseFromData(parameters));
                        break;
                    }
                    default:
                        throw new RuntimeException("Unsupported command");
                }
            }
        });
    }

    private VisualizeWebResponse successResponseFromData(Object objectData) {
        if (objectData instanceof Map) {
            Map<String, Object> parametersAsMap = (Map<String, Object>) objectData;
            String operationType = (String) parametersAsMap.get("operationType");
            Object data = parametersAsMap.get("data");
            return new VisualizeWebResponse(
                    operationType,
                    data,
                    null
            );
        } else {
            // TODO: may be throw error?
        }
        return null;
    }

    private VisualizeWebResponse failResponseFromData(Object data) {
        if (data instanceof Map) {
            Map<String, Object> parametersAsMap = (Map<String, Object>) data;
            String operationType = (String) parametersAsMap.get("operationType");
            Map<String, Object> errorObject = (Map<String, Object>) parametersAsMap.get("errorObject");
            String message = (String) errorObject.get("message");
            return new VisualizeWebResponse(
                    operationType,
                    null,
                    message
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
        LISTENER
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
