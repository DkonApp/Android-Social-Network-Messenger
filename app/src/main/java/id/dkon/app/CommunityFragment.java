package id.dkon.app;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.balysv.materialripple.MaterialRippleLayout;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import id.dkon.app.adapter.AdvancedItemListAdapter;
import id.dkon.app.adapter.FollowersSpotlightListAdapter;
import id.dkon.app.app.App;
import id.dkon.app.constants.Constants;
import id.dkon.app.model.Group;
import id.dkon.app.model.Item;
import id.dkon.app.model.Profile;
import id.dkon.app.util.Api;
import id.dkon.app.util.CustomRequest;
import id.dkon.app.util.Helper;

import static com.facebook.FacebookSdk.getApplicationContext;

public class CommunityFragment extends Fragment implements Constants, SwipeRefreshLayout.OnRefreshListener {

    private static final String STATE_LIST = "State Adapter Data";
    private static final String STATE_FOLLOWERS_SPOTLIGHT_LIST = "State Adapter Data 3";

    private ProgressDialog pDialog;

    private static final String TAG = CommunityFragment.class.getSimpleName();

    private static final int SELECT_PHOTO = 10000;
    private static final int CREATE_PHOTO = 10001;

    String [] names = {};

    Toolbar mToolbar;

    Button mFollowersSpotlightMoreButton;
    TextView mFollowersSpotlightTitle, mItemsSpotlightTitle, mFollowersSpotlightCount, mItemsSpotlightCount;
    CardView mFollowersSpotlight, mItemsSpotlight, mNewItemSpotlight;
    RecyclerView mFollowersSpotlightRecyclerView;

    private ArrayList<Profile> followersSpotlightList;
    private FollowersSpotlightListAdapter followersSpotlightAdapter;

    LinearLayout mAboutContainer, mLocationContainer, mUrlContainer;

    ImageView verifiedIcon, cover;

    TextView name, infoMessage, mErrorScreenMsg, mDisabledScreenMsg;
    TextView mLocationLabel, mUrlLabel, mAboutLabel;

    RelativeLayout mLoadingScreen, mErrorScreen, mDisabledScreen;
    LinearLayout mInfoContainer;

    SwipeRefreshLayout mRefreshLayout;
    NestedScrollView mNestedScrollView;
    RecyclerView mRecyclerView;

    private CircularImageView mNewItemImage;
    private TextView mNewItemTitle;

    private MaterialRippleLayout mNewItemButton, mMoreButton;
    private Button mActionButton;

    private BottomSheetBehavior mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private View mBottomSheet;

    private RelativeLayout mSetCoverButton;

    Group community;

    private ArrayList<Item> itemsList;
    private AdvancedItemListAdapter itemsAdapter;

    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    private ActivityResultLauncher<Intent> imgFromGalleryActivityResultLauncher;
    private ActivityResultLauncher<Intent> imgFromCameraActivityResultLauncher;

    private Uri selectedImage;

    private String selectedImagePath = "", newImageFileName = "";

    private Boolean loadingComplete = false;
    private Boolean loadingMore = false;
    private Boolean viewMore = false;

    private String community_mention;
    public long community_id;
    int itemId = 0;
    int arrayLength = 0;
    int accessMode = 0;

    private Boolean loading = false;
    private Boolean restore = false;
    private Boolean preload = false;
    private Boolean loaded = false;

    public CommunityFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(false);

        initpDialog();

        Intent i = getActivity().getIntent();

        community_id = i.getLongExtra("groupId", 0);
        community_mention = i.getStringExtra("groupMention");

        community = new Group();
        community.setId(community_id);

        itemsList = new ArrayList<Item>();
        itemsAdapter = new AdvancedItemListAdapter(getActivity(), itemsList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_community, container, false);

        if (savedInstanceState != null) {

            itemsList = savedInstanceState.getParcelableArrayList(STATE_LIST);
            itemsAdapter = new AdvancedItemListAdapter(getActivity(), itemsList);

            followersSpotlightList = savedInstanceState.getParcelableArrayList(STATE_FOLLOWERS_SPOTLIGHT_LIST);
            followersSpotlightAdapter = new FollowersSpotlightListAdapter(getActivity(), followersSpotlightList);

            itemId = savedInstanceState.getInt("itemId");

            restore = savedInstanceState.getBoolean("restore");
            loading = savedInstanceState.getBoolean("loading");
            preload = savedInstanceState.getBoolean("preload");
            loaded = savedInstanceState.getBoolean("loaded");

            community = savedInstanceState.getParcelable("communityObj");

        } else {

            itemsList = new ArrayList<Item>();
            itemsAdapter = new AdvancedItemListAdapter(getActivity(), itemsList);

            followersSpotlightList = new ArrayList<Profile>();
            followersSpotlightAdapter = new FollowersSpotlightListAdapter(getActivity(), followersSpotlightList);

            itemId = 0;

            restore = false;
            loading = false;
            preload = false;
            loaded = false;
        }

        if (loading) {


            showpDialog();
        }

        //

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

                        File f = new File(selectedImagePath);

                        uploadFile(METHOD_PROFILE_UPLOAD_IMAGE, f);
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

                        File f = new File(selectedImagePath);

                        uploadFile(METHOD_PROFILE_UPLOAD_IMAGE, f);
                    }
                }
            }
        });

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

                showChoiceImageDialog();

            } else {

                Log.e("Permissions", "denied");

                Snackbar.make(getView(), getString(R.string.label_no_storage_permission) , Snackbar.LENGTH_LONG).setAction(getString(R.string.action_settings), new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + App.getInstance().getPackageName()));
                        startActivity(appSettingsIntent);

                        Toast.makeText(getActivity(), getString(R.string.label_grant_storage_permission), Toast.LENGTH_SHORT).show();
                    }

                }).show();
            }

        });

        //

        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mNestedScrollView = (NestedScrollView) rootView.findViewById(R.id.nested_view);

        mLoadingScreen = (RelativeLayout) rootView.findViewById(R.id.loading_screen);
        mErrorScreen = (RelativeLayout) rootView.findViewById(R.id.error_screen);
        mDisabledScreen = (RelativeLayout) rootView.findViewById(R.id.disabled_screen);

        mErrorScreenMsg = (TextView) rootView.findViewById(R.id.error_screen_message);
        mDisabledScreenMsg = (TextView) rootView.findViewById(R.id.disabled_screen_message);

        // Prepare bottom sheet

        mBottomSheet = rootView.findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);

        // Prepare set cover button

        mSetCoverButton = (RelativeLayout) rootView.findViewById(R.id.setCoverButton);

        mSetCoverButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (!checkPermission()) {

                    requestPermission();

                } else {

                    showChoiceImageDialog();
                }
            }
        });

        // Start prepare action buttons

        mActionButton = (Button) rootView.findViewById(R.id.actionButton);

        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().getId() == community.getAuthorId()) {

                    Intent i = new Intent(getActivity(), GroupSettingsActivity.class);
                    i.putExtra("group_id", community.getId());
                    i.putExtra("group_name", community.getFullname());
                    i.putExtra("group_location", community.getLocation());
                    i.putExtra("group_site", community.getWebPage());
                    i.putExtra("year", community.getYear());
                    i.putExtra("day", community.getDay());
                    i.putExtra("month", community.getMonth());
                    i.putExtra("group_allow_comments", community.getAllowComments());
                    i.putExtra("group_allow_posts", community.getAllowPosts());
                    i.putExtra("group_category", community.getCategory());
                    i.putExtra("group_desc", community.getBio());
                    startActivityForResult(i, ACTION_EDIT);

                } else {

                    addFollower();
                }
            }
        });

        mNewItemImage = (CircularImageView) rootView.findViewById(R.id.newItemImage);

        mNewItemButton = (MaterialRippleLayout) rootView.findViewById(R.id.newItemButton);
        mMoreButton = (MaterialRippleLayout) rootView.findViewById(R.id.moreButton);

        mNewItemButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), NewItemActivity.class);
                intent.putExtra("groupId", community.getId());
                startActivityForResult(intent, ACTION_NEW);
            }
        });

        mMoreButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                showMoreDialog();
            }
        });

        // Start prepare Followers Spotlight

        mFollowersSpotlightTitle = (TextView) rootView.findViewById(R.id.followersSpotlightTitle);
        mFollowersSpotlightCount = (TextView) rootView.findViewById(R.id.followersSpotlightCount);
        mFollowersSpotlightMoreButton = (Button) rootView.findViewById(R.id.followersSpotlightMoreBtn);
        mFollowersSpotlight = (CardView) rootView.findViewById(R.id.followersSpotlight);
        mFollowersSpotlightRecyclerView = (RecyclerView) rootView.findViewById(R.id.followersSpotlightRecyclerView);

        mFollowersSpotlight.setVisibility(View.GONE);

        mFollowersSpotlightRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mFollowersSpotlightRecyclerView.setAdapter(followersSpotlightAdapter);

        followersSpotlightAdapter.setOnItemClickListener(new FollowersSpotlightListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Profile obj, int position) {

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("profileId", obj.getId());
                startActivity(intent);
            }
        });

        // Start prepare Items Spotlight

        mItemsSpotlightTitle = (TextView) rootView.findViewById(R.id.itemsSpotlightTitle);
        mItemsSpotlightCount = (TextView) rootView.findViewById(R.id.itemsSpotlightCount);
        mItemsSpotlight = (CardView) rootView.findViewById(R.id.itemsSpotlight);

        // Start prepare New Item Spotlight

        mNewItemSpotlight = (CardView) rootView.findViewById(R.id.newItemBox);

        //

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        itemsAdapter.setOnMoreButtonClickListener(new AdvancedItemListAdapter.OnItemMenuButtonClickListener() {

            @Override
            public void onItemClick(View v, Item obj, int actionId, int position) {

                switch (actionId){

                    case ITEM_ACTION_REPOST: {

                        if (obj.getFromUserId() != App.getInstance().getId()) {

                            if (obj.getRePostFromUserId() != App.getInstance().getId()) {

                                repost(position);

                            } else {

                                Toast.makeText(getActivity(), getActivity().getString(R.string.msg_not_make_repost), Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            Toast.makeText(getActivity(), getActivity().getString(R.string.msg_not_make_repost), Toast.LENGTH_SHORT).show();
                        }

                        break;
                    }

                    case ITEM_ACTIONS_MENU: {

                        showItemActionDialog(position);

                        break;
                    }
                }
            }
        });

        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(itemsAdapter);

        mRecyclerView.setNestedScrollingEnabled(false);

        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {

            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {

                    if (!loadingMore && (viewMore) && !(mRefreshLayout.isRefreshing())) {

                        mRefreshLayout.setRefreshing(true);

                        loadingMore = true;

                        getItems();
                    }
                }
            }
        });

        name = (TextView) rootView.findViewById(R.id.name);

        mInfoContainer = (LinearLayout) rootView.findViewById(R.id.infoContainer);

        mLocationContainer = (LinearLayout) rootView.findViewById(R.id.locationContainer);
        mLocationLabel = (TextView) rootView.findViewById(R.id.locationLabel);

        mUrlContainer = (LinearLayout) rootView.findViewById(R.id.urlContainer);
        mUrlLabel = (TextView) rootView.findViewById(R.id.urlLabel);

        mAboutContainer = (LinearLayout) rootView.findViewById(R.id.aboutContainer);
        mAboutLabel = (TextView) rootView.findViewById(R.id.aboutLabel);

        verifiedIcon = (ImageView) rootView.findViewById(R.id.verifiedIcon);


        mUrlContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!community.getWebPage().startsWith("https://") && !community.getWebPage().startsWith("http://")){

                    community.setWebPage("http://" + community.getWebPage());
                }

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(community.getWebPage()));
                startActivity(i);
            }
        });

        mFollowersSpotlightMoreButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), FollowersActivity.class);
                intent.putExtra("profileId", community.getId());
                startActivity(intent);
            }
        });

        infoMessage = (TextView) rootView.findViewById(R.id.info_message);
        infoMessage.setVisibility(View.GONE);

        cover = (ImageView) rootView.findViewById(R.id.cover);

        cover.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (community.getNormalPhotoUrl().length() > 0) {

                    Intent i = new Intent(getActivity(), PhotoViewActivity.class);
                    i.putExtra("imgUrl", community.getNormalPhotoUrl());
                    startActivity(i);
                }
            }
        });

        if (community.getFullname() == null || community.getFullname().length() == 0) {

            if (App.getInstance().isConnected()) {

                showLoadingScreen();
                getData();

                Log.e("Group", "OnReload");

            } else {

                showErrorScreen();
            }

        } else {

            if (App.getInstance().isConnected()) {

                if (community.getState() == ACCOUNT_STATE_ENABLED) {

                    showContentScreen();

                    loadingComplete();
                    updateView();

                } else {

                    showDisabledScreen();
                }

            } else {

                showErrorScreen();
            }
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putInt("itemId", itemId);

        outState.putBoolean("restore", restore);
        outState.putBoolean("loading", loading);
        outState.putBoolean("preload", preload);
        outState.putBoolean("loaded", loaded);

        outState.putParcelable("communityObj", community);
        outState.putParcelableArrayList(STATE_LIST, itemsList);
        outState.putParcelableArrayList(STATE_FOLLOWERS_SPOTLIGHT_LIST, followersSpotlightList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_EDIT && resultCode == getActivity().RESULT_OK) {

            community.setFullname(data.getStringExtra("group_name"));
            community.setLocation(data.getStringExtra("group_location"));
            community.setWebPage(data.getStringExtra("group_site"));
            community.setBio(data.getStringExtra("group_desc"));

            community.setCategory(data.getIntExtra("group_category", 0));
            community.setAllowPosts(data.getIntExtra("group_allow_posts", 0));
            community.setAllowComments(data.getIntExtra("group_allow_comments", 0));

            community.setYear(data.getIntExtra("year", 0));
            community.setMonth(data.getIntExtra("month", 0));
            community.setDay(data.getIntExtra("day", 0));

            updateView();

        } else if (requestCode == ACTION_NEW && resultCode == getActivity().RESULT_OK) {

            getData();

        } else if (requestCode == ITEM_EDIT && resultCode == getActivity().RESULT_OK) {

            int position = data.getIntExtra("position", 0);

            if (data.getExtras() != null) {

                Item item = (Item) data.getExtras().getParcelable("item");

                itemsList.set(position, item);
            }

            itemsAdapter.notifyDataSetChanged();

        } else if (requestCode == ITEM_REPOST && resultCode == getActivity().RESULT_OK) {

            int position = data.getIntExtra("position", 0);

            Item item = itemsList.get(position);

            item.setMyRePost(true);
            item.setRePostsCount(item.getRePostsCount() + 1);

            itemsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefresh() {

        if (App.getInstance().isConnected()) {

            getData();

        } else {

            mRefreshLayout.setRefreshing(false);
        }
    }

    public void updateView() {

        mFollowersSpotlightCount.setText(community.getFollowersCount() + " " + getString(R.string.label_followers));
        mItemsSpotlightCount.setText(community.getItemsCount() + " " + getString(R.string.label_items));

        verifiedIcon.setVisibility(View.GONE);
        mInfoContainer.setVisibility(View.GONE);

        mLocationContainer.setVisibility(View.GONE);
        mUrlContainer.setVisibility(View.GONE);
        mAboutContainer.setVisibility(View.GONE);

        name.setText(community.getFullname());

        if (community.getLocation().length() > 0) {

            mInfoContainer.setVisibility(View.VISIBLE);

            mLocationContainer.setVisibility(View.VISIBLE);
            mLocationLabel.setText(community.getLocation());
        }

        if (community.getWebPage().length() > 0) {

            mInfoContainer.setVisibility(View.VISIBLE);

            mUrlContainer.setVisibility(View.VISIBLE);
            mUrlLabel.setText(community.getWebPage());
        }

        if (community.getBio().length() > 0) {

            mInfoContainer.setVisibility(View.VISIBLE);

            mAboutContainer.setVisibility(View.VISIBLE);
            mAboutLabel.setText(community.getBio());
        }

        if (community.isVerify()) {

            verifiedIcon.setVisibility(View.VISIBLE);
        }

        if (community.getAllowPosts() == 0 && App.getInstance().getId() != community.getAuthorId()) {

            mNewItemSpotlight.setVisibility(View.GONE);

        } else {

            mNewItemSpotlight.setVisibility(View.VISIBLE);
        }

        // hide follow button is your group
        if (community.getAuthorId() == App.getInstance().getId()) {

            mSetCoverButton.setVisibility(View.VISIBLE);
            mActionButton.setText(R.string.action_profile_edit);

            if (community.getNormalPhotoUrl().length() != 0) {

                ImageLoader imageLoader = App.getInstance().getImageLoader();

                imageLoader.get(community.getNormalPhotoUrl(), ImageLoader.getImageListener(mNewItemImage, R.drawable.profile_default_photo, R.drawable.profile_default_photo));
            }

        } else {

            if (App.getInstance().getPhotoUrl().length() != 0) {

                ImageLoader imageLoader = App.getInstance().getImageLoader();

                imageLoader.get(App.getInstance().getPhotoUrl(), ImageLoader.getImageListener(mNewItemImage, R.drawable.profile_default_photo, R.drawable.profile_default_photo));
            }

            mSetCoverButton.setVisibility(View.GONE);

            if (community.isFollow()) {

                mActionButton.setText(R.string.action_group_leave);

            } else {

                mActionButton.setText(R.string.action_group_join);
            }
        }

        showCover(community.getNormalPhotoUrl());

        showContentScreen();
    }

    public void getData() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GROUP_GET, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "GroupFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                community = new Group(response);

                                if (community.getItemsCount() > 0) {

                                    getItems();
                                }

                                if (community.getState() == ACCOUNT_STATE_ENABLED) {

                                    showContentScreen();

                                    updateView();

                                } else {

                                    showDisabledScreen();
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                            Log.d("Response GetData", response.toString());

                        } finally {

                            if (community.getState() == ACCOUNT_STATE_ENABLED && community.getFollowersCount() > 0) {

                                getFollowersSpotlight();
                            }

                            loaded = true;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "GroupFragment Not Added to Activity");

                    return;
                }

                Log.e("Error GetData", error.toString());

                showErrorScreen();

                loaded = true;
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("groupId", Long.toString(community_id));

                return params;
            }
        };

        jsonReq.setRetryPolicy(new RetryPolicy() {

            @Override
            public int getCurrentTimeout() {

                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {

                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void getFollowersSpotlight() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PROFILE_FOLLOWERS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "GroupFragment Not Added to Activity");

                            return;
                        }

                        try {

                            followersSpotlightList.clear();

                            if (!response.getBoolean("error")) {

                                if (response.has("friends")) {

                                    JSONArray itemsArray = response.getJSONArray("friends");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject userObj = (JSONObject) itemsArray.get(i);

                                            Profile item = new Profile(userObj);

                                            followersSpotlightList.add(item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.d("Followers", response.toString());

                            loadingComplete();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "GroupFragment Not Added to Activity");

                    return;
                }

                Log.e("getFollowersSpotlight", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("profileId", Long.toString(community_id));
                params.put("itemId", Integer.toString(0));
                params.put("language", "en");

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void addFollower() {

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GROUP_FOLLOW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "GroupFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!response.getBoolean("error")) {

                                community.setFollowersCount(response.getInt("followersCount"));

                                community.setFollow(response.getBoolean("follow"));

                                if (response.getBoolean("follow")) {

                                    mActionButton.setText(getString(R.string.action_group_leave));

                                } else {

                                    mActionButton.setText(getString(R.string.action_group_join));
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "GroupFragment Not Added to Activity");

                    return;
                }

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("groupId", Long.toString(community_id));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void showCover(String coverUrl) {

        if (coverUrl.length() > 0) {

            ImageLoader imageLoader = App.getInstance().getImageLoader();

            imageLoader.get(coverUrl, ImageLoader.getImageListener(cover, R.drawable.profile_default_cover, R.drawable.profile_default_cover));

            if (Build.VERSION.SDK_INT > 15) {

                cover.setImageAlpha(200);
            }
        }
    }

    public void getItems() {

        if (loadingMore) {

            mRefreshLayout.setRefreshing(true);

        } else{

            itemId = 0;
        }

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_GROUP_GET_WALL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!isAdded() || getActivity() == null) {

                            Log.e("ERROR", "GroupFragment Not Added to Activity");

                            return;
                        }

                        try {

                            if (!loadingMore) {

                                itemsList.clear();
                            }

                            arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                itemId = response.getInt("itemId");

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            Item item = new Item(itemObj);

                                            itemsList.add(item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loadingComplete();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (!isAdded() || getActivity() == null) {

                    Log.e("ERROR", "GroupFragment Not Added to Activity");

                    return;
                }

                loadingComplete();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("groupId", Long.toString(community.getId()));
                params.put("itemId", Integer.toString(itemId));
                params.put("accessMode", Integer.toString(accessMode));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void loadingComplete() {

        if (arrayLength == LIST_ITEMS) {

            viewMore = true;

        } else {

            viewMore = false;
        }

        itemsAdapter.notifyDataSetChanged();
        followersSpotlightAdapter.notifyDataSetChanged();

        if (followersSpotlightAdapter.getItemCount() > 0) {

            mFollowersSpotlight.setVisibility(View.VISIBLE);
        }

        mRefreshLayout.setRefreshing(false);

        loadingMore = false;
    }

    public void showLoadingScreen() {

        mRefreshLayout.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);
        mDisabledScreen.setVisibility(View.GONE);

        mLoadingScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showErrorScreen() {

        mLoadingScreen.setVisibility(View.GONE);
        mDisabledScreen.setVisibility(View.GONE);
        mRefreshLayout.setVisibility(View.GONE);

        mErrorScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showDisabledScreen() {

        if (community.getState() != ACCOUNT_STATE_ENABLED) {

            mDisabledScreenMsg.setText(getText(R.string.msg_account_blocked));
        }

        getActivity().setTitle(getText(R.string.label_account_disabled));

        mRefreshLayout.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mDisabledScreen.setVisibility(View.VISIBLE);

        loadingComplete = false;
    }

    public void showContentScreen() {

        getActivity().setTitle(community.getFullname());

        mDisabledScreen.setVisibility(View.GONE);
        mLoadingScreen.setVisibility(View.GONE);
        mErrorScreen.setVisibility(View.GONE);

        mRefreshLayout.setVisibility(View.VISIBLE);
        mRefreshLayout.setRefreshing(false);

        loadingComplete = true;
        restore = true;
    }

    public void delete(final int position) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getText(R.string.label_delete));

        alertDialog.setMessage(getText(R.string.label_delete_msg));
        alertDialog.setCancelable(true);

        alertDialog.setNegativeButton(getText(R.string.action_no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton(getText(R.string.action_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                final Item item = itemsList.get(position);

                itemsList.remove(position);
                itemsAdapter.notifyDataSetChanged();

                community.setItemsCount(community.getItemsCount() - 1);

                updateView();

                if (itemsAdapter.getItemCount() == 0) {


                } else {

                    showContentScreen();
                }

                if (App.getInstance().isConnected()) {

                    Api api = new Api(getActivity());

                    api.postDelete(item.getId(), 0);

                } else {

                    Toast.makeText(getActivity(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.show();
    }

    public void share(final int position) {

        final Item item = itemsList.get(position);

        Api api = new Api(getActivity());
        api.postShare(item);
    }

    public void repost(final int position) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getText(R.string.label_post_share));

        alertDialog.setMessage(getText(R.string.label_post_share_desc));
        alertDialog.setCancelable(true);

        alertDialog.setNegativeButton(getText(R.string.action_no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton(getText(R.string.action_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                Item item = itemsList.get(position);

                Intent i = new Intent(getActivity(), NewItemActivity.class);
                i.putExtra("position", position);
                i.putExtra("repost", item);
                startActivityForResult(i, ITEM_REPOST);
            }
        });

        alertDialog.show();
    }

    public void report(final int position) {

        String[] profile_report_categories = new String[] {

                getText(R.string.label_profile_report_0).toString(),
                getText(R.string.label_profile_report_1).toString(),
                getText(R.string.label_profile_report_2).toString(),
                getText(R.string.label_profile_report_3).toString(),

        };

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getText(R.string.label_post_report_title));

        alertDialog.setSingleChoiceItems(profile_report_categories, 0, null);
        alertDialog.setCancelable(true);

        alertDialog.setNegativeButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                AlertDialog alert = (AlertDialog) dialog;
                int reason = alert.getListView().getCheckedItemPosition();

                final Item item = itemsList.get(position);

                Api api = new Api(getActivity());

                api.newReport(item.getId(), REPORT_TYPE_ITEM, reason);

                Toast.makeText(getActivity(), getActivity().getString(R.string.label_post_reported), Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }


    protected void initpDialog() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void profileReport() {

        String[] profile_report_categories = new String[] {

                getText(R.string.label_profile_report_0).toString(),
                getText(R.string.label_profile_report_1).toString(),
                getText(R.string.label_profile_report_2).toString(),
                getText(R.string.label_profile_report_3).toString(),

        };

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getText(R.string.label_post_report_title));

        alertDialog.setSingleChoiceItems(profile_report_categories, 0, null);
        alertDialog.setCancelable(true);

        alertDialog.setNegativeButton(getText(R.string.action_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.setPositiveButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                AlertDialog alert = (AlertDialog) dialog;
                int reason = alert.getListView().getCheckedItemPosition();

                Api api = new Api(getActivity());

                api.newReport(community.getId(), REPORT_TYPE_COMMUNITY, reason);

                Toast.makeText(getActivity(), getText(R.string.label_profile_reported), Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.show();
    }

    public Boolean uploadFile(String serverURL, File file) {

        loading = true;

        showpDialog();

        final OkHttpClient client = new OkHttpClient();

        client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

        try {

            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("uploaded_file", file.getName(), RequestBody.create(MediaType.parse("text/csv"), file))
                    .addFormDataPart("accountId", Long.toString(App.getInstance().getId()))
                    .addFormDataPart("accessToken", App.getInstance().getAccessToken())
                    .addFormDataPart("imgType", Integer.toString(IMAGE_TYPE_PROFILE_PHOTO))
                    .addFormDataPart("groupId", Long.toString(community_id))
                    .build();

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(serverURL)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(com.squareup.okhttp.Request request, IOException e) {

                    loading = false;

                    hidepDialog();

                    Log.e("failure", request.toString());
                }

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {

                    String jsonData = response.body().string();

                    Log.e("response", jsonData);

                    try {

                        JSONObject result = new JSONObject(jsonData);

                        if (!result.getBoolean("error")) {

                            community.setLowPhotoUrl(result.getString("lowPhotoUrl"));
                            community.setBigPhotoUrl(result.getString("bigPhotoUrl"));
                            community.setNormalPhotoUrl(result.getString("normalPhotoUrl"));
                        }

                        Log.d("My App", response.toString());

                    } catch (Throwable t) {

                        Log.e("My App", "Could not parse malformed JSON: \"" + response.body().string() + "\"");

                    } finally {

                        loading = false;

                        hidepDialog();

                        getData();
                    }

                }
            });

            return true;

        } catch (Exception ex) {
            // Handle the error

            loading = false;

            hidepDialog();
        }

        return false;
    }

    private void showMoreDialog() {

        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.profile_sheet_list, null);

        MaterialRippleLayout mRefreshButton = (MaterialRippleLayout) view.findViewById(R.id.refresh_button);
        MaterialRippleLayout mEditButton = (MaterialRippleLayout) view.findViewById(R.id.edit_button);
        MaterialRippleLayout mGiftButton = (MaterialRippleLayout) view.findViewById(R.id.gift_button);
        MaterialRippleLayout mOpenUrlButton = (MaterialRippleLayout) view.findViewById(R.id.open_url_button);
        MaterialRippleLayout mCopyUrlButton = (MaterialRippleLayout) view.findViewById(R.id.copy_url_button);
        MaterialRippleLayout mReportButton = (MaterialRippleLayout) view.findViewById(R.id.report_button);

        MaterialRippleLayout mBlockButton = (MaterialRippleLayout) view.findViewById(R.id.block_button);
        ImageView mBlockIcon = (ImageView) view.findViewById(R.id.block_icon);
        TextView mBlockTitle = (TextView) view.findViewById(R.id.block_label);

        if (!WEB_SITE_AVAILABLE) {

            mOpenUrlButton.setVisibility(View.GONE);
            mCopyUrlButton.setVisibility(View.GONE);
        }

        mBlockButton.setVisibility(View.GONE);
        mGiftButton.setVisibility(View.GONE);

        if (App.getInstance().getId() == community.getAuthorId()) {

            mReportButton.setVisibility(View.GONE);
            mEditButton.setVisibility(View.VISIBLE);

        } else {

            mReportButton.setVisibility(View.VISIBLE);
            mEditButton.setVisibility(View.GONE);
        }

        mRefreshButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                mRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });

        mEditButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                Intent i = new Intent(getActivity(), GroupSettingsActivity.class);
                i.putExtra("group_id", community.getId());
                i.putExtra("group_name", community.getFullname());
                i.putExtra("group_location", community.getLocation());
                i.putExtra("group_site", community.getWebPage());
                i.putExtra("year", community.getYear());
                i.putExtra("day", community.getDay());
                i.putExtra("month", community.getMonth());
                i.putExtra("group_allow_comments", community.getAllowComments());
                i.putExtra("group_allow_posts", community.getAllowPosts());
                i.putExtra("group_category", community.getCategory());
                i.putExtra("group_desc", community.getBio());
                startActivityForResult(i, ACTION_EDIT);
            }
        });

        mReportButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                profileReport();
            }
        });

        mCopyUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(community.getUsername(), API_DOMAIN + community.getUsername());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getActivity(), getText(R.string.msg_profile_link_copied), Toast.LENGTH_SHORT).show();
            }
        });

        mOpenUrlButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(API_DOMAIN + community.getUsername()));
                startActivity(i);
            }
        });


        mBottomSheetDialog = new BottomSheetDialog(getActivity());

        mBottomSheetDialog.setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();

        doKeepDialog(mBottomSheetDialog);

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                mBottomSheetDialog = null;
            }
        });
    }

    private void showChoiceImageDialog() {

        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.choice_image_sheet_list, null);

        MaterialRippleLayout mGalleryButton = (MaterialRippleLayout) view.findViewById(R.id.gallery_button);
        MaterialRippleLayout mCameraButton = (MaterialRippleLayout) view.findViewById(R.id.camera_button);

        mGalleryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/jpeg");

                imgFromGalleryActivityResultLauncher.launch(intent);
            }
        });

        mCameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                try {

                    newImageFileName = Helper.randomString(6) + ".jpg";

                    selectedImage = FileProvider.getUriForFile(App.getInstance().getApplicationContext(), App.getInstance().getPackageName() + ".provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), newImageFileName));

                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, selectedImage);
                    cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    imgFromCameraActivityResultLauncher.launch(cameraIntent);

                } catch (Exception e) {

                    Toast.makeText(getActivity(), "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(getActivity());

        mBottomSheetDialog.setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();

        doKeepDialog(mBottomSheetDialog);

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                mBottomSheetDialog = null;
            }
        });
    }

    private void showItemActionDialog(final int position) {

        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.item_action_sheet_list, null);

        MaterialRippleLayout mLoginButton = (MaterialRippleLayout) view.findViewById(R.id.login_button);
        MaterialRippleLayout mSignupButton = (MaterialRippleLayout) view.findViewById(R.id.signup_button);

        mLoginButton.setVisibility(View.GONE);
        mSignupButton.setVisibility(View.GONE);

        MaterialRippleLayout mEditButton = (MaterialRippleLayout) view.findViewById(R.id.edit_button);
        MaterialRippleLayout mDeleteButton = (MaterialRippleLayout) view.findViewById(R.id.delete_button);
        MaterialRippleLayout mShareButton = (MaterialRippleLayout) view.findViewById(R.id.share_button);
        MaterialRippleLayout mRepostButton = (MaterialRippleLayout) view.findViewById(R.id.repost_button);
        MaterialRippleLayout mReportButton = (MaterialRippleLayout) view.findViewById(R.id.report_button);
        MaterialRippleLayout mOpenUrlButton = (MaterialRippleLayout) view.findViewById(R.id.open_url_button);
        MaterialRippleLayout mCopyUrlButton = (MaterialRippleLayout) view.findViewById(R.id.copy_url_button);
        MaterialRippleLayout mPinButton = (MaterialRippleLayout) view.findViewById(R.id.pin_button);

        mPinButton.setVisibility(View.GONE);

        if (!WEB_SITE_AVAILABLE) {

            mOpenUrlButton.setVisibility(View.GONE);
            mCopyUrlButton.setVisibility(View.GONE);
        }

        final Item item = itemsList.get(position);

        if (item.getFromUserId() == App.getInstance().getId()) {

            mEditButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);

            mRepostButton.setVisibility(View.GONE);
            mReportButton.setVisibility(View.GONE);

        } else {

            mEditButton.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.GONE);

            if (item.getFromUserId() == community_id && community.getAuthorId() == App.getInstance().getId()) {

                mEditButton.setVisibility(View.VISIBLE);
            }

            mRepostButton.setVisibility(View.VISIBLE);
            mReportButton.setVisibility(View.VISIBLE);
        }

        if (App.getInstance().getId() == community.getAuthorId()) {

            mDeleteButton.setVisibility(View.VISIBLE);
        }

        mEditButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                Intent i = new Intent(getActivity(), NewItemActivity.class);
                i.putExtra("item", item);
                i.putExtra("position", position);
                startActivityForResult(i, ITEM_EDIT);
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                delete(position);
            }
        });

        mShareButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                share(position);
            }
        });

        mRepostButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                repost(position);
            }
        });

        mReportButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                report(position);
            }
        });

        mCopyUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("post url", item.getLink());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getActivity(), getText(R.string.msg_post_link_copied), Toast.LENGTH_SHORT).show();
            }
        });

        mOpenUrlButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mBottomSheetDialog.dismiss();

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(item.getLink()));
                startActivity(i);
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(getActivity());

        mBottomSheetDialog.setContentView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();

        doKeepDialog(mBottomSheetDialog);

        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {

                mBottomSheetDialog = null;
            }
        });
    }

    private void animateActionButtonIcon(CircularImageView icon) {

        // rotate animation

        // RotateAnimation rotate = new RotateAnimation(0, 45, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        // rotate.setDuration(175);
        // rotate.setInterpolator(new LinearInterpolator());


        // Scale animation

        ScaleAnimation scale = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(175);
        scale.setInterpolator(new LinearInterpolator());

        icon.startAnimation(scale);
    }

    // Prevent dialog dismiss when orientation changes
    private static void doKeepDialog(Dialog dialog) {

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
    }

    private boolean checkPermission() {

        if (ContextCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            return true;
        }

        return false;
    }

    private void requestPermission() {

        storagePermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE});
    }
}