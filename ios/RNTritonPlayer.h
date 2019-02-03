#import <React/RCTBridgeModule.h>
#import "TritonPlayerSDK/TritonPlayerSDK.h"

@interface RNTritonPlayer : NSObject <RCTBridgeModule, TritonPlayerDelegate>

    @property (strong, nonatomic) TritonPlayer *tritonPlayer;

@end
  
