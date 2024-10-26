package com.example.login_logout.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_logout.ChatDetailActivity;
import com.example.login_logout.HelperClass;
import com.example.login_logout.R;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{
    ArrayList<HelperClass> list;
    Context context;

    public UserAdapter(Context context, ArrayList<HelperClass> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.individual_user,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        HelperClass helperClass = list.get(position);
        holder.userName.setText(helperClass.getUsername());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("username", helperClass.getUsername());
                intent.putExtra("isLoggedIn",String.valueOf(helperClass.getLoggedin()));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView userName, lastMessage;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            userName = itemView.findViewById(R.id.nameOfUser);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }
}