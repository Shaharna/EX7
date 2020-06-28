package com.example.ex7.work;

import android.content.Context;

import com.example.ex7.data.Ticket;
import com.example.ex7.data.User;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Response;

public class ConnectivityCheckWorker extends Worker {
    public ConnectivityCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;

        try {
            Response<User> step1response = serverInterface.connectivityCheck().execute();
            return Result.success();

        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
