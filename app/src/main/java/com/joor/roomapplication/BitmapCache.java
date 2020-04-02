package com.joor.roomapplication;

import android.graphics.Bitmap;

import com.android.volley.toolbox.ImageLoader;

class BitmapCache implements ImageLoader.ImageCache {
    @Override
    public Bitmap getBitmap(String url) {
        return null;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {

    }
}
