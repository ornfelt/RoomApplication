package com.joor.roomapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //launches activity ShowItems
    public void onClickShowRooms(View view){
        Intent intent = new Intent(getApplicationContext(),
                ShowRoomsActivity.class);
        startActivity(intent);
    }
}
