package com.shopify.hackday.ar;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shopify.hackday.ar.model.ObjectMesh;

import java.util.List;

/**
 * Created by meganboudreau on 2016-06-09.
 */


public class FirebaseManager {

    private static final String TAG = "ShopifyAR";
    private DatabaseReference mDatabase;

    //This will write a new object to the Firebase database at a specified ID
    public void writeNewObject(String id, String mesh) {
        ObjectMesh obj = new ObjectMesh(mesh);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //I'm assuming it's going to make a json with objects, id, object
        mDatabase.child(id).setValue(obj);
    }

//    //parentNode = id?
//    private ObjectMesh fetchObjectMesh(String id){
//        ObjectMesh obj;
//        mDatabase.child("objects").child(id).addListenerForSingleValueEvent(
//                new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        // Get user value
//                        Object obj = dataSnapshot.getValue(ObjectMesh.class);
//                        //User user = dataSnapshot.getValue(User.class);
//
//                        // ...
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
//                    }
//                });
//
//        return obj;
//    }


}
