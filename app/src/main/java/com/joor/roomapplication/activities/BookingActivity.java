package com.joor.roomapplication.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.joor.roomapplication.R;
import com.joor.roomapplication.controllers.AppController;
import com.joor.roomapplication.data.RoomData;
import com.joor.roomapplication.utility.HtmlStringValue;
import com.joor.roomapplication.utility.ScriptCounter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import static android.graphics.Color.TRANSPARENT;

/**
 * Activity class that uses WebView to direct user to a given url
 * @author Jonas Ornfelt & Daniel Arnesson
 */

public class BookingActivity extends AppCompatActivity {

    public static String INTENT_MESSAGE_KEY = "BOOKING_TIME";
    public static String RESERVATION_ROOM_NAME = "";
    public static String RESERVATION_DATE = "BOOKING_DATE";
    private static final String url = "https://timeeditrestapi.herokuapp.com/reservations/";
    private String booking_time;
    private String room;
    public String booking_date;
    private WebView webView;
    private ImageView imageView;
    private int timeSteps;
    private String htmlContent;
    private boolean bookingScriptsDone;
    private boolean newBookingFound;
    private boolean firstBookingScriptDone;
    private boolean secondBookingScriptDone;
    private boolean thirdBookingScriptDone;
    Date dateToday;

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
        booking_date = intent.getStringExtra(RESERVATION_DATE);
        imageView = (ImageView) findViewById(R.id.imageViewCover);
        webView = (WebView) findViewById(R.id.web_view);
        htmlContent = "";
        bookingScriptsDone = false;
        firstBookingScriptDone = false;
        secondBookingScriptDone = false;
        thirdBookingScriptDone = false;

        //set webview settings
        WebSettings webSettings = webView.getSettings();
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowContentAccess(true);
        webSettings.setJavaScriptEnabled(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){
            // Do something for kitkat and above versions
            webView.setWebViewClient(new MyWebViewClient());
        } else{
            // Do something for phones running an SDK before kitkat
            webView.setWebViewClient(new MyLowApiWebViewClient());
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            webView.addJavascriptInterface(new MyJavaScriptInterface(),"HTMLOUT");
        }

        //link for redirect to booking site
        loadUrl(room, booking_time);
    }

    public void loadUrl(String name, String bookingTime) {
        System.out.println("bookingTime: " + bookingTime);
        timeSteps = timeStepsToTraverse(bookingTime);
        webView.loadUrl(correctUrl(booking_date, room));
    }

    private String correctUrl (String date, String room){
        //init roomdata where we can access the rooms object name
        RoomData roomData = new RoomData();

        //first check if date is today
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        String dateTodayString = formatter.format(today);

        //if selected date is todays date, return url for today
        if(date.equals(dateTodayString)){
            String urlForTodayFirstPart = "https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&objects=";
            String urlForTodaySecondPart = "%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1";
            System.out.println("is todays date");
            return urlForTodayFirstPart + roomData.getObjectNameByRoomName(room) + urlForTodaySecondPart;
        }else {
            System.out.println("not todays date " + date + ", dateTodayString: " + dateTodayString);
            String urlFirstPart = "https://cloud.timeedit.net/uu/web/wr_student/ri.html?h=t&sid=10&p=";
            //format date by removing all "-"
            String formattedDate = date.replaceAll("-", "");
            String urlSecondPart = formattedDate + "-" + formattedDate;

            String urlThirdPart = "&objects=" + roomData.getObjectNameByRoomName(room);
            String urlFourthPart = "%2C&ox=0&types=0&fe=0&part=f&tg=-1&se=f&exw=t&rr=1";

            return urlFirstPart + urlSecondPart + urlThirdPart + urlFourthPart;
        }
    }

    private int timeStepsToTraverse(String time){
        String[]timeSplit = time.split("-");
        String[] startTimeSplit = timeSplit[0].split(":");
        //clarifying example: if hour is 12 then 12-8 = 4, then times 4 because all quarters need to be counted as well.
        int hourSteps = (Integer.parseInt(startTimeSplit[0]) - 8)*4;

        //count minute steps
        int minSteps = 0;
        int minutes = (Integer.parseInt(startTimeSplit[1]));
        //if 0, then minSteps can remain at 0
        if(minutes == 15){
            minSteps = 1;
        }else if(minutes == 30){
            minSteps = 2;
        }else if(minutes == 45){
            minSteps = 3;
        }
        //I added "30" to bookingTime when user presses the second block of an hour in previous activity, so when len > 6, minutes = 30
        if(timeSplit[1].length() > 6){
            minSteps = 2;
        }
        System.out.println("timeStepsToTraverse: " + (hourSteps+minSteps) + ", for timee: " + time + ", length: " + time.length());
        return hourSteps + minSteps;
    }

    //this script will automatically click 'login via CAS', so the user can enter his/hers credentials
    private static String loginScript() {
        System.out.println("running loginscript");
        String timerStart = "setTimeout(function(){";
        String myFunction = " document.getElementsByClassName('items itemsbox')[0].click(); ";
        String timerEnd = "}, 800);";
        return "javascript:(function(){" + timerStart + myFunction + timerEnd + "})()";
    }

    //this script will select the correct day
    private static String bookingScript(int daysToTraverse){
        String timerStart = "setTimeout(function(){";
        String myFunction = "";
        String dayClick = "document.getElementById('leftresdateinc').click();";
        System.out.println("daysToTraverse: " + daysToTraverse);
        myFunction = dayClick;
        //this function will filter the view to show day-schedule (seems like this is only needed for desktop)
        //String myFunction = " document.getElementById('contents').getElementsByTagName('table')[1].getElementsByTagName('td')[1].click();";
        String timerEnd = "}, 250);";
        return "javascript:(function(){" + timerStart + myFunction + timerEnd + "})()";
    }

    //count steps between today's date and chosen date
    private static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    //count weekdays between two dates where
    private static int weekDaysBetweenDates(Date date1, Date date2) {
        SimpleDateFormat weekDayFormat = new SimpleDateFormat("E");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        int daysCount = 0;
        String date1WeekDay = weekDayFormat.format(date1);
        String date2WeekDay = weekDayFormat.format(date2);

        while(!date1WeekDay.equals(date2WeekDay)) {
            daysCount++;
            Calendar c = Calendar.getInstance();
            c.setTime(date1);
            c.add(Calendar.DATE, 1);  // add one day
            String newDate1String = sdf.format(c.getTime());  //new date
            try {
                date1 = sdf.parse(newDate1String);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            date1WeekDay = weekDayFormat.format(date1);
        }
        System.out.println("returning daysCount: " + daysCount);
        return daysCount;
    }

    private boolean isCurrentWeek(Date date1, Date date2) {
        SimpleDateFormat weekDayFormat = new SimpleDateFormat("E");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date1WeekDay = weekDayFormat.format(date1);
        String date2WeekDay = weekDayFormat.format(date2);

        while(!date2WeekDay.toLowerCase().equals("mon")) {
            Calendar c = Calendar.getInstance();
            c.setTime(date2);
            c.add(Calendar.DATE, -1);  // subtract one day
            String newDate2String = sdf.format(c.getTime());  //new date
            try {
                date2 = sdf.parse(newDate2String);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            date2WeekDay = weekDayFormat.format(date2);

            //check if today's date is reached
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(date1);
            cal2.setTime(date2);
            boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
            if(date1WeekDay.equals(date2WeekDay) && sameDay){
                return true;
            }
        }

        //if this is reached, then it's not the same week - change today's date to Monday of date2's week
        dateToday = date2;
        return false;
    }

    //TODO: we need to change the selected index in the script below so that the correct day is selected.
    // Currently, the first available day is selected (today)
    //this script clicks on the first free 'target' on the day
    private static String bookingScript2(String date){
        System.out.println("running bookingscript2 ");
        String timerStart = "setTimeout(function(){";
        String formattedDate = date.replaceAll("-", "");
        //String myFunction = "document.querySelectorAll(\"[data-dates='" + formattedDate + "']\")[1].click();";
        String myFunction = "document.getElementsByClassName('slotfreetarget slotfree2')[0].click();";
        String timerEnd = "}, 1600);";
        return "javascript:(function(){" + timerStart + myFunction + timerEnd + "})()";
        //return "javascript:(function(){" + myFunction + "})()";
    }

    //this script will open the start time dropdown
    private static String selectBookingTime(){
        String timerStart = "setTimeout(function(){";
        //String myFunction = "document.getElementsByClassName('timeslotdrop timeslotStart slotDays slotday0 slotday1 slotday2 slotday3 slotday4 slotday5 slotday6')[0].click();";
        String myFunction = " var dropdown = document.getElementsByClassName('timeslotdrop timeslotStart slotDays slotday0 slotday1 slotday2 slotday3 slotday4 slotday5 slotday6')[0];";
        String myFunction2 = "var event = document.createEvent('MouseEvents');\n" +
                "event.initMouseEvent('mousedown', true, true, window);" +
                "dropdown.dispatchEvent(event);" +
                "dropdown.children.style.opacity = 0;";
        //String myFunction2 = "document.getElementsByClassName('timeslotdrop timeslotEnd slotDays slotday0 slotday1 slotday2 slotday3 slotday4 slotday5 slotday6')[0].options[8].selected = true;";
        String timerEnd = "}, 2200);";
        return "javascript:(function(){" + timerStart + myFunction + myFunction2 + timerEnd + "})()";
    }

    //this script selects a specific option in the start time dropdown
    private static String selectBookingTime2(int selectIndex){
        System.out.println("running selectbookingtime2Script");
        String timerStart = "setTimeout(function(){";
        String myFunction = "document.getElementsByClassName('timeslotdrop timeslotStart slotDays slotday0 slotday1 slotday2 slotday3 slotday4 slotday5 slotday6')[0].options["+selectIndex+"].selected = true;";
        String myFunction2 = "$('.timeslotdrop').change();";
        String timerEnd = "}, 2200);";
        return "javascript:(function(){" + timerStart + myFunction + myFunction2 + timerEnd + "})()";
    }

    //this script clicks book and completes the booking - might be better if the user clicks him/herself
    private static String finishBookingScript(){
        String myFunction = "document.getElementById('continueRes2').click();";
        return "javascript:(function(){" + myFunction + "})()";
    }

    //Webview for API >= kitkat
    private class MyWebViewClient extends WebViewClient {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onPageFinished(final WebView view, final String url) {
            super.onPageFinished(view, url);

            ScriptCounter sc = ScriptCounter.getInstance();
            System.out.println("onPageFinished... Url: " + url + ", scripCount: " + sc.scriptCounter);
            //set loading image visible or invisible (user needs to see when he/she's login in)
            if(url.contains("cloud.timeedit.net")){
                imageView.setVisibility(View.VISIBLE);
                imageView.bringToFront();
                webView.setAlpha(0);
            }else{
                imageView.setVisibility(View.INVISIBLE);
                webView.setAlpha(1);
            }

                webView.evaluateJavascript(
                        "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String html) {
                                ScriptCounter sc = ScriptCounter.getInstance();
                                //Log.d("HTML", html);
                                htmlContent = html;

                                if(htmlContent.contains("itemsbox")){
                                    webView.evaluateJavascript(loginScript(), new ValueCallback<String>() {
                                        @Override
                                        public void onReceiveValue(String s) {
                                            Log.d("JS", "Response: " + s);
                                        }
                                    });
                                    //wait for user to login - no scripts
                                }else if(!url.contains("cloud.timeedit.net")){

                                }
                                else if(url.contains("cloud.timeedit.net") && !sc.bookingScriptStarted &&
                                bookingScriptsDone){ //this means that the user has completed the booking

                                    imageView.setVisibility(View.INVISIBLE);
                                    webView.setAlpha(1);
                                    //wait a bit so that the new booking is picked up by the REST API (doesn't always work it seems)
                                    try{
                                        Thread.sleep(2000);
                                        checkForNewBooking();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                else if(url.contains("cloud.timeedit.net") && !sc.bookingScriptStarted){
                                    sc.bookingScriptStarted = true;
                                    if(!firstBookingScriptDone) {
                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                        //get today's date
                                        dateToday = new Date();
                                        int daysBetweenToday = 0;
                                        try {
                                            Date parsedBookingDate = formatter.parse(booking_date);
                                            //check if booking date is monday or today's date
                                            SimpleDateFormat weekDayFormat = new SimpleDateFormat("E");
                                            Calendar cal1 = Calendar.getInstance();
                                            Calendar cal2 = Calendar.getInstance();
                                            cal1.setTime(dateToday);
                                            cal2.setTime(parsedBookingDate);
                                            boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                                                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
                                            if(weekDayFormat.format(parsedBookingDate).toLowerCase().equals("mon")
                                            || sameDay){
                                                //this will return 0 which is good because the booking date is a monday
                                                daysBetweenToday = weekDaysBetweenDates(parsedBookingDate, parsedBookingDate);
                                                System.out.println("date is monday... daysBetweenToday: " + daysBetweenToday);
                                            }else {
                                                //if booking date isn't a monday, we need to find how many
                                                // weekdays away the booking date is from monday of that specific week
                                                boolean currentWeek = isCurrentWeek(dateToday, parsedBookingDate);
                                                //calculate amount of weekdays between today and booking date
                                                daysBetweenToday = weekDaysBetweenDates(dateToday, parsedBookingDate);
                                                System.out.println("isCurrentWeek: " + currentWeek + ", daysbetween: " + daysBetweenToday);
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        try{
                                            Thread.sleep(300);
                                            //if amount of weekdays > 0, then we need to select the correct date via javascript
                                            if(daysBetweenToday > 0) {
                                                for(int i =0; i < daysBetweenToday; i++){
                                                    webView.evaluateJavascript(bookingScript(daysBetweenToday), new ValueCallback<String>() {
                                                        @Override
                                                        public void onReceiveValue(String s) {
                                                            Log.d("JS", "Response: " + s);
                                                        }
                                                    });
                                                }
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        firstBookingScriptDone = true;
                                    }
                                    webView.evaluateJavascript(bookingScript2(booking_date), new ValueCallback<String>() {
                                        @Override
                                        public void onReceiveValue(String s) {
                                            Log.d("JS", "Response: " + s);
                                        }
                                    });
                                    webView.evaluateJavascript(selectBookingTime2(timeSteps), new ValueCallback<String>() {
                                        @Override
                                        public void onReceiveValue(String s) {
                                            Log.d("JS", "Response: " + s);
                                        }
                                    });

                                    //wait a bit then run completeBookingProcess
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(2800);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        completeBookingProcess();
                                                    }
                                                });
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                }).start();
                                    /*
                                    //this is not needed now!

                                    webView.evaluateJavascript(selectBookingTime(), new ValueCallback<String>() {
                                        @Override
                                        public void onReceiveValue(String s) {
                                            Log.d("JS", "Response: " + s);
                                        }
                                    });
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Instrumentation inst = new Instrumentation();
                                                //begin after 2s, can be adjusted to make the automatic work faster
                                                Thread.sleep(2000);
                                                for (int i = 0; i < timeSteps+1; i++) {
                                                    Thread.sleep(5);
                                                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
                                                }
                                                //Thread.sleep(150);
                                                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
                                                Thread.sleep(100);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        completeBookingProcess();
                                                    }
                                                });
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                    */
                                }
                            }
                        });
            sc.scriptCounter++;
        }

        //booking_time is formatted as startTime-endTime, so we split to get startTime only.
        private String bookingStartTime (String bookingTime){
            String[] bookingTimeSplit = bookingTime.split("-");
            return bookingTimeSplit[0];
        }

        //this can be used to check if the new booking exists
        private void checkForNewBooking(){
            String requestUrl = url + "room/" + room;
            newBookingFound = false;
            System.out.println("checking for new booking... booking_date: " + booking_date);
            final String newBookingTime = bookingStartTime(booking_time);

            //clear cache before request
            AppController.getmInstance().getmRequestQueue().getCache().clear();

            // Request a json response from the provided URL
            JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, requestUrl, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                //loops through response data
                                for (int j = 0; j < response.length(); j++) {
                                    JSONObject JSONreservation = response.getJSONObject(j);
                                    String startDate = JSONreservation.getString("startDate");
                                    System.out.println("checkForNewBooking: response startDate: " + startDate);
                                    //if reservation is for today
                                    if (startDate.equals(booking_date)) {
                                        //sets start and end time for reservation
                                        String startTime = JSONreservation.getString("startTime");
                                        System.out.println("checkForNewBooking: booking found for date: " + booking_date + ", startTime: "
                                        + startTime + ", booking startime: " + newBookingTime);
                                        if(startTime.equals(newBookingTime)){
                                            //new booking found
                                            Toast.makeText(getApplicationContext(), "Booking succeeded!",
                                                    Toast.LENGTH_LONG).show();
                                            newBookingFound = true;
                                            //return to main activity when new booking is found?
                                            /*
                                            Intent intent = new Intent(getApplicationContext(),
                                                    MainActivity.class);
                                            startActivity(intent);
                                             */
                                            break;
                                        }
                                    }
                                }
                                if(!newBookingFound){
                                    //new booking not found
                                    Toast.makeText(getApplicationContext(), "Couldn't find new booking...",
                                            Toast.LENGTH_LONG).show();
                                }
                            }catch (Exception e) {
                                System.out.println("something wrong..");
                                e.printStackTrace();
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
    }

        private void completeBookingProcess(){
            //now the user can confirm the time and add description text for the booking
            imageView.setVisibility(View.INVISIBLE);
            webView.setAlpha(1);
            Toast.makeText(getApplicationContext(), "Please confirm" +
                            " booking time and add a description if you want!",
                    Toast.LENGTH_LONG).show();
            ScriptCounter.getInstance().resetScriptCounter();
            bookingScriptsDone = true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // The webView is about to navigate to the specified url.
            System.out.println("shouldOverride... " + url);
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    //when user navigates back
    @Override
    public void onBackPressed(){
        if(webView.canGoBack()){
            //webView.goBack();
            //navigate to main
            Intent intent = new Intent(getApplicationContext(),
                    MainActivity.class);
            startActivity(intent);
        }
        else{
            super.onBackPressed();
        }
    }

    /**
     * Below is a webview for API's < kitkat. It is similar but uses loadUrl instead of evalutate javascript.
     */

    //if API is lower than kitkat, do loadUrl instead of evaluateJavascript. A bit redundant code...
    private class MyLowApiWebViewClient extends WebViewClient {
        @SuppressLint("JavascriptInterface")
        @Override
        public void onPageFinished(final WebView view, final String url) {
            super.onPageFinished(view, url);
            ScriptCounter sc = ScriptCounter.getInstance();
            System.out.println("onPageFinished... Url: " + url + ", scripCount: " + sc.scriptCounter);
            //set loading image visible or invisible (user needs to see when he/she's login in)
            if (url.contains("cloud.timeedit.net")) {
                //imageView.setVisibility(View.VISIBLE);
                imageView.bringToFront();
                webView.setAlpha(1);
            } else {
                imageView.setVisibility(View.INVISIBLE);
                webView.setAlpha(1);
            }

            webView.loadUrl("javascript:window.HTMLOUT.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            //ScriptCounter sc = ScriptCounter.getInstance();
            //Log.d("HTML", html);
            //htmlContent = html;
            try{
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            htmlContent = HtmlStringValue.getInstance().myHtmlContent;

            if (htmlContent.contains("itemsbox")) {
                try{
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*
                webView.loadUrl("javascript:(function(){l=document.getElementsByClassName('items itemsbox')[0];"
                        +"e=document.createEvent('HTMLEvents');"
                        +"e.initEvent('click',true,true);"
                        +"l.dispatchEvent(e);})()");
                 */

                webView.loadUrl("javascript:(function(){setTimeout(function(){l=document.getElementsByClassName('items itemsbox')[0];"
                        +"e=document.createEvent('HTMLEvents');"
                        +"e.initEvent('click',true,true);"
                        +"l.dispatchEvent(e); }, 800);})()");

                //wait for user to login - no scripts
            } else if (!url.contains("cloud.timeedit.net")) {

            } else if (url.contains("cloud.timeedit.net") && !sc.bookingScriptStarted &&
                    bookingScriptsDone) { //this means that the user has completed the booking
                imageView.setVisibility(View.INVISIBLE);
                webView.setAlpha(1);
                //wait a bit so that the new booking is picked up by the REST API (doesn't always work it seems)
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkForNewBooking();
            } else if (url.contains("cloud.timeedit.net") && !sc.bookingScriptStarted) {
                sc.bookingScriptStarted = true;
                if(!firstBookingScriptDone) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    //get today's date
                    dateToday = new Date();
                    int daysBetweenToday = 0;
                    try {
                        Date parsedBookingDate = formatter.parse(booking_date);
                        //check if booking date is monday or today's date
                        SimpleDateFormat weekDayFormat = new SimpleDateFormat("E");
                        Calendar cal1 = Calendar.getInstance();
                        Calendar cal2 = Calendar.getInstance();
                        cal1.setTime(dateToday);
                        cal2.setTime(parsedBookingDate);
                        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
                        if(weekDayFormat.format(parsedBookingDate).toLowerCase().equals("mon")
                                || sameDay){
                            //this will return 0 which is good because the booking date is a monday
                            daysBetweenToday = weekDaysBetweenDates(parsedBookingDate, parsedBookingDate);
                            System.out.println("date is monday... daysBetweenToday: " + daysBetweenToday);
                        }else {
                            //if booking date isn't a monday, we need to find how many
                            // weekdays away the booking date is from monday of that specific week
                            boolean currentWeek = isCurrentWeek(dateToday, parsedBookingDate);
                            //calculate amount of weekdays between today and booking date
                            daysBetweenToday = weekDaysBetweenDates(dateToday, parsedBookingDate);
                            System.out.println("isCurrentWeek: " + currentWeek + ", daysbetween: " + daysBetweenToday);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    try{
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //if amount of weekdays > 0, then we need to select the correct date via javascript
                    if(daysBetweenToday > 0) {
                        for(int i =0; i < daysBetweenToday; i++){
                            /*
                            webView.loadUrl("javascript:(function(){l=document.getElementById('leftresdateinc');"
                                    +"e=document.createEvent('HTMLEvents');"
                                    +"e.initEvent('click',true,true);"
                                    +"l.dispatchEvent(e);})()");
                             */
                            webView.loadUrl("javascript:(function(){setTimeout(function(){l=document.getElementById('leftresdateinc');"
                                    +"e=document.createEvent('HTMLEvents');"
                                    +"e.initEvent('click',true,true);"
                                    +"l.dispatchEvent(e);}, 250);})()");
                        }
                    }
                    firstBookingScriptDone = true;
                }
                /*
                webView.loadUrl("javascript:(function(){l=document.getElementsByClassName('slotfreetarget slotfree2')[0];"
                        +"e=document.createEvent('HTMLEvents');"
                        +"e.initEvent('click',true,true);"
                        +"l.dispatchEvent(e);})()");
                        */
                webView.loadUrl("javascript:(function(){setTimeout(function(){l=document.getElementsByClassName('slotfreetarget slotfree2')[0];"
                        +"e=document.createEvent('HTMLEvents');"
                        +"e.initEvent('click',true,true);"
                        +"l.dispatchEvent(e);}, 1600);})()");

                /*
                webView.loadUrl("javascript:(function(){document.getElementsByClassName('timeslotdrop timeslotStart slotDays slotday0 slotday1 slotday2 slotday3 slotday4 slotday5 slotday6')[0].options["+timeSteps+"].selected = true;"
                        + "$('.timeslotdrop').change();})()");
                 */
                webView.loadUrl("javascript:(function(){setTimeout(function(){document.getElementsByClassName('timeslotdrop timeslotStart slotDays slotday0 slotday1 slotday2 slotday3 slotday4 slotday5 slotday6')[0].options["+timeSteps+"].selected = true;"
                        + "$('.timeslotdrop').change()}, 2300);})()");

                //wait a bit then run completeBookingProcess
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2800);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    completeBookingProcess();
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            sc.scriptCounter++;
        }

        //booking_time is formatted as startTime-endTime, so we split to get startTime only.
        private String bookingStartTime (String bookingTime){
            String[] bookingTimeSplit = bookingTime.split("-");
            return bookingTimeSplit[0];
        }

        private void completeBookingProcess(){
            //now the user can confirm the time and add description text for the booking
            imageView.setVisibility(View.INVISIBLE);
            webView.setAlpha(1);
            Toast.makeText(getApplicationContext(), "Please confirm" +
                            " booking time and add a description if you want!",
                    Toast.LENGTH_LONG).show();
            ScriptCounter.getInstance().resetScriptCounter();
            bookingScriptsDone = true;
        }

        //this can be used to check if the new booking exists
        private void checkForNewBooking(){
            String requestUrl = url + "room/" + room;
            newBookingFound = false;
            System.out.println("checking for new booking... booking_date: " + booking_date);
            final String newBookingTime = bookingStartTime(booking_time);

            //clear cache before request
            AppController.getmInstance().getmRequestQueue().getCache().clear();

            // Request a json response from the provided URL
            JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, requestUrl, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                //loops through response data
                                for (int j = 0; j < response.length(); j++) {
                                    JSONObject JSONreservation = response.getJSONObject(j);
                                    String startDate = JSONreservation.getString("startDate");
                                    System.out.println("checkForNewBooking: response startDate: " + startDate);
                                    //if reservation is for today
                                    if (startDate.equals(booking_date)) {
                                        //sets start and end time for reservation
                                        String startTime = JSONreservation.getString("startTime");
                                        System.out.println("checkForNewBooking: booking found for date: " + booking_date + ", startTime: "
                                                + startTime + ", booking startime: " + newBookingTime);
                                        if(startTime.equals(newBookingTime)){
                                            //new booking found
                                            Toast.makeText(getApplicationContext(), "Booking succeeded!",
                                                    Toast.LENGTH_LONG).show();
                                            newBookingFound = true;
                                            break;
                                        }
                                    }
                                }
                                if(!newBookingFound){
                                    //new booking not found
                                    Toast.makeText(getApplicationContext(), "Couldn't find new booking...",
                                            Toast.LENGTH_LONG).show();
                                }
                            }catch (Exception e) {
                                System.out.println("something wrong..");
                                e.printStackTrace();
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
        }
    }
    class MyJavaScriptInterface
    {
        @SuppressWarnings("unused")
        @JavascriptInterface
        public void showHTML(String html)
        {
            System.out.println("setting myhtmlcontent...");
            HtmlStringValue.getInstance().myHtmlContent = html;
        }
    }
}


