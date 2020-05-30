package com.joor.roomapplication.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.os.ConfigurationCompat;
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
import com.joor.roomapplication.utility.LoadImage;
import com.joor.roomapplication.utility.RoomData;
import com.joor.roomapplication.utility.ShowAmountValues;
import com.joor.roomapplication.utility.TempValues;

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
    private TextView roomName;
    private TextView todaysDate;
    private TextView dayofweek;
    private ImageView rightClick;
    private ImageView leftClick;
    private ImageView infoClick;
    private Date constantDate;
    private Date changableDate;
    private Date maxDate;
    private Calendar constantCalendar;
    private Calendar changableCalendar;
    private String dateToday;
    private String selectedDate;
    Dialog roomInfoDialog;

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
        setContentView(R.layout.activity_show_day_schedule);
        // Set today's date
        bindViews();
        // handleSwipes();
        //init view elements and checks if there's a saved instance
        setViewElements(savedInstanceState);
        ShowAmountValues showAmountValues = ShowAmountValues.getInstance();

        if(showAmountValues.showAmountList.size() == 0){
            //show info toast
            Toast toast = Toast.makeText(getApplicationContext(), "Swipe up/down to change room.", Toast.LENGTH_SHORT);
            toast.show();

            //wait 2s for second toast
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast toast= Toast.makeText(getApplicationContext(),
                            "Swipe sideways to\nchange selected day.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT, 0, 0);
                    toast.show();
                }
            }, 2000);
        }

        ViewConfiguration vc = ViewConfiguration.get(this);
        //mSlop = vc.getScaledTouchSlop();

        try {
            setDate(selectedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        setDateTextView();
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

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
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
            //ViewGroup.MarginLayoutParams recyclerParams = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
            //recyclerParams.topMargin = 100;
            //recyclerView.setLayoutParams(recyclerParams);
        }else if (dHeight >= 1900){
            System.out.println("dHeight >= 1900 ");
            ViewGroup.MarginLayoutParams recyclerParams = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
            recyclerParams.topMargin = 70;
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
        infoClick = (ImageView) findViewById(R.id.buttonShowViewInfo);
        dayofweek = (TextView) findViewById(R.id.dayofweek);
        roomName.setText(room_name);
        //setDateTextView();
        safetyString = new String[]{"Backsippan", "C11", "C13", "C15", "Flundran", "Heden", "Rauken", "Myren", "Änget"};


        //reset utility list used in adapter
        TempValues tempValues = TempValues.getInstance();
        tempValues.resetTempValuesList();
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

        // Used to only be able to check availability for 252, i.e. 36 weeks
        Calendar maxCalendar= constantCalendar;
        maxCalendar.add(Calendar.DATE, 252);
        maxDate = maxCalendar.getTime();
    }

    private void setAdapter() {
        //creates RecycleAdapter and sets it
        adapter = new ReservationAdapter(this, reservations);
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

        roomInfoDialog = new Dialog(ShowDayActivitySchedule.this);
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

                String svTitleText = "Välkommen till Rumsvyn";
                String engTitleText = "Welcome to RoomView";

                String svViewInfo =
                        "Swipe höger går till nästa dag" + "\n" +
                        "Swipe vänster går till föregående dag" + "\n" +
                        "Swipe upp/ner växlar mellan rum" + "\n" + "\n" +
                                "Klick på datum öppnar kalendern för val av specifikt datum " + "\n"+"\n"+
                                "Klicka på den tid du vill boka för omredigering till TimeEdit";

                String engViewInfo = "In this view...";

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

    private void getAvailability(final Date date) {
        //create list containing all times for a day
        final ArrayList<String> daySchedule = timeList();

        //spelling fix for low level API's
        if(room_name.toLowerCase().equals("änget")){
            room_name = "anget";
        }
        //new request url to get data from specific room
        String requestUrl = url + "room/" + room_name;

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
                dialog.getDatePicker().setMaxDate(maxDate.getTime());
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

        String language = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0).toString();
        int dayNumber = changableCalendar.get(Calendar.DAY_OF_WEEK);
        String[] swedishDays = new String[] { "Söndag", "Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag" };
        String[] englishDays = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
        String weekday ="";
        if (language.equals("sv_SE")){
            //weekday = swedishDays[dayNumber-1];
            dayofweek.setText(swedishDays[dayNumber-1]);
        }
        else{dayofweek.setText(englishDays[dayNumber-1]);}

        if (dateToday.equals(selectedDate)) {
            String today = getResources().getString(R.string.today);
            todaysDate.setText(today);
        } else if (selectedDate.equals(dateTomorrow)) {
            String tomorrow= getResources().getString(R.string.tomorrow);
            todaysDate.setText(tomorrow);
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
        ShowAmountValues showAmountValues = ShowAmountValues.getInstance();
        if(showAmountValues.showAmountList.size() == 0){
            showAmountValues.showAmountList.add(0);
        }

        extras.putString(ROOMNAME_EXTRA, room_name);
        extras.putString(DATE_EXTRA, selectedDate);
        //send false if intent is not from main activity
        i.putExtras(extras);
        // Hides the transition between intents
        finish();
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
            if(clickedDate.after(maxDate)){
                Toast toast = Toast.makeText(getApplicationContext(), "Can't go further", Toast.LENGTH_SHORT);
                toast.show();
                clickedDate = maxDate;
            }}

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
        Intent i = new Intent(ShowDayActivitySchedule.this, ShowDayActivitySchedule.class);
        Bundle extras = new Bundle();
        System.out.println("UpdateView date is " + selectedDate);
        extras.putString(ROOMNAME_EXTRA, name);
        extras.putString(DATE_EXTRA, selectedDate);
        i.putExtras(extras);
        finish();
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
}