package com.example.ex7.server;

import android.content.Context;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerHolder {
    private static ServerHolder instance = null;


    public synchronized static ServerHolder getInstance() {
        if (instance != null)
            return instance;

        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("https://hujipostpc2019.pythonanywhere.com/") // notice the absence of the last slash!
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MyOfficeServerInterface serverInterface = retrofit.create(MyOfficeServerInterface.class);
        instance = new ServerHolder(serverInterface);
        return instance;
    }


    public final MyOfficeServerInterface serverInterface;

    private ServerHolder(MyOfficeServerInterface serverInterface) {
        this.serverInterface = serverInterface;
    }
}

