package com.example.ex7.work;

import android.content.Context;

import com.google.gson.Gson;
import com.example.ex7.data.Ticket;
import com.example.ex7.data.User;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Response;

public class GetAllTicketsForUserWorker extends Worker {
    public GetAllTicketsForUserWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;

        String userId = getInputData().getString("key_user_id");
        try {
            Response<List<Ticket>> response = serverInterface.getAllTicketsForUser(userId).execute();
            List<Ticket> userTickets = response.body();
            String ticketsAsJson = new Gson().toJson(userTickets);

            Data outputData = new Data.Builder()
                    .putString("key_output_tickets", ticketsAsJson)
                    .build();

            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
