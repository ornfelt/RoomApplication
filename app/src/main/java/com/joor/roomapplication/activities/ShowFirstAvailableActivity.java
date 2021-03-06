package com.joor.roomapplication.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.ConfigurationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.joor.roomapplication.R;
import com.joor.roomapplication.adapters.FirstReservationsAdapter;
import com.joor.roomapplication.controllers.AppController;
import com.joor.roomapplication.models.Reservation;
import com.joor.roomapplication.utility.ShowAmountValues;

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

    public static String VALUES_EXTRA;
    private String room_name;
    private RecyclerView recyclerView;
    private static final String url = "https://timeeditrestapi.herokuapp.com/reservations/";
    private List<Reservation> reservations = new ArrayList<Reservation>();
    private FirstReservationsAdapter adapter;

    private TextView roomName;
    private TextView todaysDate;
    private ImageView rightClick;
    private ImageView leftClick;
    private ImageView infoClick;
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
    private String timeNow;
    private String targetTime1, targetTime2, targetTime3, targetTime4;
    private int indexOfTimeNow;
    private int showMoreAmount;
    private int showLessAmount;
    private int dayCount;
    private boolean isFirstResult;
    private boolean dayCountWasAdded = false;
    Dialog roomInfoDialog;

    private static final String TAG = "Swipe Position";
    private float x1, x2, y1, y2;
    private static int MIN_DISTANCE = 100;

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
        scaleAccordingToResolution();
        // Set up the adapter
        setAdapter();
        // Gets the availability for a specific room
        getAvailability(changableDate);
/*
        if(showMoreAmount == 0 && dayCount == 0){
            //show info toast
            Toast toast = Toast.makeText(getApplicationContext(), "Swipe up/down to see more/previous.", Toast.LENGTH_LONG);
            toast.show();

            //wait 2s for second toast
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast toast= Toast.makeText(getApplicationContext(),
                            "Swipe sideways to\nchange selected day.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT, 0, 0);
                    toast.show();
                }
            }, 2000);
        }*/

        // Implementation of custom Actionbar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
    }

    //gets id for view
    private void bindViews() {
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void scaleAccordingToResolution() {
        //get display width and height
        displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int dWidth = displayMetrics.widthPixels;
        int dHeight = displayMetrics.heightPixels;
        int dDensityPerInch = displayMetrics.densityDpi;
        System.out.println("width for device: " + dWidth + ", height: " + dHeight + " density: " + dDensityPerInch);
        Configuration configuration = this.getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
        int screenHeightDp = configuration.screenHeightDp;
        System.out.println("Usable width in this activity: " + screenWidthDp + ", usable height: " + screenHeightDp);

        //this should fix layout for devices with similar resolution as Galaxy Nexus (720p)
        if (dHeight <= 1200) {
            System.out.println("dHeight <= 1200 ");
            ViewGroup.MarginLayoutParams textRoomParams = (ViewGroup.MarginLayoutParams) roomName.getLayoutParams();
            //textRoomParams.topMargin = 5;
            roomName.setLayoutParams(textRoomParams);

            ViewGroup.MarginLayoutParams textDateParams = (ViewGroup.MarginLayoutParams) todaysDate.getLayoutParams();
            todaysDate.setLayoutParams(textDateParams);

            //this seems to fix for dHeight <= 1200
            ViewGroup.MarginLayoutParams recyclerParams = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
            recyclerParams.topMargin = 5;
            recyclerView.setLayoutParams(recyclerParams);
        }
    }

    //when user navigates back
    @Override
    public void onBackPressed() {
        ShowAmountValues showAmountValues = ShowAmountValues.getInstance();
        showAmountValues.resetShowAmountList();
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
        infoClick = (ImageView) findViewById(R.id.buttonShowAvailableViewInfo);

        setDateTextView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //saves when onDestroyed
        outState.putStringArray("roomNames", roomNames);
        outState.putString("selectedDate", selectedDate);
        outState.putInt("showMoreAmount", showMoreAmount);
        outState.putInt("showLessAmount", showLessAmount);
        outState.putInt("dayCount", dayCount);
    }

    private void initValuesFromSavedState(Bundle savedInstanceState) throws IOException {
        roomNames = savedInstanceState.getStringArray("roomNames");
        selectedDate = savedInstanceState.getString("selectedDate");
        showMoreAmount = savedInstanceState.getInt("showMoreAmount");
        showLessAmount = savedInstanceState.getInt("showLessAmount");
        dayCount = savedInstanceState.getInt("dayCount");
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
                dayCount++;
                showMoreAmount = 0;
                ShowAmountValues showAmountValues = ShowAmountValues.getInstance();
                showAmountValues.resetShowAmountList();
                updateView();
            }
        });

        leftClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //adjust dayCount if it was added in current view
                if (dayCountWasAdded) {
                    dayCount--;
                }
                ShowAmountValues showAmountValues = ShowAmountValues.getInstance();
                if (dayCount != 0) {
                    dayCount--;
                    showMoreAmount = 0;
                    showAmountValues.resetShowAmountList();
                    updateView();
                } else {
                    // Toast toast = Toast.makeText(getApplicationContext(), "Can't go further back", Toast.LENGTH_SHORT);
                    // toast.show();
                }
            }
        });

        roomInfoDialog = new Dialog(ShowFirstAvailableActivity.this);
        infoClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //set new content view
                roomInfoDialog.setContentView(R.layout.room_popup);

                //init image and text view inside popup
                final ImageView closeInfoPopup = roomInfoDialog.findViewById(R.id.imgCloseInfoPopup);
                final TextView textViewTitle = roomInfoDialog.findViewById(R.id.textRoomName);
                final ImageView roomImage = roomInfoDialog.findViewById(R.id.imgRoom);
                final TextView textViewViewInfo = roomInfoDialog.findViewById(R.id.textRoomInfo);
                textViewViewInfo.setGravity(Gravity.LEFT);
                String svTitleText = "Första Lediga";
                String engTitleText = "First Available";

                String svViewInfo =
                        "Swipe upp/ner visar fler lediga tider samma dag" + "\n" + "\n" +
                                "Swipe höger visar nästa dag" + "\n" + "\n" +
                                "Swipe vänster visar förgående dag" + "\n" + "\n" +
                                "Klick på datum öppnar kalendern " + "\n" + "\n" +
                                "Klick på ledig tid öppnar inloggning till TimeEdit";

                String engViewInfo = "Swipe up/down displays more available times for same day " + "\n" + "\n" +
                        "Swipe right displays next day" + "\n" + "\n" +
                        "Swipe left displays previous day" + "\n" + "\n" +
                        "Click on date opens the calendar " + "\n" + "\n" +
                        "Click on available time opens TimeEdit login ";


                String language = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0).toString();
                if (language.equals("sv_SE")) {
                    textViewTitle.setText(svTitleText);
                    textViewViewInfo.setText(svViewInfo);
                } else {
                    textViewTitle.setText(engTitleText);
                    textViewViewInfo.setText(engViewInfo);
                }

                closeInfoPopup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        roomInfoDialog.dismiss();
                    }
                });
                roomInfoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                roomInfoDialog.show();

            }
        });
    }

    private String getCurrentTimeStamp() {
        String dateTimeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        String[] dateTimeSplit = dateTimeNow.split(" ");
        //return time only (hour and min)
        return dateTimeSplit[1];
    }

    //adjust time string to match booking schedule, example: if timeNow is 20:30, then set time to 08:00
    private String adjustTimeStamp(String t) {
        //incoming t should be formatted as: HH:mm
        String[] tSplit = t.split(":");
        String completeHour = tSplit[0];
        String[] hourSplit = completeHour.split("");

        int hour1;
        int hour2;
        //split hour, example: split 08 to 0 and 8
        if (hourSplit.length == 3) {
            hour1 = Integer.parseInt(hourSplit[1]);
            hour2 = Integer.parseInt(hourSplit[2]);
        } else {
            hour1 = Integer.parseInt(hourSplit[0]);
            hour2 = Integer.parseInt(hourSplit[1]);
        }

        String completeMin = tSplit[1];
        int min = Integer.parseInt(tSplit[1]);
        String adjustedTime = "";

        //if current hour is >= 20
        if (Integer.parseInt(completeHour) >= 20) {

            // get a calendar instance, which defaults to "now"
            Calendar calendar = Calendar.getInstance();
            // add one day to the date/calendar
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            // now get "tomorrow"
            Date tomorrow = calendar.getTime();
            dateToday = formatter.format(tomorrow);
            selectedDate = formatter.format(tomorrow);

            //set and return adjusted time
            adjustedTime = "08:00";
            return adjustedTime;

        } else if (hour1 == 0 && hour2 < 8) {
            //set and return adjusted time
            adjustedTime = "08:00";
            return adjustedTime;
        }

        //if min is 0, 15, 30, 45 then no need to adjust
        if (min == 0 || min == 15 || min == 30 || min == 45) {
        }
        //else adjust to closest quarter value
        else {
            if (min < 15) {
                completeMin = "15";
            } else if (min < 30) {
                completeMin = "30";
            } //if time is > 19:30 go to next day
            else if (min > 30 && completeHour.equals("19")) {
                //recurse
                adjustedTime = adjustTimeStamp("20:00");
                return adjustedTime;
            } else if (min < 45) {
                completeMin = "45";
            }
            //else means minute is >= 45, then hour needs adjustment as well
            else {
                completeMin = "00";
                if (hour1 == 0 && hour2 == 8) {
                    completeHour = "09";
                } else if (hour1 == 0 && hour2 == 9) {
                    completeHour = "10";
                }//else hour is >= 10, then add 1 unless hour is 19, then set next day
                else {
                    if (!completeHour.equals("19")) {
                        completeHour = Integer.toString((Integer.parseInt(completeHour)) + 1);
                    } else {
                        //recurse
                        adjustedTime = adjustTimeStamp("20:00");
                        return adjustedTime;
                    }
                }
            }
        }

        //set and return adjusted time
        adjustedTime = completeHour + ":" + completeMin;
        return adjustedTime;
    }

    //compares two time strings and returns true if t1 is earlier than, or equal to t2
    private boolean isFirstTimeEarlier(String t1, String t2) {
        //incoming time strings should be formatted as: HH:mm
        String[] t1Split = t1.split(":");
        int t1Hour = Integer.parseInt(t1Split[0]);
        int t1Min = Integer.parseInt(t1Split[1]);

        String[] t2Split = t2.split(":");
        int t2Hour = Integer.parseInt(t2Split[0]);
        int t2Min = Integer.parseInt(t2Split[1]);

        //return true if example: t1: 08:10 and t2: 09:00
        if (t1Hour < t2Hour) {
            return true;
        } //return true if example: t1: 08:30 and t2: 08:35
        else if (t1Hour == t2Hour && t2Min >= t1Min) {
            return true;
        } //return false otherwise
        else {
            return false;
        }
    }

    //this method adds to dayCount if necessary, a bit redundant code here...
    private void addToDayCount(String t) {
        //incoming t should be formatted as: HH:mm
        String[] tSplit = t.split(":");
        String completeHour = tSplit[0];
        String[] hourSplit = completeHour.split("");

        int hour1;
        int hour2;
        //split hour, example: split 08 to 0 and 8
        if (hourSplit.length == 3) {
            hour1 = Integer.parseInt(hourSplit[1]);
            hour2 = Integer.parseInt(hourSplit[2]);
        } else {
            hour1 = Integer.parseInt(hourSplit[0]);
            hour2 = Integer.parseInt(hourSplit[1]);
        }

        String completeMin = tSplit[1];
        int min = Integer.parseInt(tSplit[1]);

        //only add if time is > 19:30
        if (min > 30 && completeHour.equals("19")) {
            dayCount++;
        } else if (Integer.parseInt(completeHour) > 19) {
            dayCount++;
        }
    }

    private void getAvailability(final Date date) {
        //create list containing all times for a day
        final ArrayList<String> daySchedule = timeList();
        dateToday = formatter.format(date);
        timeNow = getCurrentTimeStamp();
        //test time
        if(dayCount == 0) {
            timeNow = "10:45";
        }

        //if end of days schedule is reached, then look for next days availability
        if (dayCount > 0) {
            // get a calendar instance, which defaults to "now"
            Calendar calendar = Calendar.getInstance();
            // add one day to the date/calendar
            calendar.add(Calendar.DAY_OF_YEAR, dayCount);
            // now get "tomorrow"
            Date tomorrow = calendar.getTime();
            dateToday = formatter.format(tomorrow);
            selectedDate = formatter.format(tomorrow);
            //get first time of day's schedule if new day
            timeNow = daySchedule.get(0);
            System.out.println("dateToday changed in getAvailability: " + dateToday);
        }

        addToDayCount(timeNow);

        System.out.println("date and time before adjust: " + dateToday + ", " + timeNow);
        timeNow = adjustTimeStamp(timeNow);
        //date may change depending on adjusted time, so date text is set again here
        setDateTextView();
        dateToday = selectedDate;
        indexOfTimeNow = 0;
        System.out.println("dayCount: " + dayCount + ", dateToday: " + dateToday + ", adjusted timeNow: " + timeNow);

        for (String s : daySchedule) {
            if (s.equals(timeNow)) {
                if (showMoreAmount != 0 && showMoreAmount < daySchedule.size()) {
                    //when user clicks on right/left click, the view should show the next/previous set of available times
                    indexOfTimeNow = showMoreAmount;
                    isFirstResult = false;
                } else {
                    System.out.println("indexOfTimeNow found at: " + indexOfTimeNow);
                    isFirstResult = true;
                    indexOfTimeNow = daySchedule.indexOf(s);

                }
            }
        }

        //clear cache before request
        AppController.getmInstance().getmRequestQueue().getCache().clear();

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
                            int reservationsSizeCap = 30;
                            //loop through every half hour to find free times (most likely won't loop through it all since it'll probably break before end)
                            mainloop:
                            for (int t = indexOfTimeNow; t < daySchedule.size() - 1; t++) {

                                //set string targetTime1: example: 08:15, and targetTime2: example: 08:30 etc...
                                targetTime1 = daySchedule.get(t);

                                try {
                                    if (t + 1 <= daySchedule.size() - 1) {
                                        targetTime2 = daySchedule.get(t + 1);
                                    } else {
                                        //targetTime2 = null;
                                        targetTime2 = adjustTimeStamp(daySchedule.get(daySchedule.size() - 1));
                                        //update t
                                        for (String s : daySchedule) {
                                            if (s.equals(targetTime2)) {
                                                t = daySchedule.indexOf(s);
                                            }
                                        }
                                    }
                                    t++;

                                    if (t + 1 <= daySchedule.size() - 1) {
                                        targetTime3 = daySchedule.get(t + 1);
                                    } else {
                                        targetTime3 = adjustTimeStamp(daySchedule.get(daySchedule.size() - 1));
                                        //update t
                                        for (String s : daySchedule) {
                                            if (s.equals(targetTime2)) {
                                                t = daySchedule.indexOf(s);
                                            }
                                        }
                                    }
                                    t++;
                                    if (t + 1 <= daySchedule.size() - 1) {
                                        targetTime4 = daySchedule.get(t + 1);
                                    } else {
                                        targetTime4 = adjustTimeStamp(daySchedule.get(daySchedule.size() - 1));
                                        //update t
                                        for (String s : daySchedule) {
                                            if (s.equals(targetTime2)) {
                                                t = daySchedule.indexOf(s);
                                            }
                                        }
                                    }
                                    t++;
                                } catch (Exception e) {
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
                                            for (String s : roomNamesSplit) {
                                                if (s.equals(room)) {
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
                                        if (targetTime2 != null && targetTime1.equals(freeTimesList.get(i)) && targetTime2.equals(freeTimesList.get(i + 1))) {
                                            Reservation fillerReservation = new Reservation();
                                            fillerReservation.setStartTime(targetTime1);
                                            //add end time if there's a booking close to time (needed for recyclerview)
                                            if (bookedStartTimes.size() > 0) {
                                                for (String time : bookedStartTimes) {
                                                    if (isFirstTimeEarlier(targetTime2, time)) {
                                                        fillerReservation.setEndTime(time);
                                                        bookedStartTimes.remove(time);
                                                        break;
                                                    }
                                                }
                                            }
                                            //boolean to prevent bug where same room repeated
                                            boolean isNotSameAsLast = true;
                                            if (reservations.size() > 1) {
                                                if (room.equals(reservations.get(reservations.size() - 2).getName()[0])) {
                                                    isNotSameAsLast = false;
                                                }
                                            }
                                            //add room name
                                            String[] roomArr = {room};
                                            fillerReservation.setName(roomArr);
                                            //check reservations size before adding new
                                            if (reservations.size() < reservationsSizeCap && isNotSameAsLast) {
                                                reservations.add(fillerReservation);
                                            }
                                            //else nothing happens - can't break main loop within jsonrequest

                                            //if first half hour is free, then also check next half hour
                                            if (i + 2 != freeTimesList.size() && targetTime3 != null && targetTime4 != null
                                                    && targetTime3.equals(freeTimesList.get(i + 2)) && targetTime4.equals(freeTimesList.get(i + 3))) {
                                                Reservation fillerReservation2 = new Reservation();
                                                fillerReservation2.setStartTime(targetTime3);
                                                //add end time if there's a booking close to time (needed for recyclerview)
                                                if (bookedStartTimes.size() > 0) {
                                                    for (String time : bookedStartTimes) {
                                                        if (isFirstTimeEarlier(targetTime4, time)) {
                                                            fillerReservation.setEndTime(time);
                                                            bookedStartTimes.remove(time);
                                                            break;
                                                        }
                                                    }
                                                }
                                                //add room name
                                                fillerReservation2.setName(roomArr);
                                                //check reservations size before adding new
                                                if (reservations.size() < reservationsSizeCap && isNotSameAsLast) {
                                                    reservations.add(fillerReservation2);
                                                }
                                            }
                                            //break after targetTimes are found
                                            break;
                                        }
                                    }
                                }
                                if (reservations.size() >= reservationsSizeCap) {
                                    System.out.println("Breaking mainloop... dayShedule size: " + daySchedule.size());
                                    showMoreAmount = t + 1;
                                    //if all times have been looped
                                    if (showMoreAmount >= daySchedule.size() - 4) {
                                        adjustTimeStamp(daySchedule.get(showMoreAmount));
                                        showMoreAmount = 0;
                                        dayCount++;
                                        dayCountWasAdded = true;
                                    }
                                    break mainloop;
                                }
                            }
                            //if all times in schedule has been checked, then set new day
                            if (showMoreAmount > 0 && showMoreAmount >= daySchedule.size() - 2) {
                                adjustTimeStamp(daySchedule.get(showMoreAmount));
                                showMoreAmount = 0;
                                dayCount++;
                                dayCountWasAdded = true;
                            }
                            if (showMoreAmount > 0 && targetTime4.equals(daySchedule.get(daySchedule.size() - 2))) {
                                adjustTimeStamp(daySchedule.get(showMoreAmount));
                                showMoreAmount = 0;
                                dayCount++;
                                dayCountWasAdded = true;
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

    private void editDate(String date) {
        try {
            changableDate = formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void setDateTextView() {
        Calendar dateChecker = Calendar.getInstance();
        dateToday = formatter.format(dateChecker.getTime());
        dateChecker.add(Calendar.DATE, 1);
        String dateTomorrow = formatter.format(dateChecker.getTime());
        String today = getResources().getString(R.string.today);
        String tomorrow = getResources().getString(R.string.tomorrow);
        if (selectedDate == null) {
            selectedDate = dateToday;
        }

        if (dateToday.equals(selectedDate)) {
            todaysDate.setText(today);
        } else if (selectedDate.equals(dateTomorrow)) {
            todaysDate.setText(tomorrow);
        } else
            todaysDate.setText(selectedDate);
    }

    //get values from intent
    private void getIntents() {
        roomNames = MainActivity.safetyString;
        Bundle extras = getIntent().getExtras();
        int[] extraArr = extras.getIntArray(VALUES_EXTRA);
        showMoreAmount = extraArr[0];
        dayCount = extraArr[1];
        if (showMoreAmount > 0) {
            //save previous
            showLessAmount = showMoreAmount;
            //create instance of utility singleton class ShowAmountValues and add current showLessAmount
            ShowAmountValues showAmountValues = ShowAmountValues.getInstance();
            showAmountValues.showAmountList.add(showLessAmount);
        }
    }

    private void updateView() {
        Intent i = new Intent(ShowFirstAvailableActivity.this, ShowFirstAvailableActivity.class);
        Bundle extras = new Bundle();
        int[] extraArr = {showMoreAmount, dayCount};
        extras.putIntArray(VALUES_EXTRA, extraArr);
        i.putExtras(extras);
        // Hides the transition between intents
        finish();
        startActivity(i);
        overridePendingTransition(0, 0);
    }

    // Allows to read swipe-gestures over all elements in viewgroup
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                // Getting value for horizontal swipe
                float valueX = x2 - x1;
                float valueY = y2 - y1;

                if (Math.abs(valueX) > MIN_DISTANCE) {
                    // Detects horizontal swipes and changes display of available times to day before or after depending on swipe direction
                    checkHorizontal(x1, x2);
                    // After swipe is detected, consumes action
                    // Which means in this case, the recycler won't be clicked after a swipe
                    // Same goes for return true after checkVertical
                    return true;
                }

                if (Math.abs(valueY) > MIN_DISTANCE) {
                    // Detects top & bottom swipes & changes display of available times same day
                    checkVertical(y1, y2);
                    return true;
                }
        }
        return super.dispatchTouchEvent(event);
    }

    private void checkHorizontal(float x1, float x2) {

        if (x2 > x1) {
            // Right swiped
            Log.d(TAG, "Right Swipe");
            // Adjust dayCount if it was added in current view
            if (dayCountWasAdded) {
                dayCount--;
            }
            ShowAmountValues showAmountValues = ShowAmountValues.getInstance();
            if (dayCount != 0) {
                dayCount--;
                showMoreAmount = 0;
                showAmountValues.resetShowAmountList();
                updateView();
            } else {
                //Toast toast = Toast.makeText(getApplicationContext(), "Can't go further back", Toast.LENGTH_SHORT);
                //toast.show();
            }

        } else {
            // Left swiped
            Log.d(TAG, "Left Swipe");
            dayCount++;
            showMoreAmount = 0;
            ShowAmountValues showAmountValues = ShowAmountValues.getInstance();
            showAmountValues.resetShowAmountList();
            updateView();

        }

    }

    private void checkVertical(float y1, float y2) {
        if (y2 > y1) {
            //Down swiped
            Log.d(TAG, "Down swiped");
            ShowAmountValues showAmountValues = ShowAmountValues.getInstance();
            if (!isFirstResult) {
                if (showAmountValues.showAmountList.size() > 1) {
                    //get second last
                    showMoreAmount = showAmountValues.showAmountList.get(showAmountValues.showAmountList.size() - 2);
                } else {
                    showMoreAmount = 0;
                }
                showAmountValues.resetShowAmountList();
                updateView();
            } else if (isFirstResult && dayCount > 0) {
                dayCount--;
                showMoreAmount = 0;
                showAmountValues.resetShowAmountList();
                updateView();
            } else {
                // Toast toast = Toast.makeText(getApplicationContext(), "Can't go further back", Toast.LENGTH_SHORT);
                // toast.show();
            }
        }
        // Up swiped
        else {
            Log.d(TAG, "Up swiped");
            updateView();
        }

    }
}
