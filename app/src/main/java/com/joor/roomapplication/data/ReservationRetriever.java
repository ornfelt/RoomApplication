package com.joor.roomapplication.data;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.joor.roomapplication.adapters.ReservationAdapter;
import com.joor.roomapplication.controllers.AppController;
import com.joor.roomapplication.models.Reservation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReservationRetriever {

    private static final String url = "https://timeeditrestapi.herokuapp.com/reservations/";

    //no args constructor
    public ReservationRetriever(){}

    public List<Reservation> getAllReservations() {
        final List<Reservation> reservations = new ArrayList<Reservation>();

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

        return reservations;
    }

    public List<Reservation> getRoomReservations(String roomName) {
        final List<Reservation> reservations = new ArrayList<Reservation>();

        //new request url to get data from specific room
        String requestUrl = url + "room/" + roomName;
        System.out.println("requestUrl: " + requestUrl);
        // Request a json response from the provided URL
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, requestUrl, null,
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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Volleyerror: " + error.toString());
            }
        });

        // Add the request to the RequestQueue.
        AppController.getmInstance().addToRequestQueue(jsonRequest);

        return reservations;
    }

    public List<Reservation> getReservationById(String id) {
        final List<Reservation> reservations = new ArrayList<Reservation>();

        //new request url to get data from specific room
        String requestUrl = url + "id/" + id;
        System.out.println("requestUrl: " + requestUrl);
        // Request a json response from the provided URL
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, requestUrl, null,
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
                        //adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Volleyerror: " + error.toString());
            }
        });

        // Add the request to the RequestQueue.
        AppController.getmInstance().addToRequestQueue(jsonRequest);

        return reservations;
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
}
