package com.shopify.hackday.ar.model;


import android.util.Base64;

/**
 * Created by meganboudreau on 2016-06-09.
 */

public class ObjectMesh {

    //private String id;    //shop_id, product_id concatenated
    private String mesh;

    public ObjectMesh(){}

//    public ObjectMesh(String id, String mesh){
//        //this.id = id;
//        this.mesh = mesh;
//    }

    public ObjectMesh(String mesh){
        this.mesh = mesh;
    }
    private byte[] meshStringToBytes(String meshString){
         byte[] bytes = Base64.decode(meshString, Base64.DEFAULT);
         return bytes;
    }

}
