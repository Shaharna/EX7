package com.example.ex7;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.ex7.work.GetUserTokenWorker;
import com.example.ex7.work.ConnectivityCheckWorker;
import com.example.ex7.work.GetUserWorker;

import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {
    private static final String USER_ID = "3";
    public static final String SP_USER_TOKEN = "user_token";
    private static String TAG = "MainActivity";
    private String _userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkConnectivityAndSetUI();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        _userToken = sp.getString(SP_USER_TOKEN, null);
        if (_userToken == null)
        {
            getUserToken();
            _userToken = sp.getString(SP_USER_TOKEN, null);

        }
        getUser();

        //createSampleTicket();

    }


    private void checkConnectivityAndSetUI(){

        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(ConnectivityCheckWorker.class)

                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                // if we will remove the constraints - then the connectivity check will happen immediately
                // if we will add the constraints - then the connectivity check will happen only after we have access to the internet

                .build();

        Operation runningWork = WorkManager.getInstance().enqueue(checkConnectivityWork);

        runningWork.getState().observe(this, new Observer<Operation.State>() {
            @Override
            public void onChanged(Operation.State state) {
                if (state == null) return;

                if (state instanceof Operation.State.SUCCESS) {
                    // update UI - connected
                }
                else {
                    // update UI - not connected :(
                }
            }
        });
    }

    private void getUserToken(){
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(GetUserTokenWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("key_user_id", USER_ID).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(checkConnectivityWork);
        WorkManager.getInstance().getWorkInfoByIdLiveData(workTagUniqueId)
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo info) {
                        if (info != null && info.getState().isFinished()) {
                            String userToken = info.getOutputData().getString("key_output_user");
                            // ... do something with the result ...
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString(SP_USER_TOKEN, userToken);
                            editor.apply();

                            Log.d("MainActivity", userToken);
                        }
                    }
                });
    }
    private void getUser(){
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(GetUserWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("key_user_token", _userToken).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(checkConnectivityWork);
        WorkManager.getInstance().getWorkInfoByIdLiveData(workTagUniqueId)
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo info) {
                        if (info != null && info.getState().isFinished()) {
                            String user = info.getOutputData().getString("key_output_user");
                            // ... do something with the result ...

                            Log.d("MainActivity", user);
                        }
                    }
                });
    }
//
//    private void getAllTicketsForUser(){
//        UUID workTagUniqueId = UUID.randomUUID();
//        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(GetUserWorker.class)
//                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
//                .setInputData(new Data.Builder().putString("key_user_id", USER_ID).build())
//                .addTag(workTagUniqueId.toString())
//                .build();
//
//        WorkManager.getInstance().enqueue(checkConnectivityWork);
//
//        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
//            @Override
//            public void onChanged(List<WorkInfo> workInfos) {
//                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
//                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
//                // so check for that
//                if (workInfos == null || workInfos.isEmpty())
//                    return;
//
//                WorkInfo info = workInfos.get(0);
//
//                // now we can use it
//                String ticketsAsJson = info.getOutputData().getString("key_output_tickets");
//                List<Ticket> allTickets = new Gson().fromJson(ticketsAsJson, new TypeToken<List<Ticket>>(){}.getType());
//
//                Log.d(TAG, "got tickets list with size " + allTickets.size());
//
//
//                // update UI with the list we got
//            }
//        });
//    }
//
//
//    private void createSampleTicket(){
//        Ticket ticket = new Ticket();
//        ticket.id = 0;
//        ticket.user_id = Integer.valueOf(USER_ID);
//        ticket.title = "mock ticket";
//        ticket.completed = false;
//
//        String ticketAsJson = new Gson().toJson(ticket);
//
//        UUID workTagUniqueId = UUID.randomUUID();
//        OneTimeWorkRequest checkConnectivityWork = new OneTimeWorkRequest.Builder(CreateNewTicketWorker.class)
//                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
//                .setInputData(new Data.Builder().putString("key_input_ticket", ticketAsJson).build())
//                .addTag(workTagUniqueId.toString())
//                .build();
//
//        WorkManager.getInstance().enqueue(checkConnectivityWork);
//
//        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
//            @Override
//            public void onChanged(List<WorkInfo> workInfos) {
//                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
//                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
//                // so check for that
//                if (workInfos == null || workInfos.isEmpty())
//                    return;
//
//                WorkInfo info = workInfos.get(0);
//
//                // now we can use it
//                String ticketAsJson = info.getOutputData().getString("key_output");
//                Log.d(TAG, "got created ticket: " + ticketAsJson);
//                Ticket ticketResponse = new Gson().fromJson(ticketAsJson, Ticket.class);
//
//                // update UI with the ticket response.
//            }
//        });
//    }
}
