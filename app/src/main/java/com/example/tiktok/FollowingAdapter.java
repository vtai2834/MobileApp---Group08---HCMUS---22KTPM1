package com.example.tiktok;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.FollowingViewHolder> {

    private Context context;
    private List<User> followingList;

    public FollowingAdapter(Context context, List<User> followingList) {
        this.context = context;
        this.followingList = followingList;
    }

    @NonNull
    @Override
    public FollowingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_following, parent, false);
        return new FollowingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowingViewHolder holder, int position) {
        User following = followingList.get(position);

        holder.usernameText.setText(following.getName());
        holder.subtitleText.setText("Những người bạn có thể biết");
        holder.followDateText.setText("Đã theo dõi từ " + "1 tháng trước");  // Bạn có thể thay bằng thời gian thực

        Glide.with(context)
                .load(following.getAvatar())
                .into(holder.avatarImageView);  // Glide để load avatar
    }

    @Override
    public int getItemCount() {
        return followingList.size();
    }

    public static class FollowingViewHolder extends RecyclerView.ViewHolder {
        de.hdodenhof.circleimageview.CircleImageView avatarImageView;
        TextView usernameText, subtitleText, followDateText;

        public FollowingViewHolder(View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            usernameText = itemView.findViewById(R.id.usernameText);
            subtitleText = itemView.findViewById(R.id.subtitleText);
            followDateText = itemView.findViewById(R.id.followDateText);
        }
    }
}
