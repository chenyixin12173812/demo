package com.patterndesign.factory.factorymethod;

import com.patterndesign.factory.simple.Pizza;

abstract class PizzaFactory {
    public Pizza oderPizza() {
        Pizza pizza = createPize();
        pizza.prepare();
        pizza.bake();
        pizza.cut();
        pizza.box();
        return pizza;
    }

     protected  abstract Pizza createPize();

}
