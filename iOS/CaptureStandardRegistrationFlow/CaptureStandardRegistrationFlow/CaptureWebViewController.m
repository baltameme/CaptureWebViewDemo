#import "CaptureWebViewController.h"
#import "debug_log.h"
#import "stdarg.h"

@implementation NSString (Janrain_Url_Escaping)
- (NSString *)stringByUrlEncoding
{
    NSString *encodedString = (__bridge_transfer NSString *) CFURLCreateStringByAddingPercentEscapes(
            NULL,
            (__bridge CFStringRef) self,
            NULL,
            (CFStringRef) @"!*'();:@&=+$,/?%#[]",
            kCFStringEncodingUTF8);

    return encodedString;
}

- (NSString *)stringByUrlDecoding
{
    return [self stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
}
@end

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
        if ([request.URL.absoluteString hasPrefix:@"janrain:accessToken"])
        {
            NSString *token = [[request.URL.absoluteString componentsSeparatedByString:@"="] objectAtIndex:1];
            [self sendOptionalDelegateMessage:@selector(signInDidSucceedWithAccessToken:) withArgument:token];
        }
        else
        {
            // General case of JS <-> host event bridging
            NSString *pathString = [request.URL.absoluteString substringFromIndex:[@"janrain:" length]];
            NSArray *pathComponents = [pathString componentsSeparatedByString:@"?"];
            NSString *eventName = [pathComponents objectAtIndex:0];
            NSString *argsComponent = [pathComponents objectAtIndex:1];
            NSArray *argPairs = [argsComponent componentsSeparatedByString:@"&"];
            NSMutableDictionary *argsDict = [NSMutableDictionary dictionary];
            for (id argPair in argPairs)
            {
                NSArray *sides = [argPair componentsSeparatedByString:@"="];
                [argsDict setObject:[sides objectAtIndex:1] forKey:[sides objectAtIndex:0]];
            }

            NSString *eventArgsJson = [[argsDict objectForKey:@"arguments"] stringByUrlDecoding];
            NSData *eventArgsJsonData = [eventArgsJson dataUsingEncoding:NSUTF8StringEncoding];
            id eventArgs = [NSJSONSerialization JSONObjectWithData:eventArgsJsonData options:0 error:nil];

            DLog(@"event: %@ args: %@", eventName, eventArgs);

            return NO;
        }
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
    [webView stringByEvaluatingJavaScriptFromString:@"janrainNativeAppBridgeEnabled = true;"];
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
