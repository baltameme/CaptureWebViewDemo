//
//  AppDelegate.h
//  CaptureStandardRegistrationFlow
//
//  Created by Nathan on 02/11/13.
//  Copyright (c) 2013 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CaptureWebViewController.h"

@class DemoNavController;
@class CaptureWebViewController;

@interface AppDelegate : UIResponder <UIApplicationDelegate, CaptureWebViewControllerDelegate>

@property (strong, nonatomic) UIWindow *window;
@property (strong, nonatomic) CaptureWebViewController *captureController;

+ (AppDelegate *)sharedDelegate;
@end