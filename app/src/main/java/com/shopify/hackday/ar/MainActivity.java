package com.shopify.hackday.ar;

import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;

import com.shopify.hackday.ar.vuforia.app.VirtualButtons.VirtualButtonsActivity;


public class MainActivity extends VirtualButtonsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
    }
}
