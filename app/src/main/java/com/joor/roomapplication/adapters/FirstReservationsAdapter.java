package com.joor.roomapplication.adapters;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.joor.roomapplication.R;
import com.joor.roomapplication.activities.BookingActivity;
import com.joor.roomapplication.activities.ShowDayActivity;
import com.joor.roomapplication.activities.ShowDayActivitySchedule;
import com.joor.roomapplication.activities.ShowFirstAvailableActivity;
import com.joor.roomapplication.models.Reservation;
import com.joor.roomapplication.controllers.AppController;

import java.util.List;

/**
 * Recycler View for Reservations
 * @author Jonas Ornfelt & Daniel Arnesson
 */

public class FirstReservationsAdapter extends RecyclerView.Adapter<FirstReservationsAdapter.ViewHolder> {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Reservation> reservations;
    ImageLoader imageLoader = AppController.getmInstance().getmImageLoader();
    View convertView;
    DisplayMetrics displayMetrics;

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View v){
            super(v);
        }
    }

    //constructor - RecyclerAdapter for better performance
    public FirstReservationsAdapter(Activity activity, List<Reservation> reservations){
        this.activity = activity;
        this.reservations = reservations;
    }

    @Override
    public FirstReservationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //create new view
        convertView = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout, parent, false);
        ViewHolder vh = new ViewHolder(convertView);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (inflater == null) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_layout, null);
        }

        //init textviews and buttons
        final TextView textHour = (TextView) convertView.findViewById(R.id.textHour);
        final TextView textHourBooking = (TextView) convertView.findViewById(R.id.textHourBooking);
        final Button buttonBook = (Button) convertView.findViewById(R.id.buttonBook);
        final Button buttonBook2 = (Button) convertView.findViewById(R.id.buttonBook2);

        if (imageLoader != null) {
            //init imageLoader
            imageLoader = AppController.getmInstance().getmImageLoader();
            if(position % 2 == 0) {

                //get display width and height
                displayMetrics = activity.getResources().getDisplayMetrics();
                int displayWidth = displayMetrics.widthPixels;
                int displayHeight = displayMetrics.heightPixels;

                //loops two times to get two reservation object for a specific hour
                for (int positionCount = 0; positionCount < 2; positionCount++) {
                    final Reservation reservation;

                    //in case list limit is reached
                    if(positionCount == 1 && reservations.size() == position+positionCount){
                        break;
                    }

                    //get reservation object
                    reservation = reservations.get(position + positionCount);

                        //set button color to green
                        if (positionCount == 0) {
                            buttonBook.setBackgroundColor(Color.parseColor("#ff93e6b3"));
                            ViewGroup.LayoutParams buttonBookParams = buttonBook.getLayoutParams();
                            buttonBookParams.width = displayWidth;
                            buttonBook.setLayoutParams(buttonBookParams);
                            buttonBook.setText(reservation.getName()[0]);
                            buttonBook.bringToFront();
                        } else {
                            buttonBook2.setBackgroundColor(Color.parseColor("#ff93e6b3"));
                            ViewGroup.LayoutParams buttonBook2Params = buttonBook2.getLayoutParams();
                            buttonBook2Params.width = displayWidth;
                            buttonBook2.setLayoutParams(buttonBook2Params);
                            //only set text for second block if it's not the same as first room name
                            if(!buttonBook.getText().equals(reservation.getName()[0])) {
                                buttonBook2.setText(reservation.getName()[0]);
                                buttonBook2.bringToFront();
                            }
                        }
                        //if reservation object and next is the same room, then the room is available for an entire hour
                        if(positionCount == 0 && position+1 != reservations.size() && reservation.getName()[0].equals(reservations.get(position+1).getName()[0])){
                            //set text to show that room is available for an hour
                            //TODO: fix end time & texthourbooking not showing up
                            textHour.setText(reservation.getStartTime()+" - 1 hour forward");
                            textHour.bringToFront();
                            //remove second textview
                            ViewGroup layout = (ViewGroup) textHourBooking.getParent();
                            layout.removeView(textHourBooking);
                        }
                        //else means two texts should be set
                        else if(positionCount == 0){
                            textHour.setText(reservation.getStartTime()+"-ENDTIME");
                            textHour.bringToFront();
                            if(position+1 != reservations.size()) {
                                textHourBooking.setText(reservations.get(position + 1).getStartTime() + "-ENDTIME");
                                textHourBooking.setGravity(Gravity.BOTTOM);
                                textHourBooking.bringToFront();
                            }
                        }
                }

            }else{
                //if position is an odd number, remove view elements
                ViewGroup layout = (ViewGroup) textHour.getParent();
                layout.removeView(textHour);
                layout.removeView(buttonBook);
                layout.removeView(buttonBook2);
                ViewGroup layout2 = (ViewGroup) textHourBooking.getParent();
                layout2.removeView(textHourBooking);
            }

            //add onclicklistener to booking buttons
            if(buttonBook.isClickable()){
                buttonBook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //navigate to booking view
                        Intent intent = new Intent(activity,
                                BookingActivity.class);
                        intent.putExtra(BookingActivity.INTENT_MESSAGE_KEY, textHour.getText());
                        activity.startActivity(intent);

                    }
                });
            }
            if(buttonBook2.isClickable()) {
                buttonBook2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //navigate to booking view
                        Intent intent = new Intent(activity,
                                BookingActivity.class);
                        intent.putExtra(BookingActivity.INTENT_MESSAGE_KEY, textHour.getText());
                        activity.startActivity(intent);

                    }
                });
            }
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
                ShowFirstAvailableActivity.class);
        context.startActivity(intent);
    }

    private String getTimeByPosition(int pos){
        String time = "";

        if(pos == 0){
            time = "08:00-09:00";
        }else if(pos == 2){
            time = "09:00-10:00";
        }else if(pos == 4){
            time = "10:00-11:00";
        }else if(pos == 6){
            time = "11:00-12:00";
        }else if(pos == 8){
            time = "12:00-13:00";
        }else if(pos == 10){
            time = "13:00-14:00";
        }else if(pos == 12){
            time = "14:00-15:00";
        }else if(pos == 14){
            time = "15:00-16:00";
        }else if(pos == 16){
            time = "16:00-17:00";
        }else if(pos == 18){
            time = "17:00-18:00";
        }else if(pos == 20){
            time = "18:00-19:00";
        }else if(pos == 22){
            time = "19:00-20:00";
        }
        return time;
    }
}
