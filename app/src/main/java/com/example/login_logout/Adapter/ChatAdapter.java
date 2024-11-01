package com.example.login_logout.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.Image;
import android.net.Uri;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.login_logout.MessageClass;
import com.example.login_logout.R;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<MessageClass> messageClasses;
    private final Context context;
    private final String receiverId;
    private static final int SENDER_VIEW_TYPE = 1;
    private static final int RECEIVER_VIEW_TYPE = 2;
    private final RelativeLayout parentLayout;

    public ChatAdapter(ArrayList<MessageClass> messageClasses, Context context, String receiverId, RelativeLayout parentLayout) {
        this.messageClasses = messageClasses;
        this.context = context;
        this.receiverId = receiverId;
        this.parentLayout = parentLayout;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_view, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.reciever_view, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        String messageUserId = messageClasses.get(position).getUserId();
        return messageUserId.equals(receiverId) ? RECEIVER_VIEW_TYPE : SENDER_VIEW_TYPE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageClass messageClass = messageClasses.get(position);
        String time = formatTimestamp(messageClass.getTimestamp());

        if (holder instanceof SenderViewHolder) {
            bindSenderMessage((SenderViewHolder) holder, messageClass, time);
        } else if (holder instanceof ReceiverViewHolder) {
            bindReceiverMessage((ReceiverViewHolder) holder, messageClass, time);
        }
    }


    @Override
    public int getItemCount() {
        return messageClasses.size();
    }

    // Format timestamp to a readable time string
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
        return sdf.format(timestamp);
    }

    // Method to delete message for everyone
    private void deleteMessageForEveryone(MessageClass messageClass) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String senderRoom = messageClass.getSenderRoom();
        String receiverRoom = messageClass.getRecieverRoom();
        String senderMessageId = messageClass.getSenderMessageId();
        String recieverMessageId = messageClass.getRecieverMessageId();

        database.getReference().child("one-one-chats")
                .child(senderRoom).child(senderMessageId)
                .removeValue()
                .addOnSuccessListener(unused ->
                        database.getReference().child("one-one-chats").child(receiverRoom).child(recieverMessageId)
                                .removeValue()
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(context, "Message deleted for everyone", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed to delete for receiver", Toast.LENGTH_SHORT).show())
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to delete for sender", Toast.LENGTH_SHORT).show()
                );
    }

    private void deleteMessageForMe(MessageClass messageClass, String recipient) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String senderRoom = messageClass.getSenderRoom();
        String receiverRoom = messageClass.getRecieverRoom();
        String senderMessageId = messageClass.getSenderMessageId();
        String recieverMessageId = messageClass.getRecieverMessageId();

        if (recipient.equals("sender")) {
            database.getReference().child("one-one-chats")
                    .child(senderRoom).child(senderMessageId)
                    .removeValue()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Message deleted for you", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show());
        } else {
            database.getReference().child("one-one-chats")
                    .child(receiverRoom).child(recieverMessageId)
                    .removeValue()
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Message deleted for you", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show());
        }
    }

    private void bindSenderMessage(SenderViewHolder holder, MessageClass messageClass, String time) {
        //Alert box for deleting messages for both me and others
        holder.itemView.setOnLongClickListener(v -> {
            holder.senderDelete.setVisibility(View.VISIBLE);
            parentLayout.setOnClickListener(view -> {
                holder.senderDelete.setVisibility(View.GONE);
            });
            return true;
        });

        if (messageClass.getMessageStatus().equals("scheduled")) {
            holder.scheduledStatus.setVisibility(View.VISIBLE);
            holder.scheduledStatus.setText("To be delivered");
        } else {
            holder.scheduledStatus.setVisibility(View.GONE);
        }

        holder.senderDelete.setOnClickListener(v -> {
            // Create an AlertDialog with the options
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete Message")
                    .setItems(new String[]{"Delete for Me", "Delete for Everyone"}, (dialog, which) -> {
                        if (which == 0) {
                            deleteMessageForMe(messageClass, "sender");
                        }
                        else if (which == 1) {
                            // "Delete for Everyone" option selected
                            deleteMessageForEveryone(messageClass);
                        }
                    })
                    .show();
        });

        holder.senderTime.setText(time);
        String messageType = messageClass.getMessageType();


        // Reset all views to GONE
        holder.senderMsg.setVisibility(View.GONE);
        holder.senderImage.setVisibility(View.GONE);
        holder.senderVideo.setVisibility(View.GONE);
        holder.senderThumbnail.setVisibility(View.GONE);

        switch (messageType) {
            case "Text":
                holder.senderMsg.setVisibility(View.VISIBLE);
                holder.senderMsg.setText(messageClass.getMessage());
                break;

            case "Image":
                holder.senderImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(messageClass.getMediaUrl())
                        .error(R.drawable.error_image)
                        .into(holder.senderImage);
                break;

            case "Video":
                holder.senderVideo.setVisibility(View.VISIBLE);
                holder.senderThumbnail.setVisibility(View.VISIBLE);

                // Load thumbnail
                Glide.with(context)
                        .load(messageClass.getMediaUrl())  // Assuming it's the video URL; replace if you have a separate thumbnail
                        .error(R.drawable.error_image)
                        .into(holder.senderThumbnail);

                // Set up video URI and controller
                holder.senderVideo.setVideoURI(Uri.parse(messageClass.getMediaUrl()));
                MediaController mediaController = new MediaController(context);
                mediaController.setAnchorView(holder.senderVideo);
                holder.senderVideo.setMediaController(mediaController);

                holder.progressBar.setVisibility(View.VISIBLE);

                // Video prepared listener
                holder.senderVideo.setOnPreparedListener(mp -> {
                    mp.setLooping(true);  // Optional looping
                    holder.senderThumbnail.setVisibility(View.GONE);  // Hide thumbnail on start
                    holder.senderVideo.start();  // Start playing
                });

                // Error handling
                holder.senderVideo.setOnErrorListener((mp, what, extra) -> {
                    Toast.makeText(context, "Error loading video", Toast.LENGTH_SHORT).show();
                    holder.senderThumbnail.setVisibility(View.VISIBLE);  // Show thumbnail as fallback
                    return true;
                });

                // Toggle playback on thumbnail click
                holder.senderThumbnail.setOnClickListener(v -> {
                    holder.senderThumbnail.setVisibility(View.GONE);  // Hide thumbnail on play
                    holder.senderVideo.start();
                });

                // Toggle playback on video click
                holder.senderVideo.setOnClickListener(v -> {
                    if (holder.senderVideo.isPlaying()) {
                        holder.senderVideo.pause();
                        holder.senderThumbnail.setVisibility(View.VISIBLE);  // Show thumbnail when paused
                    } else {
                        holder.senderThumbnail.setVisibility(View.GONE);
                        holder.senderVideo.start();
                    }
                });
                break;
        }
    }

    private void bindReceiverMessage(ReceiverViewHolder holder, MessageClass messageClass, String time) {
        holder.receiverTime.setText(time);
        String messageType = messageClass.getMessageType();

        holder.itemView.setOnLongClickListener(v -> {
            holder.recieverDelete.setVisibility(View.VISIBLE);
            parentLayout.setOnClickListener(view -> {
                holder.recieverDelete.setVisibility(View.GONE);
            });
            return true;
        });

        holder.recieverDelete.setOnClickListener(v->{
            deleteMessageForMe(messageClass, "reciever");
        });

        // Reset all views to GONE
        holder.receiverMsg.setVisibility(View.GONE);
        holder.receiverImage.setVisibility(View.GONE);
        holder.receiverVideo.setVisibility(View.GONE);

        switch (messageType) {
            case "Text":
                holder.receiverMsg.setVisibility(View.VISIBLE);
                holder.receiverMsg.setText(messageClass.getMessage());
                break;

            case "Image":
                holder.receiverImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(messageClass.getMediaUrl())
                        .error(R.drawable.error_image)
                        .into(holder.receiverImage);
                break;

            case "Video":
                holder.receiverVideo.setVisibility(View.VISIBLE);
                holder.receiverThumbnail.setVisibility(View.VISIBLE);

                // Load thumbnail
                Glide.with(context)
                        .load(messageClass.getMediaUrl())  // Assuming it's the video URL; replace if you have a separate thumbnail
                        .error(R.drawable.error_image)
                        .into(holder.receiverThumbnail);

                // Set up video URI and controller
                holder.receiverVideo.setVideoURI(Uri.parse(messageClass.getMediaUrl()));
                MediaController mediaController = new MediaController(context);
                mediaController.setAnchorView(holder.receiverVideo);
                holder.receiverVideo.setMediaController(mediaController);

                holder.progressBar.setVisibility(View.VISIBLE);

                // Video prepared listener
                holder.receiverVideo.setOnPreparedListener(mp -> {
                    mp.setLooping(true);  // Optional looping
                    holder.receiverThumbnail.setVisibility(View.GONE);  // Hide thumbnail on start
                    holder.receiverVideo.start();  // Start playing
                });

                // Error handling
                holder.receiverVideo.setOnErrorListener((mp, what, extra) -> {
                    Toast.makeText(context, "Error loading video", Toast.LENGTH_SHORT).show();
                    holder.receiverThumbnail.setVisibility(View.VISIBLE);  // Show thumbnail as fallback
                    return true;
                });

                // Toggle playback on thumbnail click
                holder.receiverThumbnail.setOnClickListener(v -> {
                    holder.receiverThumbnail.setVisibility(View.GONE);  // Hide thumbnail on play
                    holder.receiverVideo.start();
                });

                // Toggle playback on video click
                holder.receiverVideo.setOnClickListener(v -> {
                    if (holder.receiverVideo.isPlaying()) {
                        holder.receiverVideo.pause();
                        holder.receiverThumbnail.setVisibility(View.VISIBLE);  // Show thumbnail when paused
                    } else {
                        holder.receiverThumbnail.setVisibility(View.GONE);
                        holder.receiverVideo.start();
                    }
                });
                break;
        }
    }

    // ViewHolder for receiver messages
    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receiverMsg, receiverTime;
        ImageView receiverImage, receiverThumbnail;
        VideoView receiverVideo;
        ProgressBar progressBar;
        ImageButton recieverDelete;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = itemView.findViewById(R.id.recieverText);
            receiverTime = itemView.findViewById(R.id.recieverTime);
            receiverImage = itemView.findViewById(R.id.recieverImage);
            receiverVideo = itemView.findViewById(R.id.recieverVideo);
            receiverThumbnail = itemView.findViewById(R.id.recieverVideoThumbnail);
            progressBar = itemView.findViewById(R.id.progressBar1);
            recieverDelete = itemView.findViewById(R.id.recieverDelete);
        }
    }

    // ViewHolder for sender messages
    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMsg, senderTime, scheduledStatus;
        ImageView senderImage, senderThumbnail;
        VideoView senderVideo;
        ProgressBar progressBar;
        ImageButton senderDelete;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            senderImage = itemView.findViewById(R.id.senderImage);
            senderVideo = itemView.findViewById(R.id.senderVideo);
            senderThumbnail = itemView.findViewById(R.id.senderVideoThumbnail);
            progressBar = itemView.findViewById(R.id.progressBar);
            senderDelete = itemView.findViewById(R.id.senderDelete);
            scheduledStatus = itemView.findViewById(R.id.scheduledStatus);

        }
    }
}

