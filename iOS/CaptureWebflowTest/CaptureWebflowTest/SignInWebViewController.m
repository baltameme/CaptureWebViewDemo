//
//  SignInWebViewController.m
//  CaptureWebflowTest
//
//  Created by Lilli Szafranski on 2/20/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "debug_log.h"
#import "SignInWebViewController.h"

/*
 * To run this demo on your own Capture app edit the captureUiDomain and captureApidClient constants to correspond to
 * your own Capture instance and contact your deployment engineer to set up the mobile optimized screens.
 */

/**
 * This is the fully qualified domain name of your Capture UI server.
 */
static NSString *const captureUiDomain = @"webview-poc.dev.janraincapture.com";

/**
 * This is the Capture apid client ID.
 */
static NSString *const captureApidClientId = @"zc7tx83fqy68mper69mxbt5dfvd7c2jh";

/**
 * This is an arbitrary sentinel URL that will be used as the redirect parameter and be monitored for redirects to by
 * the UIWebView delegate.
*/
static NSString *const sentinelUrl = @"https://mobilefinish.janraincapture.com";

/**
 * This is the path the to the sign in web page optimized for mobile.
 */
static NSString *const signinUrlPath = @"/oauth/signin_mobile";

@interface SignInWebViewController ()
@property (weak) id<SignInWebViewControllerDelegate> signInDelegate;
@property BOOL didSucceed;
@property (strong) NSString *accessToken;
@end

@implementation SignInWebViewController
@synthesize uiWebView;
@synthesize signInDelegate;
@synthesize didSucceed;
@synthesize accessToken;

#define JANRAIN_BLUE [UIColor colorWithRed:0.102 green:0.33 blue:0.48 alpha:1.0]

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
          andDelegate:(id<SignInWebViewControllerDelegate>)delegate
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        self.signInDelegate = delegate;
    }
    return self;
}

- (void)viewDidLoad
{
    [self.navigationController.navigationBar setTintColor:JANRAIN_BLUE];
    [self setTitle:@"Please Sign In"];

    UIBarButtonItem *cancelButton =
                        [[UIBarButtonItem alloc]
                                initWithBarButtonSystemItem:UIBarButtonSystemItemCancel
                                                     target:self
                                                     action:@selector(cancelButtonPressed:)];


    self.navigationItem.rightBarButtonItem         = cancelButton;
    self.navigationItem.rightBarButtonItem.enabled = YES;

    self.navigationItem.rightBarButtonItem.style   = UIBarButtonItemStyleBordered;

}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];

    [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"accessToken"];

    // This is the absolute URL for the sign in web page optimized for mobile.
    NSString *captureSignInUrl =
            [[NSArray arrayWithObjects:@"https://", captureUiDomain,
                                       signinUrlPath,
                                       @"?redirect_uri=", sentinelUrl,
                                       @"&client_id=", captureApidClientId,
                                       @"&response_type=token", nil] componentsJoinedByString:@""];

    [uiWebView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:captureSignInUrl]]];
}

- (void)cancelButtonPressed:(id)sender
{
    if ([signInDelegate respondsToSelector:@selector(signInDidFailWithError:)])
        [signInDelegate signInDidFailWithError:nil];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{

}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (didSucceed)
    {
        if ([signInDelegate respondsToSelector:@selector(signInDidSuccceedWithAccessToken:)])
            [signInDelegate signInDidSuccceedWithAccessToken:accessToken];
    }
    else
    {
        if ([signInDelegate respondsToSelector:@selector(signInDidFailWithError:)])
            [signInDelegate signInDidFailWithError:nil];
    }
}


- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request
 navigationType:(UIWebViewNavigationType)navigationType
{
    DLog(@"%@", [[request URL] absoluteString]);

    if ([[[request URL] absoluteString] hasPrefix:sentinelUrl])
    {
        NSString *urlString = [[request URL] absoluteString];

        NSArray *parametersArray =
                        [urlString componentsSeparatedByCharactersInSet:
                                [NSCharacterSet characterSetWithCharactersInString:@"#?&="]];

        for (NSUInteger i = 0; i < [parametersArray count] - 1; i++)
        {
            if ([[parametersArray objectAtIndex:i] isEqual:@"access_token"])
            {
                [self setAccessToken:[parametersArray objectAtIndex:i + 1]];
                [[NSUserDefaults standardUserDefaults] setObject:accessToken forKey:@"accessToken"];

                break;
            }
        }
        if (accessToken)
        {
            didSucceed = YES;

            UIAlertView *alert = [[UIAlertView alloc]
                    initWithTitle:@"Sentinel URL Reached"
                          message:[NSString stringWithFormat:@"Access token parsed: %@", accessToken]
                         delegate:self
                cancelButtonTitle:@"OK"
                otherButtonTitles:nil];

            [alert show];
        }
        else
        {
            didSucceed = NO;

            UIAlertView *alert = [[UIAlertView alloc]
                                initWithTitle:@"Sentinel URL Reached"
                                      message:@"Access token could not be parsed"
                                     delegate:self
                            cancelButtonTitle:@"OK"
                            otherButtonTitles:nil];

            [alert show];
        }

        return NO;
    }
    return YES;
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
}

- (void)webViewDidStartLoad:(UIWebView *)webView
{

}

#pragma mark - View lifecycle

- (void)viewDidUnload
{
    [super viewDidUnload];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
@end
