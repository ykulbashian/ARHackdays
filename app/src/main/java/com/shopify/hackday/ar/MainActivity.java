package com.shopify.hackday.ar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private FirebaseManager fbManager;

    //hardcoded objects for testing
    String objectId = "12344";
    String meshString = "Mesh test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fbManager = new FirebaseManager();

        fbManager.writeNewObject(objectId, meshString);


    }
}
