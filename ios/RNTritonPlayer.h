
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

#import <TritonPlayerSDK/TritonPlayerSDK.h>

@interface RNTritonPlayer : NSObject <RCTBridgeModule>

    @property (strong, nonatomic) TritonPlayer *tritonPlayer;

@end
  
