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
import com.joor.roomapplication.models.Reservation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShowReservationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private static final String url = "https://timeeditrestapi.herokuapp.com/reservations/";
    private List<Reservation> reservations = new ArrayList<Reservation>();
    private ReservationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_reservations);

        //init view elements
        bindViews();

        //creates RecycleAdapter and sets it
        adapter = new ReservationAdapter(this, reservations);
        recyclerView.setAdapter(adapter);

        // Request a json response from the provided URL
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject JSONreservation = response.getJSONObject(i);
                                System.out.println("Adding reservation to list: " + JSONreservation.getString("id"));
                                //int id, String starttime, String startdate, String endtime, String enddate, String[] columns

                                //required variables for each reservation object
                                int reservationId = Integer.parseInt(JSONreservation.getString("id"));
                                String startTime = JSONreservation.getString("startTime");
                                String startDate = JSONreservation.getString("startDate");
                                String endTime = JSONreservation.getString("endTime");
                                String endDate = JSONreservation.getString("endDate");
                                JSONArray reservationColumns = JSONreservation.getJSONArray("columns");
                                JSONArray reservationNames = JSONreservation.getJSONArray("name");

                                //create reservation object and add to reservations (list)
                                Reservation reservation = new Reservation(reservationId, startTime, startDate, endTime, endDate,
                                        toStringArray(reservationColumns));
                                reservation.setName(toStringArray(reservationNames));
                                reservations.add(reservation);

                                System.out.println("Reservation added: " + reservation.toString());
                            }
                        } catch (Exception e) {
                            System.out.println("something wrong..");
                        }
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //txtView.setText("That didn't work!");
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

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        //saves when onDestroyed
    }

    private void initValuesFromSavedState(Bundle savedInstanceState) throws IOException {
        //init values from saved state
    }
}
