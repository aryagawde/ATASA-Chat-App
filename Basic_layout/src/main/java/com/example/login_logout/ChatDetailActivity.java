package com.example.login_logout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatDetailActivity extends AppCompatActivity{
    TextView userfield, status;
    Button back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        getSupportActionBar().hide();
        back = findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(ChatDetailActivity.this, RecentChats.class);
                startActivity(intent);
                finish();
            }
        });
        String username = getIntent().getStringExtra("username");
        String isOnline = getIntent().getStringExtra("isLoggedIn");
        userfield = findViewById(R.id.user_name);
        userfield.setText(username);
        status = findViewById(R.id.status);
        status.setText(isOnline);

    }
}