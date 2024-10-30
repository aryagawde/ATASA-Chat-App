package com.example.login_logout;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_logout.Adapter.UserAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RecentChats extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference reference;
    ArrayList<HelperClass> list = new ArrayList<>();
    UserAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_chats);

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.individual_user_recycler); // Make sure the ID matches your XML

        // Set up the adapter and layout manager
        UserAdapter adapter = new UserAdapter(this, list);
        recyclerView.setAdapter(adapter);

        // Attach a layout manager to RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Or GridLayoutManager if desired

        // Initialize Firebase database and data listener
        database = FirebaseDatabase.getInstance();
        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String username = sharedPreferences.getString("username", null);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HelperClass helperClass = dataSnapshot.getValue(HelperClass.class);
                    String user = dataSnapshot.getKey();
                    if (helperClass != null && (username == null || !user.equals(username))) {
                        helperClass.setUsername(user);
                        list.add(helperClass);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors if any
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        // Access SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userID = sharedPreferences.getString("username", null);

        if (userID != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
            databaseReference.child(userID).child("isLoggedIn").setValue(false);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(RecentChats.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(RecentChats.this, "User is already logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RecentChats.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}