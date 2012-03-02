//
//  SignInWebViewController.h
//  CaptureWebflowTest
//
//  Created by Lilli Szafranski on 2/20/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol SignInWebViewControllerDelegate <NSObject>
@optional
- (void)signInDidSuccceedWithAccessToken:(NSString *)accessToken;
- (void)signInDidFailWithError:(NSString *)error;
@end

@interface SignInWebViewController : UIViewController <UIWebViewDelegate, UIAlertViewDelegate>
@property (weak) IBOutlet UIWebView *webview;
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
          andDelegate:(id<SignInWebViewControllerDelegate>)delegate;
@end
