package com.joor.roomapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.joor.roomapplication.R;
import com.joor.roomapplication.activities.MainActivity;
import com.joor.roomapplication.adapters.ReservationAdapter;
import com.joor.roomapplication.controllers.AppController;
import com.joor.roomapplication.data.ReservationRetriever;
import com.joor.roomapplication.models.Reservation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ShowDayActivityPerHour extends AppCompatActivity {

    public static String INTENT_MESSAGE_KEY = "ROOM_NAME";
    private String room_name;
    private RecyclerView recyclerView;
    private static final String url = "https://timeeditrestapi.herokuapp.com/reservations/";
    private List<Reservation> reservations = new ArrayList<Reservation>();
    private ReservationAdapter adapter;

    private TextView roomName;
    private TextView todaysDate;
    private ImageView rightClick;
    private ImageView leftClick;
    private Date constantDate;
    private Calendar constantCalendar;
    private Calendar changableCalendar;
    private String dateToday;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_day_per_hour);
        // Set today's date
        setDate();
        bindViews();
        //init view elements and checks if there's a saved instance
        setViewElements(savedInstanceState);
        // Set up imagelisteners
        setImgListeners();
        setDatePicker();
        // Set up the adapter
        setAdapter();
        // Gets the availability for a specific room
        getAvailability();

    }

    //gets id for view
    private void bindViews() {
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    //when user navigates back
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),
                MainActivity.class);
        getApplicationContext().startActivity(intent);
    }

    public static String[] toStringArray(JSONArray array) {
        if (array == null)
            return null;

        String[] arr = new String[array.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = array.optString(i);
        }
        return arr;
    }

    //This class shows available times for a specific day and a specific room, hence these values needs to be loaded from intent
    private void initValuesFromIntent(){
        Intent intent = getIntent();
        room_name = intent.getStringExtra(INTENT_MESSAGE_KEY);

        todaysDate = findViewById(R.id.date);
        roomName = findViewById(R.id.txtRoomName);
        // Clicklistener for rightClick.
        rightClick = (ImageView)findViewById(R.id.rightClick);
        leftClick = (ImageView)findViewById(R.id.leftClick);

        todaysDate.setText(dateToday);
        roomName.setText(room_name);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        //saves when onDestroyed
        outState.putString("roomName", room_name);
    }

    private void initValuesFromSavedState(Bundle savedInstanceState) throws IOException {
        room_name = savedInstanceState.getString("roomName");
    }

    private ArrayList<String> timeList (){
        ArrayList<String> availableTimesList = new ArrayList<>();
        String hour = "8";
        String full = "00";
        String quarter = "15";
        String half = "30";
        String threeQuarter = "45";

        for(int i = 0; i < 12; i++){
            if(Integer.parseInt(hour) < 10){
                availableTimesList.add("0" + hour + ":" + full);
                availableTimesList.add("0" + hour + ":" + quarter);
                availableTimesList.add("0" + hour + ":" + half);
                availableTimesList.add("0" + hour + ":" + threeQuarter);
            }else{
                availableTimesList.add(hour + ":" + full);
                availableTimesList.add(hour + ":" + quarter);
                availableTimesList.add(hour + ":" + half);
                availableTimesList.add(hour + ":" + threeQuarter);
            }
            if(i == 11) availableTimesList.add("20:00");
            hour = Integer.toString(Integer.parseInt(hour)+ 1);
        }

        return availableTimesList;
    }

    private void setViewElements(Bundle savedInstanceState){
        if(savedInstanceState == null) {
            initValuesFromIntent();
        }else{
            try {
                initValuesFromSavedState(savedInstanceState);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void setDate(){
        constantCalendar = Calendar.getInstance();
        changableCalendar = constantCalendar;
        constantDate = constantCalendar.getTime();

        // Creates datepattern for todays date
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        dateToday = simpleDateFormat.format(constantDate);
    }

    private void setAdapter(){
        //creates RecycleAdapter and sets it
        adapter = new ReservationAdapter(this, reservations);
        recyclerView.setAdapter(adapter);
    }

    private void setImgListeners (){
        rightClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changableCalendar.add(Calendar.DATE, 1);
                Date today2 = changableCalendar.getTime();

                // Creates datepattern for todays date
                String pattern = "yyyy-MM-dd";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String today = simpleDateFormat.format(today2);
                todaysDate.setText(today);
            }
        });

        leftClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changableCalendar.add(Calendar.DATE, -1);
                String pattern = "yyyy-MM-dd";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                Date today2 = changableCalendar.getTime();
                String today = simpleDateFormat.format(today2);

                if (today2.compareTo(constantDate) == -1){
                    System.out.println("Can't go further back");
                    changableCalendar =  Calendar.getInstance();
                }
                else{
                    todaysDate.setText(today);
                }


            }
        });
    }

    private void getAvailability()
    {
        //create list containing all times for a day
        final ArrayList<String> daySchedule = timeList();

        //new request url to get data from specific room
        String requestUrl = url + "room/" + room_name;
        System.out.println("requestUrl: " + requestUrl);
        // Request a json response from the provided URL
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, requestUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            //list that will contain all times available for booking
                            ArrayList<String> freeTimesList = new ArrayList<>();
                            //create today's date
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = new Date();
                            String dateToday = formatter.format(date);
                            System.out.println("dateToday: " + dateToday);
                            //test date (change below)
                            dateToday = "2020-04-30";

                            //loops through all available times during a day to find available times
                            for (int i = 0; i < daySchedule.size(); i++) {
                                boolean timeFound = false;

                                //loops through response data
                                for(int j = 0; j < response.length(); j++) {
                                    JSONObject JSONreservation = response.getJSONObject(j);
                                    String startDate = JSONreservation.getString("startDate");
                                    //if reservation is for today
                                    if (startDate.equals(dateToday)) {
                                        //sets start and end time for reservation
                                        String startTime = JSONreservation.getString("startTime");

                                        //example: if 08:00 is booked
                                        if (daySchedule.get(i).equals(startTime)) {
                                            timeFound = true;
                                            freeTimesList.add("startTime");
                                            //get end time for reservation
                                            String endTime = JSONreservation.getString("endTime");
                                            boolean endTimeReached = false;
                                            //loops until end time is found
                                            while(!endTimeReached){
                                                //set i to new time, example: 08:15
                                                i++;
                                                if(daySchedule.get(i).equals(endTime)){
                                                    endTimeReached = true;
                                                    //change i back
                                                    i--;
                                                }else{
                                                    freeTimesList.add("Time booked");
                                                }
                                            }
                                        }
                                    }
                                }
                                if(!timeFound){
                                    //time is added to freeTimesList
                                    freeTimesList.add(daySchedule.get(i));
                                }
                            }

                            //size-1 because no need to check 19:45 - 20:00 since it can't be booked
                            for(int i = 0; i < daySchedule.size()-1; i++) {
                                if(daySchedule.get(i).equals(freeTimesList.get(i)) && daySchedule.get(i+1).equals(freeTimesList.get(i+1))){
                                    Reservation fillerReservation = new Reservation();
                                    fillerReservation.setStartTime("free");
                                    reservations.add(fillerReservation);
                                    //skips ahead by one
                                    i++;
                                }
                                else {
                                    for (int j = 0; j < response.length(); j++) {
                                        JSONObject JSONreservation = response.getJSONObject(j);
                                        //parameters needed: int id, String starttime, String startdate, String endtime, String enddate, String[] columns

                                        //create required variables for each reservation object
                                        int reservationId = Integer.parseInt(JSONreservation.getString("id"));
                                        String startTime = JSONreservation.getString("startTime");
                                        String startDate = JSONreservation.getString("startDate");
                                        String endTime = JSONreservation.getString("endTime");
                                        String endDate = JSONreservation.getString("endDate");
                                        JSONArray reservationColumns = JSONreservation.getJSONArray("columns");
                                        JSONArray reservationNames = JSONreservation.getJSONArray("name");

                                        //if selected date is the same as reservation date
                                        if(dateToday.equals(startDate)) {
                                            if (daySchedule.get(i).equals(startTime)) {
                                                //create reservation object and add to reservations (list)
                                                Reservation reservation = new Reservation(reservationId, startTime, startDate, endTime, endDate,
                                                        toStringArray(reservationColumns));
                                                reservation.setName(toStringArray(reservationNames));
                                                reservations.add(reservation);

                                                boolean resEndTimeReached = false;
                                                while (!resEndTimeReached) {
                                                    if (!daySchedule.get(i + 1).equals(endTime) && !daySchedule.get(i+2).equals(endTime)) {
                                                        Reservation fillerReservation = new Reservation();
                                                        fillerReservation.setStartTime("booked");
                                                        reservations.add(fillerReservation);
                                                        System.out.println("fillerReservation added: booked");
                                                        i += 2;
                                                    }else{
                                                        resEndTimeReached = true;
                                                        i--;
                                                    }
                                                }
                                                System.out.println("Reservation added: " + reservation.toString());
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("something wrong..");
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Volleyerror: " + error.toString());
            }
        });

        // Add the request to the RequestQueue.
        AppController.getmInstance().addToRequestQueue(jsonRequest);
        //queue.add(jsonRequest);

    }

    private void setDatePicker() {
        todaysDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ShowDayActivityPerHour.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getDatePicker().setMinDate(constantDate.getTime());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                if (month<10 && day<10 ){

                    String date = year + "-0" + month + "-0" + day;
                    todaysDate.setText(date);
                }
                else if (day<10) {

                    String date = year + "-" + month + "-0" + day;
                    todaysDate.setText(date);
                }
                else if (month<10) {

                    String date = year + "-0" + month + "-" + day;
                    todaysDate.setText(date);
                }

                else
                { String date = year + "-" + month + "-" + day;
                    todaysDate.setText(date);}

            }
        };
    }

}