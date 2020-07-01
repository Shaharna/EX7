package com.example.ex7.work;

import android.content.Context;
import android.util.Log;

import com.example.ex7.server.SetUserPrettyNameRequest;
import com.example.ex7.server.UserResponse;
import com.google.gson.Gson;
import com.example.ex7.data.User;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Response;

public class SetUserPrettyNameWorker extends Worker {

    private static final String PROGRESS = "PROGRESS";


    public SetUserPrettyNameWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        setProgressAsync(new Data.Builder().putInt(PROGRESS, 0).build());
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;

        String userToken = getInputData().getString("key_user_token");
        SetUserPrettyNameRequest prettyNameRequest = new SetUserPrettyNameRequest();
        prettyNameRequest.pretty_name = getInputData().getString("key_user_pretty_name");
        try {
            String tokenValue = "token " + userToken;
            Response<UserResponse> response = serverInterface.setPrettyName(tokenValue, prettyNameRequest).execute();
            User userResponse = response.body().data;
            String userAsJson = new Gson().toJson(userResponse);

            Data outputData = new Data.Builder()
                    .putString("key_output_user", userAsJson)
                    .build();

            // now we can use it
            Log.d("userWorker", "got user: " + userAsJson);

            User user = new Gson().fromJson(userAsJson, User.class);
            // update UI with the user we got

            setProgressAsync(new Data.Builder().putInt(PROGRESS, 100).build());

            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
