## Embedding Widget in a Web View

### Overview

The Capture widget normally uses popup windows to load social provider's login
screens. Popups do not work well on mobile devices for a number of reasons.
Instead, the Capture widget can be configured to redirect to the social
provider's login screen, then on to the Capture UI server, and finally back to
the original page.

### Example HTML Files

* [Example Index Page](https://raw.github.com/janrain/CaptureWebViewDemo/gh-pages/index.html)
* [Example Page With All Screens Using One FLow](https://raw.github.com/janrain/CaptureWebViewDemo/gh-pages/index-oneflow.html)
* [Example Edit Profile Page](https://raw.github.com/janrain/CaptureWebViewDemo/gh-pages/edit-profile.html)

### Starting the Widget

The Capture user registration widget handles user registration and related flows.
Typically each widget flow is embedded in one host webpage. To start the flow,
include the standard block of widget settings, and then run:

    janrain.capture.ui.start();

Flows are configured separately for each instance of Capture, and can be customized.
This example uses ~iPhone optimized flows named webViewProfile and webViewSignIn.
If you so choose, you can use the webViewDemo flow name with the markup in index-oneflow.html
to have all screens on one page using one flow.

#### Widget Settings

The following settings configure the Capture widget to use redirection:

* janrain.settings.popup = false; // required

The widget spawns new windows for identity provider sign-in by default, this
flag forces the widget to operate in a single-window mode appropriate for
operation inside a UIWebView.

* janrain.settings.capture.redirectFlow = true;

Required to configured the Capture widget to operate in single-window mode.

* janrain.settings.tokenAction = 'url';

Configures the Engage widget to redirect after authentication.

* janrain.settings.type = 'embed';

Configures the Janrain widgets to render embedded in the host page's DOM,
as opposed to rendering in a modal popup dialog.

* janrain.settings.redirectUri = location.href; // required.

Configures Capture to redirect back to the host page after signing a user in.

* janrain.settings.capture.captureServer = 'https://yourappdomain.janraincapture.com';

Configures the base URL of your Capture server instance

* janrain.settings.tokenUrl = janrain.settings.capture.captureServer;

Unused in the redirect flow but must be set to an Engage whitelisted
domain for proper operation.

Specifies the page to be redirected to after signing in.

### Interacting with the Widget from the iOS Host App

The UIWebView message `+(NSString *)stringByEvaluatingJavaScriptFromString:(NSString *) jsString`
serves as a bridge to inject and extract information from the UIWebView.

The widget can also emit information to the host app by initiating specially
formed page loads, and responding to the UIWebViewDelegate-webView:shouldStartLoadWithRequest:navigationType: message.

For a discussion of this technique and links to open source implementations
see this Stackoverflow question: http://stackoverflow.com/questions/9473582/ios-javascript-bridge

Also see the reference implementation's JavaScript <-> iOS bridge in janrain-bridge.js

#### Setting the Access Token

After the widget is started you can call:

    janrain.capture.ui.createCaptureSession("accessToken");

#### Getting the Access Token

To retrieve the Capture access token (which can be used with the
Capture API directly,) register an event handler to the onCaptureLoginSuccess 
event and pass the token with the iOS <-> JS bridge technique:

    janrain.events.onCaptureLoginSuccess.addHandler(function (result) {
        if (result.accessToken && !result.oneTime) {
            document.location.href = "janrain:accessToken=" + result.accessToken;
        }
    });

... and monitor request URLs in `UIWebView-webView:shouldStartLoadWithRequest:navigationType`:

    if ([request.URL.scheme isEqualToString:@"janrain"])
    {
        NSString *token = [[request.URL.absoluteString componentsSeparatedByString:@"="] objectAtIndex:1];
        [self sendOptionalDelegateMessage:@selector(signInDidSucceedWithAccessToken:) withArgument:token];
    }

#### Handling JavaScript Events in the Host App

`janrain-bridge.js` in the reference implementation bridges all Janrain
JavaScript events to the iOS host app. Add event handlers to the reference
implementations jsEventHandlers object:

    /**
     * NSString -> NSArray map with (void)^(NSDictionary *eventArgs) blocks/handlers
     * It is okay to add handlers to this structure from outside of this class.
     */
    @property(nonatomic, strong) id jsEventHandlers;

For example, the reference implementation adds a handler to onCaptureLoginSuccess:

    [self.jsEventHandlers setObject:[NSMutableArray array]forKey:@"onCaptureLoginSuccess"];
    [[self.jsEventHandlers objectForKey:@"onCaptureLoginSuccess"] addObject:[^(id eventArgs){
        NSDictionary *result = [eventArgs objectAtIndex:0];
        [self sendOptionalDelegateMessage:@selector(signInDidSucceedWithAccessToken:) withArgument:result];
    } copy]];
    
### An Example

This Xcode project, CaptureStandardRegistrationFlow, is a working example of
running the Capture user registration widget in a UIWebView.  It loads static
pages hosted in the gh-pages branch of this Github repository.
Each page embeds the widget and invokes a specific flow when loaded.
