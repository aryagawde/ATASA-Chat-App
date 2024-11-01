package com.example.login_logout;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_logout.Adapter.ChatAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatDetailActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_IMAGE = 101;
    private static final int REQUEST_CODE_VIDEO = 102;
    private static final int REQUEST_PERMISSION_CODE = 123;
    TextView userfield, status, messageInput;
    FirebaseDatabase database;
    Button back;
    ImageButton send, attach_media;
    RecyclerView chatRecyclerView;
    RelativeLayout chat_parent_layout;

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
        chat_parent_layout = findViewById(R.id.chat_parent_layout);

        attach_media = findViewById(R.id.button_attach);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }

        attach_media.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Media Type")
                    .setItems(new String[]{"Image", "Video"}, (dialog, which) -> {
                        if (which == 0) {
                            // User selected Image
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_CODE_IMAGE);
                        } else if (which == 1) {
                            // User selected Video
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("video/*");
                            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_CODE_VIDEO);
                        }
                    });
            builder.show();
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

        if (receiver_id == null){
            return;
        }

        database = FirebaseDatabase.getInstance();

        // Set up RecyclerView and Adapter
        ArrayList<MessageClass> messageClasses = new ArrayList<>();
        ChatAdapter chatAdapter = new ChatAdapter(messageClasses, this, receiver_id, chat_parent_layout);
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
            messageInput.setText(""); // Clear input

            DatabaseReference senderMsgRef = database.getReference().child("one-one-chats").child(senderRoom).push();
            String senderMessageId = senderMsgRef.getKey();

            DatabaseReference recieverMessageRef = database.getReference().child("one-one-chats") .child(receiverRoom).push();
            String recieverMessageId = recieverMessageRef.getKey();

            final MessageClass model = new MessageClass(sender_id, message, "Text", "", senderRoom, receiverRoom, senderMessageId, recieverMessageId);
            model.setTimestamp(new Date().getTime());
            senderMsgRef.setValue(model)
                    .addOnSuccessListener(unused -> recieverMessageRef.setValue(model));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedMediaUri = data.getData();
            if (selectedMediaUri != null) {
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String receiver_id = sharedPreferences.getString("userId", null);
                if (receiver_id != null) {
                    if (requestCode == REQUEST_CODE_IMAGE) {
                        uploadMediaToFirebase(selectedMediaUri, receiver_id, "Image");
                    } else if (requestCode == REQUEST_CODE_VIDEO) {
                        uploadMediaToFirebase(selectedMediaUri, receiver_id, "Video");
                    }
                } else {
                    Toast.makeText(this, "Receiver ID not found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void uploadMediaToFirebase(Uri mediaUri, String receiver_id, String mediaType) {
        String senderId = getIntent().getStringExtra("userId");
        String fileName = mediaType + "/" + senderId + "_" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileName);

        storageReference.putFile(mediaUri).addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String mediaUrl = uri.toString();
                sendMessageWithMedia(mediaUrl, receiver_id, mediaType);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload" + mediaType, Toast.LENGTH_SHORT).show();
        });
    }


    private void sendMessageWithMedia(String mediaUrl, String receiver_id, String mediaType) {
        String senderId = getIntent().getStringExtra("userId");
        String senderRoom = senderId + receiver_id;
        String receiverRoom = receiver_id + senderId;

        DatabaseReference senderMsgRef = database.getReference().child("one-one-chats").child(senderRoom).push();
        String senderMessageId = senderMsgRef.getKey();
        DatabaseReference recieverMsgRef = database.getReference().child("one-one-chats").child(receiverRoom).push();
        String recieverMessageId = recieverMsgRef.getKey();

        // Create a message with a media URL
        MessageClass mediaMessage = new MessageClass(senderId, mediaType + " is attached", mediaType, mediaUrl, senderRoom, receiverRoom, senderMessageId, recieverMessageId);
        mediaMessage.setTimestamp(new Date().getTime());

        // Send to both sender and receiver rooms
        senderMsgRef.setValue(mediaMessage).addOnSuccessListener(unused -> recieverMsgRef.setValue(mediaMessage));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied, handle appropriately
                Toast.makeText(this, "Permission denied to read external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
