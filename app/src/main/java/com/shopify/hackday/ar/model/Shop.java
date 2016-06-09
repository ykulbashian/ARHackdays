package com.shopify.hackday.ar.model;

import java.util.Map;

public class Shop {
    Long id ;
    Map<Long,Product> products ;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<Long, Product> getProducts() {
        return products;
    }

    public void setProducts(Map<Long, Product> products) {
        this.products = products;
    }
}
