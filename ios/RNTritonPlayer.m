
#import "RNTritonPlayer.h"

@implementation RNTritonPlayer

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(play:(NSString *)tritonName tritonStation:(NSString *)tritonStation)
{
    NSDictionary *settings = @{
                               SettingsStationNameKey : tritonName,
                               SettingsBroadcasterKey : @"Triton Digital",
                               SettingsMountKey : tritonStation
                               };
    
    
    
    self.tritonPlayer = [[TritonPlayer alloc] initWithDelegate:self andSettings:settings];
    
    
    [self.tritonPlayer play];
}

@end
  
