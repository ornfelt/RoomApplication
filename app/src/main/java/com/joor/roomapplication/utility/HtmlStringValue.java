package com.joor.roomapplication.utility;

//this singleton class provides utility by storing the htmlcontent in BookingActivity
public class HtmlStringValue {

        //instance of HtmlStringValue (singleton)
        private static HtmlStringValue single_instance = null;

        //string that contains html
        public String myHtmlContent;

        private HtmlStringValue(){
            //init string
            myHtmlContent = "";
        }

        public static HtmlStringValue getInstance(){
            if(single_instance == null){
                single_instance = new HtmlStringValue();
            }
            return single_instance;
        }

        public void resetTempValuesList(){
            myHtmlContent = "";
        }
    }
