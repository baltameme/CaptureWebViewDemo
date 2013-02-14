function createJanrainBridge(){

    function bridgeIsEnabled(){
        return !!(typeof janrainNativeAppBridgeEnabled !== 'undefined' && janrainNativeAppBridgeEnabled);
    }

    janrain.events.onCaptureLoginSuccess.addHandler(function (result) {
        alert(bridgeIsEnabled() + " " + result);
        if (bridgeIsEnabled() && result.accessToken && !result.oneTime) {
            window.location = "janrain:accessToken=" + result.accessToken;
        }
    });

    for (var e in janrain.events) {
        if (Object.prototype.hasOwnProperty.call(janrain.events, e)){
            (function(eventName) {
                if (typeof janrain.events[eventName].addHandler === 'undefined') return;
                janrain.events[eventName].addHandler(function() {
                    var argsUrl = "janrain:" + eventName + "?arguments=" + encodeURIComponent(JSON.stringify(arguments));
                    if (bridgeIsEnabled()) {
                        window.location = argsUrl;
                    }
                });
            })(e);
        }
    }
}
