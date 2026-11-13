package com.example.simplemusicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

public class MusicService extends Service {

    public static final String ACTION_PLAY = "com.example.simplemusicplayer.PLAY";
    public static final String ACTION_PAUSE = "com.example.simplemusicplayer.PAUSE";
    public static final String ACTION_STOP = "com.example.simplemusicplayer.STOP";

    private static final String CHANNEL_ID = "music_channel";
    private MediaPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_PLAY : intent.getAction();

        if (ACTION_STOP.equals(action)) {
            stopPlayer();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(1, makeNotification());

        if (ACTION_PAUSE.equals(action)) {
            if (player != null && player.isPlaying()) {
                player.pause();
            }
        } else {
            playMusic();
        }

        return START_STICKY;
    }

    private void playMusic() {
        if (player == null) {
            player = MediaPlayer.create(this, R.raw.sample_music);
            if (player == null) {
                stopSelf();
                return;
            }
            player.setOnCompletionListener(mp -> {
                mp.seekTo(0);
                mp.start();
            });
        }

        if (!player.isPlaying()) {
            player.start();
        }
    }

    private void stopPlayer() {
        if (player != null) {
            if (player.isPlaying()) player.stop();
            player.release();
            player = null;
        }
    }

    private PendingIntent actionIntent(String action, int requestCode) {
        Intent intent = new Intent(this, MusicReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private Notification makeNotification() {
        Intent openApp = new Intent(this, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(this, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("پخش‌کننده موسیقی")
                .setContentText("آهنگ نمونه")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(openPending)
                .addAction(android.R.drawable.ic_media_play, "پخش", actionIntent(ACTION_PLAY, 1))
                .addAction(android.R.drawable.ic_media_pause, "مکث", actionIntent(ACTION_PAUSE, 2))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "توقف", actionIntent(ACTION_STOP, 3))
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "پخش موسیقی", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        stopPlayer();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
