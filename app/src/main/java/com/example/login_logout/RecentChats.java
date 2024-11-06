package com.example.login_logout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    ArrayList<HelperClass> list = new ArrayList<>();
    UserAdapter adapter;
    RecyclerView recyclerView;
    TextView currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_chats);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.individual_user_recycler);
        currentUser = findViewById(R.id.currentUser);

        //If the user is recognised
        String currentUsername = getIntent().getStringExtra("currentUser");
        if(currentUsername != null){
            currentUser.setText(currentUsername);
        }
        else {
            currentUser.setText("Unknown");
        }

        // Set up the adapter and layout manager
        adapter = new UserAdapter(this, list); // Correctly use the class variable
        recyclerView.setAdapter(adapter);

        // Attach a layout manager to RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Or GridLayoutManager if desired

        // Initialize Firebase database and data listener
        database = FirebaseDatabase.getInstance();
        loadDataFromFirebase();
    }

    private void loadDataFromFirebase(){
        // Show loading indicator if necessary

        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String username = sharedPreferences.getString("username", null);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HelperClass helperClass = dataSnapshot.getValue(HelperClass.class);
                    String user = dataSnapshot.getKey();
                    if (helperClass != null && (username == null || !username.equals(user))) {
                        helperClass.setUsername(user);
                        list.add(helperClass);
                    }
                }
                adapter.notifyDataSetChanged(); // Notify the adapter of data changes
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
        if (id == R.id.logout) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        // Access SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
            databaseReference.child(username).child("status").setValue(false);

            Intent intent = new Intent(RecentChats.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(RecentChats.this, "User is already logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RecentChats.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
