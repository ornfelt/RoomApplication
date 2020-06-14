package com.joor.roomapplication.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
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
    public static String RESERVATION_ROOM_NAME = "";
    private String booking_time;
    private String room;
    private WebView webView;

    //this method is called on start and sends user to given url
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        //get and set chosen booking time
        Intent intent = getIntent();
        booking_time = intent.getStringExtra(INTENT_MESSAGE_KEY);
        room= intent.getStringExtra(RESERVATION_ROOM_NAME);
        TextView textTime = (TextView) findViewById(R.id.textBookingTime);
        textTime.setText(booking_time);
        webView = (WebView) findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        // Enables Javascript
        webSettings.setJavaScriptEnabled(true);
        //link for redirect to booking site
        loadUrl(room);
    }

    public void loadUrl(String name) {
        // TODO:
        //  Implement an in-parameter for selected_date. If date is today then use the already implemented if-case, else dynamically build links.
        //  Format in-parameter value to same format as"p=20200526-20200526" which is "p=YYYY-mm-dd". Try to make sure it opens the dayview inside the phone.
        //  Link to concat is  https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&p=20200526-20200526&objects=2547815.212%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1

        if (name.toLowerCase().equals("änget")) {
            webView.loadUrl("https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&objects=2547822.212%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1");
        } else if (name.toLowerCase().equals("backsippan")) {
            webView.loadUrl("https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&objects=2547823.212%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1");
        } else if (name.toLowerCase().equals("c11")) {
            webView.loadUrl("https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&objects=2547815.212%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1");
        } else if (name.toLowerCase().equals("c13")) {
            webView.loadUrl("https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&objects=2547817.212%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1");
        } else if (name.toLowerCase().equals("c15")) {
            webView.loadUrl( "https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&objects=2547818.212%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1");
        } else if (name.toLowerCase().equals("flundran")) {
            webView.loadUrl("https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&objects=2547820.212%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1");
        } else if (name.toLowerCase().equals("heden")) {
          webView.loadUrl("https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&objects=3654145.212%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1");
        } else if (name.toLowerCase().equals("myren")) {
            webView.loadUrl("https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&objects=3654146.212%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1");
        } else if (name.toLowerCase().equals("rauken")) {
            webView.loadUrl("https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&objects=3654146.212%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1");
        }
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
