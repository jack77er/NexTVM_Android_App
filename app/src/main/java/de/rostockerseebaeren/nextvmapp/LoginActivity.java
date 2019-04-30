package de.rostockerseebaeren.nextvmapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import io.fabric.sdk.android.Fabric;


public class LoginActivity extends AppCompatActivity {


    public static final String PREFS_NAME = "TVMUserCredentials";

    /**
     * Connection timeout for request in milliseconds
     */
    private static int CONNECTION_TIMEOUT = 8000;
        /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private TextView mLoginView;
    private EditText mPasswordView;
    private CheckBox mShouldStoreView;
    private CheckBox mAutoLoginView;
    private View mProgressView;
    private View mLoginFormView;

    private String mError = "";
    private User mUser = null;

    Context c = null;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_login);
        c = getApplicationContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        boolean shouldStore = settings.getBoolean("shouldStore", false);
        boolean autologin = settings.getBoolean("autoLogin", false);
        String userName = settings.getString("userName","");
        String userPass = settings.getString("userPass","");

        CONNECTION_TIMEOUT = Integer.parseInt(settings.getString("networkTimeout","4000"));
        // Set up the login form.
        mLoginView = (TextView) findViewById(R.id.login);
        mPasswordView = (EditText) findViewById(R.id.password);
        mShouldStoreView = (CheckBox) findViewById(R.id.checkboxRemember);
        mAutoLoginView = (CheckBox) findViewById(R.id.checkboxAutologin);

        mShouldStoreView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mAutoLoginView.setEnabled(true);
                } else {
                    mAutoLoginView.setChecked(false);
                    mAutoLoginView.setEnabled(false);
                }
            }
        });

        if(shouldStore) {
            mLoginView.setText(userName);
            mPasswordView.setText(userPass);
            mShouldStoreView.setChecked(shouldStore);
            mAutoLoginView.setEnabled(true);
            mAutoLoginView.setChecked(autologin);
        }


        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        if(autologin) {
            attemptLogin();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mLoginView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String login = mLoginView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address or username
        if (mLoginView.getText().length() == 0) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(login, password, getApplicationContext());
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = settings.edit();

        if(mShouldStoreView.isChecked()) {
            editor.putBoolean("shouldStore", true);
            editor.putString("userName", mLoginView.getText().toString());
            editor.putString("userPass", mPasswordView.getText().toString());
            if(mAutoLoginView.isChecked()) {
                editor.putBoolean("autoLogin", true);
            } else {
                editor.putBoolean("autoLogin", false);
            }
        } else {
            editor.putBoolean("shouldStore", false);
            editor.putString("userName", "");
            editor.putString("userPass", "");
            editor.putBoolean("autoLogin", false);
        }
        editor.commit();
    }

    public void forceCrash(View view) {
        throw new RuntimeException("This is a crash");
    }



    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        Context context;

        UserLoginTask(String username, String password, Context cont) {
            mUsername = username;
            mPassword = password;
            context = cont;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpsURLConnection client = null;
            URL url = null;

            // initiale SSL
            SSLContext sc = null;

            try {
                sc = SSLContext.getInstance("TLS");
                sc.init(null, null, new java.security.SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                mError = getString(R.string.error_ssl_failed);
                return false;
            } catch (KeyManagementException e) {
                e.printStackTrace();
                mError = getString(R.string.error_ssl_failed);
                return false;
            }

            //String paramStr = "&user=Jacob&password=123456";
            //String paramStr = "?option=com_ajax&module=tvm&method=CheckUserLogin&data%5Buser%5D=Jacob&data%5Bpassword%5D=123456&format=raw";
            String paramStr = null;
            try {
                paramStr = "name="+ URLEncoder.encode(mUsername, "UTF-8") + "&pass="+ URLEncoder.encode(mPassword,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                url = new URL("https://rostockerseebaeren.de/?option=com_tvm&task=checkUserAccess&format=json");
                client = (HttpsURLConnection)url.openConnection();
                client.setConnectTimeout(CONNECTION_TIMEOUT);
                client.setReadTimeout(CONNECTION_TIMEOUT);
                client.setSSLSocketFactory(sc.getSocketFactory());
                client.setRequestMethod("POST");
                client.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                client.setRequestProperty("Content-Length", "" + Integer.toString(paramStr.getBytes().length));
                client.setRequestProperty("Content-Language", "en-US");

                client.setUseCaches (false);
                client.setDoInput(true);
                client.setDoOutput(true);

                //client.setChunkedStreamingMode(0);
            } catch (Exception e) {
                e.printStackTrace();
                mError = getString(R.string.error_no_internet);
                Toast.makeText(getApplicationContext(),getString(R.string.error_no_internet), Toast.LENGTH_LONG);
            }
            try {
                //paramStr = URLEncoder.encode(paramStr,"UTF-8");
                DataOutputStream wr = new DataOutputStream(client.getOutputStream());
                wr.write(paramStr.getBytes("UTF-8"));
                wr.flush();
                wr.close();

                int responseCode = client.getResponseCode();
                String responseMessage = client.getResponseMessage();
                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + paramStr);
                System.out.println("Response Code : " + responseCode);
                System.out.println("Response Message : " + responseMessage);

            } catch (Exception e){
                mError = getString(R.string.error_no_internet);
                e.printStackTrace();
            }

            StringBuffer response = new StringBuffer();
            int lines = 0;
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine).append("\r");
                    lines++;
                }
                in.close();
            } catch (FileNotFoundException e) {
                mError = getString(R.string.error_no_internet);
                e.printStackTrace();
                return false;
            } catch (IOException ioe){
                mError = getString(R.string.error_no_internet);
                ioe.printStackTrace();
                return false;
            }

            if(response.charAt(0) == '0'){
                mError = getString(R.string.error_incorrect_password);
                return false;
            } else {
                if(mUser != null) {
                    mUser = null;
                }
                mUser = new User();
                mUser.setUser(response.toString());
                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            if (success) {
                mPasswordView.setError(null);
                Intent starter = new Intent(c, NextvmActivity.class);
                starter.putExtra("user", mUser);
                starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(starter);
            } else {
                mPasswordView.setError(mError);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

