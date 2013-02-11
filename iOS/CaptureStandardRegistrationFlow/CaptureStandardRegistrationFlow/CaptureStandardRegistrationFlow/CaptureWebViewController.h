#import <Foundation/Foundation.h>


@protocol CaptureWebViewControllerDelegate <NSObject>
@optional
- (void)captureWebViewWillCancel;
//- (void)signInDidSuccceedWithAccessToken:(NSString *)accessToken;
//- (void)signInDidFailWithError:(NSString *)error;
@end

@interface CaptureWebViewController : UIViewController <UIWebViewDelegate, UIAlertViewDelegate>
@property (weak) IBOutlet UIWebView *uiWebView;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
          andDelegate:(id<CaptureWebViewControllerDelegate>)delegate;
@end