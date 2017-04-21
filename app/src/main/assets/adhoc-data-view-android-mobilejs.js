Android.onScriptLoaded();
//
//
//
//Android.onLoadStart();
//Android.onLoadDone();


// Start Point
document.addEventListener("DOMContentLoaded", function(event) {
    // intercepting network calls
    XMLHttpRequest.prototype.reallySend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function(body) {
//        JasperMobile.Callback.log("send body: " + body);
        this.reallySend(body);
    };
    Android.onScriptLoaded();
    console.log("inside of 'DOMContentLoaded'");
}, false);


window.addEventListener("load", function(event) {
    // intercepting network calls
    XMLHttpRequest.prototype.reallySend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.send = function(body) {
//        JasperMobile.Callback.log("send body: " + body);
        this.reallySend(body);
    };
    Android.onScriptLoaded();
    console.log("inside of 'load'");
}, false);

window.addEventListener("message", function(event) {
    Android.onScriptLoaded();
    console.log("inside of 'message'");
}, false);

visualize({
  auth: {
    name: "superuser",
    password: "superuser"
  }
}, function(v) {
	v.adhocView({
              resource: "/public/Samples/Ad_Hoc_Views/01__Geographic_Results_by_Segment",
              container: "#container",
              success: function() {
                console.log("success of loading adhoc view");
              },
              error: function() {
                console.log("error of loading adhoc view");
              }
            });
});