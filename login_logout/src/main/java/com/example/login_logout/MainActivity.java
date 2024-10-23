package com.example.login_logout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity{

    EditText full_name, username, password;
    Button already_registered, signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        full_name = findViewById(R.id.full_name);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        already_registered = findViewById(R.id.already_registered);
        signup = findViewById(R.id.signup);

        signup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                processFormFields();
                GoToSignIn(view);
            }
        });
    }
    public void processFormFields(){
        Toast.makeText(MainActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
    }

    public void GoToSignIn(View view) {
        Intent intent = new Intent(MainActivity.this, Login_Activity.class);
        startActivity(intent);
        finish();
    }
}