package com.joor.roomapplication.activities;

import android.app.Activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.joor.roomapplication.R;
import com.joor.roomapplication.activities.ShowDayActivitySchedule;
import com.joor.roomapplication.adapters.ReservationAdapter;
import com.joor.roomapplication.controllers.AppController;
import com.joor.roomapplication.models.Reservation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.joor.roomapplication.activities.ShowDayActivitySchedule.toStringArray;

/**
 * This class shows availability for a specific day by making a request via volley.
 * @author Jonas Ornfelt & Daniel Arnesson
 */
public class DayActivityRequest {

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private List<Reservation> reservations = new ArrayList<Reservation>();
    private ReservationAdapter adapter;
    private RecyclerView recyclerView;
    String room_name;
    Date date;
    String url;
    Activity activity;


    public DayActivityRequest (final Date date, String room_name, String url, Activity activity){
        this.date = date;
        this.room_name = room_name;
        this.url = url;
        this.activity = activity;
    }

    public void startRequest (){
        bindViews();
        setAdapter();
        getAvailability();

    }

    //gets id for view
    private void bindViews() {
        recyclerView = activity.findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
    }

    private void setAdapter() {
        //creates RecycleAdapter and sets it
        adapter = new ReservationAdapter(activity, reservations, room_name);
        recyclerView.setAdapter(adapter);
    }

    //TODO: the following method is used in both requests and could be separated into a utility class perhaps
    public ArrayList<String> timeList() {

        // Set up the adapter
        setAdapter();

        ArrayList<String> availableTimesList = new ArrayList<>();
        String hour = "8";
        String full = "00";
        String quarter = "15";
        String half = "30";
        String threeQuarter = "45";

        for (int i = 0; i < 12; i++) {
            if (Integer.parseInt(hour) < 10) {
                availableTimesList.add("0" + hour + ":" + full);
                availableTimesList.add("0" + hour + ":" + quarter);
                availableTimesList.add("0" + hour + ":" + half);
                availableTimesList.add("0" + hour + ":" + threeQuarter);
            } else {
                availableTimesList.add(hour + ":" + full);
                availableTimesList.add(hour + ":" + quarter);
                availableTimesList.add(hour + ":" + half);
                availableTimesList.add(hour + ":" + threeQuarter);
            }
            if (i == 11) availableTimesList.add("20:00");
            hour = Integer.toString(Integer.parseInt(hour) + 1);
        }

        return availableTimesList;
    }

    private void getAvailability() {
        //create list containing all times for a day
        final ArrayList<String> daySchedule = timeList();

        //spelling fix for low level API's
        if(room_name.toLowerCase().equals("änget")){
            room_name = "anget";
        }
        //new request url to get data from specific room
        String requestUrl = url;

        //change back to correct room name after requestUrl is set
        if(room_name.equals("anget")){
            room_name = "Änget";
        }
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
                            //Date date = new Date();
                            String dateToday = formatter.format(date);
                            System.out.println("Selected date is: " + dateToday);
                            //System.out.println("dateToday: " + dateToday);
                            //test date (change below)
                            //dateToday = "2020-04-30";

                            //loops through all available times during a day to find available times
                            for (int i = 0; i < daySchedule.size(); i++) {
                                boolean timeFound = false;

                                //loops through response data
                                for (int j = 0; j < response.length(); j++) {
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
                                            while (!endTimeReached) {
                                                //set i to new time, example: 08:15
                                                i++;
                                                if (daySchedule.get(i).equals(endTime)) {
                                                    endTimeReached = true;
                                                    //change i back
                                                    i--;
                                                } else {
                                                    freeTimesList.add("Time booked");
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!timeFound) {
                                    //time is added to freeTimesList
                                    freeTimesList.add(daySchedule.get(i));
                                }
                            }

                            //size-1 because no need to check 19:45 - 20:00 since it can't be booked
                            for (int i = 0; i < daySchedule.size() - 1; i++) {
                                if (daySchedule.get(i).equals(freeTimesList.get(i)) && daySchedule.get(i + 1).equals(freeTimesList.get(i + 1))) {
                                    Reservation fillerReservation = new Reservation();
                                    fillerReservation.setStartTime("free");
                                    reservations.add(fillerReservation);
                                    //skips ahead by one
                                    i++;
                                } else {
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
                                        if (dateToday.equals(startDate)) {
                                            if (daySchedule.get(i).equals(startTime)) {
                                                //create reservation object and add to reservations (list)
                                                Reservation reservation = new Reservation(reservationId, startTime, startDate, endTime, endDate,
                                                        toStringArray(reservationColumns));
                                                reservation.setName(toStringArray(reservationNames));
                                                reservations.add(reservation);

                                                boolean resEndTimeReached = false;
                                                while (!resEndTimeReached) {
                                                    if (!daySchedule.get(i + 1).equals(endTime) && !daySchedule.get(i + 2).equals(endTime)) {
                                                        Reservation fillerReservation = new Reservation();
                                                        fillerReservation.setStartTime("booked");
                                                        reservations.add(fillerReservation);
                                                        System.out.println("fillerReservation added: booked");
                                                        i += 2;
                                                    } else {
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
}
