package com.example.login_logout.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_logout.GroupChats;
import com.example.login_logout.GroupClass;
import com.example.login_logout.R;

import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {
    private List<GroupClass> groupList;
    private Context context;

    public GroupListAdapter(Context context, List<GroupClass> groupList){
        this.context = context;
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupClass group = groupList.get(position);
        holder.groupName.setText(group.getName());
        holder.memberCount.setText("Members: " + group.getMembers().size());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GroupChats.class);
            intent.putExtra("groupId", group.getGroupId());
            intent.putExtra("groupName", group.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupName, memberCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupName);
            memberCount = itemView.findViewById(R.id.memberCount);
        }
    }
}
