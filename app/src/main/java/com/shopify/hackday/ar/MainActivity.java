package com.shopify.hackday.ar;

import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;

import com.shopify.hackday.ar.vuforia.app.VirtualButtons.VirtualButtons;


public class MainActivity extends VirtualButtons {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
    }
}
