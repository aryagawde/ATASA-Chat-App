package com.example.login_logout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import android.widget.Button;
import android.widget.EditText;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    EditText full_name, username, password;
    Button already_registered, signup;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        full_name = findViewById(R.id.full_name);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        already_registered = findViewById(R.id.already_registered);
        signup = findViewById(R.id.signup);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String FullName = full_name.getText().toString();
                String UserName = username.getText().toString();
                String Password = password.getText().toString();

                processFormFields(FullName, UserName, Password);
            }
        });

        already_registered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToSignIn();}
        });
    }

    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public void processFormFields(String fullName, String userName, String password) {
        boolean isFullNameValid = validateFullName(fullName);
        boolean isPasswordValid = validatePassword(password);

        if (isFullNameValid && isPasswordValid) {
            validateUsername(userName, fullName, password);
        } else {
            Toast.makeText(MainActivity.this, "Registration Unsuccessful", Toast.LENGTH_LONG).show();
        }
    }

    public void validateUsername(String userName, String fullName, String password) {
        if (userName.isEmpty()) {
            username.setError("Username cannot be empty!");
            return;
        }
        username.setError(null);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query check = reference.orderByChild("username").equalTo(userName);

        check.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(MainActivity.this, "Username already exists", Toast.LENGTH_LONG).show();
                } else {
                    String userId = generateUserId();
                    registerUser(fullName, userName, password, userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Database Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void registerUser(String fullName, String userName, String password, String userId) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        String hashedPassword = hashPassword(password);
        HelperClass helperClass = new HelperClass(hashedPassword, userName, fullName, false, userId);
        reference.child(userName).setValue(helperClass);

        Toast.makeText(MainActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
        GoToSignIn();
    }

    public boolean validatePassword(String password) {
        if (password.isEmpty()) {
            this.password.setError("Password cannot be empty!");
            return false;
        }
        this.password.setError(null);
        return true;
    }

    public boolean validateFullName(String fullName) {
        if (fullName.isEmpty()) {
            this.full_name.setError("Full Name cannot be empty!");
            return false;
        }
        this.full_name.setError(null);
        return true;
    }

    public void GoToSignIn() {
        Intent intent = new Intent(MainActivity.this, Login_Activity.class);
        startActivity(intent);
        finish();
    }

    public String generateUserId() {
        String userId = UUID.randomUUID().toString().replace("-", "");
        return userId;
    }
}
