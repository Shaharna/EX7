package com.example.ex7.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ex7.data.User;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;
import com.example.ex7.server.SetUserImageRequest;
import com.example.ex7.server.SetUserPrettyNameRequest;
import com.example.ex7.server.UserResponse;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Response;

public class SetUserImageWorker extends Worker {
    private static final String PROGRESS = "PROGRESS";

    public SetUserImageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        setProgressAsync(new Data.Builder().putInt(PROGRESS, 0).build());
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;

        String userToken = getInputData().getString("key_user_token");
        SetUserImageRequest imageUrlRequest = new SetUserImageRequest();
        imageUrlRequest.image_url = getInputData().getString("key_user_image_url");
        try {
            String tokenValue = "token " + userToken;
            Response<UserResponse> response = serverInterface.setImageUrl(tokenValue, imageUrlRequest).execute();
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

            return ListenableWorker.Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return ListenableWorker.Result.retry();
        }
    }
}
