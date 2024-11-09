package com.example.login_logout;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_logout.Adapter.ChatAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.google.firebase.database.FirebaseDatabase;

public class GroupChatActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_IMAGE = 101;
    private static final int REQUEST_CODE_VIDEO = 102;
    private static final int REQUEST_PERMISSION_CODE = 123;
    TextView groupNameField, messageInput;
    FirebaseDatabase database;
    Button back;
    ImageButton send, attachMedia;
    RecyclerView groupChatRecyclerView;
    RelativeLayout groupChatParentLayout;
    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        getSupportActionBar().hide();

        // Initialize views
        back = findViewById(R.id.back_button2);
        send = findViewById(R.id.button_send1);
        messageInput = findViewById(R.id.message_input1);
        groupNameField = findViewById(R.id.group_name);
        groupChatRecyclerView = findViewById(R.id.group_chats_recycler);
        groupChatParentLayout = findViewById(R.id.group_chat_parent_layout);
        attachMedia = findViewById(R.id.button_attach1);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }

        // Sender Information
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String senderId = sharedPreferences.getString("userId", null);
        String senderUsername = sharedPreferences.getString("username", null);

        // Group Information
        groupId = getIntent().getStringExtra("groupId");
        String groupName = getIntent().getStringExtra("groupName");
        groupNameField.setText(groupName);

        database = FirebaseDatabase.getInstance();

        // Set up RecyclerView and Adapter
        ArrayList<MessageClass> messageClasses = new ArrayList<>();
        ChatAdapter chatAdapter = new ChatAdapter(messageClasses, this, groupId, groupChatParentLayout);
        groupChatRecyclerView.setAdapter(chatAdapter);
        groupChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch and display group messages
        database.getReference().child("group-chats").child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageClasses.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessageClass model = snapshot1.getValue(MessageClass.class);
                            if (model != null) {
                                messageClasses.add(model);
                            }
                        }
                        chatAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Send Message
        send.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            messageInput.setText(""); // Clear input
            if (!message.isEmpty()) {
                sendGroupMessage(senderId, message, senderUsername);
            }
        });

        // Attach Media
        attachMedia.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Media Type")
                    .setItems(new String[]{"Image", "Video"}, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_CODE_IMAGE);
                        } else if (which == 1) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("video/*");
                            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_CODE_VIDEO);
                        }
                    });
            builder.show();
        });

        // Back button to go to the previous screen
        back.setOnClickListener(view -> finish());
    }

    // Send a message to the group
    private void sendGroupMessage(String senderId, String message, String senderName) {
        DatabaseReference groupMsgRef = database.getReference().child("group-chats").child(groupId).push();
        String messageId = groupMsgRef.getKey();
        long timestamp = new Date().getTime();
        MessageClass model = new MessageClass(senderId, message, "Text", "", groupId, null, messageId, "", "Sent", timestamp, senderName);
        groupMsgRef.setValue(model);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedMediaUri = data.getData();
            if (selectedMediaUri != null) {
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String senderId = sharedPreferences.getString("userId", null);
                String senderUsername = sharedPreferences.getString("username", null);

                if (requestCode == REQUEST_CODE_IMAGE) {
                    uploadMediaToFirebase(selectedMediaUri, "Image", senderId, senderUsername);
                } else if (requestCode == REQUEST_CODE_VIDEO) {
                    uploadMediaToFirebase(selectedMediaUri, "Video", senderId, senderUsername);
                }
            }
        }
    }

    private void uploadMediaToFirebase(Uri mediaUri, String mediaType, String senderId, String senderName) {
        String fileName = mediaType + "/" + senderId + "_" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileName);

        storageReference.putFile(mediaUri).addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String mediaUrl = uri.toString();
                sendGroupMessageWithMedia(mediaUrl, mediaType, senderId, senderName);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload " + mediaType, Toast.LENGTH_SHORT).show();
        });
    }

    private void sendGroupMessageWithMedia(String mediaUrl, String mediaType, String senderId, String senderName) {
        DatabaseReference groupMsgRef = database.getReference().child("group-chats").child(groupId).push();
        String messageId = groupMsgRef.getKey();
        long timestamp = new Date().getTime();
        MessageClass mediaMessage = new MessageClass(senderId, mediaType + " is attached", mediaType, mediaUrl, groupId, null, messageId, "", "Sent", timestamp, senderName);
        groupMsgRef.setValue(mediaMessage);
    }
}
