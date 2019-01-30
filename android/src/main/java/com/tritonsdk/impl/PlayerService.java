package com.tritonsdk.impl;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.tritondigital.player.MediaPlayer;
import com.tritondigital.player.TritonPlayer;
import com.tritonsdk.R;


public class PlayerService extends Service implements TritonPlayer.OnCuePointReceivedListener, TritonPlayer.OnStateChangedListener, TritonPlayer.OnMetaDataReceivedListener {

    // Constants
    public static final String ARG_STREAM = "stream";
    public static final String ARG_TRACK = "track";
    public static final String ARG_STATE = "state";
    public static final String DEFAULT_CHANNEL = "default";
    public static final String ACTION_INIT = "PlayerService.ACTION_INIT";
    public static final String ACTION_PLAY = "PlayerService.ACTION_PLAY";
    public static final String ACTION_STOP = "PlayerService.ACTION_STOP";
    public static final String ACTION_QUIT = "PlayerService.ACTION_QUIT";
    public static final String CUE_TYPE_TRACK = "track";
    public static final String CUE_TYPE_AD = "ad";
    public static final String EVENT_TRACK_CHANGED = "PlayerService.EVENT_TRACK_CHANGED";
    public static final String EVENT_STREAM_CHANGED = "PlayerService.EVENT_STREAM_CHANGED";
    public static final String EVENT_STATE_CHANGED = "PlayerService.EVENT_STATE_CHANGED";
    public static final int NOTIFICATION_SERVICE = 8;

    // Binder
    private final IBinder iBinder = new LocalBinder();

    // Player
    private TritonPlayer mPlayer;
    private Stream mCurrentStream;
    private Track mCurrentTrack;

    // Notification
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mRemoteViews;
    private NotificationManager mNotificationManager;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_INIT:
                    // nothing
                    break;
                case ACTION_PLAY:
                    if (intent.hasExtra(ARG_STREAM)) {
                        mCurrentStream = (Stream) intent.getSerializableExtra(ARG_STREAM);
                        notifyStationUpdate();
                        mCurrentTrack = null;
                        notifyTrackUpdate();
                    }

                    play();
                    break;
                case ACTION_QUIT:
                    releasePlayer();
                    stopForeground(true);
                    stopSelf();
                    break;
                case ACTION_STOP:
                    stop();
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void playMedia() {
        if (mCurrentStream == null) return;

        Bundle settings = new Bundle();
        settings.putString(TritonPlayer.SETTINGS_STATION_BROADCASTER, "Triton Digital");
        settings.putString(TritonPlayer.SETTINGS_STATION_NAME, mCurrentStream.getTritonName());
        settings.putString(TritonPlayer.SETTINGS_STATION_MOUNT, mCurrentStream.getTritonMount());
        mPlayer = new TritonPlayer(this, settings);
        mPlayer.setOnStateChangedListener(this);
        mPlayer.setOnCuePointReceivedListener(this);
        mPlayer.setOnMetaDataReceivedListener(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mPlayer.play();
                Looper.loop();
            }
        }).start();
    }

    private boolean isPlaying() {
        return mPlayer != null && mPlayer.getState() == TritonPlayer.STATE_PLAYING;
    }

    public void play() {
        if (mCurrentStream == null) return;
        releasePlayer();
        playMedia();
        showNotification();
    }

    public void stop() {
        if (!isPlaying() || mPlayer == null) return;
        mPlayer.stop();
    }

    public int getState() {
        if (mPlayer == null) {
            return -1;
        }
        return mPlayer.getState();
    }

    private void releasePlayer() {
        if (mPlayer == null) return;
        int state = mPlayer.getState();
        if (state == TritonPlayer.STATE_CONNECTING || state == TritonPlayer.STATE_PLAYING || state == TritonPlayer.STATE_PAUSED) {
            mPlayer.stop();
        }
        mPlayer.release();
        mPlayer = null;
    }

    private void pause() {
        if (!isPlaying()) return;
        mPlayer.pause();
    }

    public Stream getCurrentStream() {
        return mCurrentStream;
    }

    public Track getCurrentTrack() {
        return mCurrentTrack;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCuePointReceived(MediaPlayer mediaPlayer, Bundle cuePoint) {
        if (cuePoint == null) return;


        String cueType = cuePoint.getString("cue_type", null);
        if (cueType == null) return;
        switch (cueType) {
            case CUE_TYPE_TRACK:
                if (cuePoint.containsKey("cue_title") && cuePoint.containsKey("track_artist_name")) {
                    String artist = cuePoint.getString("track_artist_name");
                    String song = cuePoint.getString("cue_title");
                    mCurrentTrack = new Track(song, artist);

                    mRemoteViews.setTextViewText(R.id.song_title, mCurrentTrack.getTitle());
                    mRemoteViews.setTextViewText(R.id.station_artist, mCurrentTrack.getArtist());

                    mNotificationManager.notify(NOTIFICATION_SERVICE, mBuilder.build());

                    notifyTrackUpdate();
                }
                break;
            case CUE_TYPE_AD:
                mCurrentTrack = new Track(true);
                mRemoteViews.setTextViewText(R.id.song_title, "Reclame");
                mRemoteViews.setTextViewText(R.id.station_artist, "Reclame");

                mNotificationManager.notify(NOTIFICATION_SERVICE, mBuilder.build());

                notifyTrackUpdate();
                break;
        }
    }

    private void notifyTrackUpdate() {
        Intent intent = new Intent(EVENT_TRACK_CHANGED);
        intent.putExtra(ARG_TRACK, mCurrentTrack);
        sendBroadcast(intent);
    }

    private void notifyStationUpdate() {
        Intent intent = new Intent(EVENT_STREAM_CHANGED);
        intent.putExtra(ARG_STREAM, mCurrentStream);
        sendBroadcast(intent);
    }

    private void notifyStateUpdate(int state) {
        Intent intent = new Intent(EVENT_STATE_CHANGED);
        intent.putExtra(ARG_STREAM, mCurrentStream);
        intent.putExtra(ARG_STATE, state);
        sendBroadcast(intent);
    }

    @Override
    public void onStateChanged(MediaPlayer mediaPlayer, int state) {
        updateNotification();
        notifyStateUpdate(state);
    }

    @Override
    public void onMetaDataReceived(MediaPlayer mediaPlayer, Bundle bundle) {
        if (bundle == null) return;
    }

    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    public void showNotification() {
        if (isShowingNotification()) {
            return;
        }
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Music player notification";
            String description = "Music player notifications for this app.";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(DEFAULT_CHANNEL, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        mRemoteViews = new RemoteViews(getPackageName(), R.layout.slam_player_small);
        mBuilder = new NotificationCompat.Builder(this, DEFAULT_CHANNEL);

        mBuilder
                .setCustomContentView(mRemoteViews)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_player_notification); //small icon
        startForeground(NOTIFICATION_SERVICE, mBuilder.build());

        updateNotification();
    }

    private void updateNotification() {
        if (isShowingNotification()) {

            Intent stopIntent = new Intent(this, PlayerService.class);
            stopIntent.setAction(ACTION_STOP);
            PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent quitIntent = new Intent(this, PlayerService.class);
            quitIntent.setAction(ACTION_QUIT);
            PendingIntent pendingQuitIntent = PendingIntent.getService(this, 0, quitIntent, 0);

            Intent playIntent = new Intent(this, PlayerService.class);
            //playIntent.putExtra(ARG_STATION, mCurrentStation);
            playIntent.setAction(ACTION_PLAY);
            PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // use right actions depending on playstate
            if (isPlaying()) {
                mRemoteViews.setOnClickPendingIntent(R.id.station_play_pause_button, pausePendingIntent);
                //mRemoteViews.setImageViewResource(R.id.station_audio_image, R.drawable.icon_state_pause);
            } else {
                mRemoteViews.setOnClickPendingIntent(R.id.station_play_pause_button, playPendingIntent);
                //mRemoteViews.setImageViewResource(R.id.station_audio_image, R.drawable.icon_state_play);
            }
            mRemoteViews.setOnClickPendingIntent(R.id.station_exit, pendingQuitIntent);

            mNotificationManager.notify(NOTIFICATION_SERVICE, mBuilder.build());
        }
    }

    public boolean isShowingNotification() {
        return mBuilder != null;
    }
}

