package com.joor.roomapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.joor.roomapplication.R;
import com.joor.roomapplication.adapters.ReservationAdapter;
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

public class ShowDayActivitySchedule extends AppCompatActivity {

    public static String ROOMNAME_EXTRA = "ROOM_NAME";
    public static String DATE_EXTRA;
    private String room_name;
    private RecyclerView recyclerView;
    private static final String url = "https://timeeditrestapi.herokuapp.com/reservations/";
    private List<Reservation> reservations = new ArrayList<Reservation>();
    private ReservationAdapter adapter;
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
    private GestureDetector gestureDetector;
    String[] safetyString;


    private int mSlop;
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_day_schedule);
        // Set today's date
        bindViews();
        // handleSwipes();
        //init view elements and checks if there's a saved instance
        setViewElements(savedInstanceState);

        ViewConfiguration vc = ViewConfiguration.get(this);
        mSlop = vc.getScaledTouchSlop();

        try {
            setDate(selectedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Set up imagelisteners
        setImgListeners();
        setDatePicker();
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

        roomName.setText(room_name);
        setDateTextView();
        filter = findViewById(R.id.textViewFilter);
        setSpinner();
        filter.setVisibility(View.GONE);
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
        adapter = new ReservationAdapter(this, reservations);
        recyclerView.setAdapter(adapter);
        // ItemTouchHelper itHelper = new ItemTouchHelper(simpleCallback);
        // itHelper.attachToRecyclerView(recyclerView);

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
                        ShowDayActivitySchedule.this,
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
        room_name = extras.getString(ROOMNAME_EXTRA);
        selectedDate = extras.getString(DATE_EXTRA);
    }

    private void updateView() {
        Intent i = new Intent(ShowDayActivitySchedule.this, ShowDayActivitySchedule.class);
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

    private void setSpinner() {
        filterOptions = (Spinner) findViewById(R.id.spinnerFilter);
        filterOptions.setVisibility(View.GONE);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.filters, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        filterOptions.setAdapter(adapter);

        filterOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            int clickCounter = 0;

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /*
                if (clickCounter == 0 && position == 0){
                    System.out.println("This is the first try. Position is " + position );
                }*/


                Object item = parent.getItemAtPosition(position);
                System.out.println("Valt värde är " + item);
                clickCounter++;
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    public void onClickFilter(View v) {
        filter.setVisibility(View.GONE);
        if (filterOptions.getVisibility() == View.VISIBLE) {
            //filterOptions.setVisibility(View.GONE);
        } else {
            // filterOptions.setVisibility(View.VISIBLE);
        }

    }

    /*

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        gestureDetector.onTouchEvent(event);
        // starting to swipe time gesture
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            // ending time swipe gesture
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();

                // getting value for horizontal swipe
                float valueX = x2 - x1;

                // getting value for vertical swipe
                float valueY = y2 - y1;

                if (Math.abs(valueX) > MIN_DISTANCE) {
                    //detect left to right swipe
                    if (x2 > x1) {
                        //Toast.makeText(this, "Right is swiped", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Right Swipe");
                        dateBackward();

                    } else {// detect right to left swipe}
                        //Toast.makeText(this, "Left is swiped", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Left Swipe");
                        dateForward();

                    }

                } else if (Math.abs(valueY) > MIN_DISTANCE) {
                    // detect top to bottom swipe
                    if (y2 > y1) {
                        //Toast.makeText(this, "Right is swiped", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Bottom Swipe");

                        // Changes Room/ & activity with down swipe
                        for (int i = 0; i < safetyString.length; i++) {
                            if (safetyString[i].equals(room_name)) {
                                System.out.println("Matchning på " + room_name + " vid position " + i);
                                // if match on last position -> go to first (since update is +1)
                                if ((safetyString[safetyString.length - 1].equals(room_name))) {
                                    updateViewName(safetyString[0]);

                                    break;
                                }

                                // System.out.println("Nästa namn är"+ safetyString[i+1]);
                                updateViewName(safetyString[i + 1]);
                                break;
                            }
                        }
                    }
                    // detect bottom to to top swipe
                    else {// detect right to left swipe}
                        Log.d(TAG, "Top Swipe");

                        // Changes Room/ & activity with up swipe
                        for (int i = 0; i < safetyString.length; i++) {
                            if (safetyString[i].equals(room_name)) {
                                System.out.println("Matchning på " + room_name + " vid position " + i);
                                // if match on first position -> go to last position
                                if (safetyString[0].equals(room_name)) {
                                    updateViewName(safetyString[safetyString.length - 1]);
                                    break;
                                }
                                // if match on last position -> go to second last position
                                else if (safetyString[safetyString.length - 1].equals(room_name)) {
                                    updateViewName(safetyString[safetyString.length - 2]);
                                    break;
                                } else

                                    // System.out.println("Nästa namn är"+ safetyString[i+1]);
                                    updateViewName(safetyString[i - 1]);
                                break;
                            }
                        }
                    }

                }


        }
        return super.onTouchEvent(event);
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {


    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

     */

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

    private void updateViewName(String name, String direction) {
        Intent i = new Intent(ShowDayActivitySchedule.this, ShowDayActivitySchedule.class);
        Bundle extras = new Bundle();
        System.out.println("UpdateView date is " + selectedDate);
        extras.putString(ROOMNAME_EXTRA, name);
        extras.putString(DATE_EXTRA, selectedDate);
        i.putExtras(extras);
        // Hides the transition between intents
        startActivity(i);
        overridePendingTransition(0, 0);

        /*if (direction.equals("down")) {
            overridePendingTransition(R.anim.slide_down, R.anim.slide_up);
        } else


    {
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        //overridePendingTransition(0, 0);
    }
    */}

    // Allows to read swipe-gestures over all elements in viewgroup
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                mSwiping = false;
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();

                // getting value for horizontal swipe
                float valueX = x2 - x1;

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
/*
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                float xDelta = Math.abs(x - mDownX);
                float yDelta = Math.abs(y - mDownY);

                if (yDelta > mSlop && yDelta / 2 > xDelta) {
                    mSwiping = true;
                    return true;
                }
                break;*/
        }

        return super.dispatchTouchEvent(event);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                y1 = event.getY();
                mSwiping = false;
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
                                    updateViewName(safetyString[0], direction);

                                    return true;
                                }

                                // System.out.println("Nästa namn är"+ safetyString[i+1]);
                                updateViewName(safetyString[i + 1],direction);
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
                                    updateViewName(safetyString[safetyString.length - 1],direction);
                                    return true;
                                }
                                // if match on last position -> go to second last position
                                else if (safetyString[safetyString.length - 1].equals(room_name)) {
                                    updateViewName(safetyString[safetyString.length - 2], direction);
                                    return true;
                                } else

                                    // System.out.println("Nästa namn är"+ safetyString[i+1]);
                                    updateViewName(safetyString[i - 1], direction);
                                return true;
                            }
                        }
                    }

                }


        }
        return true;
    }


    private void updateView(String direction) {
        Intent i = new Intent(ShowDayActivitySchedule.this, ShowDayActivitySchedule.class);
        Bundle extras = new Bundle();
        System.out.println("UpdateView date is " + selectedDate);
        extras.putString(ROOMNAME_EXTRA, room_name);
        extras.putString(DATE_EXTRA, selectedDate);
        i.putExtras(extras);
        // Hides the transition between intents
        startActivity(i);
        if (direction.equals("Left")) {
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
            //overridePendingTransition(0, 0);
        }
        else
        {
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        }
}


    private void dateForward(String direction) {
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
        updateView(direction);
    }

    private void dateBackward(String direction) {

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



}

