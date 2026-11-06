package com.example.mobile2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private final String USERNAME = "admin";
    private final String PASSWORD = "1234";

    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.btnLogin);

        loginButton.setOnClickListener(v -> {

            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (username.equals(USERNAME) && password.equals(PASSWORD)) {

                Intent intent = new Intent(MainActivity.this, homepage.class);
                intent.putExtra("username", username);
                startActivity(intent);

            } else {

                Toast.makeText(
                        MainActivity.this,
                        "نام کاربری یا رمز عبور اشتباه است",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}