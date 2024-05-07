package id.dkon.app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.dkon.app.app.App;
import id.dkon.app.common.ActivityBase;
import id.dkon.app.util.CustomRequest;

public class AppActivity extends ActivityBase {

    Button loginBtn, signupBtn, mLanguageBtn, mExploreBtn;

    ProgressBar progressBar;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_app);

      //  AppEventsLogger.activateApp(getApplication());

        // Get Firebase token


        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {

            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();

                App.getInstance().setGcmToken(token);

                Log.d("FCM Token", token);
            }
        });


        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                    if (task.isSuccessful() && task.getResult() != null) {

                        mLastLocation = task.getResult();

                        // Set geo data to App class

                        App.getInstance().setLat(mLastLocation.getLatitude());
                        App.getInstance().setLng(mLastLocation.getLongitude());

                        // Save data

                        App.getInstance().saveData();

                        // Send location data to server

                        App.getInstance().setLocation();

                    } else {

                        Log.d("GPS", "AppActivity getLastLocation:exception", task.getException());
                    }
                }
            });
        }

        loginBtn = (Button) findViewById(R.id.loginBtn);
        signupBtn = (Button) findViewById(R.id.signupBtn);
        mLanguageBtn = (Button) findViewById(R.id.languageBtn);
        mExploreBtn = (Button) findViewById(R.id.exploreBtn);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mExploreBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(AppActivity.this, StreamActivity.class);
                startActivity(i);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(AppActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(AppActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        mLanguageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<String> language_names = new ArrayList<String>();

                Resources r = getResources();
                Configuration c = r.getConfiguration();

                for (int i = 0; i < App.getInstance().getLanguages().size(); i++) {

                    language_names.add(App.getInstance().getLanguages().get(i).get("lang_name"));
                }

                AlertDialog.Builder b = new AlertDialog.Builder(AppActivity.this);
                b.setTitle(getText(R.string.title_select_language));

                b.setItems(language_names.toArray(new CharSequence[language_names.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        App.getInstance().setLanguage(App.getInstance().getLanguages().get(which).get("lang_id"));

                        App.getInstance().saveData();
                        App.getInstance().readData();

                        // Set App Language

                        App.getInstance().setLocale(App.getInstance().getLanguage());

                        setLanguageBtnTitle();
                    }
                });

                b.setNegativeButton(getText(R.string.action_cancel), null);

                AlertDialog d = b.create();
                d.show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                window.setStatusBarColor(getColor(R.color.app_bg));

            } else {

                window.setStatusBarColor(getApplicationContext().getResources().getColor(R.color.app_bg));
            }
        }

        // Night mode

        if (App.getInstance().getNightMode() == 1) {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        } else {

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void  onStart() {

        super.onStart();

        if (App.getInstance().isConnected() && App.getInstance().getId() != 0) {

            showLoadingScreen();

            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_AUTHORIZE, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (App.getInstance().authorize(response)) {

                                if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                    App.getInstance().updateGeoLocation();

                                    Intent intent = new Intent(AppActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                } else {

                                    showContentScreen();
                                    App.getInstance().logout();
                                }

                            } else {

                                showContentScreen();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    showContentScreen();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("clientId", CLIENT_ID);
                    params.put("accountId", Long.toString(App.getInstance().getId()));
                    params.put("accessToken", App.getInstance().getAccessToken());
                    params.put("appType", Integer.toString(APP_TYPE_ANDROID));
                    params.put("fcm_regId", App.getInstance().getGcmToken());

                    return params;
                }
            };

            App.getInstance().addToRequestQueue(jsonReq);

        } else {

            showContentScreen();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void setLanguageBtnTitle() {

        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(new Locale(App.getInstance().getLanguage()));

        mExploreBtn.setText(createConfigurationContext(config).getText(R.string.action_explore).toString());
        mLanguageBtn.setText(createConfigurationContext(config).getText(R.string.settings_language_label).toString() + ": " + App.getInstance().getLanguageNameByCode(App.getInstance().getLanguage()));

        loginBtn.setText(createConfigurationContext(config).getText(R.string.action_login).toString());
        signupBtn.setText(createConfigurationContext(config).getText(R.string.action_signup).toString());
    }

    public void showContentScreen() {

        setLanguageBtnTitle();

        progressBar.setVisibility(View.GONE);

        if (EXPLORE_FEATURE) {

            mExploreBtn.setVisibility(View.VISIBLE);

        } else {

            mExploreBtn.setVisibility(View.GONE);
        }

        mLanguageBtn.setVisibility(View.VISIBLE);
        loginBtn.setVisibility(View.VISIBLE);
        signupBtn.setVisibility(View.VISIBLE);
    }

    public void showLoadingScreen() {

        progressBar.setVisibility(View.VISIBLE);

        mExploreBtn.setVisibility(View.GONE);
        mLanguageBtn.setVisibility(View.GONE);
        loginBtn.setVisibility(View.GONE);
        signupBtn.setVisibility(View.GONE);
    }
}
