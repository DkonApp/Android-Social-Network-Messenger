package id.dkon.app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
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

public class NotificationsSettingsFragment extends PreferenceFragment implements Constants {

    private CheckBoxPreference mAllowLikesGCM, mAllowCommentsGCM, mAllowFollowersGCM, mAllowMessagesGCM, mAllowCommentReplyGCM, mAllowGiftsGCM;

    private ProgressDialog pDialog;

    int mAllowLikes, mAllowComments, mAllowFollowers, mAllowMessages, mAllowCommentReply, mAllowGifts;

    private Boolean loading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        initpDialog();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.notifications_settings);

        mAllowLikesGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowLikesGCM");

        mAllowLikesGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowLikes = 1;

                    } else {

                        mAllowLikes = 0;
                    }

                    saveSettings();
                }

                return true;
            }
        });

        mAllowCommentsGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowCommentsGCM");

        mAllowCommentsGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowComments = 1;

                    } else {

                        mAllowComments = 0;
                    }

                    saveSettings();
                }

                return true;
            }
        });

        mAllowFollowersGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowFollowersGCM");

        mAllowFollowersGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowFollowers = 1;

                    } else {

                        mAllowFollowers = 0;
                    }

                    saveSettings();
                }

                return true;
            }
        });

        mAllowMessagesGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowMessagesGCM");

        mAllowMessagesGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowMessages = 1;

                    } else {

                        mAllowMessages = 0;
                    }

                    saveSettings();
                }

                return true;
            }
        });

        mAllowCommentReplyGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowCommentReplyGCM");

        mAllowCommentReplyGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowCommentReply = 1;

                    } else {

                        mAllowCommentReply = 0;
                    }

                    saveSettings();
                }

                return true;
            }
        });

        mAllowGiftsGCM = (CheckBoxPreference) getPreferenceManager().findPreference("allowGiftsGCM");

        mAllowGiftsGCM.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue instanceof Boolean) {

                    Boolean value = (Boolean) newValue;

                    if (value) {

                        mAllowGifts = 1;

                    } else {

                        mAllowGifts = 0;
                    }

                    saveSettings();
                }

                return true;
            }
        });

        checkAllowLikes(App.getInstance().getAllowLikesGCM());
        checkAllowComments(App.getInstance().getAllowCommentsGCM());
        checkAllowFollowers(App.getInstance().getAllowFollowersGCM());
        checkAllowMessages(App.getInstance().getAllowMessagesGCM());
        checkAllowCommentReply(App.getInstance().getAllowCommentReplyGCM());
        checkAllowGifts(App.getInstance().getAllowGiftsGCM());
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

    public void checkAllowLikes(int value) {

        if (value == 1) {

            mAllowLikesGCM.setChecked(true);
            mAllowLikes = 1;

        } else {

            mAllowLikesGCM.setChecked(false);
            mAllowLikes = 0;
        }
    }

    public void checkAllowComments(int value) {

        if (value == 1) {

            mAllowCommentsGCM.setChecked(true);
            mAllowComments = 1;

        } else {

            mAllowCommentsGCM.setChecked(false);
            mAllowComments = 0;
        }
    }

    public void checkAllowFollowers(int value) {

        if (value == 1) {

            mAllowFollowersGCM.setChecked(true);
            mAllowFollowers = 1;

        } else {

            mAllowFollowersGCM.setChecked(false);
            mAllowFollowers = 0;
        }
    }

    public void checkAllowMessages(int value) {

        if (value == 1) {

            mAllowMessagesGCM.setChecked(true);
            mAllowMessages = 1;

        } else {

            mAllowMessagesGCM.setChecked(false);
            mAllowMessages = 0;
        }
    }

    public void checkAllowCommentReply(int value) {

        if (value == 1) {

            mAllowCommentReplyGCM.setChecked(true);
            mAllowCommentReply = 1;

        } else {

            mAllowCommentReplyGCM.setChecked(false);
            mAllowCommentReply = 0;
        }
    }

    public void checkAllowGifts(int value) {

        if (value == 1) {

            mAllowGiftsGCM.setChecked(true);
            mAllowGifts = 1;

        } else {

            mAllowGiftsGCM.setChecked(false);
            mAllowGifts = 0;
        }
    }

    public void saveSettings() {

        App.getInstance().setAllowLikesGCM(mAllowLikes);
        App.getInstance().setAllowGiftsGCM(mAllowGifts);
        App.getInstance().setAllowMessagesGCM(mAllowMessages);
        App.getInstance().setAllowCommentsGCM(mAllowComments);
        App.getInstance().setAllowCommentReplyGCM(mAllowCommentReply);
        App.getInstance().setAllowFollowersGCM(mAllowFollowers);

        App.getInstance().saveData();
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