package id.dkon.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import id.dkon.app.app.App;
import id.dkon.app.constants.Constants;

public class LocationFragment extends Fragment implements Constants {

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    TextView mPrompt;
    Button mOpenSettings;

    public LocationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_location, container, false);

        mPrompt = (TextView) rootView.findViewById(R.id.prompt);

        mOpenSettings= (Button) rootView.findViewById(R.id.openSettings);

        mOpenSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(viewIntent, 1);
            }
        });


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {

            // Check GPS is enabled
            LocationManager lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

                mFusedLocationClient.getLastLocation().addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        if (task.isSuccessful() && task.getResult() != null) {

                            mLastLocation = task.getResult();

                            // Set geo data to App class

                            App.getInstance().setLat(mLastLocation.getLatitude());
                            App.getInstance().setLng(mLastLocation.getLongitude());

                            // Save data

                            App.getInstance().saveData();

                            // Get address

                            App.getInstance().getAddress(App.getInstance().getLat(), App.getInstance().getLng());

                        } else {

                            Log.d("GPS", "New Item getLastLocation:exception", task.getException());
                        }
                    }
                });
            }

            Intent i = new Intent();
            getActivity().setResult(getActivity().RESULT_OK, i);

            getActivity().finish();
        }
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