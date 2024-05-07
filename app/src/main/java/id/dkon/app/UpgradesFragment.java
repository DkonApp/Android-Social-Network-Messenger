package id.dkon.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.dkon.app.app.App;
import id.dkon.app.constants.Constants;
import id.dkon.app.util.CustomRequest;

public class UpgradesFragment extends Fragment implements Constants {

    private ProgressDialog pDialog;

    Button mGetCreditsButton, mGhostModeButton, mVerifiedBadgeButton, mDisableAdsButton;
    TextView mLabelCredits;

    private Boolean loading = false;

    public UpgradesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        initpDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_upgrades, container, false);

        if (loading) {

            showpDialog();
        }

        mLabelCredits = (TextView) rootView.findViewById(R.id.credits_label);

        mGhostModeButton = (Button) rootView.findViewById(R.id.ghost_button);
        mVerifiedBadgeButton = (Button) rootView.findViewById(R.id.verified_button);
        mDisableAdsButton = (Button) rootView.findViewById(R.id.ad_button);

        mGetCreditsButton = (Button) rootView.findViewById(R.id.get_credits_button);

        mGetCreditsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), BalanceActivity.class);
                startActivityForResult(i, 1945);
            }
        });

        mGhostModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().getBalance() >= GHOST_MODE_COST) {

                    upgrade(PA_BUY_GHOST_MODE, GHOST_MODE_COST);

                } else {

                    Toast.makeText(getActivity(), getString(R.string.error_credits), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mVerifiedBadgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().getBalance() >= VERIFIED_BADGE_COST) {

                    upgrade(PA_BUY_VERIFIED_BADGE, VERIFIED_BADGE_COST);

                } else {

                    Toast.makeText(getActivity(), getString(R.string.error_credits), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mDisableAdsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (App.getInstance().getBalance() >= DISABLE_ADS_COST) {

                    upgrade(PA_BUY_DISABLE_ADS, DISABLE_ADS_COST);

                } else {

                    Toast.makeText(getActivity(), getString(R.string.error_credits), Toast.LENGTH_SHORT).show();
                }
            }
        });

        update();

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1945 && resultCode == getActivity().RESULT_OK && null != data) {

            update();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    @Override
    public void onStart() {

        super.onStart();

        update();
    }

    public void update() {

        mLabelCredits.setText(getString(R.string.label_credits) + " (" + Integer.toString(App.getInstance().getBalance()) + ")");

        if (App.getInstance().getGhost() == 0) {

            mGhostModeButton.setEnabled(true);
            mGhostModeButton.setBackground(getActivity().getResources().getDrawable(R.drawable.button_primary));
            mGhostModeButton.setTextColor(getResources().getColor(R.color.white));
            mGhostModeButton.setText(getString(R.string.action_enable) + " (" + Integer.toString(GHOST_MODE_COST) + ")");

        } else {

            mGhostModeButton.setEnabled(false);
            mGhostModeButton.setBackground(getActivity().getResources().getDrawable(R.drawable.button_gray));
            mGhostModeButton.setTextColor(getResources().getColor(R.color.gray_text));
            mGhostModeButton.setText(getString(R.string.label_ghost_mode_enabled));
        }

        if (App.getInstance().getVerify() == 0) {

            mVerifiedBadgeButton.setEnabled(true);
            mVerifiedBadgeButton.setBackground(getActivity().getResources().getDrawable(R.drawable.button_primary));
            mVerifiedBadgeButton.setTextColor(getResources().getColor(R.color.white));
            mVerifiedBadgeButton.setText(getString(R.string.action_enable) + " (" + Integer.toString(VERIFIED_BADGE_COST) + ")");

        } else {

            mVerifiedBadgeButton.setEnabled(false);
            mVerifiedBadgeButton.setBackground(getActivity().getResources().getDrawable(R.drawable.button_gray));
            mVerifiedBadgeButton.setTextColor(getResources().getColor(R.color.gray_text));
            mVerifiedBadgeButton.setText(getString(R.string.label_verified_badge_enabled));
        }

        if (App.getInstance().getAdmob() == ADMOB_ENABLED) {

            mDisableAdsButton.setEnabled(true);
            mDisableAdsButton.setBackground(getActivity().getResources().getDrawable(R.drawable.button_primary));
            mDisableAdsButton.setTextColor(getResources().getColor(R.color.white));
            mDisableAdsButton.setText(getString(R.string.action_enable) + " (" + Integer.toString(DISABLE_ADS_COST) + ")");

        } else {

            mDisableAdsButton.setEnabled(false);
            mDisableAdsButton.setBackground(getActivity().getResources().getDrawable(R.drawable.button_gray));
            mDisableAdsButton.setTextColor(getResources().getColor(R.color.gray_text));
            mDisableAdsButton.setText(getString(R.string.label_disable_ads_enabled));
        }

        int paddingDp = 12;
        float density = getActivity().getResources().getDisplayMetrics().density;
        int paddingPixel = (int)(paddingDp * density);

        mGhostModeButton.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
        mVerifiedBadgeButton.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
        mDisableAdsButton.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
    }

    public void upgrade(final int upgradeType, final int credits) {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_UPGRADE, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                switch (upgradeType) {

                                    case PA_BUY_VERIFIED_BADGE: {

                                        App.getInstance().setBalance(App.getInstance().getBalance() - credits);
                                        App.getInstance().setVerify(1);

                                        Toast.makeText(getActivity(), getString(R.string.msg_success_verified_badge), Toast.LENGTH_SHORT).show();

                                        break;
                                    }

                                    case PA_BUY_GHOST_MODE: {

                                        App.getInstance().setBalance(App.getInstance().getBalance() - credits);
                                        App.getInstance().setGhost(1);

                                        Toast.makeText(getActivity(), getString(R.string.msg_success_ghost_mode), Toast.LENGTH_SHORT).show();

                                        break;
                                    }

                                    case PA_BUY_DISABLE_ADS: {

                                        App.getInstance().setBalance(App.getInstance().getBalance() - credits);
                                        App.getInstance().setAdmob(ADMOB_DISABLED);

                                        Toast.makeText(getActivity(), getString(R.string.msg_success_disable_ads), Toast.LENGTH_SHORT).show();

                                        break;
                                    }

                                    default: {

                                        break;
                                    }
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

                update();

                hidepDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("upgradeType", Integer.toString(upgradeType));
                params.put("credits", Integer.toString(credits));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
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
}