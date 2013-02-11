#import "CaptureWebViewController.h"
#import "debug_log.h"
#define JANRAIN_BLUE ([UIColor colorWithRed:0.102 green:0.33 blue:0.48 alpha:1.0])


@interface CaptureWebViewController ()

@property (weak) id<CaptureWebViewControllerDelegate> captureDelegate;

@end

@implementation CaptureWebViewController

/**
 * This is the fully qualified domain name of your Capture UI server.
 */
static NSString *const captureUiDomain = @"webview-poc.dev.janraincapture.com";

/**
 * This is the Capture apid client ID for the mobile app. DO NOT USE THE OWNER CLIENT ID.
 */
static NSString *const captureApidClientId = @"zc7tx83fqy68mper69mxbt5dfvd7c2jh";

@synthesize uiWebView;
@synthesize captureDelegate;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
          andDelegate:(id<CaptureWebViewControllerDelegate>)delegate
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        self.captureDelegate = delegate;
    }
    return self;
}

- (void)viewDidLoad
{
    [self.navigationController.navigationBar setTintColor:JANRAIN_BLUE];
    [self setTitle:];

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

    NSString *captureUrl =;

    [uiWebView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:captureUrl]]];
}

- (void)cancelButtonPressed:(id)sender
{
    if ([captureDelegate respondsToSelector:@selector(captureWebViewWillCancel)])
        [captureDelegate captureWebViewWillCancel];

    [[self navigationController] popViewControllerAnimated:YES];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{

}

- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request
 navigationType:(UIWebViewNavigationType)navigationType
{
    return YES;
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
}

- (void)webViewDidStartLoad:(UIWebView *)webView
{

}

- (NSUInteger)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskAllButUpsideDown;
}

@end
