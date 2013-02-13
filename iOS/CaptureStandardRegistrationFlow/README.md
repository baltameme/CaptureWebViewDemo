## Embedding Widget in a Web View

### Overview

The Capture widget normally uses popup windows to load social provider's login
screens. Popups do not work well on mobile devices for a number of reasons.
Instead, the Capture widget can be configured to redirect to the social
provider's login screen, then on to the Capture UI server, and finally back to
the original page.

### Example HTML Files

* [Example Index Page](https://raw.github.com/janrain/CaptureWebViewDemo/gh-pages/index.html)

* [Example Edit Profile Page](https://raw.github.com/janrain/CaptureWebViewDemo/gh-pages/edit-profile.html)

### Starting the Widget

The Capture user registration widget handles user registration and related flows.
Typically each widget flow is embedded in one host webpage. To start the flow,
include the standard block of widget settings, first run:

    janrain.capture.ui.start();

... then:

    janrain.capture.ui.triggerFlow('flow-name-here');

Valid flow names are:

 * signIn

Flows are configured per instance of Capture via the Flow system.

#### Widget Settings

The following settings configure the Capture widget to use redirection.

* janrain.settings.popup = false; // required

The widget spawns new windows for identity provider sign-in by default, this
flag forces the widget to operate in a single-window mode appropriate for
operation inside a UIWebView.

* janrain.settings.capture.redirectFlow = true; // required

* janrain.settings.tokenAction = 'url'; // suggested

* janrain.settings.type = 'embed'; // suggested

* janrain.settings.redirectUri = location.href; // required.

* janrain.settings.capture.captureServer = 'https://yourappdomain.janraincapture.com';

* janrain.settings.tokenUrl = janrain.settings.capture.captureServer;

Specifies the page to be redirected to after signing in.

### Interacting with the Widget from the iOS Host App

The UIWebView message `+(NSString *)stringByEvaluatingJavaScriptFromString:(NSString *) jsString`
serves as a bridge to inject and extract information from the UIWebView.

The widget can also emit information to the host app by initiating specially
formed page loads, and responding to the UIWebView-webView:shouldStartLoadWithRequest:navigationType:

For a discussion of this technique and links to implementations see this
Stackoverflow question: http://stackoverflow.com/questions/9473582/ios-javascript-bridge

#### Setting the Access Token

Call janrain.capture.ui.createCaptureSession(accessToken)

#### Getting the Access Token

Register an event handler to the onCaptureLoginSuccess event.

    janrain.events.onCaptureLoginSuccess.addHandler(function (result) {
        if (result.accessToken && !result.oneTime) {
            document.location.href = "janrain:accessToken=" + result.accessToken;
        }
    });

And monitor request URLs in `UIWebView-webView:shouldStartLoadWithRequest:navigationType`:

    if ([request.URL.scheme isEqualToString:@"janrain"])
    {
        NSString *token = [[request.URL.absoluteString componentsSeparatedByString:@"="] objectAtIndex:1];
        [self sendOptionalDelegateMessage:@selector(signInDidSucceedWithAccessToken:) withArgument:token];
    }

### An Example

This Xcode project, CaptureStandardRegistrationFlow, is a working example of
running the Capture user registration widget in a UIWebView.  It loads static
pages hosted in the gh-pages branch of this Github repository.
