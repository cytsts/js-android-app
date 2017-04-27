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
                JasperMobile.VIZ.AdhocDataView.resizeFn();
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
    VIZ : {
        AdhocDataView: null
    }
};

JasperMobile.Logger = {
    log: function(message) {
        console.log(message);
    },
    logObject: function(object) {
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

JasperMobile.VIZ.AdhocDataView = {
    adhocViewInstance: null,
    resourceUrl: null,
    authFn: {
        loginFn: function(properties, request) {
            return (new jQuery.Deferred()).resolve();
        }
    },
    successRunCallback: function() {
        JasperMobile.Logger.log("success of loading adhoc view");
        JasperMobile.Helper.addOnResizeListener();
        JasperMobile.Callback.callback("onLoadDone", null);
    },
    errorRunCallback: function(errorObject) {
        JasperMobile.Logger.logObject(errorObject);
        JasperMobile.Callback.callback("onLoadError", errorObject);
    },
    runFn: function(resourceUrl) {
        this.resourceUrl = resourceUrl;
        var adhocStruct = {
            resource: resourceUrl,
            container: "#container",
            success: this.successRunCallback,
            error: this.errorRunCallback
        };
        var that = this;
        return function(v) {
            JasperMobile.Callback.callback("onLoadStart", null);
            that.adhocViewInstance = v.adhocView(adhocStruct);
        }
    },
    refreshFn: function() {
        this.adhocViewInstance.refresh();
    },
    resizeFn: function() {
        JasperMobile.Helper.removeOnResizeListener();
        this.adhocViewInstance.destroy();
        this.API.run(this.resourceUrl);
    },
    askAvailableTypesFn: function() {
        var availableTypes = this.adhocViewInstance.data().query.availableTypes;
    },
    changeCanvasTypeFn: function(newCanvasType) {
        this.adhocViewInstance.canvas({type : newCanvasType}).run();
    }
};

JasperMobile.VIZ.AdhocDataView.API = {
    run: function(resourceUrl) {
        visualize(
            JasperMobile.VIZ.AdhocDataView.authFn,
            JasperMobile.VIZ.AdhocDataView.runFn(resourceUrl)
        );
    },
    refresh: function() {
        JasperMobile.VIZ.AdhocDataView.refreshFn();
    }
};