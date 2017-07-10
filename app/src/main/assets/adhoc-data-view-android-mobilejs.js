// Start Point
document.addEventListener("DOMContentLoaded", function(event) {
    // intercepting network calls
    XMLHttpRequest.prototype.reallySend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function(body) {
//        JasperMobile.Callback.log("send body: " + body);
        this.reallySend(body);
    };
    JasperMobile.Listener.event("onEnvironmentReady", null);
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
        isReady: false,
        setScale: function(scale) {
            document.body.style.transform = "scale(" + scaleValue + ")";
            document.body.style.transformOrigin = "0% 0%";
            document.body.style.width = scale * 100 + "%";
            document.body.style.height = scale * 100 + "%";
        },
        askIsReady: function() {
            JasperMobile.AdhocDataView.Callback.success("askIsReady", {isReady: this.isReady});
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
        try {
            var json = this.jsonFromObject(object);
            this.logJson(json);
        } catch(error) {
            this.log("exception (" + error + ") while logging object: " + object);
        }
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

JasperMobile.Listener = {
    logEnable: true, // For now changes by hands
    postMessage: function(params) {
        try {
            window.Android.postMessage(JSON.stringify(params));
        } catch(error) {
            console.log(error);
        }
    },
    operation: function(operation, parameters) {
        this.postMessage(
            {
                "type"       : "callback",
                "operation"  : operation,
                "parameters" : parameters
            }
        );
    },
    event: function(operation, parameters) {
        this.postMessage(
            {
                "type"       : "event",
                "operation"  : operation,
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
        var that = this;
        return function(v) {
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
        JasperMobile.Listener.event("onVisualizeReady", null);
        JasperMobile.Environment.isReady = true;
    },
    visualizeFailedCallback: function(errorObject) {
        JasperMobile.Listener.event("onVisualizeFailed", {
            operationType: "prepareVisualize",
            errorObject: errorObject
        });
    }
}

JasperMobile.AdhocDataView = {
    elemToLinkPairs: null,
    instance: null,
    initInstance: function(resourceUrl) {
        JasperMobile.Logger.log("initInstance");
        if (this.instance == null) {
            JasperMobile.Logger.log("create a new adhoc view instance");
            this.instance = JasperMobile.VIZ.instance.adhocView(
                {
                    resource: resourceUrl,
                    container: "#container",
                    runImmediately: false,
                    linkOptions: {
                        events: {
                            "click" : function(ev, data, defaultHadlder, extendedData) {
                                JasperMobile.Logger.log("events (click), ev: " + ev);
                                JasperMobile.Logger.log("events (click), data: " + data);
                                JasperMobile.Logger.logObject(data);
                                JasperMobile.Logger.log("events (click), defaultHadlder: " + defaultHadlder);
                                JasperMobile.Logger.log("events (click), extendedData: " + extendedData);
                                JasperMobile.Logger.logObject(extendedData);
                            }
                        },
                        beforeRender: function(elemToLinkPairs) {
                            JasperMobile.Logger.log("beforeRender elemToLinkPairs: " + elemToLinkPairs);
                            JasperMobile.AdhocDataView.elemToLinkPairs = elemToLinkPairs;
                        }
                    }
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
                JasperMobile.AdhocDataView.Callback.success("run", null);
            }
        )
        .fail(
            JasperMobile.AdhocDataView.Callback.fail("run")
        );
    },
    refreshFn: function() {
        this.instance.refresh()
        .done(
            function(data) {
                JasperMobile.Logger.log("refreshFn done with data: " + data);
                JasperMobile.AdhocDataView.Callback.success("refresh", null);
            }
        )
        .fail(
            JasperMobile.AdhocDataView.Callback.fail("refresh")
        );
    },
    resizeFn: function() {
        JasperMobile.Logger.log("resizeFn");
        JasperMobile.Helper.removeOnResizeListener();
        this.runFn();
    },
    availableTypesFn: function() {
        var availableTypes = this.instance.data().query.availableTypes;
        var requiredFormat = []; // [{name:"some name}, ...]; for reusing of existing code for 'ChartType'
        for (var i = 0, len = availableTypes.length; i < len; i++) {
            requiredFormat.push(
                {
                    name: availableTypes[i]
                }
            );
        }
        JasperMobile.AdhocDataView.Callback.success("availableTypes", requiredFormat);
    },
    changeCanvasTypeFn: function(newCanvasType) {
        this.instance.canvas({type : newCanvasType}).run()
        .done(
            function(data) {
                JasperMobile.Logger.log("changeCanvasTypeFn done with data: " + data);
                JasperMobile.AdhocDataView.Callback.success("changeCanvasType", null);
            }
        )
        .fail(
            JasperMobile.AdhocDataView.Callback.fail("changeCanvasType")
        );
    },
    destroyFn: function() {
        this.instance.destroy();
        this.instance = null;

        // move to helpers
        var dialogs = document.getElementsByClassName("jr-mDialog");
        if (dialogs.length == 1) {
            document.body.removeChild(dialogs[0]);
        }
    },
    applyFiltersFn: function(params) {
        this.instance.params(params).run()
        .done(
            function(data) {
                JasperMobile.Logger.log("applyFiltersFn done with data: " + data);
                JasperMobile.AdhocDataView.Callback.success("applyFilters", null);
            }
        )
        .fail(
            function(error) {
                JasperMobile.AdhocDataView.Callback.fail("applyFilters");
            }
        );
    }
};

JasperMobile.AdhocDataView.Callback = {
    success: function(operation, data) {
            var parameters;
            if (data == null) {
                parameters = {
                    "operationType": operation
                }
            } else {
                // TODO: verify that data is object
                parameters = {
                    "operationType": operation,
                    "dataObject": JSON.stringify(data)
                }
            }
            JasperMobile.Listener.operation("onOperationDone", parameters);
        },
    fail: function(operation) {
        return function(errorObject) {
            JasperMobile.Listener.operation("onOperationDone", {
                "operationType": operation,
                "errorObject": errorObject
            });
        }
    }
}

JasperMobile.AdhocDataView.API = {
    run: function(resourceUrl) {
        try {
            JasperMobile.AdhocDataView.runFn(resourceUrl);
        } catch(error) {
            JasperMobile.Logger.log("exception of run: " + error);
//            JasperMobile.Logger.logObject(error);
        }
    },
    refresh: function() {
        JasperMobile.AdhocDataView.refreshFn();
    },
    availableTypes: function() {
        JasperMobile.AdhocDataView.availableTypesFn();
    },
    changeCanvasType: function(canvasType) {
        JasperMobile.AdhocDataView.changeCanvasTypeFn(canvasType);
    },
    destroy: function() {
        JasperMobile.AdhocDataView.destroyFn();
    },
    applyFilters: function(params) {
        JasperMobile.AdhocDataView.applyFiltersFn(params);
    }
};