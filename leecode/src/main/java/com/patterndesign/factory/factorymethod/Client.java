package com.patterndesign.factory.factorymethod;


public class Client {
    public static void main(String[] args) {
        CheeseFactory cheese = new CheeseFactory();
        cheese.oderPizza();

        GreekFactory greekFactory = new GreekFactory();
        greekFactory.oderPizza();
    }
}
