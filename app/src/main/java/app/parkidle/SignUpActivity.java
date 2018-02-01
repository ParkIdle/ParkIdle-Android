package app.parkidle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
    public  static FirebaseAuth mauth;
    public static FirebaseUser currentuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mauth = FirebaseAuth.getInstance();

        EditText email = findViewById(R.id.input_e_signup);
        EditText password = findViewById(R.id.input_p_signup);

        String email_compilata = email.toString();
        String password_compilata = password.toString();

    }

    @Override
    protected void onStart() {
        super.onStart();
        currentuser = mauth.getCurrentUser();
        if (currentuser != null)
            updateUI(currentuser);
    }

    private void updateUI(FirebaseUser user){
        if(user == null){
            Toast.makeText(this, "Account null", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            currentuser = user;
        }
    }
}
