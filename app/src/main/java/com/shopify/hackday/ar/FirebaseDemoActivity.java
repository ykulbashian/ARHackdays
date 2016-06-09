package com.shopify.hackday.ar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by kanawish on 2016-06-09.
 */

public class FirebaseDemoActivity extends AppCompatActivity {

    private static final String TAG = FirebaseDemoActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_demo);

        Button readButton = (Button) findViewById(R.id.readButton);
        Button writeButton = (Button) findViewById(R.id.writeButton);

        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        DatabaseReference ref = instance.getReference("test");

        readButton.setOnClickListener(v -> {
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String value = dataSnapshot.getValue(String.class);
                    Log.i(TAG, String.format("One shot call got %s!",value));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, ":onCancelled", databaseError.toException());
                }
            });
        });

        writeButton.setOnClickListener(v -> ref.setValue("Hello world!"));

    }

}
