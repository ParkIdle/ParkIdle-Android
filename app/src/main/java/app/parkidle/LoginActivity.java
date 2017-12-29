package app.parkidle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private final String OAuthUriFacebook = "https://parkidle-186720.firebaseapp.com/__/auth/handler";
    private final String idAppFacebook = "156734918407060";
    private final String appSecretByFacebook = "803ca34aed8c4311022bbb8ba3e2bbbc";
    private final String clientIdByServer = "857680111540-b81g5usqot58ebdslmotn8ou53n3v926.apps.googleusercontent.com";
    private final String clientPasswordByServer = "6MSQmXxyeW-TaccqdbkIngHf";
    private final int RC_SIGN_IN_GOOGLE = 1;
    private final int RC_SIGN_IN_FACEBOOK = 2;
    private GoogleSignInOptions google_options;
    public static GoogleSignInClient googleSignIn;

    public static FacebookAuthProvider facebookSignIn;
    private GoogleSignInAccount account;
    private final String TAG = "LoginActivity";
    public final static String EXTRA_ACCOUNT = "app.parkidle.account";
    public static FirebaseAuth mAuth;
    private FirebaseApp mApp;
    public static FirebaseUser currentUser;
    private GoogleSignInAccount currentAccount;
    public static GoogleApiClient mGoogleApiClient;
    private CallbackManager mCallbackManager;

    private static boolean withGoogle;
    private static boolean withFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);

        //inizialiting the facebook options by the id provided from firebase
        Log.w("helper","i'm here dude_main_inizialiting");

        mCallbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email","public_profile");
        Log.w("helper","i'm here dude_after inizialiting");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.w("FACEBOOK CALLBACK","hey its on success");
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.w("FACEBOOK CALLBACK","hey its on cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.w("FACEBOOK CALLBACK",error.toString());
            }

        });


        //inizialiting the google options by the id provided from firebase
        // Configure Google Sign In
        google_options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientIdByServer)
                .requestEmail()
                .build();
        googleSignIn = GoogleSignIn.getClient(this,google_options);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, google_options)
                .build();
        mGoogleApiClient.connect();

        mAuth = FirebaseAuth.getInstance();


        //starting the listener of the button
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.sign_in_button:
                        signIn_google();
                        break;
                }
            }
        });

    }//qua finisce on create

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        //currentAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(currentUser != null)
            updateUI(currentUser);
    }

    private void signIn_google() {

        Intent signInIntent_google = googleSignIn.getSignInIntent();
        startActivityForResult(signInIntent_google, RC_SIGN_IN_GOOGLE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //currentAccount = account;
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                //Log.w(TAG, "Google sign in failed" + e.getStatusCode());
                Toast.makeText(this, "Google sign in failed" + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                Log.w("GOOGLE SIGN IN: ",e.getStatusCode()+ " -> " + e.toString());
                // ...
            }
        }else{
            mCallbackManager.onActivityResult(requestCode,resultCode,data);
        }


    }


    private void updateUI(FirebaseUser user){
        if(user == null){
            Toast.makeText(this, "Account null", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            currentUser = user;
            //onPause();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        withGoogle = true;
        withFacebook = false;
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        withFacebook = true;
        withGoogle = false;
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);

                        }

                        // ...
                    }
                });
    }


    public static boolean isWithGoogle() {

        return withGoogle;
    }

    public static boolean isWithFacebook(){
        return withFacebook;
    }

    public static FirebaseUser getUser(){
        return currentUser;
    }



}
