package familyconnect.familyconnect;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.util.Log;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import familyconnect.familyconnect.json.FamilyConnectHttpResponse;

/**
 * UserLoginActivity.java - a class that provides functionality for the user to login.
 *
 * @author  Jawan Higgins
 * @version 1.0
 * @created 2017-11-23
 */
public class UserLoginActivity extends AppCompatActivity {

    private static final int REQUEST_SIGNUP = 0;

    private static EditText emailText;
    private EditText passwordText;
    private Button loginButton;
    private TextView signupLink;
    private RequestQueue queue;
    private boolean GET, POST = false;
    private static int ID;
    private static String TOKEN;
    private static String username;


    /**
     * @method onCreate()
     *
     * This method creates the android activity and initializes each instance variable.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        queue = Volley.newRequestQueue(this);

        emailText = findViewById(R.id.input_email);
        passwordText = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.btn_login);
        signupLink = findViewById(R.id.link_signup);

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), UserSignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }


    /**
     * @method login()
     *
     * This method that sends a request for the user to login.
     */
    public void login() {

        if (!validate()) {

            return;
        }

        loginButton.setEnabled(false);

        UserLoginActivity.FamilyConnectFetchTask task = new UserLoginActivity.FamilyConnectFetchTask();
        String uriPost ="https://family-connect-ggc-2017.herokuapp.com/sessions";
        task.execute(uriPost);
        POST = true;
        GET = false;

    }


    /**
     * @method onBackPressed()
     *
     * This method responds when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        //Do Nothing
    }


    /**
     * @method onLoginSuccess()
     *
     * This method sets the login button back to enabled.
     */
    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        finish();
    }

    /**
     * @method onLoginFailed()
     *
     * This method resets the password field and displays a failed message.
     */
    public void onLoginFailed() {
        passwordText.setText("");

        Toast.makeText(getBaseContext(), "Login Failed", Toast.LENGTH_LONG).show();

        loginButton.setEnabled(true);
    }

    /**
     * @method validate()
     *
     * This method validates the user's credentials to login.
     *
     * @return
     */
    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty()) {
            passwordText.setError("Please enter a password");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    /**
     * @class FamilyConnectFetchTask
     *
     * This class performs an Async Task that calls the Restful Api
     *
     */
    private class FamilyConnectFetchTask extends AsyncTask<String, Void, Bitmap> {

        //Converts JSON string into a Activity object
        private FamilyConnectHttpResponse getTask(String json){
            Gson gson = new GsonBuilder().create();

            if(json.charAt(0) == ','){
                json = json.substring(1);
            }
            FamilyConnectHttpResponse user = gson.fromJson(json, FamilyConnectHttpResponse.class);
            return user;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            Log.v("FamilyConnect", "URL = " + params[0]);

            //POST REQUEST
            if (POST) {

                StringRequest postRequest = new StringRequest(Request.Method.POST, params[0],
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(final String response) {

                                String jsonRequest [ ] = response.split(",");

                                ID = Integer.parseInt(jsonRequest[0].substring(6, jsonRequest[0].toString().length()));
                                TOKEN = jsonRequest[2].substring(24, jsonRequest[2].toString().length()-2);


                                Log.v("ID", jsonRequest[0].substring(6, jsonRequest[0].toString().length()));
                                Log.v("TOKEN", jsonRequest[2].substring(24, jsonRequest[2].toString().length()-2));

                                Log.d("POST REQUEST", response);


                                final ProgressDialog progressDialog = new ProgressDialog(UserLoginActivity.this,
                                        R.style.AppTheme_PopupOverlay);
                                progressDialog.setIndeterminate(true);
                                progressDialog.setMessage("Signing in...");
                                progressDialog.show();

                                new android.os.Handler().postDelayed(
                                        new Runnable() {
                                            public void run() {

                                                if(((Integer) ID != null) && (TOKEN != null)) {
                                                    UserLoginActivity.FamilyConnectFetchTask task = new UserLoginActivity.FamilyConnectFetchTask();
                                                    String uriGet ="https://family-connect-ggc-2017.herokuapp.com/users";
                                                    task.execute(uriGet);
                                                    POST = false;
                                                    GET = true;
                                                }
                                                else {
                                                    onLoginFailed();
                                                }

                                                progressDialog.dismiss();
                                            }
                                        }, 3000);
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                onLoginFailed();

                                Log.d("Error.Response", "" + error);
                            }
                        }
                ) {

                    //PASS PARAMETERS
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("email", emailText.getText().toString().toLowerCase());
                        params.put("password", passwordText.getText().toString());

                        return params;
                    }
                };
                queue.add(postRequest);
            }


            //GET REQUEST FOR USERNAME
            if (GET) {

                final JSONObject jsonObject = new JSONObject();

                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, params[0] + "/" + ID, jsonObject,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {

                                try {
                                    username = response.getString("user_name");

                                    Intent homePage = new Intent(UserLoginActivity.this, GroupedActivities.class);
                                    UserLoginActivity.this.startActivity(homePage);

                                    onLoginSuccess();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                onLoginFailed();
                                Log.d("Error.Response", error.toString());
                            }
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json");
                        headers.put("X-User-Email", emailText.getText().toString().toLowerCase());
                        headers.put("X-User-Token", TOKEN);

                        return headers;
                    }
                };
                queue.add(getRequest);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

        }
    }

    public static String getToken() {
        return TOKEN;
    }

    public static String getEmail() {
        return emailText.getText().toString();
    }

    public static int getID() { return ID; }

    public static String getUsername() {
        return username;
    }
}
