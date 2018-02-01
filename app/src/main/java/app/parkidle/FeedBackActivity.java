package app.parkidle;

import android.provider.ContactsContract;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;


public class FeedBackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        TextInputLayout box_parere = findViewById(R.id.textInputLayout);
        TextView titolo = findViewById(R.id.textView5);
        titolo.setText("Salve! Ti ringraziamo per voler contribuire al migliorare la nostra applicazione!  In questo semplice form potete inserire problemi o eventuali bug che avete riscontrato durante l'utilizzo dell'applicazione, questo aiuterà noi a migliorare sempre più la qualità del servizio!");

        String parere = box_parere.getEditText().toString();





    }
}
