package com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc;

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
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.javascriptmessage.JavascriptMessage;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.adhoc.javascriptmessage.JavascriptMessage.MessageType;
import com.jaspersoft.android.jaspermobile.webview.WebInterface;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aleksandrdakhno on 4/21/17.
 */

public class AdhocDataViewWebInterface extends WebInterface {

    private final AdhocDataViewWebInterfaceListener listener;

    private AdhocDataViewWebInterface(AdhocDataViewWebInterfaceListener listener) {
        this.listener = listener;
    }

    public static WebInterface from(AdhocDataViewWebInterfaceListener listener) {
        return new AdhocDataViewWebInterface(listener);
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
        builder.registerTypeAdapter(JavascriptMessage.class, new JsonDeserializer<JavascriptMessage>() {
            @Override
            public JavascriptMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                return new JavascriptMessage(
                        parseMessageType(jsonObject),
                        parseCommand(jsonObject),
                        parseParameters(jsonObject)
                );
            }

            private MessageType parseMessageType(JsonObject jsonObject) {
                JsonElement typeElement = jsonObject.get("type");
                return MessageType.valueOf(typeElement.getAsString().toUpperCase());
            }

            private String parseCommand(JsonObject jsonObject) {
                JsonElement commandElement = jsonObject.get("command");
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

        JavascriptMessage javascriptMessage = builder.create().fromJson(message, JavascriptMessage.class);
        switch (javascriptMessage.getType()) {
            case CALLBACK: {
                processCallback(javascriptMessage.getCommand(), javascriptMessage.getParameter());
                break;
            }
            case LISTENER: {
                Log.d("ADViewWebInterface", "listener for: " + javascriptMessage.getCommand());
                break;
            }
        }
    }

    /*
     * Private Methods
     */

    private void processCallback(final String command, final Object parameters) {
        handleCallback(new Runnable() {
            @Override
            public void run() {
                switch (command) {
                    case "onEnvironmentReady":
                        listener.onEnvironmentReady();
                        break;
                    case "onVisualizeReady":
                        listener.onVisualizeReady();
                        break;
                    case "onVisualizeFailed": {
                        String message = null;
                        if (parameters instanceof Map) {
                            Map<String, Object> parametersAsMap = (Map<String, Object>) parameters;
                            message = (String) parametersAsMap.get("message");
                        }
                        listener.onVisualizeFailed(message);
                        break;
                    }
                    case "onOperationDone":
                        listener.onOperationDone();
                        break;
                    case "onOperationError": {
                        String message = null;
                        if (parameters instanceof Map) {
                            Map<String, Object> parametersAsMap = (Map<String, Object>) parameters;
                            message = (String) parametersAsMap.get("message");
                        }
                        listener.onOperationError(message);
                        break;
                    }
                    default:
                        throw new RuntimeException("Unsupported command");
                }
            }
        });
    }
}
