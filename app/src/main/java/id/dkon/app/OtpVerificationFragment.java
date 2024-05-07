package id.dkon.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import id.dkon.app.app.App;
import id.dkon.app.constants.Constants;
import id.dkon.app.util.CustomRequest;

public class OtpVerificationFragment extends Fragment implements Constants {

    private ProgressDialog pDialog;

    TextView mResendCode, mInfoBox;
    Button mActionButton;
    EditText mInputRow;
    String phoneNumber = "", code = "", token = "";

    private Boolean loading = false;

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private FirebaseAuth mAuth;

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    public OtpVerificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {

            mAuth.signOut();
        }

        initpDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_otp_verification, container, false);

        if (loading) {

            showpDialog();
        }


        mInputRow = rootView.findViewById(R.id.inputRow);

        mInfoBox = rootView.findViewById(R.id.infoBox);
        mResendCode = rootView.findViewById(R.id.resendCode);

        mResendCode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), RecoveryActivity.class);
                startActivity(i);
            }
        });

        mActionButton = rootView.findViewById(R.id.actionBtn);

        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Hide keyboard

                if (mInputRow.getText().toString().trim().length() != 0) {

                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                };

                if (!mVerificationInProgress) {

                    phoneNumber = mInputRow.getText().toString();

                    if (phoneNumber.length() > 10) {

                        startPhoneNumberVerification(phoneNumber);
                    }

                } else {

                    code = mInputRow.getText().toString();

                    if (code.length() == 6) {

                        verifyPhoneNumberWithCode(mVerificationId, code);
                    }
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                Log.d(TAG, "onVerificationCompleted:" + credential);

                loading = false;

                hidepDialog();

                mVerificationInProgress = false;

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                Log.w(TAG, "onVerificationFailed", e);

                loading = false;

                hidepDialog();

                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                    // Invalid request

                    mInputRow.setError(getString(R.string.otp_verification_phone_number_error_msg_2));
                    Toast.makeText(getActivity(), getString(R.string.otp_verification_phone_number_error_msg), Toast.LENGTH_SHORT).show();

                } else if (e instanceof FirebaseTooManyRequestsException) {

                    // The SMS quota for the project has been exceeded

                    Snackbar.make(getView(), getString(R.string.otp_verification_many_requests_error_msg), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {

                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                Log.d(TAG, "onCodeSent:" + verificationId);

                loading = false;

                hidepDialog();

                mVerificationInProgress = true;

                // Save verification ID and resending token so we can use them later

                mVerificationId = verificationId;
                mResendToken = token;

                mInputRow.setText("");

                Toast.makeText(getActivity(), getString(R.string.otp_verification_code_sent_msg), Toast.LENGTH_LONG).show();
                updateUI();
            }
        };

        updateUI();

        // Inflate the layout for this fragment
        return rootView;
    }

    private void startPhoneNumberVerification(String phoneNumber) {

        loading = true;

        showpDialog();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(getActivity())                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {

        loading = true;

        showpDialog();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

                            mUser.getIdToken(true)
                                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                        public void onComplete(@NonNull Task<GetTokenResult> task) {

                                            if (task.isSuccessful()) {

                                                token = task.getResult().getToken();

                                                Log.e("Firebase token", token);

                                                finishVerification();

                                            } else {

                                                // Handle error -> task.getException();
                                            }
                                        }
                                    });

                        } else {

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                // The verification code entered was invalid

                                loading = false;

                                hidepDialog();

                                mInputRow.setError(getString(R.string.otp_verification_code_error_msg_2));
                                Toast.makeText(getActivity(), getString(R.string.otp_verification_code_error_msg), Toast.LENGTH_LONG).show();
                            }

                            updateUI();
                        }
                    }
                });
    }

    public void updateUI() {

        mResendCode.setVisibility(View.GONE);

        if (App.getInstance().getOtpVerified() == 1) {

            mInputRow.setVisibility(View.GONE);
            mActionButton.setVisibility(View.GONE);
            mResendCode.setVisibility(View.GONE);

            mInfoBox.setText(getString(R.string.otp_verification_success_msg));

        } else {

            if (mVerificationInProgress) {

                mInputRow.setHint(getString(R.string.otp_sms_code_placeholder));
                mInputRow.setFilters(new InputFilter[] {new InputFilter.LengthFilter(6)});
                mInputRow.setText("");
                mActionButton.setText(getString(R.string.otp_action_check_code));
                mInfoBox.setText(R.string.otp_sms_code_info);

            } else {

                mInputRow.setHint(getString(R.string.otp_phone_number_placeholder));
                mInputRow.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});
                mInputRow.setText("");
                mActionButton.setText(getString(R.string.otp_action_send_code));
                mInfoBox.setText(R.string.otp_phone_number_info);
            }
        }
    }

    public void onDestroyView() {

        super.onDestroyView();

        hidepDialog();
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
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void finishVerification() {

        loading = true;

        showpDialog();

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ACCOUNT_OTP, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (response.has("error")) {

                                if (!response.getBoolean("error")) {

                                    if (response.has("verified")) {

                                        if (response.getBoolean("verified")) {

                                            App.getInstance().setOtpVerified(1);
                                            App.getInstance().setOtpPhone(phoneNumber);

                                            App.getInstance().saveData();
                                        }
                                    }

                                } else {

                                    if (response.has("error_code")) {

                                        int error_code = response.getInt("error_code");

                                        if (error_code == ERROR_OTP_PHONE_NUMBER_TAKEN) {

                                            Toast.makeText(getActivity(), getString(R.string.otp_verification_phone_number_taken_error_msg), Toast.LENGTH_LONG).show();

                                            mVerificationInProgress = false;

                                        } else {

                                            Toast.makeText(getActivity(), getString(R.string.otp_verification_error_msg), Toast.LENGTH_LONG).show();
                                        }
                                    }

                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            Log.e("Response", response.toString());

                            loading = false;

                            hidepDialog();

                            updateUI();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("OTP finishVerification", error.toString());

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
                params.put("token", token);
                params.put("phoneNumber", phoneNumber);

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
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