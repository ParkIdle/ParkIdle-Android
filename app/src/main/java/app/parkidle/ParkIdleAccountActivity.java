package app.parkidle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.ButterKnife;


public class ParkIdleAccountActivity extends AppCompatActivity {
    public static FirebaseAuth mAuth;
    public EditText email;
    public EditText password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_idle_account);

        mAuth = FirebaseAuth.getInstance();

        EditText email = findViewById(R.id.input_email);
        EditText password = findViewById(R.id.input_password);

        findViewById(R.id.btn_parkidle_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btn_parkidle_signup:
                        signupActivity();
                        break;
                }
            }
        });

        findViewById(R.id.btn_parkidle_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btn_parkidle_login:
                        handleLogin();
                        break;
                }
            }
        });
    }

    private void signupActivity(){
        Intent i = new Intent(this,SignUpActivity.class);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //FirebaseUser currentuser = mAuth.getCurrentUser();


    }

    private void handleLogin(){
        String email1 = email.getText().toString();
        String password1 = password.getText().toString();

        if(email1.equals("") ||password1.equals("") ){
            Toast.makeText(this, "EMAIL OR PASSWORD FIELD EMPTY", Toast.LENGTH_SHORT).show();
            //email.clearComposingText();
            email.setText("");
            password.setText("");
        }
        else{





        }
    }
}
