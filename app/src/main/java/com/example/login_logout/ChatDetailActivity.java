package com.example.login_logout;

import android.app.AlertDialog;

//import com.google.mlkit.nl.languageid.LanguageIdentification;
//import com.google.mlkit.nl.languageid.LanguageIdentifier;
//import com.google.cloud.translate.Translate;
//import com.google.cloud.translate.TranslateOptions;
//import com.google.cloud.translate.Translation;

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

        // Sender Information
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String sender_id = sharedPreferences.getString("userId", null);
        String sender_username = sharedPreferences.getString("username", null);

        // Receiver Information
        String receiverUsername = getIntent().getStringExtra("username");
        String isOnline = getIntent().getStringExtra("status");
        String receiver_id = getIntent().getStringExtra("userId");

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
            intent.putExtra("currentUser", sender_username);
            startActivity(intent);
            finish();
        });

        userfield.setText(receiverUsername);
        if (isOnline.equals("false")) {status.setText("Offline"); }
        else {status.setText("Online");}

        if (receiver_id == null){return;}

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
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Send Message
        send.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            messageInput.setText(""); // Clear input

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Send Schedule")
                    .setItems(new String[]{"Now", "Later"}, (dialog, which) -> {
                        if (which == 0) {
                            SendNow(senderRoom, receiverRoom, sender_id, message);
                        } else if (which == 1) {
                            SendLater(senderRoom, receiverRoom, sender_id, message, chatAdapter);
                        }
                    });
            builder.show();
        });
    }

    private void SendNow(String senderRoom, String receiverRoom, String sender_id, String message){
        DatabaseReference senderMsgRef = database.getReference().child("one-one-chats").child(senderRoom).push();
        String senderMessageId = senderMsgRef.getKey();
        DatabaseReference recieverMessageRef = database.getReference().child("one-one-chats") .child(receiverRoom).push();
        String recieverMessageId = recieverMessageRef.getKey();

        long timestamp = new Date().getTime();
        final MessageClass model = new MessageClass(sender_id, message, "Text", "", senderRoom, receiverRoom, senderMessageId, recieverMessageId, "Sent", timestamp);
        senderMsgRef.setValue(model)
                .addOnSuccessListener(unused -> recieverMessageRef.setValue(model));
    }
    private void SendLater(String senderRoom, String receiverRoom, String sender_id, String message, ChatAdapter chatAdapter){
        Calendar currentTime = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(year, month, dayOfMonth, hourOfDay, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm a", Locale.getDefault());
                String scheduledDateTime = sdf.format(selectedTime.getTime()); // use selectedTime here
                Toast.makeText(this, "Message Scheduled for: " + scheduledDateTime, Toast.LENGTH_SHORT).show();
                scheduleMessage(selectedTime.getTimeInMillis(), message, senderRoom, receiverRoom, sender_id, chatAdapter);
            }, currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }, currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


    private void scheduleMessage(long scheduledTime, String message, String senderRoom, String recieverRoom, String sender_id, ChatAdapter chatAdapter){
        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("one-one-chats").child(senderRoom).push();
        String senderMessageId = senderRef.getKey();

        //No reciever id because if user deletes the message before it goes to reciever then it is deleted for everyone
        long timestamp = new Date().getTime();
        final MessageClass model = new MessageClass(sender_id, message, "Text", "", senderRoom, recieverRoom, senderMessageId, "", "Scheduled", scheduledTime);
        senderRef.setValue(model).addOnSuccessListener(unused -> {
                    startMessageScheduler(scheduledTime, model, senderRoom, recieverRoom, chatAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to schedule message", Toast.LENGTH_SHORT).show()
                );
    }

    // Method to start the delayed task for message delivery
    private void startMessageScheduler(long scheduledTimestamp, MessageClass message, String senderRoom, String receiverRoom, ChatAdapter chatAdapter) {
        long delay = scheduledTimestamp - System.currentTimeMillis();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            sendMessageToReceiverRoom(message, senderRoom, receiverRoom, chatAdapter);
        }, delay);
    }

    // Method to move the message to the receiver's room and update status
    private void sendMessageToReceiverRoom(MessageClass message, String senderRoom, String receiverRoom, ChatAdapter chatAdapter) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference senderRoomRef = database.getReference("one-one-chats").child(senderRoom);
        DatabaseReference receiverRoomRef = database.getReference("one-one-chats").child(receiverRoom).push();

        senderRoomRef.child(message.getSenderMessageId()).child("messageStatus").setValue("Sent").addOnSuccessListener(unused -> {
            message.setMessageStatus("Sent");

            // Find the position of the message in the adapter
            int index = chatAdapter.getMessageIndex(message);
            if (index != -1) {
                chatAdapter.notifyItemChanged(index);
            }
        });

        String receiverMessageId = receiverRoomRef.getKey();
        message.setRecieverMessageId(receiverMessageId);
        message.setMessageStatus("Sent");

        receiverRoomRef.setValue(message)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Message delivered to receiver", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to deliver message", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String sender_id = sharedPreferences.getString("userId", null);
        String receiver_id = getIntent().getStringExtra("userId");

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedMediaUri = data.getData();
            if (selectedMediaUri != null) {
                if (receiver_id != null) {
                    if (requestCode == REQUEST_CODE_IMAGE) {
                        uploadMediaToFirebase(selectedMediaUri, receiver_id, "Image", sender_id);
                    } else if (requestCode == REQUEST_CODE_VIDEO) {
                        uploadMediaToFirebase(selectedMediaUri, receiver_id, "Video", sender_id);
                    }
                } else {
                    Toast.makeText(this, "Receiver ID not found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void uploadMediaToFirebase(Uri mediaUri, String receiver_id, String mediaType, String senderId) {
        String fileName = mediaType + "/" + senderId + "_" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileName);

        storageReference.putFile(mediaUri).addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String mediaUrl = uri.toString();
                sendMessageWithMedia(mediaUrl, receiver_id, mediaType, senderId);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload" + mediaType, Toast.LENGTH_SHORT).show();
        });
    }


    private void sendMessageWithMedia(String mediaUrl, String receiver_id, String mediaType, String senderId) {
        String senderRoom = senderId + receiver_id;
        String receiverRoom = receiver_id + senderId;


        DatabaseReference senderMsgRef = database.getReference().child("one-one-chats").child(senderRoom).push();
        String senderMessageId = senderMsgRef.getKey();
        DatabaseReference recieverMsgRef = database.getReference().child("one-one-chats").child(receiverRoom).push();
        String recieverMessageId = recieverMsgRef.getKey();

        // Create a message with a media URL
        long timestamp = new Date().getTime();
        MessageClass mediaMessage = new MessageClass(senderId, mediaType + " is attached", mediaType, mediaUrl, senderRoom, receiverRoom, senderMessageId, recieverMessageId, "Sent", timestamp);

        // Send to both sender and receiver rooms
        senderMsgRef.setValue(mediaMessage).addOnSuccessListener(unused -> recieverMsgRef.setValue(mediaMessage));
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
//            } else {
//                // Permission denied, handle appropriately
//                Toast.makeText(this, "Permission denied to read external storage", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
}