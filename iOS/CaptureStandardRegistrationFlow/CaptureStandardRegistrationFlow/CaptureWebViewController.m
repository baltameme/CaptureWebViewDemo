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
                        @"url" : @"http://janrain.github.com/CaptureWebViewDemo/index.html"
            },
            @"profile" : @{
                    @"title" : @"Update Profile",
                    @"url" : @"http://janrain.github.com/CaptureWebViewDemo/edit-profile.html"
            }
    };
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self setTitle:[[JR_CAPTURE_WEBVIEW_PAGES objectForKey:activePageName] objectForKey:@"title"]];
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
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
}

- (BOOL)webView:(UIWebView *)webView_ shouldStartLoadWithRequest:(NSURLRequest *)request
 navigationType:(UIWebViewNavigationType)navigationType
{
    if ([request.URL.scheme isEqualToString:@"janrain"])
    {
        NSString *token = [[request.URL.absoluteString componentsSeparatedByString:@"="] objectAtIndex:1];
        [self sendOptionalDelegateMessage:@selector(signInDidSucceedWithAccessToken:) withArgument:token];
    }
    DLog(@"webView shouldStartLoadWithRequest %@", request);
    return YES;
}

- (void)sendOptionalDelegateMessage:(SEL)selector withArgument:(id)argument
{
    if ([captureDelegate respondsToSelector:selector])
    {
        [captureDelegate performSelector:selector withObject:argument];
    }
}

- (void)webViewDidFinishLoad:(UIWebView *)webView_
{
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
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
