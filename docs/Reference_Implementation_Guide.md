# Embedding JUMP in a Web View

This guide discusses the technique of using JUMP in a native application, via a WebView (Android) or a UIWebView (iOS).
The guide also discusses the reference implementations provided by Janrain.

## Introduction

The Janrain User Registration Widget is primarily designed for use in browsers, but it is also compatible with
operation while embedded in native mobile apps. Some settings specifically required for this technique, and a method
to interface the widget with the native application are discussed here.

Once embedded in a native application, the widget can be used to provide the user registration and management portion
of the application.

To embed the the widget in your native app, you will need to create a web page hosting the widget and use a
WebView or UIWebView to display that page. (**Warning**: `file://` scheme URLs for widget-hosting pages will not work,
so you cannot use a page compiled into the app, it must be hosted on the internet.)

Once the widget is hosted in a page, it will need to be configured for compatibility with the WebView. This
configuration includes disabling the pop-up window experience, and optionally configuring the widget to use a small-
screen optimized registration-flow.

## Principal of Operation

At its most fundamental, embedding the User registration widget in a WebView works because information can be exchanged
between the WebView and the native Android host app. This information exchange can be accomplished in different ways:

1. (**Android** only) Using the WebView's Java to JavaScript binding API

   http://developer.android.com/guide/webapps/webview.html#BindingJavaScript

   This technique allows you to register a Java object of your definition with the WebView, and Javascript running in
   the WebView will be able to call methods available on the Java object.

2. Injecting information by running JavaScript in the WebView

   **Android**: loading `javascript:` scheme URLs in the WebView will cause the WebView to run JavaScript contained in
   the body of the URL.

   If you call WebView#loadUrl with a URL with the `javascript` scheme the WebView will evaluate the path of the URL
   as JavaScript. (So, e.g., `javascript:alert("These Snozzberries taste like Snozzberries!");` will display a
   JavaScript alert dialog.) This allows you to inject information into the WebView from the host app, but doesn't
   enable the extraction of information from the WebView into the host app.

   **iOS**: using `-[UIWebView stringByEvaluatingJavaScriptFromString:]` will cause the UIWebView to run the JavaScript
   passed to the method.

3. Extracting information by overloading the URL loading delegate method:

   **Android**: `WebViewClient#shouldOverrideUrlLoading`

   **iOS**: `-[UIWebViewDelegate webView:shouldStartLoadWithRequest:navigationType:]`

   By overriding these delegate methods (which have very similar usage on iOS and Android) the host application can
   receive information from the WebView whenever a URL is loaded. E.g. if JavaScript is written to load
   `http://a?b=c` the overridden delegate methods can observe that URL being loaded, and extract the value of the
   parameter "b" (and furthermore, the delegate methods can *prevent the WebView from doing further processing the URL,
   preserving its otherwise normal operation*.)

For further discussion, and links to open-source implementations, see this Stackoverflow question:

    http://stackoverflow.com/questions/9473582/ios-javascript-bridge

## Implementation Overview

1. Create a WebView (UIWebView on iOS)
2. Create a page, hosted on a web server (not the filesystem), to be loaded in in the WebView
3. Embed the User Registration widget in the page, configure it as necessary
4. Load the page's URL in the WebView
5. Enable a bridge to transfer information between the host application and the JavaScript runtime in the WebView.
   (See `janrain-bridge.js` for implementation details.)

### Set the Required Widget Settings

The following settings configure the Capture widget for use in a WebView or UIWebView:

#### Disable Popups

    janrain.settings.popup = false; // required

The widget spawns new windows for identity provider sign-in by default, this
flag forces the widget to operate in a single-window mode appropriate for
operation inside a UIWebView.

#### Enable the Redirect Flow

    janrain.settings.capture.redirectFlow = true;

Required to configured the Capture widget to operate in single-window mode.

#### Set the Appropriate Token Action

    janrain.settings.tokenAction = 'url';

Configures the Engage widget to redirect after authentication.

#### Set the Appropriate Display Type

    janrain.settings.type = 'embed';

Configures the Janrain widgets to render embedded in the host page's DOM,
as opposed to rendering in a modal popup dialog.

#### Set an Appropriate Redirect URI

    janrain.settings.redirectUri = location.href; // required.

Configures Capture to redirect back to the host page after signing a user in.

#### Configure the Capture Base URL

    janrain.settings.capture.captureServer = 'https://yourappdomain.janraincapture.com';

Configures the base URL of your Capture server instance

#### Set an Appropriate Engage Token URL

    janrain.settings.tokenUrl = janrain.settings.capture.captureServer;

Unused in the redirect flow but must be set to an Engage whitelisted
domain for proper operation.

### Start the Widget

The [Janrain User Registration Widget](http://developers.janrain.com/documentation/widgets/user-registration-widget/)
handles user registration and related user experiences. Typically, experience is embedded in a separate webpage. To
start the user registration widget include the standard block of widget settings (described above) and run:

    janrain.capture.ui.start();

### Interface with the Host App from JavaScript

The reference implementation uses a JavaScript array to store a queue of Janrain JavaScript events and event arguments.
As Janrain's JavaScript events occur, they are enqueued onto the array, and the native app is signaled of the change
by loading a special URL.

See the reference implementation's JavaScript <-> native bridge in `janrain-bridge.js` (on the gh-pages branch of this
git repository)

### Receive Janrain JavaScript Events From the Widget from an iOS Host App

See `-[CaptureWebViewController webView:shouldStartLoadWithRequest:navigationType:]` in `CaptureWebViewController.m`.

Whenever the special signalling URL is loaded from by `janrain-bridge.js` the UIWebView's delegate (an instance of
`CaptureWebViewController` is notified of the URL load. The special signalling URL is detected, and then
`dispatchEventQueue` is run to pump all enqueued events out of the JavaScript array into the host app.

`dispatchEventQueue` pumps events from JavaScript to Objective C by calling
`-[UIWebView stringByEvaluatingJavaScriptFromString:]`. The JavaScript evaluated dequeues events, JSON encodes them,
and returns them to the Objective C code in a string. The Objective C code decodes the JSON-encoded string, and
dispatches the events to native event handlers. (Which are registered with
`-[CaptureWebViewController addEventHandler:eventName:]`).

For example, the reference implementation adds a handler for the onCaptureLoginSuccess event:

    [self.captureController addEventHandler:^(NSArray *eventArgs)
    {
        NSDictionary *result = [eventArgs objectAtIndex:0]; // the sign-in result is the first argument of this event
        NSString *token = [result objectForKey:@"accessToken"];
        [self signInDidSucceedWithAccessToken:token];
    }                             eventName:@"onCaptureLoginSuccess"];

### Receive Janrain JavaScript Events From the Widget from an Android Host App

Whenever the special signalling URL is laoded by `janrain-bridge.js` the WebView's WebViewClient's
`shouldOverrideUrlLoading` method is called. The WebViewClient subclass condfigured for the WebView in
`WebViewActivity` shows detection of the signalling URL.

Once the signalling URL is detected, event-pumping JavaScript is executed in the WebView by using `WebView#loadUrl`.
The event pumping JavaScript then uses the Android JavaScript to Java interface to send each event back to the host
application. The registered JavaScript to Java interface (written in Java in the host application), receives each
event and dispatches it.

The Java to JavaScript interface registed in the reference implementation, `WebViewActivity.WebAppInterface`, decodes
each event and if it is the `onCaptureLoginSuccess` event then it dispatches the event to a native event handler,
`WebAppInterface#processOnCaptureLoginSuccess`, which in turn closes the `WebViewActivity` and displays a dialog with
the Capture access token.

### Example User Registraton Web Pages

Example pages hosting the user registration widget, and running the `janrain-bridge.js` can be found in the `gh-pages`
branch of this git repository:

#### Example Registration Page

[Example User Registration Page](https://raw.github.com/janrain/CaptureWebViewDemo/gh-pages/index.html)

This page is an example which allows the user to sign-in or to register.

#### Example Profile Editing Page

[Example Edit Profile Page](https://raw.github.com/janrain/CaptureWebViewDemo/gh-pages/edit-profile.html)

This page is an example profile editing page which is configured to have the widget load the profile editing screen
directly. If the WebView does not have a valid Capture access token stored in local state then the user is prompted to
sign-in instead. (The widget stores access tokens in cookies or in local storage).

## Advanced Operation

### Setting the Access Token

After the widget is started you can run the following JavaScript ...:

    janrain.capture.ui.createCaptureSession("access_token_here");

... which will seed the access token into the widget's internal state.

### Receiving the Access Token

To receive the Capture access token (which can be used with the Capture API directly,) register a native event handler
for the onCaptureLoginSuccess event:

#### iOS

    [self addEventHandler:[^(NSArray *eventArgs)
    {
        NSDictionary *token = [eventArgs objectAtIndex:0]; // the token is the first argument of this event
        [self sendOptionalDelegateMessage:@selector(signInDidSucceedWithAccessToken:) withArgument:token];
    } copy] eventName:@"onCaptureLoginSuccess"];

#### Android

The Android reference implementation is hard-coded to handle only the `onCaptureLoginSuccess` event, which is handled
by `WebAppClient#processOnCaptureLoginSuccess`.

### An iOS and Android Reference Implementation

See the accompanying Xcode project, CaptureStandardRegistrationFlow, which is a working example of running the user
registration widget in a UIWebView.  It loads static pages hosted in the gh-pages branch of this Github repository.
Each page embeds the widget and invokes a specific flow when loaded.

See also the accompanying Android project, also called CaptureStandardRegistration flow.