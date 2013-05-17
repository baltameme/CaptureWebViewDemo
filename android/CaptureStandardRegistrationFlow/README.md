## Embedding Widget in a Web View

### Overview

The [Janrain User Registration Widget](http://developers.janrain.com/documentation/widgets/user-registration-widget/) 
normally uses popup windows to load social provider's login screens 
when used in a browser. However, popups do not work well on mobile devices. Instead, the Widget is 
configured to redirect to the social provider's login screen, then on to the Capture UI server, and 
finally back to the original page. 

For detailed information on integrating with [Android](http://www.android.com/), please see the 
[JUMP for Android](http://developers.janrain.com/documentation/mobile-libraries/jump-for-android/) 
documentation. 

### How it Works

Your Android app embeds a 
[WebView](http://developer.android.com/reference/android/webkit/WebView.html) in a native layout 
and [binds JavaScript](http://developer.android.com/guide/webapps/webview.html#UsingJavaScript) 
code to your native Android code. The WebView will contain the 
[Janrain User Registration Widget](http://developers.janrain.com/documentation/widgets/user-registration-widget/), 
thereby leveraging the abilities of the widget to handle your user registration. It is important to remember that your 
[AndroidManifest.xml](http://developer.android.com/guide/topics/manifest/manifest-intro.html) must specify the 
[INTERNET permission](http://developer.android.com/reference/android/Manifest.permission.html#INTERNET) 
for the WebView to function. You may find it easier to inject JavaScript directly from your 
native code by using the WebView's 
[addJavascriptInterface](http://developer.android.com/reference/android/webkit/WebView.html#addJavascriptInterface%28java.lang.Object, java.lang.String%29) 
method. 

### Example HTML Files

* [Example Index Page](https://raw.github.com/janrain/CaptureWebViewDemo/gh-pages/index.html)
* [Example Page With All Screens Using One FLow](https://raw.github.com/janrain/CaptureWebViewDemo/gh-pages/index-oneflow.html)
* [Example Edit Profile Page](https://raw.github.com/janrain/CaptureWebViewDemo/gh-pages/edit-profile.html)

### Starting the Widget

The [Janrain User Registration Widget](http://developers.janrain.com/documentation/widgets/user-registration-widget/) 
handles user registration and related flows. Typically each widget flow is embedded in one host 
webpage. To start the flow, include the standard block of widget settings and then run:

    janrain.capture.ui.start();

Flows are configured separately for each instance of Capture, and can be customized.
This example uses mobile optimized flows named webViewProfile and webViewSignIn.
If you so choose, you can use the webViewDemo flow name with the markup in index-oneflow.html
to have all screens on one page using one flow.

#### Widget Settings

The following settings configure the Capture widget to use redirection:

* janrain.settings.popup = false; // required

The widget spawns new windows for identity provider sign-in by default, this 
flag forces the widget to operate in a single-window mode appropriate for 
operation inside a WebView.

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

### Interacting with the Widget from the Android Host App

The WebView [setWebViewClient](http://developer.android.com/reference/android/webkit/WebView.html#setWebViewClient%28android.webkit.WebViewClient%29) 
method is used to take over control when a new url is about to be loaded. Using the 
[shouldOverrideUrlLoading](http://developer.android.com/reference/android/webkit/WebViewClient.html#shouldOverrideUrlLoading%28android.webkit.WebView, java.lang.String%29) 
method, inspect and detect the for the janrain [scheme](http://en.wikipedia.org/wiki/URI_scheme). 
Whenever the janrain scheme is detected, the format for the URL should be as follows:

janrain:eventName?arguments=URL%20ENCODED%20JSON%20ARRAY%20HERE 

When the eventName is [onCaptureLoginSuccess](http://developers.janrain.com/documentation/widgets/user-registration-widget/capture-widget-api/events/), 
the user has successfully logged in and you can dismiss the 
[WebView](http://developer.android.com/reference/android/webkit/WebView.html). The 
argument will contain an [encoded](http://en.wikipedia.org/wiki/Percent-encoding) 
[JSON](http://www.json.org/) array. 

For a discussion of this technique see [http://developer.android.com/guide/webapps/webview.html#UsingJavaScript](http://developer.android.com/guide/webapps/webview.html#UsingJavaScript)

#### Setting the Access Token

After the widget is started you can call:

    janrain.capture.ui.createCaptureSession("accessToken");

TBD

#### Getting the Access Token

The access token is available as a string value inside the JSON array. Here is an example JSON 
array:

[
{
"screen": "socialMobileRegistration",
"transactionId": "5ndexq577nsspzee6o7fmma85t8morkgpl6uxlyv",
"oneTime": false,
<b>"accessToken": "s27epv36fu2vzzj5"</b>,
"status": "success",
"keepMeLoggedIn": false,
"flow": "webViewSignIn",
"userData":
{ "displayName": "Alfred Neuman", "email": "aleneuman@janrain.com", "uuid": "37bfe4a7-8c76-4aad-a04e-b2e7568dc66b" }
,
"renders": false,
"version": "8oIhjzZcloz6EsAHPcUxwQ",
"statusMessage": "signedIn",
"action": "socialSignin",
"ssoImplicitLogin": false,
"authProfileData": {},
"authProvider": "google"
}
]

#### Handling JavaScript Events in the Host App

The Javascript code you write will 
[bind](http://developer.android.com/guide/webapps/webview.html#UsingJavaScript) to your native 
Android code, invoke a callback method and send it the event queue as a parameter. The event queue 
will contain a JSON array that requires further filtering. You will to parse the JSON Array looking 
for a URL element with the janrain scheme. Once found, you will inspect the URL for the event name of 
"onCaptureLoginSuccess" and decode the encoded JSON array passed as "arguments". The format of the
URL will be as follows:

janrain:onCaptureLoginSuccess?arguments=URL%20ENCODED%20JSON%20ARRAY%20HERE

### An Example

This Eclipse project, CaptureStandardRegistrationFlow, is a working example of
running the Capture user registration widget in a WebView. It loads static
pages hosted in the gh-pages branch of this Github repository.
Each page embeds the widget and invokes a specific flow when loaded.
