/*
 * Adds handlers to 'janrain.events' that will be used to emit
 * the handler's arguments as JSON through a URL change.
 *
 * This allows the host iOS app to listen to Janrain JavaScript events.
 *
 * 'createJanrainBridge()' should be called within the global
 * 'janrainCaptureWidgetOnLoad()' function.
 */
function createJanrainBridge(){
    var bridgeIsEnabled = !!navigator.userAgent.match(/janrainNativeAppBridgeEnabled/);

    var eventNotificationFrame = document.createElement("iframe");
    createJanrainBridge.eventQueue = [];
    eventNotificationFrame.style.display = "none";
    document.body.appendChild(eventNotificationFrame);

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
                    if (bridgeIsEnabled) {
                        eventNotificationFrame.src =
                            "janrain:eventsAvailable?" + (new Date()).getTime();
                        createJanrainBridge.eventQueue.push(argsUrl);
                    }
                });
            })(e);
        }
    }
}
