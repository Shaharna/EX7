package com.example.ex7.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ex7.data.User;
import com.example.ex7.server.AllImagesResponse;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;
import com.google.gson.Gson;

import java.io.IOException;
import retrofit2.Response;

public class GetAllImagesWorker extends Worker {
    public GetAllImagesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;
        try {
            Response<AllImagesResponse> response = serverInterface.getAllImages().execute();
            AllImagesResponse imagesResponse = response.body();
            String[] allImages =imagesResponse.data;

            Data outputData = new Data.Builder()
                    .putStringArray("key_output_all_images_list", allImages)
                    .build();

            // now we can use it
            Log.d("AllImagesWorker", "got list: " + allImages.toString());

            // update UI with the user we got
            return ListenableWorker.Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return ListenableWorker.Result.retry();
        }
    }
}
