package com.joor.roomapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.android.volley.toolbox.ImageLoader;

import com.joor.roomapplication.controllers.AppController;
import com.joor.roomapplication.R;
import com.joor.roomapplication.activities.ShowReservationsActivity;
import com.joor.roomapplication.models.Reservation;

import java.util.List;

public class RoomAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Reservation> reservations;
    ImageLoader imageLoader = AppController.getmInstance().getmImageLoader();

    //constructor
    public RoomAdapter(Activity activity, List<Reservation> reservations){
        this.activity = activity;
        this.reservations = reservations;
    }

    @Override
    public int getCount(){
        return reservations.size();
    }

    @Override
    public Object getItem(int position){
        return reservations.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(inflater == null){
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if(convertView == null){
            convertView = inflater.inflate(R.layout.custom_layout, null);
        }

        if(imageLoader != null) {
            //init values
            imageLoader = AppController.getmInstance().getmImageLoader();
            //final NetworkImageView imageView = (NetworkImageView) convertView.findViewById(R.id.imgItem);
            final TextView itemName = (TextView) convertView.findViewById(R.id.textId);
            final Reservation reservation = reservations.get(position);

            //imageView.setImageUrl(itemExpired.getPicture(), imageLoader);
            itemName.setText(reservation.getId());

        }else{
        }


        return convertView;
    }

    //refreshes item list
    private void refreshList(Context context){
        Intent intent = new Intent(context,
                ShowReservationsActivity.class);
        context.startActivity(intent);
    }
}
