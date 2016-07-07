package com.tal.wannatalk;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

    private View availableContactsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        System.out.println("Talllllll");
        boolean isRegistered = intent.getBooleanExtra(WelcomeActivity.IS_REGISTERED, false);
        if (isRegistered) {
//            availableContactsView = findViewById(R.id.availableContacts);
//            TextView textView = new TextView(this);
//            textView.setTextSize(40);
//            textView.setText("TEst 12344");
//
//            RelativeLayout layout = (RelativeLayout) findViewById(R.id.availableContacts);
//            layout.addView(textView);

        } else {
            Intent intentRegister = new Intent(this, RegisterActivity.class);
            startActivity(intentRegister);
        }
    }



}
