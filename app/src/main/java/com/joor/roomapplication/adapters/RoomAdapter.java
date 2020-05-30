package com.joor.roomapplication.adapters;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.joor.roomapplication.R;
import com.joor.roomapplication.interfaces.RecyclerClickInterface;
import com.joor.roomapplication.utility.LoadImage;
import com.joor.roomapplication.utility.RoomData;

import java.util.ArrayList;


public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.MyViewHolder> {
    Context mContext;

    private ArrayList<String> mDataset;
    private final RecyclerClickInterface mRecyclerClickInterface;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageView showRoomInfo;
        Dialog roomInfoDialog;

        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.roomMain);
            showRoomInfo = v.findViewById(R.id.buttonShowRoomInfo);
            roomInfoDialog = new Dialog(mContext);
        }
    }

    public RoomAdapter(Context context, ArrayList<String> myDataSet, RecyclerClickInterface mRecyclerClickInterface) {
        mContext = context;
        mDataset = myDataSet;
        this.mRecyclerClickInterface = mRecyclerClickInterface;
    }

    @Override
    public RoomAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
       View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.roomnames_layout, parent, false);

       MyViewHolder vh = new MyViewHolder(rowView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final String room = mDataset.get(position);
        holder.textView.setText(room);

        holder.showRoomInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                holder.roomInfoDialog.setContentView(R.layout.room_popup);
                //init image and text view inside popup
                final ImageView closeInfoPopup = holder.roomInfoDialog.findViewById(R.id.imgCloseInfoPopup);
                final TextView textViewRoomName = holder.roomInfoDialog.findViewById(R.id.textRoomName);
                final ImageView roomImage = holder.roomInfoDialog.findViewById(R.id.imgRoom);
                final TextView textViewRoomInfo = holder.roomInfoDialog.findViewById(R.id.textRoomInfo);

                textViewRoomName.setText(room);
                RoomData roomData = new RoomData();
                //get image link for specific room
                String imageLink = roomData.getLinkByRoomName(room);
                LoadImage loadImage = new LoadImage(roomImage);
                //load image from url
                loadImage.execute(imageLink);
                //set room info text
                textViewRoomInfo.setText(roomData.getInfoByRoomName(room));

                closeInfoPopup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.roomInfoDialog.dismiss();
                    }
                });
                holder.roomInfoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                holder.roomInfoDialog.show();
            }
        });

        // Alternates color between listitems
        if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#E5E5E5"));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerClickInterface.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

