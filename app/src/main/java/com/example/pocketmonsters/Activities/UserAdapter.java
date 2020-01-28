package com.example.pocketmonsters.Activities;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pocketmonsters.Utilis.ImageUtilities;
import com.example.pocketmonsters.R;
import com.example.pocketmonsters.Model.User;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;
    private Context context;

    static class UserViewHolder extends RecyclerView.ViewHolder {

        private ImageView userImageView;
        private TextView usernameTextView;
        private TextView userLpTextView;
        private TextView userXpTextView;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);

            userImageView = itemView.findViewById(R.id.img_user);
            usernameTextView = itemView.findViewById(R.id.txt_user_name);
            userLpTextView = itemView.findViewById(R.id.txt_user_lp);
            userXpTextView = itemView.findViewById(R.id.txt_user_xp);

        }
    }

    UserAdapter(List<User> userList) {
        users = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.card_user_ranking, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        User currentUser = users.get(position);
        holder.userLpTextView.setText(context.getString(R.string.life_points, currentUser.getLifePoints()));
        holder.userXpTextView.setText(context.getString(R.string.exp_points, currentUser.getExpPoints()));

        if (currentUser.getUsername() != null) {
            holder.usernameTextView.setText(currentUser.getUsername());
        } else {
            holder.usernameTextView.setText(R.string.user_name_anonym);
            holder.usernameTextView.setTypeface(null, Typeface.ITALIC);
        }

        if (currentUser.getBase64Image() != null && currentUser.getBase64Image().length() > 0)
            holder.userImageView.setImageBitmap(ImageUtilities.getBitmapFromString(currentUser.getBase64Image()));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}

