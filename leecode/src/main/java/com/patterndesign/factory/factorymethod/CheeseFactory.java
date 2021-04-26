package com.patterndesign.factory.factorymethod;

import com.patterndesign.factory.simple.CheesePizza;
import com.patterndesign.factory.simple.Pizza;

public class CheeseFactory extends PizzaFactory {

    @Override
    protected Pizza createPize() {
        return new CheesePizza();
    }
}
