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
import com.joor.roomapplication.activities.ShowDayActivity;
import com.joor.roomapplication.activities.ShowDayActivitySchedule;
import com.joor.roomapplication.models.Reservation;
import com.joor.roomapplication.controllers.AppController;

import java.util.List;

/**
 * Recycler View for Reservations
 * @author Jonas Ornfelt & Daniel Arnesson
 */

public class ReservationTestAdapter extends RecyclerView.Adapter<ReservationTestAdapter.ViewHolder> {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Reservation> reservations;
    ImageLoader imageLoader = AppController.getmInstance().getmImageLoader();
    View convertView;
    DisplayMetrics displayMetrics;

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView itemImg;
        private TextView itemDescription;

        public ViewHolder(View v){
            super(v);
        }
    }

    //constructor - RecyclerAdapter for better performance
    public ReservationTestAdapter(Activity activity, List<Reservation> reservations){
        this.activity = activity;
        this.reservations = reservations;
    }

    @Override
    public ReservationTestAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //create new view
        convertView = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.test_cutom_layout, parent, false);
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

        if (imageLoader != null) {
                //init values
                imageLoader = AppController.getmInstance().getmImageLoader();
                final Button buttonBook = (Button) convertView.findViewById(R.id.buttonBook);
                final Button buttonBook2 = (Button) convertView.findViewById(R.id.buttonBook2);
                final TextView textHour = (TextView) convertView.findViewById(R.id.textHour);

            if(position % 2 == 0) {
                //get display width and height
                displayMetrics = activity.getResources().getDisplayMetrics();
                int displayWidth = displayMetrics.widthPixels;
                int displayHeight = displayMetrics.heightPixels;
                ViewGroup.LayoutParams buttonBookParams = buttonBook.getLayoutParams();
                ViewGroup.LayoutParams buttonBook2Params = buttonBook2.getLayoutParams();

                //loops two times to get two reservation object for a specific hour
                for (int positionCount = 0; positionCount < 2; positionCount++) {
                    final Reservation reservation;

                    //in case list limit is reached
                    if(positionCount == 1 && reservations.size() == position+1){
                        break;
                    }else if (positionCount == 0 && reservations.size() == position + positionCount+1){
                        break;
                    }
                    reservation = reservations.get(position + positionCount);

                    if (reservation.getStartTime().equals("free")) {
                        //set button color to green
                        if (positionCount == 0) {
                            buttonBook.setBackgroundColor(Color.parseColor("#ff93e6b3"));
                            buttonBookParams.width = displayWidth;
                            buttonBook.setLayoutParams(buttonBookParams);
                        } else {
                            buttonBook2.setBackgroundColor(Color.parseColor("#ff93e6b3"));
                            buttonBook2Params.width = displayWidth;
                            buttonBook2.setLayoutParams(buttonBook2Params);
                        }
                    } else if (reservation.getStartTime().equals("booked")) {
                        if (positionCount == 0) {
                            buttonBook.setBackgroundColor(Color.parseColor("#fffa7d89"));
                            buttonBook.setClickable(false);
                            buttonBookParams.width = displayWidth;
                            buttonBook.setLayoutParams(buttonBookParams);
                            //if previous block is NOT free
                            if(!reservations.get(position + positionCount-1).getStartTime().equals("free")){
                                //then remove border from red area
                                ViewGroup.MarginLayoutParams buttonMargin = (ViewGroup.MarginLayoutParams) buttonBook.getLayoutParams();
                                buttonMargin.topMargin = 0;
                                buttonBook.setLayoutParams(buttonMargin);
                            }
                        } else {
                            buttonBook2.setBackgroundColor(Color.parseColor("#fffa7d89"));
                            buttonBook2.setClickable(false);
                            buttonBook2Params.width = displayWidth;
                            buttonBook2.setLayoutParams(buttonBook2Params);
                        }
                    }
                    //else means the reservation is booked
                    else {
                        //set button color to red and make nonclickable
                        if (positionCount == 0) {
                            buttonBook.setBackgroundColor(Color.parseColor("#fffa7d89"));
                            buttonBook.setClickable(false);
                            buttonBookParams.width = displayWidth;
                            buttonBook.setLayoutParams(buttonBookParams);
                            //buttonBook.setText(reservation.getStartTime() + "-" + reservation.getEndTime());
                        } else {
                            buttonBook2.setBackgroundColor(Color.parseColor("#fffa7d89"));
                            buttonBook2.setClickable(false);
                            buttonBook2Params.width = displayWidth;
                            buttonBook2.setLayoutParams(buttonBook2Params);
                            //buttonBook2.setText(reservation.getStartTime() + "-" + reservation.getEndTime());
                        }
                    }
                }

                //set time text
                if (getTimeByPosition(position).equals("")) {
                    //removes every other textview
                    ViewGroup layout = (ViewGroup) textHour.getParent();
                    layout.removeView(textHour);
                } else {
                    textHour.setText(getTimeByPosition(position));
                    textHour.setGravity(Gravity.CENTER_VERTICAL);

                    //if current block is free and next reservation is booked
                    if(reservations.get(position).getStartTime().equals("free") &&
                    !reservations.get(position+1).getStartTime().equals("free")){
                        //then change first hour in time text to half hour
                        String hourText = getTimeByPosition(position);
                        //split time into two parts, clarifying example: split into 08:00 and 09:00
                        String[] timeSplit = hourText.split("-");

                        //split hours, example: split into 08 and 00
                        String[] firstHourSplit = timeSplit[0].split(":");
                        //create new hour string with first hour split and "30", example: 08 + "30"
                        String newHour = firstHourSplit[0] + ":30";

                        //set new text and move up
                        textHour.setText(timeSplit[0] + "-" + newHour);
                        textHour.setGravity(20);
                    }
                    //else if current block is booked and next is free
                    else if(!reservations.get(position).getStartTime().equals("free") &&
                    reservations.get(position+1).getStartTime().equals("free")){
                        //then change second hour in time text to half hour
                        String hourText = getTimeByPosition(position);
                        //split time into two parts, clarifying example: split into 08:00 and 09:00
                        String[] timeSplit = hourText.split("-");

                        //split hours, example: split into 08 and 00
                        String[] firstHourSplit = timeSplit[0].split(":");
                        //create new hour string with first hour split and "30", example: 08 + "30"
                        String newHour = firstHourSplit[0] + ":30";

                        //set new text and move down
                        textHour.setText(newHour + "-" + timeSplit[1]);
                        textHour.setGravity(80);
                    }
                }
            }else{
                ViewGroup layout = (ViewGroup) textHour.getParent();
                layout.removeView(textHour);
                layout.removeView(buttonBook);
                layout.removeView(buttonBook2);
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
                ShowDayActivitySchedule.class);
        context.startActivity(intent);
    }

    //ugh...
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
