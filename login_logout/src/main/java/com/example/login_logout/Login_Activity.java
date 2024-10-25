package com.example.login_logout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.mindrot.jbcrypt.BCrypt;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Login_Activity extends AppCompatActivity {

    EditText username_given, password_given;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username_given = findViewById(R.id.username_given);
        password_given = findViewById(R.id.password_given);
        login = findViewById(R.id.login);

        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                authenticate();
            }
        });
    }

    public boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public Boolean authenticate(){
        String username = username_given.getText().toString();
        String password = password_given.getText().toString();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query check = reference.orderByChild("username").equalTo(username);
        check.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String correctPassword = snapshot.child(username).child("password").getValue(String.class);
                    if(!checkPassword(password, correctPassword)){
                        Toast.makeText(Login_Activity.this, "Login unsuccessful", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(Login_Activity.this, "Login Successful", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(Login_Activity.this, "User does not exists", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return true;
    }
}