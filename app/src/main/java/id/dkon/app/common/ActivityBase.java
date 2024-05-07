package id.dkon.app.common;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import id.dkon.app.R;
import id.dkon.app.app.App;
import id.dkon.app.constants.Constants;

public class ActivityBase extends AppCompatActivity implements Constants {

    public static final String TAG = "ActivityBase";

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        initpDialog();
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) {

            try {

                pDialog.show();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) {

            try {

                pDialog.dismiss();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(updateBaseContextLocale(base));
    }

    private Context updateBaseContextLocale(Context context) {

        Locale myLocale;

        if (App.getInstance().getLanguage().length() == 0) {

            myLocale = Locale.getDefault();

        } else {

            myLocale = new Locale(App.getInstance().getLanguage());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            return updateResourcesLocale(context, myLocale);
        }

        return updateResourcesLocaleLegacy(context, myLocale);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Context updateResourcesLocale(Context context, Locale locale) {

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private Context updateResourcesLocaleLegacy(Context context, Locale locale) {

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            // update overrideConfiguration with your locale

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                getApplicationContext().createConfigurationContext(overrideConfiguration);

            } else {

                Resources res = getBaseContext().getResources();
                DisplayMetrics dm = res.getDisplayMetrics();

                res.updateConfiguration(overrideConfiguration, dm);
            }
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }
}
