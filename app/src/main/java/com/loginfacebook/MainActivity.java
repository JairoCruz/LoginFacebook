package com.loginfacebook;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.loginfacebook.pojo.User;
import com.loginfacebook.util.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {


    CallbackManager callbackManager;
    Button share;
    Button details;
    ShareDialog shareDialog;
    LoginButton login;
    ProfilePictureView profilePictureView;
    Dialog details_dialog;
    TextView details_txt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        callbackManager = CallbackManager.Factory.create();
        login = (LoginButton) findViewById(R.id.login_button);
        profilePictureView = (ProfilePictureView) findViewById(R.id.picture);
        shareDialog = new ShareDialog(this);
        share = (Button) findViewById(R.id.share);
        details = (Button) findViewById(R.id.details);

        login.setReadPermissions("public_profile email");
        share.setVisibility(View.INVISIBLE);
        details.setVisibility(View.INVISIBLE);
        details_dialog = new Dialog(this);
        details_dialog.setContentView(R.layout.activity_dialog_details);
        details_dialog.setTitle("Details");
        details_txt = (TextView) details_dialog.findViewById(R.id.details);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                details_dialog.show();
            }
        });

        if(AccessToken.getCurrentAccessToken() != null){
            RequestData();
            share.setVisibility(View.VISIBLE);
            details.setVisibility(View.INVISIBLE);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AccessToken.getCurrentAccessToken() != null){
                    share.setVisibility(View.INVISIBLE);
                    details.setVisibility(View.INVISIBLE);
                    profilePictureView.setProfileId(null);
                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareLinkContent content = new ShareLinkContent.Builder().build();
                shareDialog.show(content);
            }
        });

        login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if(AccessToken.getCurrentAccessToken() != null){
                    RequestData();
                    share.setVisibility(View.VISIBLE);
                    details.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void RequestData() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                JSONObject json = response.getJSONObject();
                try{
                    if(json != null){
                        String text = "<b>Name: </b>" + json.getString("name") + "<br><br><b>Email: </b>" + json.getString("email") + "<br><br><b>Profile link: </b>" + json.getString("link");
                        details_txt.setText(Html.fromHtml(text));
                        profilePictureView.setProfileId(json.getString("id"));
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    /*private CallbackManager callbackManager;
    private LoginButton loginButton;
    private TextView btnLogin;
    private ProgressDialog progressDialog;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);


        if(PrefUtils.getCurrentUser(MainActivity.this) != null){
            Intent homeIntent = new Intent(MainActivity.this, LogoutActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile","email","user_friends");
        btnLogin = (TextView) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Loading...");
                progressDialog.show();
                loginButton.performClick();
                loginButton.setPressed(true);
                loginButton.invalidate();
                loginButton.registerCallback(callbackManager, mCallBack);
                loginButton.setPressed(false);
                loginButton.invalidate();
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private FacebookCallback<LoginResult> mCallBack = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            progressDialog.dismiss();

            GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    Log.e("response: ", response + "");
                    try{
                        user = new User();
                        user.facebookID = object.getString("id").toString();
                        user.email = object.getString("email").toString();
                        user.name = object.getString("name").toString();
                        user.gender = object.getString("gender").toString();
                        PrefUtils.setCurrentUser(user,MainActivity.this);

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    Toast.makeText(MainActivity.this, "Bienvenido " + user.name, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, LogoutActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,birthday");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {
            progressDialog.dismiss();
        }

        @Override
        public void onError(FacebookException error) {
            progressDialog.dismiss();
        }
    };*/
}
