package com.example.login_logout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_logout.Adapter.GroupListAdapter;
import com.example.login_logout.Adapter.UserSelectionAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.rpc.Help;

import java.util.ArrayList;
import java.util.List;

public class GroupChats extends AppCompatActivity {

    Button back;
    TextView user;
    FloatingActionButton addGroup;
    private RecyclerView groupRecyclerView;
    private GroupListAdapter groupAdapter;
    private List<GroupClass> groupList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chats);

        // Initialize views
        user = findViewById(R.id.currentUser1); // Initialize TextView here
        back = findViewById(R.id.back_button1);
        addGroup = findViewById(R.id.addGroup);
        groupRecyclerView = findViewById(R.id.group_chat_recycler);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get currentUser from intent
        String currentUser = getIntent().getStringExtra("currentUser");
        user.setText(currentUser);

        groupAdapter = new GroupListAdapter(this, groupList);
        groupRecyclerView.setAdapter(groupAdapter);

        loadGroupsFromFirebase();

        back.setOnClickListener(v -> {
            Intent intent = new Intent(GroupChats.this, RecentChats.class);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
            finish();
        });

        addGroup.setOnClickListener(v -> fetchUsersAndShowDialog());
    }

    private void loadGroupsFromFirebase() {
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("groups");
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                groupList.clear();
                for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                    GroupClass group = groupSnapshot.getValue(GroupClass.class);
                    if (group != null) {
                        group.setGroupId(groupSnapshot.getKey());
                        groupList.add(group);
                    }
                }
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }

    private void fetchUsersAndShowDialog() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<HelperClass> userList = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    HelperClass user = userSnapshot.getValue(HelperClass.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }

                if (!userList.isEmpty()) {
                    showUserSelectionDialog(userList);  // Only show dialog if the list is not empty
                } else {
                    Log.e("UserSelection", "User list is empty");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }

    private void showUserSelectionDialog(List<HelperClass> userList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_selection, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewUsers);
        Button finishButton = dialogView.findViewById(R.id.buttonFinish);

        UserSelectionAdapter adapter = new UserSelectionAdapter(userList);
        recyclerView.setAdapter(adapter);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        finishButton.setOnClickListener(v -> {
            List<HelperClass> selectedUsers = adapter.getSelectedUsers();
            addUsersToGroup(selectedUsers);
            dialog.dismiss();
        });

        dialog.show();
        adapter.notifyDataSetChanged();  // Ensure data is properly bound to the RecyclerView
    }


    private void addUsersToGroup(List<HelperClass> selectedUsers) {
        // Logic to add selected users to the group (e.g., save in Firebase)
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups").push();  // Save under 'groups'
        for (HelperClass user : selectedUsers) {
            groupRef.child("members").child(user.getUserId()).setValue(true);  // Add user to members
        }
        Toast.makeText(this, "Group created with selected users", Toast.LENGTH_SHORT).show();

        // Optionally, refresh the group list after adding users
        loadGroupsFromFirebase();
    }
}
