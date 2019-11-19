package com.taehoon.videoplayerview.ExoPlayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.taehoon.videoplayerview.R;

public class PlaySpeedAdapter extends RecyclerView.Adapter {

    private final float[] PLAY_SPEEDS = {0.5f, 1.0f, 1.5f, 2.0f};
    private float mCurrSpeed;
    private View.OnClickListener mListener;

    PlaySpeedAdapter(float currSpeed, @NonNull View.OnClickListener listener) {
        this.mCurrSpeed = currSpeed;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_play_speed, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        if (PLAY_SPEEDS[position] == mCurrSpeed) {
            itemHolder.ivCheck.setVisibility(View.VISIBLE);
        }
        itemHolder.tvSpeed.setText(String.valueOf(PLAY_SPEEDS[position]));
        itemHolder.clContainer.setOnClickListener(mListener);
        itemHolder.clContainer.setTag(PLAY_SPEEDS[position]);
    }

    @Override
    public int getItemCount() {
        return PLAY_SPEEDS.length;
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        ConstraintLayout clContainer;
        TextView tvSpeed;
        ImageView ivCheck;

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            clContainer = itemView.findViewById(R.id.cl_item_play_speed_container);
            tvSpeed = itemView.findViewById(R.id.tv_item_play_speed);
            ivCheck = itemView.findViewById(R.id.iv_check);
        }
    }
}
