package id.dkon.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.toolbox.ImageLoader;
import com.balysv.materialripple.MaterialRippleLayout;

import id.dkon.app.app.App;
import id.dkon.app.constants.Constants;


public class MenuFragment extends Fragment implements Constants {

    ImageLoader imageLoader;

    private ImageView mFriendsIcon, mGuestsIcon;

    private ImageView mNavGalleryIcon, mNavGroupsIcon, mNavFriendsIcon, mNavGuestsIcon, mNavMarketIcon, mNavNearbyIcon, mNavFavoritesIcon, mNavStreamIcon, mNavPopularIcon, mNavUpgradesIcon, mNavSettingsIcon;

    private LinearLayout mNavGallery, mNavGroups, mNavFriends, mNavStream, mNavMarket, mNavGuests, mNavFavorites, mNavNearby, mNavPopular, mNavUpgrades, mNavSettings;

    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        imageLoader = App.getInstance().getImageLoader();

        setHasOptionsMenu(false);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);

        getActivity().setTitle(R.string.nav_menu);

        mNavGallery = (LinearLayout) rootView.findViewById(R.id.nav_gallery);
        mNavGroups = (LinearLayout) rootView.findViewById(R.id.nav_groups);
        mNavFriends = (LinearLayout) rootView.findViewById(R.id.nav_friends);
        mNavGuests = (LinearLayout) rootView.findViewById(R.id.nav_guests);
        mNavMarket = (LinearLayout) rootView.findViewById(R.id.nav_market);
        mNavNearby = (LinearLayout) rootView.findViewById(R.id.nav_nearby);
        mNavFavorites = (LinearLayout) rootView.findViewById(R.id.nav_favorites);
        mNavStream = (LinearLayout) rootView.findViewById(R.id.nav_stream);
        mNavPopular = (LinearLayout) rootView.findViewById(R.id.nav_popular);
        mNavUpgrades = (LinearLayout) rootView.findViewById(R.id.nav_upgrades);
        mNavSettings = (LinearLayout) rootView.findViewById(R.id.nav_settings);

        // Counters

        mFriendsIcon = (ImageView) rootView.findViewById(R.id.nav_friends_count_icon);
        mGuestsIcon = (ImageView) rootView.findViewById(R.id.nav_guests_count_icon);

        // Icons

        mNavGalleryIcon = (ImageView) rootView.findViewById(R.id.nav_gallery_icon);
        mNavGroupsIcon = (ImageView) rootView.findViewById(R.id.nav_groups_icon);
        mNavFriendsIcon = (ImageView) rootView.findViewById(R.id.nav_friends_icon);
        mNavGuestsIcon = (ImageView) rootView.findViewById(R.id.nav_guests_icon);
        mNavMarketIcon = (ImageView) rootView.findViewById(R.id.nav_market_icon);
        mNavNearbyIcon = (ImageView) rootView.findViewById(R.id.nav_nearby_icon);
        mNavFavoritesIcon = (ImageView) rootView.findViewById(R.id.nav_favorites_icon);
        mNavStreamIcon = (ImageView) rootView.findViewById(R.id.nav_stream_icon);
        mNavPopularIcon = (ImageView) rootView.findViewById(R.id.nav_popular_icon);
        mNavUpgradesIcon = (ImageView) rootView.findViewById(R.id.nav_upgrades_icon);
        mNavSettingsIcon = (ImageView) rootView.findViewById(R.id.nav_settings_icon);

        if (!MARKETPLACE_FEATURE) {

            mNavMarket.setVisibility(View.GONE);
        }

        if (!UPGRADES_FEATURE) {

            mNavUpgrades.setVisibility(View.GONE);
        }

        mNavGallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), GalleryActivity.class);
                i.putExtra("profileId", App.getInstance().getId());
                getActivity().startActivity(i);
            }
        });

        mNavGroups.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), GroupsActivity.class);
                startActivity(i);
            }
        });

        mNavFriends.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), FriendsActivity.class);
                i.putExtra("profileId", App.getInstance().getId());
                startActivity(i);
            }
        });

        mNavGuests.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), GuestsActivity.class);
                startActivity(i);
            }
        });


        mNavMarket.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), MarketActivity.class);
                startActivity(i);
            }
        });

        mNavNearby.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), NearbyActivity.class);
                startActivity(i);
            }
        });

        mNavFavorites.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), FavoritesActivity.class);
                i.putExtra("profileId", App.getInstance().getId());
                startActivity(i);
            }
        });

        mNavStream.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), StreamActivity.class);
                startActivity(i);
            }
        });

        mNavPopular.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), PopularActivity.class);
                startActivity(i);
            }
        });

        mNavUpgrades.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), UpgradesActivity.class);
                startActivity(i);
            }
        });

        mNavSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), SettingsActivity.class);
                startActivity(i);
            }
        });

        updateView();

        // Inflate the layout for this fragment
        return rootView;
    }

    public void updateView() {

        // Counters

        mFriendsIcon.setVisibility(View.GONE);
        mGuestsIcon.setVisibility(View.GONE);

        if (App.getInstance().getNewFriendsCount() != 0) {

            mFriendsIcon.setVisibility(View.VISIBLE);
        }

        if (App.getInstance().getGuestsCount() != 0) {

            mGuestsIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        updateView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}