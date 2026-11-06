package com.example.mobile2;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class homepage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        TextView welcomeText = findViewById(R.id.welcomeText);

        String username = getIntent().getStringExtra("username");

        welcomeText.setText(username + " خوش آمدید\nورود موفق");
    }
}