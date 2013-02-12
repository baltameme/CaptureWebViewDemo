#import "CaptureWebViewController.h"
#import "debug_log.h"

/**
* This is the fully qualified domain name of your Capture UI server.
*/
static NSString *const captureUiDomain = @"webview-poc.dev.janraincapture.com";

/**
* This is the Capture apid client ID for the mobile app. DO NOT USE THE OWNER CLIENT ID.
*/
static NSString *const captureApidClientId = @"zc7tx83fqy68mper69mxbt5dfvd7c2jh";

@interface CaptureWebViewController ()

@property(strong) UIWebView *webView;
@property(weak) id<CaptureWebViewControllerDelegate> captureDelegate;
@property(nonatomic, strong) NSString *activeFlow;

@end

@implementation CaptureWebViewController

static NSDictionary *JR_CAPTURE_WEBVIEW_FLOWS;

+(void)initialize
{
    JR_CAPTURE_WEBVIEW_FLOWS = @{
    @"signin":@{
    @"title":@"Sign In",
    @"url":@"https://mulciber.janrain.com/CaptureWidget/mobile/index.php"
},
    @"profile":@{
    @"title":@"Update Profile",
    @"url":@""
}
};
}

@synthesize webView;
@synthesize captureDelegate;
@synthesize activeFlow;

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

- (void)loadView
{
    self.view = self.webView = [[UIWebView alloc] initWithFrame:CGRectMake(0, 0, 1, 1)];
    webView.delegate = self;
}

- (void)viewDidLoad
{
    [self setTitle:[[JR_CAPTURE_WEBVIEW_FLOWS objectForKey:activeFlow] objectForKey:@"title"]];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];

    NSString *captureUrl = [[JR_CAPTURE_WEBVIEW_FLOWS objectForKey:activeFlow] objectForKey:@"url"];

    [webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:captureUrl]]];
}

- (void)pushFlow:(NSString *) flowName ontoNavigationController:(UINavigationController *) nc
{
    self.activeFlow = flowName;
    [nc pushViewController:self animated:YES];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    DLog(@"webView error: %@", error);
}

- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request
navigationType:(UIWebViewNavigationType)navigationType
{
    DLog(@"webView shouldStartLoadWithRequest %@", request);
    return YES;
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
}

- (void)webViewDidStartLoad:(UIWebView *)webView
{
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
}

- (NSUInteger)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskAllButUpsideDown;
}

@end
