package id.dkon.app.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

import id.dkon.app.R;
import id.dkon.app.constants.Constants;

public class InterstitialAdSettings extends Application implements Constants {

	public static final String TAG = InterstitialAdSettings.class.getSimpleName();

    private SharedPreferences sharedPref;
    private static Resources res;

    private int interstitialAdAfterNewItem = 0, interstitialAdAfterNewGalleryItem = 0, interstitialAdAfterNewMarketItem = 0, interstitialAdAfterNewLike = 2;
    private int currentInterstitialAdAfterNewItem = 0, currentInterstitialAdAfterNewGalleryItem = 0, currentInterstitialAdAfterNewMarketItem = 0, currentInterstitialAdAfterNewLike = 0;

	@Override
	public void onCreate() {

		super.onCreate();

        this.res = getResources();

        sharedPref = getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE);
	}

    public void read_from_json(JSONObject jsonData) {

        try {

            if (jsonData.has("interstitialAdAfterNewItem")) {

                this.setInterstitialAdAfterNewItem(jsonData.getInt("interstitialAdAfterNewItem"));
            }

            if (jsonData.has("interstitialAdAfterNewGalleryItem")) {

                this.setInterstitialAdAfterNewGalleryItem(jsonData.getInt("interstitialAdAfterNewGalleryItem"));
            }

            if (jsonData.has("interstitialAdAfterNewMarketItem")) {

                this.setInterstitialAdAfterNewMarketItem(jsonData.getInt("interstitialAdAfterNewMarketItem"));
            }

            if (jsonData.has("interstitialAdAfterNewLike")) {

                this.setInterstitialAdAfterNewLike(jsonData.getInt("interstitialAdAfterNewLike"));
            }

        } catch (Throwable t) {

            Log.e("InterstitialAdSettings", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.e("InterstitialAdSettings", "");
        }
    }

    public void read_settings() {

        if (sharedPref.contains(getString(R.string.settings_interstitial_ad_after_new_item))) {

            this.setInterstitialAdAfterNewItem(sharedPref.getInt(getString(R.string.settings_interstitial_ad_after_new_item), 0));
            this.setInterstitialAdAfterNewGalleryItem(sharedPref.getInt(getString(R.string.settings_interstitial_ad_after_new_gallery_item), 0));
            this.setInterstitialAdAfterNewMarketItem(sharedPref.getInt(getString(R.string.settings_interstitial_ad_after_new_market_item), 0));
            this.setInterstitialAdAfterNewLike(sharedPref.getInt(getString(R.string.settings_interstitial_ad_after_new_like), 0));

            this.setCurrentInterstitialAdAfterNewItem(sharedPref.getInt(getString(R.string.settings_interstitial_ad_after_new_item_current_val), 0));
            this.setCurrentInterstitialAdAfterNewGalleryItem(sharedPref.getInt(getString(R.string.settings_interstitial_ad_after_new_gallery_item_current_val), 0));
            this.setCurrentInterstitialAdAfterNewMarketItem(sharedPref.getInt(getString(R.string.settings_interstitial_ad_after_new_market_item_current_val), 0));
            this.setCurrentInterstitialAdAfterNewLike(sharedPref.getInt(getString(R.string.settings_interstitial_ad_after_new_like_current_val), 0));
        }

        Log.e("InterstitialAdSettings", "read settings");
    }

    public void save_settings() {

	    Log.e("InterstitialAdSettings", Integer.toString(this.getInterstitialAdAfterNewItem()));
        Log.e("InterstitialAdSettings1", Integer.toString(this.getInterstitialAdAfterNewGalleryItem()));
        Log.e("InterstitialAdSettings2", Integer.toString(this.getInterstitialAdAfterNewMarketItem()));

        sharedPref.edit().putInt(getString(R.string.settings_interstitial_ad_after_new_item), this.getInterstitialAdAfterNewItem()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_interstitial_ad_after_new_gallery_item), this.getInterstitialAdAfterNewGalleryItem()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_interstitial_ad_after_new_market_item), this.getInterstitialAdAfterNewMarketItem()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_interstitial_ad_after_new_like), this.getInterstitialAdAfterNewLike()).apply();

        sharedPref.edit().putInt(getString(R.string.settings_interstitial_ad_after_new_item_current_val), this.getCurrentInterstitialAdAfterNewItem()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_interstitial_ad_after_new_gallery_item_current_val), this.getCurrentInterstitialAdAfterNewGalleryItem()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_interstitial_ad_after_new_market_item_current_val), this.getCurrentInterstitialAdAfterNewMarketItem()).apply();
        sharedPref.edit().putInt(getString(R.string.settings_interstitial_ad_after_new_like_current_val), this.getCurrentInterstitialAdAfterNewLike()).apply();

        Log.e("InterstitialAdSettings", "save settings");
    }

    public void setInterstitialAdAfterNewItem(int interstitialAdAfterNewItem) {

        this.interstitialAdAfterNewItem = interstitialAdAfterNewItem;
    }

    public int getInterstitialAdAfterNewItem() {

        return this.interstitialAdAfterNewItem;
    }

    public void setCurrentInterstitialAdAfterNewItem(int currentInterstitialAdAfterNewItem) {

        this.currentInterstitialAdAfterNewItem = currentInterstitialAdAfterNewItem;
    }

    public int getCurrentInterstitialAdAfterNewItem() {

        return this.currentInterstitialAdAfterNewItem;
    }

    //

    public void setInterstitialAdAfterNewGalleryItem(int interstitialAdAfterNewGalleryItem) {

        this.interstitialAdAfterNewGalleryItem = interstitialAdAfterNewGalleryItem;
    }

    public int getInterstitialAdAfterNewGalleryItem() {

        return this.interstitialAdAfterNewGalleryItem;
    }

    public void setCurrentInterstitialAdAfterNewGalleryItem(int currentInterstitialAdAfterNewGalleryItem) {

        this.currentInterstitialAdAfterNewGalleryItem = currentInterstitialAdAfterNewGalleryItem;
    }

    public int getCurrentInterstitialAdAfterNewGalleryItem() {

        return this.currentInterstitialAdAfterNewGalleryItem;
    }

    //

    public void setInterstitialAdAfterNewMarketItem(int interstitialAdAfterNewMarketItem) {

        this.interstitialAdAfterNewMarketItem = interstitialAdAfterNewMarketItem;
    }

    public int getInterstitialAdAfterNewMarketItem() {

        return this.interstitialAdAfterNewMarketItem;
    }

    public void setCurrentInterstitialAdAfterNewMarketItem(int currentInterstitialAdAfterNewMarketItem) {

        this.currentInterstitialAdAfterNewMarketItem = currentInterstitialAdAfterNewMarketItem;
    }

    public int getCurrentInterstitialAdAfterNewMarketItem() {

        return this.currentInterstitialAdAfterNewMarketItem;
    }

    //

    public void setInterstitialAdAfterNewLike(int interstitialAdAfterNewLike) {

        this.interstitialAdAfterNewLike = interstitialAdAfterNewLike;
    }

    public int getInterstitialAdAfterNewLike() {

        return this.interstitialAdAfterNewLike;
    }

    public void setCurrentInterstitialAdAfterNewLike(int currentInterstitialAdAfterNewLike) {

        this.currentInterstitialAdAfterNewLike = currentInterstitialAdAfterNewLike;
    }

    public int getCurrentInterstitialAdAfterNewLike() {

        return this.currentInterstitialAdAfterNewLike;
    }
}