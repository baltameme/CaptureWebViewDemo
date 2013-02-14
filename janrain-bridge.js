function createJanrainBridge(){

    function bridgeIsEnabled(){
        return !!navigator.userAgent.match(/janrainNativeAppBridgeEnabled/);
    }

    janrain.events.onCaptureLoginSuccess.addHandler(function (result) {
        alert(bridgeIsEnabled() + " " + result.toString())
        if (bridgeIsEnabled() && result.accessToken && !result.oneTime) {
            window.location = "janrain:accessToken=" + result.accessToken;
        }
    });

    for (var e in janrain.events) {
        if (Object.prototype.hasOwnProperty.call(janrain.events, e)){
            (function(eventName) {
                if (typeof janrain.events[eventName].addHandler === 'undefined') return;
                janrain.events[eventName].addHandler(function() {
                    var argsUrl;
                    try {
                        argsUrl = "janrain:" + eventName + "?arguments=" + encodeURIComponent(JSON.stringify(arguments));
                    } catch (e) {
                        var errString = "error encoding arguments" + e.toString();
                        argsUrl = "janrain:" + eventName + "?error=" + encodeURIComponent(errString);
                    }
                    if (bridgeIsEnabled()) {
                        window.location = argsUrl;
                    }
                });
            })(e);
        }
    }
}
