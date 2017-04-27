// Start Point
document.addEventListener("DOMContentLoaded", function(event) {
    // intercepting network calls
    XMLHttpRequest.prototype.reallySend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function(body) {
//        JasperMobile.Callback.log("send body: " + body);
        this.reallySend(body);
    };
    Android.onEnvironmentReady();
    JasperMobile.Logger.log("inside of 'DOMContentLoaded'");
}, false);

var Environment = {
    setScale: function(scale) {
        document.body.style.transform = "scale(" + scaleValue + ")";
        document.body.style.transformOrigin = "0% 0%";
        document.body.style.width = scale * 100 + "%";
        document.body.style.height = scale * 100 + "%";
    }
}

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
    Logger: {},
    Callback : {},
    VIZ : {}
}

JasperMobile.Logger = {
    log: function(message) {
        console.log(message);
    },
    logObject: function(object) {
        var jsonString = JSON.stringify(object);
        log(jsonString);
    }
}

JasperMobile.VIZ = {
    AdhocDataView: {}
}

JasperMobile.VIZ.AdhocDataView = {
    adhocViewObject: null,
    resourceUrl: null,
    authFn: {
        loginFn: function(properties, request) {
            return (new jQuery.Deferred()).resolve();
        }
    },
    successCallback: function() {
        JasperMobile.Logger.log("success of loading adhoc view");
        JasperMobile.Helper.addOnResizeListener();
        Android.onLoadDone();
    },
    errorCallback: function(errorObject) {
        JasperMobile.Logger.log("error of loading adhoc view: " + error);
        Android.onLoadError();
    },
    runFn: function(resourceUrl) {
        JasperMobile.VIZ.AdhocDataView.resourceUrl = resourceUrl;
        var adhocStruct = {
            resource: resourceUrl,
            container: "#container",
            success: JasperMobile.VIZ.AdhocDataView.successCallback,
            error: JasperMobile.VIZ.AdhocDataView.errorCallback
        };
        return function(v) {
            Android.onLoadStart();
            JasperMobile.VIZ.AdhocDataView.adhocViewObject = v.adhocView(adhocStruct);
        }
    },
    refreshFn: function() {
        JasperMobile.VIZ.AdhocDataView.adhocViewObject.refresh();
    },
    resizeFn: function() {
        JasperMobile.Helper.removeOnResizeListener();
        JasperMobile.VIZ.AdhocDataView.adhocViewObject.destroy();
        JasperMobile.VIZ.AdhocDataView.API.run(JasperMobile.VIZ.AdhocDataView.resourceUrl);
    }
}

JasperMobile.VIZ.AdhocDataView.API = {
    run: function(resourceUrl) {
        visualize(
            JasperMobile.VIZ.AdhocDataView.authFn,
            JasperMobile.VIZ.AdhocDataView.runFn(resourceUrl)
        );
    }
}

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