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
    private String lastRoomName = "";

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
        convertView = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout_available, parent, false);
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
            convertView = inflater.inflate(R.layout.custom_layout_available, null);
        }

        //init textviews and buttons
        final TextView textHour = (TextView) convertView.findViewById(R.id.textHour);
        final TextView textHourBooking = (TextView) convertView.findViewById(R.id.textHourBooking);
        final Button buttonBook = (Button) convertView.findViewById(R.id.buttonBook);
        final Button buttonBook2 = (Button) convertView.findViewById(R.id.buttonBook2);

        //get display width and height
        displayMetrics = activity.getResources().getDisplayMetrics();
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        int dDensityPerInch = displayMetrics.densityDpi;


        if (imageLoader != null) {
                //init imageLoader
                imageLoader = AppController.getmInstance().getmImageLoader();
                try {
                    if (position % 2 == 0) {

                        //loops two times to get two reservation object for a specific hour
                        for (int positionCount = 0; positionCount < 2; positionCount++) {
                            final Reservation reservation;

                            //in case list limit is reached
                            if (positionCount == 1 && reservations.size() == position + positionCount) {
                                break;
                            }
                            /*
                        if(dDensityPerInch>420){
                            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                            layoutParams.height = 86;
                            convertView.setLayoutParams(layoutParams);
                        }
                             */

                        //in case list limit is reached
                        if (positionCount == 1 && reservations.size() == position + positionCount) {
                            break;
                        }

                            //get reservation object
                            reservation = reservations.get(position + positionCount);
                            if (buttonBook == null) {
                                System.out.println("null pointer here, position: " + position + ", res size: " +
                                        reservations.size());
                            }

                            //set button color to green
                            if (positionCount == 0) {
                                buttonBook.setBackgroundColor(Color.parseColor("#ff93e6b3"));
                                ViewGroup.LayoutParams buttonBookParams = buttonBook.getLayoutParams();
                                buttonBookParams.width = displayWidth;
                                buttonBook.setLayoutParams(buttonBookParams);
                                if (position > 0 && !reservation.getName().equals(reservations.get(position - 1).getName()) &&
                                        !lastRoomName.equals(reservation.getName()[0])) {
                                    buttonBook.setText(reservation.getName()[0]);
                                    buttonBook.bringToFront();
                                    lastRoomName = reservation.getName()[0];
                                } else if (position == 0) {
                                    buttonBook.setText(reservation.getName()[0]);
                                    buttonBook.bringToFront();
                                }
                            } else {
                                buttonBook2.setBackgroundColor(Color.parseColor("#ff93e6b3"));
                                ViewGroup.LayoutParams buttonBook2Params = buttonBook2.getLayoutParams();
                                buttonBook2Params.width = displayWidth;
                                buttonBook2.setLayoutParams(buttonBook2Params);
                                //only set text for second block if it's not the same as first room name
                                if (!buttonBook.getText().equals(reservation.getName()[0])) {
                                    buttonBook2.setText(reservation.getName()[0]);
                                    buttonBook2.bringToFront();
                                    //also add border to separate
                                    ViewGroup.MarginLayoutParams buttonMargin = (ViewGroup.MarginLayoutParams) buttonBook2.getLayoutParams();
                                    buttonMargin.topMargin = 2;
                                    buttonBook2.setLayoutParams(buttonMargin);
                                    //set text for next block
                                    if (position + 1 != reservations.size()) {
                                        if (reservations.get(position + 1).getEndTime() != null) {
                                            textHourBooking.setText(reservations.get(position + 1).getStartTime() + "-" +
                                                    reservations.get(position + 1).getEndTime());
                                            textHourBooking.bringToFront();
                                            textHourBooking.setGravity(Gravity.BOTTOM);
                                        } else {
                                            textHourBooking.setText(reservations.get(position - 1).getStartTime()
                                                    + "-" + getTimePlusOneHour(reservations.get(position - 1).getStartTime()));
                                            textHourBooking.bringToFront();
                                            textHourBooking.setGravity(Gravity.BOTTOM);
                                        }
                                    }
                                }
                            }
                            if (position > 0 && positionCount == 0 && reservations.get(position - 1).getName()[0].equals(reservation.getName()[0])) {
                                //if last reservation is same room as current, then remove margin from top
                                ViewGroup.MarginLayoutParams buttonMargin = (ViewGroup.MarginLayoutParams) buttonBook.getLayoutParams();
                                buttonMargin.topMargin = 0;
                                buttonBook.setLayoutParams(buttonMargin);
                            }
                            //if reservation object and next is the same room, then the room is available for an entire hour
                            if (positionCount == 0 && position + 1 != reservations.size() &&
                                    reservation.getName()[0].equals(reservations.get(position + 1).getName()[0])) {

                                //check that last reservation object does NOT have the same room as current reservation
                                if (position > 0 && !reservations.get(position - 1).getName()[0].equals(reservation.getName()[0])) {
                                    //set text to show that room is available for an hour
                                    textHour.setText(reservation.getStartTime() + "-" + getTimePlusOneHour(reservation.getStartTime()));
                                    textHour.bringToFront();
                                    //remove second textview
                                } else if (position == 0) {
                                    textHour.setText(reservation.getStartTime() + "-" + getTimePlusOneHour(reservation.getStartTime()));
                                    textHour.bringToFront();
                                    //remove second textview
                                }
                            }

                            //else means two texts should be set
                            else if (positionCount == 0) {
                                if (reservation.getEndTime() != null) {
                                    textHour.setText(reservation.getStartTime() + "-" + reservation.getEndTime());
                                    textHour.bringToFront();
                                }
                                if (position + 1 != reservations.size()) {
                                    if (reservations.get(position + 1).getEndTime() != null) {
                                        textHourBooking.setText(reservations.get(position + 1).getStartTime() + "-" +
                                                reservations.get(position + 1).getEndTime());
                                        textHourBooking.bringToFront();
                                        textHourBooking.setGravity(Gravity.BOTTOM);
                                    }
                                }
                                //fix for cases where no text is set
                                if (position > 0 && textHour.getText().equals("") && textHourBooking.getText().equals("")) {
                                    textHourBooking.setText(reservations.get(position - 1).getStartTime()
                                            + "-" + getTimePlusOneHour(reservations.get(position - 1).getStartTime()));
                                    textHourBooking.bringToFront();
                                    textHourBooking.setGravity(Gravity.BOTTOM);
                                } else if (position > 0 && textHour.getText().equals(null) && textHourBooking.getText().equals(null)) {
                                    textHourBooking.setText(reservations.get(position - 1).getStartTime()
                                            + "-" + getTimePlusOneHour(reservations.get(position - 1).getStartTime()));
                                    textHourBooking.bringToFront();
                                    textHourBooking.setGravity(Gravity.BOTTOM);
                                }
                            }
                        }

                    } else {
                        //if position is an odd number, remove view elements
                        ViewGroup layout = (ViewGroup) textHour.getParent();
                        layout.removeView(textHour);
                        layout.removeView(buttonBook);
                        layout.removeView(buttonBook2);
                        ViewGroup layout2 = (ViewGroup) textHourBooking.getParent();
                        layout2.removeView(textHourBooking);
                    }

                    //add onclicklistener to booking buttons
                    if (buttonBook.isClickable()) {
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
                    if (buttonBook2.isClickable()) {
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
                } catch (Exception e) {
                    e.printStackTrace();
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

    //string that returns incoming time string after adding 1 hour to it
    private String getTimePlusOneHour(String t){

        //this string will be returned
        String returnTime;
        //incoming t should be formatted as: HH:mm
        String[] tSplit = t.split(":");
        //create int containing hour
        int hour = Integer.parseInt(tSplit[0]);
        //String containing minutes
        String min = tSplit[1];

        //if hour is 08
        if(hour == 8){
            returnTime = "09:"+min;
        }else{
            returnTime = hour+1 + ":" + min;
            if(hour+1 == 20){
                returnTime = hour+1 + ":" + "00";
            }
        }

        return returnTime;
    }
}
