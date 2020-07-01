package com.example.ex7.work;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;
import com.example.ex7.server.TokenResponse;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Response;

public class GetUserTokenWorker extends Worker {
    public static final String SP_USER_TOKEN = "user_token";

    public GetUserTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;

        String userId = getInputData().getString("key_user_id");
        try {
            Response<TokenResponse> response = serverInterface.getToken(userId).execute();
            TokenResponse TokenResponse = response.body();
            String token = TokenResponse.data;

            Data outputData = new Data.Builder()
                    .putString("key_output_token", token)
                    .build();

            Log.d("TokenWorker", token);

            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
