package com.joor.roomapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.joor.roomapplication.R;
import com.joor.roomapplication.models.Room;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.joor.roomapplication.activities.ShowDayActivity.DATE_EXTRA;
import static com.joor.roomapplication.activities.ShowDayActivity.ROOMNAME_EXTRA;

public class MainActivity extends AppCompatActivity {
    private String dateToday;
    Spinner roomNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Sets dateToday to today's date
        setDate();
        setSpinner();
    }

    private void setDate() {
        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        dateToday = formatter.format(today);
    }

    private void setSpinner() {
        roomNameText = (Spinner) findViewById(R.id.spinnerRoomName);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.grouprooms, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        roomNameText.setAdapter(adapter);
    }

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

    //when user clicks on show day activity (per hour)
    public void onClickShowDayHours(View view) {
        String roomName = roomNameText.getSelectedItem().toString();

        navigateToShowDayActivityPerHour(roomName);
    }


    //launches activity ShowDayActivity
    private void navigateToShowDayActivity(String room_name) {
        Intent intent = new Intent(getApplicationContext(), ShowDayActivity.class);
        Bundle extras = new Bundle();
        extras.putString(ROOMNAME_EXTRA, room_name);
        extras.putString(DATE_EXTRA, dateToday);
        intent.putExtras(extras);
        startActivity(intent);
    }

    //launches activity ShowDayActivitySchedule
    private void navigateToShowDayActivitySchedule(String room_name) {
        Intent intent = new Intent(getApplicationContext(), ShowDayActivitySchedule.class);
        Bundle extras = new Bundle();
        extras.putString(ROOMNAME_EXTRA, room_name);
        extras.putString(DATE_EXTRA, dateToday);
        intent.putExtras(extras);
        startActivity(intent);
    }

    //launches activity ShowDayActivityPerHour
    private void navigateToShowDayActivityPerHour(String room_name) {
        Intent intent = new Intent(getApplicationContext(),
                ShowDayActivityPerHour.class);
        intent.putExtra(ShowDayActivityPerHour.INTENT_MESSAGE_KEY, room_name);
        startActivity(intent);
    }
}
