package com.tal.wannatalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
    ContactsFragment.OnContactsInteractionListener {

    private View availableContactsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        System.out.println("Talllllll");
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean isRegistered = sharedPref.getBoolean(getString(R.string.is_registered), false);
        if (isRegistered) {
            Intent bgServiceIntent = new Intent(getBaseContext(),BgService.class);
            getBaseContext().startService(bgServiceIntent);
            Log.i("BgService", "started");
//            availableContactsView = findViewById(R.id.availableContacts);
//            TextView textView = new TextView(this);
//            textView.setTextSize(40);
//            textView.setText("TEst 12344");

            ContactsFragment mContactsListFragment = (ContactsFragment)
                    getSupportFragmentManager().findFragmentById(R.id.contacts_list);


        } else {
            Intent intentRegister = new Intent(this, RegisterActivity.class);
            startActivity(intentRegister);
        }
    }


    @Override
    public void onContactSelected(Uri contactUri) {
        //start a new ContactDetailActivity with the contact Uri
        Intent intent = new Intent(this, ContactDetailActivity.class);
        intent.setData(contactUri);
        startActivity(intent);
    }

    @Override
    public void onSelectionCleared() {


    }
}
