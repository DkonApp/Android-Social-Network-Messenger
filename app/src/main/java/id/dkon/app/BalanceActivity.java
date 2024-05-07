package id.dkon.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.dkon.app.app.App;
import id.dkon.app.common.ActivityBase;
import id.dkon.app.util.CustomRequest;


public class BalanceActivity extends ActivityBase {

    Toolbar mToolbar;

    // For Google Play

    private static final String TAG = "ifsoft.inappbilling";            // Here you can write anything you like! For example: obama.white.house.billing
    private static final String TOKEN = "ifsoft.inappbilling";          // Here you can write anything you like! For example: obama.white.house.billing

    // See documentation!

    static final String ITEM_SKU_1 = "network.ifsoft.ru.iap1";          // Change to: yourdomain.com.iap1
    static final String ITEM_SKU_2 = "network.ifsoft.ru.iap2";          // Change to: yourdomain.com.iap2
    static final String ITEM_SKU_3 = "network.ifsoft.ru.iap3";          // Change to: yourdomain.com.iap3
    static final String ITEM_SKU_4 = "android.test.purchased";          // Not used. For testing

    static final int ITEM_SKU_1_AMOUNT = 100;          // in usd cents | 1 usd = 100 cents
    static final int ITEM_SKU_2_AMOUNT = 200;          // in usd cents | 2 usd = 200 cents
    static final int ITEM_SKU_3_AMOUNT = 300;          // in usd cents | 3 usd = 300 cents

    Button mBuy1Button, mBuy2Button, mBuy3Button, mBuy4Button;
    TextView mLabelCredits;

    Button mRewardedAdButton;

    private RewardedAd mRewardedAd;

    private Boolean loading = false;

    private BillingClient mBillingClient;
    private Map<String, SkuDetails> mSkuDetailsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        if (savedInstanceState != null) {

            loading = savedInstanceState.getBoolean("loading");

        } else {

            loading = false;
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (loading) {

            showpDialog();
        }

        mLabelCredits = (TextView) findViewById(R.id.labelCredits);

        mBuy1Button = (Button) findViewById(R.id.iap1_google_btn);
        mBuy2Button = (Button) findViewById(R.id.iap2_google_btn);
        mBuy3Button = (Button) findViewById(R.id.iap3_google_btn);
        mBuy4Button = (Button) findViewById(R.id.iap4_google_btn);      // For test Google Pay Button

        mRewardedAdButton = (Button) findViewById(R.id.rewarded_ad_btn);

        if (!GOOGLE_PAY_TEST_BUTTON) {

            mBuy4Button.setVisibility(View.GONE);
        }

        mBuy1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                launchBilling(ITEM_SKU_1);
            }
        });

        mBuy2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                launchBilling(ITEM_SKU_2);
            }
        });

        mBuy3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                launchBilling(ITEM_SKU_3);
            }
        });

        mBuy4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                launchBilling(ITEM_SKU_4);
            }
        });

        mRewardedAdButton.setVisibility(View.GONE);
        mRewardedAdButton.setEnabled(false);

        if (App.getInstance().getAllowRewardedAds() == 1) {

            mRewardedAdButton.setVisibility(View.VISIBLE);

            loadRewardedVideoAd();
        }

        mRewardedAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mRewardedAd != null) {

                    Activity activityContext = BalanceActivity.this;

                    mRewardedAd.show(activityContext, rewardItem -> {

                        // Handle the reward.
                        Log.d("TAG", "The user earned the reward.");

                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();

                        Log.d("Rewarded Video", "onRewarded");

                        Toast.makeText(BalanceActivity.this, getString(R.string.msg_success_rewarded), Toast.LENGTH_SHORT).show();

                        mRewardedAdButton.setEnabled(false);

                        App.getInstance().setBalance(App.getInstance().getBalance() + rewardItem.getAmount());
                        payment(rewardItem.getAmount(), 0, PT_ADMOB_REWARDED_ADS,false);
                    });

                } else {

                    Log.d("TAG", "The rewarded ad wasn't ready yet.");
                }
            }
        });

        update();

        setupBilling();
    }

    private void setupBilling() {

        mBillingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(new PurchasesUpdatedListener() {

            @Override
            public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {

                if (purchases != null) {

                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {

                        for (Purchase purchase : purchases) {

                            try {

                                JSONObject obj = new JSONObject(purchase.getOriginalJson());

                                consume(purchase.getPurchaseToken(), purchase.getDeveloperPayload(), obj.getString("productId"));

                                Log.e("Billing", obj.toString());

                            } catch (Throwable t) {

                                Log.e("Billing", "Could not parse malformed JSON: \"" + purchase.toString() + "\"");
                            }
                        }

                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {

                        // Handle an error caused by a user cancelling the purchase flow.

                    } else {

                        // Handle any other error codes.
                    }
                }
            }
        }).build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    // The BillingClient is ready. You can query purchases here.

                    querySkuDetails();

                    Log.e("onBillingSetupFinished", "WORKS");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.

                Log.e("onBillingSetupFinished", "NOT WORKS");
            }
        });
    }

    private void consume(String purchaseToken, String payload, String sku) {

        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String outToken) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    // Handle the success of the consume operation.
                    // For example, increase the number of coins inside the user's basket.

                    switch (sku) {

                        case ITEM_SKU_1:

                            App.getInstance().setBalance(App.getInstance().getBalance() + 30);
                            payment(30, ITEM_SKU_1_AMOUNT, PT_GOOGLE_PURCHASE,true);

                            break;

                        case ITEM_SKU_2:

                            App.getInstance().setBalance(App.getInstance().getBalance() + 70);
                            payment(70, ITEM_SKU_2_AMOUNT, PT_GOOGLE_PURCHASE,true);

                            break;

                        case ITEM_SKU_3:

                            App.getInstance().setBalance(App.getInstance().getBalance() + 120);
                            payment(120, ITEM_SKU_3_AMOUNT, PT_GOOGLE_PURCHASE,true);

                            break;

                        case ITEM_SKU_4:

                            Log.e("Payment", "Call");

                            App.getInstance().setBalance(App.getInstance().getBalance() + 100);
                            payment(100, 0, PT_GOOGLE_PURCHASE,true);

                            break;

                        default:

                            break;
                    }
                }
            }
        };

        mBillingClient.consumeAsync(consumeParams, listener);
    }

    private void querySkuDetails() {

        SkuDetailsParams.Builder skuDetailsParamsBuilder = SkuDetailsParams.newBuilder();
        List<String> skuList = new ArrayList<>();
        skuList.add(ITEM_SKU_1);
        skuList.add(ITEM_SKU_2);
        skuList.add(ITEM_SKU_3);

        if (GOOGLE_PAY_TEST_BUTTON) {

            skuList.add(ITEM_SKU_4);
        }

        skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    if (skuDetailsList != null) {

                        for (SkuDetails skuDetails : skuDetailsList) {

                            mSkuDetailsMap.put(skuDetails.getSku(), skuDetails);
                        }
                    }
                }
            }
        });
    }

    public void launchBilling(String skuId) {

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(mSkuDetailsMap.get(skuId))
                .build();
        mBillingClient.launchBillingFlow(this, billingFlowParams);
    }

    private List<Purchase> queryPurchases() {
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
        return purchasesResult.getPurchasesList();
    }

    public void payment(final int cost, final int amount, final int paymentType, final Boolean showSuccess) {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_PAYMENTS_NEW, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                if (response.has("balance")) {

                                    App.getInstance().setBalance(response.getInt("balance"));
                                }

                                if (showSuccess) {

                                    success();
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();

                            update();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("credits", Integer.toString(cost));
                params.put("paymentType", Integer.toString(paymentType));
                params.put("amount", Integer.toString(amount));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }


    public void update() {

        mLabelCredits.setText(getString(R.string.label_credits) + " (" + Integer.toString(App.getInstance().getBalance()) + ")");
    }

    public void success() {

        Toast.makeText(BalanceActivity.this, getString(R.string.msg_success_purchase), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("loading", loading);
    }

    @Override
    protected void onStart() {

        super.onStart();

        if (!mBillingClient.isReady()) {

            setupBilling();
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        hidepDialog();

        mBillingClient.endConnection();
    }

    @Override protected void onResume() {

        super.onResume();

        update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_deactivate, menu);
        return true;
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



    private void loadRewardedVideoAd() {

        mRewardedAdButton.setText(getString(R.string.msg_loading));

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, getString(R.string.rewarded_ad_unit_id), adRequest, new RewardedAdLoadCallback() {

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                // Handle the error.

                Log.d("Rewarded Ad", loadAdError.getMessage());

                mRewardedAd = null;
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {

                mRewardedAd = rewardedAd;

                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {

                    @Override
                    public void onAdShowedFullScreenContent() {

                        // Called when ad is shown.

                        Log.d("Rewarded Ad", "onAdShowedFullScreenContent");

                        mRewardedAdButton.setEnabled(false);

                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {

                        // Called when ad fails to show.

                        Log.d("Rewarded Ad", "onAdFailedToShowFullScreenContent");

                        mRewardedAdButton.setEnabled(false);

                        loadRewardedVideoAd();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {

                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.

                        Log.d("Rewarded Ad", "onAdDismissedFullScreenContent");

                        mRewardedAdButton.setEnabled(false);

                        loadRewardedVideoAd();
                    }
                });

                mRewardedAdButton.setText(getString(R.string.action_view_rewarded_video_ad));
                mRewardedAdButton.setEnabled(true);

                Log.d("Rewarded Ad", "onAdLoaded");
            }
        });
    }
}
