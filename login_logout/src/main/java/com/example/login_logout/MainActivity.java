package com.example.login_logout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import org.mindrot.jbcrypt.BCrypt;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity{

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
        String FullName = full_name.getText().toString();
        String UserName = username.getText().toString();
        String Password = password.getText().toString();

        signup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                processFormFields(FullName, UserName, Password);
                GoToSignIn(view);
            }
        });
    }

    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public void processFormFields(String full_name, String username, String password){
        boolean p = validatePassword(password);
        boolean u = validateUsername(username);
        boolean f = validateFullName(full_name);
        String p = String.valueof
        Toast.makeText(MainActivity.this, p, u, f, Toast.LENGTH_LONG).show();
        if(!validatePassword(password) && !validateUsername(username) && !validateFullName(full_name)){
            database = FirebaseDatabase.getInstance();
            reference = database.getReference("users");
            String hashed_password = hashPassword(password);
            HelperClass helperClass = new HelperClass(hashed_password, username, full_name);
            reference.child(username).setValue(helperClass);
            Toast.makeText(MainActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(MainActivity.this, "Registration Unuccessful", Toast.LENGTH_LONG).show();
        }

    }

    public boolean validateUsername(String UserName){
        final boolean[] flag = { true };
        if(UserName.isEmpty()){
            username.setError("Username cannot be empty!");
            return false;
        }
        else{
            username.setError(null);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            Query check = reference.orderByChild("username").equalTo(UserName);
            check.addListenerForSingleValueEvent(new ValueEventListener(){
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        flag[0] = false;
                        Toast.makeText(MainActivity.this, "Username already exists", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        return flag[0];
    }
    public boolean validatePassword(String Password){
        if(Password.isEmpty()){
            password.setError("Password cannot be empty!");
            return false;
        }
        else{
            password.setError(null);
            return true;}
    }
    public boolean validateFullName(String FullName){
        if(FullName.isEmpty()){
            full_name.setError("Full Name cannot be empty!");
            return false;
        }
        else{
            full_name.setError(null);
            return true;
        }
    }
    public void GoToSignIn(View view) {
        Intent intent = new Intent(MainActivity.this, Login_Activity.class);
        startActivity(intent);
        finish();
    }
}