package id.dkon.app;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import id.dkon.app.app.App;
import id.dkon.app.common.ActivityBase;
import id.dkon.app.util.CustomRequest;
import id.dkon.app.util.Helper;

public class RegisterActivity extends ActivityBase {

    public static final int SELECT_PHOTO_IMG = 20;
    public static final int CREATE_PHOTO_IMG = 21;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;

    private Toolbar mToolbar;

    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;
    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<String[]> multiplePermissionLauncher;

    private Uri selectedImage;

    private String selectedImagePath = "", newImageFileName = "";

    private ViewPager mViewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout mMarkersLayout;
    private TextView[] markers;
    private int[] screens;
    private Button mButtonBack, mButtonFinish;

    private RelativeLayout mNavigator;

    // Google

    SignInButton mGoogleSignInButton;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private ActivityResultLauncher<Intent> googleSigninActivityResultLauncher;

    // Screen 0

    private EditText mUsername, mFullname, mPassword, mEmail, mReferrer;
    private LinearLayout mFacebookAuthContainer;
    private TextView mOauthTypeLabel;

    private TextView mButtonRegularAuth, mButtonTerms;

    private LoginButton mFacebookAuth;

    private Button mButtonContinue;

    // Screen 1

    private Button mButtonChoosePhoto;
    private CircularImageView mPhoto;

    // Screen 2

    private Button mButtonChooseAge;
    private Button mButtonChooseGender;


    // Screen 2, 3

    private ImageView mImage;

    // Screen 3

    private Button mButtonGrantLocationPermission;

    //

    private int age = 0, gender = 0; // gender: 0 - unknown; 1 = male; 2 = female
    private String username = "", password = "", email = "", language = "en", fullname = "", photo_url = "", referrer = "", oauth_id = "";
    private int oauth_type = 0;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

       // if (AccessToken.getCurrentAccessToken()!= null) LoginManager.getInstance().logOut();

        callbackManager = CallbackManager.Factory.create();

        //

        Intent i = getIntent();
        oauth_id = i.getStringExtra("oauth_id");

        if (oauth_id == null) {

            oauth_id = "";
        }

        oauth_type = i.getIntExtra("oauth_type", 0);

        //

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
              //  .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(RegisterActivity.this, gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            // User is signed in

            FirebaseAuth.getInstance().signOut();
        }

        googleSigninActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // There are no request codes
                    Intent data = result.getData();

                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                    try {

                        GoogleSignInAccount account = task.getResult(ApiException.class);

                        // Signed in successfully, show authenticated UI.

                        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                        mAuth.signInWithCredential(credential)
                                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {

                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (task.isSuccessful()) {

                                            // Sign in success, update UI with the signed-in user's information

                                            FirebaseUser user = mAuth.getCurrentUser();

                                            oauth_id = user.getUid();
                                            fullname = user.getDisplayName();
                                            email = user.getEmail();
                                            oauth_type = OAUTH_TYPE_GOOGLE;

                                            showpDialog();

                                            CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_GOOGLE_AUTH, null,
                                                    new Response.Listener<JSONObject>() {
                                                        @Override
                                                        public void onResponse(JSONObject response) {

                                                            if (App.getInstance().authorize(response)) {

                                                                if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                                                    App.getInstance().updateGeoLocation();

                                                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(intent);

                                                                } else {

                                                                    if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {

                                                                        App.getInstance().logout();
                                                                        Toast.makeText(RegisterActivity.this, getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();

                                                                    } else {

                                                                        App.getInstance().updateGeoLocation();

                                                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(intent);
                                                                    }
                                                                }

                                                            } else {

                                                                if (oauth_id.length() != 0) {

                                                                    updateView();

                                                                } else {

                                                                    Toast.makeText(RegisterActivity.this, getString(R.string.error_signin), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                            hidepDialog();
                                                        }
                                                    }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {

                                                    Toast.makeText(RegisterActivity.this, getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();

                                                    hidepDialog();
                                                }
                                            }) {

                                                @Override
                                                protected Map<String, String> getParams() {
                                                    Map<String, String> params = new HashMap<String, String>();
                                                    params.put("client_id", CLIENT_ID);
                                                    params.put("uid", oauth_id);
                                                    params.put("app_type", Integer.toString(APP_TYPE_ANDROID));
                                                    params.put("fcm_regId", App.getInstance().getGcmToken());

                                                    return params;
                                                }
                                            };

                                            App.getInstance().addToRequestQueue(jsonReq);

                                        } else {

                                            // If sign in fails, display a message to the user.
                                            Log.e("Google", "signInWithCredential:failure", task.getException());
                                            Toast.makeText(RegisterActivity.this, getText(R.string.error_data_loading), Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });

                    } catch (ApiException e) {

                        // The ApiException status code indicates the detailed failure reason.
                        // Please refer to the GoogleSignInStatusCodes class reference for more information.
                        Log.e("Google", "Google sign in failed", e);
                    }
                }
            }
        });

        //

        multiplePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = true;

            for (Map.Entry<String, Boolean> x : isGranted.entrySet())

                if (!x.getValue()) granted = false;

            if (granted) {

                Log.e("Permissions", "granted");

                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

                    mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {

                            if (task.isSuccessful() && task.getResult() != null) {

                                mLastLocation = task.getResult();

                                App.getInstance().setLat(mLastLocation.getLatitude());
                                App.getInstance().setLng(mLastLocation.getLongitude());

                            } else {

                                Log.d("GPS", "getLastLocation:exception", task.getException());
                            }
                        }
                    });
                }

                animateIcon(mImage);

                mButtonGrantLocationPermission.setEnabled(false);
                mButtonGrantLocationPermission.setText(R.string.action_grant_access_success);

            } else {

                Log.e("Permissions", "denied");

                mButtonGrantLocationPermission.setEnabled(true);
                mButtonGrantLocationPermission.setText(R.string.action_grant_access);
            }
        });

        imgFromGalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    // The document selected by the user won't be returned in the intent.
                    // Instead, a URI to that document will be contained in the return intent
                    // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

                    if (result.getData() != null) {

                        selectedImage = result.getData().getData();

                        newImageFileName = Helper.randomString(6) + ".jpg";

                        Helper helper = new Helper(App.getInstance().getApplicationContext());
                        helper.saveImg(selectedImage, newImageFileName);

                        selectedImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + newImageFileName;

                        mPhoto.setImageURI(null);
                        mPhoto.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));

                        updateView();
                    }
                }
            }
        });

        imgFromCameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK) {

                    if (result.getData() != null) {

                        selectedImage = result.getData().getData();

                        selectedImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + newImageFileName;

                        mPhoto.setImageURI(null);
                        mPhoto.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", new File(selectedImagePath)));

                        updateView();
                    }
                }
            }
        });

        //

        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> isGranted) -> {

            boolean granted = false;

            for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {

                if (x.getKey().equals(READ_EXTERNAL_STORAGE)) {

                    if (x.getValue()) {

                        granted = true;
                    }
                }
            }

            if (granted) {

                Log.e("Permissions", "granted");

                choiceImage();

            } else {

                Log.e("Permissions", "denied");

                Snackbar.make(findViewById(android.R.id.content), getString(R.string.label_no_storage_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(RegisterActivity.this, getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }

        });

        //

        setContentView(R.layout.activity_register);

        if (savedInstanceState != null) {

            age = savedInstanceState.getInt("age");
            gender = savedInstanceState.getInt("gender");

            username = savedInstanceState.getString("username");
            password = savedInstanceState.getString("password");
            email = savedInstanceState.getString("email");
            fullname = savedInstanceState.getString("fullname");
            referrer = savedInstanceState.getString("referrer");
            oauth_id = savedInstanceState.getString("oauth_id");
            oauth_type = savedInstanceState.getInt("oauth_type");
            selectedImagePath = savedInstanceState.getString("selectedImagePath");
            newImageFileName = savedInstanceState.getString("newImageFileName");
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.GONE);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mMarkersLayout = (LinearLayout) findViewById(R.id.layout_markers);

        mNavigator = (RelativeLayout) findViewById(R.id.navigator_layout);
        mNavigator.setVisibility(View.GONE);

        mButtonBack = (Button) findViewById(R.id.button_back);
        mButtonFinish = (Button) findViewById(R.id.button_next);

        screens = new int[]{
                R.layout.register_screen_1,
                R.layout.register_screen_2,
                R.layout.register_screen_3,
                R.layout.register_screen_4};

        addMarkers(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        mViewPager.setAdapter(myViewPagerAdapter);
        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        mViewPager.beginFakeDrag();

        mButtonBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);

                updateView();
            }
        });

        mButtonFinish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int current = mViewPager.getCurrentItem();

                if (current < screens.length - 1) {

                    switch (current) {

                        case 1: {

                            if (selectedImagePath.length() != 0) {

                                mViewPager.setCurrentItem(current + 1);

                            } else {

                                Toast.makeText(RegisterActivity.this, getString(R.string.register_screen_2_msg), Toast.LENGTH_SHORT).show();
                                animateIcon(mPhoto);
                            }

                            break;
                        }

                        case 2: {

                            if (gender > 0 && age > 4) {

                                mViewPager.setCurrentItem(current + 1);

                            } else {

                                Toast.makeText(RegisterActivity.this, getString(R.string.register_screen_3_msg), Toast.LENGTH_SHORT).show();
                                animateIcon(mImage);
                            }

                            break;
                        }

                        default: {

                            mViewPager.setCurrentItem(current + 1);

                            break;
                        }
                    }

                    updateView();

                } else {

                    register();
                }
            }
        });
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {

        for (int i = 0; i < signInButton.getChildCount(); i++) {

            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {

                TextView tv = (TextView) v;
                tv.setTextSize(15);
                tv.setTypeface(null, Typeface.NORMAL);
                tv.setText(buttonText);

                return;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putInt("age", age);
        outState.putInt("gender", gender);

        outState.putString("username", username);
        outState.putString("password", password);
        outState.putString("email", email);
        outState.putString("fullname", fullname);
        outState.putString("referrer", referrer);
        outState.putString("oauth_id", oauth_id);
        outState.putInt("oauth_type", oauth_type);
        outState.putString("selectedImagePath", selectedImagePath);
        outState.putString("newImageFileName", newImageFileName);
    }

    private void updateView() {

        int current = mViewPager.getCurrentItem();

        setStatusBarColor(this, current);
        //setToolBarColor(current);

        mToolbar.setVisibility(View.GONE);

        mNavigator.setVisibility(View.VISIBLE);

        switch (current) {

            case 0: {

                mToolbar.setVisibility(View.VISIBLE);
                mNavigator.setVisibility(View.GONE);

                if (username.length() != 0) {

                    mUsername.setText(username);
                }

                if (fullname.length() != 0) {

                    mFullname.setText(fullname);
                }

                if (password.length() != 0) {

                    mPassword.setText(password);
                }

                if (email.length() != 0) {

                    mEmail.setText(email);
                }

                mReferrer.setText(referrer);

                if (!GOOGLE_AUTHORIZATION) {

                   // mGoogleSignInButton.setVisibility(View.GONE);

                 //   mFacebookAuthContainer.setVisibility(View.GONE);
                }

                if (!FACEBOOK_AUTHORIZATION) {

                  //  mFacebookAuth.setVisibility(View.GONE);

                  //  mFacebookAuthContainer.setVisibility(View.GONE);
                }

                if (oauth_id.length() != 0) {

                  //  mFacebookAuthContainer.setVisibility(View.VISIBLE);
                 //   mFacebookAuth.setVisibility(View.GONE);
                   // mGoogleSignInButton.setVisibility(View.GONE);

                    if (oauth_type == OAUTH_TYPE_FACEBOOK) {

                        mOauthTypeLabel.setText(getString(R.string.label_authorization_via_facebook));
                    }

                    if (oauth_type == OAUTH_TYPE_GOOGLE) {

                        mOauthTypeLabel.setText(getString(R.string.label_authorization_via_google));
                    }

                } else {

                    mFacebookAuthContainer.setVisibility(View.GONE);
                    mFacebookAuth.setVisibility(View.GONE);
                    mGoogleSignInButton.setVisibility(View.GONE);
                }

                break;
            }

            case 2: {

                if (age != 0) {

                    mButtonChooseAge.setText(getString(R.string.action_choose_age) + ": " + Integer.toString(age));

                } else {

                    mButtonChooseAge.setText(getString(R.string.action_choose_age));
                }

                if (gender == 0) {

                    mButtonChooseGender.setText(getString(R.string.action_choose_gender));

                } else {

                    mButtonChooseGender.setText(getString(R.string.action_choose_gender) + ": " + Helper.getGenderTitle(this, gender));
                }

                break;
            }

            case 3: {

                if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mButtonGrantLocationPermission.setEnabled(false);
                    mButtonGrantLocationPermission.setText(R.string.action_grant_access_success);

                } else {

                    mButtonGrantLocationPermission.setEnabled(true);
                    mButtonGrantLocationPermission.setText(R.string.action_grant_access);
                }

                break;
            }

            default: {

                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    public int getColorWrapper(Context context, int id) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            return context.getColor(id);

        } else {

            //noinspection deprecation
            return context.getResources().getColor(id);
        }
    }

    public void setStatusBarColor(Activity act, int index) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            switch (index) {

                case 0: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.statusBarColor));

                    break;
                }

                case 1: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.register_screen_2));

                    break;
                }

                case 2: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.register_screen_3));

                    break;
                }

                case 3: {

                    window.setStatusBarColor(getColorWrapper(act, R.color.register_screen_4));

                    break;
                }

                default: {

                    window.setStatusBarColor(Color.TRANSPARENT);

                    break;
                }
            }
        }
    }

    private void addMarkers(int currentPage) {

        markers = new TextView[screens.length];

        mMarkersLayout.removeAllViews();

        for (int i = 0; i < markers.length; i++) {

            markers[i] = new TextView(this);
            markers[i].setText(Html.fromHtml("&#8226;"));
            markers[i].setTextSize(35);
            markers[i].setTextColor(getResources().getColor(R.color.grey_90));
            mMarkersLayout.addView(markers[i]);
        }

        if (markers.length > 0)

            markers[currentPage].setTextColor(getResources().getColor(R.color.white));
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {

            addMarkers(position);

            if (position == screens.length - 1) {

                mButtonFinish.setText(getString(R.string.action_finish));

            } else {

                mButtonFinish.setText(getString(R.string.action_next));
            }

            switch (position) {

                case 0: {

                    setStatusBarColor(RegisterActivity.this, 0);

                    break;
                }

                case 1: {

                    setStatusBarColor(RegisterActivity.this, 1);

                    break;
                }

                case 2: {

                    setStatusBarColor(RegisterActivity.this, 2);

                    break;
                }

                case 3: {

                    setStatusBarColor(RegisterActivity.this, 3);

                    break;
                }

                default: {

                    break;
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };


    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(screens[position], container, false);
            container.addView(view);

            switch (position) {

                case 0: {

                    mUsername = (EditText) view.findViewById(R.id.username_edit);
                    mFullname = (EditText) view.findViewById(R.id.fullname_edit);
                    mPassword = (EditText) view.findViewById(R.id.password_edit);
                    mEmail = (EditText) view.findViewById(R.id.email_edit);
                    mReferrer = (EditText) view.findViewById(R.id.referrer_edit);

                    // Google Button

                    mGoogleSignInButton = view.findViewById(R.id.google_sign_in_button);
                    mGoogleSignInButton.setSize(SignInButton.SIZE_WIDE);

                    setGooglePlusButtonText(mGoogleSignInButton, getString(R.string.action_signup_with_google));

                    mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                            googleSigninActivityResultLauncher.launch(signInIntent);
                        }
                    });

                    //

                    mFacebookAuthContainer = (LinearLayout) view.findViewById(R.id.facebook_auth_container);
                    mOauthTypeLabel = view.findViewById(R.id.oauth_type_label);

                    mFacebookAuth = (LoginButton) view.findViewById(R.id.button_facebook_login);
                    mFacebookAuth.setPermissions("public_profile"); // "email",

                    // Registering CallbackManager with the LoginButton
                //    mFacebookAuth.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

                    //    @Override
                   //     public void onSuccess(LoginResult loginResult) {

                            // Retrieving access token using the LoginResult
                    //        AccessToken accessToken = loginResult.getAccessToken();

                   //         useLoginInformation(accessToken);
                   //     }

                  //      @Override
                  //      public void onCancel() {

                  //      }
                 //       @Override
                  //      public void onError(FacebookException error) {

                 //       }
                 //   });

                    mButtonRegularAuth = (TextView) view.findViewById(R.id.button_regular_auth);
                    mButtonTerms = (TextView) view.findViewById(R.id.button_terms);

                    mButtonContinue = (Button) view.findViewById(R.id.button_continue);

                    mButtonContinue.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            hideKeyboard();

                            next();
                        }
                    });

                    mButtonRegularAuth.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            oauth_id = "";
                            oauth_type = 0;

                            updateView();
                        }
                    });

                    mButtonTerms.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(RegisterActivity.this, WebViewActivity.class);
                            i.putExtra("url", METHOD_APP_TERMS);
                            i.putExtra("title", getText(R.string.signup_label_terms_and_policies));
                            startActivity(i);
                        }
                    });

                    mUsername.addTextChangedListener(new TextWatcher() {

                        public void afterTextChanged(Editable s) {

                            if (App.getInstance().isConnected() && check_username()) {

                                CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_APP_CHECKUSERNAME, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {

                                                    if (response.getBoolean("error")) {

                                                        mUsername.setError(getString(R.string.error_login_taken));
                                                    }

                                                } catch (JSONException e) {

                                                    e.printStackTrace();

                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                        Log.e("Username()", error.toString());

                                    }
                                }) {

                                    @Override
                                    protected Map<String, String> getParams() {
                                        Map<String, String> params = new HashMap<String, String>();
                                        params.put("username", username);

                                        return params;
                                    }
                                };

                                App.getInstance().addToRequestQueue(jsonReq);
                            }
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }
                    });

                    mFullname.addTextChangedListener(new TextWatcher() {

                        public void afterTextChanged(Editable s) {

                            check_fullname();
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    });

                    mPassword.addTextChangedListener(new TextWatcher() {

                        public void afterTextChanged(Editable s) {

                            check_password();
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    });

                    mEmail.addTextChangedListener(new TextWatcher() {

                        public void afterTextChanged(Editable s) {

                            if (App.getInstance().isConnected() && check_email()) {

                                CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_APP_CHECK_EMAIL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {

                                                    if (response.getBoolean("error")) {

                                                        mEmail.setError(getString(R.string.error_email_taken));
                                                    }

                                                } catch (JSONException e) {

                                                    e.printStackTrace();

                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                        Log.e("Email()", error.toString());

                                    }
                                }) {

                                    @Override
                                    protected Map<String, String> getParams() {
                                        Map<String, String> params = new HashMap<String, String>();
                                        params.put("email", email);

                                        return params;
                                    }
                                };

                                App.getInstance().addToRequestQueue(jsonReq);
                            }
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }
                    });

                    break;
                }

                case 1: {

                    mPhoto = (CircularImageView) view.findViewById(R.id.photo_image);

                    if (newImageFileName != null && newImageFileName.length() > 0) {

                        mPhoto.setImageURI(FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(selectedImagePath)));
                    }

                    mButtonChoosePhoto = (Button) view.findViewById(R.id.button_choose_photo);

                    mButtonChoosePhoto.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            if (!checkPermission()) {

                                requestPermission();

                            } else {

                                choiceImage();
                            }
                        }
                    });

                    break;
                }

                case 2: {

                    mImage = (ImageView) view.findViewById(R.id.image);

                    mButtonChooseGender = (Button) view.findViewById(R.id.button_choose_gender);
                    mButtonChooseAge = (Button) view.findViewById(R.id.button_choose_age);

                    mButtonChooseGender.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            choiceGender();
                        }
                    });

                    mButtonChooseAge.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            choiceAge();
                        }
                    });

                    break;
                }

                case 3: {

                    mImage = (ImageView) view.findViewById(R.id.image);
                    mButtonGrantLocationPermission = (Button) view.findViewById(R.id.button_grant_location_permission);

                    mButtonGrantLocationPermission.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            grantLocationPermission();
                        }
                    });
                }
            }

            updateView();

            return view;
        }

        @Override
        public int getCount() {

            return screens.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            View view = (View) object;
            container.removeView(view);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case android.R.id.home: {

                finish();
                return true;
            }

            default: {

                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onBackPressed(){

        finish();
    }

    private void useLoginInformation(AccessToken accessToken) {

        /**
         Creating the GraphRequest to fetch user details
         1st Param - AccessToken
         2nd Param - Callback (which will be invoked once the request is successful)
         **/

        showpDialog();

        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {

            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                try {

                    if (object.has("id")) {

                        oauth_id = object.getString("id");
                    }

                    if (object.has("name")) {

                        fullname = object.getString("name");
                    }

                    if (object.has("email")) {

                        email = object.getString("email");
                    }

                    oauth_type = OAUTH_TYPE_FACEBOOK;

                } catch (JSONException e) {

                    Log.e("Facebook Login", "Could not parse malformed JSON: \"" + object.toString() + "\"");

                } finally {

                    if (AccessToken.getCurrentAccessToken() != null) LoginManager.getInstance().logOut();

                    if (!oauth_id.equals("")) {

                        signinByFacebookId();

                    } else {

                        hidepDialog();
                    }
                }
            }
        });

        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        // parameters.putString("fields", "id,name,email,picture.width(200)");
        parameters.putString("fields", "id, name");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
    }

    public void signinByFacebookId() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGINBYFACEBOOK, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (App.getInstance().authorize(response)) {

                            if (App.getInstance().getState() == ACCOUNT_STATE_ENABLED) {

                                go();

                            } else if (App.getInstance().getState() == ACCOUNT_STATE_BLOCKED) {

                                Toast.makeText(RegisterActivity.this, getText(R.string.msg_account_blocked), Toast.LENGTH_SHORT).show();

                            } else if (App.getInstance().getState() == ACCOUNT_STATE_DEACTIVATED) {

                                Toast.makeText(RegisterActivity.this, getText(R.string.msg_account_deactivated), Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            if (!oauth_id.equals("")) {

                                mFacebookAuth.setVisibility(View.GONE);

                                mFacebookAuthContainer.setVisibility(View.VISIBLE);

                                updateView();
                            }
                        }

                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("Facebook Login", "Error");

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("facebookId", oauth_id);
                params.put("clientId", CLIENT_ID);

                params.put("appType", Integer.toString(APP_TYPE_ANDROID));
                params.put("fcm_regId", App.getInstance().getGcmToken());

                params.put("lang", "en");

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    private void animateIcon(ImageView icon) {

        ScaleAnimation scale = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(175);
        scale.setInterpolator(new LinearInterpolator());

        icon.startAnimation(scale);
    }

    private void choiceImage() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        arrayAdapter.add(getString(R.string.action_gallery));
        arrayAdapter.add(getString(R.string.action_camera));

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    case 0: {

                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/jpeg");

                        imgFromGalleryActivityResultLauncher.launch(intent);

                        break;
                    }

                    default: {

                        try {

                            newImageFileName = Helper.randomString(6) + ".jpg";

                            selectedImage = FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newImageFileName));

                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImage);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            imgFromCameraActivityResultLauncher.launch(cameraIntent);

                        } catch (Exception e) {

                            Toast.makeText(RegisterActivity.this, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                        }

                        break;
                    }
                }

            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    private void choiceAge() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        for (int i = 4; i < 111; i++) {

            arrayAdapter.add(Integer.toString(i));
        }

        builderSingle.setTitle(getText(R.string.register_screen_3_title));


        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                age = which + 4;

                updateView();
            }
        });

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    private void choiceGender() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        arrayAdapter.add(getString(R.string.label_male));
        arrayAdapter.add(getString(R.string.label_female));
        arrayAdapter.add("no");

        builderSingle.setTitle(getText(R.string.action_choose_gender));


        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                gender = which + 1;

                updateView();
            }
        });

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        AlertDialog d = builderSingle.create();
        d.show();
    }

    private void grantLocationPermission() {

        if (ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)){

                multiplePermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});

            } else {

                multiplePermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
            }
        }
    }

    private void next() {

        if (verifyRegForm()) {

            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);

            updateView();
        }
    }

    private void go() {

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("signup", true);
        intent.putExtra("pageId", 2); // 2 = profile page
        startActivity(intent);
    }

    public Boolean verifyRegForm() {

        username = mUsername.getText().toString();
        fullname = mFullname.getText().toString();
        password = mPassword.getText().toString();
        email = mEmail.getText().toString();
        referrer = mReferrer.getText().toString();

        mUsername.setError(null);
        mFullname.setError(null);
        mPassword.setError(null);
        mEmail.setError(null);

        Helper helper = new Helper();

        if (username.length() == 0) {

            mUsername.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (username.length() < 5) {

            mUsername.setError(getString(R.string.error_small_username));

            return false;
        }

        if (!helper.isValidLogin(username)) {

            mUsername.setError(getString(R.string.error_wrong_format));

            return false;
        }

        if (fullname.length() == 0) {

            mFullname.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (fullname.length() < 2) {

            mFullname.setError(getString(R.string.error_small_fullname));

            return false;
        }

        if (password.length() == 0) {

            mPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            mPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            mPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        if (email.length() == 0) {

            mEmail.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (!helper.isValidEmail(email)) {

            mEmail.setError(getString(R.string.error_wrong_format));

            return false;
        }

        return true;
    }

    public Boolean check_username() {

        username = mUsername.getText().toString();

        Helper helper = new Helper();

        if (username.length() == 0) {

            mUsername.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (username.length() < 5) {

            mUsername.setError(getString(R.string.error_small_username));

            return false;
        }

        if (!helper.isValidLogin(username)) {

            mUsername.setError(getString(R.string.error_wrong_format));

            return false;
        }

        mUsername.setError(null);

        return  true;
    }

    public Boolean check_fullname() {

        fullname = mFullname.getText().toString();

        if (fullname.length() == 0) {

            mFullname.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (fullname.length() < 2) {

            mFullname.setError(getString(R.string.error_small_fullname));

            return false;
        }

        mFullname.setError(null);

        return  true;
    }

    public Boolean check_password() {

        password = mPassword.getText().toString();

        Helper helper = new Helper();

        if (password.length() == 0) {

            mPassword.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (password.length() < 6) {

            mPassword.setError(getString(R.string.error_small_password));

            return false;
        }

        if (!helper.isValidPassword(password)) {

            mPassword.setError(getString(R.string.error_wrong_format));

            return false;
        }

        mPassword.setError(null);

        return true;
    }

    public Boolean check_email() {

        email = mEmail.getText().toString();

        Helper helper = new Helper();

        if (email.length() == 0) {

            mEmail.setError(getString(R.string.error_field_empty));

            return false;
        }

        if (!helper.isValidEmail(email)) {

            mEmail.setError(getString(R.string.error_wrong_format));

            return false;
        }

        mEmail.setError(null);

        return true;
    }

    private void hideKeyboard() {

        View view = this.getCurrentFocus();

        if (view != null) {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void register() {

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SIGNUP, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.e("Profile", "Malformed JSON: \"" + response.toString() + "\"");

                        if (App.getInstance().authorize(response)) {

                            Log.e("Profile", "Malformed JSON: \"" + response.toString() + "\"");

                            // Upload profile photo

                            File file = new File(selectedImagePath);

                            final OkHttpClient client = new OkHttpClient();

                            client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

                            try {

                                RequestBody requestBody = new MultipartBuilder()
                                        .type(MultipartBuilder.FORM)
                                        .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("text/csv"), file))
                                        .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                                        .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                                        .addFormDataPart("imgType", Integer.toString(UPLOAD_TYPE_PHOTO))
                                        .build();

                                com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                                        .url(METHOD_PROFILE_UPLOAD_IMAGE)
                                        .addHeader("Accept", "application/json;")
                                        .post(requestBody)
                                        .build();

                                client.newCall(request).enqueue(new Callback() {

                                    @Override
                                    public void onFailure(com.squareup.okhttp.Request request, IOException e) {

                                        go();

                                        Log.e("failure", request.toString());
                                    }

                                    @Override
                                    public void onResponse(com.squareup.okhttp.Response response) throws IOException {

                                        String jsonData = response.body().string();

                                        Log.e("response", jsonData);

                                        try {

                                            JSONObject result = new JSONObject(jsonData);

                                            if (!result.getBoolean("error")) {

                                                if (result.has("lowPhotoUrl")) {

                                                    App.getInstance().setPhotoUrl(result.getString("lowPhotoUrl"));
                                                }

                                                if (result.has("moderateImgUrl")) {

                                                    App.getInstance().setPhotoUrl(result.getString("moderateImgUrl"));
                                                }
                                            }

                                            Log.d("My App", response.toString());

                                        } catch (Throwable t) {

                                            Log.e("My App", "Could not parse malformed JSON: \"" + t.getMessage() + "\"");

                                        } finally {

                                            go();
                                        }
                                    }
                                });

                            } catch (Exception ex) {
                                // Handle the error

                                go();
                            }

                        } else {

                            hidepDialog();

                            switch (App.getInstance().getErrorCode()) {

                                case ERROR_CLIENT_ID : {

                                    Toast.makeText(RegisterActivity.this, getString(R.string.error_client_id), Toast.LENGTH_SHORT).show();
                                }

                                case ERROR_CLIENT_SECRET : {

                                    Toast.makeText(RegisterActivity.this, getString(R.string.error_client_secret), Toast.LENGTH_SHORT).show();
                                }

                                case ERROR_LOGIN_TAKEN : {

                                    mViewPager.setCurrentItem(0);

                                    Toast.makeText(RegisterActivity.this, getString(R.string.error_login_taken), Toast.LENGTH_SHORT).show();

                                    break;
                                }

                                case ERROR_EMAIL_TAKEN : {

                                    mViewPager.setCurrentItem(0);

                                    Toast.makeText(RegisterActivity.this, getString(R.string.error_email_taken), Toast.LENGTH_SHORT).show();

                                    break;
                                }

                                case ERROR_MULTI_ACCOUNT : {

                                    Toast.makeText(RegisterActivity.this, getString(R.string.label_multi_account_msg), Toast.LENGTH_SHORT).show();

                                    break;
                                }

                                default: {

                                    Log.e("Profile", "Could not parse malformed JSON: \"" + response.toString() + "\"");
                                    break;
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("signup()", error.toString());

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("fullname", fullname);
                params.put("password", password);
                params.put("photo", photo_url);
                params.put("email", email);
                params.put("referrer", referrer);
                params.put("language", language);
                params.put("oauth_id", oauth_id);
                params.put("oauth_type", Integer.toString(oauth_type));
                params.put("gender", Integer.toString(gender));
                params.put("age", Integer.toString(age));
                params.put("clientId", CLIENT_ID);
                params.put("hash", Helper.md5(Helper.md5(username) + CLIENT_SECRET));
                params.put("appType", Integer.toString(APP_TYPE_ANDROID));
                params.put("fcm_regId", App.getInstance().getGcmToken());

                return params;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonReq.setRetryPolicy(policy);

        App.getInstance().addToRequestQueue(jsonReq);
    }

    private boolean checkPermission() {

        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            return true;
        }

        return false;
    }

    private void requestPermission() {

        storagePermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE});
    }
}
