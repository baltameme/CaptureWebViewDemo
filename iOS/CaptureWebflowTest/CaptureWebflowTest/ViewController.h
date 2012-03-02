//
//  ViewController.h
//  CaptureWebflowTest
//
//  Created by lilli on 02/20/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SignInWebViewController.h"

@interface ViewController : UIViewController <SignInWebViewControllerDelegate>
@property (weak) IBOutlet UILabel  *signedInLabel;
@property (weak) IBOutlet UILabel  *accessTokenLabel;
@property (weak) IBOutlet UIButton *signInButton;
- (IBAction)signInButtonPressed:(id)sender;
@end
