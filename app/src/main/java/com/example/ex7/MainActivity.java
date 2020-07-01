package com.example.ex7;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.example.ex7.work.GetAllImagesWorker;
import com.example.ex7.work.GetUserTokenWorker;
import com.example.ex7.work.GetUserWorker;
import com.example.ex7.data.User;
import com.example.ex7.work.SetUserImageWorker;
import com.example.ex7.work.SetUserPrettyNameWorker;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity {

    public static final String HELLO_AGAIN_MSG = "Hello again, ";
    private String _prettyName;
    public static final String SP_USER_TOKEN = "user_token";
    private static String TAG = "MainActivity";
    private WorkManager _workManager;
    int check = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _workManager = WorkManager.getInstance(this);
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //sp.edit().clear().apply();
        String userToken = sp.getString(SP_USER_TOKEN, null);

        final Button setUserNameBtn = findViewById(R.id.main_set_user_name_btn);
        final Button setPrettyNameBtn = findViewById(R.id.main_set_pretty_name_btn);
        final Button erasePrettyNameBtn = findViewById(R.id.main_erase_pretty_name_btn);
        final EditText prettyNameText = findViewById(R.id.main_edit_pretty_name);
        final EditText usernameField = findViewById(R.id.main_edit_user_name);
        final ImageView imageView = findViewById(R.id.main_image_view);
        final Spinner allImagesSpinner = findViewById(R.id.main_images_spinner);
        final TextView allImagesHeader = findViewById(R.id.main_all_images_header);

        if (userToken == null) {
            ShowAndHideUI(setUserNameBtn, setPrettyNameBtn, erasePrettyNameBtn,
                    prettyNameText, usernameField, imageView, allImagesHeader, allImagesSpinner,
                    View.VISIBLE, View.INVISIBLE);
        } else {
            Log.d(TAG, "*** calling get user ***");
            getUser();
            ShowAndHideUI(setUserNameBtn, setPrettyNameBtn, erasePrettyNameBtn,
                    prettyNameText, usernameField, imageView,
                    allImagesHeader, allImagesSpinner, View.INVISIBLE, View.VISIBLE);
        }

        setUserNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = usernameField.getText().toString();
                Log.d(TAG, "*** calling get user token ***");
                ShowAndHideUI(setUserNameBtn, setPrettyNameBtn, erasePrettyNameBtn,
                        prettyNameText, usernameField, imageView,
                        allImagesHeader, allImagesSpinner, View.INVISIBLE, View.VISIBLE);
                getUserToken(userName);
            }
        });

        setPrettyNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _prettyName = prettyNameText.getText().toString();
                prettyNameText.getText().clear();
                setPrettyName();
            }
        });

        erasePrettyNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _prettyName = "";
                prettyNameText.getText().clear();
                setPrettyName();
            }
        });

        allImagesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                if(++check > 1) {
                    Object item = adapterView.getItemAtPosition(position);
                    if (item != null) {
                        setImageUrl(item.toString());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void ShowAndHideUI(Button setUserNameBtn, Button setPrettyNameBtn,
                               Button erasePrettyNameBtn, EditText prettyNameText,
                               EditText usernameField, ImageView imageView, TextView allImagesHeader,
                               Spinner allImagesSpinner,
                               int visible, int invisible) {
        usernameField.setVisibility(visible);
        setUserNameBtn.setVisibility(visible);
        prettyNameText.setVisibility(invisible);
        setPrettyNameBtn.setVisibility(invisible);
        erasePrettyNameBtn.setVisibility(invisible);
        imageView.setVisibility(invisible);
        allImagesHeader.setVisibility(invisible);
        allImagesSpinner.setVisibility(invisible);

    }

    private void getUserToken(String userName) {
        Log.d(TAG, "*** started get user token ***");
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest getTokenWork = new OneTimeWorkRequest.Builder(GetUserTokenWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("key_user_id", userName).build())
                .addTag(workTagUniqueId.toString())
                .build();

        _workManager.enqueue(getTokenWork);
        _workManager.getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                if (workInfos == null || workInfos.isEmpty())
                    return;

                WorkInfo info = workInfos.get(0);

                // now we can use it
                String userToken = info.getOutputData().getString("key_output_token");
                if (userToken != null) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(SP_USER_TOKEN, userToken);
                    editor.apply();

                    getUser();
                }
            }
        });
    }

    private void getUser() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userToken = sp.getString(SP_USER_TOKEN, null);
        Log.d(TAG, "*** started get user ***");
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest getUserWork = new OneTimeWorkRequest.Builder(GetUserWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("key_user_token", userToken).build())
                .addTag(workTagUniqueId.toString())
                .build();

        _workManager.enqueue(getUserWork);
        _workManager.getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
                // so check for that
                if (workInfos == null || workInfos.isEmpty())
                    return;

                WorkInfo info = workInfos.get(0);
                ProgressBar progressBar = findViewById(R.id.main_progress_bar);
                progressBar.setVisibility(View.VISIBLE);

                if (workInfos.get(0).getState() == WorkInfo.State.SUCCEEDED) {
                    // now we can use it
                    String userAsJson = info.getOutputData().getString("key_output_user");
                    Log.d(TAG, "got user: " + userAsJson);

                    User user = new Gson().fromJson(userAsJson, User.class);
                    // update UI with the user we got
                    updatePrettyNameUI(user);
                    progressBar.setVisibility(View.INVISIBLE);
                    getAllImages(user);
                }
                if (info.getState() == WorkInfo.State.FAILED) {
                    ShowFailedToLoadToast();
                }
            }
        });
    }

    private void setPrettyName() {
        Log.d(TAG, "*** started set pretty name ***");
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userToken = sp.getString(SP_USER_TOKEN, null);
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest setPrettyNameWorker = new OneTimeWorkRequest.Builder(SetUserPrettyNameWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("key_user_token", userToken)
                        .putString("key_user_pretty_name", _prettyName).build())
                .addTag(workTagUniqueId.toString())
                .build();

        _workManager.enqueue(setPrettyNameWorker);
        _workManager.getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                if (workInfos == null || workInfos.isEmpty())
                    return;
                WorkInfo info = workInfos.get(0);
                ProgressBar progressBar = findViewById(R.id.main_progress_bar);
                progressBar.setVisibility(View.VISIBLE);

                if (info.getState() == WorkInfo.State.SUCCEEDED) {
                    // now we can use it
                    String userAsJson = info.getOutputData().getString("key_output_user");
                    Log.d(TAG, "got user: " + userAsJson);

                    User user = new Gson().fromJson(userAsJson, User.class);
                    // update UI with the user we got
                    updatePrettyNameUI(user);
                    progressBar.setVisibility(View.INVISIBLE);
                }

                if (info.getState() == WorkInfo.State.FAILED) {
                    ShowFailedToLoadToast();
                }
            }
        });
    }

    private void ShowFailedToLoadToast() {
        Context context = getApplicationContext();
        CharSequence text = "Oops Something went Wrong";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void updatePrettyNameUI(User user) {
        TextView prettyNameView = findViewById(R.id.main_pretty_name_view);
        ImageView imageView = findViewById(R.id.main_image_view);
        String path = String.format("https://hujipostpc2019.pythonanywhere.com/%s", user.image_url);
        Picasso.get().load(path).into(imageView);
        if (user.pretty_name != null) {
            if (!user.pretty_name.equals("")) {
                prettyNameView.setText(String.format("%s%s", HELLO_AGAIN_MSG, user.pretty_name));
                return;
            }
        }
        prettyNameView.setText(String.format("%s%s", HELLO_AGAIN_MSG, user.username));
    }

    private void getAllImages(final User user) {
        Log.d(TAG, "*** started get all images ***");
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest getAllImagesWork = new OneTimeWorkRequest.Builder(GetAllImagesWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag(workTagUniqueId.toString())
                .build();

        _workManager.enqueue(getAllImagesWork);
        _workManager.getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
                // so check for that
                if (workInfos == null || workInfos.isEmpty())
                    return;

                WorkInfo info = workInfos.get(0);

                if (workInfos.get(0).getState() == WorkInfo.State.SUCCEEDED) {
                    // now we can use it

                    String[] allImages = info.getOutputData().getStringArray("key_output_all_images_list");
                    Log.d(TAG, "got user: " + allImages.toString());
                    setAllImagesSpinner(allImages, user);
                    // update UI with the user we got
                }
                if (info.getState() == WorkInfo.State.FAILED) {
                    ShowFailedToLoadToast();
                }
            }
        });
    }

    private void setAllImagesSpinner(String[] allImages, User user) {
        final Spinner imageSpinner = findViewById(R.id.main_images_spinner);
        ArrayList<String> imagesList = new ArrayList<>();
        String regex = "/images/(.*).png";
        Pattern r = Pattern.compile(regex);
        for (String imageUrl: allImages)
        {
            Matcher m = r.matcher(imageUrl);
            if (m.find())
            {
                String imageUIName = m.group(1);
                if (imageUrl.equals(user.image_url))
                {
                    imagesList.set(0, imageUIName);
                }
                else
                {
                    imagesList.add(imageUIName);
                }
            }
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, imagesList); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imageSpinner.setAdapter(spinnerArrayAdapter);

    }

    private void setImageUrl(String imageUrl) {
        Log.d(TAG, "*** started set image url ***");
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userToken = sp.getString(SP_USER_TOKEN, null);
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest setImageUrlWorker = new OneTimeWorkRequest.Builder(SetUserImageWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("key_user_token", userToken)
                        .putString("key_user_image_url", ("/images/").concat(imageUrl).concat(".png")).build())
                .addTag(workTagUniqueId.toString())
                .build();

        _workManager.enqueue(setImageUrlWorker);
        _workManager.getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                if (workInfos == null || workInfos.isEmpty())
                    return;
                WorkInfo info = workInfos.get(0);
                ProgressBar progressBar = findViewById(R.id.main_progress_bar);
                progressBar.setVisibility(View.VISIBLE);

                if (info.getState() == WorkInfo.State.SUCCEEDED) {
                    // now we can use it
                    String userAsJson = info.getOutputData().getString("key_output_user");
                    Log.d(TAG, "got user: " + userAsJson);

                    User user = new Gson().fromJson(userAsJson, User.class);
                    // update UI with the user we got
                    updatePrettyNameUI(user);
                    progressBar.setVisibility(View.INVISIBLE);
                }

                if (info.getState() == WorkInfo.State.FAILED) {
                    ShowFailedToLoadToast();
                }
            }
        });
    }

}