
package com.tritonsdk;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tritonsdk.impl.PlayerService;
import com.tritonsdk.impl.Stream;
import com.tritonsdk.impl.Track;

import static android.content.Context.BIND_AUTO_CREATE;

public class RNTritonPlayerModule extends ReactContextBaseJavaModule {
    private static final String EVENT_TRACK_CHANGED = "trackChanged";
    private static final String EVENT_STATE_CHANGED = "stateChanged";
    private static final String EVENT_STREAM_CHANGED = "streamChanged";

    private final ReactApplicationContext reactContext;

    private PlayerService mService;
    private boolean mServiceBound;

    public RNTritonPlayerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        registerReceiver();
    }

    @Override
    public String getName() {
        return "RNTritonPlayer";
    }

    private void initPlayer() {
        if (!mServiceBound) {
            Intent intent = new Intent(reactContext, PlayerService.class);
            intent.setAction(PlayerService.ACTION_INIT);
            reactContext.bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
            reactContext.startService(intent);
        }
    }

    @ReactMethod
    public void play(String tritonName, String tritonMount) {
        initPlayer();

        if (mService != null) {
            mService.stop();
        }

        Intent intent = new Intent(reactContext, PlayerService.class);
        intent.setAction(PlayerService.ACTION_PLAY);
        intent.putExtra(PlayerService.ARG_STREAM, new Stream("", "", tritonName, tritonMount));
        reactContext.bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        reactContext.startService(intent);
    }

    @ReactMethod
    public void pause() {
        if (mService != null) {
            mService.pause();
        }
    }

    @ReactMethod
    public void unPause() {
        if (mService != null) {
            mService.unPause();
        }
    }

    @ReactMethod
    public void stop() {
        if (mService != null) {
            mService.stop();
        }
    }

    @ReactMethod
    public void quit() {
        if (mService != null) {
            mService.quit();
        }
    }

    private void sendEvent(String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    private void onStreamChanged(Stream stream) {
        WritableMap map = Arguments.createMap();
        map.putString("stream", stream.getTritonMount());

        sendEvent(EVENT_STREAM_CHANGED, map);
    }

    private void onStateChanged(int state) {
        WritableMap map = Arguments.createMap();
        map.putInt("state", state);

        sendEvent(EVENT_STATE_CHANGED, map);
    }

    private void onTrackChanged(Track track) {
        WritableMap map = Arguments.createMap();
        map.putString("artist", track != null ? track.getArtist() : "-");
        map.putString("title", track != null ? track.getTitle() : "-");
        map.putInt("duration", track != null ? track.getDuration() : 0);
        map.putBoolean("isAd", track != null && track.isAds());

        sendEvent(EVENT_TRACK_CHANGED, map);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mServiceBound = true;
            mService = ((PlayerService.LocalBinder) binder).getService();

            if (mService.getCurrentStream() != null) {
                onStreamChanged(mService.getCurrentStream());
            }
            if (mService.getCurrentTrack() != null) {
                onTrackChanged(mService.getCurrentTrack());
            }

            onStateChanged(mService.getState());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerService.EVENT_TRACK_CHANGED);
        filter.addAction(PlayerService.EVENT_STREAM_CHANGED);
        filter.addAction(PlayerService.EVENT_STATE_CHANGED);
        reactContext.registerReceiver(mReceiver, filter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;

            switch (intent.getAction()) {
                case PlayerService.EVENT_TRACK_CHANGED:
                    Track track = (Track) intent.getSerializableExtra(PlayerService.ARG_TRACK);
                    onTrackChanged(track);
                    break;
                case PlayerService.EVENT_STREAM_CHANGED:
                    Stream stream = (Stream) intent.getSerializableExtra(PlayerService.ARG_STREAM);
                    onStreamChanged(stream);
                    break;
                case PlayerService.EVENT_STATE_CHANGED:
                    int state = intent.getIntExtra(PlayerService.ARG_STATE, -1);
                    onStateChanged(state);
            }
        }
    };

}