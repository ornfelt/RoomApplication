package com.joor.roomapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.joor.roomapplication.R;
import com.joor.roomapplication.adapters.FirstReservationsAdapter;
import com.joor.roomapplication.controllers.AppController;
import com.joor.roomapplication.models.Reservation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ShowFirstAvailableActivity extends AppCompatActivity {

    public static String DATE_EXTRA;
    public static String ROOMNAMES_EXTRA;
    private String room_name;
    private RecyclerView recyclerView;
    private static final String url = "https://timeeditrestapi.herokuapp.com/reservations/";
    private List<Reservation> reservations = new ArrayList<Reservation>();
    private FirstReservationsAdapter adapter;

    private TextView roomName;
    private TextView todaysDate;
    private ImageView rightClick;
    private ImageView leftClick;
    private Date constantDate;
    private Date changableDate;
    private Calendar constantCalendar;
    private Calendar changableCalendar;
    private String dateToday;
    private String selectedDate;
    private String[] roomNames;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private boolean datePicked;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private DisplayMetrics displayMetrics;
    private String targetTime1, targetTime2, targetTime3, targetTime4;
    private int indexOfTimeNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_first_available);

        bindViews();
        //init view elements and checks if there's a saved instance
        setViewElements(savedInstanceState);

        try {
            setDate(selectedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Set up imagelisteners
        setImgListeners();
        setDatePicker();
        scaleAccordingToResolution();
        // Set up the adapter
        setAdapter();
        // Gets the availability for a specific room
        getAvailability(changableDate);
    }

    //gets id for view
    private void bindViews() {
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void scaleAccordingToResolution(){

        //get display width and height
        displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int dWidth = displayMetrics.widthPixels;
        int dHeight = displayMetrics.heightPixels;
        System.out.println("width for device: " + dWidth + ", height: " + dHeight);

        //this should fix layout for devices with similar resolution as Galaxy Nexus (720p)
        if(dHeight <= 1200) {

            ViewGroup.MarginLayoutParams textRoomParams = (ViewGroup.MarginLayoutParams) roomName.getLayoutParams();
            //textRoomParams.topMargin = 5;
            roomName.setLayoutParams(textRoomParams);

            ViewGroup.MarginLayoutParams textDateParams = (ViewGroup.MarginLayoutParams) todaysDate.getLayoutParams();
            todaysDate.setLayoutParams(textDateParams);

            //this seems to fix correctly
            ViewGroup.MarginLayoutParams recyclerParams = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
            recyclerParams.topMargin = 50;
            recyclerView.setLayoutParams(recyclerParams);
        }
    }

    //when user navigates back
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),
                MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

    private void initValues() {
        // Getting Room name and Date From Intent.
        getIntents();
        // Date and Room
        todaysDate = findViewById(R.id.date);
        roomName = findViewById(R.id.txtRoomName);
        // Clicklisteners
        rightClick = (ImageView) findViewById(R.id.rightClick);
        leftClick = (ImageView) findViewById(R.id.leftClick);

        room_name = "first available";
        roomName.setText(room_name);
        setDateTextView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //saves when onDestroyed
        outState.putStringArray("roomNames", roomNames);
        outState.putString("selectedDate", selectedDate);
    }

    private void initValuesFromSavedState(Bundle savedInstanceState) throws IOException {
        roomNames = savedInstanceState.getStringArray("roomNames");
        selectedDate = savedInstanceState.getString("selectedDate");
    }

    private ArrayList<String> timeList() {
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

    private void setViewElements(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            initValues();
        } else {
            try {
                initValuesFromSavedState(savedInstanceState);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setDate(String date) throws ParseException {
        System.out.println("Begärt datum är"+ date);
        Date d1 = formatter.parse(date);
        changableCalendar = Calendar.getInstance();
        changableCalendar.setTime(d1);
        changableDate = changableCalendar.getTime();

        constantCalendar = Calendar.getInstance();
        constantDate = constantCalendar.getTime();
        datePicked = false;
    }

    private void setAdapter() {
        //creates RecycleAdapter and sets it
        adapter = new FirstReservationsAdapter(this, reservations);
        recyclerView.setAdapter(adapter);
    }

    private void setImgListeners() {
        rightClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date clickedDate;

                if (datePicked == true) {
                    changableCalendar.setTime(changableDate);
                    changableCalendar.add(Calendar.DATE, 1);
                    clickedDate = changableCalendar.getTime();
                    datePicked = false;
                } else
                {
                    changableCalendar.add(Calendar.DATE, 1);
                    clickedDate = changableCalendar.getTime();
                }

                selectedDate= formatter.format(clickedDate);
                updateView();

            }
        });

        leftClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (datePicked == true) {
                    changableCalendar.setTime(changableDate);
                    datePicked = false;
                }

                changableCalendar.add(Calendar.DATE, -1);
                Date clickedDate = changableCalendar.getTime();
                // Checks if wanted date is before today's date.
                if (clickedDate.before(constantDate)) {
                    System.out.println("Can't go further back");
                    changableCalendar = Calendar.getInstance();
                    clickedDate = changableCalendar.getTime();
                    selectedDate = formatter.format(clickedDate);
                    updateView();
                } else {
                    // if wanted date is not before today then set's date to
                    selectedDate= formatter.format(clickedDate);
                    updateView();
                }


            }
        });
    }

    private String getCurrentTimeStamp(){
        String dateTimeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        String[] dateTimeSplit = dateTimeNow.split(" ");
        //return time only (hour and min)
        return dateTimeSplit[1];
    }

    //adjust time string to match booking schedule, example: if timeNow is 20:30, then set time to 08:00
    private String adjustTimeStamp(String t){
        //incoming t should be formatted as: HH:mm
        String[] tSplit = t.split(":");
        String completeHour = tSplit[0];
        String[] hourSplit = completeHour.split("");

        int hour1;
        int hour2;
        //split hour, example: split 08 to 0 and 8
        if(hourSplit.length == 3){
            hour1 = Integer.parseInt(hourSplit[1]);
            hour2 = Integer.parseInt(hourSplit[2]);
        }else{
            hour1 = Integer.parseInt(hourSplit[0]);
            hour2 = Integer.parseInt(hourSplit[1]);
        }

        String completeMin = tSplit[1];
        int min = Integer.parseInt(tSplit[1]);
        String adjustedTime = "";

        //if current hour is >= 20 or less than 08
        if(Integer.parseInt(completeHour) >= 20){
            //set hour to 08
            completeHour = "08";

            // get a calendar instance, which defaults to "now"
            Calendar calendar = Calendar.getInstance();
            // add one day to the date/calendar
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            // now get "tomorrow"
            Date tomorrow = calendar.getTime();
            //TODO: control so that tomorrow.toString() doesnt contain sat or sun (not weekend), if that's the case, skip ahead till monday
            dateToday = formatter.format(tomorrow);

        }else if(hour1 == 0 && hour2 < 8){
            completeHour = "08";
        }

        //if min is 0, 15, 30, 45 then no need to adjust
        if(min == 0 || min == 15 || min == 30 || min == 45){}
        //else adjust to closest quarter value
        else{
            if(min < 15 ){
                completeMin = "15";
            }else if(min < 30){
                completeMin = "30";
            }else if(min < 45){
                completeMin = "45";
            } //else means minute is >= 45, then hour needs adjustment as well
            else{
                completeMin = "00";
                if(hour1 == 0 && hour2 == 8){
                    completeHour = "09";
                }else if(hour1 == 0 && hour2 == 9){
                    completeHour = "10";
                }//else hour is >= 10, then add 1 unless hour is 19, then set next day
                else{
                    if(!completeHour.equals("19")){
                        completeHour = Integer.toString((Integer.parseInt(completeHour))+1);
                    }else{
                        //recurse
                        adjustedTime = adjustTimeStamp("20:00");
                        return adjustedTime;
                    }
                }
            }
        }

        //set and return adjusted time
        adjustedTime = completeHour + ":" +  completeMin;
        return adjustedTime;
    }

    //compares two time strings and returns true if t1 is earlier than, or equal to t2
    private boolean isFirstTimeEarlier(String t1, String t2){
        //incoming time strings should be formatted as: HH:mm
        String[] t1Split = t1.split(":");
        int t1Hour = Integer.parseInt(t1Split[0]);
        int t1Min = Integer.parseInt(t1Split[1]);

        String[] t2Split = t2.split(":");
        int t2Hour = Integer.parseInt(t2Split[0]);
        int t2Min = Integer.parseInt(t2Split[1]);

        //return true if example: t1: 08:10 and t2: 09:00
        if(t1Hour < t2Hour){
            return true;
        } //return true if example: t1: 08:30 and t2: 08:35
        else if(t1Hour == t2Hour && t2Min >= t1Min){
            return true;
        } //return false otherwise
        else{
            return false;
        }
    }

    //TODO: get availability for first available booking time
    private void getAvailability(final Date date) {
        //create list containing all times for a day
        final ArrayList<String> daySchedule = timeList();
        //create today's date
        dateToday = formatter.format(date);
        String timeNow = getCurrentTimeStamp();
        timeNow = "19:50";
        System.out.println("date and time before adjust: " + dateToday +", " + timeNow);
        timeNow = adjustTimeStamp(timeNow);
        indexOfTimeNow = 0;
        System.out.println("dateToday: " + dateToday + ", adjusted timeNow: " + timeNow);

        for (String s: daySchedule) {
            if(s.equals(timeNow)){
                indexOfTimeNow = daySchedule.indexOf(s);
                System.out.println("index of time now found at: " + indexOfTimeNow);
            }
        }

                //String requestUrl = url + "room/" + room;
                String requestUrl = url;
                System.out.println("requestUrl: " + requestUrl);
                // Request a json response from the provided URL
                JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, requestUrl, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    //variable for reservations cap. Can be extended to fix layout in recyclerview.
                                    int reservationsSizeCap = 24;
                                    //loop through every half hour to find free times (most likely won't loop through it all since it'll probably break before end)
                                    mainloop:
                                    for(int t = indexOfTimeNow; t < daySchedule.size()-1; t++) {

                                        //set string targetTime1: example: 08:15, and targetTime2: example: 08:30 etc...
                                        targetTime1 = daySchedule.get(t);

                                        try {
                                            if (t + 1 <= daySchedule.size() - 1) {
                                                targetTime2 = daySchedule.get(t + 1);
                                            } else {
                                                targetTime2 = null;
                                            }
                                            t++;

                                            if (t + 1 <= daySchedule.size() - 1) {
                                                targetTime3 = daySchedule.get(t + 1);
                                            } else {
                                                targetTime3 = null;
                                            }
                                            t++;
                                            if (t + 1 <= daySchedule.size() - 1) {
                                                targetTime4 = daySchedule.get(t + 1);
                                            } else {
                                                targetTime4 = null;
                                            }
                                            t++;
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }

                                        //loop through all rooms to see if target times are available
                                        for (final String room : roomNames) {
                                            //list that will contain all times available for booking
                                            ArrayList<String> freeTimesList = new ArrayList<>();
                                            //list that will contain all startimes for bookings (needed for recyclerview)
                                            ArrayList<String> bookedStartTimes = new ArrayList<>();

                                            //loops through all available times during a day to find available times
                                            for (int i = 0; i < daySchedule.size(); i++) {
                                                boolean timeFound = false;

                                                //loops through response data
                                                for (int j = 0; j < response.length(); j++) {
                                                    JSONObject JSONreservation = response.getJSONObject(j);
                                                    String startDate = JSONreservation.getString("startDate");
                                                    String roomNames = JSONreservation.getString("name");

                                                    //split based on quotation mark
                                                    String[] roomNamesSplit = roomNames.split("\"");
                                                    boolean roomFound = false;
                                                    for(String s : roomNamesSplit){
                                                        if(s.equals(room)){
                                                            roomFound = true;
                                                        }
                                                    }

                                                    //if reservation is for today
                                                    if (startDate.equals(dateToday) && roomFound) {
                                                        //sets start and end time for reservation
                                                        String startTime = JSONreservation.getString("startTime");
                                                        bookedStartTimes.add(startTime);

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

                                            //loop through available times for a specific room to see if targeted times are free
                                            for (int i = 0; i < freeTimesList.size() - 1; i++) {
                                                //check target times and add to list if they are free
                                                if (targetTime2 != null && targetTime1.equals(freeTimesList.get(i)) && targetTime2.equals(freeTimesList.get(i+1))) {
                                                    Reservation fillerReservation = new Reservation();
                                                    fillerReservation.setStartTime(targetTime1);
                                                    //add end time if there's a booking close to time (needed for recyclerview)
                                                    if(bookedStartTimes.size() > 0){
                                                        for (String time: bookedStartTimes) {
                                                            if(isFirstTimeEarlier(targetTime2, time)){
                                                                fillerReservation.setEndTime(time);
                                                                System.out.println("endTime added for fillerRes: " + time);
                                                                bookedStartTimes.remove(time);
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    //add room name
                                                    String[] roomArr = {room};
                                                    fillerReservation.setName(roomArr);
                                                    //check reservations size before adding new
                                                    if (reservations.size() < reservationsSizeCap) {
                                                        reservations.add(fillerReservation);
                                                        //System.out.println("added reservation: " + fillerReservation.getStartTime());
                                                    }
                                                    //else nothing happens - can't break main loop within jsonrequest

                                                    //if first half hour is free, then also check next half hour
                                                    if (i + 2 != freeTimesList.size() && targetTime3 != null && targetTime4 != null
                                                            && targetTime3.equals(freeTimesList.get(i + 2)) && targetTime4.equals(freeTimesList.get(i + 3))) {
                                                        Reservation fillerReservation2 = new Reservation();
                                                        fillerReservation2.setStartTime(targetTime3);
                                                        //add end time if there's a booking close to time (needed for recyclerview)
                                                        if(bookedStartTimes.size() > 0){
                                                            for (String time: bookedStartTimes) {
                                                                if(isFirstTimeEarlier(targetTime4, time)){
                                                                    fillerReservation.setEndTime(time);
                                                                    System.out.println("endTime added for fillerRes: " + time);
                                                                    bookedStartTimes.remove(time);
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        //add room name
                                                        fillerReservation2.setName(roomArr);
                                                        //check reservations size before adding new
                                                        if (reservations.size() < reservationsSizeCap) {
                                                            reservations.add(fillerReservation2);
                                                            //System.out.println("added reservation: " + fillerReservation2.getStartTime());
                                                        }
                                                    }
                                                    /*else{
                                                        Reservation fillerReservation3 = new Reservation();
                                                        fillerReservation3.setStartTime("skip");
                                                        fillerReservation3.setName(roomArr);
                                                        if (reservations.size() < reservationsSizeCap) {
                                                            reservations.add(fillerReservation3);
                                                            reservationsSizeCap++;
                                                        }
                                                    }
                                                     */
                                                    //break after targetTimes are found
                                                    break;
                                                }
                                            }
                                        }
                                        if(reservations.size() == reservationsSizeCap){
                                            System.out.println("Breaking mainloop");
                                            break mainloop;
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
                int year = changableCalendar.get(Calendar.YEAR);
                int month = changableCalendar.get(Calendar.MONTH);
                int day = changableCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ShowFirstAvailableActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getDatePicker().setMinDate(constantDate.getTime());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                datePicked = true;

                if (month < 10 && day < 10) {
                    selectedDate = year + "-0" + month + "-0" + day;
                    editDate(selectedDate);
                    updateView();
                } else if (day < 10) {
                    selectedDate = year + "-" + month + "-0" + day;
                    editDate(selectedDate);
                    updateView();

                } else if (month < 10) {
                    selectedDate = year + "-0" + month + "-" + day;
                    editDate(selectedDate);
                    updateView();

                } else {
                    selectedDate = year + "-" + month + "-" + day;
                    editDate(selectedDate);
                    updateView();
                }
            }

        };
    }

    private void editDate(String date) {
        try {
            changableDate = formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void setDateTextView(){
        Calendar dateChecker = Calendar.getInstance();
        dateToday = formatter.format(dateChecker.getTime());
        dateChecker.add(Calendar.DATE, 1);
        String dateTomorrow = formatter.format(dateChecker.getTime());

        if (dateToday.equals(selectedDate)) {
            todaysDate.setText("Today");
        } else if (selectedDate.equals(dateTomorrow)) {
            todaysDate.setText("Tomorrow");
        } else
            todaysDate.setText(selectedDate);
    }

    //get values from intent
    private void getIntents(){
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        selectedDate = extras.getString(DATE_EXTRA);
        roomNames = extras.getStringArray(ROOMNAMES_EXTRA);
        final String [] safetyString = {"C11", "C13", "C15", "Flundran", "Rauken", "Änget", "Backsippan", "Heden", "Myren"};
        roomNames = safetyString;
        System.out.println("tried to get roomNames " + roomNames[0]);
        for(String s : roomNames){
            System.out.println(s);
        }
    }

    private void updateView(){
        Intent i= new Intent(ShowFirstAvailableActivity.this,ShowFirstAvailableActivity.class);
        Bundle extras = new Bundle();
        System.out.println("UpdateView date is " + selectedDate);
        extras.putString(DATE_EXTRA, selectedDate);
        extras.putStringArray(ROOMNAMES_EXTRA, roomNames);
        i.putExtras(extras);
        // Hides the transition between intents
        startActivity(i);
        overridePendingTransition( 0, 0);
        overridePendingTransition( 0, 0);
    }
}
