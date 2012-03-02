//
//  SignInWebViewController.m
//  CaptureWebflowTest
//
//  Created by Lilli Szafranski on 2/20/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#define DEBUG
#ifdef DEBUG
#define DLog(fmt, ...) NSLog((@"%s [Line %d] " fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__)
#else
#define DLog(...)
#endif

#define ALog(fmt, ...) NSLog((@"%s [Line %d] " fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__)

#import "SignInWebViewController.h"

@interface SignInWebViewController ()
@property (weak) id<SignInWebViewControllerDelegate> signInDelegate;
@property BOOL didSucceed;
@property (strong) NSString *accessToken;
@end

@implementation SignInWebViewController
@synthesize webview;
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

    NSURL *url =
            [NSURL URLWithString:@"https://webview-poc.dev.janraincapture.com/oauth/signin_mobile?redirect_uri=https://mobilefinish.janraincapture.com&client_id=zc7tx83fqy68mper69mxbt5dfvd7c2jh&response_type=token"];
//    [NSURL URLWithString:@"https://webview-poc.dev.janraincapture.com/oauth/signin_mobile?redirect_uri=https://redirect.com&client_id=zc7tx83fqy68mper69mxbt5dfvd7c2jh&response_type=token"];

    NSURLRequest *req = [NSURLRequest requestWithURL:url];

    [webview loadRequest:req];

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


- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType
{
    DLog(@"%@", [[request URL] absoluteString]);

    if ([[[request URL] absoluteString] hasPrefix:@"https://mobilefinish.janraincapture.com"])
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
