package com.joor.roomapplication.interfaces;

import org.json.JSONArray;

/**
 *  @author Daniel Arnesson
 *  Callback Interface
 */
public interface IRequestInterface {
    void onSuccess(JSONArray response);
}