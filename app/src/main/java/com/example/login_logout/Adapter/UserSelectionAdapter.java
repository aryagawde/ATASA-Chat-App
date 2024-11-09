package com.example.login_logout.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.login_logout.HelperClass;
import com.example.login_logout.R;
import java.util.ArrayList;
import java.util.List;

public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.ViewHolder> {
    private List<HelperClass> userList;
    private List<HelperClass> selectedUsers = new ArrayList<>();

    public UserSelectionAdapter(List<HelperClass> userList) {
        this.userList = userList;
    }

    public List<HelperClass> getSelectedUsers() {
        return selectedUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HelperClass user = userList.get(position);
        holder.checkBox.setText(user.getUsername());

        // Set the checkbox state based on whether the user is selected
        holder.checkBox.setChecked(selectedUsers.contains(user));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Add to selected users if checked
                if (!selectedUsers.contains(user)) {
                    selectedUsers.add(user);
                }
            } else {
                // Remove from selected users if unchecked
                selectedUsers.remove(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkboxUser);
        }
    }
}
