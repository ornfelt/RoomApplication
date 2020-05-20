package com.joor.roomapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.joor.roomapplication.R;
import com.joor.roomapplication.adapters.FirstAvailableAdapter;
import com.joor.roomapplication.controllers.AppController;
import com.joor.roomapplication.models.AvailableTimes;
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

public class ShowFirstAvailableDay extends AppCompatActivity {


    public static String ROOMNAME_EXTRA = "ROOM_NAME";
    public static String DATE_EXTRA;
    private String room_name;
    private RecyclerView recyclerView;
    private static final String url = "https://timeeditrestapi.herokuapp.com/reservations/";
    private List<Reservation> reservations = new ArrayList<Reservation>();
    private FirstAvailableAdapter adapter;
    private Spinner filterOptions;
    private TextView roomName;
    private TextView filter;
    private TextView todaysDate;
    private ImageView rightClick;
    private ImageView leftClick;
    private Date constantDate;
    private Date changableDate;
    private Calendar constantCalendar;
    private Calendar changableCalendar;
    private String dateToday;
    private String selectedDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private boolean datePicked;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private DisplayMetrics displayMetrics;

    private static final String TAG = "Swipe Position";
    private float x1, x2, y1, y2;
    private static int MIN_DISTANCE = 100;
    String[] safetyString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_first_available_day);
        // Set today's date
        bindViews();
        // handleSwipes();
        //init view elements and checks if there's a saved instance
        setViewElements(savedInstanceState);

        ViewConfiguration vc = ViewConfiguration.get(this);
        //mSlop = vc.getScaledTouchSlop();

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

        displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int dWidth = displayMetrics.widthPixels;
        int dHeight = displayMetrics.heightPixels;
        int dDensityPerInch = displayMetrics.densityDpi;
        System.out.println("width for device: " + dWidth + ", height: " + dHeight + " density: " + dDensityPerInch);

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
        int dDensityPerInch = displayMetrics.densityDpi;
        System.out.println("width for device: " + dWidth + ", height: " + dHeight + " density: " + dDensityPerInch);
        Configuration configuration = this.getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
        int screenHeightDp = configuration.screenHeightDp;
        System.out.println("Usable width in this activity: " + screenWidthDp + ", usable height: " + screenHeightDp);

        //this should fix layout for devices with similar resolution as Galaxy Nexus (720p)
        if(dHeight <= 1200) {
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

    //This class shows available times for a specific day and a specific room, hence these values needs to be loaded from intent
    private void initValues() {
        // Getting Room name and Date From Intent.
        getIntents();
        // Date and Room
        todaysDate = findViewById(R.id.date);
        roomName = findViewById(R.id.txtRoomName);
        // Clicklisteners
        rightClick = (ImageView) findViewById(R.id.rightClick);
        leftClick = (ImageView) findViewById(R.id.leftClick);

        roomName.setText("First Available");
        setDateTextView();
        safetyString = new String[]{"Backsippan", "C11", "C13", "C15", "Flundran", "Heden", "Rauken", "Myren", "Änget"};
        //this.gestureDetector = new GestureDetector(ShowDayActivitySchedule.this, this);


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //saves when onDestroyed
        outState.putString("roomName", room_name);
        outState.putString("selectedDate", selectedDate);
    }

    private void initValuesFromSavedState(Bundle savedInstanceState) throws IOException {
        room_name = savedInstanceState.getString("roomName");
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
        System.out.println("Begärt datum är" + date);
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
        adapter = new FirstAvailableAdapter(this, reservations);
        recyclerView.setAdapter(adapter);
    }

    private void setImgListeners() {
        rightClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateForward();
            }
        });
        leftClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateBackward();
            }
        });
    }

    private void getAvailability(final Date date) {
        //create list containing all times for a day
        final ArrayList<String> daySchedule = timeList();

        //new request url to get data from specific room
        String requestUrl = url;
        System.out.println("requestUrl: " + requestUrl);
        // Request a json response from the provided URL
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, requestUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            //list that will contain all times available for booking
                            ArrayList<String> freeTimesList = new ArrayList<>();
                            ArrayList<AvailableTimes> bookableList = new ArrayList<>();

                            String dateToday = formatter.format(date);
                            System.out.println("Selected date is: " + dateToday);


                            //loops through all available times during a day to find available times
                            for (int i = 0; i < 100; i++) {
                                boolean timeFound = false;

                                //loops through response data
                                for (int j = 0; j < response.length(); j++) {
                                    JSONObject JSONreservation = response.getJSONObject(j);
                                    String startDate = JSONreservation.getString("startDate");
                                    //if reservation is for today
                                    if (startDate.equals(dateToday)) {
                                        //sets start and end time for reservation
                                        String startTime = JSONreservation.getString("startTime");


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
                int year = changableCalendar.get(Calendar.YEAR);
                int month = changableCalendar.get(Calendar.MONTH);
                int day = changableCalendar.get(Calendar.DAY_OF_MONTH);


                // Old settings are:
                // android.R.style.Theme_Holo_Light_Dialog_MinWidth
                // dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                DatePickerDialog dialog = new DatePickerDialog(
                        ShowFirstAvailableDay.this,
                        R.style.DatePickerStyle,
                        mDateSetListener,
                        year, month, day);
                dialog.getDatePicker().setMinDate(constantDate.getTime());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.uppsalaGray)));
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

    private void setDateTextView() {
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

    private void getIntents() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        selectedDate = extras.getString(DATE_EXTRA);
    }

    private void updateView() {
        Intent i = new Intent(ShowFirstAvailableDay.this, ShowDayActivitySchedule.class);
        Bundle extras = new Bundle();
        System.out.println("UpdateView date is " + selectedDate);
        extras.putString(ROOMNAME_EXTRA, room_name);
        extras.putString(DATE_EXTRA, selectedDate);
        i.putExtras(extras);
        // Hides the transition between intents
        startActivity(i);
        overridePendingTransition(0,0);
        //overridePendingTransition(0, 0);
    }

    private void dateForward() {
        Date clickedDate;

        if (datePicked == true) {
            changableCalendar.setTime(changableDate);
            changableCalendar.add(Calendar.DATE, 1);
            clickedDate = changableCalendar.getTime();
            datePicked = false;
        } else {
            changableCalendar.add(Calendar.DATE, 1);
            clickedDate = changableCalendar.getTime();
        }

        selectedDate = formatter.format(clickedDate);
        updateView();
    }

    private void dateBackward() {

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
            selectedDate = formatter.format(clickedDate);
            updateView();
        }
    }

    private void updateViewName(String name) {
        Intent i = new Intent(ShowFirstAvailableDay.this, ShowDayActivitySchedule.class);
        Bundle extras = new Bundle();
        System.out.println("UpdateView date is " + selectedDate);
        //extras.putString(ROOMNAME_EXTRA, name);
        extras.putString(DATE_EXTRA, selectedDate);
        i.putExtras(extras);
        // Hides the transition between intents
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
                //mSwiping = false;
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                // getting value for horizontal swipe
                float valueX = x2 - x1;
                float valueY = y2 - y1;

                if (Math.abs(valueX) > MIN_DISTANCE) {
                    //detect left to right swipe
                    if (x2 > x1) {
                        //Toast.makeText(this, "Right is swiped", Toast.LENGTH_SHORT).show();
                        String direction = "Right";
                        Log.d(TAG, "Right Swipe");
                        dateBackward();

                        // After swipe is detected, consumes action
                        // Which means in this case, the recycler won't be clicked after a swipe.
                        return true;
                    } else {// detect right to left swipe}
                        //Toast.makeText(this, "Left is swiped", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Left Swipe");
                        // After swipe is detected, consumes action
                        // Which means in this case, the recycler won't be clicked after a swipe
                        String direction = "Left";
                        dateForward();

                        return true;
                    }

                }

                if (Math.abs(valueY) > MIN_DISTANCE) {
                    // detect top to bottom swipe
                    if (y2 > y1) {
                        //Toast.makeText(this, "Right is swiped", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Bottom Swipe");
                        String direction = "down";
                        // Changes Room/ & activity with down swipe
                        for (int i = 0; i < safetyString.length; i++) {
                            if (safetyString[i].equals(room_name)) {
                                System.out.println("Matchning på " + room_name + " vid position " + i);
                                // if match on last position -> go to first (since update is +1)
                                if ((safetyString[safetyString.length - 1].equals(room_name))) {
                                    updateViewName(safetyString[0]);

                                    return true;
                                }

                                // System.out.println("Nästa namn är"+ safetyString[i+1]);
                                updateViewName(safetyString[i + 1]);
                                return true;
                            }
                        }
                    }

                    // detect bottom to to top swipe
                    else {// detect right to left swipe}
                        Log.d(TAG, "Top Swipe");
                        String direction = "up";
                        // Changes Room/ & activity with up swipe
                        for (int i = 0; i < safetyString.length; i++) {
                            if (safetyString[i].equals(room_name)) {
                                System.out.println("Matchning på " + room_name + " vid position " + i);
                                // if match on first position -> go to last position
                                if (safetyString[0].equals(room_name)) {
                                    updateViewName(safetyString[safetyString.length - 1]);
                                    return true;
                                }
                                // if match on last position -> go to second last position
                                else if (safetyString[safetyString.length - 1].equals(room_name)) {
                                    updateViewName(safetyString[safetyString.length - 2]);
                                    return true;
                                } else

                                    // System.out.println("Nästa namn är"+ safetyString[i+1]);
                                    updateViewName(safetyString[i - 1]);
                                return true;
                            }
                        }
                    }

                }



        }

        return super.dispatchTouchEvent(event);
    }

/*
    // Handles up/down swipes to change name in array
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                y1 = event.getY();
                //mSwiping = false;
                break;
            case MotionEvent.ACTION_UP:
                y2 = event.getY();

                // getting value for vertical swipe
                float valueY = y2 - y1;

                if (Math.abs(valueY) > MIN_DISTANCE) {
                    // detect top to bottom swipe
                    if (y2 > y1) {
                        //Toast.makeText(this, "Right is swiped", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Bottom Swipe");
                        String direction = "down";
                        // Changes Room/ & activity with down swipe
                        for (int i = 0; i < safetyString.length; i++) {
                            if (safetyString[i].equals(room_name)) {
                                System.out.println("Matchning på " + room_name + " vid position " + i);
                                // if match on last position -> go to first (since update is +1)
                                if ((safetyString[safetyString.length - 1].equals(room_name))) {
                                    updateViewName(safetyString[0]);

                                    return true;
                                }

                                // System.out.println("Nästa namn är"+ safetyString[i+1]);
                                updateViewName(safetyString[i + 1]);
                                return true;
                            }
                        }
                    }

                    // detect bottom to to top swipe
                    else {// detect right to left swipe}
                        Log.d(TAG, "Top Swipe");
                        String direction = "up";
                        // Changes Room/ & activity with up swipe
                        for (int i = 0; i < safetyString.length; i++) {
                            if (safetyString[i].equals(room_name)) {
                                System.out.println("Matchning på " + room_name + " vid position " + i);
                                // if match on first position -> go to last position
                                if (safetyString[0].equals(room_name)) {
                                    updateViewName(safetyString[safetyString.length - 1]);
                                    return true;
                                }
                                // if match on last position -> go to second last position
                                else if (safetyString[safetyString.length - 1].equals(room_name)) {
                                    updateViewName(safetyString[safetyString.length - 2]);
                                    return true;
                                } else

                                    // System.out.println("Nästa namn är"+ safetyString[i+1]);
                                    updateViewName(safetyString[i - 1]);
                                return true;
                            }
                        }
                    }

                }


        }
        return true;
    }
    */







}

