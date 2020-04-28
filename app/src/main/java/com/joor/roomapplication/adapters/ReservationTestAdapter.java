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

        if (imageLoader != null) {
            //init values
            imageLoader = AppController.getmInstance().getmImageLoader();
            final Button buttonBook = (Button) convertView.findViewById(R.id.buttonBook);
            final Button buttonBook2 = (Button) convertView.findViewById(R.id.buttonBook2);
            //test
            System.out.println("reservation size: " + reservations.size());

            //get display width and height
            displayMetrics = activity.getResources().getDisplayMetrics();
            int displayWidth = displayMetrics.widthPixels;
            int displayHeight = displayMetrics.heightPixels;

            if(position % 2 == 0){
                //loops two times to get two reservation object for a specific hour
                for (int positionCount = 0; positionCount < 2; positionCount++) {
                    final Reservation reservation = reservations.get(position + positionCount);

                    if (reservation.getStartTime().equals("free")) {
                        //if positionCount is 0 then set values for first button
                        if (positionCount == 0) {
                            buttonBook.setBackgroundColor(Color.parseColor("#ff93e6b3"));
                            ViewGroup.LayoutParams buttonBookParams = buttonBook.getLayoutParams();
                            buttonBookParams.width = displayWidth/2;
                            buttonBook.setLayoutParams(buttonBookParams);
                            //else set values for second button
                        } else {
                            buttonBook2.setBackgroundColor(Color.parseColor("#ff93e6b3"));
                            ViewGroup.LayoutParams buttonBook2Params = buttonBook2.getLayoutParams();
                            //subtract 2 pixels for border
                            buttonBook2Params.width = (displayWidth/2)-2;
                            buttonBook2.setLayoutParams(buttonBook2Params);
                        }
                    } else {
                        if (positionCount == 0) {
                            buttonBook.setBackgroundColor(Color.parseColor("#fffa7d89"));
                        } else {
                            buttonBook2.setBackgroundColor(Color.parseColor("#fffa7d89"));
                        }

                        //count "time steps" till endTime is reached
                        int timeStepCount = 1;
                        int posAdd = 1;
                        boolean endTimeReached = false;
                        while (!endTimeReached) {
                            //if startTime is NOT equals to booked
                            if (!reservations.get(position + positionCount + posAdd).getStartTime().equals("booked")) {
                                //endTime is reached
                                endTimeReached = true;
                            } else {
                                //otherwise increase time step counter (increased by 2 to only count half hours
                                timeStepCount += 2;
                                posAdd++;
                            }
                        }

                        if (positionCount == 0) {
                            //original height is 22 300
                            ViewGroup.LayoutParams buttonBookParams = buttonBook.getLayoutParams();
                            buttonBookParams.height = 20 * timeStepCount;
                            buttonBookParams.width = displayWidth;
                            buttonBook.setLayoutParams(buttonBookParams);
                        } else {
                            ViewGroup.LayoutParams buttonBook2Params = buttonBook2.getLayoutParams();
                            buttonBook2Params.height = 20 * timeStepCount;
                            buttonBook2Params.width = displayWidth;
                            buttonBook2.setLayoutParams(buttonBook2Params);
                            //bring forth the first button
                            buttonBook.bringToFront();
                        }
                    }

                    if (reservation.getStartTime().equals("booked")) {
                        if (positionCount == 0) {
                            if(!reservations.get(position + positionCount + 1).getStartTime().equals("free")) {
                                ViewGroup layout = (ViewGroup) buttonBook.getParent();
                                layout.removeView(buttonBook);
                            }else{
                                ViewGroup.LayoutParams buttonBookParams = buttonBook.getLayoutParams();
                                ViewGroup.LayoutParams buttonBookParams2 = buttonBook2.getLayoutParams();
                                ViewGroup.MarginLayoutParams buttonMargin = (ViewGroup.MarginLayoutParams) buttonBook.getLayoutParams();
                                buttonMargin.topMargin = 0;
                                buttonBookParams.height = buttonBookParams2.height;
                                buttonBookParams.width = displayWidth/2;
                                buttonBook.setLayoutParams(buttonBookParams);
                                buttonBook.setLayoutParams(buttonMargin);
                                buttonBook.setText("");

                            }
                        } else {
                            ViewGroup layout = (ViewGroup) buttonBook2.getParent();
                            layout.removeView(buttonBook2);
                        }
                    } else if (reservation.getStartTime().equals("free")) {
                        if(positionCount == 0) {
                            buttonBook.setText(getTimeByPosition(position));
                        }else {
                            buttonBook2.setText(getTimeByPosition(position + 1));
                        }

                    } else {
                        if (positionCount == 0) {
                            buttonBook.setText(reservation.getStartTime() + "-" + reservation.getEndTime());
                        } else {
                            buttonBook2.setText(reservation.getStartTime() + "-" + reservation.getEndTime());
                        }
                    }
                }
            } else if (position % 2 == 1) {
                ViewGroup layout = (ViewGroup) buttonBook.getParent();
                layout.removeView(buttonBook);
                ViewGroup layout2 = (ViewGroup) buttonBook2.getParent();
                layout2.removeView(buttonBook2);
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
