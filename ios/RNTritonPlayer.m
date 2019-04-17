
#import "RNTritonPlayer.h"

NSString* const EventTrackChanged = @"trackChanged";
NSString* const EventStreamChanged = @"streamChanged";
NSString* const EventStateChanged = @"stateChanged";

const NSInteger STATE_COMPLETED = 200;
const NSInteger STATE_CONNECTING = 201;
const NSInteger STATE_ERROR = 202;
const NSInteger STATE_PLAYING = 203;
const NSInteger STATE_RELEASED = 204;
const NSInteger STATE_STOPPED = 205;
const NSInteger STATE_PAUSED = 206;

@implementation RNTritonPlayer

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents
{
    return @[EventTrackChanged, EventStreamChanged, EventStateChanged];
}

RCT_EXPORT_METHOD(play:(NSString *)tritonName tritonStation:(NSString *)tritonStation)
{
    // Init Triton Player if its not set yet
    if (self.tritonPlayer == NULL) {
        self.tritonPlayer = [[TritonPlayer alloc] initWithDelegate:self andSettings:nil];
        self.track = @"-";
        self.title = @"-";
        self.state = 0;
    }
    
    // Set Station Details
    NSDictionary *settings = @{
                               SettingsStationNameKey : tritonName,
                               SettingsBroadcasterKey : @"Triton Digital",
                               SettingsMountKey : tritonStation,
                               SettingsPlayerServicesRegion: @"EU",
                               SettingsEnableLocationTrackingKey : @(YES),
                               SettingsTtagKey : @[@"PLAYER:NOPREROLL"]
                               };
    
    // Stop Current Stream (if playing)
    //if ([self.tritonPlayer isExecuting]) {
    [self.tritonPlayer stop];
    //}
    
    // Setup stuff
    [self configureRemoteCommandHandling];
    
    // Update Triton Player settings
    [self.tritonPlayer updateSettings:settings];
    
    // Start Playing!
    [self.tritonPlayer play];
    
    // Notify stream change
    [self sendEventWithName:EventStreamChanged body:@{@"stream": tritonStation}];
}

RCT_EXPORT_METHOD(stop)
{
    if (self.tritonPlayer != NULL) {
        [self.tritonPlayer stop];
    }
}

RCT_EXPORT_METHOD(pause)
{
    if (self.tritonPlayer != NULL && self.tritonPlayer.state == kTDPlayerStatePlaying) {
        [self.tritonPlayer pause];
    }
}

RCT_EXPORT_METHOD(unPause)
{
    if (self.tritonPlayer != NULL) {
        [self.tritonPlayer play];
    }
}

RCT_EXPORT_METHOD(quit)
{
    
}


- (void)player:(TritonPlayer *)player didChangeState:(TDPlayerState)state {
    NSInteger eventState;
    
    // Map to Android value..
    switch(state) {
        case kTDPlayerStateStopped:
            eventState = STATE_RELEASED;
            break;
        case kTDPlayerStatePlaying:
            eventState = STATE_PLAYING;
            break;
        case kTDPlayerStateConnecting:
            eventState = STATE_CONNECTING;
            break;
        case kTDPlayerStatePaused:
            eventState = STATE_PAUSED;
            break;
        case kTDPlayerStateError:
            eventState = STATE_ERROR;
            break;
        case kTDPlayerStateCompleted:
            eventState = STATE_COMPLETED;
            break;
    }
    
    self.state = eventState;
    
    // Notify state change
    [self sendEventWithName:EventStateChanged body:@{@"state": @(eventState)}];
    [self configureNowPlayingInfo];
}

- (void)player:(TritonPlayer *)player didReceiveCuePointEvent:(CuePointEvent *)cuePointEvent {
    [self configureNowPlayingInfo];
    if ([cuePointEvent.type isEqualToString:EventTypeAd]) {
        // Type CUE ad
        [self sendEventWithName:EventTrackChanged body:@{@"artist": @"-", @"title": @"-", @"isAd": @TRUE}];
        self.track = @"-";
        self.title = @"-";
    } else if ([cuePointEvent.type isEqualToString:EventTypeTrack]) {
        // Type CUE track
        
        NSString *songTitle = [cuePointEvent.data objectForKey:CommonCueTitleKey];
        NSString *artistName = [cuePointEvent.data objectForKey:TrackArtistNameKey];
        NSString *durationTime = [cuePointEvent.data objectForKey:CommonCueTimeDurationKey];
        
        NSInteger duration = 0;
        
        if (durationTime != NULL) {
            duration = [durationTime integerValue];
        }
        
        [self sendEventWithName:EventTrackChanged body:@{@"artist": artistName, @"title": songTitle, @"duration": @(duration), @"isAd": @FALSE}];
        
        self.track = artistName;
        self.title = songTitle;
    }
}

- (void)playerBeginInterruption:(TritonPlayer *) player {
    if (self.tritonPlayer != NULL && [self.tritonPlayer isExecuting]) {
        [self.tritonPlayer stop];
        [self sendEventWithName:EventStateChanged body:@{@"state": @(STATE_RELEASED)}];
        self.tritonPlayer = NULL;
        self.interruptedOnPlayback = YES;
    }
}

- (void)playerEndInterruption:(TritonPlayer *) player {
    if (self.tritonPlayer != NULL && self.interruptedOnPlayback) {
        self.interruptedOnPlayback = NO;
    }
}

- (void)configureRemoteCommandHandling
{
    MPRemoteCommandCenter *commandCenter = [MPRemoteCommandCenter sharedCommandCenter];
    
    [commandCenter.playCommand setEnabled:true];
    // register to receive remote play event
    [commandCenter.playCommand addTargetWithHandler:^MPRemoteCommandHandlerStatus(MPRemoteCommandEvent * _Nonnull event) {
        [self.tritonPlayer play];
        return MPRemoteCommandHandlerStatusSuccess;
    }];
    
    [commandCenter.pauseCommand setEnabled:true];
    // register to receive remote pause event
    [commandCenter.pauseCommand addTargetWithHandler:^MPRemoteCommandHandlerStatus(MPRemoteCommandEvent * _Nonnull event) {
        [self.tritonPlayer pause];
        return MPRemoteCommandHandlerStatusSuccess;
    }];
}

- (void)configureNowPlayingInfo
{
    
    MPNowPlayingInfoCenter* info = [MPNowPlayingInfoCenter defaultCenter];
    NSMutableDictionary* newInfo = [NSMutableDictionary dictionary];
    
    // Set song title info
    [newInfo setObject:self.title forKey:MPMediaItemPropertyTitle];
    [newInfo setObject:self.track forKey:MPMediaItemPropertyArtist];
    
    [newInfo setValue:[NSNumber numberWithDouble:1] forKey:MPNowPlayingInfoPropertyPlaybackRate];
    
    
    if (self.state == STATE_PAUSED) {
        info.playbackState = MPMusicPlaybackStatePaused;
        [newInfo setValue:[NSNumber numberWithDouble:0] forKey:MPNowPlayingInfoPropertyPlaybackRate];
    } else if (self.state == STATE_PLAYING) {
        info.playbackState = MPMusicPlaybackStatePlaying;
        [newInfo setValue:[NSNumber numberWithDouble:1] forKey:MPNowPlayingInfoPropertyPlaybackRate];
    } else if (self.state == STATE_STOPPED) {
        info.playbackState = MPMusicPlaybackStateStopped;
        [newInfo setValue:[NSNumber numberWithDouble:0] forKey:MPNowPlayingInfoPropertyPlaybackRate];
    }
    // Update the now playing info
    info.nowPlayingInfo = newInfo;
}

@end

