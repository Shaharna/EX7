package com.example.ex7.work;

import android.content.Context;

import com.google.gson.Gson;
import com.example.ex7.data.Ticket;
import com.example.ex7.data.User;
import com.example.ex7.server.MyOfficeServerInterface;
import com.example.ex7.server.ServerHolder;

import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Response;

public class CreateNewTicketWorker extends Worker {
    public CreateNewTicketWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;

        String ticketAsJson = getInputData().getString("key_input_ticket");
        Ticket ticket = new Gson().fromJson(ticketAsJson, Ticket.class);
        try {
            Response<Ticket> response = serverInterface.insertNewTicket(ticket).execute();
            Ticket responseBody = response.body();
            String responseAsJson = new Gson().toJson(responseBody);
            Data outputData = new Data.Builder()
                    .putString("key_output", responseAsJson)
                    .build();

            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
