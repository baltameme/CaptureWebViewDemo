#import "CaptureWebViewController.h"
#import "debug_log.h"

@interface CaptureWebViewController ()

@property(strong) UIWebView *webView;
@property(weak) id<CaptureWebViewControllerDelegate> captureDelegate;
@property(nonatomic, strong) NSString *activePageName;

@end

@implementation CaptureWebViewController

static NSDictionary *JR_CAPTURE_WEBVIEW_PAGES;

+(void)initialize
{
    JR_CAPTURE_WEBVIEW_PAGES = @{
            @"signin" : @{
                    @"title" : @"Sign In",
                    @"url" : @"http://mulciber.janrain.com/CaptureWidget/mobile/index.php"
            },
            @"profile" : @{
                    @"title" : @"Update Profile",
                    @"url" : @"http://mulciber.janrain.com/CaptureWidget/mobile/edit-profile.php"
            }
    };
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

@synthesize webView;
@synthesize captureDelegate;
@synthesize activePageName;

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
    [self setTitle:[[JR_CAPTURE_WEBVIEW_PAGES objectForKey:activePageName] objectForKey:@"title"]];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [webView loadData:[@"" dataUsingEncoding:NSUTF8StringEncoding] MIMEType:@"text/html"
     textEncodingName:@"utf8" baseURL:[NSURL URLWithString:@"about:blank"]];

    [super viewDidDisappear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];

    NSString *captureUrl = [[JR_CAPTURE_WEBVIEW_PAGES objectForKey:activePageName] objectForKey:@"url"];

    [webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:captureUrl]]];
}

- (void)pushFlow:(NSString *) flowName ontoNavigationController:(UINavigationController *) nc
{
    self.activePageName = flowName;
    [nc pushViewController:self animated:YES];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)webView:(UIWebView *)webView_ didFailLoadWithError:(NSError *)error
{
    DLog(@"webView load error: %@", error);
}

- (BOOL)webView:(UIWebView *)webView_ shouldStartLoadWithRequest:(NSURLRequest *)request
navigationType:(UIWebViewNavigationType)navigationType
{
    //DLog(@"webView shouldStartLoadWithRequest %@", request);
    return YES;
}

- (void)webViewDidFinishLoad:(UIWebView *)webView_
{
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
}

- (NSString *)getAccesstoken
{
    return [webView stringByEvaluatingJavaScriptFromString:@"capture.getAccessToken();"];
}

- (void)webViewDidStartLoad:(UIWebView *)webView_
{
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
}

- (NSUInteger)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskAllButUpsideDown;
}

@end
