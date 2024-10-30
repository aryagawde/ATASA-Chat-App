package com.example.login_logout;

import static android.app.PendingIntent.getActivity;

import android.media.MediaPlayer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
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

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {
    TextView userfield, status, messageInput;
    FirebaseDatabase database;
    Button back;
    ImageButton send, attach_media;
    RecyclerView chatRecyclerView;
    private static final int REQUEST_CODE_IMAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        getSupportActionBar().hide();

        // Initialize views
        back = findViewById(R.id.back_button);
        send = findViewById(R.id.button_send);
        messageInput = findViewById(R.id.message_input);
        userfield = findViewById(R.id.user_name);
        status = findViewById(R.id.status);
        chatRecyclerView = findViewById(R.id.chats_recycler);
        attach_media = findViewById(R.id.button_attach);

        attach_media.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*, video/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Select Picture or Video"), REQUEST_CODE_IMAGE);
        });

        // Set up back button
        back.setOnClickListener(view -> {
            Intent intent = new Intent(ChatDetailActivity.this, RecentChats.class);
            startActivity(intent);
            finish();
        });

        // Sender Information
        String sender_username = getIntent().getStringExtra("username");
        String isOnline = getIntent().getStringExtra("isLoggedIn");
        String sender_id = getIntent().getStringExtra("userId");

        userfield.setText(sender_username);
        if (isOnline.equals("false")) { status.setText("Offline"); }
        else { status.setText("Online"); }

        // Receiver Information
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String receiver_id = sharedPreferences.getString("userId", null);

        if (receiver_id == null) {
            // Handle case where receiver info is missing
            return;
        }

        database = FirebaseDatabase.getInstance();

        // Set up RecyclerView and Adapter
        ArrayList<MessageClass> messageClasses = new ArrayList<>();
        ChatAdapter chatAdapter = new ChatAdapter(messageClasses, this, receiver_id);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Chat Room Identifiers
        String senderRoom = sender_id + receiver_id;
        String receiverRoom = receiver_id + sender_id;

        // Fetch and display messages
        database.getReference().child("one-one-chats").child(senderRoom)
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
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error if needed
                    }
                });

        // Send Message
        send.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            final MessageClass model = new MessageClass(sender_id, message, "");
            model.setTimestamp(new Date().getTime());
            messageInput.setText(""); // Clear input

            database.getReference().child("one-one-chats").child(senderRoom)
                    .push().setValue(model)
                    .addOnSuccessListener(unused -> database.getReference().child("one-one-chats")
                            .child(receiverRoom).push().setValue(model));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String receiver_id = sharedPreferences.getString("userId", null);
                if (receiver_id != null) {
                    uploadMediaToFirebase(selectedImageUri, receiver_id);
                } else {
                    Toast.makeText(this, "Receiver ID not found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadMediaToFirebase(Uri mediaUri, String receiver_id) {
        String senderId = getIntent().getStringExtra("userId");
        String fileName = "media/" + senderId + "_" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileName);

        storageReference.putFile(mediaUri).addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String mediaUrl = uri.toString();
                sendMessageWithMedia(mediaUrl, receiver_id);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload media", Toast.LENGTH_SHORT).show();
        });
    }


    private void sendMessageWithMedia(String mediaUrl, String receiver_id) {
        String senderId = getIntent().getStringExtra("userId");
        String senderRoom = senderId + receiver_id;
        String receiverRoom = receiver_id + senderId;

        // Create a message with a media URL
        MessageClass mediaMessage = new MessageClass(senderId, "Image", mediaUrl);
        mediaMessage.setTimestamp(new Date().getTime());

        // Send to both sender and receiver rooms
        database.getReference().child("one-one-chats").child(senderRoom).push()
                .setValue(mediaMessage)
                .addOnSuccessListener(unused -> database.getReference()
                        .child("one-one-chats").child(receiverRoom).push().setValue(mediaMessage));
    }

}
