package com.example.login_logout.Adapter;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.login_logout.MessageClass;
import com.example.login_logout.R;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
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
        int layoutId = (viewType == SENDER_VIEW_TYPE) ? R.layout.sender_view : R.layout.reciever_view;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return viewType == SENDER_VIEW_TYPE ? new SenderViewHolder(view) : new ReceiverViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return messageClasses.get(position).getUserId().equals(receiverId) ? RECEIVER_VIEW_TYPE : SENDER_VIEW_TYPE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageClass message = messageClasses.get(position);
        String time = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(message.getTimestamp());

        if (holder instanceof SenderViewHolder) {
            bindMessage((SenderViewHolder) holder, message, time, true);
        } else {
            bindMessage((ReceiverViewHolder) holder, message, time, false);
        }
    }

    @Override
    public int getItemCount() {
        return messageClasses.size();
    }

    private void bindMessage(MessageViewHolder holder, MessageClass message, String time, boolean isSender) {
        holder.messageTime.setText(time);
        holder.downloadButton.setVisibility(View.GONE);
        holder.deleteButton.setOnClickListener(v -> showDeleteOptions(message, isSender));

        // Common for Text, Image, and Video types
        holder.textMessage.setVisibility(View.GONE);
        holder.imageView.setVisibility(View.GONE);
        holder.videoView.setVisibility(View.GONE);
        holder.thumbnailView.setVisibility(View.GONE);
        holder.progressBar.setVisibility(View.GONE);

        switch (message.getMessageType()) {
            case "Text":
                holder.textMessage.setVisibility(View.VISIBLE);
                holder.textMessage.setText(message.getMessage());
                break;
            case "Image":
                holder.imageView.setVisibility(View.VISIBLE);
                holder.downloadButton.setVisibility(View.VISIBLE);
                Glide.with(context).load(message.getMediaUrl()).error(R.drawable.error_image).into(holder.imageView);
                holder.downloadButton.setOnClickListener(v -> downloadMedia(context, message.getMediaUrl(), message.getMessageType()));
                break;
            case "Video":
                setupVideo(holder, message.getMediaUrl());
                holder.downloadButton.setVisibility(View.VISIBLE);
                holder.downloadButton.setOnClickListener(v -> downloadMedia(context, message.getMediaUrl(), message.getMessageType()));
                break;
        }


    }

    private void showDeleteOptions(MessageClass message, boolean isSender) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Message")
                .setItems(new String[]{"Delete for Me", "Delete for Everyone"}, (dialog, which) -> {
                    if (which == 0) {
                        deleteMessage(message, isSender ? "sender" : "receiver");
                    } else {
                        deleteMessageForEveryone(message);
                    }
                }).show();
    }

    private void deleteMessage(MessageClass message, String recipient) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String room = recipient.equals("sender") ? message.getSenderRoom() : message.getRecieverRoom();
        String messageId = recipient.equals("sender") ? message.getSenderMessageId() : message.getRecieverMessageId();

        database.getReference().child("one-one-chats").child(room).child(messageId).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show());
    }

    private void deleteMessageForEveryone(MessageClass message) {
        deleteMessage(message, "sender");
        deleteMessage(message, "receiver");
    }

    private void setupVideo(MessageViewHolder holder, String videoUrl) {
        // Show VideoView and Thumbnail initially
        holder.videoView.setVisibility(View.VISIBLE);
        holder.thumbnailView.setVisibility(View.VISIBLE);

        // Load thumbnail using Glide
        Glide.with(context)
                .load(videoUrl)
                .error(R.drawable.error_image)
                .into(holder.thumbnailView);

        // Set video URI for playback
        holder.videoView.setVideoURI(Uri.parse(videoUrl));

        // Set OnPreparedListener to handle video playback
        holder.videoView.setOnPreparedListener(mp -> {
            // Set video to loop when it finishes
            mp.setLooping(true);
            // Hide thumbnail and start playing video
            holder.thumbnailView.setVisibility(View.GONE);
            holder.videoView.start();
        });

        // Set OnErrorListener to handle video loading errors
        holder.videoView.setOnErrorListener((mp, what, extra) -> {
            // Show error message and display the thumbnail again if the video fails to load
            Toast.makeText(context, "Error loading video", Toast.LENGTH_SHORT).show();
            holder.thumbnailView.setVisibility(View.VISIBLE);
            return true;
        });

        // Toggle video play/pause when the thumbnail is clicked
        holder.thumbnailView.setOnClickListener(v -> toggleVideo(holder));

        // Toggle video play/pause when the video itself is clicked
        holder.videoView.setOnClickListener(v -> toggleVideo(holder));
    }

    private void toggleVideo(MessageViewHolder holder) {
        // Check if video is already playing
        if (holder.videoView.isPlaying()) {
            // Pause video and show thumbnail again
            holder.videoView.pause();
            holder.thumbnailView.setVisibility(View.VISIBLE);
        } else {
            // Start video playback and hide thumbnail
            holder.thumbnailView.setVisibility(View.GONE);
            holder.videoView.start();
        }
    }

    private void downloadMedia(Context context, String mediaUrl, String mediaType) {
        new DownloadMediaTask(context, mediaUrl, mediaType).execute();
    }

    private static class DownloadMediaTask extends AsyncTask<Void, Void, Boolean> {
        private final Context context;
        private final String mediaUrl;
        private final String mediaType;

        public DownloadMediaTask(Context context, String mediaUrl, String mediaType) {
            this.context = context;
            this.mediaUrl = mediaUrl;
            this.mediaType = mediaType;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try (InputStream input = new URL(mediaUrl).openStream();
                 OutputStream output = getOutputStream(context, mediaType)) {
                if (output == null) return false;
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Toast.makeText(context, success ? "Downloaded to Gallery" : "Download failed", Toast.LENGTH_SHORT).show();
        }

        private OutputStream getOutputStream(Context context, String mediaType) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "Media_" + System.currentTimeMillis());
            values.put(MediaStore.MediaColumns.MIME_TYPE, mediaType.equals("Image") ? "image/jpeg" : "video/mp4");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, mediaType.equals("Image") ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_MOVIES);

            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(mediaType.equals("Image") ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI : MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            try {
                return resolver.openOutputStream(uri);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class SenderViewHolder extends MessageViewHolder {
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView, R.id.senderText, R.id.senderTime, R.id.senderImage, R.id.senderVideo, R.id.senderVideoThumbnail, R.id.progressBar, R.id.senderDelete, R.id.senderDownload);
        }
    }

    public static class ReceiverViewHolder extends MessageViewHolder {
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView, R.id.recieverText, R.id.recieverTime, R.id.recieverImage, R.id.recieverVideo, R.id.recieverVideoThumbnail, R.id.progressBar1, R.id.recieverDelete, R.id.recieverDownload);
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, messageTime;
        ImageView imageView, thumbnailView;
        VideoView videoView;
        ProgressBar progressBar;
        ImageButton deleteButton, downloadButton;

        public MessageViewHolder(View itemView, int textMessageId, int timeId, int imageViewId, int videoViewId, int thumbnailId, int progressBarId, int deleteButtonId, int downloadButtonId) {
            super(itemView);
            textMessage = itemView.findViewById(textMessageId);
            messageTime = itemView.findViewById(timeId);
            imageView = itemView.findViewById(imageViewId);
            videoView = itemView.findViewById(videoViewId);
            thumbnailView = itemView.findViewById(thumbnailId);
            progressBar = itemView.findViewById(progressBarId);
            deleteButton = itemView.findViewById(deleteButtonId);
            downloadButton = itemView.findViewById(downloadButtonId);
        }
    }
}