package com.shopify.hackday.ar.model;

/**
 */
public class Product {
    Long id ;
    byte[] objectMesh ;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getObjectMesh() {
        return objectMesh;
    }

    public void setObjectMesh(byte[] objectMesh) {
        this.objectMesh = objectMesh;
    }
}
