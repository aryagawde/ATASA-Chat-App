package com.example.login_logout.Adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.login_logout.MessageClass;
import com.example.login_logout.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<MessageClass> messageClasses;
    private final Context context;
    private final String receiverId;

    private static final int SENDER_VIEW_TYPE = 1;
    private static final int RECEIVER_VIEW_TYPE = 2;

    public ChatAdapter(ArrayList<MessageClass> messageClasses, Context context, String receiverId) {
        this.messageClasses = messageClasses;
        this.context = context;
        this.receiverId = receiverId;
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
        // Check if message sender is the receiver or the current user (assumed as sender)
        return messageUserId.equals(receiverId) ? RECEIVER_VIEW_TYPE : SENDER_VIEW_TYPE;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageClass messageClass = messageClasses.get(position);
        String time = formatTimestamp(messageClass.getTimestamp());

        if (holder instanceof SenderViewHolder){
            if(((SenderViewHolder) holder).senderMsg.toString() != ""){
                ((SenderViewHolder) holder).senderMsg.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).senderMsg.setText(messageClass.getMessage());
            }
            ((SenderViewHolder) holder).senderTime.setText(time);

            String mediaUrl = messageClass.getMediaUrl();
            if (mediaUrl != null && !mediaUrl.isEmpty()) {
                if (mediaUrl.endsWith(".mp4")) {
                    // Display video, hide image
                    ((SenderViewHolder) holder).senderImage.setVisibility(View.GONE);
                    ((SenderViewHolder) holder).senderVideo.setVisibility(View.VISIBLE);
                    ((SenderViewHolder) holder).senderVideo.setVideoURI(Uri.parse(mediaUrl));
                    ((SenderViewHolder) holder).senderVideo.setOnPreparedListener(MediaPlayer::start);
                } else {
                    // Display image, hide video
                    ((SenderViewHolder) holder).senderVideo.setVisibility(View.GONE);
                    ((SenderViewHolder) holder).senderImage.setVisibility(View.VISIBLE);
                    Glide.with(context).load(mediaUrl).into(((SenderViewHolder) holder).senderImage);
                }
            } else {
                // No media, hide both ImageView and VideoView
                ((SenderViewHolder) holder).senderImage.setVisibility(View.GONE);
                ((SenderViewHolder) holder).senderVideo.setVisibility(View.GONE);
            }
        }
        if (holder instanceof ReceiverViewHolder){
            if(((ReceiverViewHolder) holder).receiverMsg.toString() != ""){
                ((ReceiverViewHolder) holder).receiverMsg.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder) holder).receiverMsg.setText(messageClass.getMessage());
            }
            ((ReceiverViewHolder) holder).receiverTime.setText(time);

            String mediaUrl = messageClass.getMediaUrl();
            if (mediaUrl != null && !mediaUrl.isEmpty()) {
                if (mediaUrl.endsWith(".mp4")) {
                    // Display video, hide image
                    ((ReceiverViewHolder) holder).receiverImage.setVisibility(View.GONE);
                    ((ReceiverViewHolder) holder).receiverImage.setVisibility(View.VISIBLE);
                    ((ReceiverViewHolder) holder).receiverVideo.setVideoURI(Uri.parse(mediaUrl));
                    ((ReceiverViewHolder) holder).receiverVideo.setOnPreparedListener(MediaPlayer::start);
                } else {
                    // Display image, hide video
                    ((ReceiverViewHolder) holder).receiverVideo.setVisibility(View.GONE);
                    ((ReceiverViewHolder) holder).receiverImage.setVisibility(View.VISIBLE);
                    Glide.with(context).load(mediaUrl).into(((ReceiverViewHolder) holder).receiverImage);
                }
            } else {
                // No media, hide both ImageView and VideoView
                ((ReceiverViewHolder) holder).receiverImage.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiverVideo.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageClasses.size();
    }

    // Format timestamp to a readable time string
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(timestamp);
    }

    // ViewHolder for receiver messages
    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receiverMsg, receiverTime;
        ImageView receiverImage;
        VideoView receiverVideo;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.receiverTime);
            receiverImage = itemView.findViewById(R.id.receiverImage);
            receiverVideo = itemView.findViewById(R.id.receiverVideo);
        }
    }

    // ViewHolder for sender messages
    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMsg, senderTime;
        ImageView senderImage;
        VideoView senderVideo;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            senderImage = itemView.findViewById(R.id.senderImage);
            senderVideo = itemView.findViewById(R.id.senderVideo);
        }
    }
}
