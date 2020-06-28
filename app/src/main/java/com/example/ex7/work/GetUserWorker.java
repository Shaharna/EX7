package com.example.ex7.work;

import android.content.Context;

import com.example.ex7.server.UserResponse;
import com.google.gson.Gson;
import com.example.ex7.data.Ticket;
import com.example.ex7.data.User;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Response;

public class GetUserWorker extends Worker {
    public GetUserWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;

        String userToken = getInputData().getString("key_user_token");
        try {
            Response<UserResponse> response = serverInterface.getUser("token "+userToken).execute();
            UserResponse userResponse = response.body();
            String userAsJson = new Gson().toJson(userResponse.data);

            Data outputData = new Data.Builder()
                    .putString("key_output_user", userAsJson)
                    .build();

            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
