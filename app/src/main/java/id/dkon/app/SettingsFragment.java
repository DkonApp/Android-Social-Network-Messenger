package id.dkon.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import id.dkon.app.app.App;
import id.dkon.app.constants.Constants;
import id.dkon.app.util.CustomRequest;

public class SettingsFragment extends PreferenceFragmentCompat implements Constants {

    private Preference languagePreference, itemOtpVerification, recentlyDeletedPreference, balancePreference, balanceHistoryPreference, logoutPreference, itemContactUs, aboutPreference, changePassword, itemServices, itemTerms, itemThanks, itemReferralsList, itemBlackList, itemNotifications, itemDeactivateAccount, itemPrivacy;
    private CheckBoxPreference allowComments, allowMessages, allowGalleryComments;
    private SwitchPreference mNightModeSwitch;

    private ProgressDialog pDialog;

    int mAllowComments, mAllowMessages, mAllowGalleryComments;

    LinearLayout aboutDialogContent;
    TextView aboutDialogAppName, aboutDialogAppVersion, aboutDialogAppCopyright;

    private Boolean loading = false;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setRetainInstance(true);

        initpDialog();

        // Load the preferences from an XML resource
        //addPreferencesFromResource(R.xml.settings);

        setPreferencesFromResource(R.xml.settings, rootKey);

        Preference pref = findPreference("settings_version");

        pref.setTitle(getString(R.string.app_name) + " v" + getString(R.string.app_version));

        pref = findPreference("settings_logout");

        pref.setSummary(App.getInstance().getUsername());

//        pref = findPreference("settings_copyright_info");
//
//        pref.setSummary(APP_COPYRIGHT + " © " + APP_YEAR);

        languagePreference = findPreference("settings_language");

        languagePreference.setSummary(App.getInstance().getLanguageNameByCode(App.getInstance().getLanguage()));

        languagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                List<String> language_names = new ArrayList<String>();

                Resources r = getResources();
                Configuration c = r.getConfiguration();

                for (int i = 0; i < App.getInstance().getLanguages().size(); i++) {

                    language_names.add(App.getInstance().getLanguages().get(i).get("lang_name"));
                }

                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle(getText(R.string.title_select_language));

                b.setItems(language_names.toArray(new CharSequence[language_names.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        App.getInstance().setLanguage(App.getInstance().getLanguages().get(which).get("lang_id"));

                        App.getInstance().saveData();

                        // Set App Language

                        App.getInstance().setLocale(App.getInstance().getLanguage());

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });

                b.setNegativeButton(getText(R.string.action_cancel), null);

                AlertDialog d = b.create();
                d.show();

                return true;
            }
        });

        //

        recentlyDeletedPreference = findPreference("settings_recently_deleted");

        recentlyDeletedPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), RecentlyDeletedActivity.class);
                startActivity(i);

                return true;
            }
        });

        balancePreference = findPreference("settings_balance");

        balancePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), BalanceActivity.class);
                startActivity(i);

                return true;
            }
        });

        balanceHistoryPreference = findPreference("settings_balance_history");

        balanceHistoryPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), BalanceHistoryActivity.class);
                startActivity(i);

                return true;
            }
        });

        logoutPreference = findPreference("settings_logout");
        aboutPreference = findPreference("settings_version");
        changePassword = findPreference("settings_change_password");
        itemDeactivateAccount = findPreference("settings_deactivate_account");
        itemServices = findPreference("settings_services");
        itemTerms = findPreference("settings_terms");
        itemThanks = findPreference("settings_thanks");
        itemBlackList = findPreference("settings_blocked_list");
        itemReferralsList = findPreference("settings_referrals_list");
        itemNotifications = findPreference("settings_push_notifications");
        itemPrivacy = findPreference("settings_privacy");
        itemContactUs = findPreference("settings_contact_us");
        itemOtpVerification = findPreference("settings_otp_verification");

        itemOtpVerification.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), OtpVerificationActivity.class);
                startActivity(i);

                return true;
            }
        });

        itemContactUs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), SupportActivity.class);
                startActivity(i);

                return true;
            }
        });

        itemPrivacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), PrivacySettingsActivity.class);
                startActivity(i);

                return true;
            }
        });

        itemNotifications.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), NotificationsSettingsActivity.class);
                startActivity(i);

                return true;
            }
        });

        itemBlackList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), BlackListActivity.class);
                startActivity(i);

                return true;
            }
        });

        itemReferralsList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), ReferralsActivity.class);
                startActivity(i);

                return true;
            }
        });

        itemThanks.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("url", METHOD_APP_THANKS);
                i.putExtra("title", getText(R.string.settings_thanks));
                startActivity(i);

                return true;
            }
        });

        itemTerms.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                Intent i = new Intent(getActivity(), WebViewActivity.class);
                i.putExtra("url", METHOD_APP_TERMS);
                i.putExtra("title", getText(R.string.settings_terms));
                startActivity(i);

                return true;
            }
        });

        aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(getText(R.string.action_about));

                aboutDialogContent = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.about_dialog, null);

                alertDialog.setView(aboutDialogContent);

                aboutDialogAppName = (TextView) aboutDialogContent.findViewById(R.id.aboutDialogAppName);
                aboutDialogAppVersion = (TextView) aboutDialogContent.findViewById(R.id.aboutDialogAppVersion);
                aboutDialogAppCopyright = (TextView) aboutDialogContent.findViewById(R.id.aboutDialogAppCopyright);

                aboutDialogAppName.setText(getString(R.string.app_name));
                aboutDialogAppVersion.setText("Version " + getString(R.string.app_version));
                aboutDialogAppCopyright.setText("Copyright © " + getString(R.string.app_year) + " " + getString(R.string.app_copyright));

//                    alertDialog.setMessage("Version " + APP_VERSION + "/r/n" + APP_COPYRIGHT);
                alertDialog.setCancelable(true);
                alertDialog.setPositiveButton(getText(R.string.action_ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });

                alertDialog.show();

                return false;
            }
        });

        logoutPreference.setSummary(App.getInstance().getUsername());

        logoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference arg0) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(getText(R.string.action_logout));

                alertDialog.setMessage(getText(R.string.msg_action_logout));
                alertDialog.setCancelable(true);

                alertDialog.setNegativeButton(getText(R.string.action_no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });

                alertDialog.setPositiveButton(getText(R.string.action_yes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        loading = true;

                        showpDialog();

                        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_LOGOUT, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        try {

                                            if (!response.getBoolean("error")) {

                                                Log.d("Logout", "Logout success");
                                            }

                                        } catch (JSONException e) {

                                            e.printStackTrace();

                                        } finally {

                                            loading = false;

                                            hidepDialog();

                                            App.getInstance().removeData();
                                            App.getInstance().readData();

                                            App.getInstance().setNotificationsCount(0);
                                            App.getInstance().setMessagesCount(0);
                                            App.getInstance().setId(0);
                                            App.getInstance().setOtpPhone("");
                                            App.getInstance().setOtpVerified(0);
                                            App.getInstance().setUsername("");
                                            App.getInstance().setFullname("");

                                            Intent intent = new Intent(getActivity(), AppActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
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

                                return params;
                            }
                        };

                        RetryPolicy policy = new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(VOLLEY_REQUEST_SECONDS), DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                        jsonReq.setRetryPolicy(policy);

                        App.getInstance().addToRequestQueue(jsonReq);
                    }
                });

                alertDialog.show();

                return true;
            }
        });

        changePassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent i = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivity(i);

                return true;
            }
        });

        itemDeactivateAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent i = new Intent(getActivity(), DeactivateActivity.class);
                startActivity(i);

                return true;
            }
        });

        itemServices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent i = new Intent(getActivity(), ServicesActivity.class);
                startActivity(i);

                return true;
            }
        });

        if (!FACEBOOK_AUTHORIZATION && !GOOGLE_AUTHORIZATION) {

            PreferenceCategory headerGeneral = (PreferenceCategory) findPreference("header_general");

            headerGeneral.removePreference(itemServices);
        }

        allowComments = (CheckBoxPreference) getPreferenceManager().findPreference("allowComments");

        allowComments.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowComments = 1;

                    } else {

                        mAllowComments = 0;
                    }

                    if (App.getInstance().isConnected()) {

                        setAllowComments();

                    } else {

                        Toast.makeText(getActivity().getApplicationContext(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            }
        });

        allowGalleryComments = (CheckBoxPreference) getPreferenceManager().findPreference("allowGalleryComments");

        allowGalleryComments.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowGalleryComments = 1;

                    } else {

                        mAllowGalleryComments = 0;
                    }

                    if (App.getInstance().isConnected()) {

                        setAllowGalleryComments();

                    } else {

                        Toast.makeText(getActivity().getApplicationContext(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            }
        });

        allowMessages = (CheckBoxPreference) getPreferenceManager().findPreference("allowMessages");

        allowMessages.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowMessages = 1;

                    } else {

                        mAllowMessages = 0;
                    }

                    if (App.getInstance().isConnected()) {

                        setAllowMessages();

                    } else {

                        Toast.makeText(getActivity().getApplicationContext(), getText(R.string.msg_network_error), Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            }
        });

        //

        mNightModeSwitch = (SwitchPreference) getPreferenceManager().findPreference("nightThemeSwitch");

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {

            mNightModeSwitch.setChecked(true);

        } else {

            mNightModeSwitch.setChecked(false);
        }

        mNightModeSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                        App.getInstance().setNightMode(1);

                    } else {

                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                        App.getInstance().setNightMode(0);
                    }

                    App.getInstance().saveData();
                }

                return true;
            }
        });

        //

        checkAllowComments(App.getInstance().getAllowComments());
        checkAllowGalleryComments(App.getInstance().getAllowGalleryComments());
        checkAllowMessages(App.getInstance().getAllowMessages());
    }

    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            loading = savedInstanceState.getBoolean("loading");

        } else {

            loading = false;
        }

        if (loading) {

            showpDialog();
        }
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean("loading", loading);
    }

    public void checkAllowComments(int value) {

        if (value == 1) {

            allowComments.setChecked(true);
            mAllowComments = 1;

        } else {

            allowComments.setChecked(false);
            mAllowComments = 0;
        }
    }

    public void checkAllowGalleryComments(int value) {

        if (value == 1) {

            allowGalleryComments.setChecked(true);
            mAllowGalleryComments = 1;

        } else {

            allowGalleryComments.setChecked(false);
            mAllowGalleryComments = 0;
        }
    }

    public void checkAllowMessages(int value) {

        if (value == 1) {

            allowMessages.setChecked(true);
            mAllowMessages = 1;

        } else {

            allowMessages.setChecked(false);
            mAllowMessages = 0;
        }
    }

    public void setAllowComments() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SET_ALLOW_COMMENTS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                App.getInstance().setAllowComments(response.getInt("allowComments"));

                                checkAllowComments(App.getInstance().getAllowComments());
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;

                hidepDialog();

                Toast.makeText(getActivity().getApplicationContext(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("allowComments", Integer.toString(mAllowComments));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void setAllowGalleryComments() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SET_ALLOW_GALLERY_COMMENTS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                App.getInstance().setAllowGalleryComments(response.getInt("allowGalleryComments"));

                                checkAllowGalleryComments(App.getInstance().getAllowGalleryComments());
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;

                hidepDialog();

                Toast.makeText(getActivity().getApplicationContext(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("allowGalleryComments", Integer.toString(mAllowGalleryComments));

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }

    public void setAllowMessages() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_SET_ALLOW_MESSAGES, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (!response.getBoolean("error")) {

                                App.getInstance().setAllowMessages(response.getInt("allowMessages"));

                                checkAllowMessages(App.getInstance().getAllowMessages());
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            loading = false;

                            hidepDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loading = false;

                hidepDialog();

                Toast.makeText(getActivity().getApplicationContext(), getText(R.string.error_data_loading), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("clientId", CLIENT_ID);
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("allowMessages", Integer.toString(mAllowMessages));

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

        if (!pDialog.isShowing())
            pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}