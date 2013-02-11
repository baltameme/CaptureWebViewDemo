//
//  DemoViewController.m
//  CaptureStandardRegistrationFlow
//
//  Created by Nathan on 02/11/13.
//  Copyright (c) 2013 __MyCompanyName__. All rights reserved.
//

#import "DemoViewController.h"

static void makeAutoresizing(UIView *v)
{
    v.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
}

@interface RootViewController : UIViewController
@property(nonatomic, strong) DemoViewController *demoViewController;
@end

@implementation RootViewController
@synthesize demoViewController;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
   demoViewController:(DemoViewController *)demoViewController_
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        self.demoViewController = demoViewController_;
    }

    return self;
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    self.navigationController.title = @"Capture";
}

@end

@interface DemoViewController ()
@property(nonatomic, strong) UINavigationController *navCon;
@property(nonatomic, strong) RootViewController *rootViewController;
@end

@implementation DemoViewController
@synthesize rootViewController;
@synthesize navCon;

- (void)signInButtonPressed
{
     
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end