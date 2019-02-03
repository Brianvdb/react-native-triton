
#import "RNTritonPlayer.h"

@implementation RNTritonPlayer

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
 }
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(play:(NSString *)tritonName tritonStation:(NSString *)tritonStation)
{
    if (self.tritonPlayer == NULL) {
        self.tritonPlayer = [[TritonPlayer alloc] initWithDelegate:self andSettings:nil];
    }
    
    NSDictionary *settings = @{
                               SettingsStationNameKey : tritonName,
                               SettingsBroadcasterKey : @"Triton Digital",
                               SettingsMountKey : tritonStation
                               };
    
    if (self.tritonPlayer.state == kTDPlayerStatePlaying) {
        [self.tritonPlayer stop];
    }
    
    [self.tritonPlayer updateSettings:settings];
    
    [self.tritonPlayer play];
}

- (void)player:(TritonPlayer *)player didChangeState:(TDPlayerState)state {
    
}

- (void)player:(TritonPlayer *)player didReceiveCuePointEvent:(CuePointEvent *)cuePointEvent {
    
}

@end
  
