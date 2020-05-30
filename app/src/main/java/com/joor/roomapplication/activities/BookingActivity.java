package com.joor.roomapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.joor.roomapplication.R;

/**
 * Activity class that uses WebView to direct user to a given url
 * @author Jonas Ornfelt & Daniel Arnesson
 */

public class BookingActivity extends AppCompatActivity {

    public static String INTENT_MESSAGE_KEY = "BOOKING_TIME";
    private String booking_time;
    private WebView webView;

    //this method is called on start and sends user to given url
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        //get and set chosen booking time
        Intent intent = getIntent();
        booking_time = intent.getStringExtra(INTENT_MESSAGE_KEY);
        TextView textTime = (TextView) findViewById(R.id.textBookingTime);
        textTime.setText(booking_time);
        // TODO: room name needs to be sent as intent as well from previous view. More links are also needed.
        // The one below only works for C11

        //redirect user to booking site
        webView = (WebView) findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient());
        //link for redirect to C11 booking site
        webView.loadUrl("https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&p=20200526-20200526&objects=2547815.212%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1");
    }

    //when user navigates back
    @Override
    public void onBackPressed(){
        if(webView.canGoBack()){
            webView.goBack();
        }
        else{
            super.onBackPressed();
        }
    }

}
