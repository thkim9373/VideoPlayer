package com.taehoon.videoplayerview.ExoPlayer;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.taehoon.videoplayerview.R;

public class PlaySpeedBottomSheetDialog extends BottomSheetDialog {

    private Context mContext;
    private float mCurrPlaySpeed;
    private View.OnClickListener mListener;

    PlaySpeedBottomSheetDialog(@NonNull Context context, float currPlaySpeed, @NonNull View.OnClickListener listener) {
        super(context);
        this.mContext = context;
        this.mCurrPlaySpeed = currPlaySpeed;
        this.mListener = listener;
        create();
    }

    public void create() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_play_speed, null, false);
        setContentView(view);

        RecyclerView recyclerView = view.findViewById(R.id.rv_play_speed);
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new PlaySpeedAdapter(mCurrPlaySpeed, mListener));

        ((View) view.getParent()).setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        this.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
    }
}