package app.parkidle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class LoginActivity extends AppCompatActivity {
    private final String clientIdByServer = "857680111540-b81g5usqot58ebdslmotn8ou53n3v926.apps.googleusercontent.com";
    private final String clientPasswordByServer = "6MSQmXxyeW-TaccqdbkIngHf";
    private GoogleSignInOptions google_options;
    private GoogleSignInClient googleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        google_options = new GoogleSignInOptions.Builder()
                .requestIdToken(clientIdByServer)
                .build();
        googleSignIn = GoogleSignIn.getClient(this,google_options);

    }
    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

    }
}
