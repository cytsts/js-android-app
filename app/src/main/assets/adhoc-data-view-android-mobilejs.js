// Start Point
document.addEventListener("DOMContentLoaded", function(event) {
    // intercepting network calls
    XMLHttpRequest.prototype.reallySend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function(body) {
//        JasperMobile.Callback.log("send body: " + body);
        this.reallySend(body);
    };
    Android.onEnvironmentReady();
    console.log("inside of 'DOMContentLoaded'");
}, false);

window.addEventListener("load", function(event) {
    console.log("inside of 'load'");
}, false);

window.addEventListener("message", function(event) {
    console.log("inside of 'message'");
}, false);

var JasperMobile = {
    Helper : {
        resetBodyTransformStyles: function() {
            var scale = "";
            var origin = "";
            JasperMobile.Helper.updateTransformStyles(document.body, scale, origin);
        },
        setBodyTransformStyles: function(scaleValue) {
            var scale = "scale(" + scaleValue + ")";
            var origin = "0% 0%";
            JasperMobile.Helper.updateTransformStyles(document.body, scale, origin);
        },
        setElementTransformStyles: function(element, scaleValue) {
            var scale = "scale(" + scaleValue + ")";
            var origin = "0% 0%";
            JasperMobile.Helper.updateTransformStyles(element, scale, origin);
        },
        updateTransformStyles: function(element, scale, origin) {
            element.style.transform = scale;
            element.style.transformOrigin = origin;
        }
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
    authFn: {
        loginFn: function(properties, request) {
            return (new jQuery.Deferred()).resolve();
        }
    },
    successCallback: function() {
        console.log("success of loading adhoc view");
        Android.onLoadDone();
    },
    errorCallback: function(errorObject) {
        console.log("error of loading adhoc view: " + error);
        Android.onLoadError();
    },
    runFn: function(resourceUrl) {
        var adhocStruct = {
            resource: resourceUrl,
            container: "#container",
            success: JasperMobile.VIZ.AdhocDataView.successCallback,
            error: JasperMobile.VIZ.AdhocDataView.errorCallback
        };
        return function(v) {
            Android.onLoadStart();
            adhocViewObject = v.adhocView(adhocStruct);
        }
    }
}

JasperMobile.VIZ.AdhocDataView.API = {
    run: function(resourceUrl) {
//        JasperMobile.Helper.setBodyTransformStyles(2);
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
    console.log(message);
    // run default handler
    return false;
};