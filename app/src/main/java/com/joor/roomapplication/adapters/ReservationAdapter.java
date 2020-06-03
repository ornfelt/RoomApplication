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
import android.media.Image;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.ImageLoader;
import com.joor.roomapplication.R;
import com.joor.roomapplication.activities.BookingActivity;
import com.joor.roomapplication.activities.ShowDayActivity;
import com.joor.roomapplication.activities.ShowDayActivitySchedule;
import com.joor.roomapplication.models.Reservation;
import com.joor.roomapplication.controllers.AppController;
import com.joor.roomapplication.utility.ShowAmountValues;
import com.joor.roomapplication.utility.TempValues;

import java.util.List;

/**
 * Recycler View for Reservations
 * @author Jonas Ornfelt & Daniel Arnesson
 */

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    private Activity activity;
    private LayoutInflater inflater;
   // private GestureDetector gestureDetector;
    private List<Reservation> reservations;
    ImageLoader imageLoader = AppController.getmInstance().getmImageLoader();
    View convertView;
    DisplayMetrics displayMetrics;
private String roomName;

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView itemImg;
        private TextView itemDescription;

        public ViewHolder(View v){
            super(v);
        }
    }

    //constructor - RecyclerAdapter for better performance
    public ReservationAdapter(Activity activity, List<Reservation> reservations, String roomName){
        this.activity = activity;
        this.reservations = reservations;
        this.roomName = roomName;
    }

    @Override
    public ReservationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //get display height
        displayMetrics = activity.getResources().getDisplayMetrics();
        int displayHeight = displayMetrics.heightPixels;
        //create new view
        if(displayHeight <= 1200){
            System.out.println("720p layout set");
            convertView = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout_720p, parent, false);
        }else if(displayHeight >= 1920){
            convertView = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout_1920, parent, false);
        }
        //default layout
        else {
            convertView = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout, parent, false);
        }

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

        displayMetrics = activity.getResources().getDisplayMetrics();
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;

        int dDensityPerInch = displayMetrics.densityDpi;

        if (imageLoader != null) {
            //init values
            imageLoader = AppController.getmInstance().getmImageLoader();
            final Button buttonBook = (Button) convertView.findViewById(R.id.buttonBook);
            final Button buttonBook2 = (Button) convertView.findViewById(R.id.buttonBook2);
            final TextView textHour = (TextView) convertView.findViewById(R.id.textHour);
            final TextView textHourBooking = (TextView) convertView.findViewById(R.id.textHourBooking);

            //boolean used to find middle time block in reservation
            boolean isMiddleReservation = false;
            //boolean to fix a specific bug where available time text showed incorrectly for a quarter time slot
            boolean doSetText = true;

            try {
                //boolean to fix bug where an extra block element appeared
                boolean allElementsShown = false;
                //fix for extra block appearing after schedule
                if(position > 2 && getTimeByPosition(position-2).equals("19:00-20:00")){
                    //remove elements
                    allElementsShown = true;
                }

                if (position % 2 == 0 && !allElementsShown) {

                    //get display width and height
                    ViewGroup.LayoutParams buttonBookParams = buttonBook.getLayoutParams();
                    ViewGroup.LayoutParams buttonBook2Params = buttonBook2.getLayoutParams();
                    ViewGroup.LayoutParams textHourBookingParams = textHourBooking.getLayoutParams();

                    //loops two times to get two reservation object for a specific hour
                    for (int positionCount = 0; positionCount < 2; positionCount++) {
                        final Reservation reservation;

                        if (displayHeight >= 1900) {
                            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                            layoutParams.height = 115;
                            convertView.setLayoutParams(layoutParams);
                        } else if (dDensityPerInch > 420 && displayHeight > 1200) {
                            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                            layoutParams.height = 100;
                            convertView.setLayoutParams(layoutParams);
                        } else if (dDensityPerInch < 350 && displayHeight < 1200) {
                        }

                        //in case list limit is reached
                        if (positionCount == 1 && reservations.size() == position + positionCount) {
                            break;
                        }

                        reservation = reservations.get(position + positionCount);
                        //don't set if booking is only available for a quarter (example: 14:45-15:00)
                        TempValues tempValues = TempValues.getInstance();
                        String lastEndTime = "";
                        if (tempValues.tempValuesList.size() > 0) {
                            lastEndTime = tempValues.tempValuesList.get(0);
                        }
                        int endTimeMin = 0;
                        if (!lastEndTime.equals("")) {
                            endTimeMin = Integer.parseInt(lastEndTime.split(":")[1]);
                        }
                        if (endTimeMin == 45 && positionCount == 1 && reservation.getStartTime().equals("free")) {
                            tempValues.resetTempValuesList();
                            doSetText = false;
                        }

                        //if time slot is available
                        // && endTimeMin != 45
                        if (reservation.getStartTime().equals("free")) {
                            //set button color to greenb
                            if (positionCount == 0) {
                                // old color #ff93e6b3
                                // alternate1 (as of 2020-06-01) "#e5e5e5"
                                buttonBook.setBackgroundColor(Color.parseColor("#e5e5e5"));
                                buttonBookParams.width = displayWidth;
                                buttonBook.setLayoutParams(buttonBookParams);
                            } else {
                                // old color #ff93e6b3"
                                buttonBook2.setBackgroundColor(Color.parseColor("#e5e5e5"));
                                buttonBook2Params.width = displayWidth;
                                buttonBook2.setLayoutParams(buttonBook2Params);
                            }
                        }
                        //else means the reservation is booked
                        else {
                            if (dDensityPerInch > 420 && displayHeight > 1200) {
                                ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                                // As of now this one is a little bigger, because otherwise the color won't cover the hourtext. If that's fixed it should be the same as the other if-case.
                                layoutParams.height = 103;
                                convertView.setLayoutParams(layoutParams);
                            }
                            //set button color to red and make nonclickable
                            if (positionCount == 0) {
                                // old color #fffa7d89
                                // alternate1 c1c1c1
                                // alternate2 (as of 2020-06-01) "#b2b2b2"
                                buttonBook.setBackgroundColor(Color.parseColor("#b2b2b2"));
                                buttonBook.setClickable(false);
                                buttonBookParams.width = displayWidth;
                                buttonBook.setLayoutParams(buttonBookParams);
                                //if previous block is NOT free
                                if (position > 0 && !reservations.get(position + positionCount - 1).getStartTime().equals("free")) {
                                    //then remove border from red area
                                    ViewGroup.MarginLayoutParams buttonMargin = (ViewGroup.MarginLayoutParams) buttonBook.getLayoutParams();
                                    buttonMargin.topMargin = 0;
                                    buttonBook.setLayoutParams(buttonMargin);
                                }
                            } else {
                                // old color #fffa7d89
                                buttonBook2.setBackgroundColor(Color.parseColor("#b2b2b2"));
                                buttonBook2.setClickable(false);
                                buttonBook2Params.width = displayWidth;
                                buttonBook2.setLayoutParams(buttonBook2Params);
                                //buttonBook2.setText(reservation.getStartTime() + "-" + reservation.getEndTime());
                            }

                            //count "time steps" till endTime is reached
                            int timeStepToEnd = 1;
                            int posAdd = 1;
                            boolean endTimeReached = false;
                            while (!endTimeReached) {
                                if (reservations.size() > position + positionCount + posAdd) {
                                    //if endTime is NOT equal to "booked"
                                    if (!reservations.get(position + positionCount + posAdd).getStartTime().equals("booked")) {
                                        //endTime is reached
                                        endTimeReached = true;
                                    } else {
                                        //otherwise increase time step counter
                                        timeStepToEnd++;
                                        posAdd++;
                                    }
                                } else {
                                    endTimeReached = true;
                                }
                            }

                            //count "time steps" till startTime is reached
                            int timeStepToStart = 1;
                            posAdd = 1;
                            if (position > 0) {
                                boolean startTimeReached = false;
                                //if current position contains booking
                                if (!reservations.get(position + positionCount).getStartTime().equals("booked") &&
                                        !reservations.get(position + positionCount).getStartTime().equals("free")) {
                                    //then startTime is reached
                                    startTimeReached = true;
                                    timeStepToStart = 0;
                                }
                                //else find startTime
                                else {
                                    while (!startTimeReached) {
                                        //if startTime is NOT equal to "booked"
                                        if (!reservations.get(position + positionCount - posAdd).getStartTime().equals("booked")) {
                                            //startTime is reached
                                            startTimeReached = true;
                                        } else {
                                            //otherwise increase time step counter
                                            timeStepToStart++;
                                            posAdd++;
                                        }
                                    }
                                }
                            } else {
                                timeStepToStart = 0;
                            }

                            //set reservation time at first block
                            if (timeStepToStart == 0 && reservations.get(position + positionCount - timeStepToStart).getEndTime() != null) {
                                //then set text
                                textHourBooking.setEnabled(true);
                                textHourBooking.setText(reservations.get(position + positionCount - timeStepToStart).getStartTime() +
                                        "-" + reservations.get(position + positionCount - timeStepToStart).getEndTime());
                                //save endtime value
                                tempValues = TempValues.getInstance();
                                //reset list if value already exists
                                if (tempValues.tempValuesList.size() != 0) {
                                    tempValues.resetTempValuesList();
                                }
                                tempValues.tempValuesList.add(reservations.get(position + positionCount - timeStepToStart).getEndTime());
                                isMiddleReservation = true;
                                textHourBooking.setGravity(Gravity.BOTTOM);
                                textHourBooking.bringToFront();
                            }
                        }
                    }
                    //remove second textview if time block isn't middle of reservation
                    if (!isMiddleReservation) {
                        ViewGroup layout2 = (ViewGroup) textHourBooking.getParent();
                        layout2.removeView(textHourBooking);
                    }

                    //set time text
                    if (getTimeByPosition(position).equals("") && !isMiddleReservation) {
                        //removes every other textview
                        System.out.println(textHour.getText() + ", textHour removed (1) " + isMiddleReservation + ", " + position);
                        ViewGroup layout = (ViewGroup) textHour.getParent();
                        layout.removeView(textHour);
                    } else if (reservations.get(position).getStartTime().equals("booked") &&
                            reservations.get(position + 1).getStartTime().equals("booked") && !isMiddleReservation) {
                        //removes every other textview
                        ViewGroup layout = (ViewGroup) textHour.getParent();
                        layout.removeView(textHour);
                        System.out.println("textHour removed (2)");
                    } else {
                        //don't set text if block is a reservation
                        if (!isMiddleReservation && reservations.get(position).getStartTime().equals("free")) {
                            textHour.setText(getTimeByPosition(position));
                            textHour.setGravity(Gravity.CENTER_VERTICAL);
                            textHour.bringToFront();
                        }

                        //if current block is free and next reservation is booked
                        if (reservations.get(position).getStartTime().equals("free") &&
                                !reservations.get(position + 1).getStartTime().equals("free")) {
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
                            textHour.setGravity(15);
                            textHour.bringToFront();
                        }
                        //else if current block is booked and next is free
                        else if (!reservations.get(position).getStartTime().equals("free") &&
                                reservations.get(position + 1).getStartTime().equals("free")) {
                            //then change second hour in time text to half hour
                            String hourText = getTimeByPosition(position);
                            //split time into two parts, clarifying example: split into 08:00 and 09:00
                            String[] timeSplit = hourText.split("-");

                            //split hours, example: split into 08 and 00
                            String[] firstHourSplit = timeSplit[0].split(":");
                            //create new hour string with first hour split and "30", example: 08 + "30"
                            String newHour = firstHourSplit[0] + ":30";

                            //set new text and move down
                            if(doSetText) {
                                textHour.setText(newHour + "-" + timeSplit[1]);
                                textHour.setGravity(80);
                                textHour.bringToFront();
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
                            intent.putExtra(BookingActivity.RESERVATION_ROOM_NAME, roomName);
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
                            intent.putExtra(BookingActivity.RESERVATION_ROOM_NAME, roomName);
                            activity.startActivity(intent);

                        }
                    });
                }
            }catch(Exception e ){
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





