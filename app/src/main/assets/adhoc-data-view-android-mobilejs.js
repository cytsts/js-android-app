// Start Point
document.addEventListener("DOMContentLoaded", function(event) {
    // intercepting network calls
    XMLHttpRequest.prototype.reallySend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function(body) {
//        JasperMobile.Callback.log("send body: " + body);
        this.reallySend(body);
    };
    JasperMobile.Callback.callback("onEnvironmentReady", null);
    JasperMobile.Logger.log("inside of 'DOMContentLoaded'");
}, false);

window.onerror = function myErrorHandler(message, source, lineno, colno, error) {
//    JasperMobile.Callback.listener("JasperMobile.Events.Window.OnError", {
//        "error" : {
//            "code" : "window.onerror",
//            "message" : message + " " + source + " " + lineno + " " + colno + " " + error,
//            "source" : source
//        }
//    });
    JasperMobile.Logger.log(message);
    // run default handler
    return false;
};

// Specifying scopes
var JasperMobile = {
    Helper: {
        addOnResizeListener: function() {
            window.onresize = function() {
                JasperMobile.Logger.log("window.onresize");
                JasperMobile.AdhocDataView.resizeFn();
            };
        },
        removeOnResizeListener: function() {
            window.onresize = null;
        },
    },
    Logger: null,
    Environment : {
        setScale: function(scale) {
            document.body.style.transform = "scale(" + scaleValue + ")";
            document.body.style.transformOrigin = "0% 0%";
            document.body.style.width = scale * 100 + "%";
            document.body.style.height = scale * 100 + "%";
        }
    },
    Callback : null,
    VIZ : null,
    AdhocDataView: null
};

JasperMobile.Logger = {
    log: function(message) {
        console.log(message);
    },
    logObject: function(object) {
        if (object == null) {
            this.log("'null' object can't be logged");
            return;
        }
        this.logJson(this.jsonFromObject(object));
    },
    logJson: function(json) {
        this.log(JSON.stringify(json));
    },
    jsonFromObject: function(object) {
        var listOfProperties = Object.getOwnPropertyNames(object);
        var objectJSON = {};
        for(var propertyIndex in listOfProperties) {
            var propertyName = listOfProperties[propertyIndex];
            var property = object[propertyName];
            var propertyType = typeof property;

            // construct structure
            var structure = {
                "type" : propertyType
            };
            if (propertyType === 'object') {
                structure["object"] = this.jsonFromObject(property);
            } else {
                structure["value"] = property;
            }
            objectJSON[propertyName] = structure;
        }
        return objectJSON;
    }
};

JasperMobile.Callback = {
    logEnable: true, // For now changes by hands
    postMessage: function(params) {
        window.Android.postMessage(JSON.stringify(params));
    },
    callback: function(command, parameters) {
        this.postMessage(
            {
                "type" : "callback",
                "command"    : command,
                "parameters" : parameters
            }
        );
    },
    listener: function(command, parameters) {
        this.postMessage(
            {
                "type" : "listener",
                "command"    : command,
                "parameters" : parameters
            }
        );
    }
};

JasperMobile.VIZ = {
    instance: null,
    authFn: {
        loginFn: function(properties, request) {
            return (new jQuery.Deferred()).resolve();
        }
    },
    prepareFn: function() {
        JasperMobile.Logger.log("prepareFn");
        var that = this;
        return function(v) {
            JasperMobile.Logger.log("inside of function with 'v'");
            that.instance = v;
            that.visualizeReadyCallback();
        }
    },
    prepare: function() {
        visualize(
            this.authFn,
            this.prepareFn(),
            this.visualizeFailedCallback
        );
    },
    visualizeReadyCallback: function() {
        JasperMobile.Logger.log("successVisualizeReadyCallback");
        JasperMobile.Callback.callback("onVisualizeReady", null);
    },
    visualizeFailedCallback: function(errorObject) {
        JasperMobile.Logger.log("errorVisualizeFailedCallback");
        JasperMobile.Logger.logObject(errorObject);
        JasperMobile.Callback.callback("onVisualizeFailed", errorObject);
    }
}

JasperMobile.AdhocDataView = {
    instance: null,
    initInstance: function(resourceUrl) {
        JasperMobile.Logger.log("initInstance");
        if (this.instance == null) {
            JasperMobile.Logger.log("create a new adhoc view instance");
            this.instance = JasperMobile.VIZ.instance.adhocView(
                {
                    resource: resourceUrl,
                    container: "#container",
                    runImmediately: false
                }
            );
        } else {
            JasperMobile.Logger.log("reuse existing adhoc view instance");
        }
    },
    runFn: function(resourceUrl) {
        JasperMobile.Logger.log("runFn");
        JasperMobile.Logger.log("this.adhocViewInstance: " + this.adhocViewInstance);
        this.initInstance(resourceUrl);
        var that = this;
        this.instance.run()
        .done(
            function(data) {
                JasperMobile.Logger.log("runFn done with data: " + data);
                JasperMobile.Helper.addOnResizeListener();
                that.successOperationCallback();
            }
        )
        .fail(
            that.errorOperationCallback
        );
    },
    refreshFn: function() {
        this.instance.refresh();
    },
    resizeFn: function() {
        JasperMobile.Logger.log("resizeFn");
        JasperMobile.Helper.removeOnResizeListener();
        this.runFn();
    },
    askAvailableTypesFn: function() {
        var availableTypes = this.instance.data().query.availableTypes;
    },
    changeCanvasTypeFn: function(newCanvasType) {
        this.instance.canvas({type : newCanvasType}).run();
    },
    successOperationCallback: function() {
        JasperMobile.Logger.log("successOperationCallback");
        JasperMobile.Callback.callback("onOperationDone", null);
    },
    errorOperationCallback: function(errorObject) {
        JasperMobile.Logger.log("errorOperationCallback");
        JasperMobile.Logger.logObject(errorObject);
        JasperMobile.Callback.callback("onOperationError", errorObject);
    }
};

JasperMobile.AdhocDataView.API = {
    run: function(resourceUrl) {
        JasperMobile.AdhocDataView.runFn(resourceUrl);
    },
    refresh: function() {
        JasperMobile.AdhocDataView.refreshFn();
    }
};