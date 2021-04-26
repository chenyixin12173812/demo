package com.patterndesign.factory.factorymethod;

import com.patterndesign.factory.simple.GreekPizza;
import com.patterndesign.factory.simple.Pizza;

public class GreekFactory extends PizzaFactory {
    @Override
    protected Pizza createPize() {
        return new GreekPizza();
    }
}
