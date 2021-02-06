package com.chenyixin.springboot.entity;

import java.io.Serializable;

/**
 * @description: 商品
 */
public class Goods implements Serializable {

    private Long id;

    private String name;

    private Double price;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Goods{" + "id=" + id + ", name='" + name + '\'' + ", price=" + price + '}';
    }
}
