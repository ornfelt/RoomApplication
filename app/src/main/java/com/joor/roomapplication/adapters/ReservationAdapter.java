package com.joor.roomapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.joor.roomapplication.R;
import com.joor.roomapplication.activities.ShowDayActivity;
import com.joor.roomapplication.models.Reservation;
import com.joor.roomapplication.controllers.AppController;

import java.util.List;

/**
 * Recycler View for Reservations
 * @author Jonas Ornfelt & Daniel Arnesson
 */

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Reservation> reservations;
    ImageLoader imageLoader = AppController.getmInstance().getmImageLoader();
    View convertView;

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView itemImg;
        private TextView itemDescription;

        public ViewHolder(View v){
            super(v);
        }
    }

    //constructor - RecyclerAdapter for better performance
    public ReservationAdapter(Activity activity, List<Reservation> reservations){
        this.activity = activity;
        this.reservations = reservations;
    }

    @Override
    public ReservationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //create new view
        convertView = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout, parent, false);
        ViewHolder vh = new ViewHolder(convertView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if(inflater == null){
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if(convertView == null){
            convertView = inflater.inflate(R.layout.custom_layout, null);
        }

        if(imageLoader != null) {
            //init values
            imageLoader = AppController.getmInstance().getmImageLoader();
            final TextView itemName = (TextView) convertView.findViewById(R.id.textId);
            //TextView itemExpired = (TextView) convertView.findViewById(R.id.textExpired);
            final Reservation reservation = reservations.get(position);

            itemName.setText(Integer.toString(reservation.getId()));

            final Button buttonBook = (Button) convertView.findViewById(R.id.buttonBook);

        }
    }

    //getItemCount needs to return the size of the reservations list, otherwise recyclerview never tries to instatiate a view
    @Override
    public int getItemCount() {
        return reservations.size();
    }

    //refreshes item list
    private void refreshList(Context context){
        Intent intent = new Intent(context,
                ShowDayActivity.class);
        context.startActivity(intent);
    }
}
