package com.joor.roomapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.joor.roomapplication.R;
import com.joor.roomapplication.adapters.RoomAdapter;
import com.joor.roomapplication.interfaces.RecyclerClickInterface;
import com.joor.roomapplication.models.Room;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.joor.roomapplication.activities.ShowDayActivitySchedule.DATE_EXTRA;
import static com.joor.roomapplication.activities.ShowDayActivitySchedule.ROOMNAME_EXTRA;
import static com.joor.roomapplication.activities.ShowFirstAvailableActivity.VALUES_EXTRA;
//import static com.joor.roomapplication.activities.ShowFirstAvailableActivity.FIRST_DATE_EXTRA;

public class MainActivity extends AppCompatActivity implements RecyclerClickInterface {
    private String dateToday;
    private Spinner roomNameText;
    private RecyclerView recyclerView;
    private ArrayList <String> roomNames;
    //can be used by other classes
    public static String [] safetyString = {"C11", "C13", "C15", "Flundran", "Rauken", "Änget", "Backsippan", "Heden", "Myren"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Sets dateToday to today's date
        roomNames = new ArrayList<>();
        startRecycle();
        getNames();
        setDate();

        TypedValue tv = new TypedValue();
        getApplicationContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        int actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);
        Button btn = findViewById(R.id.buttonShowFirst);
        btn.setHeight(actionBarHeight);
    }

    private void setDate() {
        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        dateToday = formatter.format(today);
    }

    /*
    private void setSpinner(ArrayList roomNames) {
        roomNameText = (Spinner) findViewById(R.id.spinnerRoomName);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, roomNames);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        roomNameText.setAdapter(adapter);
    }*/

    //launches activity ShowItems
    public void onClickShowRooms(View view) {
        Intent intent = new Intent(getApplicationContext(),
                ShowReservationsActivity.class);
        startActivity(intent);
    }

    //when user clicks on show day activity
    public void onClickShowDay(View view) {
        //gets selected room from user
        String roomName = roomNameText.getSelectedItem().toString();
        navigateToShowDayActivity(roomName);
    }

    //when user clicks on show day activity (schedule)
    public void onClickShowDaySchedule(View view) {
        String roomName = roomNameText.getSelectedItem().toString();
        navigateToShowDayActivitySchedule(roomName);
    }

    //when user clicks on first available
    public void onClickShowFirstAvailable(View view) {
        navigateToShowFirstAvailableActivity();
    }

    //launches activity ShowDayActivity
    private void navigateToShowDayActivity(String room_name) {
        Intent intent = new Intent(getApplicationContext(), ShowDayActivity.class);
        Bundle extras = new Bundle();
        extras.putString(ROOMNAME_EXTRA, room_name);
        extras.putString(DATE_EXTRA, dateToday);
        intent.putExtras(extras);
        startActivity(intent);
        overridePendingTransition(0, 0);
        overridePendingTransition(0, 0);
    }

    //launches activity ShowDayActivitySchedule
    private void navigateToShowDayActivitySchedule(String room_name) {
        Intent intent = new Intent(getApplicationContext(), ShowDayActivitySchedule.class);
        Bundle extras = new Bundle();
        extras.putString(ROOMNAME_EXTRA, room_name);
        extras.putString(DATE_EXTRA, dateToday);
        intent.putExtras(extras);
        startActivity(intent);
        overridePendingTransition(0, 0);
        overridePendingTransition(0, 0);
    }

    //launches activity ShowFirstAvailableActivity
    private void navigateToShowFirstAvailableActivity() {
        Intent intent = new Intent(getApplicationContext(), ShowFirstAvailableActivity.class);
        Bundle extras = new Bundle();
        int[] extraArr = {0,0};
        extras.putIntArray(VALUES_EXTRA, extraArr);
        //extras.putString(FIRST_DATE_EXTRA, dateToday);
        intent.putExtras(extras);
        startActivity(intent);
        overridePendingTransition(0, 0);
        overridePendingTransition(0, 0);
    }

    // Makes a request to the Rest API to collect all names.
    // However, since it's relying on current reservations it still needs a "safety" string with all roomnames
    // Another way to go (probably to prefer) is to make roomNames available directly in the Rest-API
    public void getNames() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://timeeditrestapi.herokuapp.com/reservations/";
        //final ArrayList<String> roomNames = new ArrayList<>();
        final Set <String> removeDuplicates = new LinkedHashSet<>();

        // Request a json response from the provided URL, in this case an jsonarray.
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Get's all the names and removes surrounding brackets
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject JSONreservation = response.getJSONObject(i);
                                String name = JSONreservation.getString("name");
                                name = name.replaceAll("\\[", "").replaceAll("\\]","").replace("\"", "");
                                String [] multipleNames;

                                // if name contains "," it includes multiple names, then split to array
                                if (name.contains(",")){
                                    multipleNames = name.split(",");
                                    // add all names to roomNames
                                    for(int j = 0; j < multipleNames.length; j++){
                                    roomNames.add(multipleNames[j]);
                                    }
                                }
                                else
                                roomNames.add(name);
                            }
                            // Adding all roomNames to the LinkedHashSet to remove all duplicates
                            removeDuplicates.addAll(roomNames);
                            // Clearing the list
                            roomNames.clear();
                            // Adding back values without duplicates
                            roomNames.addAll(removeDuplicates);
                            // Sorting the roomNames

                            // In case some rooms has no reservation, the values need to be set accordingly to the safetyString
                            // However, if the API share's the roomnames without depending on reservations this if case can be removed.
                           /*
                            if(!Arrays.asList(safetyString).equals(roomNames))
                            {
                                roomNames.clear();
                                roomNames.addAll(Arrays.asList(safetyString));
                            }
                            */
                            Collections.sort(roomNames);
                            // Spinner values is now fetched roomNames
                           // setSpinner(roomNames);

                            fillList();

                        } catch (Exception e) {
                            System.out.println("Exception " + e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Volley Error " + error);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonRequest);
    }

    private void startRecycle() {
        recyclerView = findViewById(R.id.new_recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
    }

    private void fillList() {
        recyclerView.setAdapter(new RoomAdapter(this, roomNames, this));
        for (String room : roomNames) {
            System.out.println("Hämtade namn är " + room);
        }
    }

    @Override
    public void onItemClick(int position) {
        String roomName = roomNames.get(position);
        navigateToShowDayActivitySchedule(roomName);
    }
}
