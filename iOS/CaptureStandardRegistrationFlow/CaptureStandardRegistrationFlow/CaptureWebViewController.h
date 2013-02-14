#import <Foundation/Foundation.h>


@protocol CaptureWebViewControllerDelegate <NSObject>
@optional
- (void)signInDidSucceedWithAccessToken:(NSDictionary *)accessToken;
@end

@interface CaptureWebViewController : UIViewController <UIWebViewDelegate>
/**
 * NSString -> NSArray map with (void)^(NSDictionary *eventArgs) blocks/handlers
 * It is okay to add handlers to this structure from outside of this class.
 */
@property(nonatomic, strong) id jsEventHandlers;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
          andDelegate:(id<CaptureWebViewControllerDelegate>)delegate;

- (void)pushFlow:(id)flow ontoNavigationController:(UINavigationController *)nc;

@end