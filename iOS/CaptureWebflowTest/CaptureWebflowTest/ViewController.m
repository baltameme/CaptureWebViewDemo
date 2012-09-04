//
//  ViewController.m
//  CaptureWebflowTest
//
//  Created by lilli on 02/20/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "ViewController.h"

@implementation ViewController

@synthesize signInButton;
@synthesize accessTokenLabel;
@synthesize signedInLabel;

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - View lifecycle

- (void)showSignedIn
{
    signedInLabel.text      = @"User is signed in";
    accessTokenLabel.text   =
            [NSString stringWithFormat:@"Access token: %@",
                      [[NSUserDefaults standardUserDefaults] objectForKey:@"accessToken"]];
    accessTokenLabel.hidden = NO;

    [signInButton setTitle:@"Sign in again"
                  forState:UIControlStateNormal];
}

- (void)showSignedOut
{
    signedInLabel.text      = @"Not signed in";
    accessTokenLabel.text   =
            [NSString stringWithFormat:@"Access token: %@",
                      [[NSUserDefaults standardUserDefaults] objectForKey:@"accessToken"]];
    accessTokenLabel.hidden = YES;

    [signInButton setTitle:@"Sign in"
                  forState:UIControlStateNormal];
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    NSString *accessToken = [[NSUserDefaults standardUserDefaults] objectForKey:@"accessToken"];

    if (accessToken)
        [self showSignedIn];
    else
        [self showSignedOut];
}

- (IBAction)signInButtonPressed:(id)sender
{
    SignInWebViewController *webViewController   =
                                    [[SignInWebViewController alloc] initWithNibName:@"SignInWebViewController"
                                                                              bundle:[NSBundle mainBundle]
                                                                         andDelegate:self];
    UINavigationController *navigationController =
                                    [[UINavigationController alloc] initWithRootViewController:webViewController];

    [self presentModalViewController:navigationController animated:YES];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)signInDidSuccceedWithAccessToken:(NSString *)accessToken
{
    [self dismissModalViewControllerAnimated:YES];
    [self showSignedIn];
}

- (void)signInDidFailWithError:(NSString *)error
{
    [self dismissModalViewControllerAnimated:YES];
    [self showSignedOut];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
}

@end
