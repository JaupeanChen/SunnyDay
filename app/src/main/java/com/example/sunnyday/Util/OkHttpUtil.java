package com.example.sunnyday.Util;


import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class OkHttpUtil {
    public static void sendRequest(String string, Callback callback){

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(string)
                .build();
        client.newCall(request).enqueue(callback);





    }
}
