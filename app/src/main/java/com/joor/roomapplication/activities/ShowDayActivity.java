package com.joor.roomapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

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
import java.util.Date;
import java.util.List;

public class ShowDayActivity extends AppCompatActivity {

    public static String INTENT_MESSAGE_KEY = "ROOM_NAME";
    private String room_name;
    private RecyclerView recyclerView;
    private static final String url = "https://timeeditrestapi.herokuapp.com/reservations/";
    private List<Reservation> reservations = new ArrayList<Reservation>();
    private ReservationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_day);

        //init view elements and checks if there's a saved instance
        bindViews();
        if(savedInstanceState == null) {
            initValuesFromIntent();
        }else{
            try {
                initValuesFromSavedState(savedInstanceState);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //creates RecycleAdapter and sets it
        adapter = new ReservationAdapter(this, reservations);
        recyclerView.setAdapter(adapter);

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
                            ArrayList<String> freeTimesList = new ArrayList<>();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = new Date();
                            String dateToday = formatter.format(date);
                            System.out.println("dateToday: " + dateToday);
                            //test date
                            dateToday = "2020-04-24";

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
                                            freeTimesList.add("Time booked");
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
                                    System.out.println("fillerReservation added: free");
                                }else {
                                    for (int j = 0; j < response.length(); j++) {
                                        JSONObject JSONreservation = response.getJSONObject(j);
                                        //int id, String starttime, String startdate, String endtime, String enddate, String[] columns

                                        //required variables for each reservation object
                                        int reservationId = Integer.parseInt(JSONreservation.getString("id"));
                                        String startTime = JSONreservation.getString("startTime");
                                        String startDate = JSONreservation.getString("startDate");
                                        String endTime = JSONreservation.getString("endTime");
                                        String endDate = JSONreservation.getString("endDate");
                                        JSONArray reservationColumns = JSONreservation.getJSONArray("columns");
                                        JSONArray reservationNames = JSONreservation.getJSONArray("name");

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
                            //reservations = makeCompleteDaySchedule();
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

    private List<Reservation> makeCompleteDaySchedule(){
        List<Reservation> reservationSchedule = new ArrayList<>();

        //checks every hour between 8-20 and makes sure the new list contains all hours
        for (int hourCount = 8; hourCount < 21; hourCount++) {
            boolean hourFound = false;

            for (Reservation r : reservations) {
                //gets reservation hours
                String startHour = r.getStartTime().split(":")[0];
                String endHour = r.getEndTime().split(":")[0];

                //if reservation is at hourCount
                if(Character.getNumericValue(startHour.charAt(1)) == hourCount || Integer.parseInt(startHour) == hourCount){
                    //if reservation covers less than one hour
                    if (startHour.equals(endHour)) {
                        reservationSchedule.add(r);
                        hourFound = true;
                    }else{
                        //checks if time is 08:00, 09:00 or double values (>= 10:00)
                        boolean startPastTen = false;
                        boolean endPastTen = false;

                        if(Character.getNumericValue(Character.getNumericValue(startHour.charAt(0))) == 1){
                            startPastTen = true;
                        }
                        if(Character.getNumericValue(Character.getNumericValue(endHour.charAt(0))) == 1){
                            endPastTen = true;
                        }
                        //if both booleans are true then hourSpan is always 1
                        int hourSpan = 1;

                        if(startPastTen && endPastTen) {
                            //clarifying example: 20 - 14 = 6
                            hourSpan = Integer.parseInt(endHour) - Integer.parseInt(startHour);
                        }else if(!startPastTen && endPastTen){
                            //clarifying example: 20 - 08 = 12
                            hourSpan = Integer.parseInt(endHour) - Character.getNumericValue(startHour.charAt(1));
                        }
                        for(int i = 0; i < hourSpan; i++){
                            if(i == 0){
                                reservationSchedule.add(r);
                            }else {
                                //adds filler reservations for booked time span
                                Reservation fillerReservation = new Reservation();
                                fillerReservation.setStartTime("booked");
                                reservationSchedule.add(fillerReservation);
                            }
                            hourCount++;
                            hourFound = true;
                        }
                    }
                }
            }
            //if a specific hour is not found, a filler reservation is added
            if(!hourFound){
                Reservation fillerReservation = new Reservation();
                fillerReservation.setStartTime("free");
                reservationSchedule.add(fillerReservation);
            }
        }

        //sout-test for complete schedule
        for (Reservation res : reservationSchedule) {
            System.out.println("reservationSchedule: " + res.getStartTime());
        }
        return reservationSchedule;
    }


}