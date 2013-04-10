# Mobile App Password Reset Flow

This document describes how to reset passwords for native mobile
applications embedding the "iframe" version of Capture.

## Implementation Guide

#### Step 1 - The Sign In Page is Loaded in the App

Your native mobile application loads the Capture sign-in page in a 
`UIWebView`. The URL of this page is:  

    https://your_capture_domain/oauth/signin?
    response_type=token
    &redirect_uri=https://yourdomain.com/password_reset.php
    &client_id=your_mobile_apps_client_id

**Warning**: The `redirect_uri` parameter is normally populated with a
sentinel value when running Capture in an embedded browser. In this 
case the `redirect_uri` must serve *both* as a sentinel value and as a 
pointer to the app-specific password reset page. Adjust the sentinel 
detection logic in your `UIWebViewDelegate` to adjust.

#### Step 2 - The User Initiates Password Reset from the Sign-In Page:

The user initiates the password reset flow from inside your mobile app. 
They do this by tapping the password reset link in the sign-in page.

Capture then sends a user a password reset email, containing a password
reset link. The link is exactly the `redirect_uri` parameter used to
load the sign-in page, with an oauth token appended.

#### Step 3 - The User Opens the Password-Reset Email

The user opens the password-reset email sent by Capture. The email 
contains the password-reset link.

#### Step 4 - User Opens Password Reset Link

The user opens the password-reset link in the email. The link opens in 
their mobile browser and points to your `password_reset.php` page.

#### Step 5 - Password Reset Page Loads

Your `password_reset.php` page loads:

 1. It reads the `token` parameter from the GET request
 2. Your password reset page renders, using the `token` to generate
    an `iframe` tag loading the Capture change-password iframe.

The src attribute for the Capture iframe tag rendered in step 4 is:

    https://your_capture_domain/oauth/profile_change_password?
    token=the_token_from_step_1
    &callback=nameOfYourPasswordChangeCallback
    &xd_receiver=url_to_xdcomm.html

The JavaScript callback (called `nameOfYourPasswordChangeCallback` 
above,) will be invoked after the user has changed their password.
`password_reset.php` includes the source code for this callback. The
callback is used to open your app's [custom URL scheme](http://developer.apple.com/library/ios/#documentation/iPhone/Conceptual/iPhoneOSProgrammingGuide/AdvancedAppTricks/AdvancedAppTricks.html).

**Warning**: Your page must reference a local copy of xdcomm.html for the
callback to be invoked after password reset.

#### Step 6 - The User Changes Their Password

The user enters a new password and submits the password reset form, which
finishes the password change in the Capture database.

#### Step 7 - The JavaScript Callback is Invoked

Once the password change has finished, your JavaScript callback 
(referenced by the `callback` parameter used to load the password reset
iframe) is invoked. Your callback (defined in `password_reset.php`)
opens your iOS app.

