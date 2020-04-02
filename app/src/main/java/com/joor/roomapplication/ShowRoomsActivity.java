package com.joor.roomapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;


import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.joor.roomapplication.adapters.RoomAdapter;
import com.joor.roomapplication.models.Room;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShowRoomsActivity extends AppCompatActivity {

    private ListView listView;
    private static final String url = "https://cloud.timeedit.net/uu/web/schema/ri12Y693Y23063QQ26Z552690558655965396351Y855X85X896X2658815856960969XY788256655YY85X76597553YX95X132Y16Y53X678656662559Q7.json";
    private List<Room> roomsList = new ArrayList<Room>();
    private RoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_rooms);

        bindViews();
        adapter = new RoomAdapter(this,roomsList);
        listView.setAdapter(adapter);

    // Request a json response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray reservations = response.getJSONArray("reservations");

                            JSONObject booking = reservations.getJSONObject(0);
                            System.out.println("booking: " + booking.toString());
                            String firstBooking = booking.getString("id");

                            //create new room object
                            Room room = new Room(firstBooking);
                            roomsList.add(room);
                            System.out.println("firstbooking: "+ room.getId());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });


    // Add the request to the RequestQueue.
        AppController.getmInstance().addToRequestQueue(jsonRequest);
    //queue.add(jsonRequest);
}

    //gets id for view
    private void bindViews() {
        listView = findViewById(R.id.list_view);
    }

    //when user navigates back
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),
                MainActivity.class);
        getApplicationContext().startActivity(intent);
    }
}

