// import RCTBridgeModule
#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#elif __has_include(“RCTBridgeModule.h”)
#import “RCTBridgeModule.h”
#else
#import “React/RCTBridgeModule.h” // Required when used as a Pod in a Swift project
#endif

// import RCTEventEmitter
#if __has_include(<React/RCTEventEmitter.h>)
#import <React/RCTEventEmitter.h>
#elif __has_include(“RCTEventEmitter.h”)
#import “RCTEventEmitter.h”
#else
#import “React/RCTEventEmitter.h” // Required when used as a Pod in a Swift project
#endif

#import "TritonPlayerSDK/TritonPlayerSDK.h"

extern NSString* const EventTrackChanged;
extern NSString* const EventStreamChanged;
extern NSString* const EventStateChanged;


extern const NSInteger STATE_COMPLETED;
extern const NSInteger STATE_CONNECTING;
extern const NSInteger STATE_ERROR;
extern const NSInteger STATE_PLAYING;
extern const NSInteger STATE_RELEASED;
extern const NSInteger STATE_STOPPED;
extern const NSInteger STATE_PAUSED;

@interface RNTritonPlayer : RCTEventEmitter <RCTBridgeModule, TritonPlayerDelegate>

    @property (strong, nonatomic) TritonPlayer *tritonPlayer;
    @property (strong, nonatomic) NSString *track;
    @property (strong, nonatomic) NSString *title;
    @property (nonatomic) NSInteger state;
    @property (nonatomic) BOOL interruptedOnPlayback;

@end
  
