package com.joor.roomapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
            final TextView textHour = (TextView) convertView.findViewById(R.id.textTime);
            final Button buttonBook = (Button) convertView.findViewById(R.id.buttonBook);
            final Reservation reservation = reservations.get(position);
            System.out.println("reservation size: " + reservations.size());

            if(reservation.getStartTime().equals("booked")){
                buttonBook.setBackgroundColor(Color.RED);
                itemName.setText("Time booked");

            }else if(reservation.getStartTime().equals("free")){
                buttonBook.setBackgroundColor(Color.GREEN);
                itemName.setText("Time available");
            }else{
                itemName.setText(Integer.toString(reservation.getId()));
                buttonBook.setBackgroundColor(Color.RED);
            }

            textHour.setText(getTimeByPosition(position));
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

    //ugh...
    private String getTimeByPosition(int pos){
        String time = "";
        if(pos == 0){
            time = "08:00-08:30";
        }else if(pos == 1){
            time = "08:30-09:00";
        }else if(pos == 2){
            time = "09:00-09:30";
        }else if(pos == 3){
            time = "09:30-10:00";
        }
        else if(pos == 4){
            time = "10:00-10:30";
        }else if(pos == 5){
            time = "10:30-11:00";
        }
        else if(pos == 6){
            time = "11:00-11:30";
        }else if(pos == 7){
            time = "11:30-12:00";
        }
        else if(pos == 8){
            time = "12:00-12:30";
        }else if(pos == 9){
            time = "12:30-13:00";
        }
        else if(pos == 10){
            time = "13:00-13:30";
        }else if(pos == 11){
            time = "13:30-14:00";
        }
        else if(pos == 12){
            time = "14:00-14:30";
        }else if(pos == 13){
            time = "14:30-15:00";
        }
        else if(pos == 14){
            time = "15:00-15:30";
        }
        else if(pos == 15){
            time = "15:30-16:00";
        }
        else if(pos == 16){
            time = "16:00-16:30";
        }
        else if(pos == 17){
            time = "16:30-17:00";
        }
        else if(pos == 18){
            time = "17:00-17:30";
        }
        else if(pos == 19){
            time = "17:30-18:00";
        }
        else if(pos == 20){
            time = "18:00-18:30";
        }
        else if(pos == 21){
            time = "18:30-19:00";
        }
        else if(pos == 22){
            time = "19:00-19:30";
        }
        else if(pos == 23){
            time = "19:30-20:00";
        }
        else{
            time = "xx:xx-xx:xx";
        }
        return time;
    }
}
