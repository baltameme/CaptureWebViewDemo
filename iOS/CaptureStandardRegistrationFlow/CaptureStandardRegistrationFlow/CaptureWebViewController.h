#import <Foundation/Foundation.h>


@protocol CaptureWebViewControllerDelegate <NSObject>
@optional
- (void)captureWebViewWillCancel;
- (void)signInDidSucceedWithAccessToken:(NSString *)accessToken;
@end

@interface CaptureWebViewController : UIViewController <UIWebViewDelegate>

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
          andDelegate:(id<CaptureWebViewControllerDelegate>)delegate;

- (void)pushFlow:(id)flow ontoNavigationController:(UINavigationController *)nc;

//- (void)presentFlow:(id)flow fromViewController:(UIViewController *)vc;

@end