
#import "RNTritonPlayer.h"

NSString* const EventTrackChanged = @"trackChanged";

@implementation RNTritonPlayer

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
 }
RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents
{
    return @[EventTrackChanged];
}

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
    if ([cuePointEvent.type isEqualToString:EventTypeAd]) {
        [self sendEventWithName:EventTrackChanged body:@{@"artist": @"-", @"title": @"-", @"isAd": @TRUE}];
    } else if ([cuePointEvent.type isEqualToString:EventTypeTrack]) {
        NSString *songTitle = [cuePointEvent.data objectForKey:CommonCueTitleKey];
        NSString *artistName = [cuePointEvent.data objectForKey:TrackArtistNameKey];
        
        [self sendEventWithName:EventTrackChanged body:@{@"artist": artistName, @"title": songTitle, @"isAd": @FALSE}];
    }
}

@end
  
