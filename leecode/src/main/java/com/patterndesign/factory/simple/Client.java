package com.patterndesign.factory.simple;

public class Client {

    public static void main(String[] args) {
        SimplePizzaFactory simplePizzaFactory = new SimplePizzaFactory();
        Pizza greek = simplePizzaFactory.oderPizza("greek");
    }
}
