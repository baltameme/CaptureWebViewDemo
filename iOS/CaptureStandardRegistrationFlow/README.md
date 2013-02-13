## Embedding Widget in a Web View

### Overview

The Capture widget normally uses popup windows to load social provider's login
screens. Popups do not work well on mobile devices for a number of reasons.
Instead, the Capture widget can be configured to redirect to the social
provider's login screen, then on to the Capture UI server, and finally back to
the original page.

1. An example bare minimum widget setup
    * skeleton HTML
        * with appropriate viewport tag
            <meta name='viewport' content='width: device-width' />
        * sane and basic set of event hooks registered
2. How to start the widget on a particular screen / flow
    * signin flow / trad reg flow
        janrain.capture.ui.start();
        janrain.capture.ui.triggerFlow('signIn');
    * the password reset flow
        This is included within the signin screen.
    * the profile edit flow
    * link account flow
        janrain.capture.ui.modal.open(screenName)
3. How to interact with a running widget
    * Todo: figure out which events fire
    * setting the access token
    janrain.capture.ui.createCaptureSession(accessToken)
    * getting the access token

### Starting the Widget

The Capture user registration widget handles user registration and related flows.
Typically each widget flow is embedded in one host webpage. To start the flow,
include the standard block of widget settings, first run:

    janrain.capture.ui.start();

... then:

    janrain.capture.ui.triggerFlow('flow-name-here');

Valid flow names are:

 * signIn
 * configured per instance of Capture via the Flows system

#### Widget Settings

The following settings

* janrain.settings.popup = false; // required
The widget spawns new windows for identity provider sign-in by default, this
flag forces the widget to operate in a single-window mode appropriate for
operation inside a UIWebView

* janrain.settings.capture.redirectFlow = true; // required
???

* janrain.settings.tokenAction = 'url'; // suggested

* janrain.settings.type = 'embed'; // suggested

* janrain.settings.redirectUri = location.href; //required

### Interacting with the Widget

#### Interacting with the UIWebView from the iOS Host App

The UIWebView message `+(NSString *)stringByEvaluatingJavaScriptFromString:(NSString *) jsString`
serves as a bridge to inject and extract information from the UIWebView.

The widget can also emit information to the host app by initiating specially
formed page loads, and responding to the UIWebView-webView:shouldStartLoadWithRequest:navigationType:
For a discussion of this technique and links to implementations see this
Stackoverflow question: http://stackoverflow.com/questions/9473582/ios-javascript-bridge

### An Example

