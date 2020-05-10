package com.joor.roomapplication.adapters;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.joor.roomapplication.R;
import com.joor.roomapplication.interfaces.RecyclerClickInterface;

import java.util.ArrayList;


public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.MyViewHolder> {
    Context mContext;

    private  ArrayList<String> mDataset;
    private  final RecyclerClickInterface mRecyclerClickInterface;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.roomMain);
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
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        String room = mDataset.get(position);
        holder.textView.setText(room);

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

