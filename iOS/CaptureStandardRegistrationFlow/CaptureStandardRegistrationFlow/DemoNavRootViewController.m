//
// Created by nathan on 2/11/13.
//
// To change the template use AppCode | Preferences | File Templates.
//


#import "DemoNavRootViewController.h"
#import "AppDelegate.h"
#import "CaptureWebViewController.h"

static void makeAutoresizing(UIView *v)
{
    v.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
}

@implementation DemoNavRootViewController
@synthesize demoNavController;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
   demoViewController:(DemoNavController *)demoViewController_
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        self.demoNavController = demoViewController_;
    }

    return self;
}

- (void)loadView
{
    self.view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 1, 1)];
}

- (void)viewDidLoad
{
    [self addButtonForTitle:@"Sign In" action:@selector(signInButtonPressed)];
    [self addButtonForTitle:@"Edit Profile" action:@selector(editProfileButtonPressed)];
}

- (void)signInButtonPressed
{
    [[AppDelegate sharedDelegate].captureController pushFlow:@"signin"
                                    ontoNavigationController:self.navigationController];
}

- (void)editProfileButtonPressed
{
    [[AppDelegate sharedDelegate].captureController pushFlow:@"profile"
                                    ontoNavigationController:self.navigationController];
}

- (void)addButtonForTitle:(NSString *)title action:(SEL)action
{
    UIButton *b = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    NSUInteger c = [self.view.subviews count];
    b.frame = CGRectMake(10, c*50 + 10, 300, 40); // yech
    [b setTitle:title forState:UIControlStateNormal];
    [b addTarget:self.demoNavController action:action forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:b];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}


- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self setTitle:@"Capture Demo"];
}

@end
