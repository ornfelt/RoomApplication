package com.joor.roomapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import com.joor.roomapplication.AppController;
import com.joor.roomapplication.R;
import com.joor.roomapplication.ShowRoomsActivity;
import com.joor.roomapplication.models.Room;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RoomAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Room> rooms;
    ImageLoader imageLoader = AppController.getmInstance().getmImageLoader();

    //constructor
    public RoomAdapter(Activity activity, List<Room> rooms){
        this.activity = activity;
        this.rooms = rooms;
    }

    @Override
    public int getCount(){
        return rooms.size();
    }

    @Override
    public Object getItem(int position){
        return rooms.get(position);
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
            final TextView itemName = (TextView) convertView.findViewById(R.id.textItem);
            final Room room = rooms.get(position);

            //imageView.setImageUrl(itemExpired.getPicture(), imageLoader);
            itemName.setText(room.getId());

        }else{
        }


        return convertView;
    }

    //refreshes item list
    private void refreshList(Context context){
        Intent intent = new Intent(context,
                ShowRoomsActivity.class);
        context.startActivity(intent);
    }
}
