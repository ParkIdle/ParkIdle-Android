package app.parkidle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private final String clientIdByServer = "857680111540-b81g5usqot58ebdslmotn8ou53n3v926.apps.googleusercontent.com";
    private final String clientPasswordByServer = "6MSQmXxyeW-TaccqdbkIngHf";
    private final int RC_SIGN_IN = 1;
    private GoogleSignInOptions google_options;
    private GoogleSignInClient googleSignIn;
    private GoogleSignInAccount account;
    private final String TAG = "LoginActivity";
    public final static String EXTRA_ACCOUNT = "app.parkidle.account";
    private FirebaseAuth mAuth;
    private FirebaseApp mApp;
    private FirebaseUser currentUser;
    private GoogleSignInAccount currentAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);


        //inizialiting the google options by the id provided from firebase
        // Configure Google Sign In
        google_options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientIdByServer)
                .build();
        googleSignIn = GoogleSignIn.getClient(this,google_options);


        mAuth = FirebaseAuth.getInstance();


        //starting the listener of the button
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.sign_in_button:
                        signIn();
                        break;

                        //continua con altri casi
                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        currentAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(currentAccount == null){
            signIn();
        }else updateUI(currentUser);
    }

    private void signIn() {
        Intent signInIntent = googleSignIn.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                //Log.w(TAG, "Google sign in failed" + e.getStatusCode());
                Toast.makeText(this, "Google sign in failed" + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    /*private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount newAccount = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(newAccount);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "signInResult:failed code=" + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            //firebaseAuthWithGoogle(null);
        }
    }*/

    private void updateUI(FirebaseUser user){
        /*if(user == null){
            //Toast.makeText(this, "Account null", Toast.LENGTH_SHORT).show();
            return;
        }*/
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_ACCOUNT, new String[]{user.getDisplayName(),user.getEmail(),user.getPhotoUrl().toString()});
        startActivity(intent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

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


}
