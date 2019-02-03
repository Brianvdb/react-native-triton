#import <React/RCTBridgeModule.h>
#import "TritonPlayerSDK/TritonPlayerSDK.h"

@interface RNTritonPlayer : NSObject <RCTBridgeModule>

    @property (strong, nonatomic) TritonPlayer *tritonPlayer;

@end
  
