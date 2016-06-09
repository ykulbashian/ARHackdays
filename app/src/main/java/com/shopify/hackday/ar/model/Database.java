package com.shopify.hackday.ar.model;

import java.util.Map;

/**
 */
public class Database {
    Map<Long,Shop> shops ;

    public Map<Long, Shop> getShops() {
        return shops;
    }

    public void setShops(Map<Long, Shop> shops) {
        this.shops = shops;
    }
}
