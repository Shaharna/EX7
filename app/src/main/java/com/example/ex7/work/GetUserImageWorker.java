package com.example.ex7.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ex7.data.User;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;
import com.example.ex7.server.UserResponse;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class GetUserImageWorker extends Worker {

    public GetUserImageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;

        String userToken = getInputData().getString("key_user_token");
        try {
            String tokenValue = "token " + userToken;
            Call<UserResponse> test = serverInterface.getUser(tokenValue);
            Response<UserResponse> response= test.execute();
            UserResponse userResponse = response.body();
            String userAsJson = new Gson().toJson(userResponse);

            Data outputData = new Data.Builder()
                    .putString("key_output_user", userAsJson)
                    .build();

            // now we can use it
            Log.d("userWorker", "got user: " + userAsJson);

            User user = new Gson().fromJson(userAsJson, User.class);
            // update UI with the user we got

            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
