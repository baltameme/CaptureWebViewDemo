#import "CaptureWebViewController.h"
#import "debug_log.h"

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
@property(weak) id<CaptureWebViewControllerDelegate> captureWebViewDelegate;
@property(nonatomic, strong) NSString *activePageName;

@end

@implementation CaptureWebViewController

static NSDictionary *JR_CAPTURE_WEBVIEW_PAGES;

+(void)initialize
{
    JR_CAPTURE_WEBVIEW_PAGES = @{
            @"signin" : @{
                    @"title" : @"",
                    @"url" : @"http://janrain.github.com/CaptureWebViewDemo/index.html"
            },
            @"profile" : @{
                    @"title" : @"",
                    @"url" : @"http://janrain.github.com/CaptureWebViewDemo/edit-profile.html"
            }
    };

    // Dirty hack to enable the native app JS bridge by altering the UA string:
    NSString *oldUa = @"Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Mobile/10A403";
    NSString *newUa = [NSString stringWithFormat:@"%@ janrainNativeAppBridgeEnabled", oldUa];
    NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:newUa, @"UserAgent", nil];
    [[NSUserDefaults standardUserDefaults] registerDefaults:dictionary];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self setTitle:[[JR_CAPTURE_WEBVIEW_PAGES objectForKey:activePageName] objectForKey:@"title"]];
}

@synthesize webView;
@synthesize captureWebViewDelegate;
@synthesize activePageName;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
          andDelegate:(id<CaptureWebViewControllerDelegate>)delegate
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        self.captureWebViewDelegate = delegate;
    }
    return self;
}

- (void)addEventHandler:(void (^)(NSArray *))handler eventName:(NSString *)eventName
{
    if (!self.jsEventHandlers) self.jsEventHandlers = [NSMutableDictionary dictionary];

    if (![self.jsEventHandlers objectForKey:eventName])
    {
        [self.jsEventHandlers setObject:[NSMutableArray array] forKey:eventName];
    }

    [[self.jsEventHandlers objectForKey:eventName] addObject:handler];
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

//- (void)setWidgetAccessToken:(NSString *)accessToken
//{
//    NSString *jsStatement = [NSString stringWithFormat:@"janrain.capture.ui.createCaptureSession(%@);", accessToken];
//    [webView stringByEvaluatingJavaScriptFromString:jsStatement];
//}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)webView:(UIWebView *)webView_ didFailLoadWithError:(NSError *)error
{
    DLog(@"webView load error: %@", error);
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
}

- (void)parseAndDispatchEventUrl:(NSString *)argsUrl
{
    // argsUrl will look like: janrain:eventNameHere?arguments=URL%20ENCODED%20JSON%20ARRAY%20HERE
    NSString *pathString = [argsUrl substringFromIndex:[@"janrain:" length]];
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

    // probably should use NSNotificationCenter instead
    NSArray *handlers = [self.jsEventHandlers objectForKey:eventName];
    for (void (^h)(NSArray *) in handlers)
    {
        h(eventArgs);
    }

    NSString *argsDescription = ([[eventArgs description] length] > 100) ?
            [[eventArgs description] substringToIndex:80] : [eventArgs description];
    DLog(@"event: %@ args: %@", eventName, argsDescription);
}

- (BOOL)webView:(UIWebView *)webView_ shouldStartLoadWithRequest:(NSURLRequest *)request
 navigationType:(UIWebViewNavigationType)navigationType
{
    if ([request.URL.scheme isEqualToString:@"janrain"])
    {
        [self dispatchEventQueue];
        return NO;
    }
    DLog(@"webView shouldStartLoadWithRequest %@", request);
    return YES;
}

- (void)dispatchEventQueue
{
    // Pulls events out of JS bridge queue
    NSString *bridgeJson = [webView stringByEvaluatingJavaScriptFromString:@""
            "(function() {"
            "    if (typeof createJanrainBridge.eventQueue === 'undefined') return \"undefined queue\";"
            "    var t = JSON.stringify(createJanrainBridge.eventQueue);"
            "    createJanrainBridge.eventQueue.length = 0;"
            "    return t;"
            "})();"];

    NSArray *queuedEventUrls =
            [NSJSONSerialization JSONObjectWithData:[bridgeJson dataUsingEncoding:NSUTF8StringEncoding] options:0
                                              error:nil];

    for (NSString *eventUrl in queuedEventUrls)
    {
        [self parseAndDispatchEventUrl:eventUrl];
    }
}

- (void)sendOptionalDelegateMessage:(SEL)selector withArgument:(id)argument
{
    if ([captureWebViewDelegate respondsToSelector:selector])
    {
        [captureWebViewDelegate performSelector:selector withObject:argument];
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
