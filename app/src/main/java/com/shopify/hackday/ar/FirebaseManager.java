package com.shopify.hackday.ar;

import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by meganboudreau on 2016-06-09.
 */


public class FirebaseManager {

    private static final String TAG = FirebaseManager.class.getSimpleName();

    private DatabaseReference mDatabase;

    private String objectId;
    private String meshString = "";

    public FirebaseManager(String objectId){
        this.objectId = objectId;
    }

    public String getMeshString() { return meshString; }


    public DatabaseReference getDBReference(String key){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(key);
        return dbRef;
    }

    protected DatabaseReference getDBReference(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        return dbRef;
    }

    public void writeNewObject(String id, String meshString) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(id).setValue(meshString);
    }

}