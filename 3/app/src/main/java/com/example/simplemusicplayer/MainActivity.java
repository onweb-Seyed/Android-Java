package com.example.simplemusicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private void sendAction(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 60, 32, 32);

        TextView title = new TextView(this);
        title.setText("پخش‌کننده موسیقی");
        title.setTextSize(26);
        title.setGravity(17);

        TextView info = new TextView(this);
        info.setText("آهنگ نمونه داخل برنامه پخش می‌شود. حتی با خروج از برنامه، پخش ادامه دارد.");
        info.setTextSize(16);
        info.setGravity(17);
        info.setPadding(0, 30, 0, 30);

        Button play = new Button(this);
        play.setText("پخش آهنگ");
        play.setOnClickListener(v -> sendAction(MusicService.ACTION_PLAY));

        Button pause = new Button(this);
        pause.setText("توقف موقت");
        pause.setOnClickListener(v -> sendAction(MusicService.ACTION_PAUSE));

        Button stop = new Button(this);
        stop.setText("توقف کامل");
        stop.setOnClickListener(v -> sendAction(MusicService.ACTION_STOP));

        layout.addView(title);
        layout.addView(info);
        layout.addView(play);
        layout.addView(pause);
        layout.addView(stop);
        setContentView(layout);
    }
}
